import java.net.*;
import java.util.*;
import java.io.*;
/**
 *  class GameCommandResult
 *  
 *  ゲーム内で呼び出すコマンドの結果用のクラス
 *  コマンドが正常に動作したかどうか＋結果のテキスト -2025/6/6
 *  複数のコマンドを受け取った時の処理追加 -2025/6/16
 *
 * @author Hiroaki Kaji
 * @version 2025/6/16
 */
class GameCommandResult {
    
    // 内部クラス: 送信するメッセージとその送信先を定義
    public static class MessageToSend {
        public final String message;
        public final boolean sendToAll; // trueなら全員に、falseなら元のsenderに送信

        public MessageToSend(String message, boolean sendToAll) {
            this.message = message;
            this.sendToAll = sendToAll;
        }
    }

    public final boolean success;
    private final List<MessageToSend> messages; // 複数のメッセージを保持

    // コンストラクタ（単一のメッセージをsenderに返す場合）
    public GameCommandResult(boolean success, String message) {
        this.success = success;
        this.messages = new ArrayList<>();
        this.messages.add(new MessageToSend(message, false)); // デフォルトはsenderにのみ送信
    }

    // コンストラクタ（複数のメッセージを返す場合）
    private GameCommandResult(boolean success, List<MessageToSend> messages) {
        this.success = success;
        this.messages = messages;
    }

    // 新しい静的メソッド: 複数のメッセージを持つ結果を作成
    public static GameCommandResult createMultiMessageResult(boolean success, List<MessageToSend> messages) {
        return new GameCommandResult(success, messages);
    }

    // メッセージリストを取得するゲッター
    public List<MessageToSend> getMessages() {
        return messages;
    }
}