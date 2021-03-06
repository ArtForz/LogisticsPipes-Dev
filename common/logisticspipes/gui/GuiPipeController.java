package logisticspipes.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.SneakyUpgrade;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiPipeController extends LogisticsBaseGuiScreen {
	
	private final String			PREFIX				= "gui.pipecontroller.";
	
	private final int				TAB_COUNT			= 5;
	private int						current_Tab;
	
	private final List<Slot>		TAB_SLOTS_1_1		= new ArrayList<Slot>();
	private final List<Slot>		TAB_SLOTS_1_2		= new ArrayList<Slot>();
	private final List<Slot>		TAB_SLOTS_2			= new ArrayList<Slot>();
	private final CoreRoutedPipe	pipe;
	private boolean					redstoneControll	= false;
	
	public GuiPipeController(final EntityPlayer player, final CoreRoutedPipe pipe) {
		super(180, 220, 0, 0);
		this.pipe = pipe;
		DummyContainer dummy = new DummyContainer(player, null, pipe.getUpgradeManager().getGuiController());
		dummy.addNormalSlotsForPlayerInventory(10, 135);
		
		// TAB_1 SLOTS
		for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			TAB_SLOTS_1_1.add(dummy.addRestrictedSlot(pipeSlot, pipe.getUpgradeManager().getInv(), 10 + pipeSlot * 18, 42, new ISlotCheck() {
				@Override
				public boolean isStackAllowed(ItemStack itemStack) {
					if(itemStack == null) return false;
					if(itemStack.itemID == LogisticsPipes.UpgradeItem.itemID) {
						if(!LogisticsPipes.UpgradeItem.getUpgradeForItem(itemStack, null).isAllowed(pipe)) return false;
					} else {
						return false;
					}
					return true;
				}
			}));
		}
		
		for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			TAB_SLOTS_1_2.add(dummy.addRestrictedSlot(pipeSlot, pipe.getUpgradeManager().getSneakyInv(), 10 + pipeSlot * 18, 78, new ISlotCheck() {
				@Override
				public boolean isStackAllowed(ItemStack itemStack) {
					if(itemStack == null) return false;
					if(itemStack.itemID == LogisticsPipes.UpgradeItem.itemID) {
						IPipeUpgrade upgrade = LogisticsPipes.UpgradeItem.getUpgradeForItem(itemStack, null);
						if(!(upgrade instanceof SneakyUpgrade)) return false;
						if(!upgrade.isAllowed(pipe)) return false;
					} else {
						return false;
					}
					return true;
				}
			}));
		}
		
		// TAB_2 SLOTS
		TAB_SLOTS_2.add(dummy.addStaticRestrictedSlot(0, pipe.getUpgradeManager().getSecInv(), 10, 42, new ISlotCheck() {
			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				if(itemStack == null) return false;
				if(itemStack.itemID != LogisticsPipes.LogisticsItemCard.itemID) return false;
				if(itemStack.getItemDamage() != LogisticsItemCard.SEC_CARD) return false;
				if(!SimpleServiceLocator.securityStationManager.isAuthorized(UUID.fromString(itemStack.getTagCompound().getString("UUID")))) return false;
				return true;
			}
		}, 1));
		
		this.inventorySlots = dummy;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouse_x, int mouse_y) {
		if(current_Tab == 3 && !redstoneControll) {
			current_Tab = 0;
		}
		GL11.glColor4d(1.0D, 1.0D, 1.0D, 1.0D);
		for(int i = 0; i < TAB_COUNT; i++) {
			if(i == 3 && !redstoneControll) {
				GL11.glColor4d(0.4D, 0.4D, 0.4D, 1.0D);
			}
			BasicGuiHelper.drawGuiBackGround(mc, guiLeft + (25 * i) + 2, guiTop - 2, guiLeft + 27 + (25 * i), guiTop + 35, zLevel, false, true, true, false, true);
			if(i == 3 && !redstoneControll) {
				GL11.glColor4d(1.0D, 1.0D, 1.0D, 1.0D);
			}
		}
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop + 20, right, bottom, zLevel, true);
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft + (25 * current_Tab) + 2, guiTop - 2, guiLeft + 27 + (25 * current_Tab), guiTop + 38, zLevel, true, true, true, false, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 135);
		
		// First Tab
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableGUIStandardItemLighting();
		ItemStack stack = new ItemStack(LogisticsPipes.UpgradeItem, ItemUpgrade.SNEAKY_COMBINATION);
		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, getMC().renderEngine, stack, guiLeft + 6, guiTop + 4);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		itemRenderer.zLevel = 0.0F;
		
		// Second Tab
		BasicGuiHelper.drawLockBackground(mc, guiLeft + 32, guiTop + 3);
		
		// Third Tab
		BasicGuiHelper.drawStatsBackground(mc, guiLeft + 56, guiTop + 3);
		
		// Forth Tab
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableGUIStandardItemLighting();
		ItemStack stack2 = new ItemStack(Block.torchRedstoneActive);
		if(!redstoneControll) {
			stack2 = new ItemStack(Block.torchRedstoneIdle);
		}
		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, getMC().renderEngine, stack2, guiLeft + 81, guiTop + 1);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		itemRenderer.zLevel = 0.0F;
		
		// Fifth Tab
		BasicGuiHelper.drawLinesBackground(mc, guiLeft + 106, guiTop + 3);
		
		if(current_Tab == 0) {
			// TAB_1 SLOTS
			for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + 9 + pipeSlot * 18, guiTop + 41);
			}
			if(pipe.getUpgradeManager().hasCombinedSneakyUpgrade()) {
				for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
					BasicGuiHelper.drawSlotBackground(mc, guiLeft + 9 + pipeSlot * 18, guiTop + 77);
				}
			}
		} else if(current_Tab == 1) {
			BasicGuiHelper.drawSlotBackground(mc, guiLeft + 9, guiTop + 41);
		}
		
		super.drawGuiContainerBackgroundLayer(f, mouse_x, mouse_y);
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		if(par3 == 0 && par1 > guiLeft && par1 < guiLeft + 220 && par2 > guiTop && par2 < guiTop + 20) {
			par1 -= guiLeft + 3;
			int select = Math.max(0, Math.min(par1 / 25, TAB_COUNT - 1));
			if(select != 3 || redstoneControll) {
				this.current_Tab = select;
			}
		} else {
			super.mouseClicked(par1, par2, par3);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "inventory"), 10, 122, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
		if(current_Tab == 0) {
			mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "upgrade"), 10, 28, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
			if(pipe.getUpgradeManager().hasCombinedSneakyUpgrade()) {
				mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "sneakyUpgrades"), 10, 64, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
			}
		} else if(current_Tab == 1) {
			mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "security"), 10, 28, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
			ItemStack itemStack = pipe.getUpgradeManager().getSecInv().getStackInSlot(0);
			if(itemStack != null) {
				UUID id = UUID.fromString(itemStack.getTagCompound().getString("UUID"));
				mc.fontRenderer.drawString("Id: ", 10, 68, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
				GL11.glTranslated(10, 80, 0);
				GL11.glScaled(0.75D, 0.75D, 1.0D);
				mc.fontRenderer.drawString(ChatColor.BLUE.toString() + id.toString(), 0, 0, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
				GL11.glScaled(1 / 0.75D, 1 / 0.75D, 1.0D);
				GL11.glTranslated(-10, -80, 0);
				mc.fontRenderer.drawString("Authorization: " + (SimpleServiceLocator.securityStationManager.isAuthorized(id) ? ChatColor.GREEN + "Authorized" : ChatColor.RED + "Deauthorized"), 10, 94, BasicGuiHelper.ConvertEnumToColor(Colors.DarkGrey), false);
			}
		} else if(current_Tab == 2) {
			String pipeName = ItemIdentifier.get(pipe.itemID, 0, null).getFriendlyName();
			fontRenderer.drawString(pipeName, (170 - fontRenderer.getStringWidth(pipeName)) / 2, 28, 0x83601c);
			
			int sessionxCenter = 85;
			int lifetimexCenter = 140;
			String s = null;
			
			fontRenderer.drawString(StringUtil.translate(PREFIX + "Session"), sessionxCenter - fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Session")) / 2, 40, 0x303030);
			fontRenderer.drawString(StringUtil.translate(PREFIX + "Lifetime"), lifetimexCenter - fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Lifetime")) / 2, 40, 0x303030);
			fontRenderer.drawString(StringUtil.translate(PREFIX + "Sent") + ":", 55 - fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Sent") + ":"), 55, 0x303030);
			fontRenderer.drawString(StringUtil.translate(PREFIX + "Recieved") + ":", 55 - fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Recieved") + ":"), 70, 0x303030);
			fontRenderer.drawString(StringUtil.translate(PREFIX + "Relayed") + ":", 55 - fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Relayed") + ":"), 85, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.stat_session_sent);
			fontRenderer.drawString(s, sessionxCenter - fontRenderer.getStringWidth(s) / 2, 55, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.stat_session_recieved);
			fontRenderer.drawString(s, sessionxCenter - fontRenderer.getStringWidth(s) / 2, 70, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.stat_session_relayed);
			fontRenderer.drawString(s, sessionxCenter - fontRenderer.getStringWidth(s) / 2, 85, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.stat_lifetime_sent);
			fontRenderer.drawString(s, lifetimexCenter - fontRenderer.getStringWidth(s) / 2, 55, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.stat_lifetime_recieved);
			fontRenderer.drawString(s, lifetimexCenter - fontRenderer.getStringWidth(s) / 2, 70, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.stat_lifetime_relayed);
			fontRenderer.drawString(s, lifetimexCenter - fontRenderer.getStringWidth(s) / 2, 85, 0x303030);
			
			fontRenderer.drawString(StringUtil.translate(PREFIX + "RoutingTableSize") + ":", 110 - fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "RoutingTableSize") + ":"), 110, 0x303030);
			
			s = BasicGuiHelper.getStringWithSpacesFromLong(pipe.server_routing_table_size);
			fontRenderer.drawString(s, 130 - fontRenderer.getStringWidth(s) / 2, 110, 0x303030);
		}
	}
	
	@Override
	protected void drawSlotInventory(Slot slot) {
		if(TAB_SLOTS_1_1.contains(slot) && current_Tab != 0) return;
		if(TAB_SLOTS_1_2.contains(slot) && (current_Tab != 0 || !pipe.getUpgradeManager().hasCombinedSneakyUpgrade())) return;
		if(TAB_SLOTS_2.contains(slot) && current_Tab != 1) return;
		super.drawSlotInventory(slot);
	}
	
	@Override
	protected boolean isMouseOverSlot(Slot slot, int par2, int par3) {
		if(!super.isMouseOverSlot(slot, par2, par3)) return false;
		if(TAB_SLOTS_1_1.contains(slot) && current_Tab != 0) return false;
		if(TAB_SLOTS_1_2.contains(slot) && (current_Tab != 0 || !pipe.getUpgradeManager().hasCombinedSneakyUpgrade())) return false;
		if(TAB_SLOTS_2.contains(slot) && current_Tab != 1) return false;
		return true;
	}
	
	/*
	 * @SuppressWarnings("unchecked")
	 * protected void checkButtons() {
	 * super.checkButtons();
	 * for(GuiButton button:(List<GuiButton>) this.buttonList) {
	 * //TODO
	 * }
	 * }
	 */
}
