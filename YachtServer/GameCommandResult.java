import java.net.*;
import java.util.*;
import java.io.*;
/**
 *  class GameCommandResult
 *  
 *  �Q�[�����ŌĂяo���R�}���h�̌��ʗp�̃N���X
 *  �R�}���h������ɓ��삵�����ǂ����{���ʂ̃e�L�X�g -2025/6/6
 *  �����̃R�}���h���󂯎�������̏����ǉ� -2025/6/16
 *
 * @author Hiroaki Kaji
 * @version 2025/6/16
 */
class GameCommandResult {
    
    // �����N���X: ���M���郁�b�Z�[�W�Ƃ��̑��M����`
    public static class MessageToSend {
        public final String message;
        public final boolean sendToAll; // true�Ȃ�S���ɁAfalse�Ȃ猳��sender�ɑ��M

        public MessageToSend(String message, boolean sendToAll) {
            this.message = message;
            this.sendToAll = sendToAll;
        }
    }

    public final boolean success;
    private final List<MessageToSend> messages; // �����̃��b�Z�[�W��ێ�

    // �R���X�g���N�^�i�P��̃��b�Z�[�W��sender�ɕԂ��ꍇ�j
    public GameCommandResult(boolean success, String message) {
        this.success = success;
        this.messages = new ArrayList<>();
        this.messages.add(new MessageToSend(message, false)); // �f�t�H���g��sender�ɂ̂ݑ��M
    }

    // �R���X�g���N�^�i�����̃��b�Z�[�W��Ԃ��ꍇ�j
    private GameCommandResult(boolean success, List<MessageToSend> messages) {
        this.success = success;
        this.messages = messages;
    }

    // �V�����ÓI���\�b�h: �����̃��b�Z�[�W�������ʂ��쐬
    public static GameCommandResult createMultiMessageResult(boolean success, List<MessageToSend> messages) {
        return new GameCommandResult(success, messages);
    }

    // ���b�Z�[�W���X�g���擾����Q�b�^�[
    public List<MessageToSend> getMessages() {
        return messages;
    }
}