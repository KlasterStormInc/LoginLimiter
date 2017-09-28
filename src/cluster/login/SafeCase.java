package cluster.login;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SafeCase {

	FileConfiguration config;
	File file;
	List<String> list;
	final Object sync = new Object();
	
	public SafeCase(Plugin plugin) {
		file = new File(plugin.getDataFolder() + File.separator + "usernames.yml");
		if(!file.exists()) 
			try {
				file.createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
			}
		config = YamlConfiguration.loadConfiguration(file);
		list = config.getStringList("players");
	}
	
	
	
	public boolean allowed(String name) {
		synchronized (sync) {
			try {
				if (list == null)
					return true;
				for (String s : list) {
					if (!s.equalsIgnoreCase(name))
						continue;
					if (!s.equals(name))
						return false;
				}
				return true;

			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public String getReal(String name) {
		synchronized (sync) {
			try {
				if (list == null)
					return name;
				for (String s : list) {
					if (s.equalsIgnoreCase(name))
						return s;
				}
				return null;

			} catch (Exception e) {
				return name;
			}
		}
	}
	
	public boolean write(String name) {
		String r = getReal(name);
		if(r != null) remove(r);
		return write0(name);
	}
	
	public boolean write0(String name) {
		synchronized (sync) {
			try {
				if (list == null)
					list = new ArrayList<String>();
				if (list.contains(name))
					return false;
				list.add(name);
				config.set("players", list);
				config.save(file);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}



	public String remove(String name) {
		synchronized (sync) {
			try {
				for (String s : list) {
					if(s.equalsIgnoreCase(name)) {
						list.remove(s);
						config.set("players", list);
						config.save(file);
						return s;
					}
				}
			} catch (IOException e) {
				// empty catch block
			}
			return null;
		}
	}
	
	public int size() {
		synchronized (sync) {
			return list.size();
		}
	}
	
//	public void save() {
//		synchronized (sync) {
//			try {
//				config.save(file);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//	}
}
