package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import com.seccad_elizade.energytoemc.item.PipeUpgradeItem;
import com.seccad_elizade.energytoemc.registry.ModBlocks;
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
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class EmcPipeMenu extends Container {
    private final EmcPipeBlockEntity blockEntity;
    private final IIntArray data;

    public EmcPipeMenu(int containerId, PlayerInventory inv, PacketBuffer extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new IntArray(1));
    }

    public EmcPipeMenu(int containerId, PlayerInventory inv, TileEntity entity, IIntArray data) {
        super(ModMenuTypes.EMC_PIPE_MENU.get(), containerId);

        if (entity instanceof EmcPipeBlockEntity) {
            this.blockEntity = (EmcPipeBlockEntity) entity;
        } else {
            throw new IllegalStateException("TileEntity is not an EmcPipeBlockEntity!");
        }

        this.data = data;
        checkContainerDataCount(data, 1);

        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 25) {
            @Override
            public boolean isActive() {
                return isExtractMode();
            }

            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return isExtractMode() && stack.getItem() instanceof PipeUpgradeItem;
            }
        });

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(data);
    }

    public boolean isExtractMode() {
        return this.data.get(0) == 1;
    }

    public EmcPipeBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return stillValid(IWorldPosCallable.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerIn, ModBlocks.EMC_PIPE.get());
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (isExtractMode() && itemstack1.getItem() instanceof PipeUpgradeItem) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(itemstack1, 28, 37, false)) return ItemStack.EMPTY;
                } else if (index < 37 && !this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}