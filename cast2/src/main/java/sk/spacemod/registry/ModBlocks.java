package sk.spacemod.registry;

import java.util.function.UnaryOperator;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import sk.spacemod.SpaceMod;

public final class ModBlocks {
	private ModBlocks() {}

	public static final Block MOON_STONE = register("moon_stone", p -> p
			.mapColor(MapColor.COLOR_LIGHT_GRAY)
			.strength(2.0F, 6.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE));

	public static final Block MOON_DUST = register("moon_dust", p -> p
			.mapColor(MapColor.COLOR_GRAY)
			.strength(0.6F)
			.sound(SoundType.SAND));

	public static final Block TITANIUM_ORE = register("titanium_ore", p -> p
			.mapColor(MapColor.STONE)
			.strength(3.5F, 3.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.STONE));

	public static final Block TITANIUM_BLOCK = register("titanium_block", p -> p
			.mapColor(MapColor.METAL)
			.strength(5.0F, 6.0F)
			.requiresCorrectToolForDrops()
			.sound(SoundType.METAL));

	public static final Block GLOW_CRYSTAL = register("glow_crystal", p -> p
			.mapColor(MapColor.COLOR_CYAN)
			.strength(0.8F)
			.lightLevel(state -> 12)
			.sound(SoundType.GLASS));

	private static Block register(String name, UnaryOperator<BlockBehaviour.Properties> config) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, SpaceMod.id(name));
		Block block = new Block(config.apply(BlockBehaviour.Properties.of()).setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, SpaceMod.id(name));
		BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return block;
	}

	public static void init() {
		// staticke polia sa zaregistruju pri nacitani triedy
	}
}
