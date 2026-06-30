package com.github.leopoko.tacz_attributes.mixin;

import com.github.leopoko.tacz_attributes.attribute.CustomAttributes;
import com.github.leopoko.tacz_attributes.attribute.GunType;
import com.github.leopoko.tacz_attributes.util.AttributeValueHelper;
import com.github.leopoko.tacz_attributes.util.GunTypeResolver;
import com.github.leopoko.tacz_attributes.util.MeleeAttributeContext;
import com.tacz.guns.resource.pojo.data.gun.GunDefaultMeleeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GunDefaultMeleeData.class)
public class GunDefaultMeleeDataMixin {

    @Inject(method = "getDistance", at = @At("RETURN"), cancellable = true, remap = false)
    private void tacz_attributes$modifyDefaultMeleeRange(CallbackInfoReturnable<Float> cir) {
        tacz_attributes$modifyMultiplier(cir, true);
    }

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true, remap = false)
    private void tacz_attributes$modifyDefaultMeleeDamage(CallbackInfoReturnable<Float> cir) {
        tacz_attributes$modifyMultiplier(cir, false);
    }

    @Unique
    private void tacz_attributes$modifyMultiplier(CallbackInfoReturnable<Float> cir, boolean range) {
        MeleeAttributeContext.Context context = MeleeAttributeContext.get();
        if (context == null) return;

        GunType gunType = GunTypeResolver.resolveFromItem(context.gunStack());
        double multiplier = range
                ? AttributeValueHelper.getMultiplier(
                        context.shooter(), CustomAttributes.MELEE_RANGE, gunType, GunType::getMeleeRangeAttribute)
                : AttributeValueHelper.getMultiplier(
                        context.shooter(), CustomAttributes.MELEE_DAMAGE, gunType, GunType::getMeleeDamageAttribute);
        if (multiplier == 1.0) return;

        cir.setReturnValue((float) (cir.getReturnValueF() * multiplier));
    }
}
