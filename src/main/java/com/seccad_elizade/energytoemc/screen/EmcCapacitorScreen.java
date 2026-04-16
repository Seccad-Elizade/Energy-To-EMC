package com.seccad_elizade.energytoemc.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.seccad_elizade.energytoemc.EnergyToEmc;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EmcCapacitorScreen extends ContainerScreen<EmcCapacitorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(EnergyToEmc.MOD_ID, "textures/gui/capacitor_gui.png");

    public EmcCapacitorScreen(EmcCapacitorMenu menu, PlayerInventory inventory, ITextComponent title) {
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

        // EXACT MATCH to your 1.21.1 code: ending in 176, 166
        this.blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);

        int barX = x + 38;
        int barY = y + 45;
        int barWidth = 100;
        int barHeight = 12;

        long stored = menu.getStoredEmc();
        long max = menu.getMaxEmc();

        boolean isCreative = max >= Long.MAX_VALUE || max < 0;
        double fillPct = isCreative ? 1.0 : (max > 0 ? (double) stored / max : 0);
        int scaledWidth = (int) (fillPct * barWidth);

        // Exact drawing logic from your perfect code
        fill(matrixStack, barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF000000);
        fill(matrixStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF222222);

        if (scaledWidth > 0) {
            int color1 = isCreative ? 0xFFFFD700 : 0xFFAA00FF;
            int color2 = isCreative ? 0xFFFFAA00 : 0xFF7700AA;

            fillGradient(matrixStack, barX, barY, barX + scaledWidth, barY + barHeight, color1, color2);
            fill(matrixStack, barX, barY, barX + scaledWidth, barY + 1, 0x44FFFFFF);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        long stored = menu.getStoredEmc();
        long max = menu.getMaxEmc();

        String emcText;
        if (max >= Long.MAX_VALUE || max < 0) {
            emcText = "Infinite EMC";
        } else {
            emcText = formatValue(stored) + " / " + formatValue(max) + " EMC";
        }

        int centerX = (this.imageWidth / 2) - (this.font.width(emcText) / 2);
        this.font.drawShadow(matrixStack, emcText, centerX, 65, 0xCCCCCC);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, delta);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (isHoveringArea(mouseX, mouseY, x + 38, y + 45, 100, 12)) {
            long stored = menu.getStoredEmc();
            long max = menu.getMaxEmc();
            boolean isCreative = max >= Long.MAX_VALUE || max < 0;

            String detailStored = String.format("%,d EMC", stored);
            String detailMax = isCreative ? "Infinite" : String.format("%,d EMC", max);

            List<ITextComponent> tooltip = new ArrayList<>();
            tooltip.add(new StringTextComponent("EMC Storage").withStyle(TextFormatting.BOLD, TextFormatting.LIGHT_PURPLE));
            tooltip.add(new StringTextComponent("Current: " + detailStored).withStyle(TextFormatting.WHITE));
            tooltip.add(new StringTextComponent("Capacity: " + detailMax).withStyle(TextFormatting.GRAY));

            this.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
        }

        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private boolean isHoveringArea(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private String formatValue(long value) {
        if (value < 1000) return String.valueOf(value);
        int exp = (int) (Math.log(value) / Math.log(1000));
        char unit = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f%s", value / Math.pow(1000, exp), unit);
    }
}