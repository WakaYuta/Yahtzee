import java.util.Random;

/**
 * diceRoller
 * 
 * ��������̏o�ڂ�Ԃ��N���X�B
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */

public class DiceRoller {

    /**
     * 1����6�܂ł̗����𐶐����A���̐��l�𕶎���Ƃ��ĕԂ��܂��B
     * 
     * �Ăяo�����T���v���G
     * DiceRoller roller = new DiceRoller();
        String result = roller.generateRandomNumberText();
        System.out.println("�������ꂽ����: " + result);
     *
     * @return 1����6�܂ł̗�����\������
     */
    public int generateRandomNumber() {
        Random random = new Random();
        // nextInt(bound) �� 0 (inclusive) ���� bound (exclusive) �܂ł̗����𐶐����܂��B
        // 1����6�𓾂�ɂ́AnextInt(6) �� 0-5 �𐶐����A�����1�������܂��B
        int randomNumber = random.nextInt(6) + 1;
        return randomNumber; // 
    }
}