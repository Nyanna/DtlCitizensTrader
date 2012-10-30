package net.dtl.citizens.trader;

import java.text.DecimalFormat;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.dtl.citizens.trader.TraderCharacterTrait.TraderType;
import net.dtl.citizens.trader.traders.EconomyNpc;
import net.dtl.citizens.trader.traders.Trader;
import net.dtl.citizens.trader.traders.Trader.TraderStatus;
import net.dtl.citizens.trader.traits.TraderTrait;
import net.dtl.citizens.trader.traits.TraderTrait.WalletType;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import net.sacredlabyrinth.phaed.simpleclans.Clan;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.object.Town;

/**
 * 
 * @author Dandielo
 *
 */
public final class TraderCommandExecutor implements CommandExecutor {
	
	//Config values
	//	private boolean debug;
	
	//plugin instance
	public static CitizensTrader plugin;
	
	//managers
	private static NpcEcoManager traderManager;
	private static PermissionsManager permsManager;
	private static LoggingManager logManager;
	private static LocaleManager locale;

	
	//constructor
	public TraderCommandExecutor(CitizensTrader instance) {
		plugin = instance;

		locale = CitizensTrader.getLocaleManager();
		permsManager = CitizensTrader.getPermissionsManager();
		traderManager = CitizensTrader.getNpcEcoManager();
		logManager = CitizensTrader.getLoggingManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		
		//is player
		if ( sender instanceof Player )
		{
			Player player = (Player) sender;

			//get the selected NPC
			EconomyNpc economyNpc = traderManager.getInteractionNpc(player.getName());
			
			if ( args.length < 1 )
			{
				player.sendMessage(ChatColor.AQUA + "DtlTraders " + plugin.getDescription().getVersion() + ChatColor.RED + "" );
				
				if ( economyNpc != null && economyNpc instanceof Trader )
				{
					
					Trader trader = (Trader) economyNpc;
					player.sendMessage(ChatColor.GOLD + "==== " + ChatColor.YELLOW + trader.getNpc().getName() + ChatColor.GOLD + " ====");
					player.sendMessage(locale.getLocaleString("xxx-setting-value", "setting:trader").replace("{value}", trader.getTraderConfig().getTraderType().toString()));
					player.sendMessage(locale.getLocaleString("xxx-setting-value", "setting:owner").replace("{value}", trader.getTraderConfig().getOwner()));
					player.sendMessage(locale.getLocaleString("xxx-setting-value", "setting:wallet").replace("{value}", trader.getWallet().getWalletType().toString()));
					player.sendMessage(locale.getLocaleString("xxx-setting-value", "setting:pattern").replace("{value}", trader.getTraderConfig().getPattern()));
					
				}
				return true;
				//return false;
			}
			
			
			

			
			//no npc selected
			if ( economyNpc == null )
			{
				if ( args[0].equalsIgnoreCase("help") )
				{
					player.sendMessage(ChatColor.AQUA + "DtlTraders " + plugin.getDescription().getVersion() + ChatColor.RED + " - Trader commands list" );
					return false;
				}
				//reload plugin
				if ( args[0].equalsIgnoreCase("create") )
				{
					if ( !this.generalChecks(player, "create", null) )
						return true;
					
					return createTrader(player, args);
				}
				if ( args[0].equals("log") )
				{
					if ( !this.generalChecks(player, "log", null) )
						return true;
					return log(player, "", args);
				}
				if ( args[0].equals("clearlog") )
				{
					if ( !this.generalChecks(player, "clearlog", null) )
						return true;
					
					return clearLog(player, "", args);
				}
				if ( args[0].equals("reload") )
				{
					
					sender.sendMessage( locale.getLocaleString("reload-config") );
					CitizensTrader.getInstance().getItemConfig().reloadConfig();
					CitizensTrader.getInstance().reloadConfig();
					CitizensTrader.getLocaleManager().reload();
					
					return true;
				}
				if ( args[0].equalsIgnoreCase("manage") )
				{
					if ( !this.generalChecks(player, "manage", null) )
						return true;
					
					if ( args.length < 2 )
					{
						player.sendMessage( locale.getLocaleString("xxx-argument-missing", "argument:name") );
						return true;
					}
					//TODO name adding
					String name = "";
					for ( int i = 1 ; i < args.length ; ++i )
						name += args[i];
					
					EconomyNpc trader = traderManager.getServerTraderByName(name);
					
					if ( trader == null )
					{
						player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:name") );
						return false;
					}
					traderManager.addInteractionNpc(player.getName(), trader);
					return traderManage(player, trader);
				}
				
				
				return true;
			}
			//npc has been selected
			else
			{
				
				//is trader type
				if ( !( economyNpc instanceof Trader ) )
				{
					player.sendMessage( locale.getLocaleString("xxx-not-selected", "object:trader") );
					return true;
				}
				
				Trader trader = (Trader) economyNpc;
				
				if ( args[0].equalsIgnoreCase("help") )
				{
					return false;
				}
				if ( args[0].equalsIgnoreCase("manage") )
				{

					if ( !this.generalChecks(player, "manage", null) )
						return true;
					
				//	EconomyNpc trader = traderManager.getServerTraderByName(args[1]);
					
					traderManager.addInteractionNpc(player.getName(), trader);
					return traderManage(player, trader);
					//return traderManage(player, trader);
				}
				if ( args[0].equals("open") )
				{
					if ( !this.generalChecks(player, "type", null) )
						return true;
					
					return openTraderInventory(player, trader);
				}
				//list command
				if ( args[0].equals("list") )
				{
					if (  args.length < 2 )
					{
						player.sendMessage( locale.getLocaleString("xxx-argument-missing").replace("{argument}", "Transaction type") );
						return true;
					}
					if ( !this.generalChecks(player, "list", ( args.length >= 2 ? args[1] : null )) )
						return true;
					
					return getItemList(player, trader, args, TraderStatus.getByName(args[1]) );	
				}
				if ( args[0].equals("type") )
				{
					if ( !this.generalChecks(player, "type", null) )
						return true;
					
					return setType(player, trader, ( args.length > 1 ? args[1] : "" ) );
				}
				if ( args[0].equals("wallet") )
				{
					if ( !this.generalChecks(player, "wallet", null) )
						return true;
					
					return setWallet(player, trader, ( args.length > 1 ? args[1] : "" ), ( args.length > 2 ? args[2] : "" ) );
				}
				if ( args[0].equals("owner") )
				{
					if ( !this.generalChecks(player, "owner", null) )
						return true;
					
					if ( args.length > 1 )
						return setOwner(player, trader, args[1]);
					else
						return getOwner(player, trader);
				}
				if ( args[0].equals("clear") )
				{
					if ( !this.generalChecks(player, "clear", null) )
						return true;
					
					return clear(player, trader, args);
				}
				if ( args[0].equals("pattern") )
				{
					if ( !this.generalChecks(player, "pattern", null) )
						return true;
					
					return pattern(player, trader, args);
				}
				if ( args[0].equals("log") )
				{
					if ( !this.generalChecks(player, "log", null) )
						return true;
					
					return log(player, trader.getNpc().getName(), args);
				}
				if ( args[0].equals("clearlog") )
				{
					if ( !this.generalChecks(player, "clearlog", null) )
						return true;
					
					return clearLog(player, trader.getNpc().getName(), args);
				}
				if ( args[0].equals("balance") )
				{
					if ( !this.generalChecks(player, "balance", null) )
						return true;
					
					return balance(player, trader);
				}
				if ( args[0].equals("withdraw") )
				{
					if ( !this.generalChecks(player, "withdraw", null) )
						return true;
					
					return withdraw(player, trader, args[1]);
				}
				if ( args[0].equals("deposit") )
				{
					if ( !this.generalChecks(player, "deposit", null) )
						return true;
					
					return withdraw(player, trader, args[1]);
				}
				//reload plugin
				if ( args[0].equalsIgnoreCase("create") )
				{
					if ( !this.generalChecks(player, "create", null) )
						return true;
					
					return createTrader(player, args);
				}
				
				return false;
			}
				
		}		
		else
		{
			if ( args.length < 1 )
				return false;
			
			if ( args[0].equals("reload") )
			{
				
				sender.sendMessage( locale.getLocaleString("reload-config") );
				CitizensTrader.getInstance().getItemConfig().reloadConfig();
				CitizensTrader.getInstance().reloadConfig();
				CitizensTrader.getLocaleManager().reload();
				
				return true;
			}
		}
		
		return false;
	}
	
