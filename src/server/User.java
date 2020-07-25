package server;

public class User {
	private String username, password;
	private Boolean isOnline;

	public User(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
		setIsOnline(false);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}
}
