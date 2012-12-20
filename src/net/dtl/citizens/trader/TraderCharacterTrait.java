package net.dtl.citizens.trader;


import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizens.trader.parts.BankerPart;
import net.dtl.citizens.trader.parts.TraderConfigPart;
import net.dtl.citizens.trader.parts.TraderStockPart;

public class TraderCharacterTrait extends Trait {
	private EcoNpcType type = EcoNpcType.SERVER_TRADER;
	
	private TraderConfigPart config;
	private TraderStockPart stock;
	private BankerPart banker;

	public TraderCharacterTrait() {
		super("trader");
	}
	
	@Override
	public void onSpawn() {
		CitizensTrader.getNpcEcoManager().addEconomyNpc(npc);
	}
	
	@Override
	public void onAttach()
	{
		type = EcoNpcType.SERVER_TRADER;
		implementTrader();
		
		if ( CitizensTrader.dtlWalletsEnabled() )
			config.loadDtlWallet(npc);
		
		CitizensTrader.getNpcEcoManager().addEconomyNpc(npc);
	}
	
	public TraderStockPart getStock() {
		return stock;
	}
	public TraderConfigPart getConfig() {
		return config;
	}
	
	public void implementTrader()
	{
		config = new TraderConfigPart();
		stock = new TraderStockPart("Stock");
	}
	public void implementBanker()
	{
		banker = new BankerPart();
	}
	
	//The EcoNpc's type
	public EcoNpcType getType()
	{
		return type;
	}
	public void setType(EcoNpcType type)
	{
		this.type = type;
	}
	
	@Override
	public void load(DataKey data) throws NPCLoadException {
		String type = data.getString("type", "trader");
		
		if ( type.equals("trader") )
		{
			this.type = EcoNpcType.getTypeByName( data.getString("trader") );
			
			if ( config == null )
			{
				config = new TraderConfigPart();
				stock = new TraderStockPart(getNPC().getFullName() + "'s stock");
			}
			
			config.load(data);
			stock.load(data);
			
			if ( CitizensTrader.dtlWalletsEnabled() )
				config.loadDtlWallet(npc);
			
			if ( this.type.equals(EcoNpcType.MARKET_TRADER) )
				stock.linkItems();
		}
		else
		if ( type.equals("banker") )
		{
			this.type = EcoNpcType.getTypeByName( data.getString("banker") );
			
			if ( banker == null )
				banker = new BankerPart();
			

			if ( CitizensTrader.dtlWalletsEnabled() )
				banker.loadDtlWallet(npc);
			
			banker.load(data);
		}
		//old version loading
		else
		{
			this.type = EcoNpcType.getTypeByName( data.getString("trader-type", data.getString("type")) );
			
			if ( config == null )
			{
				config = new TraderConfigPart();
				stock = new TraderStockPart(getNPC().getFullName() + "'s stock");
			}
			
			config.load(data);
			stock.load(data);
			
			if ( this.type.equals(EcoNpcType.MARKET_TRADER) )
				stock.linkItems();
		}
	}

	@Override
	public void save(DataKey data) 
	{
		if ( type.isBanker() )
		{
			data.setString("type", "banker");
			data.setString("banker", type.toString());

			banker.save(data);
		}
		else if ( type.isTrader() )
		{
			data.setString("type", "trader");
			data.setString("trader", type.toString());
			
			if ( config != null )
				config.save(data);
			if ( stock != null )
				stock.save(data);
		}
	}
	
	
	public enum EcoNpcType {
		PLAYER_TRADER, SERVER_TRADER, MARKET_TRADER, PRIVATE_BANKER, MONEY_BANKER;
		
		public boolean isTrader()
		{
			if ( this.equals(PLAYER_TRADER) 
					|| this.equals(SERVER_TRADER) 
					|| this.equals(MARKET_TRADER) )
				return true;
			return false;
		}
		public boolean isBanker()
		{
			if ( this.equals(PRIVATE_BANKER) 
					|| this.equals(MONEY_BANKER) )
				return true;
			return false;
		}
		
		public static EcoNpcType getTypeByName(String n) {
			if ( n.equalsIgnoreCase("server") ) 
				return EcoNpcType.SERVER_TRADER;
			else if ( n.equalsIgnoreCase("player") )
				return EcoNpcType.PLAYER_TRADER;
			else if ( n.equalsIgnoreCase("market") )
				return EcoNpcType.MARKET_TRADER;
			else if ( n.equalsIgnoreCase("private") )
				return EcoNpcType.PRIVATE_BANKER;
			else if ( n.equalsIgnoreCase("money") )
				return EcoNpcType.MONEY_BANKER;
			return null;
		}
		
		@Override
		public String toString() {
			switch( this ) {
			case PLAYER_TRADER:
				return "player";
			case SERVER_TRADER:
				return "server";
			case MARKET_TRADER:
				return "market";
			case PRIVATE_BANKER:
				return "private";
			case MONEY_BANKER:
				return "money";
			default:
				break;
			}
			return "";
		}
	}

	//TODO CHANGE THIS!
	public BankerPart getBankTrait() {
		return banker;
	}
	
	
}
