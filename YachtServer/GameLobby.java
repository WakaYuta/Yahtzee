import java.net.*;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

/**
 * class GameLobby
 *
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
// GameLobby.java
class GameLobby {
    private final String lobbyId;
    private final YachtClientUser creator;
    private final List<YachtClientUser> players;
    private final int maxPlayers;
    private final List<YachtClientUser> readyPlayers; // 準備完了したプレイヤーのリスト

    public GameLobby(String lobbyId, YachtClientUser creator, int maxPlayers) {
        if (maxPlayers < 2 || maxPlayers > 4) {
            throw new IllegalArgumentException("ロビーのプレイヤー数は2人から4人の間で設定してください。");
        }
        this.lobbyId = lobbyId;
        this.creator = creator;
        this.maxPlayers = maxPlayers;
        this.players = Collections.synchronizedList(new ArrayList<>());
        this.readyPlayers = Collections.synchronizedList(new ArrayList<>());
        
        // 作成者を最初に追加
        addPlayer(creator);
    }

    public String getLobbyId() { return lobbyId; }
    
    public YachtClientUser getCreator() { return creator; }
    
    public List<YachtClientUser> getPlayers() { return Collections.unmodifiableList(players); }
    
    public int getCurrentPlayerCount() { return players.size(); }
    
    public int getMaxPlayers() { return maxPlayers; }

    public boolean addPlayer(YachtClientUser player) {
        synchronized (players) {
            if (players.size() >= maxPlayers || players.contains(player)) {
                return false;
            }
            players.add(player);
            // プレイヤーが追加されたら、準備状態をリセットする
            readyPlayers.remove(player);
            return true;
        }
    }

    public boolean removePlayer(YachtClientUser player) {
        synchronized (players) {
            boolean removed = players.remove(player);
            if (removed) {
                readyPlayers.remove(player);
            }
            return removed;
        }
    }

    public boolean setPlayerReady(YachtClientUser player) {
        synchronized (readyPlayers) {
            if (!players.contains(player)) return false;
            if (!readyPlayers.contains(player)) {
                readyPlayers.add(player);
                return true;
            }
            return false;
        }
    }

    public boolean setPlayerUnready(YachtClientUser player) {
        synchronized (readyPlayers) {
            return readyPlayers.remove(player);
        }
    }

    public boolean areAllPlayersReady() {
        synchronized (players) {
            synchronized (readyPlayers) {
                return players.size() >= 2 && players.size() == readyPlayers.size();
            }
        }
    }

    public boolean isEmpty() { return players.isEmpty(); }
    public boolean isFull() { return players.size() >= maxPlayers; }

    @Override
    public String toString() {
        String playerNames = players.stream()
                                    .map(p -> p.getName() + (readyPlayers.contains(p) ? "(Ready)" : "(Waiting)"))
                                    .collect(Collectors.joining(", "));
        return "Lobby ID: " + lobbyId +
               ", Creator: " + creator.getName() +
               ", Players: " + players.size() + "/" + maxPlayers +
               ", [ " + playerNames + " ]";
    }
}