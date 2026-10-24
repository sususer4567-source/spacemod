package sk.spacemod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import sk.spacemod.registry.ModEntities;

public class SpaceModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Raketa sa zatial kresli ako velky 2D obrazok (textura itemu "rocket").
		// Neskor sa da nahradit vlastnym 3D modelom.
		EntityRendererRegistry.register(ModEntities.ROCKET,
				context -> new ThrownItemRenderer<>(context, 3.0F, true));
	}
}
