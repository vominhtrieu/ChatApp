package client;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class MainMenu {
	String username;
	Socket socket;
	BufferedReader reader;
	BufferedWriter writer;

	public MainMenu(String username, Socket socket, BufferedReader reader, BufferedWriter writer) {
		this.username = username;
		this.socket = socket;
		this.reader = reader;
		this.writer = writer;
	}

	private DefaultListModel<String> requestActiveList() {
		try {
			DefaultListModel<String> listModel = new DefaultListModel<String>();
			writer.write("active");
			writer.newLine();
			writer.flush();

			String count = reader.readLine();
			int n = Integer.parseInt(count);

			for (int i = 0; i < n; i++) {
				String username = reader.readLine();
				listModel.addElement(username);
			}
			return listModel;
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListModel<String>();
		}
	}

	public void createAndShowGUI() {
		JFrame frame = new JFrame("Chat App");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				try {
					writer.write("exit");
					writer.newLine();
					writer.flush();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		});

		Container container = frame.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setPreferredSize(new Dimension(300, 300));
		Hashtable<String, ChatWindow> windows = new Hashtable<String, ChatWindow>();

		JLabel activeLabel = new JLabel("Danh sách người dùng đang Online");

		DefaultListModel<String> listModel = requestActiveList();
		JList<String> activeList = new JList<String>(listModel);
		activeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String conversationName = (String) activeList.getSelectedValue();
					if (windows.get(conversationName) != null) {
						JOptionPane.showMessageDialog(null, "Cửa sổ này đã mở");
						return;
					}
					ChatWindow window = new ChatWindow(username, conversationName, writer);
					window.createAndShowGUI();
					windows.put(conversationName, window);
				}
			}
		});
		ListenThread listenThread = new ListenThread(reader, listModel, windows);
		listenThread.start();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(activeList);
		scrollPane.setPreferredSize(new Dimension(300, 260));

		container.add(activeLabel);
		container.add(scrollPane);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
