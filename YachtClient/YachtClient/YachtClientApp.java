/**
 * アプリケーションを起動するためのメインクラス
 */

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.swing.plaf.FontUIResource;


public class YachtClientApp {
    public static void main(String[] args) {
        try {
            // モダンデザインを適用
            UIManager.setLookAndFeel( new com.formdev.flatlaf.FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println( "Look and Feel の設定に失敗しました" );
        }
        //日本語カスタムフォントの読み込みと設定
        try {
            String fontFileName = "MPLUSRounded1c-Regular.ttf";
            
            InputStream is = YachtClientApp.class.getResourceAsStream("fonts/" + fontFileName);
            
            if (is == null) {
                System.err.println(fontFileName + " が見つかりません。'fonts'フォルダに正しく配置されているか確認してください。");
            } else {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f); // 基本のフォントサイズを14に設定
    
                // アプリケーション全体のフォントを、読み込んだカスタムフォントに設定する
                setGlobalFont(new FontUIResource(customFont));
            }

        } catch (IOException | FontFormatException e) {
            System.err.println("フォントの読み込みに失敗しました");
            e.printStackTrace();
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
    /**
     * Swingアプリケーション全体のデフォルトフォントを変更するためのメソッド
     */
    private static void setGlobalFont(FontUIResource font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
