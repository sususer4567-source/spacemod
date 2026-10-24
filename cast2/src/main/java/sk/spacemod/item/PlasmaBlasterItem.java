package sk.spacemod.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import sk.spacemod.registry.ModItems;

/** Plazmova zbran: pravy klik vystreli luc (hitscan), kazdy vystrel stoji 1 plazmovy clanok. */
public class PlasmaBlasterItem extends Item {
	private static final double RANGE = 40.0;
	private static final float DAMAGE = 9.0F;
	private static final int COOLDOWN_TICKS = 12;

	public PlasmaBlasterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(level instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		ItemStack stack = player.getItemInHand(hand);
		boolean creative = player.getAbilities().instabuild;

		if (!creative && !hasAmmo(player)) {
			player.displayClientMessage(Component.translatable("message.spacemod.no_ammo"), true);
			return InteractionResult.FAIL;
		}

		Vec3 start = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = start.add(look.scale(RANGE));

		// kam by luc dosiahol bez zasahu entity (stena)
		BlockHitResult blockHit = server.clip(new ClipContext(start, end,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 limit = blockHit.getLocation();

		// najblizsia entita na ceste luca
		AABB search = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0);
		List<Entity> candidates = server.getEntities(player, search,
				e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator());
		LivingEntity target = null;
		double best = start.distanceToSqr(limit);
		Vec3 impact = limit;
		for (Entity e : candidates) {
			var hit = e.getBoundingBox().inflate(0.3).clip(start, limit);
			if (hit.isPresent()) {
				double d = start.distanceToSqr(hit.get());
				if (d <= best) {
					best = d;
					target = (LivingEntity) e;
					impact = hit.get();
				}
			}
		}

		// vizual luca
		double length = start.distanceTo(impact);
		for (double t = 1.0; t < length; t += 0.6) {
			Vec3 p = start.add(look.scale(t));
			server.sendParticles(ParticleTypes.END_ROD, p.x, p.y - 0.1, p.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
		server.sendParticles(ParticleTypes.ELECTRIC_SPARK, impact.x, impact.y, impact.z, 12, 0.2, 0.2, 0.2, 0.2);

		if (target != null) {
			target.hurtServer(server, server.damageSources().playerAttack(player), DAMAGE);
		}

		server.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.6F);
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		if (!creative) {
			consumeAmmo(player);
		}
		return InteractionResult.SUCCESS;
	}

	private static boolean hasAmmo(Player player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).is(ModItems.PLASMA_CELL)) {
				return true;
			}
		}
		return false;
	}

	private static void consumeAmmo(Player player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.is(ModItems.PLASMA_CELL)) {
				s.shrink(1);
				return;
			}
		}
	}
}
