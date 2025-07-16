import java.net.*;
import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * PlayerScoreSheet �N���X
 * ����̃v���C���[�̃J�e�S�����Ƃ̃X�R�A�ƁA
 * ���̃J�e�S�����L���ς݂ł��邩�̃t���O���J�v�Z�������܂��B
 * 
 * @author Hiroaki Kaji
 * @version 2025/6/6
 */
public class PlayerScoreSheet {
    // �e�J�e�S���̃X�R�A (EnumMap �͗񋓌^���L�[�Ƃ���ꍇ�Ɍ����I�ł�)
    private Map<yachtCategory, Integer> categoryScores;
    // �e�J�e�S�����L���ς݂ł��邩�̃t���O
    private Map<yachtCategory, Boolean> categoryRecorded;

    /**
     * PlayerScoreSheet �̃R���X�g���N�^�B
     * �S�ẴJ�e�S���̃X�R�A��0�A�L���ς݃t���O��false�ŏ��������܂��B
     */
    public PlayerScoreSheet() {
        this.categoryScores = new EnumMap<>(yachtCategory.class);
        this.categoryRecorded = new EnumMap<>(yachtCategory.class);

        // �S�Ă̖���������
        for (yachtCategory category : yachtCategory.values()) {
            this.categoryScores.put(category, 0); // �����X�R�A��0
            this.categoryRecorded.put(category, false); // �����͖��L��
        }
    }

    /**
     * �w�肳�ꂽ�J�e�S���̃X�R�A���擾���܂��B
     * @param category �X�R�A���擾����YachtCategory
     * @return �Y���J�e�S���̃X�R�A
     */
    public int getScore(yachtCategory category) {
        return categoryScores.getOrDefault(category, 0);
    }

    /**
     * �w�肳�ꂽ�J�e�S���̃X�R�A��ݒ肵�܂��B
     * �X�R�A�����ɋL���ς݂łȂ��ꍇ�ɂ̂ݐݒ�ł��܂��B
     * @param category �X�R�A��ݒ肷��YachtCategory
     * @param score �ݒ肷��X�R�A
     * @return �X�R�A������ɐݒ肳�ꂽ�ꍇ��true�A���ɋL���ς݂Őݒ�ł��Ȃ������ꍇ��false
     */
    public boolean setScore(yachtCategory category, int score) {
        if (isRecorded(category)) {
            System.out.println("Error: Score for " + category + " has already been recorded.");
            return false;
        }
        this.categoryScores.put(category, score);
        this.categoryRecorded.put(category, true); // �L���ς݂ƃ}�[�N
        return true;
    }

    /**
     * �w�肳�ꂽ�J�e�S���̃X�R�A�����ɋL���ς݂ł��邩���m�F���܂��B
     * @param category �m�F����YachtCategory
     * @return �L���ς݂ł����true�A�����łȂ����false
     */
    public boolean isRecorded(yachtCategory category) {
        return categoryRecorded.getOrDefault(category, false);
    }

    /**
     * �S�ẴJ�e�S���̍��v�X�R�A���v�Z���Ď擾���܂��B
     * @return ���v�X�R�A
     */
    public int getTotalScore() {
        return categoryScores.values().stream()
                             .mapToInt(Integer::intValue)
                             .sum();
    }

    /**
     * �S�ẴJ�e�S���ƃX�R�A�𕶎���Ƃ��ďo�́B�i�f�o�b�O�p�j
     * @return �X�R�A�V�[�g�̕�����\��
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