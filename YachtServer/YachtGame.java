import java.net.*;
import java.util.*;
import java.io.*;

/**
 * クラス YachtGameManager の注釈をここに書きます.
 * 
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
public class YachtGame {
    private String gameId; // ゲームを一意に識別するID
    private ArrayList<YachtClientUser> playerList; // このゲームに参加しているプレイヤー
    private Map<YachtClientUser, PlayerScoreSheet> playerScores;
    private int currentPlayerIndex; // 現在のターンのプレイヤーインデックス
    private int currentRound; // current round num
    private int[] diceRolls; // サイコロの出目（例: 5個のサイコロ）
    private int remainRollCount; // 現在のターンのサイコロを振った回数

    // ScoreCalculater のインスタンス
    private ScoreCalculater scoreCalculater;

    public YachtGame(String gameId, ArrayList<YachtClientUser> playerList) {
        this.gameId = gameId;
        this.playerList = new ArrayList<>(playerList);

        this.playerScores = new HashMap<>();

        this.currentPlayerIndex = 0; // 最初のプレイヤーから開始
        this.currentRound = 0; // current round
        this.diceRolls = new int[5]; // 5個のサイコロ
        this.remainRollCount = 3;

        this.scoreCalculater = new ScoreCalculater(); // ScoreCalculaterのインスタンスを生成
        
        // 既存のplayerListに基づいてプレイヤーを初期化
        for (YachtClientUser player : playerList) {
            addPlayer(player);
        }
    }

    /**
     * 新しいプレイヤーをゲームに追加し、PlayerScoreSheet を初期化します。
     * @param player 追加するプレイヤー
     */
    public void addPlayer(YachtClientUser player) {
        this.playerScores.put(player, new PlayerScoreSheet());
    }
    
    public List<YachtClientUser> getPlayerList(){
        return this.playerList;
    }
    
    public int getRemainRollCount(){
        return this.remainRollCount;
    }
    
    public int[] getDices(){
        return this.diceRolls;
    }
    // --- ゲーム進行に関するメソッド ---

    public YachtClientUser getCurrentPlayer() {
        if (playerList.isEmpty()) {
            return null;
        }
        return playerList.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if(currentPlayerIndex + 1 >= playerList.size()){
            // 次のラウンドへ
            // 最終ラウンドが終了したらENDGAME()へ
            // カテゴリ数 == ラウンド数
            if (currentRound == yachtCategory.values().length){
                // 最終ラウンドが終了
                System.out.println("ゲーム終了！"); // 仮
            } else {
                currentPlayerIndex = 0; // 最初のプレイヤーに戻る
            }
            currentRound++;
        } else {
            currentPlayerIndex = currentPlayerIndex + 1;
        }
        this.remainRollCount = 3; // 次のターンではサイコロを振る回数をリセット
        this.diceRolls = new int[5]; // サイコロの出目もリセット
    }

    /**
     * rollDice
     *
     * @params String サイコロを振るかどうかをLとRで表した文字列
     * @returns String サイコロの出目
     */
    public int[] rollDice(String LorR) {
        // 残り振る回数が0以下の場合のチェックを修正
        if (remainRollCount <= 0) { // 修正: >= 0 から <= 0 へ
            System.out.println("もうサイコロは振れません。");
            return diceRolls; // 現在の出目を返す
        }

        MagicRollController mrcontroller = new MagicRollController();
        
        diceRolls = mrcontroller.rolldices(diceRolls,LorR);

        remainRollCount--;
        System.out.println("サイコロを振りました: " + Arrays.toString(diceRolls) + " (残り " + (remainRollCount) + "回)");
        return diceRolls;
    }

    /**
     * 整数配列をカンマ区切りの文字列に変換します。
     * 例: {1, 5, 2, 6, 3} -> "1,5,2,6,3"
     *
     * @param diceRolls 変換する整数配列
     * @return 整数配列をカンマ区切りで表現した文字列
     */
    public String convertDiceRollsToText(int[] diceRolls) {
        if (diceRolls == null || diceRolls.length == 0) {
            return "";
        }
        String result = "";
        
        for(int i = 0; i < diceRolls.length; i++) {
            result += String.valueOf(diceRolls[i]);
            if (i < diceRolls.length - 1) {
                result += ",";
            }
        }
        return result;
    }

    /**
     * 現在のプレイヤーの、指定された役のスコアを計算し、スコアシートに記入します。
     *
     * @param category スコアを記入するYachtCategory
     * @return スコアが正常に記入された場合はtrue、既に記入済みで設定できなかった場合はfalse
     */
    public boolean recordScore(yachtCategory category) {
        YachtClientUser currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) {
            System.err.println("Error: No current player.");
            return false;
        }

        PlayerScoreSheet scoreSheet = playerScores.get(currentPlayer);
        if (scoreSheet == null) {
            System.err.println("Error: Score sheet not found for current player.");
            return false;
        }

        // スコアが既に記入済みでないかを確認
        if (scoreSheet.isRecorded(category)) {
            System.out.println(currentPlayer.getName() + ": Score for " + category + " is already recorded.");
            return false;
        }

        // ScoreCalculater を使ってスコアを計算
        int calculatedScore = scoreCalculater.calcScore(category, this.diceRolls);

        // PlayerScoreSheet を介してスコアを設定
        boolean success = scoreSheet.setScore(category, calculatedScore);
        if (success) {
            System.out.println(currentPlayer.getName() + " recorded " + calculatedScore + " points for " + category + ".");
        }
        return success;
    }

    /**
     * 特定のプレイヤーの合計スコアを取得します。
     * @param player スコアを取得するプレイヤー
     * @return プレイヤーの合計スコア
     */
    public int getPlayerTotalScore(YachtClientUser player) {
        PlayerScoreSheet scoreSheet = playerScores.get(player);
        if (scoreSheet != null) {
            return scoreSheet.getTotalScore();
        }
        return 0; // プレイヤーが見つからない場合
    }

    /**
     * 特定のプレイヤーの指定されたカテゴリのスコアを取得します。
     * @param player スコアを取得するプレイヤー
     * @param category スコアを取得するカテゴリ
     * @return 該当カテゴリのスコア
     */
    public int getPlayerCategoryScore(YachtClientUser player, yachtCategory category) {
        PlayerScoreSheet scoreSheet = playerScores.get(player);
        if (scoreSheet != null) {
            return scoreSheet.getScore(category);
        }
        return 0; // プレイヤーまたはカテゴリが見つからない場合
    }
    
    //ユーザの切断時の処理
    /**
     * ゲームからプレイヤーを削除します。
     * @param player 削除するプレイヤー
     * @return ゲームが継続可能であるか (2人以上残っているか)
     */
    public boolean removePlayer(YachtClientUser player) {
        System.out.println("Attempting to remove player: " + player.getName() + " from game " + gameId);
        int removedIdx = playerList.indexOf(player);

        playerList.remove(player);
        playerScores.remove(player);
        System.out.println(player.getName() + " removed. Current players: " + playerList.size());
        
        if (removedIdx != -1) {
            // プレイヤーリストから削除された場合の currentPlayerIndex の調整
            if (playerList.size() <= 1) {
                //ユーザ数が1以下になったらゲーム強制終了になるがインデックスを超えると面倒なので更新
                currentPlayerIndex = -1;
            } else if (removedIdx <= currentPlayerIndex) {
                // 削除されたプレイヤーが現在のプレイヤーか、それより前のプレイヤーだった場合
                // インデックスがずれる可能性があるので調整
                currentPlayerIndex = currentPlayerIndex % playerList.size(); 
                // もし現在のプレイヤーが削除された場合、次のプレイヤーにターンを渡す必要がある。
                // それはYachtServer側のleaveGameで処理する。
            }
        } else {
            System.out.println(player.getName() + " was not found in game " + gameId + " player list.");
        }
        
        //リストを更新した上で、継続可能かを返す。
        return isGameContinuable();
    }

    /**
     * ゲームが継続可能かどうかを判定します。
     * プレイヤーが2人以上残っている場合に継続可能とします。
     * @return ゲームが継続可能であれば true、そうでなければ false
     */
    public boolean isGameContinuable() {
        return playerList.size() >= 2;
    }
    
    /**
     * 全てのプレイヤーが全てのカテゴリを埋めたらゲーム終了と判断する。
     * @return ゲームが終了していればtrue
     */
    public boolean isGameOver() {
        if (isGameContinuable()) return true; // プレイヤー一人だけになったら終了

        for (PlayerScoreSheet scoreSheet : playerScores.values()) {
            for (yachtCategory category : yachtCategory.values()) {
                if (!scoreSheet.isRecorded(category)) {
                    return false; // 未記入のカテゴリがあればゲームは終わっていない
                }
            }
        }
        return true; // 全員が全て記入済みならゲーム終了
    }

    /**
     * このゲームインスタンスに対するコマンドを処理する。
     * @param sender コマンドを送信したプレイヤー
     * @param gameCommand ゲーム固有のコマンド文字列 (例: "ROLL_DICE")
     * @param arguments コマンドの引数
     * @return 処理の結果 (成功/失敗、必要ならデータ)
     */
    public GameCommandResult handleGameCommand(YachtClientUser sender, String gameCommand, String arguments) {
        // 現在のプレイヤーが有効なプレイヤーリスト内にいるかを確認
        if (playerList.isEmpty() || !playerList.contains(sender)) {
            return new GameCommandResult(false, "ERROR:PLAYER_NOT_IN_GAME");
        }

        // このゲームの現在のターンプレイヤーであるかチェック (重要!)
        if (!getCurrentPlayer().equals(sender)) {
            return new GameCommandResult(false, "ERROR:NOT_YOUR_TURN");
        }

        switch (gameCommand) {
            case "ROLL_DICE":
                if (getRemainRollCount() <= 0) {
                     return new GameCommandResult(false, "ERROR:NO_ROLLS_LEFT");
                }
                int[] newDice = rollDice(arguments);
                return new GameCommandResult(true, "DICE_ROLLED:" + convertDiceRollsToText(newDice) + ":REMAIN_ROLLS:" + getRemainRollCount());
            
            case "RECORD_SCORE":
                try {
                    yachtCategory category = yachtCategory.valueOf(arguments);
                    if (getPlayerScoreSheet(sender) == null || getPlayerScoreSheet(sender).isRecorded(category)) {
                        return new GameCommandResult(false, "ERROR:CATEGORY_ALREADY_RECORDED_OR_INVALID_PLAYER");
                    }
                    
                    boolean success = recordScore(category); // スコアを記録
                    
                    if (success) {
                        this.nextTurn(); // 次のターンへ移行
                        String nextPlayerName = getCurrentPlayer().getName(); // 次のプレイヤー名を取得
    
                        List<GameCommandResult.MessageToSend> messages = new ArrayList<>();
                        // 1. スコア記録成功のメッセージ (送信者のみに)
                        messages.add(new GameCommandResult.MessageToSend(
                            "SCORE_RECORDED:" + category.name() + ":" + getPlayerCategoryScore(sender, category) + ":TOTAL_SCORE:" + getPlayerTotalScore(sender),
                            false // senderにのみ送信
                        ));
                        // 2. 次のターン通知のメッセージ (全員に)
                        messages.add(new GameCommandResult.MessageToSend(
                            "YOUR_TURN:" + nextPlayerName,
                            true // 全員に送信
                        ));
    
                        return GameCommandResult.createMultiMessageResult(true, messages); // 複数のメッセージを返す
                    } else {
                        return new GameCommandResult(false, "ERROR:COULD_NOT_RECORD_SCORE");
                    }
                } catch (IllegalArgumentException e) {
                    return new GameCommandResult(false, "ERROR:INVALID_CATEGORY:" + arguments);
                }

            case "GET_DICE": // 現在のサイコロの目を取得
                return new GameCommandResult(true, "CURRENT_DICE:" + convertDiceRollsToText(getDices()));

            case "GET_SCORE_SHEET": // 自身のスコアシートを取得
                PlayerScoreSheet sheet = getPlayerScoreSheet(sender);
                if (sheet != null) {
                    return new GameCommandResult(true, "SCORE_SHEET_UPDATE:" + sender.getName() + ":" + sheet.toString());
                } else {
                    return new GameCommandResult(false, "ERROR:SCORE_SHEET_NOT_FOUND");
                }

            // 未定義関数の場合
            default:
                return new GameCommandResult(false, "ERROR:UNKNOWN_GAME_COMMAND:" + gameCommand);
        }
    }
    
    public PlayerScoreSheet getPlayerScoreSheet(YachtClientUser player) {
        return playerScores.get(player);
    }

    /**
     * 現在のプレイヤーのスコアシートを表示。（デバッグ用）
     */
    public void displayCurrentPlayerScoreSheet() {
        YachtClientUser currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            System.out.println("\n--- " + currentPlayer.getName() + "'s Score Sheet ---");
            System.out.println(playerScores.get(currentPlayer).toString());
            System.out.println("------------------------------------");
        }
    }
}
