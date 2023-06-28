package dev.micalobia.breathinglib.mixin;

import dev.micalobia.breathinglib.data.BreathingInfo;
import dev.micalobia.breathinglib.event.BreathingCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.TypedActionResult;
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

	@Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
	public boolean BreathingLib$removeOldDrowningBehavior(LivingEntity instance, TagKey<Fluid> tagKey) {
		return false;
	}

	@Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getAir()I", ordinal = 2))
	public int BreathingLib$removeOldBreathingBehavior(LivingEntity instance) {
		return instance.getMaxAir();
	}

	@Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z", ordinal = 1))
	public void BreathingLib$addNewBreathingBehavior(CallbackInfo ci) {

		if(!this.isAlive()) {
			return;
		}

		++BreathingLib$tickCounter;
		BreathingLib$tickCounter &= 65535;

		//	Implement a new breathing behavior
		TypedActionResult<Optional<BreathingInfo>> result = BreathingCallback.EVENT.invoker().apply(self());
		BreathingInfo info;
        switch (result.getResult()) {
            case CONSUME, CONSUME_PARTIAL -> {}
            case SUCCESS, PASS -> {
                info = result.getValue().orElseGet(() -> BreathingInfo.gainingAir().build());
                if (BreathingLib$tickCounter % info.airDelta() == 0) {
	                BreathingLib$addAir(info.airPerCycle());
                }
            }
            case FAIL -> {

                info = result.getValue().orElseGet(() -> BreathingInfo.losingAir().build());
                if (BreathingLib$tickCounter % info.airDelta() == 0) {
	                BreathingLib$removeAir(info.airPerCycle(), info.ignoreRespiration());
                }

				if (this.getAir() > -info.damageAt()) {
					break;
				}

				this.setAir(0);
				DamageSource source = info.damageSource() != null ? info.damageSource() : this.getDamageSources().drown();
				if (!this.damage(source, info.damagePerCycle()) || info.particleEffect() == null) {
					break;
				}

				Vec3d velocity = this.getVelocity();
				for (int i = 0; i < 8; i++) {

					double x = this.getRandom().nextDouble() - this.getRandom().nextDouble();
					double y = this.getRandom().nextDouble() - this.getRandom().nextDouble();
					double z = this.getRandom().nextDouble() - this.getRandom().nextDouble();

					this.getWorld().addParticle(info.particleEffect(), this.getX() + x, this.getY() + y, this.getZ() + z, velocity.x, velocity.y, velocity.z);

				}

            }

        }

		if (!getWorld().isClient && this.isSubmergedIn(FluidTags.WATER) && this.hasVehicle() && this.getVehicle() != null && !this.getVehicle().shouldDismountUnderwater()) {
			this.stopRiding();
		}

	}

	private void BreathingLib$removeAir(int remove, boolean ignoreRespiration) {

		int air = this.getAir();
		if (ignoreRespiration) {
			this.setAir(air - remove);
		}

		int respirationLvl = EnchantmentHelper.getRespiration(self());
		if (respirationLvl <= 0 || this.getRandom().nextInt(respirationLvl + 1) <= 0) {
			this.setAir(air - remove);
		}

	}

	private void BreathingLib$addAir(int add) {
		this.setAir(Math.min(this.getAir() + add, this.getMaxAir()));
	}

}
