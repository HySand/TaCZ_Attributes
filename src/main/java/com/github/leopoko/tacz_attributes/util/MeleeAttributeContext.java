package com.github.leopoko.tacz_attributes.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class MeleeAttributeContext {

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private MeleeAttributeContext() {}

    public static void set(LivingEntity shooter, ItemStack gunStack) {
        CURRENT.set(new Context(shooter, gunStack));
    }

    public static void clear() {
        CURRENT.remove();
    }

    @Nullable
    public static Context get() {
        return CURRENT.get();
    }

    public record Context(LivingEntity shooter, ItemStack gunStack) {}
}
