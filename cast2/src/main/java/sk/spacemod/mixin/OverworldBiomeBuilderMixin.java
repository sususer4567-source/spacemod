package sk.spacemod.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import sk.spacemod.worldgen.BiomeSwap;

/**
 * Obali "consumer", do ktoreho vanilla zapisuje biomy Overworldu,
 * a cast bodov zameni za nase biomy (Crystal Meadow, Meteor Wastes).
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	@ModifyVariable(method = "addBiomes", at = @At("HEAD"), argsOnly = true, require = 0)
	private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> spacemod$swapBiomes(
			Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> original) {
		return pair -> original.accept(BiomeSwap.swap(pair));
	}
}
