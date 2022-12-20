package dev.micalobia.breathinglib.data;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Information on how {@link dev.micalobia.breathinglib.event.BreathingCallback#EVENT} handles the specifics of losing or gaining air.<br>
 * You shouldn't have to instantiate this directly, instead using {@link BreathingInfo#losingAir()} and {@link BreathingInfo#gainingAir()}
 *
 * @see Builder
 */
public record BreathingInfo(int airPerCycle, int airDelta, float damagePerCycle, int damageAt,
							boolean ignoreRespiration, DamageSource damageSource,
							@Nullable ParticleEffect particleEffect) {

	/**
	 * @return A builder with vanilla values for losing air
	 */
	@NotNull
	@Contract(pure = true)
	public static Builder losingAir() {
		return new Builder();
	}

	/**
	 * @return A builder with vanilla values for gaining air
	 */
	@NotNull
	@Contract(pure = true)
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

		public Builder() {
			this.airPerCycle = 1;
			this.airDelta = 1;
			this.damagePerCycle = 2f;
			this.damageAt = 20;
			this.ignoreRespiration = false;
			this.damageSource = DamageSource.DROWN;
			this.particleEffect = ParticleTypes.BUBBLE;
		}

		/**
		 * Sets how much air the entity gains or loses per air cycle. <br>
		 * Default values for vanilla are <code>4</code> and <code>1</code> for gaining and losing, respectively.
		 * Can't go below 0.
		 */
		@Contract("_ -> this")
		public Builder airPerCycle(int airPerCycle) {
			this.airPerCycle = Math.max(0, airPerCycle);
			return this;
		}

		/**
		 * Sets the number of ticks between each air cycle. <br>
		 * Default is <code>1</code>, and can't go below that.
		 */
		@Contract("_ -> this")
		public Builder airDelta(int airDelta) {
			this.airDelta = Math.max(1, airDelta);
			return this;
		}

		/**
		 * Sets how much damage the entity takes per damage cycle.
		 * Default is <code>2.0f</code>, can't go below <code>0.0f</code>.
		 */
		@Contract("_ -> this")
		public Builder damagePerCycle(float damagePerCycle) {
			this.damagePerCycle = Math.max(0f, damagePerCycle);
			return this;
		}

		/**
		 * Sets how far below <code>0</code> the air bar needs to go before damaging the entity.
		 * Defaults to <code>20</code>, can't go below <code>1</code>.
		 */
		@Contract("_ -> this")
		public Builder damageAt(int damageAt) {
			this.damageAt = Math.max(1, damageAt);
			return this;
		}

		/**
		 * Sets a flag to ignore the Respiration enchantment when calculating air decay.
		 */
		@Contract(" -> this")
		public Builder ignoreRespiration() {
			this.ignoreRespiration = true;
			return this;
		}

		/**
		 * Sets the {@link DamageSource} for the type of damage induced every damage cycle.
		 * Defaults to {@link DamageSource#DROWN}.
		 */
		@Contract("_ -> this")
		public Builder damageSource(@NotNull DamageSource damageSource) {
			this.damageSource = damageSource;
			return this;
		}

		/**
		 * Sets the {@link ParticleEffect} for the type of particles generated every damage cycle.
		 * Defaults to {@link ParticleTypes#BUBBLE}, can be null to show no particles.
		 */
		@Contract("_ -> this")
		public Builder particleEffect(@Nullable ParticleEffect particleEffect) {
			this.particleEffect = particleEffect;
			return this;
		}

		/**
		 * Builds the {@link BreathingInfo}, returning the complete record.
		 */
		@NotNull
		public BreathingInfo build() {
			return new BreathingInfo(airPerCycle, airDelta, damagePerCycle, damageAt, ignoreRespiration, damageSource, particleEffect);
		}
	}
}
