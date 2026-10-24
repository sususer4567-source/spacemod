package sk.spacemod.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import sk.spacemod.SpaceMod;
import sk.spacemod.registry.ModEntities;
import sk.spacemod.registry.ModItems;

/** Nizka gravitacia a nedostatok kyslika na Mesiaci. */
public final class MoonManager {
	private MoonManager() {}

	private static final Identifier GRAVITY_ID = SpaceMod.id("moon_gravity");
	private static final Identifier FALL_ID = SpaceMod.id("moon_safe_fall");

	/** Kolko sekund vydrzi hrac bez helmy, nez zacne dostavat zranenie. */
	private static final int GRACE_SECONDS = 15;

	private static final Map<UUID, Integer> NO_OXYGEN_SECONDS = new HashMap<>();

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(MoonManager::tick);
	}

	private static void tick(MinecraftServer server) {
		boolean secondTick = server.getTickCount() % 20 == 0;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean onMoon = player.level().dimension() == ModEntities.MOON;

			setModifier(player, Attributes.GRAVITY, GRAVITY_ID, -0.65, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, onMoon);
			setModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_ID, 12.0, AttributeModifier.Operation.ADD_VALUE, onMoon);

			if (!onMoon || player.isCreative() || player.isSpectator()) {
				NO_OXYGEN_SECONDS.remove(player.getUUID());
				continue;
			}
			if (!secondTick) {
				continue;
			}

			boolean helmet = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.SPACE_HELMET);
			if (helmet) {
				NO_OXYGEN_SECONDS.remove(player.getUUID());
				continue;
			}

			int seconds = NO_OXYGEN_SECONDS.merge(player.getUUID(), 1, Integer::sum);
			if (seconds <= GRACE_SECONDS) {
				player.displayClientMessage(
						Component.translatable("message.spacemod.no_oxygen_warning", GRACE_SECONDS - seconds + 1), true);
			} else {
				player.displayClientMessage(Component.translatable("message.spacemod.suffocating"), true);
				ServerLevel level = (ServerLevel) player.level();
				player.hurtServer(level, level.damageSources().drown(), 2.0F);
			}
		}
	}

	private static void setModifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id,
									double amount, AttributeModifier.Operation operation, boolean enabled) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance == null) {
			return;
		}
		boolean present = instance.hasModifier(id);
		if (enabled && !present) {
			instance.addTransientModifier(new AttributeModifier(id, amount, operation));
		} else if (!enabled && present) {
			instance.removeModifier(id);
		}
	}
}
