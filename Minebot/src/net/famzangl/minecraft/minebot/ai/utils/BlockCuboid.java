package net.famzangl.minecraft.minebot.ai.utils;

import net.famzangl.minecraft.minebot.Pos;
import net.famzangl.minecraft.minebot.ai.path.world.WorldData;
import net.minecraft.util.BlockPos;

/**
 * This is cuboid of blocks.
 * <p>
 * It may not be empty.
 * 
 * @author Michael Zangl
 */
public class BlockCuboid extends BlockArea {
	/**
	 * The minimum x, y, z coordinate
	 */
	private BlockPos min;
	/**
	 * The maximum x, y, z coordinate that is in the cuboid.
	 */
	private BlockPos max;

	public BlockCuboid(BlockPos p1, BlockPos p2) {
		min = Pos.minPos(p1, p2);
		max = Pos.maxPos(p1, p2);
	}

	@Override
	public boolean contains(WorldData world, int x, int y, int z) {
		return min.getX() <= x && x <= max.getX() && min.getY() <= y
				&& y <= max.getY() && min.getZ() <= z && z <= max.getZ();
	}

	public BlockPos getMax() {
		return max;
	}

	public BlockPos getMin() {
		return min;
	}

	public int getVolume() {
		return (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1)
				* (max.getZ() - min.getZ() + 1);
	}

	@Override
	public void accept(AreaVisitor v, WorldData world) {
		int minY = min.getY();
		int maxY = max.getY();
		for (int y = minY; y <= maxY; y++) {
			acceptY(v, y, world);
		}
	}

	private void acceptY(AreaVisitor v, int y, WorldData world) {
		for (int z = min.getZ(); z <= max.getZ(); z++) {
			for (int x = min.getX(); x <= max.getX(); x++) {
				v.visit(world, x, y, z);
			}
		}
	}

	/**
	 * Extend in x and z directions.
	 * 
	 * @param extend how much
	 * @return The extended cuboid.
	 */
	public BlockCuboid extendXZ(int extend) {
		return new BlockCuboid(min.add(-extend, 0, -extend), max.add(extend, 0,
				extend));
	}

	@Override
	public String toString() {
		return "BlockCuboid [min=" + min + ", max=" + max + "]";
	}
}