	private boolean openTraderInventory(Player player, Trader trader) {
		player.openInventory(trader.getInventory());
		trader.switchInventory(Trader.getManageStartStatus(player));
		return true;
	}

	private boolean traderManage(Player player, EconomyNpc economyNpc) 
	{
		Trader trader = (Trader) economyNpc;
		
		if ( !permsManager.has(player, "dtl.trader.options.manage") )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
			return true;
		}
		if ( !trader.getTraderConfig().getOwner().equals(player.getName())
				&& !player.isOp() )
		{
			if ( !permsManager.has(player, "dtl.trader.bypass.managing") )
			{
				player.sendMessage( locale.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
				return true;
			}
		}
		
		if ( TraderStatus.hasManageMode(economyNpc.getTraderStatus()) )
		{
			traderManager.removeInteractionNpc(player.getName());
			//economyNpc.setTraderStatus(TraderStatus.SELL);
			trader.switchInventory( Trader.getStartStatus(player) );
			player.sendMessage(ChatColor.AQUA + trader.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
		}
		else
		{
			//economyNpc.setTraderStatus(TraderStatus.MANAGE);
			player.sendMessage(ChatColor.AQUA + trader.getNpc().getFullName() + ChatColor.RED + " entered the manager mode!");
			trader.switchInventory( Trader.getManageStartStatus(player) );
		}
		return true;
	}

	private boolean reloadPatterns(Player player, Trader trader) 
	{
		CitizensTrader.getPatternsManager().reload();
		
		for ( NPC npc : this.traderManager.getAllServerTraders() )
		{
			npc.getTrait(TraderCharacterTrait.class).getInventoryTrait().reloadStock();
		}
		
		return true;
	}

	private boolean removePattern(Player player, Trader trader) {
		
		trader.getTraderStock().removePattern();
		trader.getTraderConfig().setPattern("");
		trader.getInventory().clear();
	//	trader.switchInventory(trader.getTraderStatus());
		player.sendMessage( locale.getLocaleString("removed-pattern") );
		
		return true;
	}
	
	private boolean pattern(Player player, Trader trader, String[] args) {
		
		if ( args.length <= 1 )
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-missing", "argument:action") );
			return true;
		}
		
		if ( args[1].toLowerCase().equals("remove") )
		{
			return this.removePattern(player, trader);
		}
		if ( args[1].toLowerCase().equals("reload") )
		{
			if ( !permsManager.has(player, "dtl.trader.commands.patternreload") )
			{
				player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
				return true;
			}
			return this.reloadPatterns(player, trader);
		}
		if ( !args[1].equals("set") )
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:action") );
			return true;
		}
		
