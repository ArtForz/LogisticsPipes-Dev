package logisticspipes.renderer;

import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.textures.Textures;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.CoreConstants;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.render.PipeRendererWorld;

public class LogisticsPipeWorldRenderer extends PipeRendererWorld {

	public void renderPipe(RenderBlocks renderblocks, IBlockAccess iblockaccess, LogisticsBlockGenericPipe block, LogisticsTileGenericPipe pipe, int x, int y, int z) {
		if(pipe.pipe instanceof PipeBlockRequestTable) {
			PipeRenderState state = pipe.getRenderState();
			IIconProvider icons = pipe.getPipeIcons();
			if (icons == null) return;
			state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
			block.setBlockBounds(0, 0, 0, 1, 1, 1);
			renderblocks.setRenderBoundsFromBlock(block);
			renderblocks.renderStandardBlock(block, x, y, z);
			return;
		}
		PipeRenderState state = pipe.getRenderState();
		IIconProvider icons = pipe.getPipeIcons();
		
		if (icons == null)
			return;

		int connectivity = state.pipeConnectionMatrix.getMask();
		float[] dim = new float[6];

		
		if(!pipe.isOpaque()) {
			// render the unconnected pipe faces of the center block (if any)
			if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
				resetToCenterDimensions(dim);
				state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
				renderTwoWayBlock(renderblocks, block, x, y, z, dim, connectivity ^ 0x3f);
			}
			
			// render the connecting pipe faces
			for (int dir = 0; dir < 6; dir++) {
				int mask = 1 << dir;
				if ((connectivity & mask) == 0) continue; // no connection towards dir
				
				// center piece offsets
				resetToCenterDimensions(dim);
				
				// extend block towards dir as it's connected to there
				dim[dir / 2] = dir % 2 == 0 ? 0 : CoreConstants.PIPE_MAX_POS;
				dim[dir / 2 + 3] = dir % 2 == 0 ? CoreConstants.PIPE_MIN_POS : 1;
	
				// the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5 -> 001111
				int renderMask = (3 << (dir / 2 * 2)) ^ 0x3f;
	
				//workaround for 1.6 texture weirdness, rotate texture for N/S/E/W connections
				renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = (dir < 2) ? 0 : 1;

				// render sub block
				state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.VALID_DIRECTIONS[dir]));
	
