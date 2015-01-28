package net.famzangl.minecraft.minebot.ai.task.place;

import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.ItemFilter;
import net.famzangl.minecraft.minebot.ai.task.BlockSide;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class JumpingPlaceBlockAtSideTask extends JumpingPlaceAtHalfTask {

	private final int attempts = 0;

	public JumpingPlaceBlockAtSideTask(BlockPos pos, ItemFilter filter,
			EnumFacing lookingDirection, BlockSide side) {
		super(pos, filter, side);
		this.lookingDirection = lookingDirection;
	}

	// @Override
	// protected void faceBlock(AIHelper h) {
	// if (side != BlockSide.UPPER_HALF && attempts % 4 == 3) {
	// faceBottomBlock(h);
	// } else if (attempts % 4 == 1) {
	// faceSideBlock(h, lookingDirection.getRotation(EnumFacing.UP));
	// } else if (attempts % 4 == 2) {
	// faceSideBlock(h, lookingDirection.getRotation(EnumFacing.DOWN));
	// } else {
	// faceSideBlock(h, lookingDirection);
	// }
	// attempts++;
	// }

	@Override
	protected EnumFacing[] getBuildDirs() {
		return side != BlockSide.UPPER_HALF ? new EnumFacing[] {
				EnumFacing.DOWN,
				lookingDirection.rotateY(),
				lookingDirection.rotateYCCW(),
				lookingDirection } : new EnumFacing[] {
				lookingDirection.rotateYCCW(),
				lookingDirection.rotateY(),
				lookingDirection };
	}

	@Override
	protected boolean isFacingRightBlock(AIHelper h) {
		if (h.getLookDirection() != lookingDirection) {
			return false;
		} else {
			return isFacing(h, lookingDirection.rotateY())
					|| isFacing(h,
							lookingDirection.rotateYCCW())
					|| isFacing(h, lookingDirection)
					|| side != BlockSide.UPPER_HALF
					&& isFacing(h,
							EnumFacing.DOWN);
		}
	}
}
