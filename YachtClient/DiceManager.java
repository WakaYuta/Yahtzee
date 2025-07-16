import java.lang.StringBuilder;
import javax.swing.Timer;

class DiceManager implements DiceClickListener {
    private DiceSeqComponent[] dices;
    private boolean isMyTurnNow = false;
    private int rollsLeft = 3;

    public DiceManager(DiceSeqComponent[] dices) {
        this.dices = dices;
        for (DiceSeqComponent dice : dices) {
            dice.setDiceClickListener(this);
        }
    }

    public void setRollsLeft(int count) {
        this.rollsLeft = count;
    }

    public void setMyTurn(boolean myTurn) {
        this.isMyTurnNow = myTurn;
        boolean canClick = myTurn && this.rollsLeft < 3;
        for (DiceSeqComponent dice : dices) {
            if (!dice.isLocked()) {
                dice.setClickable(canClick);
            }
        }
    }

    public boolean getMyTurn() {
        return this.isMyTurnNow;
    }

    @Override
    public void diceClicked(DiceSeqComponent clickedDice) {
        if (isMyTurnNow && this.rollsLeft < 3) {
            clickedDice.setLocked(!clickedDice.isLocked());
            clickedDice.setClickable(!clickedDice.isLocked());
        }
    }

    public String getKeepPattern() {
        StringBuilder pattern = new StringBuilder();
        for (DiceSeqComponent dice : dices) {
            pattern.append(dice.isLocked() ? 'L' : 'R');
        }
        return pattern.toString();
    }

    // startRollAnimation メソッドを、どのサイコロをキープするか指定できるように変更
    public void startRollAnimation(String keepPattern) {
        for (int i = 0; i < dices.length; i++) {
            // keepPatternの文字が 'R' (Roll) のサイコロだけをアニメーションさせる
            if (keepPattern.charAt(i) == 'R') {
                dices[i].startAnimation();
            }
        }
    }
    
    public void scheduleStopAnimation(int[] values) {
        int maxDuration = 0;
        for (DiceSeqComponent dice : dices) {
            if (dice.isAnimating()) {
                if (dice.animationDurationMs > maxDuration) {
                    maxDuration = dice.animationDurationMs;
                }
            }
        }

        if (maxDuration == 0) {
            setDiceValuesAndStopAnimation(values);
            return;
        }

        Timer stopTimer = new Timer(maxDuration, e -> {
            setDiceValuesAndStopAnimation(values);
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
    }

    public void setDiceValuesAndStopAnimation(int[] values) {
        if (values == null || values.length != dices.length) {
            System.err.println("Invalid dice values received.");
            return;
        }
        for (int i = 0; i < dices.length; i++) {
            dices[i].stopAnimation();
            dices[i].setDiceValue(values[i]);
        }
    }

    public void resetDices() {
        this.rollsLeft = 3;
        for (DiceSeqComponent dice : dices) {
            dice.setLocked(false);
            dice.setDiceValue(1);
            dice.setClickable(false);
        }
    }
}