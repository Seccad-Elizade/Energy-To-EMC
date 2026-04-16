package com.seccad_elizade.energytoemc.block.entity;

import com.seccad_elizade.energytoemc.block.EmcPipeBlock;
import com.seccad_elizade.energytoemc.item.PipeUpgradeItem;
import com.seccad_elizade.energytoemc.registry.ModBlockEntities;
import com.seccad_elizade.energytoemc.screen.EmcPipeMenu;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.tile.IEmcStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
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
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
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
import java.util.*;

public class EmcPipeBlockEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity, IEmcStorage {
    private int pipeMode = 0;
    private int litTimer = 0;

    private boolean isScanning = false;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof PipeUpgradeItem;
        }
    };

    protected final IIntArray data = new IntArray(1) {
        @Override public int get(int index) { return index == 0 ? pipeMode : 0; }
        @Override public void set(int index, int value) { if (index == 0) pipeMode = value; }
    };

    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEmcStorage> emcCap = LazyOptional.of(() -> this);

    public EmcPipeBlockEntity() {
        super(ModBlockEntities.EMC_PIPE_BE.get());
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;

        if (this.litTimer > 0) {
            this.litTimer--;
        }

        boolean shouldBeLit = this.litTimer > 0;
        BlockState state = getBlockState();
        if (state.hasProperty(EmcPipeBlock.LIT) && state.getValue(EmcPipeBlock.LIT) != shouldBeLit) {
            level.setBlock(worldPosition, state.setValue(EmcPipeBlock.LIT, shouldBeLit), 3);
            level.sendBlockUpdated(worldPosition, state, state.setValue(EmcPipeBlock.LIT, shouldBeLit), 3);
        }

        if (this.pipeMode == 1) {
            pumpFromMachines(level, worldPosition, state);
        }
    }

    private void pumpFromMachines(World level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            if (!state.getValue(EmcPipeBlock.getPropertyForDirection(dir))) continue;

            BlockPos targetPos = pos.relative(dir);
            TileEntity te = level.getBlockEntity(targetPos);

            if (te == null || te instanceof EmcPipeBlockEntity) continue;

            te.getCapability(ProjectEAPI.EMC_STORAGE_CAPABILITY, dir.getOpposite()).ifPresent(sourceMachine -> {
                long available = sourceMachine.getStoredEmc();
                if (available > 0) {
                    long transferLimit = getTransferLimit();
                    long toMove = Math.min(available, transferLimit);

                    Set<BlockPos> network = new HashSet<>();
                    List<EmcPipeBlockEntity> sinks = new ArrayList<>();
                    findNetworkSinks(level, pos, network, sinks);

                    if (sinks.isEmpty()) return;

                    long totalActuallyMoved = 0;
                    this.isScanning = true;
                    try {
                        long amountPerSink = toMove / sinks.size();
                        if (amountPerSink <= 0) amountPerSink = 1;

                        for (EmcPipeBlockEntity sinkPipe : sinks) {
                            totalActuallyMoved += sinkPipe.insertEmc(amountPerSink, EmcAction.EXECUTE);
                        }
                    } finally {
                        this.isScanning = false;
                    }

                    if (totalActuallyMoved > 0) {
                        sourceMachine.extractEmc(totalActuallyMoved, EmcAction.EXECUTE);

                        for (BlockPos p : network) {
                            TileEntity pte = level.getBlockEntity(p);
                            if (pte instanceof EmcPipeBlockEntity) {
                                ((EmcPipeBlockEntity) pte).litTimer = 12;
                                pte.setChanged();
                            }
                        }
                        setChanged();
                    }
                }
            });
        }
    }

    private void findNetworkSinks(World level, BlockPos start, Set<BlockPos> visited, List<EmcPipeBlockEntity> sinks) {
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState state = level.getBlockState(current);
            if (!(state.getBlock() instanceof EmcPipeBlock)) continue;

            for (Direction dir : Direction.values()) {
                if (!state.getValue(EmcPipeBlock.getPropertyForDirection(dir))) continue;
                BlockPos next = current.relative(dir);
                if (visited.contains(next)) continue;

                TileEntity te = level.getBlockEntity(next);
                if (te instanceof EmcPipeBlockEntity) {
                    EmcPipeBlockEntity neighborPipe = (EmcPipeBlockEntity) te;
                    visited.add(next);
                    queue.add(next);
                    if (neighborPipe.pipeMode == 0) {
                        sinks.add(neighborPipe);
                    }
                }
            }
        }
    }

    @Override
    public long insertEmc(long amount, EmcAction action) {
        if (this.pipeMode == 1 || amount <= 0 || level == null || isScanning) return 0;

        BlockState state = getBlockState();
        for (Direction dir : Direction.values()) {
            if (!state.getValue(EmcPipeBlock.getPropertyForDirection(dir))) continue;

            BlockPos neighborPos = worldPosition.relative(dir);
            TileEntity te = level.getBlockEntity(neighborPos);

            if (te != null && !(te instanceof EmcPipeBlockEntity)) {
                Optional<Long> accepted = te.getCapability(ProjectEAPI.EMC_STORAGE_CAPABILITY, dir.getOpposite()).map(machine -> {
                    long machineAccepted = machine.insertEmc(amount, action);
                    if (machineAccepted > 0 && action.execute()) {
                        this.litTimer = 12;
                        setChanged();
                    }
                    return machineAccepted;
                });

                long result = accepted.orElse(0L);
                if (result > 0) return result;
            }
        }
        return 0;
    }

    private long getTransferLimit() {
        long base = 2048L;
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.isEmpty() && stack.getItem() instanceof PipeUpgradeItem) {
            PipeUpgradeItem upgrade = (PipeUpgradeItem) stack.getItem();
            return base + (upgrade.getTransferPerTick() * 512L); // Scaled for 1.16.5 balance
        }
        return base;
    }

    public void toggleMode() {
        this.pipeMode = (this.pipeMode + 1) % 2;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override public long extractEmc(long amount, EmcAction action) { return 0; }
    @Override public long getStoredEmc() { return 0; }
    @Override public long getMaximumEmc() { return getTransferLimit(); }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.pipeMode = tag.getInt("pipeMode");
        this.litTimer = tag.getInt("litTimer");
        if (tag.contains("inv")) {
            itemHandler.deserializeNBT(tag.getCompound("inv"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag.putInt("pipeMode", pipeMode);
        tag.putInt("litTimer", litTimer);
        tag.put("inv", itemHandler.serializeNBT());
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return itemCap.cast();
        if (cap == ProjectEAPI.EMC_STORAGE_CAPABILITY) return emcCap.cast();
        return super.getCapability(cap, side);
    }

    public void drops() {
        if (this.level != null) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                InventoryHelper.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), itemHandler.getStackInSlot(i));
            }
        }
    }

    @Override public ITextComponent getDisplayName() {
        return new StringTextComponent("EMC Pipe (" + (pipeMode == 1 ? "Output" : "Input") + ")");
    }

    @Nullable @Override public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return new EmcPipeMenu(id, inv, this, this.data);
    }

    public ItemStackHandler getItemHandler() { return this.itemHandler; }

    @Override public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
    }
    @Override public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(getBlockState(), pkt.getTag());
    }
    @Override public CompoundNBT getUpdateTag() { return this.save(new CompoundNBT()); }
}