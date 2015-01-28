package net.famzangl.minecraft.minebot.ai.strategy;

import net.famzangl.minecraft.minebot.ai.AIHelper;

/**
 * Aborts the current strategy stack if the player dies.
 * @see StrategyStack
 * @author michael
 *
 */
public class AbortOnDeathStrategy extends AIStrategy {

	@Override
	public boolean checkShouldTakeOver(AIHelper helper) {
		return !helper.isAlive();
	}
	
	@Override
	protected TickResult onGameTick(AIHelper helper) {
		if (helper.isAlive()) {
			return TickResult.NO_MORE_WORK;
		} else {
			return TickResult.ABORT;
		}
	}
	
	@Override
	public String getDescription(AIHelper helper) {
		return "Stop when dead.";
	}

}