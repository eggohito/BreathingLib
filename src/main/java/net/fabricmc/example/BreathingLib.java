package net.fabricmc.example;

import com.google.common.base.Objects;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.data.BreathingInfo;
import net.fabricmc.example.event.BreathingCallback;
import net.fabricmc.example.mixin.LivingEntityAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class BreathingLib implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("breathinglib");

	static TypedActionResult<Optional<BreathingInfo>> vanillaBreathingBehaviour(LivingEntity entity) {
		BlockPos pos;
		if(!entity.world.isClient && !Objects.equal(((LivingEntityAccessor) entity).getLastBlockPos(), pos = entity.getBlockPos())) {
			((LivingEntityAccessor) entity).setLastBlockPos(pos);
			((LivingEntityAccessor) entity).callApplyMovementEffects(pos);
		}
		if(!entity.isSubmergedIn(FluidTags.WATER))
			return TypedActionResult.success(Optional.empty());
		if(entity.world.getBlockState(new BlockPos(entity.getX(), entity.getEyeY(), entity.getZ())).isOf(Blocks.BUBBLE_COLUMN))
			return TypedActionResult.success(Optional.empty());
		if(!entity.world.isClient && entity.hasVehicle() && entity.getVehicle() != null && !entity.getVehicle().canBeRiddenInWater())
			entity.stopRiding();
		if(entity.canBreatheInWater())
			return TypedActionResult.consume(Optional.empty());
		if(StatusEffectUtil.hasWaterBreathing(entity))
			return TypedActionResult.consume(Optional.empty());
		if(entity instanceof PlayerEntity player && player.getAbilities().invulnerable)
			return TypedActionResult.consume(Optional.empty());
		return TypedActionResult.fail(Optional.empty());
	}

	@Override
	public void onInitialize() {
		BreathingCallback.EVENT.register(BreathingCallback.VANILLA_PHASE, BreathingLib::vanillaBreathingBehaviour);
	}
}
