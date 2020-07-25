package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class Server {
	private ArrayList<User> users;
	private Hashtable<String, UserThread> userThreads;

	public Server() {
		users = new ArrayList<>();
		userThreads = new Hashtable<String, UserThread>();

		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream("users.dat"), "UTF-8"));
			String username, password;

			while ((username = reader.readLine()) != null && (password = reader.readLine()) != null) {
				users.add(new User(username, password));
			}

			reader.close();
		} catch (FileNotFoundException e) {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void listen() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(4800);
			System.out.println("Listening at port 4800");

			while (true) {
				Socket socket = serverSocket.accept();
				UserThread thread = new UserThread(socket, userThreads, users);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.listen();
	}
}
