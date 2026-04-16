package com.seccad_elizade.energytoemc.item;

import com.seccad_elizade.energytoemc.block.EmcPipeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.state.BooleanProperty;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        PlayerEntity player = context.getPlayer();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (player == null) return ActionResultType.PASS;

        // Case 1: Clicking directly on the pipe
        if (clickedState.getBlock() instanceof EmcPipeBlock) {
            if (!level.isClientSide) {
                Direction sideToToggle = getTargetDirection(context);
                toggleConnection(level, clickedPos, clickedState, sideToToggle, player);
                level.playSound(null, clickedPos, SoundEvents.ANVIL_PLACE, SoundCategory.BLOCKS, 0.5f, 2.0f);
            }
            player.swing(context.getHand());
            return ActionResultType.sidedSuccess(level.isClientSide);
        }

        // Case 2: Clicking a block face to toggle the pipe attached to that face
        BlockPos pipePos = clickedPos.relative(clickedFace);
        BlockState pipeState = level.getBlockState(pipePos);

        if (pipeState.getBlock() instanceof EmcPipeBlock) {
            if (!level.isClientSide) {
                Direction sideOfPipe = clickedFace.getOpposite();
                toggleConnection(level, pipePos, pipeState, sideOfPipe, player);
                level.playSound(null, pipePos, SoundEvents.ANVIL_PLACE, SoundCategory.BLOCKS, 0.5f, 2.0f);
            }
            player.swing(context.getHand());
            return ActionResultType.sidedSuccess(level.isClientSide);
        }

        return ActionResultType.PASS;
    }

    private Direction getTargetDirection(ItemUseContext context) {
        // In 1.16.5, we use Vector3d and subtract manually
        Vector3d hitVec = context.getClickLocation().subtract(Vector3d.atLowerCornerOf(context.getClickedPos()));
        double x = hitVec.x - 0.5;
        double y = hitVec.y - 0.5;
        double z = hitVec.z - 0.5;
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absX > absY && absX > absZ) return x > 0 ? Direction.EAST : Direction.WEST;
        if (absY > absX && absY > absZ) return y > 0 ? Direction.UP : Direction.DOWN;
        return z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private void toggleConnection(World level, BlockPos pos, BlockState state, Direction side, PlayerEntity player) {
        BooleanProperty sideProperty = EmcPipeBlock.getPropertyForDirection(side);
        boolean newState = !state.getValue(sideProperty);

        BlockPos neighborPos = pos.relative(side);
        EmcPipeBlock pipeBlock = (EmcPipeBlock) state.getBlock();

        if (newState && !pipeBlock.canConnectTo(level, neighborPos, side)) {
            player.displayClientMessage(new StringTextComponent("Cannot connect pipe to this block!").withStyle(TextFormatting.RED), true);
            return;
        }

        level.setBlock(pos, state.setValue(sideProperty, newState), 3);

        // Synchronize the connection with the neighboring pipe if it exists
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof EmcPipeBlock) {
            Direction opposingSide = side.getOpposite();
            BooleanProperty opposingProperty = EmcPipeBlock.getPropertyForDirection(opposingSide);
            if (neighborState.getValue(opposingProperty) != newState) {
                level.setBlock(neighborPos, neighborState.setValue(opposingProperty, newState), 3);
            }
        }

        TextFormatting color = newState ? TextFormatting.GREEN : TextFormatting.RED;
        String status = newState ? "Connected" : "Disconnected";
        player.displayClientMessage(new StringTextComponent("Side [" + side.name() + "] " + status).withStyle(color), true);
    }
}