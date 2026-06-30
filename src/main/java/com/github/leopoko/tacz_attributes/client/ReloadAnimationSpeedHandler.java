package com.github.leopoko.tacz_attributes.client;

import com.github.leopoko.tacz_attributes.Tacz_attributes;
import com.github.leopoko.tacz_attributes.api.ISpeedModifiable;
import com.github.leopoko.tacz_attributes.attribute.CustomAttributes;
import com.github.leopoko.tacz_attributes.attribute.GunType;
import com.github.leopoko.tacz_attributes.util.GunTypeResolver;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.DiscreteTrackArray;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * クライアント側でリロード/ボルトアニメーションの速度を属性に追従させるイベントハンドラ。
 * <p>
 * 毎クライアントtickで以下を行う:
 * 1. プレイヤーのリロード/ボルト状態を確認
 * 2. リロード中であればリロード速度倍率、ボルト中であればコッキング速度倍率を
 *    MAIN_TRACKのアニメーションランナーに設定
 * 3. どちらも終了した時に速度倍率をリセット
 */
@EventBusSubscriber(modid = Tacz_attributes.MODID, value = Dist.CLIENT)
public class ReloadAnimationSpeedHandler {

    /** アニメーション速度が変更中であることを示すフラグ */
    private static boolean wasSpeedModified = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        ReloadState reloadState = gunOperator.getSynReloadState();
        boolean isReloading = reloadState.getStateType().isReloading();
        boolean isBolting = gunOperator.getSynIsBolting();

        if (isReloading) {
            double speed = getReloadSpeedModifier(player);
            if (speed != 1.0) {
                applySpeedToActiveTracks(player, (float) speed);
            }
            wasSpeedModified = true;
        } else if (isBolting) {
            double speed = getBoltSpeedModifier(player);
            if (speed != 1.0) {
                applySpeedToActiveTracks(player, (float) speed);
            }
            wasSpeedModified = true;
        } else if (wasSpeedModified) {
            // リロード/ボルト終了時に速度倍率をリセット
            applySpeedToActiveTracks(player, 1.0f);
            wasSpeedModified = false;
        }
    }

    private static double getReloadSpeedModifier(LocalPlayer player) {
        // 全体リロード速度倍率
        double globalSpeed = 1.0;
        if (player.getAttributes().hasAttribute(CustomAttributes.RELOAD_SPEED)) {
            globalSpeed = player.getAttributeValue(CustomAttributes.RELOAD_SPEED);
        }

        // 銃種別リロード速度倍率
        double typeSpeed = 1.0;
        ItemStack mainHand = player.getMainHandItem();
        GunType gunType = GunTypeResolver.resolveFromItem(mainHand);
        if (gunType != null) {
            var typeAttr = gunType.getReloadSpeedAttribute();
            if (player.getAttributes().hasAttribute(typeAttr)) {
                typeSpeed = player.getAttributeValue(typeAttr);
            }
        }

        return globalSpeed * typeSpeed;
    }

    private static double getBoltSpeedModifier(LocalPlayer player) {
        // 全体コッキング速度倍率
        double globalSpeed = 1.0;
        if (player.getAttributes().hasAttribute(CustomAttributes.BOLT_ACTION_SPEED)) {
            globalSpeed = player.getAttributeValue(CustomAttributes.BOLT_ACTION_SPEED);
        }

        // 銃種別コッキング速度倍率
        double typeSpeed = 1.0;
        ItemStack mainHand = player.getMainHandItem();
        GunType gunType = GunTypeResolver.resolveFromItem(mainHand);
        if (gunType != null) {
            var typeAttr = gunType.getBoltActionSpeedAttribute();
            if (player.getAttributes().hasAttribute(typeAttr)) {
                typeSpeed = player.getAttributeValue(typeAttr);
            }
        }

        return globalSpeed * typeSpeed;
    }

    /**
     * 現在のステートマシンが更新対象にしている全トラックへ速度倍率を設定する。
     * 新しい銃パックではリロード/ボルトが固定の MAIN_TRACK 以外に割り当てられることがある。
     */
    private static void applySpeedToActiveTracks(LocalPlayer player, float speed) {
        ItemStack mainHand = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(mainHand);
        if (iGun == null) return;

        GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHand).orElse(null);
        if (display == null) return;

        AnimationStateMachine<?> stateMachine = display.getAnimationStateMachine();
        if (stateMachine == null || !stateMachine.isInitialized()) return;

        AnimationController controller = stateMachine.getAnimationController();
        if (controller == null) return;

        // DiscreteTrackArray からMAIN_TRACKのコントローラポインタを取得
        var context = stateMachine.getContext();
        if (context == null) return;

        DiscreteTrackArray trackArray = context.getTrackArray();
        for (int trackPointer : trackArray) {
            ObjectAnimationRunner runner = controller.getAnimation(trackPointer);
            if (runner == null) continue;
            setSpeedOnRunnerAndTransition(runner, speed);
        }
    }

    private static void setSpeedOnRunnerAndTransition(ObjectAnimationRunner runner, float speed) {
        setSpeedOnRunner(runner, speed);
        ObjectAnimationRunner transitionTo = runner.getTransitionTo();
        if (transitionTo != null) {
            setSpeedOnRunner(transitionTo, speed);
        }
    }

    private static void setSpeedOnRunner(ObjectAnimationRunner runner, float speed) {
        if (runner instanceof ISpeedModifiable modifiable) {
            modifiable.tacz_attributes$setSpeedMultiplier(speed);
        }
    }
}
