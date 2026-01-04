package net.diogomarassi.realcomputingmod.blocks;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.diogomarassi.realcomputingmod.TutorialMod;
import net.diogomarassi.realcomputingmod.blocks.custom.MagicBlock;
import net.diogomarassi.realcomputingmod.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block PINK_GARNET_BLOCK = registerBlock("pink_garnet_block", new Block(AbstractBlock.Settings.create()
            .strength(4f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block MAGIC_BLOCK = registerBlock("magic_block",
            new MagicBlock(AbstractBlock.Settings.create()
                    .strength(1f)
                    .requiresTool()
                    .luminance(state -> {
                        // Emite luz baseado em quantos lados estão ativos
                        int lightLevel = 0;
                        if (state.get(MagicBlock.POWERED_A)) lightLevel += 5;
                        if (state.get(MagicBlock.POWERED_B)) lightLevel += 5;
                        if (state.get(MagicBlock.POWERED_OUTPUT)) lightLevel += 5;
                        return Math.min(lightLevel, 15); // Máximo de 15
                    })));

    private static Block registerBlock(String name, Block block) {
        // 1. PRIMEIRO: Registra o Bloco no jogo
        Block registeredBlock = Registry.register(Registries.BLOCK, Identifier.of(TutorialMod.MOD_ID, name), block);

        // 2. SEGUNDO: Usa o bloco já registrado para criar o Item
        registerBlockItem(name, registeredBlock);

        return registeredBlock;
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(TutorialMod.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        TutorialMod.LOGGER.info("Registering ModBlocks");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.PINK_GARNET_BLOCK);
            entries.add(ModBlocks.MAGIC_BLOCK);
        });

    }
}
