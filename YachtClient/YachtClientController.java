/**
 * ユーザーのアクションとネットワーク通信を処理し、モデルとビューを仲介するコントローラ
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class YachtClientController extends WindowAdapter implements Runnable, ActionListener {

    private final GameModel model;
    private final YachtClientView view;

    private Socket socket;
    private Thread thread;
    private PrintWriter out;
    private BufferedReader in;

    // --- プロトコル定義 ---
    private static final String CMD_LOGIN = "LOGIN", CMD_LOGOUT = "LOGOUT", CMD_CREATE_LOBBY = "CREATE_LOBBY",
                               CMD_JOIN_LOBBY = "JOIN_LOBBY", CMD_READY = "READY", CMD_ROLL_DICE = "ROLL_DICE",
                               CMD_RECORD_SCORE = "RECORD_SCORE", CMD_LOGIN_SUCCESS = "LOGIN_SUCCESS",
                               CMD_LOBBY_LIST = "LOBBY_LIST", CMD_LOBBY_CREATED = "LOBBY_CREATED",
                               CMD_JOINED_LOBBY = "JOINED_LOBBY", CMD_PLAYER_JOINED = "PLAYER_JOINED",
                               CMD_PLAYER_LEFT = "PLAYER_LEFT", CMD_PLAYER_READY = "PLAYER_READY",
                               CMD_LEFT_LOBBY = "LEFT_LOBBY", CMD_GAME_STARTED = "GAME_STARTED",
                               CMD_YOUR_TURN = "YOUR_TURN", CMD_DICE_ROLLED = "DICE_ROLLED",
                               CMD_SCORE_RECORDED = "SCORE_RECORDED", CMD_GAME_ENDED = "GAME_ENDED",
                               CMD_ERROR = "ERROR",
                               CMD_BROADCAST_DICE_ROLL = "BROADCAST_DICE_ROLL",
                               CMD_IKASAMA_ROLL = "IKASAMA_ROLL",
                               CMD_FH_OR_STRAIGHT_ROLL = "FH_OR_STRAIGHT_ROLL";
    
    /**
     * コンストラクタ
     */
    public YachtClientController(GameModel model, YachtClientView view) {
        this.model = model;
        this.view = view;
    }

    // --- ネットワーク処理 ---

    private void connectServer() {
        String name = view.getNameText();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(view, "名前を入力してください", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            socket = new Socket(view.getHostText(), 20000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            thread = new Thread(this);
            thread.start();

            model.setPlayerName(name);
            sendMessage(CMD_LOGIN + ":" + name);

            view.setConnectedState();
            view.appendMessage("サーバーに接続しました。");

        } catch (IOException e) {
            view.appendMessage("サーバーに接続できませんでした: " + e.getMessage());
        }
    }

    private void closeServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                sendMessage(CMD_LOGOUT);
                socket.close();
            }
        } catch (IOException e) {
            view.appendMessage("切断エラー: " + e.getMessage());
        } finally {
            thread = null;
            model.resetForNewGame();
            view.setDisconnectedState();
            view.appendMessage("サーバーから離脱しました");
        }
    }
    
    private void sendMessage(String msg) {
        if (out != null && !out.checkError()) {
            out.println(msg);
        }
    }

    /**
     * サーバーからのメッセージを継続的に受信するためのメソッド
     */
    @Override
    public void run() {
        try {
            String line;
            while (thread != null && (line = in.readLine()) != null) {
                final String messageForLambda = line;
                SwingUtilities.invokeLater(() -> processServerMessage(messageForLambda));
            }
        } catch (IOException e) {
            if (thread != null) {
                SwingUtilities.invokeLater(() -> view.appendMessage("サーバーへの接続が失われました。"));
            }
        } finally {
            if (thread != null) {
                 SwingUtilities.invokeLater(this::closeServer);
            }
        }
    }

    // --- サーバーメッセージ処理 ---

    private void processServerMessage(String message) {
        String[] parts = message.split(":", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case CMD_LOGIN_SUCCESS: view.appendMessage("ログインしました: " + args); break;
            case CMD_LOBBY_LIST:    handleLobbyList(args); break;
            case CMD_LOBBY_CREATED:
            case CMD_JOINED_LOBBY:  handleLobbyJoined(args); break;
            case CMD_PLAYER_JOINED:
            case CMD_PLAYER_LEFT:
            case CMD_PLAYER_READY:  view.appendMessage("ロビー更新: " + args.replace(":", " ")); break;
            case CMD_LEFT_LOBBY:    handleLobbyLeft(); break;
            case CMD_GAME_STARTED:  handleGameStarted(args); break;
            case CMD_YOUR_TURN:     handleYourTurn(args); break;
            case CMD_BROADCAST_DICE_ROLL: handleBroadcastDiceRoll(args); break;
            case CMD_SCORE_RECORDED:handleScoreRecorded(args); break;
            case CMD_GAME_ENDED:    handleGameEnded(args); break;
            case CMD_ERROR:         handleError(args); break;
            default:
                view.appendMessage("サーバー(不明なコマンド): " + message);
                break;
        }
    }

    private void handleLobbyList(String args) {
        model.clearLobbyList();
        if (!args.equals("NONE")) {
            String[] lobbies = args.split(";");
            for (String lobbyInfo : lobbies) {
                String[] details = lobbyInfo.split(",");
                model.addLobbyToList(String.format("%s (%s/%s) - by %s", details[0], details[2], details[3], details[1]));
            }
        }
        view.updateLobbyList(model.getLobbyListModel());
    }

    private void handleLobbyJoined(String args) {
        String gameId = args.split(":")[0];
        model.setCurrentGameId(gameId);
        view.updateLobbyInfo("ロビーに参加中: " + gameId + "\nプレイヤー: " + args.split(":")[1]);
    }
    
    private void handleLobbyLeft() {
        model.setCurrentGameId(null);
        view.resetLobbyInfo();
    }
    
    private void handleGameStarted(String args) {
        // メッセージ形式: gameId:player1,player2,player3
        String[] parts = args.split(":", 2);
        String gameId = parts[0];
        String playerNamesString = parts[1];

        model.setCurrentGameId(gameId);
        
        // 受け取ったプレイヤー名のリストから、各プレイヤーのスコアモデルを初期化する
        String[] playerNames = playerNamesString.split(",");
        for (String name : playerNames) {
            model.getScoreModel(name); // これでGameModelに全プレイヤーの情報がセットされる
        }
        
        view.appendMessage("\n--- ゲーム " + gameId + " スタート! ---");
        view.showGameScreen();
    }

    private void handleYourTurn(String args) {
        boolean isMyTurn = args.equals(model.getPlayerName());
        view.updateTurnInfo(args, isMyTurn);
        view.updateAllScoreCards(model.getPlayerScores(), model.getPlayerName(), args);
        PlayerScoreModel myScoreModel = model.getScoreModel(model.getPlayerName());
        view.setIkasamaRollButtonEnabled(isMyTurn && !myScoreModel.hasUsedIkasama());
        view.setFhOrStraightButtonEnabled(isMyTurn && !myScoreModel.hasUsedFhOrStraight());
        if(isMyTurn) {
            view.resetDice();
        }
    }

    private void handleBroadcastDiceRoll(String args) {
        // メッセージ形式: keepPattern:diceValues:rollsLeft
        String[] parts = args.split(":", 3);
        String keepPattern = parts[0];
        String[] diceValues = parts[1].split(",");
        String rollsLeft = parts[2];
        boolean canRollAgain = !rollsLeft.equals("0");
        
        // 全員の画面で、アニメーションを開始し、結果をセットする
        view.startDiceAnimation(keepPattern);
        view.updateDice(diceValues, canRollAgain);
        view.updateRollsLeft(rollsLeft);
    }

    private void handleScoreRecorded(String args) {
        String[] scoreParts = args.split(":");
        String playerName = scoreParts[0];
        String category = scoreParts[1];
        int score = Integer.parseInt(scoreParts[2]);

        // 該当プレイヤーのスコアモデルを更新
        PlayerScoreModel scoreModel = model.getScoreModel(playerName);
        scoreModel.setScore(category, score);
        
        // メッセージを表示
        String categoryJpName = view.getCategoryEnToJpMap().getOrDefault(category, category);
        if (playerName.equals(model.getPlayerName())) {
            view.appendMessage(String.format("あなたは%sで%s点を獲得しました。", categoryJpName, score));
        } else {
            view.appendMessage(String.format("%sさんが%sで%s点を獲得しました。", playerName, categoryJpName, score));
        }
    }

    private void handleGameEnded(String args) {
        // argsの形式: player1,score1;player2,score2;...
        String[] playerScores = args.split(";");
        
        // 結果表示用のメッセージと、最高得点者を見つけるための準備
        StringBuilder resultMessage = new StringBuilder("== 結果発表 ==\n\n");
        String winnerName = "";
        int maxScore = -1;

        for(String ps : playerScores) {
            String[] parts = ps.split(",");
            String name = parts[0];
            int score = Integer.parseInt(parts[1]);
            
            resultMessage.append(name).append(" : ").append(score).append("点\n");

            if(score > maxScore) {
                maxScore = score;
                winnerName = name;
            }
        }
        
        resultMessage.append("\n勝者は ").append(winnerName).append(" さんです！\nおめでとうございます！");

        // ダイアログで結果を表示
        JOptionPane.showMessageDialog(view, resultMessage.toString(), "ゲーム終了！", JOptionPane.INFORMATION_MESSAGE);
        
        // モデルとビューをリセットして、ロビー画面に戻る
        model.resetForNewGame();
        view.resetScoreCard();
        view.showLobbyScreen();
        
        view.resetLobbyInfo(); 
    }

    private void handleError(String args) {
        JOptionPane.showMessageDialog(view, args, "サーバーエラー", JOptionPane.ERROR_MESSAGE);
        view.appendMessage("サーバーエラー: " + args);
    }
    
    // --- イベント処理 ---
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd == null || cmd.isEmpty()) return;

        if ("connect".equals(cmd)) {
            connectServer();
            return;
        }
        if ("disconnect".equals(cmd)) {
            closeServer();
            return;
        }

        if (socket == null || socket.isClosed()) {
            view.appendMessage("サーバーに接続されていません。");
            return;
        }
        
        switch (cmd) {
            case "create_lobby": sendMessage(CMD_CREATE_LOBBY + ":4"); break;
            case "join_lobby":
                String selected = view.getSelectedLobby();
                if (selected != null) {
                    sendMessage(CMD_JOIN_LOBBY + ":" + selected.split(" ")[0]);
                } else {
                    view.appendMessage("参加するロビーをリストから選択してください。");
                }
                break;
            case "ready":
                if(model.getCurrentGameId() != null) {
                    sendMessage(model.getCurrentGameId() + ":" + CMD_READY);
                }
                break;
            case "roll_dice":
                // サーバーにリクエストを送る前に、まずクライアント側でアニメーションを開始する 
                sendMessage(model.getCurrentGameId() + ":" + CMD_ROLL_DICE + ":" + view.getDiceKeepPattern());
                break;
            case "ikasama_roll":
                sendMessage(model.getCurrentGameId() + ":" + CMD_IKASAMA_ROLL + ":" + view.getDiceKeepPattern());
                break;
            case "fh_or_straight_roll":
                sendMessage(model.getCurrentGameId() + ":" + CMD_FH_OR_STRAIGHT_ROLL + ":" + view.getDiceKeepPattern());
                break;
            default:
                // 当てはまらない場合はスコア記録ボタン
                sendMessage(model.getCurrentGameId() + ":" + CMD_RECORD_SCORE + ":" + cmd);
                break;
        }
    }
    
    @Override
    public void windowClosing(WindowEvent e) {
        if (socket != null && !socket.isClosed()) {
            closeServer();
        }
    }
}