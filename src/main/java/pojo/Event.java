package pojo;

/**
 * Created by user on 4/9/2017.
 */
public class Event {
    private String minute;
    private String doer;
    private String team;
    private String teamside;
    private String type;


    public Event(String minute, String doer, String team, String teamside, String type) {
        this.minute = minute;
        this.doer = doer;
        this.team = team;
        this.teamside = teamside;
        this.type = type;
    }

    public String getTeamside() {
        return teamside;
    }

    public void setTeamside(String teamside) {
        this.teamside = teamside;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getDoer() {
        return doer;
    }

    public void setDoer(String doer) {
        this.doer = doer;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
