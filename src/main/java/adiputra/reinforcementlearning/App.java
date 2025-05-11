package adiputra.reinforcementlearning;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import adiputra.reinforcementlearning.chapter5.*;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Reinforcement Learning");
        frame.setSize(300, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("<html>Reinforcement Learning: An Introduction<br>Sutton and Barto<br>Implemented by Adi Putra</html>");
        titleLabel.setBounds(20, 0, 250, 60);
        frame.add(titleLabel);
        
        JLabel chapterLabel = new JLabel("Select a chapter");
        chapterLabel.setBounds(50, 60, 200, 20);
        frame.add(chapterLabel);
        
        String[] chapters = new String[17];
        for (int i = 0; i < 17; i++) {
            chapters[i] = "Chapter " + (i + 1);
        }
        JComboBox<String> chapterComboBox = new JComboBox<>(chapters);
        chapterComboBox.setBounds(50, 80, 200, 30);
        frame.add(chapterComboBox);
        chapterComboBox.setSelectedIndex(-1);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(50, 120, 300, 300);
        buttonPanel.setLayout(null);
        frame.add(buttonPanel);
        
        chapterComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int chapter = chapterComboBox.getSelectedIndex();
				buttonPanel.removeAll();
				switch (chapter) {
					case 4:
                        JButton chapter5Button1 = new JButton("Example 5.1 (Blackjack)");
                        chapter5Button1.setBounds(0, 0, 200, 30);
                        buttonPanel.add(chapter5Button1);
                        
                        chapter5Button1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Example5_1.main(new String[0]);
							}
						});
						break;
					default:
						break;
				}
			}
		});

        frame.setVisible(true);
    }
}

