package sk.spacemod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import sk.spacemod.entity.RocketEntity;
import sk.spacemod.registry.ModEntities;

/** Polozi raketu na zem. */
public class RocketItem extends Item {
	public RocketItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!(level instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

		RocketEntity rocket = new RocketEntity(ModEntities.ROCKET, server);
		rocket.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		rocket.setYRot(context.getRotation());

		if (!server.noCollision(rocket)) {
			return InteractionResult.FAIL;
		}
		server.addFreshEntity(rocket);

		Player player = context.getPlayer();
		if (player == null || !player.getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
