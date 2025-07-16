import java.util.Random;

/**
 * diceRoller
 * 
 * さいころの出目を返すクラス。
 * @author morita
 * @version 2025/7/16
 */

public class DiceRoller {

    /**
     * 1から6までの乱数を生成し、その数値を文字列として返す。
     * @return 1から6までの乱数を表す整数
     */
    public int generateRandomNumber() {
        Random random = new Random();
        int randomNumber = random.nextInt(6) + 1;
        return randomNumber;
    }
    /**
     * 4から6までの乱数を生成し、その数値を文字列として返す。
     * @return 4から6までの乱数を表す整数
     */
    public int generateRandomNumber2() {
        Random random = new Random();
        int randomNumber = random.nextInt(3) + 4;
        return randomNumber;
    }
}

