package dev.micalobia.breathinglib;

import dev.micalobia.breathinglib.event.BreathingCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class BreathingLib implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("breathinglib");

	/**
	 * Returns what Vanilla does
	 *
	 * @return <ul><li>{@link ActionResult#SUCCESS} means the entity is on land, gaining air, or in a bubble column
	 * <li>{@link ActionResult#FAIL} means the entity is under water, losing air
	 * <li>{@link ActionResult#CONSUME} means the entity is under water, but aren't losing air because of water breathing
	 * </ul>
	 */
	public static ActionResult vanillaBreathing(LivingEntity entity) {

		if (!entity.isSubmergedIn(FluidTags.WATER))
			return ActionResult.SUCCESS;

		if (entity.world.getBlockState(new BlockPos(entity.getX(), entity.getEyeY(), entity.getZ())).isOf(Blocks.BUBBLE_COLUMN))
			return ActionResult.SUCCESS;

		if (entity.canBreatheInWater())
			return ActionResult.CONSUME;

		if (StatusEffectUtil.hasWaterBreathing(entity))
			return ActionResult.CONSUME;

		if (entity instanceof PlayerEntity player && player.getAbilities().invulnerable)
			return ActionResult.CONSUME;

		return ActionResult.FAIL;

	}

	/**
	 * Equivelent to {@link BreathingCallback#EVENT}<code>.register(event)</code>
	 */
	public static void register(BreathingCallback event) {
		BreathingCallback.EVENT.register(event);
	}

	@Override
	public void onInitialize() {

        BreathingCallback.EVENT.register(
            BreathingCallback.VANILLA_PHASE,
            livingEntity -> switch (vanillaBreathing(livingEntity)) {
                case SUCCESS -> TypedActionResult.success(Optional.empty());
                case CONSUME, CONSUME_PARTIAL -> TypedActionResult.consume(Optional.empty());
                case PASS -> TypedActionResult.pass(Optional.empty());
                case FAIL -> TypedActionResult.fail(Optional.empty());
            }
        );

	}

}
