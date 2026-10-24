package sk.spacemod.registry;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.Equippable;
import sk.spacemod.SpaceMod;
import sk.spacemod.item.PlasmaBlasterItem;
import sk.spacemod.item.RocketItem;

public final class ModItems {
	private ModItems() {}

	public static final TagKey<Item> TITANIUM_REPAIR =
			TagKey.create(Registries.ITEM, SpaceMod.id("repairs_titanium_gear"));

	/** Titan: o nieco lepsi ako zelezo, takmer ako diamant. */
	public static final ToolMaterial TITANIUM = new ToolMaterial(
			BlockTags.INCORRECT_FOR_IRON_TOOL, 900, 7.5F, 2.5F, 14, TITANIUM_REPAIR);

	public static final Item RAW_TITANIUM = register("raw_titanium", Item::new);
	public static final Item TITANIUM_INGOT = register("titanium_ingot", Item::new);
	public static final Item ROCKET_FUEL = register("rocket_fuel", Item::new);
	public static final Item PLASMA_CELL = register("plasma_cell", Item::new);

	public static final Item ROCKET = register("rocket", p -> new RocketItem(p.stacksTo(1)));
	public static final Item PLASMA_BLASTER = register("plasma_blaster", p -> new PlasmaBlasterItem(p.stacksTo(1)));

	public static final Item TITANIUM_SWORD = register("titanium_sword",
			p -> new Item(p.sword(TITANIUM, 3.0F, -2.4F)));
	public static final Item TITANIUM_AXE = register("titanium_axe",
			p -> new Item(p.axe(TITANIUM, 5.0F, -3.0F)));
	public static final Item TITANIUM_PICKAXE = register("titanium_pickaxe",
			p -> new Item(p.pickaxe(TITANIUM, 1.0F, -2.8F)));

	/** Vesmirna helma: bez nej sa na Mesiaci nedychá. Nosi sa na hlave. */
	public static final Item SPACE_HELMET = register("space_helmet",
			p -> new Item(p.stacksTo(1)
					.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())));

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SpaceMod.id(name));
		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.accept(ModBlocks.MOON_STONE);
			entries.accept(ModBlocks.TITANIUM_BLOCK);
			entries.accept(ModBlocks.GLOW_CRYSTAL);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.accept(ModBlocks.TITANIUM_ORE);
			entries.accept(ModBlocks.MOON_DUST);
			entries.accept(ModBlocks.MOON_STONE);
			entries.accept(ModBlocks.GLOW_CRYSTAL);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(RAW_TITANIUM);
			entries.accept(TITANIUM_INGOT);
			entries.accept(ROCKET_FUEL);
			entries.accept(PLASMA_CELL);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.accept(TITANIUM_SWORD);
			entries.accept(TITANIUM_AXE);
			entries.accept(PLASMA_BLASTER);
			entries.accept(SPACE_HELMET);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(TITANIUM_PICKAXE);
			entries.accept(ROCKET);
		});
	}
}
