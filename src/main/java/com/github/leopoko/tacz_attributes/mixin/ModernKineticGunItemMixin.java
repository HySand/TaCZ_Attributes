package com.github.leopoko.tacz_attributes.mixin;

import com.github.leopoko.tacz_attributes.util.MeleeAttributeContext;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModernKineticGunItem.class)
public class ModernKineticGunItemMixin {

    @Inject(method = "melee", at = @At("HEAD"), remap = false)
    private void tacz_attributes$startMeleeContext(ShooterDataHolder data, LivingEntity shooter,
                                                   ItemStack gunStack, CallbackInfo ci) {
        MeleeAttributeContext.set(shooter, gunStack);
    }

    @Inject(method = "melee", at = @At("RETURN"), remap = false)
    private void tacz_attributes$clearMeleeContext(ShooterDataHolder data, LivingEntity shooter,
                                                   ItemStack gunStack, CallbackInfo ci) {
        MeleeAttributeContext.clear();
    }
}
