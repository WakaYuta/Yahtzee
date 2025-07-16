import java.util.Random;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * �ʏ�̃��[���ƁA�C�J�T�}���[�����Ǘ�����N���X
 * @author morita & Wakayama
 * @version 2025/07/16
 */
public class MagicRollController {
    
    private int[] diceRolls; 

    public MagicRollController() {
        this.diceRolls = new int[5];
    }

    /**
     * �ʏ�̃T�C�R����U��
     * @param currentDices ���݂̃T�C�R���̖�
     * @param keepPattern  �L�[�v����T�C�R���̃p�^�[�� ('L' or 'R')
     * @return �V�����T�C�R���̖�
     */
    public int[] rolldices(int[] currentDices, String keepPattern) {
        DiceRoller roller = new DiceRoller();

        for (int i = 0; i < diceRolls.length; i++) {
            char command = keepPattern.charAt(i); // 1���������o��

            if (command == 'L') {
                System.out.println((i + 1) + "������: L�����͂���܂����B���b�N���ꂽ�T�C�R���B");
            } else if (command == 'R') {
                System.out.println((i + 1) + "������: R�����͂���܂����B�T�C�R����U�蒼���܂��B");
                currentDices[i] = roller.generateRandomNumber(); //1~6�̗���
            } else {
                System.out.println((i + 1) + "������: �s���ȃR�}���h '" + command + "' �ł��B�X�L�b�v���܂��B");
            }
        }

        return currentDices;
    }

    /**
     * 4, 5, 6�����o�Ȃ��T�C�R����U��
     * @param currentDices  ���݂̃T�C�R���̖�
     * @param keepPattern  �L�[�v����T�C�R���̃p�^�[�� ('L' or 'R')
     * @return �V�����T�C�R���̖�
     */
    public int[] rollIkasamaDices(int[] currentDices, String keepPattern) {
        DiceRoller roller = new DiceRoller();
        for (int i = 0; i < diceRolls.length; i++) {
            char command = keepPattern.charAt(i); // 1���������o��

            if (command == 'L') {
                System.out.println((i + 1) + "������: L�����͂���܂����B���b�N���ꂽ�T�C�R���B");
            } else if (command == 'R') {
                System.out.println((i + 1) + "������: R�����͂���܂����B�T�C�R����U�蒼���܂��B");
                currentDices[i] = roller.generateRandomNumber2(); //4,5,6�̂����ꂩ�������_��
            } else {
                System.out.println((i + 1) + "������: �s���ȃR�}���h '" + command + "' �ł��B�X�L�b�v���܂��B");
            }
        }
        return currentDices;
    }
    
    public int[] rollikasamadices2(int[] currentDices, String keepPattern) {
        Random rand = new Random();
        for (int i = 0; i < diceRolls.length; i++) {
            char command = keepPattern.charAt(i); // 1���������o��

            if (command == 'L') {
                System.out.println((i + 1) + "������: L�����͂���܂����B���b�N���ꂽ�T�C�R���B");
            } else if (command == 'R') {
                System.out.println((i + 1) + "������: R�����͂���܂����B�T�C�R����U�蒼���܂��B");
                adjustRollLikelihood(rand, i); // �t���n�E�Xor�X�g���[�g(50:50)
            } else {
                System.out.println((i + 1) + "������: �s���ȃR�}���h '" + command + "' �ł��B�X�L�b�v���܂��B");
            }
        }

        return currentDices;
    }

    private void adjustRollLikelihood(Random rand, int i) {
        if (i < 0 || i >= diceRolls.length) return;

        boolean useStraight = rand.nextBoolean(); // ���񃉃��_����true/false

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
            secondNum = rand.nextInt(6) + 1; // �قȂ鐔���ɂ���
        }
        
        // 3��2���\��
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

        // ���b�N���ꂽ�ڂ�����
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

        // �g����l���Ȃ���ΓK���ɕԂ�
        if (available.isEmpty()) {
            available.add(rand.nextInt(6) + 1);
        }

        // ���ʂƂ��ĕԂ�
        int[] result = new int[available.size()];
        for (int i = 0; i < available.size(); i++) {
            result[i] = available.get(i);
        }

        return result;
    }
}