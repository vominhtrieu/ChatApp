package client;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatWindow {
	String username;
	String conversationName;
	BufferedWriter writer;
	StyledDocument document;

	static final SimpleAttributeSet boldAttributeSet = new SimpleAttributeSet();
	static final String[] imageUrls = { "./images/smile.png", "./images/love.png", "./images/sad.png",
			"./images/crying.png", "./images/angry.png" };
	static final ArrayList<Image> imageList = new ArrayList<Image>();
	static Image fileImage;

	private ArrayList<Style> styles;
	private JScrollPane scrollPane;

	static {
		StyleConstants.setBold(boldAttributeSet, true);
		for (int i = 0; i < imageUrls.length; i++) {
			ImageIcon icon = new ImageIcon(imageUrls[i]);
			imageList.add(icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
		}
		ImageIcon fileIcon = new ImageIcon("./images/file.png");
		fileImage = fileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
	}

	public ChatWindow(String username, String conversationName, BufferedWriter writer) {
		this.username = username;
		this.conversationName = conversationName;
		this.writer = writer;
		styles = new ArrayList<Style>();
	}

	private void sendMessage(String message, Boolean isEmoji) {
		try {
			synchronized (writer) {
				if (!isEmoji)
					writer.write("message");
				else
					writer.write("emoji");
				writer.newLine();
				writer.write(this.conversationName);
				writer.newLine();
				writer.write(message);
				writer.newLine();
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendFile(File file) {
		writer.write("fileUpload");
	}

	public void createAndShowGUI() {
		JFrame frame = new JFrame(conversationName);
		Container container = frame.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		JTextPane textPane = new JTextPane();
		document = textPane.getStyledDocument();
		textPane.setPreferredSize(new Dimension(300, 300));
		textPane.setEditable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textPane);
		this.scrollPane = scrollPane;
		JTextField textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(textField.getText(), false);
				addNewMessage(username, textField.getText(), false);
				textField.setText("");
			}
		});
		JPanel emojiPanel = new JPanel();
		for (int i = 0; i < imageList.size(); i++) {
			JButton btn = new JButton(new ImageIcon(imageList.get(i)));
			btn.setBorder(BorderFactory.createEmptyBorder());
			btn.setContentAreaFilled(false);
			String index = Integer.toString(i);
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sendMessage(index, true);
					addNewMessage(username, index, true);
				}
			});
			emojiPanel.add(btn);

			Style style = document.addStyle(Integer.toString(i), null);
			StyleConstants.setIcon(style, new ImageIcon(imageList.get(i)));
			styles.add(style);
		}

		JPanel chatPanel = new JPanel();
		textField.setPreferredSize(new Dimension(270, 30));
		JButton fileButton = new JButton(new ImageIcon(fileImage));
		fileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();

				}
			}
		});
		chatPanel.add(textField);
		chatPanel.add(fileButton);

		container.add(scrollPane);
		container.add(emojiPanel);
		container.add(chatPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public synchronized void addNewMessage(String sender, String content, Boolean isEmoji) {
		try {
			document.insertString(document.getLength(), sender + ": ", boldAttributeSet);
			if (!isEmoji) {
				document.insertString(document.getLength(), content + "\n", null);
				JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
				scrollBar.setValue(scrollBar.getMaximum());
			} else {
				int emojiIndex = Integer.parseInt(content);
				document.insertString(document.getLength(), "\n", null);
				document.insertString(document.getLength(), " ", styles.get(emojiIndex));
				document.insertString(document.getLength(), "\n", null);
				JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
				scrollBar.setValue(scrollBar.getMaximum() + 40);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
