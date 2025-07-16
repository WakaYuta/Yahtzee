import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.JPanel;


public class YachtClientView extends JFrame {

    private DiceSeqComponent[] dices = new DiceSeqComponent[5];
    private DiceManager diceManager;

    private static final String APPNAME = "ヤッツィークライアント";

    private JTextField hostTextField, nameTextField;
    private JButton connectButton, closeButton, createLobbyButton, joinLobbyButton, readyButton, rollDiceButton;
    private JList<String> lobbyList;
    private JTextArea lobbyInfoArea, messageArea;
    private JLabel turnInfoLabel, rollsLeftLabel;
    private JPanel lobbyPanel, gamePanel, scoreCardPanel;
    private Map<String, String> categoryEnToJpMap = new HashMap<>();

    public YachtClientView() {
        super(APPNAME);
        // 英語と日本語の役名の対応表
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
        for (int i = 0; i < categories.length; i++) {
            categoryEnToJpMap.put(categories[i], categoryJP[i]);
        }
        
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
        
        this.addWindowListener(controller);
    }

    private void setupUI() {
        JPanel topPanel = createTopPanel();
        lobbyPanel = createLobbyPanel();
        gamePanel = createGamePanel(); // 新しいレイアウトのゲームパネル

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(topPanel, BorderLayout.NORTH);
        
        // 初めはロビーパネルを表示し、ゲームが始まったらゲームパネルに切り替える
        // ここでは両方追加しておくが、最初はゲームパネルを非表示にする
        contentPane.add(lobbyPanel, BorderLayout.CENTER);
        contentPane.add(gamePanel, BorderLayout.EAST); // 最初はEASTに仮置き

        setInitialComponentState();
        
        gamePanel.setVisible(false); // 最初はゲームパネルを隠しておく
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
        // ゲーム全体のパネル
        JPanel mainGamePanel = new JPanel(new BorderLayout(5, 5));

        // --- 上部エリア（サイコロとボタン） ---
        JPanel topArea = new JPanel(new BorderLayout());
        JPanel dicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        final String IMAGE_PREFIX = "images/dice_";
        for (int i = 0; i < 5; i++) {
            dices[i] = new DiceSeqComponent(1000 + i * 100, IMAGE_PREFIX);
            dicePanel.add(dices[i]);
        }
        diceManager = new DiceManager(dices);
        
        rollDiceButton = new JButton("サイコロを振る");
        
        // ターン情報とロール回数も上部に表示
        JPanel turnInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        turnInfoLabel = new JLabel("ターン: ---");
        rollsLeftLabel = new JLabel("残りロール回数: -");
        turnInfoPanel.add(turnInfoLabel);
        turnInfoPanel.add(rollsLeftLabel);

        topArea.add(dicePanel, BorderLayout.CENTER);
        topArea.add(rollDiceButton, BorderLayout.EAST);
        topArea.add(turnInfoPanel, BorderLayout.SOUTH);

        // --- 中央エリア（ゲームメッセージ） ---
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        // メッセージエリアが大きくなりすぎないようにサイズを設定
        messageScrollPane.setPreferredSize(new Dimension(400, 300));

        // --- 下部エリア（スコアカード） ---
        scoreCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JScrollPane scoreScrollPane = new JScrollPane(scoreCardPanel);

        // 各エリアをメインパネルに配置
        mainGamePanel.add(topArea, BorderLayout.NORTH);
        mainGamePanel.add(messageScrollPane, BorderLayout.CENTER);
        mainGamePanel.add(scoreScrollPane, BorderLayout.SOUTH);

        return mainGamePanel;
    }

