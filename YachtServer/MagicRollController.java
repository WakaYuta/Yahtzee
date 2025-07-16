
/**
 * MagicRollController
 * 
 * @author morita
 * @version 2025/6/13
 */
public class MagicRollController
{
    
    private int[] diceRolls; 
    /**
     * MagicRollController クラスのインスタンスのためのコンストラクタ
     */
    public MagicRollController()
    {
        this.diceRolls = new int[5];
    }

    /**
     * メソッドの例 - メソッドとともにこのコメントも置き換えてください.
     * 
     * @param  y    メソッドのためのサンプル引数
     * @return        x と y の和
     */
    public int[] rolldices(int[] dicerolls,String LorR){
        diceRolls = dicerolls;
        DiceRoller roller = new DiceRoller();
        
        for (int i = 0; i < diceRolls.length; i++) {
            char command = LorR.charAt(i); // 1文字ずつ取り出す

            if (command == 'L') {
                System.out.println((i + 1) + "文字目: Lが入力されました。");
                // Lの場合はサイコロの目を維持するので、何もする必要がない
            } else if (command == 'R') {
                System.out.println((i + 1) + "文字目: Rが入力されました。賽を振ります。");
                diceRolls[i] = roller.generateRandomNumber(); // 1から6の乱数を生成
            } else {
                System.out.println((i + 1) + "文字目: 不明なコマンド '" + command + "' です。スキップします。");
            }
        }
        
        return diceRolls;
    }
}
