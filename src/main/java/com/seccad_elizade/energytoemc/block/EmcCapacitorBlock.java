package com.seccad_elizade.energytoemc.block;

import com.seccad_elizade.energytoemc.block.entity.EmcCapacitorBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class EmcCapacitorBlock extends ContainerBlock {
    private final long capacity;

    public EmcCapacitorBlock(Properties properties, long capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public long getCapacity() { return this.capacity; }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader level) {
        // Fixed: Use default constructor and then set the capacity
        EmcCapacitorBlockEntity be = new EmcCapacitorBlockEntity();
        be.setCapacity(this.capacity);
        return be;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!level.isClientSide) {
            TileEntity be = level.getBlockEntity(pos);
            if (be instanceof EmcCapacitorBlockEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) be, pos);
            } else {
                // This can happen briefly during block placement/removal, so log it instead of crashing
                return ActionResultType.PASS;
            }
        }
        return ActionResultType.sidedSuccess(level.isClientSide);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ITextComponent amountText;
        if (this.capacity >= Long.MAX_VALUE || this.capacity < 0) {
            amountText = new StringTextComponent("∞").withStyle(TextFormatting.LIGHT_PURPLE, TextFormatting.OBFUSCATED);
        } else {
            amountText = new StringTextComponent(String.format("%,d", this.capacity)).withStyle(TextFormatting.WHITE);
        }

        tooltip.add(new TranslationTextComponent("tooltip.energytoemc.max_storage")
                .append(new StringTextComponent(": "))
                .append(amountText)
                .append(new StringTextComponent(" EMC").withStyle(TextFormatting.GOLD))
                .withStyle(TextFormatting.GOLD));
    }
}