    /**
     * モデルのデータに基づいて、全プレイヤーのスコアカードUIを再構築する
     * @param allPlayerScores 全プレイヤーのスコアデータ
     * @param myName このクライアントのプレイヤー名
     * @param currentPlayerName 現在ターンのプレイヤー名
     */
    public void updateAllScoreCards(Map<String, PlayerScoreModel> allPlayerScores, String myName, String currentPlayerName) {
        scoreCardPanel.removeAll(); //既存のスコアカードをすべて消去する

        String[] categories = {
            "ONES", "TWOS", "THREES", "FOURS", "FIVES", "SIXES",
            "CHOICE", "FOUR_OF_A_KIND", "FULL_HOUSE",
            "SMALL_STRAIGHT", "LARGE_STRAIGHT", "YACHT"
        };
        
        // 全プレイヤーのスコアモデルをループして、スコアカードを1つずつ作成
        for (PlayerScoreModel playerScore : allPlayerScores.values()) {
            JPanel singleCard = new JPanel();
            singleCard.setBorder(BorderFactory.createTitledBorder(playerScore.getPlayerName()));
            singleCard.setLayout(new GridLayout(categories.length + 2, 3, 2, 2)); // Total Scoreと空白行を追加

            // カテゴリごとのラベルとボタンを作成
            for (String category : categories) {
                singleCard.add(new JLabel(categoryEnToJpMap.get(category)));
                singleCard.add(new JLabel(String.valueOf(playerScore.getScore(category))));

                JButton recordButton = new JButton("記録");
                recordButton.setActionCommand(category);
                
                boolean isMyTurn = playerScore.getPlayerName().equals(myName) && myName.equals(currentPlayerName);
                boolean isAlreadyRecorded = playerScore.isRecorded(category);
                recordButton.setEnabled(isMyTurn && !isAlreadyRecorded);
                
                if(rollDiceButton.getActionListeners().length > 0) {
                   recordButton.addActionListener(rollDiceButton.getActionListeners()[0]);
                }
                
                singleCard.add(recordButton);
            }
            
            singleCard.add(new JLabel(" ")); // 見た目のための空白行
            singleCard.add(new JLabel(" "));
            singleCard.add(new JLabel(" "));
            
            singleCard.add(new JLabel("合計点"));
            singleCard.add(new JLabel(String.valueOf(playerScore.getTotalScore())));
            singleCard.add(new JLabel()); // 空白のセル

            scoreCardPanel.add(singleCard);
        }

        scoreCardPanel.revalidate();
        scoreCardPanel.repaint();
    }
    
    public void resetScoreCard() {
         scoreCardPanel.removeAll();
         scoreCardPanel.revalidate();
         scoreCardPanel.repaint();
    }
    
    public void setInitialComponentState() {
        connectButton.setEnabled(true);
        closeButton.setEnabled(false);
        lobbyList.setEnabled(false);
        createLobbyButton.setEnabled(false);
        joinLobbyButton.setEnabled(false);
        readyButton.setEnabled(false);
        rollDiceButton.setEnabled(false);
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
        gamePanel.setVisible(false);
        lobbyPanel.setVisible(true);
        // lobbyPanelをCENTERに配置しなおす
        getContentPane().add(lobbyPanel, BorderLayout.CENTER);
    }
    
    public void showGameScreen() {
         lobbyPanel.setVisible(false);
        // gamePanelをCENTERに配置しなおす
        getContentPane().add(gamePanel, BorderLayout.CENTER);
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
    
    public void updateDice(String[] diceValues, boolean canRollAgain) {
        int[] intValues = new int[diceValues.length];
        try {
            for (int i = 0; i < diceValues.length; i++) {
                intValues[i] = Integer.parseInt(diceValues[i]);
            }
            diceManager.scheduleStopAnimation(intValues);
            
            rollDiceButton.setEnabled(diceManager.getMyTurn() && canRollAgain);
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
    
    public void resetDice() {
        diceManager.resetDices();
    }

    public void appendMessage(String text) {
        messageArea.append(text + "\n");
    }

    public String getHostText() { return hostTextField.getText(); }
    public String getNameText() { return nameTextField.getText(); }
    public String getSelectedLobby() { return lobbyList.getSelectedValue(); }
    public Map<String, String> getCategoryEnToJpMap() { return categoryEnToJpMap; }
    
    public void startDiceAnimation(String keepPattern) {
        diceManager.startRollAnimation(keepPattern);
    }
    
    public String getDiceKeepPattern() {
        return diceManager.getKeepPattern();
    }
}