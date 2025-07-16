import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * class ScoreCalculater.
 * �T�C�R���̖ڂ̐����̔z�� int[5]���󂯎���āA
 * �e�����Ƃ̃X�R�A���v�Z���AEnum YachtCategory�̖��Ƃ��̃X�R�A��ς������߂̌v�Z�p�N���X�B
 * 
 *
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
public class ScoreCalculater {

    /**
     * �w�肳�ꂽYachtCategory�ƃT�C�R���̖ڂɊ�Â��ăX�R�A���v�Z���܂��B
     * ���̃��\�b�h�́A�e���̋�̓I�ȃX�R�A�v�Z���\�b�h���Ăяo���܂��B
     * �T�C�R���̖ڂ̃f�t�H���g�l�Ƃ���0�����Ă���(int�̏�������)
     * �����ŁA���ׂĂ�0�̂Ƃ��͏�����ԂȂ̂ŁA�X�R�A�͂��ׂ�0�ŕԂ��悤�ɐݒ肵�Ă���B
     *
     * @param category �X�R�A���v�Z����YachtCategory
     * @param diceRolls �T�C�R���̖� (5�̐����z��)
     * @return �v�Z���ꂽ�X�R�A
     * @throws IllegalArgumentException diceRolls��null�܂��͒�����5�ł͂Ȃ��ꍇ
     */
    public int calcScore(yachtCategory category, int[] diceRolls) {
        if (diceRolls == null || diceRolls.length != 5) {
            throw new IllegalArgumentException("�T�C�R���̖ڂ�5�̔z��ł���K�v������܂��B");
        }
        //�o�ڂ��S��0�̏�����Ԃł���΁A�X�R�A��0
        boolean allZeros = true;
        for (int die : diceRolls) {
            if (die != 0) {
                allZeros = false;
                break;
            }
        }
        if (allZeros) {
            // �T�C�R���̖ڂ��S��0�̏ꍇ�A�S�Ă̖��̃X�R�A��0�Ƃ���
            return 0;
        }

        // �X�R�A�v�Z�ɖ𗧂⏕�I�ȃf�[�^�i�\�[�g�ςݔz��Ɩڂ̏o���񐔁j������
        int[] sortedDice = Arrays.copyOf(diceRolls, diceRolls.length);
        Arrays.sort(sortedDice); // ��: [1, 2, 3, 4, 5] �����ɂ��Ă����B
        
        //�T�C�R���̖ڂƂ��̏o�������}�b�v��
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
                // ���m�̃J�e�S�����ǉ����ꂽ�ꍇ�Ȃ�
                return 0;
        }
    }

    /**
     * ONES (1�̖�) �̃X�R�A���v�Z���܂��B
     * @param diceRolls �T�C�R���̖�
     * @return 1�̖ڂ̍��v�X�R�A
     */
    private int calculateOnes(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 1).sum();
    }

    /**
     * TWOS (2�̖�) �̃X�R�A���v�Z���܂��B
     * @param diceRolls �T�C�R���̖�
     * @return 2�̖ڂ̍��v�X�R�A
     */
    private int calculateTwos(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 2).map(die -> die).sum();
    }

    /**
     * THREES (3�̖�) �̃X�R�A���v�Z���܂��B
     * @param diceRolls �T�C�R���̖�
     * @return 3�̖ڂ̍��v�X�R�A
     */
    private int calculateThrees(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 3).map(die -> die).sum();
    }

    /**
     * FOURS (4�̖�) �̃X�R�A���v�Z���܂��B
     * @param diceRolls �T�C�R���̖�
     * @return 4�̖ڂ̍��v�X�R�A
     */
    private int calculateFours(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 4).map(die -> die).sum();
    }

    /**
     * FIVES (5�̖�) �̃X�R�A���v�Z���܂��B
     * @param diceRolls �T�C�R���̖�
     * @return 5�̖ڂ̍��v�X�R�A
     */
    private int calculateFives(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 5).map(die -> die).sum();
    }

    /**
     * SIXES (6�̖�) �̃X�R�A���v�Z���܂��B
     * @param diceRolls �T�C�R���̖�
     * @return 6�̖ڂ̍��v�X�R�A
     */
    private int calculateSixes(int[] diceRolls) {
        return Arrays.stream(diceRolls).filter(die -> die == 6).map(die -> die).sum();
    }

    /**
     * CHOICE �̃X�R�A���v�Z���܂��B
     * �S�ẴT�C�R���̖ڂ̍��v���X�R�A�ɂȂ�܂��B
     * @param diceRolls �T�C�R���̖�
     * @return �S�ẴT�C�R���̖ڂ̍��v
     */
    private int calculateChoice(int[] diceRolls) {
        return Arrays.stream(diceRolls).sum();
    }

    /**
     * FOUR_OF_A_KIND (�t�H�[�J�[�h) �̃X�R�A���v�Z���܂��B
     * 4�ȏ�̓����ڂ�����ꍇ�A�S�ẴT�C�R���̖ڂ̍��v���X�R�A�ɂȂ�܂��B
     * @param diceRolls �T�C�R���̖�
     * @param counts �e�ڂ̏o���񐔂��i�[�����}�b�v
     * @return �X�R�A (�����Ȃ����0)
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
     * FULL_HOUSE (�t���n�E�X) �̃X�R�A���v�Z���܂��B
     * 3�̓����ڂ�2�̓����ڂ�����ꍇ�A�S�ẴT�C�R���̖ڂ̍��v���X�R�A�ɂȂ�܂��B
     * @param diceRolls �T�C�R���̖�
     * @param counts �e�ڂ̏o���񐔂��i�[�����}�b�v
     * @return �X�R�A (�����Ȃ����0)
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
     * SMALL_STRAIGHT (�X���[���X�g���[�g) �̃X�R�A���v�Z���܂��B
     * 4�̘A����������������ꍇ�A�Œ��15�_�ł��B
     * (��: 1-2-3-4, 2-3-4-5, 3-4-5-6)
     * @param sortedDice �\�[�g���ꂽ�T�C�R���̖�
     * @return �X�R�A (�����Ȃ����0)
     */
    private int calculateSmallStraight(int[] sortedDice) {
        // �d�����������A�\�[�g���ꂽ���j�[�N�Ȗڂ̃��X�g���쐬
        // �Ⴆ�΁A[1, 1, 2, 3, 4] -> [1, 2, 3, 4]
        int[] uniqueSortedDice = Arrays.stream(sortedDice).distinct().toArray();

        // 4�ȏ�̃��j�[�N�Ȗڂ����邱�Ƃ��m�F���A�A�������`�F�b�N
        if (uniqueSortedDice.length >= 4) {
            // "1234", "2345", "3456" �̂����ꂩ�̃p�^�[�����܂ނ�
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
     * LARGE_STRAIGHT (���[�W�X�g���[�g) �̃X�R�A���v�Z���܂��B
     * 5�̘A����������������ꍇ�A�Œ��30�_�ł��B
     * (��: 1-2-3-4-5, 2-3-4-5-6)
     * @param sortedDice �\�[�g���ꂽ�T�C�R���̖�
     * @return �X�R�A (�����Ȃ����0)
     */
    private int calculateLargeStraight(int[] sortedDice) {
        // 5�̃��j�[�N�Ȗڂ�����A���A�����Ă��邩���`�F�b�N
        // distinct() ���g���ďd�����������A������5�ł��邱�Ƃ��m�F
        if (Arrays.stream(sortedDice).distinct().count() == 5) {
            // �\�[�g�ς݂Ȃ̂ŁA�ŏ��̗v�f�ƍŌ�̗v�f�̍���4�ł���ΘA��
            if (sortedDice[4] - sortedDice[0] == 4) {
                return 30;
            }
        }
        return 0;
    }

    /**
     * YACHT (���b�g) �̃X�R�A���v�Z���܂��B
     * 5�̓����ڂ�����ꍇ�A�Œ��50�_�ł��B
     * @param diceRolls �T�C�R���̖�
     * @param counts �e�ڂ̏o���񐔂��i�[�����}�b�v
     * @return �X�R�A (�����Ȃ����0)
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
