import java.net.*;
import java.util.*;
import java.io.*;

/**
 * Write a description of class Handler here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
class ClientHandler implements Runnable {
        private Socket clientSocket;
        private YachtClientUser user; // このハンドラが管理するYachtClientUserインスタンス
        private ClientMessageListener listener; 
        private PrintWriter out; // メッセージ送信用のPrintWriterを追加
        private BufferedReader in; // メッセージ受信用のBufferedReaderを追加
        
        public ClientHandler(Socket clientSocket, ClientMessageListener listener) {
            this.clientSocket = clientSocket;
            this.listener = listener;
            try {
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error setting up client handler: " + e.getMessage());
                close(); // 初期化失敗時もソケットを閉じる
            }
        }
        // ログイン成功時にYachtClientUserをセットするセッター
        public void setUser(YachtClientUser user) {
            this.user = user;
        }
        
        // ハンドラに紐付けられているYachtClientUserを返すゲッタ
        public YachtClientUser getUser() {
            return this.user;
        }
    
        // クライアントソケットを取得するゲッター (YachtServerのマップ管理で必要)
        public Socket getClientSocket() {
            return clientSocket;
        }
    
        
    
        @Override
        public void run() {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            
            try {
               
                String clientMessage;
                while (!clientSocket.isClosed() && (clientMessage = in.readLine()) != null) {
                    System.out.println("Raw received from " + (user != null ? user.getName() : "unauthenticated client") + ": " + clientMessage);
                    
                    // 受信したメッセージをリスナー（YachtServer）にコールバック
                    // メッセージを送信したClientHandler自身を引数として渡す
                    listener.onClientMessage(this, clientMessage);
                }
            } catch (IOException e) {
                // クライアントが切断した場合のIOExceptionは一般的 $#$
                if (!clientSocket.isClosed()) { // 正常な切断でなければエラーとしてログ
                    System.err.println("Error reading from client " + (user != null ? user.getName() : "unauthenticated client") + " (" + clientAddress + "): " + e.getMessage());
                    // 接続が失われたことをリスナーに通知
                    listener.onClientDisconnected(this, "Error: " + e.getMessage()); // ここを変更
                } else {
                    // 正常な切断の場合
                    System.out.println((user != null ? user.getName() : "Unauthenticated client") + " disconnected gracefully.");
                    listener.onClientDisconnected(this, "Client disconnected gracefully."); // ここを変更
                }
            } finally {
                // スレッドが終了する際、クライアントのクリーンアップはサーバー側で責任を持つ
                // onClientDisconnectedでサーバーに任せるため、ここでは明示的にクローズしない
                // ただし、onClientDisconnectedが呼ばれることを保証するため、ここで呼ばない
                // クローズ処理はonClientDisconnectedで行われるようにする
            }
        }
        
        // クライアントへメッセージを送信するメソッド
        public void sendMessageToClient(String message) {
            if (out != null) {
                out.println(message);
            }
        }
        
        // ソケットとストリームを安全にクローズするメソッド
        public void close() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client handler resources: " + e.getMessage());
            }
        }
    }