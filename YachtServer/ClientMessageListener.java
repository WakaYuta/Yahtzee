
/**
 * interface ClientMessageListener here.
 * 
 * @author Hiroaki　Kaji
 * @version 2025/6/6
 */
interface ClientMessageListener {
    /**
     * クライアントからメッセージを受信したときに呼び出されるコールバックメソッド
     * @param sender メッセージを送信したClientHandler
     * @param fullMessage 受信した完全なメッセージ文字列
     */
    void onClientMessage(ClientHandler senderHandler, String fullMessage);

    /**
     * クライアント接続が失われたときに呼び出されるコールバックメソッド
     * @param user 接続が失われたClientHandler
     * @param reason 接続喪失の理由
     */
    void onClientDisconnected(ClientHandler disconnectedHandler, String reason);
}