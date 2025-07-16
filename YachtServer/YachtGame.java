import java.net.*;
import java.util.*;
import java.io.*;

/**
 * �N���X YachtGameManager �̒��߂������ɏ����܂�.
 * 
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
public class YachtGame {
    private String gameId; // �Q�[������ӂɎ��ʂ���ID
    private ArrayList<YachtClientUser> playerList; // ���̃Q�[���ɎQ�����Ă���v���C���[
    private Map<YachtClientUser, PlayerScoreSheet> playerScores;
    private int currentPlayerIndex; // ���݂̃^�[���̃v���C���[�C���f�b�N�X
    private int currentRound; // current round num
    private int[] diceRolls; // �T�C�R���̏o�ځi��: 5�̃T�C�R���j
    private int remainRollCount; // ���݂̃^�[���̃T�C�R����U������

    // ScoreCalculater �̃C���X�^���X
    private ScoreCalculater scoreCalculater;

    public YachtGame(String gameId, ArrayList<YachtClientUser> playerList) {
        this.gameId = gameId;
        this.playerList = new ArrayList<>(playerList);

        this.playerScores = new HashMap<>();

        this.currentPlayerIndex = 0; // �ŏ��̃v���C���[����J�n
        this.currentRound = 0; // current round
        this.diceRolls = new int[5]; // 5�̃T�C�R��
        this.remainRollCount = 3;

        this.scoreCalculater = new ScoreCalculater(); // ScoreCalculater�̃C���X�^���X�𐶐�
        
        // ������playerList�Ɋ�Â��ăv���C���[��������
        for (YachtClientUser player : playerList) {
            addPlayer(player);
        }
    }

    /**
     * �V�����v���C���[���Q�[���ɒǉ����APlayerScoreSheet �����������܂��B
     * @param player �ǉ�����v���C���[
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
    // --- �Q�[���i�s�Ɋւ��郁�\�b�h ---

    public YachtClientUser getCurrentPlayer() {
        if (playerList.isEmpty()) {
            return null;
        }
        return playerList.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if(currentPlayerIndex + 1 >= playerList.size()){
            // ���̃��E���h��
            // �ŏI���E���h���I��������ENDGAME()��
            // �J�e�S���� == ���E���h��
            if (currentRound == yachtCategory.values().length){
                // �ŏI���E���h���I��
                System.out.println("�Q�[���I���I"); // ��
            } else {
                currentPlayerIndex = 0; // �ŏ��̃v���C���[�ɖ߂�
            }
            currentRound++;
        } else {
            currentPlayerIndex = currentPlayerIndex + 1;
        }
        this.remainRollCount = 3; // ���̃^�[���ł̓T�C�R����U��񐔂����Z�b�g
        this.diceRolls = new int[5]; // �T�C�R���̏o�ڂ����Z�b�g
    }

    /**
     * rollDice
     *
     * @params String �T�C�R����U�邩�ǂ�����L��R�ŕ\����������
     * @returns String �T�C�R���̏o��
     */
    public int[] rollDice(String LorR) {
        // �c��U��񐔂�0�ȉ��̏ꍇ�̃`�F�b�N���C��
        if (remainRollCount <= 0) { // �C��: >= 0 ���� <= 0 ��
            System.out.println("�����T�C�R���͐U��܂���B");
            return diceRolls; // ���݂̏o�ڂ�Ԃ�
        }

        MagicRollController mrcontroller = new MagicRollController();
        
        diceRolls = mrcontroller.rolldices(diceRolls,LorR);

        remainRollCount--;
        System.out.println("�T�C�R����U��܂���: " + Arrays.toString(diceRolls) + " (�c�� " + (remainRollCount) + "��)");
        return diceRolls;
    }

    /**
     * �����z����J���}��؂�̕�����ɕϊ����܂��B
     * ��: {1, 5, 2, 6, 3} -> "1,5,2,6,3"
     *
     * @param diceRolls �ϊ����鐮���z��
     * @return �����z����J���}��؂�ŕ\������������
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
     * ���݂̃v���C���[�́A�w�肳�ꂽ���̃X�R�A���v�Z���A�X�R�A�V�[�g�ɋL�����܂��B
     *
     * @param category �X�R�A���L������YachtCategory
     * @return �X�R�A������ɋL�����ꂽ�ꍇ��true�A���ɋL���ς݂Őݒ�ł��Ȃ������ꍇ��false
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

        // �X�R�A�����ɋL���ς݂łȂ������m�F
        if (scoreSheet.isRecorded(category)) {
            System.out.println(currentPlayer.getName() + ": Score for " + category + " is already recorded.");
            return false;
        }

        // ScoreCalculater ���g���ăX�R�A���v�Z
        int calculatedScore = scoreCalculater.calcScore(category, this.diceRolls);

        // PlayerScoreSheet ����ăX�R�A��ݒ�
        boolean success = scoreSheet.setScore(category, calculatedScore);
        if (success) {
            System.out.println(currentPlayer.getName() + " recorded " + calculatedScore + " points for " + category + ".");
        }
        return success;
    }

    /**
     * ����̃v���C���[�̍��v�X�R�A���擾���܂��B
     * @param player �X�R�A���擾����v���C���[
     * @return �v���C���[�̍��v�X�R�A
     */
    public int getPlayerTotalScore(YachtClientUser player) {
        PlayerScoreSheet scoreSheet = playerScores.get(player);
        if (scoreSheet != null) {
            return scoreSheet.getTotalScore();
        }
        return 0; // �v���C���[��������Ȃ��ꍇ
    }

    /**
     * ����̃v���C���[�̎w�肳�ꂽ�J�e�S���̃X�R�A���擾���܂��B
     * @param player �X�R�A���擾����v���C���[
     * @param category �X�R�A���擾����J�e�S��
     * @return �Y���J�e�S���̃X�R�A
     */
    public int getPlayerCategoryScore(YachtClientUser player, yachtCategory category) {
        PlayerScoreSheet scoreSheet = playerScores.get(player);
        if (scoreSheet != null) {
            return scoreSheet.getScore(category);
        }
        return 0; // �v���C���[�܂��̓J�e�S����������Ȃ��ꍇ
    }
    
    //���[�U�̐ؒf���̏���
    /**
     * �Q�[������v���C���[���폜���܂��B
     * @param player �폜����v���C���[
     * @return �Q�[�����p���\�ł��邩 (2�l�ȏ�c���Ă��邩)
     */
    public boolean removePlayer(YachtClientUser player) {
        System.out.println("Attempting to remove player: " + player.getName() + " from game " + gameId);
        int removedIdx = playerList.indexOf(player);

        playerList.remove(player);
        playerScores.remove(player);
        System.out.println(player.getName() + " removed. Current players: " + playerList.size());
        
        if (removedIdx != -1) {
            // �v���C���[���X�g����폜���ꂽ�ꍇ�� currentPlayerIndex �̒���
            if (playerList.size() <= 1) {
                //���[�U����1�ȉ��ɂȂ�����Q�[�������I���ɂȂ邪�C���f�b�N�X�𒴂���Ɩʓ|�Ȃ̂ōX�V
                currentPlayerIndex = -1;
            } else if (removedIdx <= currentPlayerIndex) {
                // �폜���ꂽ�v���C���[�����݂̃v���C���[���A������O�̃v���C���[�������ꍇ
                // �C���f�b�N�X�������\��������̂Œ���
                currentPlayerIndex = currentPlayerIndex % playerList.size(); 
                // �������݂̃v���C���[���폜���ꂽ�ꍇ�A���̃v���C���[�Ƀ^�[����n���K�v������B
                // �����YachtServer����leaveGame�ŏ�������B
            }
        } else {
            System.out.println(player.getName() + " was not found in game " + gameId + " player list.");
        }
        
        //���X�g���X�V������ŁA�p���\����Ԃ��B
        return isGameContinuable();
    }

    /**
     * �Q�[�����p���\���ǂ����𔻒肵�܂��B
     * �v���C���[��2�l�ȏ�c���Ă���ꍇ�Ɍp���\�Ƃ��܂��B
     * @return �Q�[�����p���\�ł���� true�A�����łȂ���� false
     */
    public boolean isGameContinuable() {
        return playerList.size() >= 2;
    }
    
    /**
     * �S�Ẵv���C���[���S�ẴJ�e�S���𖄂߂���Q�[���I���Ɣ��f����B
     * @return �Q�[�����I�����Ă����true
     */
    public boolean isGameOver() {
        if (isGameContinuable()) return true; // �v���C���[��l�����ɂȂ�����I��

        for (PlayerScoreSheet scoreSheet : playerScores.values()) {
            for (yachtCategory category : yachtCategory.values()) {
                if (!scoreSheet.isRecorded(category)) {
                    return false; // ���L���̃J�e�S��������΃Q�[���͏I����Ă��Ȃ�
                }
            }
        }
        return true; // �S�����S�ċL���ς݂Ȃ�Q�[���I��
    }

    /**
     * ���̃Q�[���C���X�^���X�ɑ΂���R�}���h����������B
     * @param sender �R�}���h�𑗐M�����v���C���[
     * @param gameCommand �Q�[���ŗL�̃R�}���h������ (��: "ROLL_DICE")
     * @param arguments �R�}���h�̈���
     * @return �����̌��� (����/���s�A�K�v�Ȃ�f�[�^)
     */
    public GameCommandResult handleGameCommand(YachtClientUser sender, String gameCommand, String arguments) {
        // ���݂̃v���C���[���L���ȃv���C���[���X�g���ɂ��邩���m�F
        if (playerList.isEmpty() || !playerList.contains(sender)) {
            return new GameCommandResult(false, "ERROR:PLAYER_NOT_IN_GAME");
        }

        // ���̃Q�[���̌��݂̃^�[���v���C���[�ł��邩�`�F�b�N (�d�v!)
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
                    
                    boolean success = recordScore(category); // �X�R�A���L�^
                    
                    if (success) {
                        this.nextTurn(); // ���̃^�[���ֈڍs
                        String nextPlayerName = getCurrentPlayer().getName(); // ���̃v���C���[�����擾
    
                        List<GameCommandResult.MessageToSend> messages = new ArrayList<>();
                        // 1. �X�R�A�L�^�����̃��b�Z�[�W (���M�҂݂̂�)
                        messages.add(new GameCommandResult.MessageToSend(
                            "SCORE_RECORDED:" + category.name() + ":" + getPlayerCategoryScore(sender, category) + ":TOTAL_SCORE:" + getPlayerTotalScore(sender),
                            false // sender�ɂ̂ݑ��M
                        ));
                        // 2. ���̃^�[���ʒm�̃��b�Z�[�W (�S����)
                        messages.add(new GameCommandResult.MessageToSend(
                            "YOUR_TURN:" + nextPlayerName,
                            true // �S���ɑ��M
                        ));
    
                        return GameCommandResult.createMultiMessageResult(true, messages); // �����̃��b�Z�[�W��Ԃ�
                    } else {
                        return new GameCommandResult(false, "ERROR:COULD_NOT_RECORD_SCORE");
                    }
                } catch (IllegalArgumentException e) {
                    return new GameCommandResult(false, "ERROR:INVALID_CATEGORY:" + arguments);
                }

            case "GET_DICE": // ���݂̃T�C�R���̖ڂ��擾
                return new GameCommandResult(true, "CURRENT_DICE:" + convertDiceRollsToText(getDices()));

            case "GET_SCORE_SHEET": // ���g�̃X�R�A�V�[�g���擾
                PlayerScoreSheet sheet = getPlayerScoreSheet(sender);
                if (sheet != null) {
                    return new GameCommandResult(true, "SCORE_SHEET_UPDATE:" + sender.getName() + ":" + sheet.toString());
                } else {
                    return new GameCommandResult(false, "ERROR:SCORE_SHEET_NOT_FOUND");
                }

            // ����`�֐��̏ꍇ
            default:
                return new GameCommandResult(false, "ERROR:UNKNOWN_GAME_COMMAND:" + gameCommand);
        }
    }
    
    public PlayerScoreSheet getPlayerScoreSheet(YachtClientUser player) {
        return playerScores.get(player);
    }

    /**
     * ���݂̃v���C���[�̃X�R�A�V�[�g��\���B�i�f�o�b�O�p�j
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
