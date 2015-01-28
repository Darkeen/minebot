package net.famzangl.minecraft.minebot.ai.path;

import net.famzangl.minecraft.minebot.Pos;
import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.BlockWhitelist;
import net.famzangl.minecraft.minebot.ai.ClassItemFilter;
import net.famzangl.minecraft.minebot.ai.ItemFilter;
import net.famzangl.minecraft.minebot.ai.task.UseItemOnBlockAtTask;
import net.famzangl.minecraft.minebot.ai.task.place.DestroyBlockTask;
import net.famzangl.minecraft.minebot.ai.task.place.PlaceBlockAtFloorTask;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

public class PlantPathFinder extends MovePathFinder {
	public enum PlantType {
		ANY(Blocks.farmland, Items.wheat_seeds, Items.carrot, Items.potato), WHEAT(
				Blocks.farmland, Items.wheat_seeds), CARROT(Blocks.farmland,
				Items.carrot), POTATO(Blocks.farmland, Items.potato), NETHERWART(
				Blocks.soul_sand, Items.nether_wart);

		public final Block farmland;

		private final Item[] items;

		private PlantType(Block farmland, Item... items) {
			this.farmland = farmland;
			this.items = items;
		}

		public boolean canPlantItem(Item item) {
			for (final Item i : items) {
				if (item == i) {
					return true;
				}
			}
			return false;
		}
	}

	private final class SeedFilter implements ItemFilter {
		private final PlantType type;

		public SeedFilter(PlantType type) {
			super();
			this.type = type;
		}

		@Override
		public boolean matches(ItemStack itemStack) {
			return itemStack != null && type.canPlantItem(itemStack.getItem());
		}
	}

	private final PlantType type;

	private static final BlockWhitelist farmlandable = new BlockWhitelist(
			Blocks.dirt, Blocks.grass);

	public PlantPathFinder(PlantType type) {
		this.type = type;
		allowedGroundForUpwardsBlocks = allowedGroundBlocks;
		footAllowedBlocks = AIHelper.walkableBlocks;
		headAllowedBlocks = AIHelper.headWalkableBlocks;
		footAllowedBlocks = footAllowedBlocks.intersectWith(forbiddenBlocks
				.invert());
		headAllowedBlocks = headAllowedBlocks.intersectWith(forbiddenBlocks
				.invert());
	}

	@Override
	protected float rateDestination(int distance, int x, int y, int z) {
		if (isGrown(helper, x, y, z)) {
			return distance + 1;
		} else if (helper.isAirBlock(x, y, z) && hasFarmlandBelow(x, y, z)
				&& helper.canSelectItem(new SeedFilter(type))) {
			return distance + 1;
		} else if (type.farmland == Blocks.farmland
				&& helper.isAirBlock(x, y, z)
				&& farmlandable.contains(helper.getBlock(x, y - 1, z))
				&& helper.canSelectItem(new SeedFilter(type))
				&& helper.canSelectItem(new ClassItemFilter(ItemHoe.class))) {
			return distance + 10;
		} else {
			return -1;
		}
	}

	private boolean isGrown(AIHelper helper, int x, int y, int z) {
		final Block block = helper.getBlock(x, y, z);
		if (block instanceof BlockCrops) {
			final int metadata = helper.getBlockIdWithMeta(x, y, z) & 0xf;
			return metadata >= 7;
		}
		return false;
	}

	private boolean hasFarmlandBelow(int x, int y, int z) {
		return Block.isEqualTo(helper.getBlock(x, y - 1, z), type.farmland);
	}

	@Override
	protected void addTasksForTarget(Pos currentPos) {
		if (helper.isAirBlock(currentPos.getX(), currentPos.getY(),
				currentPos.getZ())) {
			if (!hasFarmlandBelow(currentPos.getX(), currentPos.getY(),
					currentPos.getZ())) {
				addTask(new UseItemOnBlockAtTask(new ClassItemFilter(
						ItemHoe.class), currentPos.add(0, -1, 0)));
			}
			addTask(new PlaceBlockAtFloorTask(currentPos, new SeedFilter(type)));
		} else {
			addTask(new DestroyBlockTask(currentPos));
		}
	}
}
