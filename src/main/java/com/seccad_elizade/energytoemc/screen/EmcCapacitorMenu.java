package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.block.entity.EmcCapacitorBlockEntity;
import com.seccad_elizade.energytoemc.registry.ModMenuTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EmcCapacitorMenu extends Container {
    private final EmcCapacitorBlockEntity blockEntity;
    private final IIntArray data;

    // Client Constructor (called by Forge when opening the GUI)
    public EmcCapacitorMenu(int containerId, PlayerInventory inv, PacketBuffer extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new IntArray(8));
    }

    // Server Constructor
    public EmcCapacitorMenu(int containerId, PlayerInventory inv, TileEntity entity, IIntArray data) {
        super(ModMenuTypes.EMC_CAPACITOR_MENU.get(), containerId);

        if (entity instanceof EmcCapacitorBlockEntity) {
            this.blockEntity = (EmcCapacitorBlockEntity) entity;
        } else {
            throw new IllegalStateException("TileEntity is not an EmcCapacitorBlockEntity!");
        }

        checkContainerDataCount(data, 8);
        this.data = data;

        // Syncs the data array between server and client
        this.addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    // Reconstructs the 64-bit Long from 4x 16-bit Ints
    public long getStoredEmc() {
        return ((long) (data.get(0) & 0xFFFF)) |
                ((long) (data.get(1) & 0xFFFF) << 16) |
                ((long) (data.get(2) & 0xFFFF) << 32) |
                ((long) (data.get(3) & 0xFFFF) << 48);
    }

    public long getMaxEmc() {
        return ((long) (data.get(4) & 0xFFFF)) |
                ((long) (data.get(5) & 0xFFFF) << 16) |
                ((long) (data.get(6) & 0xFFFF) << 32) |
                ((long) (data.get(7) & 0xFFFF) << 48);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        // Standard distance check
        return stillValid(net.minecraft.util.IWorldPosCallable.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Logic for shifting between inventory and hotbar (since capacitors usually don't have slots)
            if (index < 27) {
                if (!this.moveItemStackTo(itemstack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    private void addPlayerInventory(PlayerInventory inv) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inv, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory inv) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }
}