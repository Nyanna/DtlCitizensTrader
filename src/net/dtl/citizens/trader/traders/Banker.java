package net.dtl.citizens.trader.traders;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.ItemsConfig;
import net.dtl.citizens.trader.managers.BankAccountsManager;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.managers.PermissionsManager;
import net.dtl.citizens.trader.objects.BankAccount;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.objects.BankTab;
import net.dtl.citizens.trader.objects.Wallet;
import net.dtl.citizens.trader.objects.Wallet.WalletType;
import net.dtl.citizens.trader.parts.BankerPart;

abstract public class Banker implements EconomyNpc {
	
	public enum BankStatus {
		TAB, REMOTE_TAB, SETTING_TAB;
	}
	
	//static global settigns
	protected static PermissionsManager permissions;
	protected static LocaleManager locale;
	protected static BankAccountsManager accounts;
	
	protected static ItemsConfig itemConfig;

	protected BankerPart settings;
	protected BankStatus bankStatus;
	
	protected BankAccount account;
	protected Inventory tabInventory;
	
	protected String tab;
	protected BankItem selectedItem;
	
	protected NPC npc;
	
	public Banker(NPC bankerNpc, BankerPart bankConfiguration, String player) {

		permissions = CitizensTrader.getPermissionsManager();
		
		itemConfig = CitizensTrader.getInstance().getItemConfig();
		
		locale = CitizensTrader.getLocaleManager();
		//loading accoutns
		
		bankStatus = BankStatus.TAB;
		npc = bankerNpc;
	}
	
	@Override
	public NPC getNpc()
	{
		return npc;
	}
	
	@Override
	public int getNpcId()
	{
		return npc.getId();
	}

	@Override
	public Inventory getInventory() {
		return tabInventory;
	}
	
	@Override
	public final boolean locked()
	{
		return false;
	}
	
	@Override
	public Wallet getWallet()
	{
		return new Wallet(WalletType.NPC);
	}
	
	public void switchInventory(String tab)
	{
		tabInventory.clear();
		//TODO inventory view
		//account.inventoryView(tabInventory, tab);
	}
	
	/*public double getTabPrice(BankTabType type)
	{
		if ( type == null || !tabPrices.containsKey(type) )
			return 0.0;
		return tabPrices.get(type);
	}*/
	
	public boolean withdrawFee(Player player)
	{
		return getWallet().withdraw(player.getName(), getSettings().getWithdrawFee());
	}
	
	public boolean depositFee(Player player)
	{
		return getWallet().withdraw(player.getName(), getSettings().getDepositFee());
	}
	
	public void tabTransaction(String tab, String player)
	{
		/*
		if ( type == null )
			return false;
		
		double price = tabPrices.get(type);
		if ( price == 0.0 )
			return true;
		
		if ( econ.getBalance(player) >= price )
		{
			econ.withdrawPlayer(player, price);
			return true;
		}
		
		return false;
		*/
	}
	
	public BankerPart getSettings()
	{
		return settings;
	}
	
	public void useSettingsInv()
	{
	//	tabInventory = account.inventoryView(54, "Bank account settings");
	}
	
	public void settingsInventory()
	{
		tabInventory.clear();
		//TODO settinsg inventory
	//	account.settingsView(tabInventory, tab);
	}
	
	public BankStatus getStatus()
	{
		return bankStatus;
	}
	
	public void setStatus(BankStatus status)
	{
		bankStatus = status;
	}

/*	public void setBankTabType(String tab)
	{
		this.tab = tab;
	}
	
	public BankTabType getBankTabType()
	{
		return tab;
	}*/
	
	public BankTab getTab(String owner)
	{
		return account.getBankTab(owner);
	}
	
	//TODO is needed?
	public void setBankTabItem(ItemStack item)
	{
		account.getBankTab(tab).setTabItem(toBankItem(item));
	}
	//tab function
	
	public boolean hasAllTabs()
	{
		return account.maxed();
	}
	
	public boolean addBankTab()
	{
		//TODO add tab function
	/*	BankTabType newTab = account.addBankTab();
		if ( newTab != null )
		{
			tab = newTab;
			return true;
		}
		return false;*/
		return false;
	}
	
	//selecting items
	public final Banker selectItem(BankItem i) {
		selectedItem = i;
		return this;
	}
	
	public final Banker selectItem(int slot) {
		selectedItem = account.getItem(tab, slot);

		return this;
	} 
	
	public final boolean hasSelectedItem() {
		return selectedItem != null;
	}
	
	public final BankItem getSelectedItem() {
		return selectedItem;
	}
	
	public void updateAccountItem(BankItem oldItem, BankItem newItem)
	{
		account.updateItem(tab, oldItem, newItem);
	}
	
	public void addItemToAccount(BankItem item)
	{
		account.addItem(tab, item);
	}
	
	public void removeItemFromAccount(BankItem item)
	{
		account.removeItem(tab, item);
	}
	
