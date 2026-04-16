package com.seccad_elizade.energytoemc.block;

import com.seccad_elizade.energytoemc.block.entity.EmcConverterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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

public class EmcConverterBlock extends ContainerBlock {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public EmcConverterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent("Converts Energy into EMC").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent("--------------------------").withStyle(TextFormatting.DARK_GRAY));

        IFormattableTextComponent rateText = new StringTextComponent("Rate: ").withStyle(TextFormatting.YELLOW)
                .append(new StringTextComponent("500 FE ").withStyle(TextFormatting.WHITE))
                .append(new StringTextComponent("-> ").withStyle(TextFormatting.GRAY))
                .append(new StringTextComponent("1 EMC").withStyle(TextFormatting.AQUA));
        tooltip.add(rateText);

        tooltip.add(new StringTextComponent("Max Storage: ").withStyle(TextFormatting.YELLOW)
                .append(new StringTextComponent("1,000,000 FE").withStyle(TextFormatting.WHITE)));
        tooltip.add(new StringTextComponent("Supports Efficiency Upgrades").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof EmcConverterBlockEntity) {
                // Fixed: Use NetworkHooks.openGui to ensure the Menu receives the BlockPos data
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
            }
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof EmcConverterBlockEntity) {
                EmcConverterBlockEntity converterBe = (EmcConverterBlockEntity) te;
                // Drops contents of the internal ItemHandler when block is broken
                for (int i = 0; i < converterBe.getItemHandler().getSlots(); i++) {
                    InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(),
                            converterBe.getItemHandler().getStackInSlot(i));
                }
                world.updateNeighborsAt(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te != null) {
                te.setChanged();
            }
        }
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) {
        return new EmcConverterBlockEntity();
    }
}