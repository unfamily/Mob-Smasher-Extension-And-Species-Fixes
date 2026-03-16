package net.unfamily.species_fix.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
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
 * When a FakePlayer (e.g. MGU Saw) attacks certain \"hard\" mobs via Player.attack, kill them directly
 * so their custom hurt() immunity never runs. This is a fallback to the AttackEntityEvent handler.
 * The list of entity IDs (modid:entity) is configured via species_fix-common.toml.
 */
@Mixin(value = Player.class, remap = false)
public class PlayerAttackQuakeRedirectMixin {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("SpeciesFix/PlayerAttack");

    static {
        LOGGER.info("[Species Fix] PlayerAttackQuakeRedirectMixin loaded (Saw/FakePlayer -> hard mobs kill active)");
    }

    private static boolean isFakePlayer(Player player) {
        if (player == null) return false;
        String name = player.getClass().getName();
        return name.contains("FakePlayer") || name.contains("MGUFakePlayer") || name.contains("mob_grinding_utils");
    }

    private static boolean isSawKillTarget(Entity entity) {
        if (entity == null) return false;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return id != null && net.unfamily.species_fix.Config.sawKillEntityIds.contains(id);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void species_fix$onAttackEntity(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity living)) return;
        Player self = (Player) (Object) this;
        if (!isFakePlayer(self) || !isSawKillTarget(living)) return;

        // Saw/FakePlayer attacking Quake: kill directly so Quake's hurt() is never invoked
        DamageSource source = living.damageSources().mobAttack(self);
        living.setHealth(0);
        living.die(source);
        LOGGER.info("[Species Fix] Saw/FakePlayer killed entity {} (PlayerAttackQuakeRedirectMixin)",
                BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()));
        ci.cancel();
    }
}
