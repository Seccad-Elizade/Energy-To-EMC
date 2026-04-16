package com.seccad_elizade.energytoemc.screen;

import com.seccad_elizade.energytoemc.block.entity.EmcConverterBlockEntity;
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
import net.minecraft.world.World;
import net.minecraftforge.items.SlotItemHandler;

public class EmcConverterMenu extends Container {
    private final EmcConverterBlockEntity blockEntity;
    private final World world;
    private final IIntArray data;

    // Client Constructor
    public EmcConverterMenu(int containerId, PlayerInventory inv, PacketBuffer extraData) {
        this(containerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new IntArray(13));
    }

    // Server Constructor
    public EmcConverterMenu(int containerId, PlayerInventory inv, TileEntity entity, IIntArray data) {
        super(ModMenuTypes.EMC_CONVERTER_MENU.get(), containerId);

        if (entity instanceof EmcConverterBlockEntity) {
            this.blockEntity = (EmcConverterBlockEntity) entity;
        } else {
            throw new IllegalStateException("TileEntity is not an EmcConverterBlockEntity!");
        }

        checkContainerDataCount(data, 13);
        this.world = inv.player.level;
        this.data = data;

        // Slot 0: Upgrade Slot
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 35));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Synchronize the data array (Indices 0-12)
        this.addDataSlots(data);
    }

    // Energy display (32-bit)
    public int getEnergy() {
        return (this.data.get(0) & 0xFFFF) | (this.data.get(1) << 16);
    }

    // EMC display (64-bit) - FIXED: Masking with 0xFFFF prevents sign-extension errors
    public long getEmc() {
        return (long) (this.data.get(2) & 0xFFFF) |
                ((long) (this.data.get(3) & 0xFFFF) << 16) |
                ((long) (this.data.get(4) & 0xFFFF) << 32) |
                ((long) (this.data.get(5) & 0xFFFF) << 48);
    }

    // Phosphor Display & Logic sync
    public int getEnergyUsage() { return this.data.get(11); }
    public int getEmcProduction() { return this.data.get(12); }
    public int getRedstoneMode() { return this.data.get(10); }

    public EmcConverterBlockEntity getBlockEntity() { return this.blockEntity; }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        // 0: Upgrade Slot, 1-27: Inventory, 28-36: Hotbar
        if (index < 1) {
            if (!moveItemStackTo(sourceStack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(sourceStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyStack;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return stillValid(IWorldPosCallable.create(world, blockEntity.getBlockPos()),
                player, ModBlocks.EMC_CONVERTER.get());
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