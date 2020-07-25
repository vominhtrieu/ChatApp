package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.Hashtable;

import javax.swing.DefaultListModel;

public class ListenThread extends Thread {
	BufferedReader reader;
	DefaultListModel<String> listModel;
	Hashtable<String, ChatWindow> windows;

	public ListenThread(BufferedReader reader, DefaultListModel<String> listModel,
			Hashtable<String, ChatWindow> windows) {
		this.reader = reader;
		this.listModel = listModel;
		this.windows = windows;
	}

	@Override
	public void run() {
		while (true) {
			try {
				String type = reader.readLine();
				if (type.equals("newUser")) {
					listModel.addElement(reader.readLine());
				} else if (type.equals("userLeft")) {
					String username = reader.readLine();
					listModel.removeElement(username);
					ChatWindow window = windows.get(username);
					if (window != null)
						window.addNewMessage(username, "Đã thoát", false);
				} else if (type.equals("message")) {
					String username = reader.readLine();
					String content = reader.readLine();
					ChatWindow window = windows.get(username);
					if (window != null) {
						window.addNewMessage(username, content, false);
					}
				} else if (type.equals("emoji")) {
					String username = reader.readLine();
					String content = reader.readLine();
					ChatWindow window = windows.get(username);
					if (window != null) {
						window.addNewMessage(username, content, true);
					}
				}
			} catch (SocketException e) {
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
