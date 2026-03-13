package net.unfamily.species_fix.mixin;

import com.ninni.species.server.entity.mob.update_3.Quake;
import net.minecraft.world.damagesource.DamageSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quake immunity is based on: getAttackCooldown() (when != 0 it "shields") and pose ATTACK/RECHARGE (absorbs).
 * 1) Saw (MGUFakePlayer): instant kill, no immunity.
 * 2) Lethal damage (amount >= health) when config enabled: bypass immunity and apply damage so Quake dies.
 */
@Mixin(value = Quake.class, remap = false)
public class QuakeMixin {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("SpeciesFix/QuakeMixin");

    static {
        LOGGER.info("[Species Fix] QuakeMixin loaded (Saw instant kill + lethal damage bypass active)");
    }

    private static boolean isMobSmasherDamage(DamageSource source) {
        if (source.getEntity() == null) return false;
        String name = source.getEntity().getClass().getName();
        return name.contains("MGUFakePlayer") || name.contains("FakePlayer") || name.contains("mob_grinding_utils");
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void species_fix$bypassImmunityOrDieIfLethal(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        Quake quake = (Quake) (Object) this;

        // Saw (Mob Grinding Utils): kill instantly, no shield/absorb
        if (isMobSmasherDamage(damageSource)) {
            quake.setHealth(0);
            quake.die(damageSource);
            LOGGER.info("[Species Fix] Saw damage killed Quake (QuakeMixin.hurt)");
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        // Lethal damage (>= current health): bypass immunity regardless of getAttackCooldown() or pose
        if (net.unfamily.species_fix.Config.quakeLethalDamageKills && amount >= quake.getHealth()) {
            float newHealth = quake.getHealth() - amount;
            quake.setHealth(Math.max(0, newHealth));
            if (quake.getHealth() <= 0) quake.die(damageSource);
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
