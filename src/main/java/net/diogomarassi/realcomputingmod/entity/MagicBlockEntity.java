package net.diogomarassi.realcomputingmod.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.diogomarassi.realcomputingmod.TutorialMod;
import net.diogomarassi.realcomputingmod.blocks.custom.MagicBlock;
import net.diogomarassi.realcomputingmod.screen.MagicBlockScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MagicBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {
    private int operation = 0;

    protected final PropertyDelegate propertyDelegate;

    public MagicBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_BLOCK_ENTITY, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return operation;
            }

            @Override
            public void set(int index, int value) {
                operation = value;
                markDirty();
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("magic_block.operation", operation);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        operation = nbt.getInt("magic_block.operation");
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Magic Block");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // Passa um inventário vazio (dummy) já que não usamos slots do bloco
        return new MagicBlockScreenHandler(syncId, playerInventory, new SimpleInventory(0), this.propertyDelegate, this.pos);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        // Envia o BlockPos para o cliente
        return this.pos;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        TutorialMod.LOGGER.info("MagicBlockEntity.setOperation: {} -> {}", this.operation, operation);
        this.operation = operation;
        this.propertyDelegate.set(0, operation);
        markDirty();

        // Recalcula e atualiza o output quando a operação muda
        if (world != null && !world.isClient) {
            BlockState state = getCachedState();
            boolean inputA = state.get(MagicBlock.POWERED_A);
            boolean inputB = state.get(MagicBlock.POWERED_B);

            boolean newOutput = switch (operation) {
                case 0 -> !inputA;           // NOT
                case 1 -> inputA && inputB;  // AND
                case 2 -> inputA || inputB;  // OR
                case 3 -> inputA ^ inputB;   // XOR
                default -> false;
            };

            boolean oldOutput = state.get(MagicBlock.POWERED_OUTPUT);

            // Atualiza o BlockState com a nova operação E o novo output
            world.setBlockState(pos, state
                .with(MagicBlock.OPERATION, operation)
                .with(MagicBlock.POWERED_OUTPUT, newOutput), Block.NOTIFY_ALL);

            TutorialMod.LOGGER.info("MagicBlockEntity.setOperation: Output recalculado! {} -> {}", oldOutput, newOutput);

            // Notifica os vizinhos sobre mudança no output
            Direction facing = state.get(MagicBlock.FACING);
            Direction outputSide = facing.getOpposite();
            BlockPos outputPos = pos.offset(outputSide);
            world.updateNeighbor(outputPos, getCachedState().getBlock(), pos);
            TutorialMod.LOGGER.info("MagicBlockEntity.setOperation: Vizinho em {} notificado", outputPos);
        }

        TutorialMod.LOGGER.info("MagicBlockEntity.setOperation: Concluído! Novo valor: {}", this.operation);
    }
}