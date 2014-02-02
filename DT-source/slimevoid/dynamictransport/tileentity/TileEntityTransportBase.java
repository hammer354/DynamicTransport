package slimevoid.dynamictransport.tileentity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimevoid.dynamictransport.core.lib.ConfigurationLib;
import slimevoidlib.blocks.BlockBase;
import slimevoidlib.tileentity.TileEntityBase;
import slimevoidlib.util.helpers.ItemHelper;

public abstract class TileEntityTransportBase extends TileEntityBase {
	protected ItemStack	camoItem;
	protected String	owner;
	protected Privacy	privacyLvl	= Privacy.Public;

	public enum Privacy {
		Public, Restricted, Private
	}

	@Override
	public int getBlockID() {
		return ConfigurationLib.blockTransportBaseID;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (this.camoItem != null) {
			NBTTagCompound itemNBTTagCompound = new NBTTagCompound();
			this.camoItem.writeToNBT(itemNBTTagCompound);

			nbttagcompound.setTag(	"CamoItem",
									itemNBTTagCompound);
		}

		if (owner != null && !owner.isEmpty()) nbttagcompound.setString("Owner",
																		owner);
		nbttagcompound.setInteger(	"PrivacyLvl",
									privacyLvl.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("CamoItem")) {
			this.camoItem = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("CamoItem"));
		} else {
			this.camoItem = null;
		}

		owner = nbttagcompound.getString("Owner");

		this.privacyLvl = Privacy.values()[nbttagcompound.getInteger("PrivacyLvl")];
	}

	@Override
	public float getBlockHardness(BlockBase blockBase) {
		return 1.0f; // TODO :: Real Block Hardness
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityplayer) {
		if (!this.worldObj.isRemote) {
			if (this.isInMaintenanceMode()) {
				ItemStack heldItem = entityplayer.getHeldItem();
				if (this.getCamoItem() == null
					&& ItemHelper.isSolidBlockStack(heldItem,
													this.getWorldObj(),
													this.xCoord,
													this.yCoord,
													this.zCoord)) {
					this.setCamoItem(heldItem.copy());
					if (!entityplayer.capabilities.isCreativeMode) {
						--heldItem.stackSize;
						if (heldItem.stackSize < 0) {
							heldItem = null;
						}
					}
					return true;
				}

				if (!this.worldObj.isRemote) {
					if (this.getCamoItem() != null
						&& entityplayer.getHeldItem() == null) {
						this.removeCamoItem();
						return true;
					}
				}
			}
		}
		return false;
	}

	public ItemStack getCamoItem() {
		return this.camoItem;
	}

	public void setCamoItem(ItemStack itemstack) {
		this.camoItem = itemstack;
		this.camoItem.stackSize = 1;
		this.updateBlock();
	}

	protected void removeCamoItem() {
		ItemHelper.dropItem(this.getWorldObj(),
							this.xCoord,
							this.yCoord,
							this.zCoord,
							this.camoItem);
		this.camoItem = null;
		this.updateBlock();
	}

	public ItemStack removeCamoItemWithoutDrop() {
		ItemStack copyCamoItem = null;
		if (this.camoItem != null) {
			copyCamoItem = this.camoItem.copy();
			this.camoItem = null;
			this.onInventoryChanged();
		}
		return copyCamoItem;
	}

	protected abstract boolean isInMaintenanceMode();

	@Override
	protected void addHarvestContents(ArrayList<ItemStack> harvestList) {
		if (this.camoItem != null) {
			harvestList.add(this.camoItem);
		}
	}

	@Override
	public int getLightValue() {
		return this.camoItem == null ? 0 : Block.lightValue[((ItemBlock) this.camoItem.getItem()).getBlockID()];
	}

}
