import java.net.*;
import java.util.*;
import java.io.*;

/**
 * ヨットのクライアントユーザクラス
 * YachtClientUser 
 * 
 * @author Hiroaki Kaji
 * @version 2025/06/10
 */

class YachtClientUser{
    //ソケット
    private Socket socket;

    //ユーザーの名前
    private String name;

    //チャットサーバー
    private YachtServer server = YachtServer.getInstance();
    
    // ClientHandlerへの参照を持つことで、sendMessageをYachtClientUserから直接呼び出せるようにする
    private ClientHandler clientHandler; // ここを追加
    
    // ロビーid管理と準備完了bool
    private String currentLobbyOrGameId; // 現在参加しているロビーまたはゲームのID
    private boolean isReady; // ロビーで準備完了状態か
    private boolean hasUsedIkasamaRoll; // イカサマを使ったかどうか
    private boolean hasUsedFhOrStraightRoll;
    
    public YachtClientUser(String name, ClientHandler handler) {
        this.name = name;
        this.clientHandler = handler;
        this.currentLobbyOrGameId = null;
        this.isReady = false;
        this.hasUsedIkasamaRoll = false; 
        this.hasUsedFhOrStraightRoll = false;
    }
    // ClientHandlerへの参照をセット/ゲットするメソッド
    public ClientHandler getClientHandler() {
        return clientHandler;
    }
    //サーバと通信を行いサーバと動悸させておきたいインスタンスのゲッタとセッタ
    //名前
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    // 参加しているゲームid
    public void setCurrentLobbyOrGameId(String gameID) {
        this.currentLobbyOrGameId = gameID;
    }
    public String getCurrentLobbyOrGameId(){
        return this.currentLobbyOrGameId;
    }
    //ロビーでの準備状態    
    public void setReady(boolean ReadyState){
        this.isReady = ReadyState;
    }
    public boolean getReady(){
        return this.isReady;
    }

    public String toString() {
        return "NAME=" + getName();
    }
    public boolean hasUsedIkasamaRoll() {
        return hasUsedIkasamaRoll;
    }
    public void setUsedIkasamaRoll(boolean used) {
        this.hasUsedIkasamaRoll = used;
    }
    public boolean hasUsedFhOrStraightRoll() {
        return hasUsedFhOrStraightRoll;
    }
    public void setUsedFhOrStraightRoll(boolean used) {
        this.hasUsedFhOrStraightRoll = used;
    }
}