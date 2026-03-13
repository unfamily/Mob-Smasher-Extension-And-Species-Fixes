package net.unfamily.species_fix;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SpeciesFixes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue SCREEN_SHAKE_FIX = BUILDER
            .comment("When true, prevents crash when camera is null during Species screen shake (recommended: true).")
            .define("screenShakeFix", true);

    private static final ForgeConfigSpec.BooleanValue QUAKE_LETHAL_DAMAGE_KILLS = BUILDER
            .comment("When true, Quake dies if incoming damage would be lethal (damage >= health) instead of starting immunity/attack.")
            .define("quakeLethalDamageKills", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Read by ScreenShakeEventMixin; true = apply null-check and return 0 when camera is null. */
    public static boolean screenShakeFixEnabled = true;
    /** Read by QuakeMixin; true = let Quake die when damage >= health. */
    public static boolean quakeLethalDamageKills = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        screenShakeFixEnabled = SCREEN_SHAKE_FIX.get();
        quakeLethalDamageKills = QUAKE_LETHAL_DAMAGE_KILLS.get();
    }
}
