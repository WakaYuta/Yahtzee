/**
 * アプリケーションを起動するためのメインクラス
 */

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class YachtClientApp {
    public static void main(String[] args) {
        try {
            // モダンなフラットデザイン（明るいテーマ）を適用
            UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println( "Look and Feel の設定に失敗しました" );
        }
        
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            YachtClientView view = new YachtClientView();
            YachtClientController controller = new YachtClientController(model, view);

            view.setController(controller);
            
            view.setSize(900, 600);

            view.setVisible(true);
        });
    }
}
