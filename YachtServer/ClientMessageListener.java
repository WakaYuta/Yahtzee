
/**
 * interface ClientMessageListener here.
 * 
 * @author Hiroaki�@Kaji
 * @version 2025/6/6
 */
interface ClientMessageListener {
    /**
     * �N���C�A���g���烁�b�Z�[�W����M�����Ƃ��ɌĂяo�����R�[���o�b�N���\�b�h
     * @param sender ���b�Z�[�W�𑗐M����ClientHandler
     * @param fullMessage ��M�������S�ȃ��b�Z�[�W������
     */
    void onClientMessage(ClientHandler senderHandler, String fullMessage);

    /**
     * �N���C�A���g�ڑ�������ꂽ�Ƃ��ɌĂяo�����R�[���o�b�N���\�b�h
     * @param user �ڑ�������ꂽClientHandler
     * @param reason �ڑ��r���̗��R
     */
    void onClientDisconnected(ClientHandler disconnectedHandler, String reason);
}