import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * class ScoreCalculater.
 * サイコロの目の整数の配列 int[5]を受け取って、
 * 各役ごとのスコアを計算し、Enum YachtCategoryの役とそのスコアを変えすための計算用クラス。
 * 
 *
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
public class ScoreCalculater {

    /**
     * 指定されたYachtCategoryとサイコロの目に基づいてスコアを計算します。
     * このメソッドは、各役の具体的なスコア計算メソッドを呼び出します。
     * サイコロの目のデフォルト値として0を入れている(intの初期化時)
     * そこで、すべてが0のときは初期状態なので、スコアはすべて0で返すように設定している。
     *
     * @param category スコアを計算するYachtCategory
     * @param diceRolls サイコロの目 (5個の整数配列)
     * @return 計算されたスコア
     * @throws IllegalArgumentException diceRollsがnullまたは長さが5ではない場合
     */
    public int calcScore(yachtCategory category, int[] diceRolls) {
        if (diceRolls == null || diceRolls.length != 5) {
            throw new IllegalArgumentException("サイコロの目は5個の配列である必要があります。");
        }
        //出目が全て0の初期状態であれば、スコアは0
        boolean allZeros = true;
        for (int die : diceRolls) {
            if (die != 0) {
                allZeros = false;
                break;
            }
        }
        if (allZeros) {
            // サイコロの目が全て0の場合、全ての役のスコアは0とする
            return 0;
        }

        // スコア計算に役立つ補助的なデータ（ソート済み配列と目の出現回数）を準備
        int[] sortedDice = Arrays.copyOf(diceRolls, diceRolls.length);
        Arrays.sort(sortedDice); // 例: [1, 2, 3, 4, 5] 昇順にしておく。
        
        //サイコロの目とその出た数をマップ化
        Map<Integer, Integer> counts = new HashMap<>();
        for (int die : diceRolls) {
            counts.put(die, counts.getOrDefault(die, 0) + 1);
        }
        

        switch (category) {
            case ONES:
                return calculateOnes(diceRolls);
            case TWOS:
                return calculateTwos(diceRolls);
            case THREES:
                return calculateThrees(diceRolls);
            case FOURS:
                return calculateFours(diceRolls);
            case FIVES:
                return calculateFives(diceRolls);
            case SIXES:
                return calculateSixes(diceRolls);
            case CHOICE:
                return calculateChoice(diceRolls);
            case FOUR_OF_A_KIND:
                return calculateFourOfAKind(diceRolls, counts);
            case FULL_HOUSE:
                return calculateFullHouse(diceRolls, counts);
            case SMALL_STRAIGHT:
                return calculateSmallStraight(sortedDice);
            case LARGE_STRAIGHT:
                return calculateLargeStraight(sortedDice);
            case YACHT:
                return calculateYacht(diceRolls, counts);
            default:
                // 未知のカテゴリが追加された場合など
                return 0;
        }
    }

