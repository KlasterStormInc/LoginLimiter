package cluster.login;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

public class LoginLimiter extends JavaPlugin implements Listener {

	public static LoginLimiter instance;
	
	private String kickLimit, kickCase, noPermission;
	private boolean checkNick;
	
	SafeCase safeCase;
	CacheType cache;
	private LoginTimer timer;
	private AuthmeHook authme;

	
	
	@Override
	public void onEnable() {
		instance = this;
		
		if(!new File(getDataFolder() + File.separator + "config.yml").exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
		
		
		load();
		safeCase = new SafeCase(this);
		
		Bukkit.getPluginManager().registerEvents(this, this);
		PluginCommand command = getCommand("loginlimiter");
		if(command != null) command.setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("loginlimiter.command")) {
			sender.sendMessage(noPermission);
			return true;
		}
		
		if(args.length == 0)
		{
			sender.sendMessage("§3--------[§bLoginLimiter§3]--------");
			sender.sendMessage("§aAdd player nickname §7- §e/" + label + " store <player>");
			sender.sendMessage("§aRemove player nickname §7- §e/" + label + " remove <player>");
			sender.sendMessage("§aReload config §7- §e/" + label + " reload");
			return true;
		}
		
		if(args[0].equalsIgnoreCase("store"))
		{
			if(args.length < 2)
			{
				sender.sendMessage("§cUsage - /" + label + " store <player>");
				return true;
			}
			
			String name = args[1];
			Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
				
				@Override
				public void run() {
					String real = safeCase.getReal(name);
					if(real != null) {
						if(real.equals(name)) {
							sender.sendMessage("§cNickname §6" + real + "§c is already stored");
							return;
						}
						safeCase.remove(real);
					}
					
					safeCase.write0(name);
					sender.sendMessage("§aNickname §6" + name + "§a has been saved" + 
							(real != null ? " instead of §c" + real : ""));
				}
			});
			
			return true;
		}
		
		
		if(args[0].equalsIgnoreCase("remove"))
		{
			if(args.length < 2)
			{
				sender.sendMessage("§cUsage - /" + label + " remove <player>");
				return true;
			}
			
			String name = args[1];
			Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
				
				@Override
				public void run() {
					String real = safeCase.getReal(name);
					
					if(real == null) {
						sender.sendMessage("§cNickname §6" + name + "§c is not stored");
						return;
					}
					safeCase.remove(real);
					sender.sendMessage("§aNickname §6" + real + "§a has been removed");
				}
			});
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("reload"))
		{
			reloadConfig();
			load();
			sender.sendMessage("§aLoginLimiter has been reloaded");
			return true;
		}
		
		sender.sendMessage("§cUnknown command - '" + args[0] + "'");
		return true;
	}
	
	@EventHandler
	public void asyncJoin(AsyncPlayerPreLoginEvent e) {
		String name = e.getName();
		String real = safeCase.getReal(name);
		
		if(real == null) {
			timer.login();
			
			if(!timer.allowed()) {
				e.disallow(Result.KICK_BANNED, kickLimit);
			}
			return;
		}
		
		if(checkNick && !real.equals(name)) {
			e.disallow(Result.KICK_BANNED, kickCase.replace("{name}", name).replace("{realname}", real));
			return;
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent e) {
		if(cache == CacheType.JOIN) {
			Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
				
				@Override
				public void run() {
					safeCase.write(e.getPlayer().getName());
				}
			});
		}
	}
	
	
	public void load() {
		if(authme != null) {
			HandlerList.unregisterAll(authme);
		}
		
		checkNick = getConfig().getBoolean("userCache.safeCase", true);
		kickLimit = getConfig().getString("messages.kickLimit", "Disconnected").replace("&", "§");
		kickCase = getConfig().getString("messages.kickCase", "Disconnected").replace("&", "§");
		noPermission = getConfig().getString("messages.noPermission", "&cInsufficient permissions!").replace("&", "§");
		
		try {
			cache = CacheType.valueOf(getConfig().getString("userCache.storeType", "DISABLED").toUpperCase());
		} catch (Exception e) {
			cache = CacheType.DISABLED;
		}
		
		timer = new LoginTimer(getConfig().getLong("timeout"), getConfig().getInt("threshold"));
		
		if(cache == CacheType.AUTHME)
		{
			try {
				Class.forName("fr.xephi.authme.events.LoginEvent");
				authme = new AuthmeHook();
				getLogger().info("AuthMe successfully hooked!");
			} catch (Exception e) {
				getLogger().warning("AuthMe is not installed on this server!");
				cache = CacheType.DISABLED;
			}
		}
	}
	
	
	
	
	
}
