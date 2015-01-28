package net.famzangl.minecraft.minebot.ai.path;

import java.util.HashSet;
import java.util.LinkedList;

import net.famzangl.minecraft.minebot.Pos;
import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.BlockWhitelist;
import net.famzangl.minecraft.minebot.ai.task.DestroyInRangeTask;
import net.minecraft.init.Blocks;

public class ClearAreaPathfinder extends MovePathFinder {

	private final Pos minPos;
	private final Pos maxPos;
	private int topY;
	private final HashSet<Pos> foundPositions = new HashSet<Pos>();
	private Pos pathEndPosition;

	public ClearAreaPathfinder(Pos pos1, Pos pos2) {
		minPos = Pos.minPos(pos1, pos2);
		maxPos = Pos.maxPos(pos1, pos2);
		topY = maxPos.getY();
	}

	@Override
	protected boolean runSearch(Pos playerPosition) {
		foundPositions.clear();
		pathEndPosition = playerPosition;
		do {
			final boolean finished = super.runSearch(pathEndPosition);
			if (!finished) {
				return false;
			}
		} while (foundPositions.size() < 20 && pathEndPosition != null);
		return true;
	}

	@Override
	protected void foundPath(LinkedList<Pos> path) {
		for (final Pos p : path) {
			foundPositions.add(p);
			foundPositions.add(p.add(0, 1, 0));
			pathEndPosition = p;
		}

		super.foundPath(path);
	}

	@Override
	protected void noPathFound() {
		pathEndPosition = null;
		super.noPathFound();
	}

	@Override
	protected float rateDestination(int distance, int x, int y, int z) {
		if (isInArea(x, y, z)
				&& (!isTemporaryCleared(x, y, z) || !isTemporaryCleared(x,
						y + 1, z) && y < maxPos.getY())) {
			final float bonus = 0.0001f * (x - minPos.getX()) + 0.001f
					* (y - minPos.getY());
			int layerMalus;
			if (topY <= y) {
				layerMalus = 5;
			} else if (!isInArea(x, y + 1, z)
					|| isTemporaryCleared(x, y + 1, z)) {
				layerMalus = 2;
			} else if (isInArea(x, y + 1, z)
					&& !isTemporaryCleared(x, y + 2, z)) {
				layerMalus = 2;
			} else {
				layerMalus = 0;
			}
			return distance + bonus + layerMalus + (maxPos.getY() - y) * 2;
		} else {
			return -1;
		}
	}

	private boolean isTemporaryCleared(int x, int y, int z) {
		return isClearedBlock(helper, x, y, z)
				|| foundPositions.contains(new Pos(x, y, z));
	}

	private boolean isInArea(int x, int y, int z) {
		return minPos.getX() <= x && x <= maxPos.getX() && minPos.getY() <= y && y <= maxPos.getY()
				&& minPos.getZ() <= z && z <= maxPos.getZ();
	}
	
	private static final BlockWhitelist clearedBlocks = new BlockWhitelist(Blocks.air,
			Blocks.torch);

	private static boolean isClearedBlock(AIHelper helper, int x, int y, int z) {
		return clearedBlocks.contains(helper.getBlockId(x, y, z));
	}

	@Override
	protected void addTasksForTarget(Pos currentPos) {
		super.addTasksForTarget(currentPos);
		Pos top = currentPos;
		for (int i = 1; i < 6; i++) {
			final Pos pos = currentPos.add(0, i, 0);
			if (pos.getY() <= maxPos.getY()) {
				top = pos;
			}
		}
		addTask(new DestroyInRangeTask(currentPos, top));
	}

	@Override
	protected int materialDistance(int x, int y, int z, boolean asFloor) {
		return isInArea(x, y, z) ? 0 : super.materialDistance(x, y, z, asFloor);
	}

	public int getAreaSize() {
		return (maxPos.getX() - minPos.getX() + 1) * (maxPos.getY() - minPos.getY() + 1)
				* (maxPos.getZ() - minPos.getZ() + 1);
	}

	public int getToClearCount(AIHelper helper) {
		int count = 0;
		int newTopY = minPos.getY();
		for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
			for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
				for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
					if (!isClearedBlock(helper, x, y, z)) {
						count++;
						newTopY = Math.max(y, newTopY);
					}
				}
			}
		}
		topY = newTopY;
		System.out.println("top Y:  " + newTopY);
		return count;
	}
}
