package sk.spacemod.worldgen;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import sk.spacemod.SpaceMod;

/**
 * Do klasickeho Overworldu pridava nase biomy tak, ze zamiena cast vanilla bodov
 * v tabulke biomov (Climate.ParameterPoint).
 * <ul>
 *   <li>Crystal Meadow nahradza cast nizin (plains) s vyssou "weirdness"</li>
 *   <li>Meteor Wastes nahradza cast pusti (desert)</li>
 * </ul>
 */
public final class BiomeSwap {
	private BiomeSwap() {}

	public static final ResourceKey<Biome> CRYSTAL_MEADOW =
			ResourceKey.create(Registries.BIOME, SpaceMod.id("crystal_meadow"));
	public static final ResourceKey<Biome> METEOR_WASTES =
			ResourceKey.create(Registries.BIOME, SpaceMod.id("meteor_wastes"));

	private static final long HIGH_WEIRDNESS = Climate.quantizeCoord(0.26F);

	public static Pair<Climate.ParameterPoint, ResourceKey<Biome>> swap(Pair<Climate.ParameterPoint, ResourceKey<Biome>> pair) {
		Climate.ParameterPoint point = pair.getFirst();
		ResourceKey<Biome> biome = pair.getSecond();

		if (biome == Biomes.PLAINS && point.weirdness().min() >= HIGH_WEIRDNESS) {
			return Pair.of(point, CRYSTAL_MEADOW);
		}
		if (biome == Biomes.DESERT && point.weirdness().max() > 0L) {
			return Pair.of(point, METEOR_WASTES);
		}
		return pair;
	}
}
