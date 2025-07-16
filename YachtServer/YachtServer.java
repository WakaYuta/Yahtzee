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
    private ExecutorService clientThreadPool; // �N���C�A���g�n���h�����Ǘ�����X���b�h�v�[��

    // �ڑ�����Ă���N���C�A���g�n���h�����Ǘ�����}�b�v (���O�C���O���܂�)
    private Map<ClientHandler, Socket> handlerSockets; // Handler -> Socket
    // ���O�C���ς݂̃��[�U�[����YachtClientUser�I�u�W�F�N�g

    // ���O�C����̃��[�U�[���ƃ��[�U�[�I�u�W�F�N�g�̃}�b�v
    private Map<String, YachtClientUser> connectedUsersByName; // ���[�U�[�� -> YachtClientUser
    // �n���h���ƃ��[�U�[�I�u�W�F�N�g�̃}�b�v (���O�C����Ɏg�p)
    private Map<ClientHandler, YachtClientUser> handlerToUserMap;
    private Map<String, GameLobby> gameLobbies; // �Q�[���J�n�O�̑ҋ@��Ԃ̃��r�[
    private Map<String, YachtGame> activeGames; // �i�s���̃Q�[��ID��YachtGame�C���X�^���X�̃}�b�v

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
                handlerSockets.put(clientHandler, clientSocket); // �n���h�����\�P�b�g�Ɗ֘A�t���ă}�b�v�ɕۑ�
                clientThreadPool.submit(clientHandler); // �ʃX���b�h�Ŏ��s
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) { // �T�[�o�[�\�P�b�g���Ӑ}��������ꂽ�ꍇ
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            close();
        }
    }

    /**
     * ClientHandler����̃��b�Z�[�W��M���ɌĂ΂��R�[���o�b�N���\�b�h
     * @param handler ���b�Z�[�W�𑗐M����ClientHandler
     * @param message ��M�������b�Z�[�W
     */
    @Override
    public void onClientMessage(ClientHandler handler, String message) {
        // �܂���ClientHandler����YachtClientUser���擾���悤�Ǝ��݂�
        // ���O�C���O�ł����user��null
        YachtClientUser sender = handler.getUser();
        
        System.out.println(sender);
        
        // �R�}���h�̃p�[�X�Ə���
        processClientCommand(handler, sender, message);
    }

    /**
     * ClientHandler�̐ڑ����ؒf���ꂽ�Ƃ��ɌĂ΂��R�[���o�b�N���\�b�h
     * @param handler �ؒf���ꂽClientHandler
     * @param reason �ؒf���R
     */
    @Override
    public void onClientDisconnected(ClientHandler handler, String reason) {
        YachtClientUser user = handler.getUser(); // �ؒf���ꂽ�n���h���Ɋ֘A�t����ꂽ���[�U�[���擾

        if (user != null) {
            System.out.println(user.getName() + " disconnected. Reason: " + reason);
            removeUser(user, handler); // ���[�U�[�ƃn���h�����폜
        } else {
            // ���O�C���O�ɐؒf���ꂽ�n���h���̏ꍇ
            System.out.println("Unauthenticated client disconnected from " + handler.getClientSocket().getInetAddress().getHostAddress() + ". Reason: " + reason);
            handlerSockets.remove(handler); // handlerSockets����폜
            handler.close(); // �n���h�����N���[�Y
        }
        broadcastLobbyList(); // ���r�[���X�g���X�V���đS�̂ɒʒm
    }

    /**
     * ���[�U�[���T�[�o�[�ɓo�^���܂��B
     * @param user �o�^����YachtClientUser�I�u�W�F�N�g
     * @param handler ���̃��[�U�[����������ClientHandler
     */
    private void registerUser(YachtClientUser user, ClientHandler handler) {
        connectedUsersByName.put(user.getName(), user); // ���[�U�[�����L�[�Ƃ���YachtClientUser��ۑ�
        handlerToUserMap.put(handler, user); // �n���h���ƃ��[�U�[��R�t��
        handler.setUser(user); // ClientHandler�ɂ�YachtClientUser��ݒ�
        System.out.println("User registered: " + user.getName() + " from " + handler.getClientSocket().getInetAddress().getHostAddress());
    }

    /**
     * ���[�U�[���T�[�o�[����폜���܂��B
     * @param user �폜����YachtClientUser�I�u�W�F�N�g
     * @param handler ���̃��[�U�[���������Ă���ClientHandler
     */
    private void removeUser(YachtClientUser user, ClientHandler handler) {
        if (user == null) return;

        // ���[�U�[�����݃��r�[�܂��̓Q�[���ɂ���ꍇ�A�������痣�E������
        if (user.getCurrentLobbyOrGameId() != null) {
            GameLobby lobby = gameLobbies.get(user.getCurrentLobbyOrGameId());
            if (lobby != null) {
                leaveLobby(user); // ���r�[���痣�E
            } else {
                leaveGame(user); // �Q�[�����痣�E
            }
        }

        connectedUsersByName.remove(user.getName()); // ���[�U�[���}�b�v����폜
        handlerToUserMap.remove(handler); // �n���h���ƃ��[�U�[�̊֘A�t�����폜
        handlerSockets.remove(handler); // handlerSockets������폜

        handler.close(); // �n���h����ʂ��ă\�P�b�g���N���[�Y

        System.out.println("User removed: " + user.getName() + ". Total logged in users: " + connectedUsersByName.size());
    }


    /**
     * �w�肵�����O�̃��[�U�[���擾����
     * @param name �������郆�[�U�[��
     * @return �Y������YachtClientUser�A�܂���null
     */
    public YachtClientUser getUserByName(String name) {
        return connectedUsersByName.get(name);
    }

    /**
     * ����̃��[�U�[�Ƀ��b�Z�[�W�𑗐M����B
     * @param recipient ���M���YachtClientUser
     * @param message ���M���郁�b�Z�[�W
     */
    public void sendMessage(YachtClientUser recipient, String message) {
        if (recipient == null) {
            System.err.println("Attempted to send message to a null recipient.");
            return;
        }

        ClientHandler handler = recipient.getClientHandler(); // YachtClientUser���璼�ڃn���h�����擾

        if (handler != null) {
            handler.sendMessageToClient(message);
        } else {
            System.err.println("Attempted to send message to user " + recipient.getName() + " with no associated ClientHandler.");
            // �K�؂ȃG���[�n���h�����O�܂��̓��[�U�[�̐ؒf�����������ɒǉ�
            // onClientDisconnected(recipient, "No associated handler found for sending message."); // ���̈����ł͌Ă΂Ȃ�
        }
    }


    /**
     * �Q�[������v���C���[�𗣒E������i�ڑ��f�ȂǂŃQ�[���𒆒f����ꍇ�j
     * @param player ���E����v���C���[
     */
    private void leaveGame(YachtClientUser player) {
        String gameId = player.getCurrentLobbyOrGameId();
        if (gameId == null || !activeGames.containsKey(gameId)) {
            return;
        }

        YachtGame game = activeGames.get(gameId);
        game.removePlayer(player); // �Q�[�����̃v���C���[���X�g����폜

        System.out.println(player.getName() + " left game: " + gameId);
        broadcastToGame(gameId, "PLAYER_LEFT_GAME:" + player.getName());

        if (game.getPlayerList().isEmpty()) {
            endGame(gameId, "ABORTED"); // �Q�[������ɂȂ�����I��
        } else {
            // ���݂̃^�[���v���C���[�����E�����ꍇ�A���̃v���C���[�Ƀ^�[����n���Ȃǂ̃��W�b�N
            if (game.getCurrentPlayer() == player) {
                game.nextTurn(); // ���̃v���C���[�Ƀ^�[����n��
                broadcastToGame(gameId, "YOUR_TURN:" + game.getCurrentPlayer().getName());
            }
        }
        player.setCurrentLobbyOrGameId(null);
        player.setReady(false);
    }


    // --- ���r�[�Ǘ����\�b�h ---

    /**
     * �V�����Q�[�����r�[���쐬���A�쐬�҂��Q��������B
     * @param creator �Q�[���쐬��
     * @param maxPlayers ���r�[�̍ő�v���C���[�� (2?4)
     * @return �V�����Q�[����ID
     */
    public String createGameLobby(YachtClientUser creator, int maxPlayers) {
        if (gameLobbies.size() >= MAX_LOBBY_COUNT) {
            sendMessage(creator, "ERROR:Cannot create more lobbies. Server full.");
            return null;
        }

        String lobbyId = "LOBBY_" + UUID.randomUUID().toString().substring(0, 4); // �Z��ID�ɂ���
        GameLobby lobby = new GameLobby(lobbyId, creator, maxPlayers);
        gameLobbies.put(lobbyId, lobby);
        
        // �쐬�҂Ƀ��r�[ID��ݒ�
        creator.setCurrentLobbyOrGameId(lobbyId);

        System.out.println(creator.getName() + " created lobby: " + lobbyId + " (Max: " + maxPlayers + ")");
        sendMessage(creator, "LOBBY_CREATED:" + lobbyId + ":" + maxPlayers);
        broadcastLobbyList(); // ���r�[���X�g�X�V��S�̂ɒʒm
        return lobbyId;
    }

    /**
     * �����̃Q�[�����r�[�ɎQ������B
     * @param lobbyId �Q�����������r�[��ID
     * @param player �Q������v���C���[
     * @return �Q���ɐ��������ꍇ��true�A���s�����ꍇ��false
     */
    public boolean joinGameLobby(String lobbyId, YachtClientUser player) {
        GameLobby lobby = gameLobbies.get(lobbyId);
        if (lobby != null) {
            if (lobby.addPlayer(player)) {
                player.setCurrentLobbyOrGameId(lobbyId); // �v���C���[�Ɍ��݂̃��r�[ID��ݒ�
                System.out.println(player.getName() + " joined lobby: " + lobbyId + ". Players: " + lobby.getCurrentPlayerCount());
                broadcastToLobby(lobbyId, "PLAYER_JOINED:" + player.getName() + ":" + lobby.getCurrentPlayerCount() + "/" + lobby.getMaxPlayers());
                sendMessage(player, "JOINED_LOBBY:" + lobbyId + ":" + lobby.getCurrentPlayerCount() + "/" + lobby.getMaxPlayers());
                broadcastLobbyList(); // ���r�[���X�g�X�V��S�̂ɒʒm
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
     * ���r�[����ޏo����B
     * @param player �ޏo����v���C���[
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
            gameLobbies.remove(lobbyId); // ���r�[����ɂȂ�����폜
            System.out.println("Lobby " + lobbyId + " is empty and removed.");
        }
        player.setCurrentLobbyOrGameId(null); // �v���C���[�̃��r�[ID�����Z�b�g
        player.setReady(false); // ������Ԃ����Z�b�g
        sendMessage(player, "LEFT_LOBBY:" + lobbyId);
        broadcastLobbyList(); // ���r�[���X�g�X�V��S�̂ɒʒm
    }

    /**
     * �v���C���[�̏������ł������Ƃ��}�[�N���A�S���������ł�����Q�[�����J�n����B
     * @param player �����ł����v���C���[
     * @param lobbyId �Q�����Ă���Q�[�����r�[��ID
     */
    public void playerReady(YachtClientUser player, String lobbyId) {
        GameLobby lobby = gameLobbies.get(lobbyId);
        if (lobby != null && lobby.getPlayers().contains(player)) {
            if (lobby.setPlayerReady(player)) {
                System.out.println(player.getName() + " is READY in lobby: " + lobbyId);
                broadcastToLobby(lobbyId, "PLAYER_READY:" + player.getName());

                if (lobby.areAllPlayersReady()) { // �S��������OK�Ȃ�Q�[���J�n
                    startGame(lobbyId, new ArrayList<>(lobby.getPlayers())); // ���r�[�v���C���[�̃R�s�[��n��
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
     * �Q�[�����J�n����B
     * @param gameId �Q�[����ID (���r�[ID�����̂܂܎g�p)
     * @param players �Q�[���ɎQ������v���C���[���X�g
     */
    private void startGame(String gameId, ArrayList<YachtClientUser> players) {
        
        YachtGame game = new YachtGame(gameId, players);
        activeGames.put(gameId, game); // �i�s���̃Q�[���Ƃ��ēo�^
        gameLobbies.remove(gameId); // ���r�[����폜

        // �Q���v���C���[�S���̖��O���J���}��؂�̕�����ɂ���
        StringBuilder playerNamesBuilder = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            playerNamesBuilder.append(players.get(i).getName());
            // �Ō�̃v���C���[�łȂ���΁A�J���}��ǉ�����
            if (i < players.size() - 1) {
                playerNamesBuilder.append(",");
            }
        }
        String playerNames = playerNamesBuilder.toString();

        for (YachtClientUser player : players) {
            player.setCurrentLobbyOrGameId(gameId);
            player.setReady(false);
            // GAME_STARTED���b�Z�[�W�ɁA�v���C���[�S���̖��O�̃��X�g��ǉ����đ��M
            sendMessage(player, "GAME_STARTED:" + gameId + ":" + playerNames);
        }
        
        // �Q�[���J�n���̍ŏ��̃^�[����ʒm
        YachtClientUser currentPlayer = game.getCurrentPlayer();
        if (currentPlayer != null) {
            broadcastToGame(gameId, "YOUR_TURN:" + currentPlayer.getName());
        }
        broadcastLobbyList();
    }

    /**
     * �Q�[�����I������B
     * @param gameId �I������Q�[����ID
     */
    public void endGame(String gameId, String finalScores) {
        YachtGame game = activeGames.remove(gameId);
        if (game != null) {
            System.out.println("Game " + gameId + " ended.");
            // �Q�[���ɎQ�����Ă����S�v���C���[�ɃQ�[���I���ƍŏI���ʂ�ʒm
            for (YachtClientUser player : game.getPlayerList()) {
                sendMessage(player, "GAME_ENDED:" + finalScores);
                player.setCurrentLobbyOrGameId(null);
                player.setReady(false);
            }
        }
        broadcastLobbyList(); // ���r�[���X�g�X�V��S�̂ɒʒm (�Q�[���I���Ń��r�[�ɋ󂫂��ł��邽��)
    }

    /**
     * ����̃Q�[��ID�Ɋ֘A���郆�[�U�[�Ƀ��b�Z�[�W���u���[�h�L���X�g����B
     * @param gameId �u���[�h�L���X�g�Ώۂ̃Q�[��ID
     * @param message ���M���郁�b�Z�[�W
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
     * ����̃��r�[�Ɋ֘A���郆�[�U�[�Ƀ��b�Z�[�W���u���[�h�L���X�g����B
     * @param lobbyId �u���[�h�L���X�g�Ώۂ̃��r�[ID
     * @param message ���M���郁�b�Z�[�W
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
     * �S�Ẵ��O�C���ς݃��[�U�[�ɗ��p�\�ȃ��r�[���X�g���u���[�h�L���X�g����B
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
        // �S�Ẵ��O�C���ς݃��[�U�[�ɑ��M
        for (YachtClientUser user : connectedUsersByName.values()) {
            // ���r�[�ɂ��Q�[���ɂ��Q�����Ă��Ȃ����[�U�[�ɂ̂ݑ��M
            if (user.getCurrentLobbyOrGameId() == null) {
                sendMessage(user, lobbyListMessage);
            }
        }
    }

    /**
     * ����̃N���C�A���g�ɂ̂݃��r�[���X�g�𑗐M����B
     * @param recipient ���M��̃N���C�A���g
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

    // processClientCommand �� onClientMessageReceived ����Ăяo�����
    // processClientCommand �� onClientMessageReceived ����Ăяo�����
    private void processClientCommand(ClientHandler handler, YachtClientUser sender, String command) {
        String[] parts = command.split(":", 3);//�ő�3����

        String contextId = null;
        String cmd;
        String args = "";

        // �R�}���h��ID�v���t�B�b�N�X�������ǂ������`�F�b�N
        if (parts.length >= 2 && (parts[0].startsWith("LOBBY_") || parts[0].startsWith("GAME_"))) {
            contextId = parts[0]; // �ŏ��̕��������r�[/�Q�[��ID
            cmd = parts[1]; // �����R�}���h
            args = parts.length > 2 ? parts[2] : ""; // �c�肪����
        } else {
            // ID���Ȃ����A��ʓI�ȃT�[�o�[�R�}���h
            cmd = parts[0];
            args = parts.length > 1 ? parts[1] : "";
        }

        // ���O�C�����K�v�ȃR�}���h�ŁAsender��null�̏ꍇ
        if (sender == null && !cmd.equals("LOGIN")) {
            handler.sendMessageToClient("ERROR:You must login first.");
            return;
        }
        
        // ���r�[/�Q�[�����R�}���h�ŁA���� sender �̃R���e�L�X�gID�ƈ�v���Ȃ��ꍇ�̃G���[�`�F�b�N
        if (sender != null && contextId != null) {
            String senderCurrentContextId = sender.getCurrentLobbyOrGameId();
            if (senderCurrentContextId == null || !contextId.equals(senderCurrentContextId)) {
                // �N���C�A���g���Ԉ�����R���e�L�X�gID�ŃR�}���h�𑗐M�����ꍇ
                handler.sendMessageToClient("ERROR:Mismatched context ID. You are in " +
                                            (senderCurrentContextId != null ? senderCurrentContextId : "no context") +
                                            ", but sent command for " + contextId + ".");
                return;
            }
        }

        // ���sID��ݒ�isender�̌��݂̃R���e�L�X�gID��D��j
        String effectiveContextId = (sender != null && sender.getCurrentLobbyOrGameId() != null) ? sender.getCurrentLobbyOrGameId() : contextId;
        
        System.out.println("command : " + cmd + args);
        //�ȉ��ŃR�}���h���Ƃ̏������s�Ȃ��A
        //�T�[�o�ւ̐ڑ���A���O�A�E�g�Ȃǂ̃T�[�o���ŏ������ׂ����̋y�у��r�[�ł̊֐��̓R�R�ŏ������L�q�B
        //�Q�[�����̏����⃍�r�[�ł̏��������Ȃǂ̏����́A�e�Q�[���C���X�^���X�ȂǂɎ󂯓n���B
        switch (cmd) {
            case "LOGIN":
                if (sender != null){
                    if (sender.getName() != null) {
                        sendMessage(sender, "ERROR:You are already logged in or username is set.");
                        System.out.println("ERROR:You are already logged in or username is set.");
                        return;
                    }else{
                        sendMessage(sender,"ERROR:���O��\�ߐݒ肵�Ă���T�[�o�ɐڑ����Ă��������B");
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
                //���̖͂��O������o�Ȃ��Ƃ킩�����̂ŁA���̂Ȃ܂���p���āA�N���C�A���g���[�U��V�K�ɍ쐬���o�^
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
                //�Q�[�����ŌĂяo�����֐���id�����������ǂ��������؂�����A�Q�[���Ɏ󂯓n���B
                if (effectiveContextId == null || !activeGames.containsKey(effectiveContextId)) {
                    sendMessage(sender, "ERROR:You are not in an active game to use this command.");
                    return;
                }
                
                YachtGame game = activeGames.get(effectiveContextId);

                if (!game.getPlayerList().contains(sender)) { // �v���C���[���Q�[���ɎQ�����Ă��Ȃ��ꍇ�̓G���[
                    sendMessage(sender, "ERROR:You are not a player in this game.");
                    return;
                }

                if (!cmd.equals("FH_OR_STRAIGHT_ROLL") && !cmd.equals("IKASAMA_ROLL") && !game.getCurrentPlayer().equals(sender)) {
                    sendMessage(sender, "ERROR:It's not your turn.");
                    return;
                }
                
                GameCommandResult gameCommandResult = game.handleGameCommand(sender, cmd, args);
                
                // GameCommandResult���烁�b�Z�[�W���X�g���擾
                List<GameCommandResult.MessageToSend> messages = gameCommandResult.getMessages();
                
                // �ŏ��̃��b�Z�[�W��GAME_OVER���ǂ�������ʂɃ`�F�b�N
                if (!messages.isEmpty() && messages.get(0).message.startsWith("GAME_OVER")) {
                    String finalScores = messages.get(0).message.split(":", 2)[1];
                    endGame(effectiveContextId, finalScores); // endGame���\�b�h�ɍŏI�X�R�A��n��
                    return; // ����ȍ~�̏����͂��Ȃ�
                }
        
                // GameCommandResult���烁�b�Z�[�W���X�g���擾���A�K�؂ɑ��M
                for (GameCommandResult.MessageToSend msg : gameCommandResult.getMessages()) {
                    if (msg.sendToAll) {
                        // �S���ɑ��M����ꍇ
                        for (YachtClientUser player : game.getPlayerList()) {
                            sendMessage(player, msg.message);
                        }
                    } else {
                        // ����̑��M�ҁi����̏ꍇ�̓R�}���h�𔭍s����sender�j�ɂ̂ݑ��M����ꍇ
                        sendMessage(sender, msg.message);
                    }
                }
                
                break;

            case "LOGOUT":
                // removeConnectedUser��onClientDisconnected����Ă΂��̂ŁA�����ł͕s�v
                // Socket��close��removeConnectedUser�ŏ��������
                sendMessage(sender, "LOGGED_OUT:Goodbye!"); // �N���C�A���g�Ƀ��O�A�E�g������ʒm
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
            // �ڑ����̑S�N���C�A���g�n���h�����N���[�Y
            // synchronized (handlerSockets) { // handlerSockets�͊���synchronizedMap�Ȃ̂ŕs�v
                for (ClientHandler handler : new ArrayList<>(handlerSockets.keySet())) { // ConcurrentModificationException����̂��߃R�s�[
                    try {
                        handler.close(); // ClientHandler��close���Ăяo��
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
            clientThreadPool.shutdownNow(); // �X���b�h�v�[�����V���b�g�_�E��
            System.out.println("Client thread pool shut down.");
        }
    }

    public static void main(String[] args) {
        YachtServer server = YachtServer.getInstance();
        server.start();
    }
}

