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
        private YachtClientUser user; // ���̃n���h�����Ǘ�����YachtClientUser�C���X�^���X
        private ClientMessageListener listener; 
        private PrintWriter out; // ���b�Z�[�W���M�p��PrintWriter��ǉ�
        private BufferedReader in; // ���b�Z�[�W��M�p��BufferedReader��ǉ�
        
        public ClientHandler(Socket clientSocket, ClientMessageListener listener) {
            this.clientSocket = clientSocket;
            this.listener = listener;
            try {
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error setting up client handler: " + e.getMessage());
                close(); // ���������s�����\�P�b�g�����
            }
        }
        // ���O�C����������YachtClientUser���Z�b�g����Z�b�^�[
        public void setUser(YachtClientUser user) {
            this.user = user;
        }
        
        // �n���h���ɕR�t�����Ă���YachtClientUser��Ԃ��Q�b�^
        public YachtClientUser getUser() {
            return this.user;
        }
    
        // �N���C�A���g�\�P�b�g���擾����Q�b�^�[ (YachtServer�̃}�b�v�Ǘ��ŕK�v)
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
                    
                    // ��M�������b�Z�[�W�����X�i�[�iYachtServer�j�ɃR�[���o�b�N
                    // ���b�Z�[�W�𑗐M����ClientHandler���g�������Ƃ��ēn��
                    listener.onClientMessage(this, clientMessage);
                }
            } catch (IOException e) {
                // �N���C�A���g���ؒf�����ꍇ��IOException�͈�ʓI $#$
                if (!clientSocket.isClosed()) { // ����Ȑؒf�łȂ���΃G���[�Ƃ��ă��O
                    System.err.println("Error reading from client " + (user != null ? user.getName() : "unauthenticated client") + " (" + clientAddress + "): " + e.getMessage());
                    // �ڑ�������ꂽ���Ƃ����X�i�[�ɒʒm
                    listener.onClientDisconnected(this, "Error: " + e.getMessage()); // ������ύX
                } else {
                    // ����Ȑؒf�̏ꍇ
                    System.out.println((user != null ? user.getName() : "Unauthenticated client") + " disconnected gracefully.");
                    listener.onClientDisconnected(this, "Client disconnected gracefully."); // ������ύX
                }
            } finally {
                // �X���b�h���I������ہA�N���C�A���g�̃N���[���A�b�v�̓T�[�o�[���ŐӔC������
                // onClientDisconnected�ŃT�[�o�[�ɔC���邽�߁A�����ł͖����I�ɃN���[�Y���Ȃ�
                // �������AonClientDisconnected���Ă΂�邱�Ƃ�ۏ؂��邽�߁A�����ŌĂ΂Ȃ�
                // �N���[�Y������onClientDisconnected�ōs����悤�ɂ���
            }
        }
        
        // �N���C�A���g�փ��b�Z�[�W�𑗐M���郁�\�b�h
        public void sendMessageToClient(String message) {
            if (out != null) {
                out.println(message);
            }
        }
        
        // �\�P�b�g�ƃX�g���[�������S�ɃN���[�Y���郁�\�b�h
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