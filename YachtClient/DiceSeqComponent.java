
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * class DiceSeqComponent
 * 
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */

// --- 新しいリスナーインターフェースの定義 ---
interface DiceClickListener {
    void diceClicked(DiceSeqComponent dice);
}
// ---

/**
 * class DiceSeqComponent
 * 純粋にサイコロの表示とアニメーション、ロック状態を管理するコンポーネント。
 * クリック時のゲームロジック（ロックのトグルなど）は外部に通知する。
 *
 * @author Hiroaki Kaji
 * @version 2025/6/7
 */

public class DiceSeqComponent extends JPanel {
    private int currentValue; // 現在のサイコロの目
    private boolean isLocked; // ロックされているか
    // private boolean isMyTurn; // 削除: このクラスでは「自分の番か」というゲーム状態を持たない

    private Timer animationTimer; // アニメーション用タイマー
    public int animationDurationMs; // アニメーションの継続時間 (ミリ秒)
    private long animationStartTime; // アニメーション開始時刻
    private int animationFrameIndex; // 現在のアニメーションフレームのインデックス

    private Map<Integer, BufferedImage> diceFaceImages; // 各サイコロの目に対応する画像 (1-6)
    private BufferedImage[] animationImages; // サイコロが回転しているアニメーションの画像シーケンス

    private DiceClickListener diceClickListener; // 削除したisMyTurnの代わりに、クリックイベントを外部に通知するためのリスナー

    private static final int DICE_SIZE = 80; // サイコロのコンポーネントサイズ (画像サイズに合わせる)
    private static final Color LOCKED_COLOR = new Color(255, 255, 150); // ロック時の背景色
    private static final Color DEFAULT_COLOR = Color.WHITE; // デフォルト背景色
    private static final Border LOCKED_BORDER = BorderFactory.createLineBorder(Color.RED, 3); // ロック時のボーダー
    private static final Border DEFAULT_BORDER = BorderFactory.createLineBorder(Color.GRAY, 1); // デフォルトボーダー
    // クリック可能状態を示すためのボーダーを追加
    private static final Border CLICKABLE_BORDER = BorderFactory.createLineBorder(Color.BLUE, 2);


