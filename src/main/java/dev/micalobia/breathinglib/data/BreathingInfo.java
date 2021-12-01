package dev.micalobia.breathinglib.data;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.jetbrains.annotations.Nullable;

public record BreathingInfo(int airPerCycle, int airDelta, float damagePerCycle, int damageAt,
							boolean ignoreRespiration, DamageSource damageSource,
							@Nullable ParticleEffect particleEffect) {
	public static Builder losingAir() {
		return new Builder();
	}

	public static Builder gainingAir() {
		return new Builder().airPerCycle(4);
	}

	public static class Builder {
		private int airPerCycle;
		private int airDelta;
		private float damagePerCycle;
		private int damageAt;
		private boolean ignoreRespiration;
		private DamageSource damageSource;
		private @Nullable
		ParticleEffect particleEffect;

		protected Builder() {
			this.airPerCycle = 1;
			this.airDelta = 1;
			this.damagePerCycle = 2f;
			this.damageAt = 20;
			this.ignoreRespiration = false;
			this.damageSource = DamageSource.DROWN;
			this.particleEffect = ParticleTypes.BUBBLE;
		}

		public Builder airPerCycle(int airPerCycle) {
			this.airPerCycle = Math.max(0, airPerCycle);
			return this;
		}

		public Builder airDelta(int airDelta) {
			this.airDelta = Math.max(1, airDelta);
			return this;
		}

		public Builder damagePerCycle(float damagePerCycle) {
			this.damagePerCycle = Math.max(0f, damagePerCycle);
			return this;
		}

		public Builder damageAt(int damageAt) {
			this.damageAt = Math.max(1, damageAt);
			return this;
		}

		public Builder ignoreRespiration() {
			this.ignoreRespiration = true;
			return this;
		}

		public Builder damageSource(DamageSource damageSource) {
			this.damageSource = damageSource;
			return this;
		}

		public Builder particleEffect(@Nullable ParticleEffect particleEffect) {
			this.particleEffect = particleEffect;
			return this;
		}

		public BreathingInfo build() {
			return new BreathingInfo(airPerCycle, airDelta, damagePerCycle, damageAt, ignoreRespiration, damageSource, particleEffect);
		}
	}
}
