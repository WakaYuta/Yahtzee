import java.util.Random;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * 通常のロールと、イカサマロールを管理するクラス
 * @author morita & Wakayama
 * @version 2025/07/16
 */
public class MagicRollController {
    
    private int[] diceRolls; 

    public MagicRollController() {
        this.diceRolls = new int[5];
    }

    /**
     * 通常のサイコロを振る
     * @param currentDices 現在のサイコロの目
     * @param keepPattern  キープするサイコロのパターン ('L' or 'R')
     * @return 新しいサイコロの目
     */
    public int[] rolldices(int[] currentDices, String keepPattern) {
        DiceRoller roller = new DiceRoller();

        for (int i = 0; i < diceRolls.length; i++) {
            char command = keepPattern.charAt(i); // 1文字ずつ取り出す

            if (command == 'L') {
                System.out.println((i + 1) + "文字目: Lが入力されました。ロックされたサイコロ。");
            } else if (command == 'R') {
                System.out.println((i + 1) + "文字目: Rが入力されました。サイコロを振り直します。");
                currentDices[i] = roller.generateRandomNumber(); //1~6の乱数
            } else {
                System.out.println((i + 1) + "文字目: 不明なコマンド '" + command + "' です。スキップします。");
            }
        }

        return currentDices;
    }

    /**
     * 4, 5, 6しか出ないサイコロを振る
     * @param currentDices  現在のサイコロの目
     * @param keepPattern  キープするサイコロのパターン ('L' or 'R')
     * @return 新しいサイコロの目
     */
    public int[] rollIkasamaDices(int[] currentDices, String keepPattern) {
        DiceRoller roller = new DiceRoller();
        for (int i = 0; i < diceRolls.length; i++) {
            char command = keepPattern.charAt(i); // 1文字ずつ取り出す

            if (command == 'L') {
                System.out.println((i + 1) + "文字目: Lが入力されました。ロックされたサイコロ。");
            } else if (command == 'R') {
                System.out.println((i + 1) + "文字目: Rが入力されました。サイコロを振り直します。");
                currentDices[i] = roller.generateRandomNumber2(); //4,5,6のいずれかをランダム
            } else {
                System.out.println((i + 1) + "文字目: 不明なコマンド '" + command + "' です。スキップします。");
            }
        }
        return currentDices;
    }
    
    public int[] rollikasamadices2(int[] currentDices, String keepPattern) {
        Random rand = new Random();
        for (int i = 0; i < diceRolls.length; i++) {
            char command = keepPattern.charAt(i); // 1文字ずつ取り出す

            if (command == 'L') {
                System.out.println((i + 1) + "文字目: Lが入力されました。ロックされたサイコロ。");
            } else if (command == 'R') {
                System.out.println((i + 1) + "文字目: Rが入力されました。サイコロを振り直します。");
                adjustRollLikelihood(rand, i); // フルハウスorストレート(50:50)
            } else {
                System.out.println((i + 1) + "文字目: 不明なコマンド '" + command + "' です。スキップします。");
            }
        }

        return currentDices;
    }

    private void adjustRollLikelihood(Random rand, int i) {
        if (i < 0 || i >= diceRolls.length) return;

        boolean useStraight = rand.nextBoolean(); // 毎回ランダムにtrue/false

        if (useStraight) {
            int[] straight = generateStraight(rand);
            diceRolls[i] = straight[rand.nextInt(straight.length)];
        } else {
            int[] fullHouse = generateFullHouse(rand);
            diceRolls[i] = fullHouse[rand.nextInt(fullHouse.length)];
        }
    }

    public int[] generateFullHouse(Random rand) {
        int[] result = new int[5];

        List<Integer> lockedNumbers = new ArrayList<>();
        for (int i = 0; i < diceRolls.length; i++) {
            if (diceRolls[i] != 0) {
                lockedNumbers.add(diceRolls[i]);
            }
        }

        int[] fullHouse = new int[5];
        int mainNum = lockedNumbers.size() > 0 ? lockedNumbers.get(0) : rand.nextInt(6) + 1;
        int secondNum = (lockedNumbers.size() > 1 ? lockedNumbers.get(1) : rand.nextInt(6) + 1);

         while (secondNum == mainNum) {
            secondNum = rand.nextInt(6) + 1; // 異なる数字にする
        }
        
        // 3つと2つを構成
        for (int i = 0; i < 3; i++) fullHouse[i] = mainNum;
        for (int i = 3; i < 5; i++) fullHouse[i] = secondNum;

        return fullHouse;
    }

    public int[] generateStraight(Random rand) {
        int[][] possibleStraights = {
            {1, 2, 3},
            {2, 3, 4},
            {3, 4, 5},
            {4, 5, 6},
            {2, 3, 4, 5},
            {3, 4, 5, 6}
        };

        int[] chosen = possibleStraights[rand.nextInt(possibleStraights.length)];

        // ロックされた目を除く
        List<Integer> locked = new ArrayList<>();
        for (int i = 0; i < diceRolls.length; i++) {
            if (diceRolls[i] != 0) {
                locked.add(diceRolls[i]);
            }
        }

        List<Integer> available = new ArrayList<>();
        for (int val : chosen) {
            if (!locked.contains(val)) {
                available.add(val);
            }
        }

        // 使える値がなければ適当に返す
        if (available.isEmpty()) {
            available.add(rand.nextInt(6) + 1);
        }

        // 結果として返す
        int[] result = new int[available.size()];
        for (int i = 0; i < available.size(); i++) {
            result[i] = available.get(i);
        }

        return result;
    }
}