    /**
     * ONES (1の目) のスコアを計算します。
     * @param diceRolls サイコロの目
     * @return 1の目の合計スコア
     */
    private int calculateOnes(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 1).sum();
    }

    /**
     * TWOS (2の目) のスコアを計算します。
     * @param diceRolls サイコロの目
     * @return 2の目の合計スコア
     */
    private int calculateTwos(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 2).map(die -> die).sum();
    }

    /**
     * THREES (3の目) のスコアを計算します。
     * @param diceRolls サイコロの目
     * @return 3の目の合計スコア
     */
    private int calculateThrees(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 3).map(die -> die).sum();
    }

    /**
     * FOURS (4の目) のスコアを計算します。
     * @param diceRolls サイコロの目
     * @return 4の目の合計スコア
     */
    private int calculateFours(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 4).map(die -> die).sum();
    }

    /**
     * FIVES (5の目) のスコアを計算します。
     * @param diceRolls サイコロの目
     * @return 5の目の合計スコア
     */
    private int calculateFives(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 5).map(die -> die).sum();
    }

    /**
     * SIXES (6の目) のスコアを計算します。
     * @param diceRolls サイコロの目
     * @return 6の目の合計スコア
     */
    private int calculateSixes(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 6).map(die -> die).sum();
    }

    /**
     * CHOICE のスコアを計算します。
     * 全てのサイコロの目の合計がスコアになります。
     * @param diceRolls サイコロの目
     * @return 全てのサイコロの目の合計
     */
    private int calculateChoice(int[] diceRolls) {
        return Arrays.stream(diceRolls).sum();
    }

    /**
     * FOUR_OF_A_KIND (フォーカード) のスコアを計算します。
     * 4つ以上の同じ目がある場合、全てのサイコロの目の合計がスコアになります。
     * @param diceRolls サイコロの目
     * @param counts 各目の出現回数を格納したマップ
     * @return スコア (役がなければ0)
     */
    private int calculateFourOfAKind(int[] diceRolls, Map<Integer, Integer> counts) {
        for (int count : counts.values()) {
            if (count >= 4) {
                return Arrays.stream(diceRolls).sum();
            }
        }
        return 0;
    }

    /**
     * FULL_HOUSE (フルハウス) のスコアを計算します。
     * 3つの同じ目と2つの同じ目がある場合、全てのサイコロの目の合計がスコアになります。
     * @param diceRolls サイコロの目
     * @param counts 各目の出現回数を格納したマップ
     * @return スコア (役がなければ0)
     */
    private int calculateFullHouse(int[] diceRolls, Map<Integer, Integer> counts) {
        boolean hasThree = false;
        boolean hasTwo = false;
        for (int count : counts.values()) {
            if (count == 3) hasThree = true;
            if (count == 2) hasTwo = true;
        }
        if (hasThree && hasTwo) {
            return Arrays.stream(diceRolls).sum();
        }
        return 0;
    }

    /**
     * SMALL_STRAIGHT (スモールストレート) のスコアを計算します。
     * 4つの連続した数字がある場合、固定で15点です。
     * (例: 1-2-3-4, 2-3-4-5, 3-4-5-6)
     * @param sortedDice ソートされたサイコロの目
     * @return スコア (役がなければ0)
     */
    private int calculateSmallStraight(int[] sortedDice) {
        // 重複を除去し、ソートされたユニークな目のリストを作成
        // 例えば、[1, 1, 2, 3, 4] -> [1, 2, 3, 4]
        int[] uniqueSortedDice = Arrays.stream(sortedDice).distinct().toArray();

        // 4つ以上のユニークな目があることを確認し、連続性をチェック
        if (uniqueSortedDice.length >= 4) {
            // "1234", "2345", "3456" のいずれかのパターンを含むか
            String diceSequence = Arrays.toString(uniqueSortedDice).replaceAll("[\\[\\], ]", "");
            if (diceSequence.contains("1234") ||
                diceSequence.contains("2345") ||
                diceSequence.contains("3456")) {
                return 15;
            }
        }
        return 0;
    }

    /**
     * LARGE_STRAIGHT (ラージストレート) のスコアを計算します。
     * 5つの連続した数字がある場合、固定で30点です。
     * (例: 1-2-3-4-5, 2-3-4-5-6)
     * @param sortedDice ソートされたサイコロの目
     * @return スコア (役がなければ0)
     */
    private int calculateLargeStraight(int[] sortedDice) {
        // 5つのユニークな目があり、かつ連続しているかをチェック
        // distinct() を使って重複を除去し、長さが5であることを確認
        if (Arrays.stream(sortedDice).distinct().count() == 5) {
            // ソート済みなので、最初の要素と最後の要素の差が4であれば連続
            if (sortedDice[4] - sortedDice[0] == 4) {
                return 30;
            }
        }
        return 0;
    }

    /**
     * YACHT (ヤット) のスコアを計算します。
     * 5つの同じ目がある場合、固定で50点です。
     * @param diceRolls サイコロの目
     * @param counts 各目の出現回数を格納したマップ
     * @return スコア (役がなければ0)
     */
    private int calculateYacht(int[] diceRolls, Map<Integer, Integer> counts) {
        for (int count : counts.values()) {
            if (count == 5) {
                return 50;
            }
        }
        return 0;
    }    
}
