
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
     * MagicRollController �N���X�̃C���X�^���X�̂��߂̃R���X�g���N�^
     */
    public MagicRollController()
    {
        this.diceRolls = new int[5];
    }

    /**
     * ���\�b�h�̗� - ���\�b�h�ƂƂ��ɂ��̃R�����g���u�������Ă�������.
     * 
     * @param  y    ���\�b�h�̂��߂̃T���v������
     * @return        x �� y �̘a
     */
    public int[] rolldices(int[] dicerolls,String LorR){
        diceRolls = dicerolls;
        DiceRoller roller = new DiceRoller();
        
        for (int i = 0; i < diceRolls.length; i++) {
            char command = LorR.charAt(i); // 1���������o��

            if (command == 'L') {
                System.out.println((i + 1) + "������: L�����͂���܂����B");
                // L�̏ꍇ�̓T�C�R���̖ڂ��ێ�����̂ŁA��������K�v���Ȃ�
            } else if (command == 'R') {
                System.out.println((i + 1) + "������: R�����͂���܂����B�΂�U��܂��B");
                diceRolls[i] = roller.generateRandomNumber(); // 1����6�̗����𐶐�
            } else {
                System.out.println((i + 1) + "������: �s���ȃR�}���h '" + command + "' �ł��B�X�L�b�v���܂��B");
            }
        }
        
        return diceRolls;
    }
}
