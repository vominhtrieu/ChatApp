package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

public class UserThread extends Thread {
	private String username;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Hashtable<String, UserThread> userThreads;
	private ArrayList<User> users;

	public UserThread(Socket socket, Hashtable<String, UserThread> userThreads, ArrayList<User> users) {
		try {
			this.socket = socket;

			this.is = socket.getInputStream();
			this.reader = new BufferedReader(new InputStreamReader(this.is, "UTF-8"));

			this.os = socket.getOutputStream();
			this.writer = new BufferedWriter(new OutputStreamWriter(this.os, "UTF-8"));

			this.userThreads = userThreads;
			this.users = users;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private User getUser(String username) {
		for (User user : users) {
			if (user.getUsername().equals(username))
				return user;
		}
		return null;
	}

	private void handleSignUp() {
		try {
			String username = reader.readLine();
			String password = reader.readLine();
			String respond = "";

			if (getUser(username) == null) {
				BufferedWriter fileWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream("users.dat", true), "UTF-8"));
				fileWriter.write(username);
				fileWriter.newLine();
				fileWriter.write(password);
				fileWriter.newLine();
				fileWriter.flush();
				fileWriter.close();

				users.add(new User(username, password));
				respond = "Đăng ký thành công";
			} else {
				respond = "Tên đăng nhập đã tồn tại";
			}

			this.receiveMessage(respond + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleSignIn() {
		try {
			String username = reader.readLine();
			String password = reader.readLine();
			String respond = "";

			User user = getUser(username);
			if (user != null && user.getPassword().equals(password)) {
				if (userThreads.get(username) == null) {
					this.username = username;

					Set<String> keys = userThreads.keySet();
					for (String key : keys) {
						userThreads.get(key).receiveMessage("newUser\n" + username + "\n");
					}

					userThreads.put(username, this);
					respond = "OK";
				} else {
					respond = "Tài khoản này đã đăng nhập";
				}
			} else {
				respond = "Sai tên đăng nhập hoặc mật khẩu";
			}
			this.receiveMessage(respond + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleExit() {
		userThreads.remove(this.username);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Set<String> keys = userThreads.keySet();
		for (String key : keys) {
			userThreads.get(key).receiveMessage("userLeft\n" + username + "\n");
		}
	}

	public void getOnlineUser() {
		String respond = "";
		Set<String> keys = userThreads.keySet();
		respond += Integer.toString(keys.size() - 1) + "\n";
		for (String key : keys) {
			if (!key.equals(username)) {
				respond += key + "\n";
			}
		}
		this.receiveMessage(respond);
	}

	public synchronized void receiveMessage(String message) {
		try {
			writer.write(message);
			writer.flush();
		} catch (IOException e) {
			e.getMessage();
		}
	}

	public void sendMessage(Boolean isEmoji) {
		String receiverUsername;
		try {
			receiverUsername = reader.readLine();
			UserThread thread = userThreads.get(receiverUsername);
			if (thread != null) {
				String respond = "";
				if (!isEmoji) {
					respond += "message\n";
				} else {
					respond += "emoji\n";
				}
				respond += this.username + "\n" + reader.readLine() + "\n";
				thread.receiveMessage(respond);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void downloadFile(String fileName) {
		try {
			writer.close();
			reader.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		BufferedInputStream fileInputStream;
		try {
			fileInputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		int temp;
		byte[] buffer = new byte[4096];
		BufferedOutputStream outStream = new BufferedOutputStream(os);
		try {
			while ((temp = fileInputStream.read(buffer)) >= 0) {
				outStream.write(temp);
			}
			outStream.flush();
			outStream.close();
			os.close();
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void uploadFile() {

		BufferedOutputStream fileOutputStream;
		try {
			BufferedInputStream inStream = new BufferedInputStream(is);
			File directory;

			directory = new File("./upload");
			directory.mkdir();
			fileOutputStream = new BufferedOutputStream(
					new FileOutputStream(File.createTempFile("temp", "", directory)));
			try {
				byte[] buffer = new byte[4096];
				while ((inStream.read(buffer)) >= 0) {
					fileOutputStream.write(buffer);
				}
				fileOutputStream.close();
				writer.write("Done");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Listening for incoming message
		while (true) {
			try {
				String header = reader.readLine();

				if (header.equals("signup")) {
					handleSignUp();
				} else if (header.equals("signin")) {
					handleSignIn();
				} else if (header.equals("message")) {
					sendMessage(false);
				} else if (header.equals("emoji")) {
					sendMessage(true);
				} else if (header.equals("active")) {
					getOnlineUser();
				} else if (header.equals("fileUpload")) {
					uploadFile();
					socket.close();
					break;
				} else if (header.equals("exit")) {
					handleExit();
					break;
				} else {
					writer.write("Không hỗ trợ");
					writer.newLine();
					writer.flush();
				}
			} catch (SocketException e) {
				if (username != null && userThreads.get(username) != null) {
					userThreads.remove(username);
				}

				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
