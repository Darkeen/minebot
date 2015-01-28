package net.famzangl.minecraft.minebot.build;

import java.util.LinkedList;

import net.famzangl.minecraft.minebot.Pos;
import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.BlockItemFilter;
import net.famzangl.minecraft.minebot.ai.BlockWhitelist;
import net.famzangl.minecraft.minebot.ai.strategy.TaskOperations;
import net.famzangl.minecraft.minebot.ai.task.AITask;
import net.famzangl.minecraft.minebot.ai.task.error.SelectTaskError;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovementInput;

public class WalkTowardsTask extends AITask {

	private static final BlockWhitelist CARPETS = new BlockWhitelist(
			Blocks.carpet);

	private static final BlockItemFilter CARPET = new BlockItemFilter(CARPETS);
	private final Pos fromPos;
	private final Pos nextPos;

	private AITask subTask;

	private final LinkedList<Pos> carpets = new LinkedList<Pos>();
	private boolean wasStandingOnDest;

	public WalkTowardsTask(Pos fromPos, Pos nextPos) {
		this.fromPos = fromPos;
		this.nextPos = nextPos;
	}

	@Override
	public boolean isFinished(AIHelper h) {
		return subTask == null
				&& h.isStandingOn(nextPos.getX(), nextPos.getY(),
						nextPos.getZ()) && carpets.isEmpty();
		/* && getUpperCarpetY(h) < 0 */
	}

	@Override
	public void runTick(AIHelper h, TaskOperations o) {
		if (subTask != null && subTask.isFinished(h)) {
			subTask = null;
		}
		if (subTask != null) {
			subTask.runTick(h, o);
		} else {
			final int carpetY = getUpperCarpetY(h);
			final double carpetBuildHeight = h.realBlockTopY(fromPos.getX(),
					Math.max(carpetY + 1, fromPos.getY()), fromPos.getZ());
			final double destHeight = h.realBlockTopY(nextPos.getX(),
					nextPos.getY(), nextPos.getZ());
			if (carpetBuildHeight < destHeight - 1) {
				System.out.println("Moving upwards. Carpets are at " + carpetY);
				final int floorY = Math.max(carpetY, fromPos.getY() - 1);
				BlockPos floor = new BlockPos(fromPos.getX(), floorY,
						fromPos.getZ());
				h.faceBlock(floor);
				if (h.isFacingBlock(floor, EnumFacing.UP)) {
					if (h.selectCurrentItem(CARPET)) {
						h.overrideUseItem();
						carpets.add(new Pos(fromPos.getX(), floorY + 1, fromPos
								.getZ()));
					} else {
						o.desync(new SelectTaskError(CARPET));
					}
				}
				final MovementInput i = new MovementInput();
				i.jump = true;
				h.overrideMovement(i);
			} else if ((h.isStandingOn(nextPos.getX(), nextPos.getY(),
					nextPos.getZ()) || wasStandingOnDest)
					&& !carpets.isEmpty()) {
				// Destruct everything after arriving at dest. Then walk to dest
				// again.

				while (!carpets.isEmpty()) {
					// Clean up carpets we already "lost"
					final Pos last = carpets.getLast();
					if (h.isAirBlock(last.getX(), last.getY(), last.getZ())) {
						carpets.removeLast();
					}
				}

				final int x = fromPos.getX() - nextPos.getX();
				final int z = fromPos.getX() - nextPos.getX();
				if (h.sneakFrom(nextPos, AIHelper.getDirectionForXZ(x, z))) {
					final Pos last = carpets.getLast();
					h.faceAndDestroy(last);
				}

				wasStandingOnDest = true;
			} else {
				h.walkTowards(nextPos.getX() + 0.5, nextPos.getZ() + 0.5,
						carpetBuildHeight < destHeight - 0.5);
			}
		}
	}

	/**
	 * Gets the Y of the topmost carpet that was placed. -1 if there was none.
	 * 
	 * @param h
	 * @return
	 */
	private int getUpperCarpetY(AIHelper h) {
		int upperCarpet = -1;
		for (int y = AIHelper.air.unionWith(CARPETS).contains(
				h.getBlock(fromPos)) ? fromPos.getY() : fromPos.getY() + 1; y < nextPos
				.getY(); y++) {
			if (CARPETS.contains(h.getBlock(fromPos.getX(), y, fromPos.getZ()))) {
				upperCarpet = y;
			} else {
				break;
			}
		}
		return upperCarpet;
	}

	@Override
	public String toString() {
		return "WalkTowardsTask [currentPos=" + fromPos + ", nextPos="
				+ nextPos + ", subTask=" + subTask + "]";
	}
}
