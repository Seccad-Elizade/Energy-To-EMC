package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.block.EmcCapacitorBlock;
import com.seccad_elizade.energytoemc.block.EmcConverterBlock;
import com.seccad_elizade.energytoemc.block.EmcPipeBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    // Fixed: Changed MODID to MOD_ID to match your main class
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EnergyToEmc.MOD_ID);

    public static final RegistryObject<Block> EMC_CONVERTER = BLOCKS.register("emc_converter",
            () -> new EmcConverterBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(4.0f, 12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)));

    public static final RegistryObject<Block> EMC_PIPE = BLOCKS.register("emc_pipe",
            () -> new EmcPipeBlock(AbstractBlock.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            ));

    public static final RegistryObject<Block> BASIC_CAPACITOR = BLOCKS.register("basic_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(3.5f), 100000L));

    public static final RegistryObject<Block> ADVANCED_CAPACITOR = BLOCKS.register("hardened_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(5.0f), 1000000L));

    public static final RegistryObject<Block> ELITE_CAPACITOR = BLOCKS.register("reinforced_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(7.0f), 10000000L));

    public static final RegistryObject<Block> ULTIMATE_CAPACITOR = BLOCKS.register("atomic_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(10.0f).lightLevel(state -> 3), 100000000L));

    public static final RegistryObject<Block> CREATIVE_CAPACITOR = BLOCKS.register("singularity_emc_capacitor",
            () -> new EmcCapacitorBlock(capacitorProps(-1.0f).lightLevel(state -> 15), Long.MAX_VALUE));

    private static AbstractBlock.Properties capacitorProps(float strength) {
        AbstractBlock.Properties props = AbstractBlock.Properties.copy(Blocks.IRON_BLOCK)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops();

        if (strength < 0) {
            return props.strength(-1.0f, 3600000.0F);
        }
        return props.strength(strength, strength * 2.5f);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}