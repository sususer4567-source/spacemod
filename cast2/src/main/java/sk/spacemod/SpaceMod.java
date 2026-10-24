package sk.spacemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.spacemod.entity.RocketEntity;
import sk.spacemod.event.MoonManager;
import sk.spacemod.registry.ModBlocks;
import sk.spacemod.registry.ModEntities;
import sk.spacemod.registry.ModItems;

public class SpaceMod implements ModInitializer {
	public static final String MOD_ID = "spacemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// poradie je dôležité: bloky -> itemy -> entity
		ModBlocks.init();
		ModItems.init();
		ModEntities.init();

		// pravý klik na raketu (nastúpenie / tankovanie)
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (entity instanceof RocketEntity rocket) {
				return rocket.onPlayerUse(player, hand);
			}
			return InteractionResult.PASS;
		});

		// titánová ruda aj v klasickom overworlde
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES,
				ResourceKey.create(Registries.PLACED_FEATURE, id("ore_titanium")));

		// gravitácia a kyslík na Mesiaci
		MoonManager.init();

		LOGGER.info("Space Expedition nacitany - dobry let!");
	}
}