		if ( args.length <= 2 )
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-missing", "argument:pattern") );
			return true;
		}
		
		if ( trader.getTraderStock().setPattern(args[2]) )
		{
			trader.getTraderConfig().setPattern(args[2]);
			player.sendMessage( locale.getLocaleString("xxx-value-changed", "", "manage:{argument}", "argument:pattern").replace("{value}", args[2].toLowerCase()) );
		}
		else
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:pattern") );
		}
		return true;
	}

	private boolean clear(Player player, Trader trader, String[] args) {
		if ( args.length > 1 )
		{
			if ( !args[1].toLowerCase().equals("sell") || !args[1].toLowerCase().equals("buy") )
			{
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:stock") );
				return true;
			}
			trader.getTraderStock().clearStock(args[1]);
			
			player.sendMessage( locale.getLocaleString("xxx-stock-cleared", "manage:" + args[1], "action:cleared") );
			return true;
		}
		
		trader.getTraderStock().clearStock("");
		
		player.sendMessage( locale.getLocaleString("xxx-stock-cleared", "manage:sell", "action:cleared") );
		player.sendMessage( locale.getLocaleString("xxx-stock-cleared", "manage:buy", "action:cleared") );
		return true;
	}

	private boolean clearLog(Player player, String trader, String[] args)
	{
		if ( trader.isEmpty() )
		{
			for ( int i = 0 ; i < args.length ; ++i )
			{
				if ( i + 1 < args.length )
				{
					trader += args[i+1] + " ";
				}
			}
			if ( !trader.isEmpty() )
				trader = trader.substring(0, trader.length()-1);
		}
		
		logManager.clearPlayerLogs(player.getName(), trader);
		player.sendMessage( locale.getLocaleString("log-xxx", "log:trader", "action:cleared") );
		
		return true;
	}
	
	private boolean log(Player player, String trader, String[] args) {
		
		if ( trader.isEmpty() )
		{
			for ( int i = 0 ; i < args.length ; ++i )
			{
				if ( i + 1 < args.length )
				{
					trader += args[i+1] + " ";
				}
			}
			if ( !trader.isEmpty() )
				trader = trader.substring(0, trader.length()-1);
		}
		
		List<String> logs = logManager.getPlayerLogs(player.getName(), trader);
		
		if ( logs == null )
			return true;
		
		for ( String log : logs )
			player.sendMessage(log);
		return true;
	}

	public boolean generalChecks(Player player, String commandPermission, String optionsPermission)
	{
		
		//check permissions
		if ( !permsManager.has(player, "dtl.trader.commands." + commandPermission)  )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return false;
		}
		
		if ( optionsPermission != null )
		//check permissions
		if ( !permsManager.has(player, "dtl.trader.options." + optionsPermission)  )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return false;
		}
		
		return true;
	}
	

	public TraderType getDefaultTraderType(Player player) {
		
		//server trader as default
		if ( permsManager.has(player, "dtl.trader.types.server") )
			return TraderType.SERVER_TRADER;
		else
		//next default is player trader 
		if ( permsManager.has(player, "dtl.trader.types.player") )
			return TraderType.SERVER_TRADER;
		
		//else return no default
		return null;
	}
	
	public WalletType getDefaultWalletType(Player player, TraderType traderType) {
		//server default is infinite
		if ( permsManager.has(player, "dtl.trader.wallets.infinite") && !traderType.equals(TraderType.PLAYER_TRADER) )
			return WalletType.INFINITE;
		else
		//next default is npc wallet
		if ( permsManager.has(player, "dtl.trader.wallets.npc") )
			return WalletType.NPC_WALLET;
		else
		//next default is player wallet
		if ( permsManager.has(player, "dtl.trader.wallets.owner") )
			return WalletType.OWNER_WALLET;
		else
		//next server default is custom bank
		if ( permsManager.has(player, "dtl.trader.wallets.bank") )
			return WalletType.BANK;
		
		//else return no default
		return null;
	}

	
	
	public boolean getItemList(Player player, Trader trader, String[] args, TraderStatus status) 
	{
		//default page '0'
		int page = 0;
		
		try 
		{

			//have we maybe got a page number?
			if ( args.length > 2 )
			{
				//get the page number, My Precious... ;>
				page = Integer.parseInt(args[2]) - 1;
			}
			
		} 
		catch (NumberFormatException e)
		{
			
			//come on can;t you write a normal number... ?
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:page") );
		//	player.sendMessage( locale.getLocaleString("command-template").replace("{command}", "list").replace("{args}", "<transaction> [page]") );
			return true;
		}
		
		int size = trader.getTraderStock().getStockSize(status);
		int totalPages = ( size % 10 == 0 ? ( size / 10 ) : ( size / 10 ) + 1 );
		
		
		//we got a item list
		player.sendMessage( locale.getLocaleString("list-header").replace("{curp}", "" + (page+1)).replace("{allp}", "" + totalPages) );
		
		
		for ( String item : trader.getTraderStock().getItemList(status, locale.getLocaleString("list-message"), page) )
			player.sendMessage(item);
		return true;
	}
	
	//set the traders wallet type
	public boolean setWallet(Player player, Trader trader, String walletString, String bankAccount)
	{

		
		WalletType wallet = WalletType.getTypeByName(walletString);
		
		
		//towny
		
		
		//show wallet
		if ( wallet == null )
		{
			String account = ""; 
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.TOWNY) )
				account = trader.getWallet().getTown();
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.SIMPLE_CLANS) )
				account = trader.getWallet().getClan();
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.FACTIONS) )
				account = trader.getWallet().getFaction();
			if ( trader.getTraderConfig().getWalletType().equals(WalletType.BANK) )
				account = trader.getWallet().getBank();
			
			//send message
			player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:wallet").replace("{value}", trader.getTraderConfig().getWalletType().toString() + ( account.isEmpty() ? "" : "�6:�e" + account )) );
			
		}
		//change wallet
		else
		{
			
			if ( !permsManager.has(player, "dtl.trader.wallets." + walletString ) )
			{
				player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:wallet") );
				return true;
			}
			
			//bank
			if ( wallet.equals(WalletType.BANK) )
			{
				if ( bankAccount.isEmpty() )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				if ( !trader.getWallet().setBank(player.getName(), bankAccount) )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
			}
			else
			//clan
			if ( wallet.equals(WalletType.SIMPLE_CLANS) )
			{
				if ( CitizensTrader.getSimpleClans() == null ) {
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:wallet") );
					return true;
				}
				
				if ( bankAccount.isEmpty() )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				Clan clan = CitizensTrader.getSimpleClans().getClanManager().getClan(bankAccount);
				if ( clan == null )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				trader.getWallet().setClan(clan);
				
			}
			else
			//towny
			if ( wallet.equals(WalletType.TOWNY) )
			{
				if ( CitizensTrader.getTowny() == null )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:wallet") );
					return true;
				}
				
				if ( bankAccount.isEmpty() )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				Town town = CitizensTrader.getTowny().getTownyUniverse().getTownsMap().get(bankAccount);
				if ( town == null )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				
				trader.getWallet().setTown(town);
				
			}
			else
			//towny
			if ( wallet.equals(WalletType.FACTIONS) )
			{
				if ( CitizensTrader.getFactions() == null )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:wallet") );
					return true;
				}
				
				if ( bankAccount.isEmpty() )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				Faction faction = Factions.i.getByTag(bankAccount);
				if ( faction == null )
				{
					player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
					return true;
				}
				trader.getWallet().setFaction(faction);
			}
			
			
			//set the wallet type for both trader and wallet
			trader.getTraderConfig().setWalletType(wallet);

			//send message
			player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:wallet").replace("{value}", walletString + (bankAccount.isEmpty()?"":"�6:�e"+bankAccount)) );
		}
		
		
		return true;
	}
	
	//set the traders type
	public boolean setType(Player player, Trader trader, String typeString)
	{
		
		TraderType type = TraderType.getTypeByName(typeString);
		
		//show current trader type
		if ( type == null )
		{
			player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:trader").replace("{value}", trader.getTraderConfig().getTraderType().toString()) );
		}
		//change trader type
		else
		{
			
			if ( !permsManager.has(player, "dtl.trader.types." + typeString ) )
			{
				player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:trader") );
				return true;
			}

			trader.getTraderConfig().setTraderType(type);
			trader.getNpc().getTrait(TraderCharacterTrait.class).setTraderType(type);
			
			player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:trader").replace("{value}", typeString) );
		}
		
		return true;
	}
	
	//show the traders balance
	public boolean balance(Player player, Trader trader)
	{
		DecimalFormat f = new DecimalFormat("#.##");
		player.sendMessage( locale.getLocaleString("xxx-wallet", "action:balance").replace("{value}", f.format(trader.getWallet().getMoney()) ) );
		
		return true;
	}
	
	//withdraw money from the trader
	public boolean withdraw(Player player, Trader trader, String withdrawString)
	{
		
		double money = trader.getWallet().getMoney();
		double withdraw = 0.0;
		try 
		{
			withdraw = Double.valueOf(withdrawString);
		} 
		catch (NumberFormatException e)
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		if ( withdraw > money )
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		trader.getWallet().setMoney(money - withdraw);
		DecimalFormat f = new DecimalFormat("#.##");

		plugin.getEconomy().depositPlayer(player.getName(), withdraw);
		
		player.sendMessage( locale.getLocaleString("xxx-wallet", "action:withdrawed").replace("{value}", withdrawString) );
		player.sendMessage( locale.getLocaleString("xxx-wallet", "action:balance").replace("{value}", f.format(trader.getWallet().getMoney())) );
		
		return true;
	}
	
	//deposit money to the trader
	public boolean deposit(Player player, Trader trader, String depositString)
	{
		
		double money = trader.getWallet().getMoney();
		double deposit = 0.0;
		try 
		{
			deposit = Double.valueOf(depositString);
		} 
		catch (NumberFormatException e)
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		if ( !plugin.getEconomy().withdrawPlayer(player.getName(), deposit).type.equals(ResponseType.SUCCESS) )
		{
			player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:amount") );
			return true;
		}
		
		trader.getWallet().setMoney(money + deposit);
		DecimalFormat f = new DecimalFormat("#.##");
		
		player.sendMessage( locale.getLocaleString("xxx-wallet", "action:deposited").replace("{value}", depositString) );
		player.sendMessage( locale.getLocaleString("xxx-wallet", "action:balance").replace("{value}", f.format(trader.getWallet().getMoney())) );
		
		return true;
	}
	
	//setting the traders owner
	private boolean setOwner(Player player, Trader trader, String owner) {
		
		trader.getTraderConfig().setOwner(owner);
		player.sendMessage( locale.getLocaleString("xxx-setting-changed", "setting:owner").replace("{value}", owner) );
		
		return true;
	}
	
	//getting the traders owner
	private boolean getOwner(Player player, Trader trader) {

		player.sendMessage( locale.getLocaleString("xxx-setting-value", "setting:owner").replace("{value}", trader.getTraderConfig().getOwner() ) );

		
		return true;
	}
	
	//creating a trader, its easy ;)
	public boolean createTrader(Player player, String[] args)
	{
		String traderName = "";
		String owner = player.getName();
		String clanTag = "";
		String townName = "";
		String factionName = "";
		
		EntityType entityType = EntityType.PLAYER;
		TraderType traderType = getDefaultTraderType(player);
		WalletType walletType = getDefaultWalletType(player, traderType);
		
				
		//lets fetch the argument list
		for ( String arg : args )
		{
			if ( arg.startsWith("o:") )
			{
				owner = arg.substring(2);
			}
			else
			if ( arg.startsWith("sc:") )
			{
				clanTag = arg.substring(3);
			}
			else
			if ( arg.startsWith("town:") )
			{
				townName = arg.substring(5);
			}
			else
			if ( arg.startsWith("f:") )
			{
				factionName = arg.substring(2);
			}
			else
			//trader type set?
			if ( arg.startsWith("t:") )
			{
				//do we have permissions to set this trader type?
				if ( !permsManager.has(player, "dtl.trader.types." + arg.substring(2) ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:trader") );
					return true;
				}
				traderType = TraderType.getTypeByName(arg.substring(2));
				if ( traderType == null || traderType.isBanker() )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:type") );
					return true;
				}
			}
			else
			//wallet type set
			if ( arg.startsWith("w:") )
			{
				//do we have permissions to set this wallet type?
				if ( !permsManager.has(player, "dtl.trader.wallets." + arg.substring(2) ) )
				{
					player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:wallet") );
					return true;
				}
				walletType = WalletType.getTypeByName(arg.substring(2));
			}
			else
			//entity type set
			if ( arg.startsWith("e:") )
			{
				entityType = EntityType.fromName(arg.substring(2));
			}
			else
			{
				traderName += arg + " ";
			}
		}

		if ( traderName.isEmpty() || args.length == 1 || traderName.equals("create ") )
			traderName = "NPC";
		else 
			traderName = traderName.substring(7, traderName.length()-1);
		
		if ( walletType == null || traderType == null || entityType == null )
		{
			player.sendMessage( locale.getLocaleString("lacks-permissions-xxx", "object:command") );
			return true;
		}
		
		//creating the npc
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, traderName);
		npc.addTrait(TraderCharacterTrait.class);
		npc.addTrait(MobType.class);
		npc.getTrait(MobType.class).setType(entityType);
		npc.spawn(player.getLocation());
		
		//TODO towny wallets
		//change the trader settings
		TraderTrait settings = npc.getTrait(TraderCharacterTrait.class).getTraderTrait();
		npc.getTrait(TraderCharacterTrait.class).setTraderType(traderType);
		settings.setWalletType(walletType);
		if ( walletType.equals(WalletType.SIMPLE_CLANS) )
		{
			Clan clan = CitizensTrader.getSimpleClans().getClanManager().getClan(clanTag);
			if ( clan == null )
			{
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
				return true;
			}
			settings.getWallet().setClan(clan);
		}
		if ( walletType.equals(WalletType.TOWNY) )
		{

			Town town = CitizensTrader.getTowny().getTownyUniverse().getTownsMap().get(townName);
			if ( town == null )
			{
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
				return true;
			}
			settings.getWallet().setTown(town);
		}
		if ( walletType.equals(WalletType.FACTIONS) )
		{
			Faction faction = Factions.i.getByTag(factionName);
			if ( faction == null )
			{
				player.sendMessage( locale.getLocaleString("xxx-argument-invalid", "argument:account") );
				return true;
			}
			settings.getWallet().setFaction(faction);
		}
		settings.setOwner(owner);
		
		player.sendMessage( locale.getLocaleString("xxx-created-xxx", "entity:player", "entity:trader").replace("{name}", player.getName()) );
		return true;
	}
	
	
	
}