	/*//inventory events
	public final boolean playerInventoryHasPlace(Player player) {
		int amountToAdd = selectedItem.getItemStack().getAmount();
		return this.inventoryHasPlaceAmount(player.getInventory(), amountToAdd);
	}
	
	//inventory events
	public final boolean bankerInventoryHasPlace() {
		int amountToAdd = selectedItem.getItemStack().getAmount();
		return this.bankerInventoryHasPlaceAmount(tabInventory, amountToAdd);
	}

	public final boolean inventoryHasPlaceAmount(Inventory nInventory,int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
					return true;
				
				
				if ( item.getAmount() < 64 ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
				}
				
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean bankerInventoryHasPlaceAmount(Inventory nInventory,int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;


		for ( Map.Entry<Integer, ? extends ItemStack> itemEntry : inventory.all(selectedItem.getItemStack().getType()).entrySet() ) {
			ItemStack item = itemEntry.getValue();
			
			if ( this.rowClicked(account.getBankTab(tab).getTabSize()+1, itemEntry.getKey()) )
				continue;
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() )
					return true;
				
				if ( item.getAmount() < 64 ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % 64; 
				}
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			return true;
		}
		return false;
	}
	
	public final boolean addSelectedToPlayerInventory(Player player) {

		int amountToAdd = selectedItem.getItemStack().getAmount();
		return addAmountToInventory(player.getInventory(), amountToAdd);
		
	}
	
	public final boolean addSelectedToBankerInventory() {

		int amountToAdd = selectedItem.getItemStack().getAmount();
		return addAmountToBankerInventory(tabInventory, amountToAdd);
		
	}*/
	
	/**
	 * SelfWritten Inventory.addItem() function for a work around with a bukkit inventory function bug
	 * 
	 */
/*	public final boolean addAmountToInventory(Inventory nInventory, int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		
		for ( ItemStack item : inventory.all(selectedItem.getItemStack().getType()).values() ) {
			
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
			 
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
				}
				
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		
		return false;
	}
	
	public boolean addAmountToBankerInventory(Inventory nInventory, int amount) {
		Inventory inventory = nInventory;
		int amountToAdd = amount;
		
		for ( Map.Entry<Integer, ? extends ItemStack> itemEntry : inventory.all(selectedItem.getItemStack().getType()).entrySet() ) {
			ItemStack item = itemEntry.getValue();
			
			if ( this.rowClicked(account.getBankTab(tab).getTabSize()+1, itemEntry.getKey()) )
				continue;
			
			selectItem(itemEntry.getKey());
			BankItem oldItem = null;
			
			if ( item.getDurability() == selectedItem.getItemStack().getDurability() ) {
				 
				
				if ( item.getAmount() + amountToAdd <= selectedItem.getItemStack().getMaxStackSize() ) {
					oldItem = toBankItem(selectedItem.getItemStack());
					oldItem.setSlot(selectedItem.getSlot());
					selectedItem.getItemStack().setAmount(item.getAmount() + amountToAdd);
					updateBankAccountItem(oldItem, selectedItem);
					
					item.setAmount( item.getAmount() + amountToAdd );
					return true;
				} 
				
				
				if ( item.getAmount() < selectedItem.getItemStack().getMaxStackSize() ) {
					amountToAdd = ( item.getAmount() + amountToAdd ) % selectedItem.getItemStack().getMaxStackSize(); 
					item.setAmount(selectedItem.getItemStack().getMaxStackSize());
					
					oldItem = toBankItem(selectedItem.getItemStack());
					oldItem.setSlot(selectedItem.getSlot());
					
					selectedItem.getItemStack().setAmount(selectedItem.getItemStack().getMaxStackSize());
					updateBankAccountItem(oldItem, selectedItem);
				}
				
				
				if ( amountToAdd <= 0 )
					return true;
			}
		}
		
		
		if ( inventory.firstEmpty() < inventory.getSize() 
				&& inventory.firstEmpty() >= 0 ) {
			
			
			ItemStack is = selectedItem.getItemStack().clone();
			is.setAmount(amountToAdd);
			
			//create a new bank item
			selectedItem = toBankItem(is);
			selectedItem.setSlot(inventory.firstEmpty());
			addItemToBankAccount(selectedItem);
			
			
			inventory.setItem(inventory.firstEmpty(), is);
			return true;
		}
		
		return false;
	}*/
	
	public final boolean removeFromInventory(ItemStack item, InventoryClickEvent event) {
		if ( item.getAmount() != selectedItem.getItemStack().getAmount() ) {
			if ( item.getAmount() % selectedItem.getItemStack().getAmount() == 0 ) 
				event.setCurrentItem(new ItemStack(Material.AIR));
			else 
				item.setAmount( item.getAmount() % selectedItem.getItemStack().getAmount() );
		} else {
			event.setCurrentItem(new ItemStack(Material.AIR));
		}
		
		return false;
	}
	
	//Helper methods
	public static boolean rowClicked( int row, int slot )
	{
		if ( ( ( row - 1 ) * 9 ) <= slot && slot < ( row * 9 ) )
			return true;
		return false;
	}
	
	public static int getRowSlot( int slot )
	{
		return slot % 9;
	}
	
	public static BankItem toBankItem(ItemStack is) {
		if ( is.getTypeId() == 0 )
			return null;
		
		String itemInfo = is.getTypeId()+":"+ is.getData().getData() +" a:"+is.getAmount() + " d:" + is.getDurability();
		if ( !is.getEnchantments().isEmpty() ) {
			itemInfo += " e:";
			for ( Enchantment ench : is.getEnchantments().keySet() ) 
				itemInfo += ench.getId() + "/" + is.getEnchantmentLevel(ench) + ",";
		}
		return new BankItem(itemInfo);
	}

	
}
