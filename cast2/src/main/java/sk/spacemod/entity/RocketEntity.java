package sk.spacemod.entity;

import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import sk.spacemod.registry.ModEntities;
import sk.spacemod.registry.ModItems;

/**
 * Raketa. Pravy klik s raketovym palivom = natankovanie, pravy klik prazdnou rukou = nastupenie.
 * Po nastupeni s palivom odpocitava 5 sekund, vzlietne a pri vyske {@value #SPACE_HEIGHT}
 * prenesie jazdca medzi Overworldom a Mesiacom. V cieli pristane na novej rakete.
 */
public class RocketEntity extends Entity implements ItemSupplier {
	public static final int IDLE = 0;
	public static final int COUNTDOWN = 1;
	public static final int ASCENDING = 2;
	public static final int DESCENDING = 3;

	private static final int COUNTDOWN_TICKS = 100;
	private static final double SPACE_HEIGHT = 360.0;

	private int fuel;
	private int phase = IDLE;
	private int timer;
	private double climbSpeed;

	public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.fuel = input.getIntOr("Fuel", 0);
		int savedPhase = input.getIntOr("Phase", IDLE);
		// let sa neda obnovit uprostred vzletu, ale pristavanie ano
		this.phase = savedPhase == DESCENDING ? DESCENDING : IDLE;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("Fuel", this.fuel);
		output.putInt("Phase", this.phase);
	}

	@Override
	public ItemStack getItem() {
		return new ItemStack(ModItems.ROCKET);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().isEmpty();
	}

	/** Zavolane z UseEntityCallback pri pravom kliku hraca na raketu. */
	public InteractionResult onPlayerUse(Player player, InteractionHand hand) {
		if (!(this.level() instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		ItemStack held = player.getItemInHand(hand);

		if (held.is(ModItems.ROCKET_FUEL)) {
			if (this.phase != IDLE) {
				return InteractionResult.SUCCESS;
			}
			if (this.fuel >= 1) {
				message(player, "message.spacemod.rocket_full");
				return InteractionResult.SUCCESS;
			}
			if (!player.getAbilities().instabuild) {
				held.shrink(1);
			}
			this.fuel = 1;
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
			message(player, "message.spacemod.rocket_fueled");
			return InteractionResult.SUCCESS;
		}

		if (this.phase != IDLE || !this.getPassengers().isEmpty() || player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}
		player.startRiding(this);
		if (this.fuel <= 0) {
			message(player, "message.spacemod.rocket_no_fuel");
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (this.isRemoved()) {
			return false;
		}
		// rakety sa da zobrat pesťou (len ked stoji a nikto v nej nesedi)
		if (this.phase == IDLE && this.getPassengers().isEmpty() && source.getEntity() instanceof Player player) {
			if (!player.getAbilities().instabuild) {
				this.spawnAtLocation(level, new ItemStack(ModItems.ROCKET));
			}
			this.discard();
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel server)) {
			return;
		}
		Entity rider = this.getFirstPassenger();

		switch (this.phase) {
			case IDLE -> tickIdle(server, rider);
			case COUNTDOWN -> tickCountdown(server, rider);
			case ASCENDING -> tickAscending(server, rider);
			case DESCENDING -> tickDescending(server, rider);
			default -> this.phase = IDLE;
		}
	}

	private void tickIdle(ServerLevel server, Entity rider) {
		// drz sa zeme
		this.move(MoverType.SELF, new Vec3(0.0, -0.15, 0.0));

		if (!(rider instanceof ServerPlayer player) || this.fuel <= 0) {
			return;
		}
		boolean rightWorld = server.dimension() == Level.OVERWORLD || server.dimension() == ModEntities.MOON;
		if (!rightWorld) {
			if (this.tickCount % 60 == 0) {
				message(player, "message.spacemod.wrong_world");
			}
			return;
		}
		if (!server.canSeeSky(this.blockPosition().above(4))) {
			if (this.tickCount % 60 == 0) {
				message(player, "message.spacemod.blocked");
			}
			return;
		}
		this.phase = COUNTDOWN;
		this.timer = COUNTDOWN_TICKS;
	}

	private void tickCountdown(ServerLevel server, Entity rider) {
		if (!(rider instanceof ServerPlayer player)) {
			// hrac vystupil, zrusene
			this.phase = IDLE;
			return;
		}
		this.timer--;
		if (this.timer % 20 == 0 && this.timer > 0) {
			player.displayClientMessage(Component.translatable("message.spacemod.countdown", this.timer / 20), true);
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.0F, 0.6F);
		}
		server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.1, this.getZ(),
				3, 0.4, 0.05, 0.4, 0.01);
		if (this.timer <= 0) {
			this.fuel = 0;
			this.phase = ASCENDING;
			this.climbSpeed = 0.0;
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 3.0F, 0.5F);
		}
	}

	private void tickAscending(ServerLevel server, Entity rider) {
		this.climbSpeed = Math.min(this.climbSpeed + 0.035, 1.6);
		this.move(MoverType.SELF, new Vec3(0.0, this.climbSpeed, 0.0));

		server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
				8, 0.2, 0.1, 0.2, 0.03);
		server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() - 0.3, this.getZ(),
				4, 0.3, 0.2, 0.3, 0.02);

		if (this.verticalCollision) {
			// zasekla sa o strop
			this.phase = IDLE;
			this.climbSpeed = 0.0;
			if (rider instanceof ServerPlayer player) {
				message(player, "message.spacemod.crashed");
			}
			return;
		}
		if (this.getY() >= SPACE_HEIGHT) {
			travel(server, rider);
		}
	}

	private void tickDescending(ServerLevel server, Entity rider) {
		int ground = server.getHeight(Heightmap.Types.MOTION_BLOCKING, this.blockPosition().getX(), this.blockPosition().getZ());
		double distance = this.getY() - ground;
		double speed = Mth.clamp(distance * 0.06, 0.12, 1.2);
		this.move(MoverType.SELF, new Vec3(0.0, -speed, 0.0));

		server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
				4, 0.2, 0.05, 0.2, 0.02);

		if (this.onGround()) {
			this.phase = IDLE;
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 1.0F, 0.6F);
			if (rider instanceof ServerPlayer player) {
				message(player, "message.spacemod.landed");
			}
		}
	}

	/** Prenesie jazdca do druhej dimenzie a vytvori tam novu raketu, ktora pristane. */
	private void travel(ServerLevel server, Entity rider) {
		ResourceKey<Level> targetKey = server.dimension() == ModEntities.MOON ? Level.OVERWORLD : ModEntities.MOON;
		ServerLevel target = server.getServer().getLevel(targetKey);
		if (target == null) {
			this.phase = IDLE;
			if (rider instanceof ServerPlayer player) {
				message(player, "message.spacemod.no_destination");
			}
			return;
		}

		double x = this.getX();
		double z = this.getZ();
		double startY = SPACE_HEIGHT - 10.0;

		RocketEntity landing = new RocketEntity(ModEntities.ROCKET, target);
		landing.setPos(x, startY, z);
		landing.phase = DESCENDING;
		landing.fuel = 0;

		ServerPlayer player = rider instanceof ServerPlayer sp ? sp : null;
		if (player != null) {
			player.stopRiding();
			player.teleportTo(target, x, startY + 1.0, z, Set.of(), player.getYRot(), player.getXRot(), true);
		}
		target.addFreshEntity(landing);
		if (player != null) {
			player.startRiding(landing, true);
			player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0, false, false));
		}
		this.discard();
	}

	private static void message(Player player, String key) {
		player.displayClientMessage(Component.translatable(key), true);
	}
}
