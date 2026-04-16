package com.seccad_elizade.energytoemc.block;

import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class EmcPipeBlock extends ContainerBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private static final VoxelShape CORE_SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public EmcPipeBlock(Properties properties) {
        super(properties.noOcclusion().strength(2.0f).isRedstoneConductor((state, level, pos) -> false));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(LIT, false)
                .setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (level.isClientSide) return ActionResultType.SUCCESS;

        TileEntity be = level.getBlockEntity(pos);
        if (!(be instanceof EmcPipeBlockEntity)) return ActionResultType.PASS;
        EmcPipeBlockEntity pipeBe = (EmcPipeBlockEntity) be;

        ItemStack stack = player.getItemInHand(hand);

        // 1. Wrench Logic: Handle connection toggling
        boolean isWrench = !stack.isEmpty() && stack.getItem().getRegistryName() != null
                && stack.getItem().getRegistryName().getPath().contains("wrench");

        if (isWrench) {
            if (player.isShiftKeyDown()) {
                pipeBe.toggleMode();
                player.displayClientMessage(new StringTextComponent("Pipe Mode Toggled").withStyle(TextFormatting.AQUA), true);
            } else {
                Direction side = hit.getDirection();
                BooleanProperty prop = getPropertyForDirection(side);
                level.setBlock(pos, state.setValue(prop, !state.getValue(prop)), 3);
            }
            return ActionResultType.SUCCESS;
        }

        // 2. GUI Logic: Only with EMPTY HAND and ONLY if connected to a MACHINE
        if (stack.isEmpty() && hand == Hand.MAIN_HAND) {
            if (isConnectedToMachine(level, pos, state)) {
                if (be instanceof INamedContainerProvider) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) be, pos);
                    return ActionResultType.SUCCESS;
                }
            } else {
                // Inform player that configuration requires a machine connection
                player.displayClientMessage(new StringTextComponent("Connect the pipe to a machine to configure it!").withStyle(TextFormatting.RED), true);
                return ActionResultType.FAIL;
            }
        }

        return ActionResultType.PASS;
    }

    /**
     * Checks if the pipe is connected to a ProjectE-compatible machine.
     * It ignores connections to other EmcPipeBlocks.
     */
    private boolean isConnectedToMachine(World level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            // Check if there is a visual connection arm in this direction
            if (state.getValue(getPropertyForDirection(dir))) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);

                // We only care if the neighbor is NOT another pipe
                if (!(neighborState.getBlock() instanceof EmcPipeBlock)) {
                    TileEntity te = level.getBlockEntity(neighborPos);
                    // Check if the neighbor has the ProjectE EMC Capability
                    if (te != null && te.getCapability(ProjectEAPI.EMC_STORAGE_CAPABILITY, dir.getOpposite()).isPresent()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = CORE_SHAPE;
        if (state.getValue(NORTH)) shape = VoxelShapes.or(shape, Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D));
        if (state.getValue(SOUTH)) shape = VoxelShapes.or(shape, Block.box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D));
        if (state.getValue(EAST))  shape = VoxelShapes.or(shape, Block.box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D));
        if (state.getValue(WEST))  shape = VoxelShapes.or(shape, Block.box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D));
        if (state.getValue(UP))    shape = VoxelShapes.or(shape, Block.box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D));
        if (state.getValue(DOWN))  shape = VoxelShapes.or(shape, Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D));
        return shape;
    }

    public boolean canConnectTo(IWorld level, BlockPos neighborPos, Direction side) {
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof EmcPipeBlock) return true;
        TileEntity te = level.getBlockEntity(neighborPos);
        return te != null && te.getCapability(ProjectEAPI.EMC_STORAGE_CAPABILITY, side.getOpposite()).isPresent();
    }

    public static BooleanProperty getPropertyForDirection(Direction direction) {
        switch (direction) {
            case NORTH: return NORTH;
            case SOUTH: return SOUTH;
            case EAST:  return EAST;
            case WEST:  return WEST;
            case UP:    return UP;
            case DOWN:  return DOWN;
            default:    return NORTH;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(NORTH, canConnectTo(level, pos.north(), Direction.NORTH))
                .setValue(SOUTH, canConnectTo(level, pos.south(), Direction.SOUTH))
                .setValue(EAST,  canConnectTo(level, pos.east(), Direction.EAST))
                .setValue(WEST,  canConnectTo(level, pos.west(), Direction.WEST))
                .setValue(UP,    canConnectTo(level, pos.above(), Direction.UP))
                .setValue(DOWN,  canConnectTo(level, pos.below(), Direction.DOWN))
                .setValue(LIT,   false)
                .setValue(AXIS,  context.getNearestLookingDirection().getAxis());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, IWorld level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(getPropertyForDirection(dir), canConnectTo(level, neighborPos, dir));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity be = level.getBlockEntity(pos);
            if (be instanceof EmcPipeBlockEntity) {
                ((EmcPipeBlockEntity) be).drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader level) { return new EmcPipeBlockEntity(); }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, LIT, AXIS);
    }
}