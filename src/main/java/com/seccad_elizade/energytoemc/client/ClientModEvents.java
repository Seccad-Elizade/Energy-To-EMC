package com.seccad_elizade.energytoemc.client;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.registry.ModBlocks;
import com.seccad_elizade.energytoemc.registry.ModMenuTypes;
import com.seccad_elizade.energytoemc.screen.EmcCapacitorScreen;
import com.seccad_elizade.energytoemc.screen.EmcConverterScreen;
import com.seccad_elizade.energytoemc.screen.EmcPipeScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EnergyToEmc.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ScreenManager.register(ModMenuTypes.EMC_PIPE_MENU.get(), EmcPipeScreen::new);
            ScreenManager.register(ModMenuTypes.EMC_CONVERTER_MENU.get(), EmcConverterScreen::new);
            ScreenManager.register(ModMenuTypes.EMC_CAPACITOR_MENU.get(), EmcCapacitorScreen::new);

            RenderTypeLookup.setRenderLayer(ModBlocks.EMC_PIPE.get(), RenderType.cutout());
        });
    }
}