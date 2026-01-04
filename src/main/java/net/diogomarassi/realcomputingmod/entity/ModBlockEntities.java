package net.diogomarassi.realcomputingmod.entity;

import net.diogomarassi.realcomputingmod.TutorialMod;
import net.diogomarassi.realcomputingmod.blocks.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<MagicBlockEntity> MAGIC_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(TutorialMod.MOD_ID, "magic_block_entity"),
                    BlockEntityType.Builder.create(MagicBlockEntity::new, ModBlocks.MAGIC_BLOCK).build(null));

    public static void registerBlockEntities() {
        TutorialMod.LOGGER.info("Registering Block Entities for " + TutorialMod.MOD_ID);
    }
}