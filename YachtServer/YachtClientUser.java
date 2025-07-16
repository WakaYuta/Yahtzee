import java.net.*;
import java.util.*;
import java.io.*;

/**
 * ���b�g�̃N���C�A���g���[�U�N���X
 * YachtClientUser 
 * 
 * @author Hiroaki Kaji
 * @version 2025/06/10
 */

class YachtClientUser{
    //�\�P�b�g
    private Socket socket;

    //���[�U�[�̖��O
    private String name;

    //�`���b�g�T�[�o�[
    private YachtServer server = YachtServer.getInstance();
    
    // ClientHandler�ւ̎Q�Ƃ������ƂŁAsendMessage��YachtClientUser���璼�ڌĂяo����悤�ɂ���
    private ClientHandler clientHandler; // ������ǉ�
    
    // ���r�[id�Ǘ��Ə�������bool
    private String currentLobbyOrGameId; // ���ݎQ�����Ă��郍�r�[�܂��̓Q�[����ID
    private boolean isReady; // ���r�[�ŏ���������Ԃ�
    
    public YachtClientUser(String name, ClientHandler handler) {
        this.name = name;
        this.clientHandler = handler;
        this.currentLobbyOrGameId = null;
        this.isReady = false;
    }
    // ClientHandler�ւ̎Q�Ƃ��Z�b�g/�Q�b�g���郁�\�b�h
    public ClientHandler getClientHandler() {
        return clientHandler;
    }
    //�T�[�o�ƒʐM���s���T�[�o�Ɠ��������Ă��������C���X�^���X�̃Q�b�^�ƃZ�b�^
    //���O
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    // �Q�����Ă���Q�[��id
    public void setCurrentLobbyOrGameId(String gameID) {
        this.currentLobbyOrGameId = gameID;
    }
    public String getCurrentLobbyOrGameId(){
        return this.currentLobbyOrGameId;
    }
    //���r�[�ł̏������    
    public void setReady(boolean ReadyState){
        this.isReady = ReadyState;
    }
    public boolean getReady(){
        return this.isReady;
    }

    public String toString() {
        return "NAME=" + getName();
    }
}