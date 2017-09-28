package cluster.login;

public enum CacheType {

	JOIN, AUTHME, DISABLED;

	public String d() {
		switch(this) {
		case AUTHME: return "§9AuthMe§r";
		case DISABLED: return "§cDisabled§r";
		case JOIN: return "§bOn join§r";
		default: return "?";
		}
	}
}
