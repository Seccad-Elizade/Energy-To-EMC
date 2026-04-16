package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.screen.EmcCapacitorMenu;
import com.seccad_elizade.energytoemc.screen.EmcConverterMenu;
import com.seccad_elizade.energytoemc.screen.EmcPipeMenu;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModMenuTypes {
    public static final DeferredRegister<ContainerType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, EnergyToEmc.MOD_ID);

    public static final RegistryObject<ContainerType<EmcConverterMenu>> EMC_CONVERTER_MENU =
            MENUS.register("emc_converter_menu", () -> IForgeContainerType.create(EmcConverterMenu::new));

    public static final RegistryObject<ContainerType<EmcPipeMenu>> EMC_PIPE_MENU =
            MENUS.register("emc_pipe_menu", () -> IForgeContainerType.create(EmcPipeMenu::new));

    public static final RegistryObject<ContainerType<EmcCapacitorMenu>> EMC_CAPACITOR_MENU =
            MENUS.register("emc_capacitor_menu", () -> IForgeContainerType.create(EmcCapacitorMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}