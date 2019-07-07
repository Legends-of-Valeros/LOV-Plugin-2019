package com.codingforcookies.robert.core;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * Plugin container class.
 * 
 * @author Stumblinbear
 *
 */
public class BukkitMain extends JavaPlugin {
	public void onEnable() {
		Robert.enablePortable(this);
	}
}