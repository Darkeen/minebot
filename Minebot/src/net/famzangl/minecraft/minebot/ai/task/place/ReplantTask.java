package net.famzangl.minecraft.minebot.ai.task.place;

import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.BlockItemFilter;
import net.famzangl.minecraft.minebot.ai.BlockWhitelist;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public class ReplantTask extends PlaceBlockAtFloorTask {

	private final static BlockWhitelist PLANTABLE = new BlockWhitelist(Blocks.dirt, Blocks.grass);
	
	public ReplantTask(BlockPos pos) {
		super(pos, new BlockItemFilter(Blocks.sapling));
	}
	
	@Override
	public boolean isFinished(AIHelper h) {
		if (!PLANTABLE.contains(h.getBlock(pos.add(0,-1,0)))) {
			return true;
		}
		
		return super.isFinished(h);
	}

}
