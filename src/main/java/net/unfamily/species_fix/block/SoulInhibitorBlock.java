package net.unfamily.species_fix.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulInhibitorBlock extends Block {
    public static final BooleanProperty ON = BooleanProperty.create("on");
    private static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    private static final VoxelShape SHAPE_NORTH = Block.box(1, 2, 14, 15, 14, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 2, 0, 15, 14, 2);
    private static final VoxelShape SHAPE_WEST = Block.box(14, 2, 1, 16, 14, 15);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 2, 1, 2, 14, 15);

    @SuppressWarnings("null")
    public SoulInhibitorBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F, 1200.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ON, true)
                .setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    @SuppressWarnings("null")
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON, POWERED, FACING);
    }

    @Override
    @SuppressWarnings("null")
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        Direction facing = context.getHorizontalDirection();
        // Place "facing" towards the player: front points to player => opposite of player's looking direction
        Direction facingTowardsPlayer = facing.getOpposite();
        return this.defaultBlockState()
                .setValue(POWERED, powered)
                .setValue(FACING, facingTowardsPlayer);
    }

    @Override
    @SuppressWarnings("null")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        level.setBlock(pos, state.cycle(ON), Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("null")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    @SuppressWarnings("null")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide()) return;

        boolean poweredNow = level.hasNeighborSignal(pos);
        boolean poweredBefore = state.getValue(POWERED);

        if (poweredNow && !poweredBefore) {
            // Rising edge => toggle ON/OFF
            level.setBlock(pos, state.setValue(POWERED, true).cycle(ON), Block.UPDATE_ALL);
            return;
        }

        if (!poweredNow && poweredBefore) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        }
    }

    @Override
    @SuppressWarnings("null")
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("null")
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction direction) {
        return 0;
    }

    @Override
    @SuppressWarnings("null")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // no-op (kept to avoid accidental scheduled tick usage)
    }
}

