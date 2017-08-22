package pojo;

/**
 * Created by user on 3/5/2017.
 */
public class Match {
    private String host;
    private String guest;
    private String host_score;
    private String quest_score;
    private String time;
    private boolean isFinished=false;

    public Match(String host, String guest, String host_score, String quest_score, String time) {
        this.host = host;
        this.guest = guest;
        this.host_score = host_score;
        this.quest_score = quest_score;
        this.time = time;
        this.isFinished = false;
    }


    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getGuest() {
        return guest;
    }

    public void setGuest(String guest) {
        this.guest = guest;
    }

    public String getHost_score() {
        return host_score;
    }

    public void setHost_score(String host_score) {
        this.host_score = host_score;
    }

    public String getQuest_score() {
        return quest_score;
    }

    public void setQuest_score(String quest_score) {
        this.quest_score = quest_score;
    }
}
