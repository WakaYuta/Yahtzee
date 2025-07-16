
/**
 * ゲームの状態とデータを保持するモデルクラス
 */
import javax.swing.DefaultListModel;
import java.util.HashSet;
import java.util.Set;

public class GameModel {

    private String currentGameId = null;
    private final DefaultListModel<String> lobbyListModel = new DefaultListModel<>();
    private final Set<String> recordedCategories = new HashSet<>();
    private String playerName;

    // --- Getterメソッド ---

    public String getCurrentGameId() {
        return currentGameId;
    }

    public DefaultListModel<String> getLobbyListModel() {
        return lobbyListModel;
    }

    public Set<String> getRecordedCategories() {
        return recordedCategories;
    }
    
    public String getPlayerName() {
        return playerName;
    }

    // --- Setter/状態変更メソッド ---

    public void setCurrentGameId(String gameId) {
        this.currentGameId = gameId;
    }

    public void addRecordedCategory(String category) {
        recordedCategories.add(category);
    }
    
    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public void clearLobbyList() {
        lobbyListModel.clear();
    }
    
    public void addLobbyToList(String lobbyInfo) {
        lobbyListModel.addElement(lobbyInfo);
    }

    /**
     * 新しいゲームが始まる、または切断した際に状態をリセット
     */
    public void resetForNewGame() {
        currentGameId = null;
        recordedCategories.clear();
    }
}
