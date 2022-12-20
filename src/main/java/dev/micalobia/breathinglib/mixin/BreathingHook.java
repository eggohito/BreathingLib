package dev.micalobia.breathinglib.mixin;

import com.google.common.base.Objects;
import dev.micalobia.breathinglib.data.BreathingInfo;
import dev.micalobia.breathinglib.event.BreathingCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class BreathingHook extends Entity {

	private int BreathingLib$tickCounter = 0;

	public BreathingHook(EntityType<?> type, World world) {
		super(type, world);
	}

	private LivingEntity self() {
		return (LivingEntity) (Object) this;
	}

	@Shadow
	public abstract boolean isAlive();

	@Shadow
	public abstract Random getRandom();

	@Shadow
	public abstract boolean damage(DamageSource source, float amount);

	@Shadow public abstract boolean isInsideWall();

	@Shadow public abstract void stopRiding();

	@Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z", ordinal = 0))
	public boolean BreathingLib$removeOldBreathingBehavior(LivingEntity instance) {
		return false;
	}

	@Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z", ordinal = 1))
	public void BreathingLib$addNewBreathingBehavior(CallbackInfo ci) {

		++BreathingLib$tickCounter;
		BreathingLib$tickCounter &= 65535;

		if(!this.isAlive()) return;

		//	Re-implement vanilla's suffocation behavior
		if (this.isInsideWall()) this.damage(DamageSource.IN_WALL, 1.0F);
		else if (self() instanceof PlayerEntity playerEntity && playerEntity.world.getWorldBorder().contains(playerEntity.getBoundingBox())) {
			double d = playerEntity.world.getWorldBorder().getDistanceInsideBorder(playerEntity) + playerEntity.world.getWorldBorder().getSafeZone();
			if (d < 0.0) {
				double e = playerEntity.world.getWorldBorder().getDamagePerBlock();
				if (e > 0.0) this.damage(DamageSource.IN_WALL, Math.max(1, MathHelper.floor(-d * e)));
			}
		}

		//	Implement a new breathing behavior
		TypedActionResult<Optional<BreathingInfo>> result = BreathingCallback.EVENT.invoker().apply(self());
		BreathingInfo info;
        switch (result.getResult()) {
            case CONSUME, CONSUME_PARTIAL -> {}
            case SUCCESS, PASS -> {
                info = result.getValue().orElseGet(() -> BreathingInfo.gainingAir().build());
                if (BreathingLib$tickCounter % info.airDelta() == 0) BreathingLib$addAir(info.airPerCycle());
            }
            case FAIL -> {

                info = result.getValue().orElseGet(() -> BreathingInfo.losingAir().build());
                if (BreathingLib$tickCounter % info.airDelta() == 0) BreathingLib$removeAir(info.airPerCycle(), info.ignoreRespiration());

				if (this.getAir() > -info.damageAt()) break;
				this.setAir(0);

				if (!this.damage(info.damageSource(), info.damagePerCycle())) break;
				if (info.particleEffect() == null) break;

				Vec3d velocity = this.getVelocity();
				for (int i = 0; i < 8; i++) {

					double x = this.getRandom().nextDouble() - this.getRandom().nextDouble();
					double y = this.getRandom().nextDouble() - this.getRandom().nextDouble();
					double z = this.getRandom().nextDouble() - this.getRandom().nextDouble();

					this.world.addParticle(info.particleEffect(), this.getX() + x, this.getY() + y, this.getZ() + z, velocity.x, velocity.y, velocity.z);

				}

            }

        }

		if (this.world.isClient) return;
		if (this.isSubmergedIn(FluidTags.WATER) && this.hasVehicle() && this.getVehicle() != null && !this.getVehicle().canBeRiddenInWater()) this.stopRiding();

		BlockPos blockPos = this.getBlockPos();
		if (Objects.equal(((LivingEntityAccessor) this).getLastBlockPos(), blockPos)) return;

		((LivingEntityAccessor) this).setLastBlockPos(blockPos);
		((LivingEntityAccessor) this).callApplyMovementEffects(blockPos);

	}

	private void BreathingLib$removeAir(int remove, boolean ignoreRespiration) {

		int air = this.getAir();
		if (ignoreRespiration) this.setAir(air - remove);

		int respirationLvl = EnchantmentHelper.getRespiration(self());
		if (respirationLvl <= 0 || this.getRandom().nextInt(respirationLvl + 1) <= 0) this.setAir(air - remove);

	}

	private void BreathingLib$addAir(int add) {
		this.setAir(Math.min(this.getAir() + add, this.getMaxAir()));
	}

}