    /**
     * DiceSeqComponentのコンストラクタ
     * @param animationDurationMs アニメーションの継続時間 (ミリ秒)
     * @param imagePathPrefix サイコロの画像ファイルへのパスのプレフィックス (例: "images/dice_")
     */
    public DiceSeqComponent(int animationDurationMs, String imagePathPrefix) {
        this.currentValue = 1; // 初期値は1
        this.isLocked = false;
        // this.isMyTurn = false; // 削除
        this.animationDurationMs = animationDurationMs;
        this.animationFrameIndex = 0;

        setPreferredSize(new Dimension(DICE_SIZE, DICE_SIZE));
        setOpaque(true); // 背景色を描画するために必要
        setBorder(DEFAULT_BORDER);
        setBackground(DEFAULT_COLOR);

        // 画像の読み込み
        loadDiceImages(imagePathPrefix);

        // クリックリスナーの追加
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // クリックされたらリスナーに通知する
                if (diceClickListener != null && !isAnimating()) { // アニメーション中でない場合のみ通知
                    diceClickListener.diceClicked(DiceSeqComponent.this);
                }
            }
        });
    }

    /**
     * サイコロの目とアニメーション画像を読み込みます。
     * @param prefix 画像ファイルパスのプレフィックス
     */
    private void loadDiceImages(String prefix) {
        diceFaceImages = new HashMap<>();
        animationImages = new BufferedImage[20]; // 20枚のアニメーション画像

        try {
            // 各サイコロの目の画像 (dice_1.png から dice_6.png)
            for (int i = 1; i <= 6; i++) {
                String path = prefix + i + ".png";
                diceFaceImages.put(i, ImageIO.read(new File(path)));
            }

            // アニメーション画像 (dice_anim_01.png から dice_anim_20.png)
            for (int i = 1; i <= 20; i++) {
                String paddedIndex = String.format("%02d", i); // 01, 02 のようにゼロパディング
                String path = prefix + "anim_" + paddedIndex + ".png";
                animationImages[i - 1] = ImageIO.read(new File(path));
            }
        } catch (IOException e) {
            System.err.println("Error loading dice images: " + e.getMessage());
            BufferedImage errorImage = new BufferedImage(DICE_SIZE, DICE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = errorImage.createGraphics();
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, DICE_SIZE, DICE_SIZE);
            g2d.setColor(Color.WHITE);
            g2d.drawString("ERROR", 10, DICE_SIZE / 2);
            g2d.dispose();
            for (int i = 1; i <= 6; i++) diceFaceImages.put(i, errorImage);
            for (int i = 0; i < 20; i++) animationImages[i] = errorImage;
        }
    }

    /**
     * このサイコロがクリックされたときに通知されるリスナーを設定します。
     * @param listener 設定するリスナー
     */
    public void setDiceClickListener(DiceClickListener listener) {
        this.diceClickListener = listener;
    }

    /**
     * サイコロの目を設定し、再描画します。
     * @param value 設定するサイコロの目 (1-6)
     */
    public void setDiceValue(int value) {
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Dice value must be between 1 and 6.");
        }
        this.currentValue = value;
        repaint(); // UIを更新
    }

    /**
     * サイコロがロックされているかどうかを設定します。
     * @param locked ロック状態
     */
    public void setLocked(boolean locked) {
        this.isLocked = locked;
        updateAppearance();
    }

    /**
     * サイコロがロックされているかどうかを返します。
     * @return ロックされていれば true
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * サイコロがクリック可能かどうかを設定します。
     * クリック可能であれば、見た目にその変化が反映されます。
     * @param clickable クリック可能であれば true
     */
    public void setClickable(boolean clickable) {
        // isMyTurnの代わりに、純粋にUIのクリック可能状態を管理する
        // ロック状態とは別に、クリックできることを視覚的に示すために利用
        if (clickable) {
            // ロックされている場合はロックボーダー優先、そうでなければクリック可能ボーダー
            if (!isLocked) { // ロックされていなければクリック可能ボーダー
                setBorder(CLICKABLE_BORDER);
            }
        } else {
            // クリック不可能になったら、ロック状態に応じてボーダーを更新
            updateAppearance(); // ロック状態が優先される
        }
    }


    /**
     * サイコロのアニメーションを開始します。
     * アニメーション中はランダムな目を表示し、指定された時間が経過したら停止します。
     */
    public void startAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            return; // 既にアニメーション中であれば何もしない
        }
        isLocked = false; // アニメーション開始時はロック解除
        updateAppearance(); // ロック解除の見た目を反映

        animationStartTime = System.currentTimeMillis();
        animationFrameIndex = 0; // アニメーション開始時にフレームをリセット

        // アニメーションタイマー。約30msごとに目を更新 (画像の切り替え速度)
        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsedTime = System.currentTimeMillis() - animationStartTime;
                if (elapsedTime < animationDurationMs) {
                    // アニメーション画像シーケンスをループ表示
                    animationFrameIndex = (animationFrameIndex + 1) % animationImages.length;
                    repaint();
                } else {
                    // 指定時間が経過したらアニメーション停止
                    stopAnimation();
                }
            }
        });
        animationTimer.start();
    }

    /**
     * サイコロのアニメーションを停止します。
     * 最終的な目を表示するために setDiceValue を呼び出す必要があります。
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        repaint(); // 最終的な目を表示するために再描画
    }

    /**
     * アニメーション中かどうかを返します。
     * @return アニメーション中であれば true
     */
    public boolean isAnimating() {
        return animationTimer != null && animationTimer.isRunning();
    }

    // ロック状態に応じて見た目を更新します。
    private void updateAppearance() {
        if (isLocked) {
            setBackground(LOCKED_COLOR);
            setBorder(LOCKED_BORDER);
        } else {
            setBackground(DEFAULT_COLOR);
            setBorder(DEFAULT_BORDER);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage imageToDraw = null;

        if (isAnimating() && animationImages[animationFrameIndex] != null) {
            // アニメーション中はアニメーション画像を連続表示
            imageToDraw = animationImages[animationFrameIndex];
        } else {
            // アニメーション停止中は現在のサイコロの目の画像を表示
            imageToDraw = diceFaceImages.get(currentValue);
        }

        if (imageToDraw != null) {
            g2d.drawImage(imageToDraw, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.drawString("N/A", getWidth() / 2 - 15, getHeight() / 2);
        }
    }

}
