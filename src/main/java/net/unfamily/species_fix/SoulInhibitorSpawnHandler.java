package net.unfamily.species_fix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.unfamily.species_fix.block.SoulInhibitorBlock;

public class SoulInhibitorSpawnHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!Config.soulInhibitorEnabled) return;

        Entity entity = event.getEntity();
        @SuppressWarnings("null")
        EntityType<?> type = entity.getType();
        ResourceLocation typeId = getEntityTypeId(type);
        if (!isForbiddenArcanusLostSoul(typeId)) return;

        Level level = (Level) event.getLevel();
        BlockPos spawnPos = entity.blockPosition();

        int r = Math.max(1, Config.soulInhibitorRadius);
        if (!hasSoulInhibitorNearby(level, spawnPos, r)) return;

        // Cancel spawn
        event.setCanceled(true);
    }

    private static boolean isForbiddenArcanusLostSoul(ResourceLocation id) {
        if (id == null) return false;
        if (!"forbidden_arcanus".equals(id.getNamespace())) return false;
        // The mod has multiple forms; keep matching stable by path substring.
        return id.getPath().contains("lost_soul");
    }

    @SuppressWarnings({"deprecation", "null"})
    private static ResourceLocation getEntityTypeId(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }

    @SuppressWarnings("null")
    private static boolean hasSoulInhibitorNearby(Level level, BlockPos center, int r) {
        Block inhibitor = net.unfamily.species_fix.registry.ModBlocks.SOUL_INHIBITOR.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        int r2 = r * r;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    int dist2 = dx * dx + dy * dy + dz * dz;
                    if (dist2 > r2) continue;
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    var state = level.getBlockState(cursor);
                    if (state.is(inhibitor) && state.getValue(SoulInhibitorBlock.ON)) return true;
                }
            }
        }
        return false;
    }
}

