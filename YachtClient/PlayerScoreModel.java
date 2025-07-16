import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerScoreModel {
    private final String playerName;
    private final Map<String, Integer> scores = new HashMap<>();
    private final Set<String> recordedCategories = new HashSet<>();
    private boolean hasUsedIkasama = false;
    private boolean hasUsedFhOrStraight = false;
    private int totalScore = 0;

    public PlayerScoreModel(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setScore(String category, int score) {
        scores.put(category, score);
        recordedCategories.add(category);
        recalculateTotalScore();
    }
    
    public int getScore(String category) {
        return scores.getOrDefault(category, 0);
    }
    
    public boolean isRecorded(String category) {
        return recordedCategories.contains(category);
    }
    
    public boolean hasUsedIkasama() {
        return hasUsedIkasama; 
    }
    
    public void setUsedIkasama(boolean used) {
        this.hasUsedIkasama = used;
    }
    
    public boolean hasUsedFhOrStraight() {
        return hasUsedFhOrStraight; 
    }
    
    public void setUsedFhOrStraight(boolean used) {
        this.hasUsedFhOrStraight = used; 
    }
    

    public int getTotalScore() {
        return totalScore;
    }
    
    private void recalculateTotalScore() {
        totalScore = 0;
        for (int score : scores.values()) {
            totalScore += score;
        }
    }
    
    public void reset() {
        scores.clear();
        recordedCategories.clear();
        totalScore = 0;
    }
}