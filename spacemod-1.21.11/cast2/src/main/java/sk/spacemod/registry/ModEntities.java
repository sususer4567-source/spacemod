package sk.spacemod.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import sk.spacemod.SpaceMod;
import sk.spacemod.entity.RocketEntity;

public final class ModEntities {
	private ModEntities() {}

	/** Kluc dimenzie Mesiac (data/spacemod/dimension/moon.json). */
	public static final ResourceKey<Level> MOON = ResourceKey.create(Registries.DIMENSION, SpaceMod.id("moon"));

	public static final EntityType<RocketEntity> ROCKET = register("rocket",
			EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
					.sized(1.4F, 3.2F)
					.clientTrackingRange(10)
					.updateInterval(1)
					.fireImmune()
					.passengerAttachments(0.8F));

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, SpaceMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		// staticke polia sa zaregistruju pri nacitani triedy
	}
}
