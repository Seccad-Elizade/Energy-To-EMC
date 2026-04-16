package com.seccad_elizade.energytoemc;

import com.seccad_elizade.energytoemc.network.ModPacketHandler;
import com.seccad_elizade.energytoemc.registry.*;
import com.seccad_elizade.energytoemc.screen.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnergyToEmc.MOD_ID)
public class EnergyToEmc {
    public static final String MOD_ID = "energytoemc";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final ItemGroup TAB = new ItemGroup("energytoemc_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.ATOMIC_CAPACITOR.get());
        }
    };

    public EnergyToEmc() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModMenuTypes.MENUS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);

        bus.addListener(this::setup);
        bus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPacketHandler.register();
            LOGGER.info("EnergyToEMC: Common Setup Complete");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ScreenManager.register(ModMenuTypes.EMC_CONVERTER_MENU.get(), EmcConverterScreen::new);
            ScreenManager.register(ModMenuTypes.EMC_PIPE_MENU.get(), EmcPipeScreen::new);
            ScreenManager.register(ModMenuTypes.EMC_CAPACITOR_MENU.get(), EmcCapacitorScreen::new);

            RenderTypeLookup.setRenderLayer(ModBlocks.EMC_PIPE.get(), RenderType.translucent());

            LOGGER.info("EnergyToEMC: Client Setup (Render Layers) Complete");
        });
    }
}