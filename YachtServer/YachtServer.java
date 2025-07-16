import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// --- YachtServer ---
public class YachtServer implements ClientMessageListener{
    private static final int PORT = 20000;
    private static final int MAX_LOBBY_COUNT = 4;

    private static YachtServer instance;

    public static YachtServer getInstance() {
        if (instance == null) {
            instance = new YachtServer();
        }
        return instance;
    }

    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool; // クライアントハンドラを管理するスレッドプール

    // 接続されているクライアントハンドラを管理するマップ (ログイン前も含む)
    private Map<ClientHandler, Socket> handlerSockets; // Handler -> Socket
    // ログイン済みのユーザー名とYachtClientUserオブジェクト

    // ログイン後のユーザー名とユーザーオブジェクトのマップ
    private Map<String, YachtClientUser> connectedUsersByName; // ユーザー名 -> YachtClientUser
    // ハンドラとユーザーオブジェクトのマップ (ログイン後に使用)
    private Map<ClientHandler, YachtClientUser> handlerToUserMap;
    private Map<String, GameLobby> gameLobbies; // ゲーム開始前の待機状態のロビー
    private Map<String, YachtGame> activeGames; // 進行中のゲームIDとYachtGameインスタンスのマップ

    private YachtServer() {
        clientThreadPool = Executors.newCachedThreadPool();
        handlerSockets = Collections.synchronizedMap(new HashMap<>());
        connectedUsersByName = Collections.synchronizedMap(new HashMap<>());
        handlerToUserMap = Collections.synchronizedMap(new HashMap<>());
        gameLobbies = Collections.synchronizedMap(new HashMap<>());
        activeGames = Collections.synchronizedMap(new HashMap<>());
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Yacht Server started on port " + PORT);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected from: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                handlerSockets.put(clientHandler, clientSocket); // ハンドラをソケットと関連付けてマップに保存
                clientThreadPool.submit(clientHandler); // 別スレッドで実行
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) { // サーバーソケットが意図せず閉じられた場合
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            close();
        }
    }

    /**
     * ClientHandlerからのメッセージ受信時に呼ばれるコールバックメソッド
     * @param handler メッセージを送信したClientHandler
     * @param message 受信したメッセージ
     */
    @Override
    public void onClientMessage(ClientHandler handler, String message) {
        // まずはClientHandlerからYachtClientUserを取得しようと試みる
        // ログイン前であればuserはnull
        YachtClientUser sender = handler.getUser();
        
        System.out.println(sender);
        
        // コマンドのパースと処理
        processClientCommand(handler, sender, message);
    }

    /**
     * ClientHandlerの接続が切断されたときに呼ばれるコールバックメソッド
     * @param handler 切断されたClientHandler
     * @param reason 切断理由
     */
    @Override
    public void onClientDisconnected(ClientHandler handler, String reason) {
        YachtClientUser user = handler.getUser(); // 切断されたハンドラに関連付けられたユーザーを取得

        if (user != null) {
            System.out.println(user.getName() + " disconnected. Reason: " + reason);
            removeUser(user, handler); // ユーザーとハンドラを削除
        } else {
            // ログイン前に切断されたハンドラの場合
            System.out.println("Unauthenticated client disconnected from " + handler.getClientSocket().getInetAddress().getHostAddress() + ". Reason: " + reason);
            handlerSockets.remove(handler); // handlerSocketsから削除
            handler.close(); // ハンドラをクローズ
        }
        broadcastLobbyList(); // ロビーリストを更新して全体に通知
    }

    /**
     * ユーザーをサーバーに登録します。
     * @param user 登録するYachtClientUserオブジェクト
     * @param handler そのユーザーを処理するClientHandler
     */
    private void registerUser(YachtClientUser user, ClientHandler handler) {
        connectedUsersByName.put(user.getName(), user); // ユーザー名をキーとしてYachtClientUserを保存
        handlerToUserMap.put(handler, user); // ハンドラとユーザーを紐付け
        handler.setUser(user); // ClientHandlerにもYachtClientUserを設定
        System.out.println("User registered: " + user.getName() + " from " + handler.getClientSocket().getInetAddress().getHostAddress());
    }

    /**
     * ユーザーをサーバーから削除します。
     * @param user 削除するYachtClientUserオブジェクト
     * @param handler そのユーザーを処理していたClientHandler
     */
    private void removeUser(YachtClientUser user, ClientHandler handler) {
        if (user == null) return;

        // ユーザーが現在ロビーまたはゲームにいる場合、そこから離脱させる
        if (user.getCurrentLobbyOrGameId() != null) {
            GameLobby lobby = gameLobbies.get(user.getCurrentLobbyOrGameId());
            if (lobby != null) {
                leaveLobby(user); // ロビーから離脱
            } else {
                leaveGame(user); // ゲームから離脱
            }
        }

        connectedUsersByName.remove(user.getName()); // ユーザー名マップから削除
        handlerToUserMap.remove(handler); // ハンドラとユーザーの関連付けを削除
        handlerSockets.remove(handler); // handlerSocketsからも削除

        handler.close(); // ハンドラを通してソケットをクローズ

        System.out.println("User removed: " + user.getName() + ". Total logged in users: " + connectedUsersByName.size());
    }


    /**
     * 指定した名前のユーザーを取得する
     * @param name 検索するユーザー名
     * @return 該当するYachtClientUser、またはnull
     */
    public YachtClientUser getUserByName(String name) {
        return connectedUsersByName.get(name);
    }

    /**
     * 特定のユーザーにメッセージを送信する。
     * @param recipient 送信先のYachtClientUser
     * @param message 送信するメッセージ
     */
    public void sendMessage(YachtClientUser recipient, String message) {
        if (recipient == null) {
            System.err.println("Attempted to send message to a null recipient.");
            return;
        }

        ClientHandler handler = recipient.getClientHandler(); // YachtClientUserから直接ハンドラを取得

        if (handler != null) {
            handler.sendMessageToClient(message);
        } else {
            System.err.println("Attempted to send message to user " + recipient.getName() + " with no associated ClientHandler.");
            // 適切なエラーハンドリングまたはユーザーの切断処理をここに追加
            // onClientDisconnected(recipient, "No associated handler found for sending message."); // この引数では呼ばない
        }
    }


    /**
     * ゲームからプレイヤーを離脱させる（接続断などでゲームを中断する場合）
     * @param player 離脱するプレイヤー
     */
    private void leaveGame(YachtClientUser player) {
        String gameId = player.getCurrentLobbyOrGameId();
        if (gameId == null || !activeGames.containsKey(gameId)) {
            return;
        }

        YachtGame game = activeGames.get(gameId);
        game.removePlayer(player); // ゲーム内のプレイヤーリストから削除

        System.out.println(player.getName() + " left game: " + gameId);
        broadcastToGame(gameId, "PLAYER_LEFT_GAME:" + player.getName());

        if (game.getPlayerList().isEmpty()) {
            endGame(gameId, "ABORTED"); // ゲームが空になったら終了
        } else {
            // 現在のターンプレイヤーが離脱した場合、次のプレイヤーにターンを渡すなどのロジック
            if (game.getCurrentPlayer() == player) {
                game.nextTurn(); // 次のプレイヤーにターンを渡す
                broadcastToGame(gameId, "YOUR_TURN:" + game.getCurrentPlayer().getName());
            }
        }
        player.setCurrentLobbyOrGameId(null);
        player.setReady(false);
    }


    // --- ロビー管理メソッド ---

    /**
     * 新しいゲームロビーを作成し、作成者を参加させる。
     * @param creator ゲーム作成者
     * @param maxPlayers ロビーの最大プレイヤー数 (2?4)
     * @return 新しいゲームのID
     */
    public String createGameLobby(YachtClientUser creator, int maxPlayers) {
        if (gameLobbies.size() >= MAX_LOBBY_COUNT) {
            sendMessage(creator, "ERROR:Cannot create more lobbies. Server full.");
            return null;
        }

        String lobbyId = "LOBBY_" + UUID.randomUUID().toString().substring(0, 4); // 短いIDにする
        GameLobby lobby = new GameLobby(lobbyId, creator, maxPlayers);
        gameLobbies.put(lobbyId, lobby);
        
        // 作成者にロビーIDを設定
        creator.setCurrentLobbyOrGameId(lobbyId);

        System.out.println(creator.getName() + " created lobby: " + lobbyId + " (Max: " + maxPlayers + ")");
        sendMessage(creator, "LOBBY_CREATED:" + lobbyId + ":" + maxPlayers);
        broadcastLobbyList(); // ロビーリスト更新を全体に通知
        return lobbyId;
    }

    /**
     * 既存のゲームロビーに参加する。
     * @param lobbyId 参加したいロビーのID
     * @param player 参加するプレイヤー
     * @return 参加に成功した場合はtrue、失敗した場合はfalse
     */
    public boolean joinGameLobby(String lobbyId, YachtClientUser player) {
        GameLobby lobby = gameLobbies.get(lobbyId);
        if (lobby != null) {
            if (lobby.addPlayer(player)) {
                player.setCurrentLobbyOrGameId(lobbyId); // プレイヤーに現在のロビーIDを設定
                System.out.println(player.getName() + " joined lobby: " + lobbyId + ". Players: " + lobby.getCurrentPlayerCount());
                broadcastToLobby(lobbyId, "PLAYER_JOINED:" + player.getName() + ":" + lobby.getCurrentPlayerCount() + "/" + lobby.getMaxPlayers());
                sendMessage(player, "JOINED_LOBBY:" + lobbyId + ":" + lobby.getCurrentPlayerCount() + "/" + lobby.getMaxPlayers());
                broadcastLobbyList(); // ロビーリスト更新を全体に通知
                return true;
            } else {
                sendMessage(player, "ERROR:Could not join lobby " + lobbyId + ". It might be full or you are already in it.");
            }
        } else {
            sendMessage(player, "ERROR:Lobby " + lobbyId + " not found.");
        }
        return false;
    }

    /**
     * ロビーから退出する。
     * @param player 退出するプレイヤー
     */
    public void leaveLobby(YachtClientUser player) {
        String lobbyId = player.getCurrentLobbyOrGameId();
        if (lobbyId == null || !gameLobbies.containsKey(lobbyId)) {
            sendMessage(player, "ERROR:You are not in any lobby.");
            return;
        }

        GameLobby lobby = gameLobbies.get(lobbyId);
        lobby.removePlayer(player);
        
        System.out.println(player.getName() + " left lobby: " + lobbyId);
        broadcastToLobby(lobbyId, "PLAYER_LEFT:" + player.getName());
        if (lobby.isEmpty()) {
            gameLobbies.remove(lobbyId); // ロビーが空になったら削除
            System.out.println("Lobby " + lobbyId + " is empty and removed.");
        }
        player.setCurrentLobbyOrGameId(null); // プレイヤーのロビーIDをリセット
        player.setReady(false); // 準備状態もリセット
        sendMessage(player, "LEFT_LOBBY:" + lobbyId);
        broadcastLobbyList(); // ロビーリスト更新を全体に通知
    }

    /**
     * プレイヤーの準備ができたことをマークし、全員が準備できたらゲームを開始する。
     * @param player 準備できたプレイヤー
     * @param lobbyId 参加しているゲームロビーのID
     */
    public void playerReady(YachtClientUser player, String lobbyId) {
        GameLobby lobby = gameLobbies.get(lobbyId);
        if (lobby != null && lobby.getPlayers().contains(player)) {
            if (lobby.setPlayerReady(player)) {
                System.out.println(player.getName() + " is READY in lobby: " + lobbyId);
                broadcastToLobby(lobbyId, "PLAYER_READY:" + player.getName());

                if (lobby.areAllPlayersReady()) { // 全員が準備OKならゲーム開始
                    startGame(lobbyId, new ArrayList<>(lobby.getPlayers())); // ロビープレイヤーのコピーを渡す
                } else {
                    sendMessage(player, "WAITING_FOR_OTHERS_TO_READY");
                }
            } else {
                sendMessage(player, "ERROR:Already ready or not in this lobby.");
            }
        } else {
            sendMessage(player, "ERROR:Not in a valid lobby.");
        }
    }

    /**
     * ゲームを開始する。
     * @param gameId ゲームのID (ロビーIDをそのまま使用)
     * @param players ゲームに参加するプレイヤーリスト
     */
    private void startGame(String gameId, ArrayList<YachtClientUser> players) {
        
        YachtGame game = new YachtGame(gameId, players);
        activeGames.put(gameId, game); // 進行中のゲームとして登録
        gameLobbies.remove(gameId); // ロビーから削除

        // 参加プレイヤー全員の名前をカンマ区切りの文字列にする
        StringBuilder playerNamesBuilder = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            playerNamesBuilder.append(players.get(i).getName());
            // 最後のプレイヤーでなければ、カンマを追加する
            if (i < players.size() - 1) {
                playerNamesBuilder.append(",");
            }
        }
        String playerNames = playerNamesBuilder.toString();

        for (YachtClientUser player : players) {
            player.setCurrentLobbyOrGameId(gameId);
            player.setReady(false);
            // GAME_STARTEDメッセージに、プレイヤー全員の名前のリストを追加して送信
            sendMessage(player, "GAME_STARTED:" + gameId + ":" + playerNames);
        }
        
        // ゲーム開始時の最初のターンを通知
        YachtClientUser currentPlayer = game.getCurrentPlayer();
        if (currentPlayer != null) {
            broadcastToGame(gameId, "YOUR_TURN:" + currentPlayer.getName());
        }
        broadcastLobbyList();
    }

    /**
     * ゲームを終了する。
     * @param gameId 終了するゲームのID
     */
    public void endGame(String gameId, String finalScores) {
        YachtGame game = activeGames.remove(gameId);
        if (game != null) {
            System.out.println("Game " + gameId + " ended.");
            // ゲームに参加していた全プレイヤーにゲーム終了と最終結果を通知
            for (YachtClientUser player : game.getPlayerList()) {
                sendMessage(player, "GAME_ENDED:" + finalScores);
                player.setCurrentLobbyOrGameId(null);
                player.setReady(false);
            }
        }
        broadcastLobbyList(); // ロビーリスト更新を全体に通知 (ゲーム終了でロビーに空きができるため)
    }

    /**
     * 特定のゲームIDに関連するユーザーにメッセージをブロードキャストする。
     * @param gameId ブロードキャスト対象のゲームID
     * @param message 送信するメッセージ
     */
    private void broadcastToGame(String gameId, String message) {
        YachtGame game = activeGames.get(gameId);
        if (game != null) {
            for (YachtClientUser player : game.getPlayerList()) {
                sendMessage(player, message);
            }
        }
    }

    /**
     * 特定のロビーに関連するユーザーにメッセージをブロードキャストする。
     * @param lobbyId ブロードキャスト対象のロビーID
     * @param message 送信するメッセージ
     */
    private void broadcastToLobby(String lobbyId, String message) {
        GameLobby lobby = gameLobbies.get(lobbyId);
        if (lobby != null) {
            for (YachtClientUser player : lobby.getPlayers()) {
                sendMessage(player, message);
            }
        }
    }

    /**
     * 全てのログイン済みユーザーに利用可能なロビーリストをブロードキャストする。
     */
    private void broadcastLobbyList() {
        StringBuilder sb = new StringBuilder("LOBBY_LIST:");
        if (gameLobbies.isEmpty()) {
            sb.append("NONE");
        } else {
            boolean first = true;
            for (GameLobby lobby : gameLobbies.values()) {
                if (!first) sb.append(";");
                sb.append(lobby.getLobbyId()).append(",")
                  .append(lobby.getCreator().getName()).append(",")
                  .append(lobby.getCurrentPlayerCount()).append(",")
                  .append(lobby.getMaxPlayers());
                first = false;
            }
        }
        String lobbyListMessage = sb.toString();
        // 全てのログイン済みユーザーに送信
        for (YachtClientUser user : connectedUsersByName.values()) {
            // ロビーにもゲームにも参加していないユーザーにのみ送信
            if (user.getCurrentLobbyOrGameId() == null) {
                sendMessage(user, lobbyListMessage);
            }
        }
    }

    /**
     * 特定のクライアントにのみロビーリストを送信する。
     * @param recipient 送信先のクライアント
     */
    private void broadcastLobbyListToOne(YachtClientUser recipient) {
        StringBuilder sb = new StringBuilder("LOBBY_LIST:");
        if (gameLobbies.isEmpty()) {
            sb.append("NONE");
        } else {
            boolean first = true;
            for (GameLobby lobby : gameLobbies.values()) {
                if (!first) sb.append(";");
                sb.append(lobby.getLobbyId()).append(",")
                  .append(lobby.getCreator().getName()).append(",")
                  .append(lobby.getCurrentPlayerCount()).append(",")
                  .append(lobby.getMaxPlayers());
                first = false;
            }
        }
        sendMessage(recipient, sb.toString());
    }

    // processClientCommand は onClientMessageReceived から呼び出される
    // processClientCommand は onClientMessageReceived から呼び出される
    private void processClientCommand(ClientHandler handler, YachtClientUser sender, String command) {
        String[] parts = command.split(":", 3);//最大3分割

        String contextId = null;
        String cmd;
        String args = "";

        // コマンドがIDプレフィックスを持つかどうかをチェック
        if (parts.length >= 2 && (parts[0].startsWith("LOBBY_") || parts[0].startsWith("GAME_"))) {
            contextId = parts[0]; // 最初の部分がロビー/ゲームID
            cmd = parts[1]; // 次がコマンド
            args = parts.length > 2 ? parts[2] : ""; // 残りが引数
        } else {
            // IDがないか、一般的なサーバーコマンド
            cmd = parts[0];
            args = parts.length > 1 ? parts[1] : "";
        }

        // ログインが必要なコマンドで、senderがnullの場合
        if (sender == null && !cmd.equals("LOGIN")) {
            handler.sendMessageToClient("ERROR:You must login first.");
            return;
        }
        
        // ロビー/ゲーム内コマンドで、かつ sender のコンテキストIDと一致しない場合のエラーチェック
        if (sender != null && contextId != null) {
            String senderCurrentContextId = sender.getCurrentLobbyOrGameId();
            if (senderCurrentContextId == null || !contextId.equals(senderCurrentContextId)) {
                // クライアントが間違ったコンテキストIDでコマンドを送信した場合
                handler.sendMessageToClient("ERROR:Mismatched context ID. You are in " +
                                            (senderCurrentContextId != null ? senderCurrentContextId : "no context") +
                                            ", but sent command for " + contextId + ".");
                return;
            }
        }

        // 実行IDを設定（senderの現在のコンテキストIDを優先）
        String effectiveContextId = (sender != null && sender.getCurrentLobbyOrGameId() != null) ? sender.getCurrentLobbyOrGameId() : contextId;
        
        System.out.println("command : " + cmd + args);
        //以下でコマンドごとの処理を行なう、
        //サーバへの接続や、ログアウトなどのサーバ側で処理すべきもの及びロビーでの関数はココで処理を記述。
        //ゲーム内の処理やロビーでの準備完了などの処理は、各ゲームインスタンスなどに受け渡す。
        switch (cmd) {
            case "LOGIN":
                if (sender != null){
                    if (sender.getName() != null) {
                        sendMessage(sender, "ERROR:You are already logged in or username is set.");
                        System.out.println("ERROR:You are already logged in or username is set.");
                        return;
                    }else{
                        sendMessage(sender,"ERROR:名前を予め設定してからサーバに接続してください。");
                    }
                }
                
                if (args.isEmpty()) {
                    sendMessage(sender, "ERROR:Username cannot be empty.");
                    System.out.println("ERROR:Username cannot be empty.");
                    return;
                }
                if (getUserByName(args) != null && !getUserByName(args).equals(sender)) {
                    sendMessage(sender, "ERROR:Username " + args + " is already taken.");
                    System.out.println("ERROR:Username " + args + " is already taken.");
                    return;
                }
                //入力の名前がから出ないとわかったので、そのなまえを用いて、クライアントユーザを新規に作成し登録
                if (sender == null){
                    sender = new YachtClientUser(args, handler);
                    registerUser(sender, handler);
                }
                
                sender.setName(args);
                System.out.println("login : "+sender.getName());
                sendMessage(sender, "LOGIN_SUCCESS:" + sender.getName());
                System.out.println(sender.getName() + " logged in.");
                broadcastLobbyListToOne(sender);
                break;

            case "GET_LOBBY_LIST":
                broadcastLobbyListToOne(sender);
                break;

            case "CREATE_LOBBY":
                if (effectiveContextId != null) {
                    sendMessage(sender, "ERROR:You are already in a lobby or game. Please leave first.");
                    return;
                }
                try {
                    int maxPlayers = Integer.parseInt(args); 
                    if (maxPlayers < 2 || maxPlayers > 4) {
                        sendMessage(sender, "ERROR:Max players must be between 2 and 4.");
                        return;
                    }
                    createGameLobby(sender, maxPlayers);
                } catch (NumberFormatException e) {
                    sendMessage(sender, "ERROR:Invalid maxPlayers argument for CREATE_LOBBY. Usage: CREATE_LOBBY:4");
                }
                break;

            case "JOIN_LOBBY":
                if (effectiveContextId != null) {
                    sendMessage(sender, "ERROR:You are already in a lobby or game. Please leave first.");
                    return;
                }
                joinGameLobby(args, sender);
                break;

            case "LEAVE_LOBBY":
                if (effectiveContextId == null || !gameLobbies.containsKey(effectiveContextId)) {
                    sendMessage(sender, "ERROR:You are not in a lobby.");
                    return;
                }
                leaveLobby(sender);
                break;

            case "READY":
                if (effectiveContextId == null || !gameLobbies.containsKey(effectiveContextId)) {
                    sendMessage(sender, "ERROR:Not in a lobby to set ready.");
                    return;
                }
                playerReady(sender, effectiveContextId);
                break;

            case "ROLL_DICE":
            case "IKASAMA_ROLL":
            case "FH_OR_STRAIGHT_ROLL":
            case "RECORD_SCORE":
            case "GET_SCORE_SHEET":
                //ゲーム内で呼び出される関数はidが正しいかどうかを検証した後、ゲームに受け渡す。
                if (effectiveContextId == null || !activeGames.containsKey(effectiveContextId)) {
                    sendMessage(sender, "ERROR:You are not in an active game to use this command.");
                    return;
                }
                
                YachtGame game = activeGames.get(effectiveContextId);

                if (!game.getPlayerList().contains(sender)) { // プレイヤーがゲームに参加していない場合はエラー
                    sendMessage(sender, "ERROR:You are not a player in this game.");
                    return;
                }

                if (!cmd.equals("FH_OR_STRAIGHT_ROLL") && !cmd.equals("IKASAMA_ROLL") && !game.getCurrentPlayer().equals(sender)) {
                    sendMessage(sender, "ERROR:It's not your turn.");
                    return;
                }
                
                GameCommandResult gameCommandResult = game.handleGameCommand(sender, cmd, args);
                
                // GameCommandResultからメッセージリストを取得
                List<GameCommandResult.MessageToSend> messages = gameCommandResult.getMessages();
                
                // 最初のメッセージがGAME_OVERかどうかを特別にチェック
                if (!messages.isEmpty() && messages.get(0).message.startsWith("GAME_OVER")) {
                    String finalScores = messages.get(0).message.split(":", 2)[1];
                    endGame(effectiveContextId, finalScores); // endGameメソッドに最終スコアを渡す
                    return; // これ以降の処理はしない
                }
        
                // GameCommandResultからメッセージリストを取得し、適切に送信
                for (GameCommandResult.MessageToSend msg : gameCommandResult.getMessages()) {
                    if (msg.sendToAll) {
                        // 全員に送信する場合
                        for (YachtClientUser player : game.getPlayerList()) {
                            sendMessage(player, msg.message);
                        }
                    } else {
                        // 特定の送信者（今回の場合はコマンドを発行したsender）にのみ送信する場合
                        sendMessage(sender, msg.message);
                    }
                }
                
                break;

            case "LOGOUT":
                // removeConnectedUserはonClientDisconnectedから呼ばれるので、ここでは不要
                // SocketのcloseもremoveConnectedUserで処理される
                sendMessage(sender, "LOGGED_OUT:Goodbye!"); // クライアントにログアウト成功を通知
                break;

            default:
                sendMessage(sender, "ERROR:Unknown command or invalid context for command: " + cmd);
                break;
        }
    }


    public void close() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server closed.");
            }
            // 接続中の全クライアントハンドラをクローズ
            // synchronized (handlerSockets) { // handlerSocketsは既にsynchronizedMapなので不要
                for (ClientHandler handler : new ArrayList<>(handlerSockets.keySet())) { // ConcurrentModificationException回避のためコピー
                    try {
                        handler.close(); // ClientHandlerのcloseを呼び出す
                    } catch (Exception e) {
                        System.err.println("Error closing client handler during server shutdown: " + e.getMessage());
                    }
                }
                handlerSockets.clear();
                handlerToUserMap.clear();
                connectedUsersByName.clear();
            // }
            gameLobbies.clear();
            activeGames.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientThreadPool.shutdownNow(); // スレッドプールをシャットダウン
            System.out.println("Client thread pool shut down.");
        }
    }

    public static void main(String[] args) {
        YachtServer server = YachtServer.getInstance();
        server.start();
    }
}

