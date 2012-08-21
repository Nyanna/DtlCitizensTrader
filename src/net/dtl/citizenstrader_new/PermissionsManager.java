package net.dtl.citizenstrader_new;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dtl.permissions.bukkit.DtlPermissions;

public class PermissionsManager {
	private final Logger logger = Logger.getLogger("Minecraft");
	private final String pluginPrefix = "[DtlTrader] ";
	
	private DtlPermissions dtlPerms;
//	private Permission vaultPerms;
//	private PexPermissions pexPerms;
//	private GroupManager gmPerms;
	
	public PermissionsManager() {
		this.initializeDtlPermissions();
		this.initializePexPermissions();
		this.initializeGroupManager();
	}
	
	public void initializeDtlPermissions() {
		dtlPerms = (DtlPermissions) Bukkit.getPluginManager().getPlugin("DtlPermissions");
		if ( dtlPerms == null )
			return;
		logger.info(pluginPrefix + dtlPerms.getDescription().getFullName() + " ver" + dtlPerms.getDescription().getVersion() + " hooked!");
		
	}
	public void initializeVaultPermissions() {
		logger.info(pluginPrefix + "Vault permissions not supported atm, sorry :<");
		logger.info(pluginPrefix + "SuperPerms enabled");
	}
	public void initializePexPermissions() {
		logger.info(pluginPrefix + "PermissionsEx not supported atm, sorry :<");
		logger.info(pluginPrefix + "SuperPerms enabled");
	}
	public void initializeGroupManager() {
		logger.info(pluginPrefix + "GroupManager not supported atm, sorry :<");
		logger.info(pluginPrefix + "SuperPerms enabled");
	}
	
	public boolean has(Player player, String permission) {
		//if using dtlPermissions System
		if ( dtlPerms != null ) 
		{
			return dtlPerms.has(player, permission);
		}
		//if no system was found, use superperms
		else
		{
			return player.hasPermission(permission);
		}
	}
}