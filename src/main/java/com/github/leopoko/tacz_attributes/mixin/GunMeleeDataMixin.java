package com.github.leopoko.tacz_attributes.mixin;

import com.github.leopoko.tacz_attributes.attribute.CustomAttributes;
import com.github.leopoko.tacz_attributes.attribute.GunType;
import com.github.leopoko.tacz_attributes.util.AttributeValueHelper;
import com.github.leopoko.tacz_attributes.util.GunTypeResolver;
import com.github.leopoko.tacz_attributes.util.MeleeAttributeContext;
import com.tacz.guns.resource.pojo.data.gun.GunMeleeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GunMeleeData.class)
public class GunMeleeDataMixin {

    @Inject(method = "getDistance", at = @At("RETURN"), cancellable = true, remap = false)
    private void tacz_attributes$modifyBaseMeleeRange(CallbackInfoReturnable<Float> cir) {
        MeleeAttributeContext.Context context = MeleeAttributeContext.get();
        if (context == null) return;

        GunType gunType = GunTypeResolver.resolveFromItem(context.gunStack());
        double multiplier = AttributeValueHelper.getMultiplier(
                context.shooter(), CustomAttributes.MELEE_RANGE, gunType, GunType::getMeleeRangeAttribute);
        if (multiplier == 1.0) return;

        cir.setReturnValue((float) (cir.getReturnValueF() * multiplier));
    }
}
