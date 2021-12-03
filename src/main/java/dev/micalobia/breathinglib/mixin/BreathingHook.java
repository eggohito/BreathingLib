package dev.micalobia.breathinglib.mixin;

import com.google.common.base.Objects;
import dev.micalobia.breathinglib.data.BreathingInfo;
import dev.micalobia.breathinglib.event.BreathingCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Random;

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

	@Redirect(method = "baseTick", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z",
			ordinal = 1
	))
	public boolean BreathingLib$removeOldBreathingBehavior(LivingEntity instance) {
		return false;
	}


	@Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z", ordinal = 2))
	public void BreathingLib$addNewBreathingBehavior(CallbackInfo ci) {
		++BreathingLib$tickCounter;
		BreathingLib$tickCounter &= 65535;
		if(!this.isAlive()) return;
		BlockPos pos;
		if(!this.world.isClient) {
			if(!Objects.equal(((LivingEntityAccessor) this).getLastBlockPos(), pos = this.getBlockPos())) {
				((LivingEntityAccessor) this).setLastBlockPos(pos);
				((LivingEntityAccessor) this).callApplyMovementEffects(pos);
			}
			if(this.isSubmergedIn(FluidTags.WATER) && this.hasVehicle() && this.getVehicle() != null && !this.getVehicle().canBeRiddenInWater())
				this.stopRiding();
		}
		TypedActionResult<Optional<BreathingInfo>> result = BreathingCallback.EVENT.invoker().apply(self());
		BreathingInfo info;
		switch(result.getResult()) {
			case CONSUME, CONSUME_PARTIAL:
				return;
			case SUCCESS, PASS:
				info = result.getValue().orElseGet(() -> BreathingInfo.gainingAir().build());
				if(BreathingLib$tickCounter % info.airDelta() == 0)
					BreathingLib$addAir(info.airPerCycle());
				return;
			case FAIL:
				info = result.getValue().orElseGet(() -> BreathingInfo.losingAir().build());
				if(BreathingLib$tickCounter % info.airDelta() == 0)
					BreathingLib$removeAir(info.airPerCycle(), info.ignoreRespiration());
				if(this.getAir() <= -info.damageAt()) {
					this.setAir(0);
					if(info.particleEffect() != null) {
						if(this.damage(info.damageSource(), info.damagePerCycle())) {
							Vec3d vel = this.getVelocity();
							for(int i = 0; i < 8; ++i) {
								double x = this.getRandom().nextDouble() - this.getRandom().nextDouble();
								double y = this.getRandom().nextDouble() - this.getRandom().nextDouble();
								double z = this.getRandom().nextDouble() - this.getRandom().nextDouble();
								this.world.addParticle(info.particleEffect(), this.getX() + x, this.getY() + y, this.getZ() + z, vel.x, vel.y, vel.z);
							}
						}
					}
				}
		}
	}

	private void BreathingLib$removeAir(int remove, boolean ignoreRespiration) {
		int air = this.getAir();
		if(ignoreRespiration) this.setAir(air - remove);
		int i = EnchantmentHelper.getRespiration(self());
		if(i <= 0 || this.getRandom().nextInt(i + 1) <= 0) this.setAir(air - remove);
	}

	private void BreathingLib$addAir(int add) {
		this.setAir(Math.min(this.getAir() + add, this.getMaxAir()));
	}
}
