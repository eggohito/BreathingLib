package net.fabricmc.example.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor
	BlockPos getLastBlockPos();

	@Accessor
	void setLastBlockPos(BlockPos pos);

	@Invoker
	void callApplyMovementEffects(BlockPos pos);
}
