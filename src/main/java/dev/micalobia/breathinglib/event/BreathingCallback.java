package dev.micalobia.breathinglib.event;

import dev.micalobia.breathinglib.data.BreathingInfo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.util.Optional;

public interface BreathingCallback {
	Identifier VANILLA_PHASE = new Identifier("breathinglib:vanilla");
	/**
	 * <p>An event to change or modify the behaviour of the breath meter and its effect on the player.
	 * <ul><li>{@link ActionResult#SUCCESS} fills the air bar
	 * <li>{@link ActionResult#FAIL} depletes the air bar
	 * <li>{@link ActionResult#CONSUME} freezes the air bar
	 * <li>{@link ActionResult#PASS} passes control to the next listener, or vanilla behaviour
	 * </ul>
	 * Can return {@link TypedActionResult} with {@link Optional#empty()} to use vanilla behavior, or you can occupy the optional with a custom {@link BreathingInfo}
	 *
	 * @see BreathingInfo
	 */
	Event<BreathingCallback> EVENT = EventFactory.createWithPhases(BreathingCallback.class,
			(listeners) -> (entity) -> {
				TypedActionResult<Optional<BreathingInfo>> result;
				for(int i = listeners.length - 1; i >= 0; --i) {
					result = listeners[i].apply(entity);
					if(result.getResult() != ActionResult.PASS) return result;
				}
				return TypedActionResult.consume(Optional.empty());
			}, VANILLA_PHASE, Event.DEFAULT_PHASE);

	TypedActionResult<Optional<BreathingInfo>> apply(LivingEntity entity);
}
