package com.seccad_elizade.energytoemc.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.seccad_elizade.energytoemc.EnergyToEmc;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EmcConverterScreen extends ContainerScreen<EmcConverterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(EnergyToEmc.MOD_ID, "textures/gui/emc_converter_gui.png");

    public EmcConverterScreen(EmcConverterMenu menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = -1000;
        this.inventoryLabelX = -1000;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            this.minecraft.getTextureManager().bind(TEXTURE);
        }

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);

        float energyPct = MathHelper.clamp((float) menu.getEnergy() / 1000000.0f, 0, 1);
        int eH = (int) (energyPct * 48);
        if (eH > 0) {
            fill(matrixStack, x + 12, y + 66 - eH, x + 21, y + 66, 0xFF00AAFF);
        }

        float emcPct = MathHelper.clamp((float) menu.getEmc() / 100000.0f, 0, 1);
        int emcH = (int) (emcPct * 48);
        if (emcH > 0) {
            fill(matrixStack, x + 154, y + 66 - emcH, x + 165, y + 66, 0xFFAA00FF);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, delta);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (isHoveringArea(mouseX, mouseY, x + 11, y + 18, 12, 50)) {
            renderEnergyTooltip(matrixStack, mouseX, mouseY);
        }
        if (isHoveringArea(mouseX, mouseY, x + 153, y + 18, 14, 50)) {
            renderEmcTooltip(matrixStack, mouseX, mouseY);
        }

        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private void renderEnergyTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(new StringTextComponent("Energy Storage").withStyle(TextFormatting.BOLD, TextFormatting.WHITE));

        String storage = String.format("%,d / 1,000,000 FE", menu.getEnergy());
        tooltip.add(new StringTextComponent(storage).withStyle(TextFormatting.BLUE));

        tooltip.add(new StringTextComponent(""));

        long usagePerSec = (long) menu.getEnergyUsage() * 20;
        ITextComponent avgLine = new StringTextComponent("Avg. Consumption: ").withStyle(TextFormatting.GRAY)
                .append(new StringTextComponent(String.format("%,d FE/s", usagePerSec)).withStyle(TextFormatting.RED));
        tooltip.add(avgLine);

        this.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
    }

    private void renderEmcTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(new StringTextComponent("EMC Storage").withStyle(TextFormatting.BOLD, TextFormatting.WHITE));

        String storage = String.format("%,d / 100,000 EMC", menu.getEmc());
        tooltip.add(new StringTextComponent(storage).withStyle(TextFormatting.LIGHT_PURPLE));

        tooltip.add(new StringTextComponent(""));

        long prodPerSec = (long) menu.getEmcProduction() * 20;
        ITextComponent avgLine = new StringTextComponent("Avg. Production: ").withStyle(TextFormatting.GRAY)
                .append(new StringTextComponent(String.format("%,d EMC/s", prodPerSec)).withStyle(TextFormatting.GREEN));
        tooltip.add(avgLine);

        this.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
    }

    private boolean isHoveringArea(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}