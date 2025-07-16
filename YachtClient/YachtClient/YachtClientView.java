import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

public class YachtClientView extends JFrame {

    private DiceSeqComponent[] dices = new DiceSeqComponent[5];
    private DiceManager diceManager;

    private static final String APPNAME = "ヤッツィークライアント";

    private JTextField hostTextField, nameTextField;
    private JButton connectButton, closeButton, createLobbyButton, joinLobbyButton, readyButton, rollDiceButton;
    private JList<String> lobbyList;
    private JTextArea lobbyInfoArea, messageArea;
    private JLabel turnInfoLabel, rollsLeftLabel;
    private Map<String, JLabel> scoreLabels = new HashMap<>();
    private Map<String, JButton> scoreButtons = new HashMap<>();
    private JPanel lobbyPanel, gamePanel, scoreCardPanel;
    
    private Map<String, String> categoryEnToJpMap = new HashMap<>();

    public YachtClientView() {
        super(APPNAME);
        setupUI();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void setController(YachtClientController controller) {
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(controller);
        
        closeButton.setActionCommand("disconnect");
        closeButton.addActionListener(controller);

        createLobbyButton.setActionCommand("create_lobby");
        createLobbyButton.addActionListener(controller);
        
        joinLobbyButton.setActionCommand("join_lobby");
        joinLobbyButton.addActionListener(controller);
        
        readyButton.setActionCommand("ready");
        readyButton.addActionListener(controller);
        
        rollDiceButton.setActionCommand("roll_dice");
        rollDiceButton.addActionListener(controller);
        
        for (JButton button : scoreButtons.values()) {
            button.addActionListener(controller);
        }
        this.addWindowListener(controller);
    }

    private void setupUI() {
        JPanel topPanel = createTopPanel();
        lobbyPanel = createLobbyPanel();
        scoreCardPanel = createScoreCardPanel();
        gamePanel = createGamePanel();

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(lobbyPanel, BorderLayout.WEST);
        contentPane.add(gamePanel, BorderLayout.CENTER);
        contentPane.add(scoreCardPanel, BorderLayout.EAST);

        setInitialComponentState();
        
        gamePanel.setVisible(false);
        scoreCardPanel.setVisible(false);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("サーバーIP:"));
        hostTextField = new JTextField("127.0.0.1", 10);
        panel.add(hostTextField);
        panel.add(new JLabel("名前:"));
        nameTextField = new JTextField(10);
        panel.add(nameTextField);
        connectButton = new JButton("接続");
        panel.add(connectButton);
        closeButton = new JButton("切断");
        panel.add(closeButton);
        return panel;
    }

