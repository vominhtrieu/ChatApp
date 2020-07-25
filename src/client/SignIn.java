package client;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SignIn {
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;

	public SignIn() {
		try {
			this.socket = new Socket("localhost", 4800);

			InputStream is = socket.getInputStream();
			this.reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			OutputStream os = socket.getOutputStream();
			this.writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String sendMessage(String header, String username, String password) {
		try {
			writer.write(header);
			writer.newLine();
			writer.write(username);
			writer.newLine();
			writer.write(password);
			writer.newLine();
			writer.flush();

			String respond = reader.readLine();
			return respond;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public void createAndShowGUI() {
		JFrame frame = new JFrame("Đăng nhập/Đăng ký");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container container = frame.getContentPane();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(6, 1));

		JLabel usernameLabel = new JLabel("Tên người dùng");
		JTextField usernameTextField = new JTextField();
		usernameTextField.setPreferredSize(new Dimension(200, 30));

		JLabel passwordLabel = new JLabel("Mật khẩu");
		JTextField passwordTextField = new JTextField();
		passwordTextField.setPreferredSize(new Dimension(200, 30));

		JButton signInBtn = new JButton("Đăng nhập");
		signInBtn.setPreferredSize(new Dimension(200, 30));
		signInBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String respond = sendMessage("signin", usernameTextField.getText(), passwordTextField.getText());
				if (respond.equals("OK")) {
					MainMenu frontEnd = new MainMenu(usernameTextField.getText(), socket, reader, writer);
					frontEnd.createAndShowGUI();
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				} else {
					JOptionPane.showMessageDialog(null, respond);
				}
			}
		});

		JButton signUpBtn = new JButton("Đăng ký");
		signUpBtn.setPreferredSize(new Dimension(200, 30));
		signUpBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String respond = sendMessage("signup", usernameTextField.getText(), passwordTextField.getText());
				JOptionPane.showMessageDialog(null, respond);
			}
		});

		mainPanel.add(usernameLabel);
		mainPanel.add(usernameTextField);
		mainPanel.add(passwordLabel);
		mainPanel.add(passwordTextField);
		mainPanel.add(signInBtn);
		mainPanel.add(signUpBtn);

		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		container.add(mainPanel);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
