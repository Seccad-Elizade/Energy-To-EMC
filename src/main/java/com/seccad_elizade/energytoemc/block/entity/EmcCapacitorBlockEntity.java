package com.seccad_elizade.energytoemc.block.entity;

import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import com.seccad_elizade.energytoemc.screen.EmcCapacitorMenu;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.tile.IEmcStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmcCapacitorBlockEntity extends TileEntity implements IEmcStorage, INamedContainerProvider, ITickableTileEntity {
    private long storedEmc = 0;
    private long capacity = 100000L;

    private final EnergyStorage energyStorage = new EnergyStorage(1000000) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) setChanged();
            return extracted;
        }
    };

    protected final IIntArray data = new IIntArray() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0: return (int) (storedEmc & 0xFFFF);
                case 1: return (int) ((storedEmc >> 16) & 0xFFFF);
                case 2: return (int) ((storedEmc >> 32) & 0xFFFF);
                case 3: return (int) ((storedEmc >> 48) & 0xFFFF);
                case 4: return (int) (capacity & 0xFFFF);
                case 5: return (int) ((capacity >> 16) & 0xFFFF);
                case 6: return (int) ((capacity >> 32) & 0xFFFF);
                case 7: return (int) ((capacity >> 48) & 0xFFFF);
                default: return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: storedEmc = (storedEmc & ~0xFFFFL) | (value & 0xFFFFL); break;
                case 1: storedEmc = (storedEmc & ~(0xFFFFL << 16)) | ((value & 0xFFFFL) << 16); break;
                case 2: storedEmc = (storedEmc & ~(0xFFFFL << 32)) | ((value & 0xFFFFL) << 32); break;
                case 3: storedEmc = (storedEmc & ~(0xFFFFL << 48)) | ((value & 0xFFFFL) << 48); break;
                case 4: capacity = (capacity & ~0xFFFFL) | (value & 0xFFFFL); break;
                case 5: capacity = (capacity & ~(0xFFFFL << 16)) | ((value & 0xFFFFL) << 16); break;
                case 6: capacity = (capacity & ~(0xFFFFL << 32)) | ((value & 0xFFFFL) << 32); break;
                case 7: capacity = (capacity & ~(0xFFFFL << 48)) | ((value & 0xFFFFL) << 48); break;
            }
        }

        @Override
        public int getCount() { return 8; }
    };

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);
    private final LazyOptional<IEmcStorage> emcCap = LazyOptional.of(() -> this);

    public EmcCapacitorBlockEntity() {
        super(ModBlockEntities.EMC_CAPACITOR_BE.get());
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    private boolean isCreative() {
        return this.capacity >= Long.MAX_VALUE || this.capacity < 0;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;

        if (isCreative()) {
            if (this.storedEmc != Long.MAX_VALUE) {
                this.storedEmc = Long.MAX_VALUE;
                setChanged();
            }
        }
    }



    @Override
    public long insertEmc(long amount, EmcAction action) {
        if (isCreative()) return amount;
        long canInsert = Math.min(amount, capacity - storedEmc);
        if (action.execute() && canInsert > 0) {
            storedEmc += canInsert;
            setChanged();
        }
        return canInsert;
    }

    @Override
    public long extractEmc(long amount, EmcAction action) {
        if (isCreative()) return amount;
        long canExtract = Math.min(amount, storedEmc);
        if (action.execute() && canExtract > 0) {
            storedEmc -= canExtract;
            setChanged();
        }
        return canExtract;
    }

    @Override public long getStoredEmc() { return storedEmc; }
    @Override public long getMaximumEmc() { return capacity; }


    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.energytoemc.emc_capacitor");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return new EmcCapacitorMenu(id, inv, this, this.data);
    }


    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.storedEmc = tag.getLong("StoredEmc");
        this.capacity = tag.getLong("Capacity");
        if (tag.contains("FEStored")) {
            this.energyStorage.receiveEnergy(tag.getInt("FEStored"), false);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.putLong("StoredEmc", storedEmc);
        tag.putLong("Capacity", capacity);
        tag.putInt("FEStored", energyStorage.getEnergyStored());
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) return energyCap.cast();
        if (cap == ProjectEAPI.EMC_STORAGE_CAPABILITY) return emcCap.cast();
        return super.getCapability(cap, side);
    }


    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
}