    private JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("ロビー"));
        
        lobbyList = new JList<>();
        panel.add(new JScrollPane(lobbyList), BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        createLobbyButton = new JButton("ロビー作成");
        joinLobbyButton = new JButton("ロビーに参加");
        buttonsPanel.add(createLobbyButton);
        buttonsPanel.add(joinLobbyButton);
        panel.add(buttonsPanel, BorderLayout.NORTH);
        
        lobbyInfoArea = new JTextArea("ロビーに参加していません", 4, 20);
        lobbyInfoArea.setEditable(false);
        lobbyInfoArea.setLineWrap(true);
        panel.add(new JScrollPane(lobbyInfoArea), BorderLayout.SOUTH);
        
        readyButton = new JButton("準備完了");
        JPanel readyPanel = new JPanel();
        readyPanel.add(readyButton);
        panel.add(readyPanel, BorderLayout.EAST);
        
        return panel;
    }
  
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("ゲーム"));
        
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel dicePanel = new JPanel(new FlowLayout());
        
        final String IMAGE_PREFIX = "images/dice_";

        for (int i = 0; i < 5; i++) {
            dices[i] = new DiceSeqComponent(1000 + i * 100, IMAGE_PREFIX);
            dicePanel.add(dices[i]);
        }
        diceManager = new DiceManager(dices);
        
        controlPanel.add(dicePanel, BorderLayout.CENTER);
        
        rollDiceButton = new JButton("サイコロを振る");
        controlPanel.add(rollDiceButton, BorderLayout.EAST);
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        turnInfoLabel = new JLabel("ターン: ---");
        rollsLeftLabel = new JLabel("残りロール回数: -");
        infoPanel.add(turnInfoLabel);
        infoPanel.add(rollsLeftLabel);
        controlPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createScoreCardPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("スコアカード"));
        
        String[] categories = {
            "ONES", "TWOS", "THREES", "FOURS", "FIVES", "SIXES",
            "CHOICE", "FOUR_OF_A_KIND", "FULL_HOUSE",
            "SMALL_STRAIGHT", "LARGE_STRAIGHT", "YACHT"
        };
        String[] categoryJP = {
            "1の目", "2の目", "3の目", "4の目", "5の目", "6の目",
            "チョイス", "フォーカード", "フルハウス",
            "Sストレート", "Lストレート", "ヤッツィー"
        };
        
        panel.setLayout(new GridLayout(categories.length, 3, 2, 2));
        for (int i = 0; i < categories.length; i++) {
            categoryEnToJpMap.put(categories[i], categoryJP[i]);
            
            panel.add(new JLabel(categoryJP[i]));
            JLabel scoreLabel = new JLabel("0");
            scoreLabels.put(categories[i], scoreLabel);
            panel.add(scoreLabel);
            JButton scoreButton = new JButton("記録");
            scoreButton.setActionCommand(categories[i]);
            scoreButtons.put(categories[i], scoreButton);
            panel.add(scoreButton);
        }
        return panel;
    }

    public void setInitialComponentState() {
        connectButton.setEnabled(true);
        closeButton.setEnabled(false);
        lobbyList.setEnabled(false);
        createLobbyButton.setEnabled(false);
        joinLobbyButton.setEnabled(false);
        readyButton.setEnabled(false);
        rollDiceButton.setEnabled(false);
        for (JButton button : scoreButtons.values()) {
            button.setEnabled(false);
        }
    }

    public void setConnectedState() {
        connectButton.setEnabled(false);
        closeButton.setEnabled(true);
        nameTextField.setEditable(false);
        hostTextField.setEditable(false);
        lobbyList.setEnabled(true);
        createLobbyButton.setEnabled(true);
        joinLobbyButton.setEnabled(true);
    }
    
    public void setDisconnectedState() {
        setInitialComponentState();
        nameTextField.setEditable(true);
        hostTextField.setEditable(true);
        lobbyInfoArea.setText("ロビーに参加していません");
        showLobbyScreen();
    }

    public void showLobbyScreen() {
        lobbyPanel.setVisible(true);
        gamePanel.setVisible(false);
        scoreCardPanel.setVisible(false);
    }
    
    public void showGameScreen() {
        lobbyPanel.setVisible(false);
        gamePanel.setVisible(true);
        scoreCardPanel.setVisible(true);
    }
    
    public void updateLobbyList(DefaultListModel<String> model) {
        lobbyList.setModel(model);
    }
    
    public void updateLobbyInfo(String text) {
        lobbyInfoArea.setText(text);
        createLobbyButton.setEnabled(false);
        joinLobbyButton.setEnabled(false);
        readyButton.setEnabled(true);
    }
    
    public void resetLobbyInfo() {
        lobbyInfoArea.setText("ロビーに参加していません");
        createLobbyButton.setEnabled(true);
        joinLobbyButton.setEnabled(true);
        readyButton.setEnabled(false);
    }

    public void updateTurnInfo(String text, boolean isMyTurn) {
        turnInfoLabel.setText("ターン: " + text);
        updateRollsLeft("3");
        rollDiceButton.setEnabled(isMyTurn);
        diceManager.setMyTurn(isMyTurn);
    }
    
    public void updateScoreButtons(boolean isMyTurn, Set<String> recordedCategories) {
        for (Map.Entry<String, JButton> entry : scoreButtons.entrySet()) {
            boolean isRecorded = recordedCategories.contains(entry.getKey());
            entry.getValue().setEnabled(isMyTurn && !isRecorded);
        }
    }

    public void updateDice(String[] diceValues, boolean canRollAgain) {
        int[] intValues = new int[diceValues.length];
        try {
            for (int i = 0; i < diceValues.length; i++) {
                intValues[i] = Integer.parseInt(diceValues[i]);
            }
            diceManager.scheduleStopAnimation(intValues);
            
            diceManager.setMyTurn(canRollAgain);
            rollDiceButton.setEnabled(canRollAgain);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse dice values: " + e.getMessage());
        }
    }
    
    public void updateRollsLeft(String count) {
        rollsLeftLabel.setText("残りロール回数: " + count);
        try {
            diceManager.setRollsLeft(Integer.parseInt(count));
        } catch (NumberFormatException e) {
            diceManager.setRollsLeft(0);
        }
    }

    public void updateScoreCard(String category, String score) {
        scoreLabels.get(category).setText(score);
        scoreButtons.get(category).setEnabled(false);
    }
    
    public void resetDice() {
        diceManager.resetDices();
    }
    
    public void resetScoreCard() {
         for(String category : scoreLabels.keySet()){
            scoreLabels.get(category).setText("0");
            scoreButtons.get(category).setEnabled(false);
        }
    }

    public void appendMessage(String text) {
        messageArea.append(text + "\n");
    }

    public String getHostText() { return hostTextField.getText(); }
    public String getNameText() { return nameTextField.getText(); }
    public String getSelectedLobby() { return lobbyList.getSelectedValue(); }
    public Map<String, String> getCategoryEnToJpMap() { return categoryEnToJpMap; }
    
    public void startDiceAnimation() {
        diceManager.startRollAnimation();
    }
    
    public String getDiceKeepPattern() {
        return diceManager.getKeepPattern();
    }
}