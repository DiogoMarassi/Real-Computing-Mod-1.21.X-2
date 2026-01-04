package net.diogomarassi.realcomputingmod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class MagicBlockScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final BlockPos pos;

    // Construtor Cliente - recebe BlockPos dos dados extras
    public MagicBlockScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, new SimpleInventory(0), new ArrayPropertyDelegate(1), pos);
    }

    // Construtor Servidor
    public MagicBlockScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate, BlockPos pos) {
        super(ModScreenHandlers.MAGIC_BLOCK_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.propertyDelegate = delegate;
        this.pos = pos;

        // Sincroniza a operação selecionada (0=NOT, 1=AND, 2=OR, 3=XOR)
        this.addProperties(delegate);

        // Espaçamento entre slots: 18 pixels (padrão Minecraft)
        int SLOT_SIZE = 18;

        // --- Slots do Jogador (Inventário Principal: 3 linhas x 9 colunas) ---
        // Inicia em 7x68
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * SLOT_SIZE, 69 + row * SLOT_SIZE));
            }
        }

        // --- Slots do Jogador (Hotbar: 1 linha x 9 colunas) ---
        // Inicia em 7x126
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * SLOT_SIZE, 127));
        }
    }

    public int getOperation() {
        return this.propertyDelegate.get(0);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}