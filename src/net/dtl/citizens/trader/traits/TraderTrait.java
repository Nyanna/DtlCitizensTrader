package net.dtl.citizens.trader.traits;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.object.Town;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderCharacterTrait.TraderType;
import net.dtl.citizens.trader.objects.Wallet;
import net.sacredlabyrinth.phaed.simpleclans.Clan;

public class TraderTrait {

	public enum WalletType {
		OWNER_WALLET, NPC_WALLET, BANK, INFINITE //Future CLAN_WALLET
, SIMPLE_CLANS, TOWNY, FACTIONS
;

		public static WalletType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("owner") ) 
				return WalletType.OWNER_WALLET;
			if ( n.equalsIgnoreCase("owner-wallet") ) 
				return WalletType.OWNER_WALLET;
			else if ( n.equalsIgnoreCase("npc") )
				return WalletType.NPC_WALLET;
			else if ( n.equalsIgnoreCase("npc-wallet") )
				return WalletType.NPC_WALLET;
			else if ( n.equalsIgnoreCase("bank") )
				return WalletType.BANK;
			else if ( n.equalsIgnoreCase("infinite") )
				return WalletType.INFINITE;
			else if ( n.equalsIgnoreCase("server-infinite") )
				return WalletType.INFINITE;
			else if ( n.startsWith("simple-clans") )
				return WalletType.SIMPLE_CLANS;
			else if ( n.startsWith("towny") )
				return WalletType.TOWNY;
			else if ( n.startsWith("factions") )
				return WalletType.FACTIONS;
			return null;
		}
		
		public String toString() {
			switch( this )
			{
			case OWNER_WALLET:
				return "owner";
			case NPC_WALLET:
				return "npc";
			case BANK:
				return "bank";
			case INFINITE:
				return "infinite";
			case SIMPLE_CLANS:
				return "simle-clans";
			case TOWNY:
				return "towny";
			case FACTIONS:
				return "factions";
			default: 
				break;
			}
			return "";
		}
		
		public static String toString(WalletType w) {
			switch( w ) {
			case OWNER_WALLET:
				return "owner";
			case NPC_WALLET:
				return "npc";
			case BANK:
				return "bank";
			case INFINITE:
				return "infinite";
			case SIMPLE_CLANS:
				return "simple-clans";
			case TOWNY:
				return "towny";
			case FACTIONS:
				return "factions";
			}
			return "";
		}
	}
	
	

	private WalletType wType = WalletType.INFINITE;
	private TraderType tType = TraderType.SERVER_TRADER;
	
	private Wallet w;
	private String owner;
	private String pattern;
	private boolean enabled;
	
	public TraderTrait() {
	//	super("type");
		w = new Wallet(wType);
		w.setMoney(0.0);
		owner = "no owner";
		enabled = true;
		pattern = "";
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwner() {
		return this.owner;
	}
	
	public void setWalletType(WalletType type) {
		// if the trader is a server trader (admin shop) allow to sat anny walet
		if ( tType.equals(TraderType.SERVER_TRADER) ) 
		{
			wType = type;
			w.setWalletType(wType);
		}
		else
		// if the trader is a player trader
		if ( tType.equals(TraderType.PLAYER_TRADER) ) 
		{
			
			//disallow infinite money
			if ( type.equals(WalletType.INFINITE) )
				return;
			
			
			wType = type;
			w.setWalletType(wType);
		}
		else
		// if the trader is a player trader
		if ( tType.equals(TraderType.MARKET_TRADER) ) 
		{
			
			//disallow infinite money
			if ( type.equals(WalletType.INFINITE) )
				return;
			
			
			wType = type;
			w.setWalletType(wType);
		}
	}
	
	public void setTraderType(TraderType type) {
		if ( type.equals(TraderType.PLAYER_TRADER) ) {
			tType = type;
			wType = WalletType.NPC_WALLET;
		}
		if ( type.equals(TraderType.SERVER_TRADER) ) {
			tType = type;
			wType = WalletType.INFINITE;
		}
		if ( type.equals(TraderType.MARKET_TRADER) ) {
			tType = type;
			wType = WalletType.OWNER_WALLET;
		}
		w.setWalletType(wType);
		return;
	}
	
	public TraderType getTraderType() {
		return tType;
	}
	public WalletType getWalletType() {
		return wType;
	}
	
	public Wallet getWallet() {
		return w;
	}
	
	public String getPattern()
	{
		return pattern;
	}
	
	public boolean buyTransaction(Player pBuying, double price) {
		return transaction(owner, pBuying.getName(), false, price);
	}
	public boolean transaction(String pSelling, String pBuying, boolean isOwner, final double price) {
		if ( w.withdraw(pBuying, price, isOwner) ) {
			w.deposit(pSelling, price, !isOwner);
			return true;
		} 
		return false;
	}
	
	public boolean sellTransaction(Player pSelling, double price) {
		return transaction(pSelling.getName(), owner, true, price);
	}
/*	public boolean sellTransaction(String pBuying, String pSelling, double price) {
		if ( w.withdraw(pBuying, price, true) ) {
			w.deposit(pSelling, price, false);
			return true;
		}
		return false;
	}*/
	
	public void load(DataKey data) throws NPCLoadException {
		/*if ( data.keyExists("trader-type") ) {
			tType = TraderType.getTypeByName(data.getString("trader-type", "server"));
		}*/
		if ( data.keyExists("wallet-type") ) {
			String walletType = data.getString("wallet-type", "infinite");
			if ( walletType.startsWith("simple-clans") )
			{
				if ( CitizensTrader.getSimpleClans() == null )
				{
					wType = WalletType.NPC_WALLET;
				}
				else
				{
					Clan clan = CitizensTrader.getSimpleClans().getClanManager().getClan(walletType.substring(13));
					if ( clan != null )
					{
						wType = WalletType.SIMPLE_CLANS;
						w.setClan(clan);
					}
				}
			}
			else
			if ( walletType.startsWith("towny") )
			{
				if ( CitizensTrader.getTowny() == null )
				{
					wType = WalletType.NPC_WALLET;
					
				}
				else
				{
					Town town = CitizensTrader.getTowny().getTownyUniverse().getTownsMap().get(walletType.substring(6));
					if ( town != null )
					{
						wType = WalletType.TOWNY;
						w.setTown(town);
					}
				}
			}
			else
			if ( walletType.startsWith("factions") )
				{
					if ( CitizensTrader.getFactions() == null )
					{
						wType = WalletType.NPC_WALLET;
						
					}
					else
					{
						Faction faction = Factions.i.getByTag(walletType.substring(9));
						if ( faction != null )
						{

							wType = WalletType.FACTIONS;
							w.setFaction(faction);
							
						}
						wType = WalletType.NPC_WALLET;
							
					}
				}
			else
				wType = WalletType.getTypeByName(walletType);
			
			w.setWalletType(wType);
		}
		if ( data.keyExists("owner") ) {
			owner = data.getString("owner","no-owner");
		}
		if ( data.keyExists("pattern") ) {
			pattern = data.getString("pattern", CitizensTrader.getInstance().getConfig().getString("trader.patterns.default",""));
		}
		if ( data.keyExists("transactions-enabled") ) {
			enabled = data.getBoolean("enabled",true);
		}
		if ( data.keyExists("money") ) {
			w.setMoney(data.getDouble("money"));
		}
	}

	public void save(DataKey data) {
		String account = ""; 
		if ( getWalletType().equals(WalletType.TOWNY) )
			account = getWallet().getTown();
		if (  getWalletType().equals(WalletType.SIMPLE_CLANS) )
			account =  getWallet().getClan();
		if (  getWalletType().equals(WalletType.FACTIONS) )
			account =  getWallet().getFaction();
		if (  getWalletType().equals(WalletType.BANK) )
			account =  getWallet().getBank();

		data.setString("wallet-type", WalletType.toString(wType) + ( account.isEmpty() ? "" : "." + account ) );
		data.setString("owner", owner);
		data.setString("pattern", pattern);
		data.setBoolean("transactions-enabled", enabled);
		data.setDouble("money", w.getMoney());
	}

	public void setPattern(String string) {
		pattern = string;
	}

}
