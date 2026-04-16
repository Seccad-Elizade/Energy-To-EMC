package com.seccad_elizade.energytoemc.block.entity;

import com.seccad_elizade.energytoemc.item.ConversionUpgradeItem;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import com.seccad_elizade.energytoemc.screen.EmcConverterMenu;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.tile.IEmcStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmcConverterBlockEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity, IEmcStorage {

    private static final int FE_PER_EMC_BASE = 500;
    private final long MAX_EMC = 100000L;
    private long emcStored = 0;

    // Smoothing Logic (Phosphor Font Support)
    private int energyUsage = 0;
    private int emcProduction = 0;
    private final int[] feHistory = new int[20];
    private final int[] emcHistory = new int[20];
    private int historyIndex = 0;

    private int redstoneMode = 0;

    private final EnergyStorage energy = new EnergyStorage(1000000, 100000, 100000);

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof ConversionUpgradeItem;
        }
    };

    // FIXED: Bridges 64-bit EMC and smoothing data to the Client
    protected final IIntArray data = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0: return energy.getEnergyStored() & 0xffff;
                case 1: return (energy.getEnergyStored() >> 16) & 0xffff;
                case 2: return (int) (emcStored & 0xffff);
                case 3: return (int) ((emcStored >> 16) & 0xffff);
                case 4: return (int) ((emcStored >> 32) & 0xffff);
                case 5: return (int) ((emcStored >> 48) & 0xffff);
                case 10: return redstoneMode;
                case 11: return energyUsage;
                case 12: return emcProduction;
                default: return 0;
            }
        }
        @Override
        public void set(int index, int value) {
            if (index == 10) redstoneMode = value;
        }
        @Override
        public int getCount() { return 13; }
    };

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEmcStorage> emcCap = LazyOptional.of(() -> this);

    public EmcConverterBlockEntity() {
        super(ModBlockEntities.EMC_CONVERTER_BE.get());
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;

        int feUsedThisTick = 0;
        int emcProducedThisTick = 0;

        // 1. Redstone Check
        if (this.redstoneMode == 2 || (this.redstoneMode == 1 && !level.hasNeighborSignal(worldPosition))) {
            updateSmoothing(0, 0);
            return;
        }

        // 2. Neighbor Interaction (Energy Pull)
        for (Direction dir : Direction.values()) {
            TileEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor != null) {
                neighbor.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite()).ifPresent(neighborEnergy -> {
                    if (neighborEnergy.canExtract()) {
                        int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
                        if (space > 0) {
                            int pulled = neighborEnergy.extractEnergy(Math.min(space, 50000), false);
                            energy.receiveEnergy(pulled, false);
                        }
                    }
                });
            }
        }

        // 3. Processing Logic (FE -> EMC)
        ItemStack upgradeStack = itemHandler.getStackInSlot(0);
        int multiplier = 1;
        int tier = 0;
        if (upgradeStack.getItem() instanceof ConversionUpgradeItem) {
            ConversionUpgradeItem upgrade = (ConversionUpgradeItem) upgradeStack.getItem();
            multiplier = upgrade.getProfitMultiplier();
            tier = upgrade.getTier();
        }

        if (energy.getEnergyStored() >= FE_PER_EMC_BASE && emcStored < MAX_EMC) {
            int unitsPerTick = (int) (10 * Math.pow(2, tier));
            int availableUnits = energy.getEnergyStored() / FE_PER_EMC_BASE;
            int unitsToProcess = Math.min(availableUnits, unitsPerTick);

            long spaceLeft = MAX_EMC - emcStored;
            long potentialEmc = (long) unitsToProcess * multiplier;

            if (potentialEmc > spaceLeft) {
                potentialEmc = spaceLeft;
                unitsToProcess = (int) (potentialEmc / multiplier);
            }

            if (unitsToProcess > 0) {
                feUsedThisTick = unitsToProcess * FE_PER_EMC_BASE;
                energy.extractEnergy(feUsedThisTick, false);
                emcStored += (long) unitsToProcess * multiplier;
                emcProducedThisTick = (int) (unitsToProcess * multiplier);
                setChanged();
            }
        }

        updateSmoothing(feUsedThisTick, emcProducedThisTick);
    }

    private void updateSmoothing(int currentFe, int currentEmc) {
        feHistory[historyIndex] = currentFe;
        emcHistory[historyIndex] = currentEmc;
        historyIndex = (historyIndex + 1) % 20;

        long totalFe = 0;
        long totalEmc = 0;
        for (int i = 0; i < 20; i++) {
            totalFe += feHistory[i];
            totalEmc += emcHistory[i];
        }

        this.energyUsage = (int) (totalFe / 20);
        this.emcProduction = (int) (totalEmc / 20);
    }

    @Override public long insertEmc(long amount, EmcAction action) { return 0; }

    // FIXED: Extraction Logic (Prevents infinite EMC from pipes)
    @Override
    public long extractEmc(long amount, EmcAction action) {
        long toExtract = Math.min(amount, this.emcStored);
        if (action.execute()) {
            this.emcStored -= toExtract;
            setChanged();
        }
        return toExtract;
    }

    @Override public long getStoredEmc() { return emcStored; }
    @Override public long getMaximumEmc() { return MAX_EMC; }
    @Override public ITextComponent getDisplayName() { return new StringTextComponent("EMC Converter"); }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.energy.receiveEnergy(tag.getInt("energy") - energy.getEnergyStored(), false);
        this.emcStored = tag.getLong("emc");
        this.redstoneMode = tag.getInt("redstoneMode");
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.putInt("energy", energy.getEnergyStored());
        tag.putLong("emc", emcStored);
        tag.putInt("redstoneMode", redstoneMode);
        tag.put("inventory", itemHandler.serializeNBT());
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) return energyCap.cast();
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return itemCap.cast();
        if (cap == ProjectEAPI.EMC_STORAGE_CAPABILITY) return emcCap.cast();
        return super.getCapability(cap, side);
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inv, PlayerEntity p) {
        return new EmcConverterMenu(id, inv, this, this.data);
    }

    @Override public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) { this.load(getBlockState(), pkt.getTag()); }
    @Override public CompoundNBT getUpdateTag() { return this.save(new CompoundNBT()); }

    public ItemStackHandler getItemHandler() { return this.itemHandler; }
}