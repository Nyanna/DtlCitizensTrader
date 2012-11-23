package net.dtl.citizens.trader.backends;

import java.util.Map;

import net.dtl.citizens.trader.objects.BankAccount;
import net.dtl.citizens.trader.objects.BankItem;
import net.dtl.citizens.trader.traders.Banker.BankTabType;

public abstract class Backend {
	protected String saveTrigger;
	
	public Backend() {
		saveTrigger = "item";
	}
	
	abstract public Map<String, BankAccount> getAccounts();
	
	abstract public void addItem(String player, BankTabType tab, BankItem item);
	abstract public void removeItem(String player, BankTabType tab, BankItem item);
	
	abstract public void addBankTab(String player, BankTabType tab);
	abstract public void setBankTabItem(String player, BankTabType tab, BankItem item);
	
	abstract public BankAccount newAccount(String player);

	abstract public void increaseTabSize(String player, BankTabType tabType, int tabSize);
	
}
