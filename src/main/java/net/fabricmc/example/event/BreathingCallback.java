package net.fabricmc.example.event;

import net.fabricmc.example.data.BreathingInfo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.util.Optional;

public interface BreathingCallback {
	Identifier VANILLA_PHASE = new Identifier("breathinglib:vanilla");
	Event<BreathingCallback> EVENT = EventFactory.createWithPhases(BreathingCallback.class,
			(listeners) -> (entity) -> {
				TypedActionResult<Optional<BreathingInfo>> result;
				for(int i = listeners.length - 1; i >= 0; --i) {
					result = listeners[i].apply(entity);
					if(result.getResult() != ActionResult.PASS) return result;
				}
				return TypedActionResult.consume(Optional.empty());
			}, Event.DEFAULT_PHASE, VANILLA_PHASE);

	TypedActionResult<Optional<BreathingInfo>> apply(LivingEntity entity);
}
