package adiputra.reinforcementlearning;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.LinkedHashMap;

import adiputra.reinforcementlearning.chapter5.models.*;
import adiputra.reinforcementlearning.chapter5.views.*;
import adiputra.reinforcementlearning.chapter5.controllers.*;

public class App {
    private static final String MAIN_MENU_CARD = "MAIN_MENU";
    private static final String EXAMPLE_5_1_CARD = "EXAMPLE_5_1_VIEW";
    private static final String EXAMPLE_5_3_CARD = "EXAMPLE_5_3_VIEW";

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final JFrame frame;

    public App() {
        frame = new JFrame("Reinforcement Learning");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel mainMenuPanel = createMainMenuPanel();
        mainPanel.add(mainMenuPanel, MAIN_MENU_CARD);

        Example5_1Model example5_1Model = new Example5_1Model();
        Example5_1View example5_1View = new Example5_1View();
        new Example5_1Controller(example5_1Model, example5_1View);
        
        Example5_3Model example5_3Model = new Example5_3Model();
        Example5_3View example5_3View = new Example5_3View();
        new Example5_3Controller(example5_3Model, example5_3View);
        
        ActionListener backAction = e -> cardLayout.show(mainPanel, MAIN_MENU_CARD);
        example5_1View.addBackListener(backAction);
        example5_3View.addBackListener(backAction);

        mainPanel.add(example5_1View, EXAMPLE_5_1_CARD);
        mainPanel.add(example5_3View, EXAMPLE_5_3_CARD);

        frame.add(mainPanel);
        frame.setSize(450, 550);
        frame.setResizable(false); 
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createMainMenuPanel() {
        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final JPanel topPanel = new JPanel(new BorderLayout());
        final JLabel titleLabel = new JLabel("<html>Reinforcement Learning: An Introduction<br>Sutton and Barto<br>Implemented by Adi Putra</html>");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        final Map<String, JPanel> chapterViews = new LinkedHashMap<>();
        chapterViews.put("Chapter 5", createChapter5Buttons());
        chapterViews.put("Test", createChapter1Buttons());
        
        final JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectionPanel.add(new JLabel("Select a chapter: "));
        final String[] chapterNames = chapterViews.keySet().toArray(new String[0]);
        final JComboBox<String> chapterComboBox = new JComboBox<>(chapterNames);
        selectionPanel.add(chapterComboBox);
        topPanel.add(selectionPanel, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);
        
        final CardLayout buttonCardLayout = new CardLayout();
        final JPanel dynamicButtonPanel = new JPanel(buttonCardLayout);

        for (Map.Entry<String, JPanel> entry : chapterViews.entrySet()) {
            dynamicButtonPanel.add(entry.getValue(), entry.getKey());
        }

        chapterComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedChapter = (String) e.getItem();
                buttonCardLayout.show(dynamicButtonPanel, selectedChapter);
            }
        });

        final JScrollPane scrollPane = new JScrollPane(dynamicButtonPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createChapter5Buttons() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = createGbc();

        final JButton chapter5Button1 = new JButton("Example 5.1");
        chapter5Button1.addActionListener(event -> {
            cardLayout.show(mainPanel, EXAMPLE_5_1_CARD);
        });
        buttonPanel.add(chapter5Button1, gbc);
        
        //final JButton chapter5Button2 = new JButton("Example 5.3");
        //chapter5Button2.addActionListener(event -> {
        //    cardLayout.show(mainPanel, EXAMPLE_5_3_CARD);
        //});
        //buttonPanel.add(chapter5Button2, gbc);

        gbc.weighty = 1.0;
        buttonPanel.add(new JPanel(), gbc);
        return buttonPanel;
    }

    private JPanel createChapter1Buttons() {
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = createGbc();

        gbc.gridy = 0;
        buttonPanel.add(new JButton("Test 1"), gbc);
        
        gbc.gridy = 1;
        buttonPanel.add(new JButton("Test 2"), gbc);
        
        gbc.gridy = 2;
        buttonPanel.add(new JButton("Test 3"), gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        buttonPanel.add(new JPanel(), gbc);
        return buttonPanel;
    }
    
    private GridBagConstraints createGbc() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.weightx = 1.0;
        return gbc;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize LaF. Using default.");
        }
        SwingUtilities.invokeLater(App::new);
    }
}