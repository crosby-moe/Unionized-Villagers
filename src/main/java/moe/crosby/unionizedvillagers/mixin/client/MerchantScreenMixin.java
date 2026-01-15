package moe.crosby.unionizedvillagers.mixin.client;

import moe.crosby.unionizedvillagers.IOshaViolationHolder;
import moe.crosby.unionizedvillagers.UnionizedVillagersClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {
    @Unique private boolean oshaViolation = false;

    private MerchantScreenMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void drawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        TradeOfferList tradeOfferList = this.handler.getRecipes();
        if (tradeOfferList.isEmpty()) return;
        for (var offer : tradeOfferList) {
            if (((IOshaViolationHolder) offer).villagerBalancing$getOshaViolationStatus()) {
                oshaViolation = true;

                context.drawTexture(UnionizedVillagersClient.WARNING_TEXTURE, backgroundWidth - 24, 8, 0, 0.0f, 0.0f, 16, 16, 16, 16);
                return;
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;II)V"))
    private void renderTooltip(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y) {
        instance.drawTooltip(textRenderer, oshaViolation ? Text.translatable("villager.gui.osha-violation") : text, x, y);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!oshaViolation) return;

        if (isPointWithinBounds(backgroundWidth - 24, 8, 16, 16, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.translatable("villager.gui.osha-violation"), mouseX, mouseY);
        }
    }
}
