package com.github.leopoko.tacz_attributes.util;

import com.github.leopoko.tacz_attributes.attribute.GunType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public final class AttributeValueHelper {

    private AttributeValueHelper() {}

    public static double getMultiplier(LivingEntity entity, Holder<Attribute> globalAttr,
                                       GunType gunType,
                                       Function<GunType, Holder<Attribute>> typeAttrGetter) {
        double global = getAttributeValue(entity, globalAttr, 1.0);
        double type = gunType != null ? getAttributeValue(entity, typeAttrGetter.apply(gunType), 1.0) : 1.0;
        return global * type;
    }

    public static double getAttributeValue(LivingEntity entity, Holder<Attribute> attribute, double fallback) {
        if (entity.getAttributes().hasAttribute(attribute)) {
            return entity.getAttributeValue(attribute);
        }
        return fallback;
    }
}
