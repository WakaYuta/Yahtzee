import java.util.Random;

/**
 * diceRoller
 * 
 * ��������̏o�ڂ�Ԃ��N���X�B
 * @author morita
 * @version 2025/7/16
 */

public class DiceRoller {

    /**
     * 1����6�܂ł̗����𐶐����A���̐��l�𕶎���Ƃ��ĕԂ��B
     * @return 1����6�܂ł̗�����\������
     */
    public int generateRandomNumber() {
        Random random = new Random();
        int randomNumber = random.nextInt(6) + 1;
        return randomNumber;
    }
    /**
     * 4����6�܂ł̗����𐶐����A���̐��l�𕶎���Ƃ��ĕԂ��B
     * @return 4����6�܂ł̗�����\������
     */
    public int generateRandomNumber2() {
        Random random = new Random();
        int randomNumber = random.nextInt(3) + 4;
        return randomNumber;
    }
}

