import java.util.Random;

/**
 * diceRoller
 * 
 * さいころの出目を返すクラス。
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */

public class DiceRoller {

    /**
     * 1から6までの乱数を生成し、その数値を文字列として返します。
     * 
     * 呼び出し方サンプル；
     * DiceRoller roller = new DiceRoller();
        String result = roller.generateRandomNumberText();
        System.out.println("生成された乱数: " + result);
     *
     * @return 1から6までの乱数を表す整数
     */
    public int generateRandomNumber() {
        Random random = new Random();
        // nextInt(bound) は 0 (inclusive) から bound (exclusive) までの乱数を生成します。
        // 1から6を得るには、nextInt(6) で 0-5 を生成し、それに1を加えます。
        int randomNumber = random.nextInt(6) + 1;
        return randomNumber; // 
    }
}