package net.diogomarassi.realcomputingmod.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.diogomarassi.realcomputingmod.TutorialMod;
import net.diogomarassi.realcomputingmod.entity.MagicBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class MagicBlock extends Block implements BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED_A = BooleanProperty.of("powered_a");
    public static final BooleanProperty POWERED_B = BooleanProperty.of("powered_b");
    public static final BooleanProperty POWERED_OUTPUT = BooleanProperty.of("powered_output");
    public static final IntProperty OPERATION = IntProperty.of("operation", 0, 3); // 0=NOT, 1=AND, 2=OR, 3=XOR
    public static final MapCodec<MagicBlock> CODEC = createCodec(MagicBlock::new);

    public MagicBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(POWERED_A, false)
            .with(POWERED_B, false)
            .with(POWERED_OUTPUT, false)
            .with(OPERATION, 0)); // Default: NOT
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED_A, POWERED_B, POWERED_OUTPUT, OPERATION);
    }

    // 2. CORRIGIDO: O método onUse agora pede para a Entity abrir o menu
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // DEBUG: Log quando clica no bloco
        TutorialMod.LOGGER.info("MagicBlock clicado por {} na posição {}", player.getName().getString(), pos);

        // DEBUG: Toca um som simples
        world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        if (!world.isClient) {
            TutorialMod.LOGGER.info("Servidor: Tentando abrir GUI...");

            // Pega a entidade que está neste bloco
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                TutorialMod.LOGGER.info("Servidor: ScreenHandlerFactory encontrada! Abrindo GUI...");
                // Abre a tela (isso vai chamar o createMenu da MagicBlockEntity)
                player.openHandledScreen(screenHandlerFactory);
            } else {
                TutorialMod.LOGGER.error("Servidor: ScreenHandlerFactory é NULL! BlockEntity não foi encontrada!");
            }
        } else {
            TutorialMod.LOGGER.info("Cliente: Clique detectado");
        }
        return ActionResult.SUCCESS;
    }

    // 3. Este método agora funciona porque implementamos BlockEntityProvider
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MagicBlockEntity(pos, state);
    }

    // 4. Retorna a BlockEntity como ScreenHandlerFactory para abrir a GUI
    @Nullable
    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory) blockEntity : null;
    }

    // 5. Detecta mudanças nos vizinhos (sinais de redstone)
    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            Direction facing = state.get(FACING);

            // WEST relativo = Input A - detecta apenas deste lado específico
            Direction westSide = facing.rotateYCounterclockwise();
            BlockPos westPos = pos.offset(westSide);
            boolean poweredA = world.getEmittedRedstonePower(westPos, westSide) > 0;

            // EAST relativo = Input B - detecta apenas deste lado específico
            Direction eastSide = facing.rotateYClockwise();
            BlockPos eastPos = pos.offset(eastSide);
            boolean poweredB = world.getEmittedRedstonePower(eastPos, eastSide) > 0;

            // Calcula o novo output
            boolean oldOutput = state.get(POWERED_OUTPUT);
            boolean newOutput = calculateOutput(world, pos, state.with(POWERED_A, poweredA).with(POWERED_B, poweredB));

            TutorialMod.LOGGER.info("MagicBlock em {}: Input A = {}, Input B = {}, Output calculado = {}", pos, poweredA, poweredB, newOutput);

            // Atualiza o estado se mudou
            if (state.get(POWERED_A) != poweredA || state.get(POWERED_B) != poweredB || oldOutput != newOutput) {
                world.setBlockState(pos, state
                    .with(POWERED_A, poweredA)
                    .with(POWERED_B, poweredB)
                    .with(POWERED_OUTPUT, newOutput), Block.NOTIFY_ALL);

                TutorialMod.LOGGER.info("MagicBlock em {}: Estado atualizado! Output {} -> {}", pos, oldOutput, newOutput);

                // Notifica os vizinhos sobre mudança no output
                Direction outputSide = facing.getOpposite();
                BlockPos outputPos = pos.offset(outputSide);
                world.updateNeighbor(outputPos, this, pos);
                TutorialMod.LOGGER.info("MagicBlock em {}: Notificando vizinho em {} (lado {})", pos, outputPos, outputSide);
            }
        }
    }

    // 6. Calcula o resultado da lógica baseado na operação e entradas
    private boolean calculateOutput(World world, BlockPos pos, BlockState state) {
        boolean inputA = state.get(POWERED_A);
        boolean inputB = state.get(POWERED_B);
        int operation = state.get(OPERATION);

        return switch (operation) {
            case 0 -> !inputA;           // NOT
            case 1 -> inputA && inputB;  // AND
            case 2 -> inputA || inputB;  // OR
            case 3 -> inputA ^ inputB;   // XOR
            default -> false;
        };
    }

    // 7. Indica que este bloco emite sinal de redstone
    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    // 8. Emite sinal de redstone fraco do lado de output (SOUTH - parte de trás)
    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        // O output sai do lado oposto ao FACING (SOUTH no modelo = parte de trás)
        Direction outputDirection = state.get(FACING).getOpposite();

        TutorialMod.LOGGER.info("MagicBlock.getWeakRedstonePower em {}: direction={}, outputDirection={}, POWERED_OUTPUT={}",
            pos, direction, outputDirection, state.get(POWERED_OUTPUT));

        // A direção recebida é de onde o vizinho está, então precisamos comparar com o oposto
        if (direction.getOpposite() == outputDirection) {
            int power = state.get(POWERED_OUTPUT) ? 15 : 0;
            TutorialMod.LOGGER.info("MagicBlock em {}: Emitindo {} de potência para direção {}", pos, power, direction);
            return power;
        }

        return 0;
    }

    // 9. Emite sinal de redstone forte do lado de output
    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        TutorialMod.LOGGER.info("MagicBlock.getStrongRedstonePower chamado em {}: direction={}", pos, direction);
        return getWeakRedstonePower(state, world, pos, direction);
    }

    // 10. Adiciona partículas de redstone quando ativado (efeito visual)
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Spawna partículas quando qualquer entrada ou output está ativado
        boolean poweredA = state.get(POWERED_A);
        boolean poweredB = state.get(POWERED_B);
        boolean poweredOutput = state.get(POWERED_OUTPUT);
        Direction facing = state.get(FACING);

        // Partículas vermelhas para inputs e output ativos
        if (poweredA || poweredB || poweredOutput) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            // Partículas de redstone dust vermelho (RGB: 1.0, 0.0, 0.0)
            DustParticleEffect redDust = new DustParticleEffect(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f);

            // Spawna partículas no lado de Input A (WEST) se ativado
            if (poweredA && random.nextFloat() < 0.5f) {
                Direction westSide = facing.rotateYCounterclockwise();
                double offsetX = x + westSide.getOffsetX() * 0.52;
                double offsetY = y + random.nextDouble() * 0.4 - 0.2;
                double offsetZ = z + westSide.getOffsetZ() * 0.52;
                world.addParticle(redDust, offsetX, offsetY, offsetZ, 0.0, 0.0, 0.0);
            }

            // Spawna partículas no lado de Input B (EAST) se ativado
            if (poweredB && random.nextFloat() < 0.5f) {
                Direction eastSide = facing.rotateYClockwise();
                double offsetX = x + eastSide.getOffsetX() * 0.52;
                double offsetY = y + random.nextDouble() * 0.4 - 0.2;
                double offsetZ = z + eastSide.getOffsetZ() * 0.52;
                world.addParticle(redDust, offsetX, offsetY, offsetZ, 0.0, 0.0, 0.0);
            }

            // Spawna partículas no lado de Output (oposto ao facing) se ativado
            if (poweredOutput && random.nextFloat() < 0.5f) {
                Direction outputSide = facing.getOpposite();
                double offsetX = x + outputSide.getOffsetX() * 0.52;
                double offsetY = y + random.nextDouble() * 0.4 - 0.2;
                double offsetZ = z + outputSide.getOffsetZ() * 0.52;
                world.addParticle(redDust, offsetX, offsetY, offsetZ, 0.0, 0.0, 0.0);
            }
        }
    }
}