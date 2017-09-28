package cluster.login;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.xephi.authme.events.LoginEvent;

public class AuthmeHook implements Listener {

	public AuthmeHook() {
		Bukkit.getPluginManager().registerEvents(this, LoginLimiter.instance);
	}
	
	@EventHandler
	public void authLogin(LoginEvent e) {
		if(LoginLimiter.instance.cache == CacheType.AUTHME) {
			Bukkit.getScheduler().runTaskAsynchronously(LoginLimiter.instance, new Runnable() {
				
				@Override
				public void run() {
					LoginLimiter.instance.safeCase.write(e.getPlayer().getName());
				}
			});
		}
	}
}
