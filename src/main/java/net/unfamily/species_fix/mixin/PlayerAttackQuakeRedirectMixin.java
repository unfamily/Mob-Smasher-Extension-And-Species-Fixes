package net.unfamily.species_fix.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When a FakePlayer (e.g. MGU Saw) attacks a Quake, kill the Quake directly so Quake's hurt() immunity never runs.
 * Saw calls fakePlayer.attack(entity); we intercept Player.attack(Entity) and if attacker is FakePlayer
 * and target is Quake, we kill the Quake and cancel the normal attack.
 */
@Mixin(value = Player.class, remap = false)
public class PlayerAttackQuakeRedirectMixin {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("SpeciesFix/PlayerAttack");
    private static final ResourceLocation QUAKE_ID = new ResourceLocation("species", "quake");

    static {
        LOGGER.info("[Species Fix] PlayerAttackQuakeRedirectMixin loaded (Saw/FakePlayer -> Quake kill active)");
    }

    private static boolean isFakePlayer(Player player) {
        if (player == null) return false;
        String name = player.getClass().getName();
        return name.contains("FakePlayer") || name.contains("MGUFakePlayer") || name.contains("mob_grinding_utils");
    }

    private static boolean isQuake(Entity entity) {
        if (entity == null) return false;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return QUAKE_ID.equals(id) || entity.getClass().getName().contains("Quake");
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void species_fix$onAttackEntity(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity living)) return;
        Player self = (Player) (Object) this;
        if (!isFakePlayer(self) || !isQuake(living)) return;

        // Saw/FakePlayer attacking Quake: kill directly so Quake's hurt() is never invoked
        DamageSource source = living.damageSources().mobAttack(self);
        living.setHealth(0);
        living.die(source);
        LOGGER.info("[Species Fix] Saw/FakePlayer killed Quake (PlayerAttackQuakeRedirectMixin)");
        ci.cancel();
    }
}