				renderTwoWayBlock(renderblocks, block, x, y, z, dim, renderMask);
				renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = 0;
			}
		} else {
			// render the unconnected pipe faces of the center block (if any)
			if (connectivity != 0x3f) { // note: 0x3f = 0x111111 = all sides
				resetToCenterDimensions(dim);
				
				//Render opaque Layer
				state.currentTexture = icons.getIcon(Textures.LOGISTICSPIPE_OPAQUE_TEXTURE.normal);
				renderOneWayBlock(renderblocks, block, x, y, z, dim, connectivity ^ 0x3f);
				
				//Render Pipe Texture
				state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.UNKNOWN));
				renderOneWayBlock(renderblocks, block, x, y, z, dim, connectivity ^ 0x3f);
			}
			
			// render the connecting pipe faces
			for (int dir = 0; dir < 6; dir++) {
				int mask = 1 << dir;
				if ((connectivity & mask) == 0) continue; // no connection towards dir
				
				// center piece offsets
				resetToCenterDimensions(dim);
				
				// extend block towards dir as it's connected to there
				dim[dir / 2] = dir % 2 == 0 ? 0 : CoreConstants.PIPE_MAX_POS;
				dim[dir / 2 + 3] = dir % 2 == 0 ? CoreConstants.PIPE_MIN_POS : 1;
	
				// the mask points to all faces perpendicular to dir, i.e. dirs 0+1 -> mask 111100, 1+2 -> 110011, 3+5 -> 001111
				int renderMask = (3 << (dir / 2 * 2)) ^ 0x3f;
				
				//workaround for 1.6 texture weirdness, rotate texture for N/S/E/W connections
				renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = (dir < 2) ? 0 : 1;

				//Render opaque Layer
				state.currentTexture = icons.getIcon(Textures.LOGISTICSPIPE_OPAQUE_TEXTURE.normal);
				renderOneWayBlock(renderblocks, block, x, y, z, dim, 0x3f);
				
				// render sub block
				state.currentTexture = icons.getIcon(state.textureMatrix.getTextureIndex(ForgeDirection.VALID_DIRECTIONS[dir]));
				renderOneWayBlock(renderblocks, block, x, y, z, dim, renderMask);
				renderblocks.uvRotateEast = renderblocks.uvRotateNorth = renderblocks.uvRotateWest = renderblocks.uvRotateSouth = 0;
			}
		}

		renderblocks.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		pipeFacadeRenderer(renderblocks, block, state, x, y, z);
		pipePlugRenderer(renderblocks, block, state, x, y, z);
	}

	private void resetToCenterDimensions(float[] dim) {
		for (int i = 0; i < 3; i++) dim[i] = CoreConstants.PIPE_MIN_POS;
		for (int i = 3; i < 6; i++) dim[i] = CoreConstants.PIPE_MAX_POS;
	}

	/**
	 * Render a block with normal and inverted vertex order so back face culling doesn't have any effect.
	 */
	private void renderOneWayBlock(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, int x, int y, int z, float[] dim, int mask) {
		assert mask != 0;

		block.setRenderMask(mask);
		renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
		renderblocks.renderStandardBlock(block, x, y, z);
	}

	/**
	 * Render a block with normal and inverted vertex order so back face culling doesn't have any effect.
	 */
	private void renderTwoWayBlock(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, int x, int y, int z, float[] dim, int mask) {
		assert mask != 0;

		block.setRenderMask(mask);
		renderblocks.setRenderBounds(dim[2], dim[0], dim[1], dim[5], dim[3], dim[4]);
		renderblocks.renderStandardBlock(block, x, y, z);
		//flip back side texture
		renderblocks.flipTexture = true;
		block.setRenderMask((mask & 0x15) << 1 | (mask & 0x2a) >> 1); // pairwise swapped mask
		renderblocks.setRenderBounds(dim[5], dim[3], dim[4], dim[2], dim[0], dim[1]);
		renderblocks.renderStandardBlock(block, x, y, z);
		renderblocks.flipTexture = false;
	}

	private void pipeFacadeRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z) {
		FacadeRenderHelper.pipeFacadeRenderer(renderblocks, block, state, x, y, z);
	}

	private void pipePlugRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z) {

		float zFightOffset = 1F / 4096F;

		float[][] zeroState = new float[3][2];
		// X START - END
		zeroState[0][0] = 0.25F + zFightOffset;
		zeroState[0][1] = 0.75F - zFightOffset;
		// Y START - END
		zeroState[1][0] = 0.125F;
		zeroState[1][1] = 0.251F;
		// Z START - END
		zeroState[2][0] = 0.25F + zFightOffset;
		zeroState[2][1] = 0.75F - zFightOffset;

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.plugMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}

		// X START - END
		zeroState[0][0] = 0.25F + 0.125F / 2 + zFightOffset;
		zeroState[0][1] = 0.75F - 0.125F / 2 + zFightOffset;
		// Y START - END
		zeroState[1][0] = 0.25F;
		zeroState[1][1] = 0.25F + 0.125F;
		// Z START - END
		zeroState[2][0] = 0.25F + 0.125F / 2;
		zeroState[2][1] = 0.75F - 0.125F / 2;

		state.currentTexture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure Pipe

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if (state.plugMatrix.isConnected(direction)) {
				float[][] rotated = MatrixTranformations.deepClone(zeroState);
				MatrixTranformations.transform(rotated, direction);

				renderblocks.setRenderBounds(rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1], rotated[2][1]);
				renderblocks.renderStandardBlock(block, x, y, z);
			}
		}

	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe pipeTile = (LogisticsTileGenericPipe) tile;
			renderPipe(renderer, world, (LogisticsBlockGenericPipe) block, pipeTile, x, y, z);
		} else {
			super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
		}
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

	@Override
	public int getRenderId() {
		return TransportProxy.pipeModel;
	}
	
	
}
