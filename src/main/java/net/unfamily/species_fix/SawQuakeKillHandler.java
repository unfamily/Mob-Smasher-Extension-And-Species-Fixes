package net.unfamily.species_fix;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 * When a FakePlayer (e.g. MGU Saw) attacks a Quake, kill the Quake and cancel the attack.
 * This runs on AttackEntityEvent so we don't depend on Player.attack() mixin (which may not
 * apply in production due to obfuscation).
 */
public class SawQuakeKillHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation QUAKE_ID = new ResourceLocation("species", "quake");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity living)) return;

        if (!isFakePlayer(player)) return;
        if (!isQuake(target)) return;

        // Kill Quake and cancel so normal attack (and Quake.hurt()) never runs
        DamageSource source = living.damageSources().mobAttack(player);
        living.setHealth(0);
        living.die(source);
        event.setCanceled(true);
        LOGGER.info("[Species Fix] Saw/FakePlayer killed Quake (AttackEntityEvent)");
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
}
