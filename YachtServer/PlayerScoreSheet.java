import java.net.*;
import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * PlayerScoreSheet クラス
 * 特定のプレイヤーのカテゴリごとのスコアと、
 * そのカテゴリが記入済みであるかのフラグをカプセル化します。
 * 
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
public class PlayerScoreSheet {
    // 各カテゴリのスコア (EnumMap は列挙型をキーとする場合に効率的です)
    private Map<yachtCategory, Integer> categoryScores;
    // 各カテゴリが記入済みであるかのフラグ
    private Map<yachtCategory, Boolean> categoryRecorded;

    /**
     * PlayerScoreSheet のコンストラクタ。
     * 全てのカテゴリのスコアを0、記入済みフラグをfalseで初期化します。
     */
    public PlayerScoreSheet() {
        this.categoryScores = new EnumMap<>(yachtCategory.class);
        this.categoryRecorded = new EnumMap<>(yachtCategory.class);

        // 全ての役を初期化
        for (yachtCategory category : yachtCategory.values()) {
            this.categoryScores.put(category, 0); // 初期スコアは0
            this.categoryRecorded.put(category, false); // 初期は未記入
        }
    }

    /**
     * 指定されたカテゴリのスコアを取得します。
     * @param category スコアを取得するYachtCategory
     * @return 該当カテゴリのスコア
     */
    public int getScore(yachtCategory category) {
        return categoryScores.getOrDefault(category, 0);
    }

    /**
     * 指定されたカテゴリのスコアを設定します。
     * スコアが既に記入済みでない場合にのみ設定できます。
     * @param category スコアを設定するYachtCategory
     * @param score 設定するスコア
     * @return スコアが正常に設定された場合はtrue、既に記入済みで設定できなかった場合はfalse
     */
    public boolean setScore(yachtCategory category, int score) {
        if (isRecorded(category)) {
            System.out.println("Error: Score for " + category + " has already been recorded.");
            return false;
        }
        this.categoryScores.put(category, score);
        this.categoryRecorded.put(category, true); // 記入済みとマーク
        return true;
    }

    /**
     * 指定されたカテゴリのスコアが既に記入済みであるかを確認します。
     * @param category 確認するYachtCategory
     * @return 記入済みであればtrue、そうでなければfalse
     */
    public boolean isRecorded(yachtCategory category) {
        return categoryRecorded.getOrDefault(category, false);
    }

    /**
     * 全てのカテゴリの合計スコアを計算して取得します。
     * @return 合計スコア
     */
    public int getTotalScore() {
        return categoryScores.values().stream()
                             .mapToInt(Integer::intValue)
                             .sum();
    }

    /**
     * 全てのカテゴリとスコアを文字列として出力。（デバッグ用）
     * @return スコアシートの文字列表現
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Score Sheet:\n");
        for (yachtCategory category : yachtCategory.values()) {
            sb.append("  ").append(category.name())
              .append(": ").append(getScore(category))
              .append(isRecorded(category) ? " (Recorded)" : " (Unrecorded)")
              .append("\n");
        }
        sb.append("Total Score: ").append(getTotalScore());
        return sb.toString();
    }
}