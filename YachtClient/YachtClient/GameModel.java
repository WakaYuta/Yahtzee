
/**
 * ゲームの状態とデータを保持するモデルクラス
 */
import javax.swing.DefaultListModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameModel {

    private String currentGameId = null;
    private final DefaultListModel<String> lobbyListModel = new DefaultListModel<>();
    private String playerName;

    // 全プレイヤーのスコア情報を管理するMapを追加
    private final Map<String, PlayerScoreModel> playerScores = new HashMap<>();

    // 全プレイヤーのスコア情報を取得するゲッター
    public Map<String, PlayerScoreModel> getPlayerScores() {
        return playerScores;
    }
    
    // 特定のプレイヤーのスコアモデルを取得するメソッド
    public PlayerScoreModel getScoreModel(String name) {
        // 存在しない場合は新しく作って返す
        return playerScores.computeIfAbsent(name, PlayerScoreModel::new);
    }
    
    public String getCurrentGameId() { return currentGameId; }
    public DefaultListModel<String> getLobbyListModel() { return lobbyListModel; }
    public String getPlayerName() { return playerName; }
    public void setCurrentGameId(String gameId) { this.currentGameId = gameId; }
    public void setPlayerName(String name) { this.playerName = name; }
    public void clearLobbyList() { lobbyListModel.clear(); }
    public void addLobbyToList(String lobbyInfo) { lobbyListModel.addElement(lobbyInfo); }

    //新しいゲームが始まる、または切断した際に状態をリセット
    public void resetForNewGame() {
        currentGameId = null;
        playerScores.clear();
    }
}
