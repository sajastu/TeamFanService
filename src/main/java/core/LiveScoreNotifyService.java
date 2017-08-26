package core;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pojo.Event;
import pojo.Match;
import pojo.MessageBank;
import util.Configures;
import util.DBUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created by sajastu on 2/26/2017.
 */
public class LiveScoreNotifyService extends TriggerCaller {

    public LiveScoreNotifyService() {

    }

    public void parseLiveScores() throws IOException, JSONException {
        Document doc = null;
        try {
            doc = Jsoup.connect(Configures.src_url).timeout(20 * 1000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert doc != null;
        Elements links = doc.select(".match-row");

        for (Element link : links) {
            if (Configures.teams_persian_name.contains(link.select(".teamname.right").text())) {
                linkAnalyzer(link, "right");
            }
            else if (Configures.teams_persian_name.contains(link.select(".teamname.left").text())){
                linkAnalyzer(link, "left");
            }
        }
    }

    private void linkAnalyzer(Element link, String observer_team) throws IOException, JSONException {
        String hostTeamName = link.select(".teamname.right").text();
        String guestTeamName = link.select(".teamname.left").text();
        String hostScore = link.select(".score.right").text();
        String guestScore = link.select(".score.left").text();
        String startTime = link.select(".start-time").text();
        String startDate = link.select(".start-date").text();
        String matchStatus = link.select(".match-status > .elapsed-time").text();

        //IF the game is running ...
        if (!Objects.equals(hostScore, "?") && !Objects.equals(matchStatus, "نتیجه نهایی")) { //TODO
            Match match = new Match(hostTeamName, guestTeamName, hostScore, guestScore, startDate + "T" + startTime);
            Match DBMatch = DBUtil.getInstance().selectMatch(match, false);
            if (DBMatch == null){
                //if the game has NOT been added already, add it to DB
                saveAndNotifyGameKickOff(match);
            }
            else {
                //If the game has already been added to DB
                monitorGameEvents(link, hostScore, guestScore, match, DBMatch);
            }
        }

        else if(Objects.equals(matchStatus, "نتیجه نهایی"))
        {
            //IF the game is over ... Notify the users who have signed up on the corresponding applet
            //THIS means that the game is over
            notifyGameTermination(hostTeamName, guestTeamName, hostScore, guestScore, startTime, startDate);
        }
    }

    private void notifyGameTermination(String hostTeamName, String guestTeamName, String hostScore, String guestScore, String startTime, String startDate) throws JSONException, IOException {
        Match match = new Match(hostTeamName, guestTeamName, hostScore, guestScore, startDate + "T" + startTime);
        fillParametersAndFire(match, new Event("","","","","end"));
        match.setFinished(true);
        Match prior_match = DBUtil.getInstance().selectMatch(match, false);
        if (prior_match != null) {
            DBUtil.getInstance().updateMatchFinished(match);
            makeBriefInfoAndFire(match);
        }
    }

    private void monitorGameEvents(Element link, String hostScore, String guestScore, Match match, Match DBMatch) throws IOException, JSONException {
        System.out.println("The match already was in DB");
        if (Integer.parseInt(DBMatch.getHost_score()) < Integer.parseInt(hostScore)) {
            //live score users should be notified
            System.out.println("host team has scored in the last 1 min!");
            DBUtil.getInstance().updateFixtures(match);
        }

        if (Integer.parseInt(DBMatch.getQuest_score()) < Integer.parseInt(guestScore)) {
            DBUtil.getInstance().updateFixtures(match);
            System.out.println("guest team has scored in the last 1 min!");
        }
        parseDetailedEvents(link, match);
    }

    private void saveAndNotifyGameKickOff(Match match) throws JSONException, IOException {
        DBUtil.getInstance().insertMatch(match, false);
        System.out.println("Match was not in DB, Now it's added successfullly");
        fillParametersAndFire(match, new Event("","","","","start"));
    }

    private void parseDetailedEvents(Element link, Match match) throws IOException, JSONException {

        ArrayList<String> sides = new ArrayList<>();
        sides.addAll(Arrays.asList("right", "left"));

        Event last_event = DBUtil.getInstance().getLastEvent(match);

        if (last_event == null){
            Elements eventsTillNow = link.select(".match-events-wrapper").select("div[class*=event-]");
            for (int i=0; i<eventsTillNow.size(); i++){
                checkEventAndFire(match, eventsTillNow, i);
            }
        }
        else{
            last_event.setDoer(last_event.getDoer().replace("ي", "ی").replace("ك", "ک"));
            Elements eventsTillNow = link.select(".match-events-wrapper").select("div[class*=event-]");
            int index =100;
            for (int i=0; i<eventsTillNow.size(); i++){
                if (Objects.equals(eventsTillNow.get(i).select(".event-" + last_event.getType() + "." +last_event.getTeamside() + " > span").text(), last_event.getDoer().split("#")[0])
                        && Objects.equals(eventsTillNow.get(i).select(".event-" + last_event.getType() + "." +last_event.getTeamside() + " > .occure-time").text(), last_event.getMinute())){
                    index = i;
                }
            }
            for (int i= index+1; i<eventsTillNow.size(); i++){
                checkEventAndFire(match, eventsTillNow, i);
            }
        }
    }

    private void checkEventAndFire(Match match, Elements events_until_now, int i) throws JSONException, IOException {
        String teamside ="";
        String team = "";
        if (events_until_now.get(i).hasClass("right")){
            teamside = "right";
            team = match.getHost();
        }
        else {
            teamside = "left";
            team = match.getGuest();
        }
        String event_id =events_until_now.get(i).attr("class");
        event_id = event_id.substring(event_id.indexOf("-")+1,event_id.indexOf(" ", event_id.indexOf("-")));
        Event new_event = null;
        if (Objects.equals(event_id, "10")) return;
        else if (Objects.equals(event_id, "9")){
            String out_player = events_until_now.get(i-1).select("span").text();
            new_event = new Event(events_until_now.get(i).select(".occure-time").text(),
                    events_until_now.get(i).select("span").text()+"#"+out_player, team, teamside, event_id);
        }
        else {
            new_event = new Event(events_until_now.get(i).select(".occure-time").text(),
                    events_until_now.get(i).select("span").text(), team, teamside, event_id);
        }
        DBUtil.getInstance().insertEvent(match, new_event);
        fillParametersAndFire(match, new_event);
    }

    private void checkEventsAndFire(Element link, Match match, String teamside, int event_id) throws JSONException, IOException {
        Elements event_elements = link.select(".match-events-wrapper > .event-" + event_id  + "." + teamside);
        String team_name=match.getHost();
        if (Objects.equals(teamside, "left")){
            team_name = match.getGuest();
        }
        for (int j=0; j<event_elements.size(); j++) {
            if (event_id==10){
                continue;
            }
            if (event_id==9){  //Event is substitution
                String in_player = event_elements.get(j).select(" span").text();
                String out_player = link.select(".match-events-wrapper > .event-10."+teamside+" span").get(j).text();
                String occure_time = event_elements.get(j).select(" > .occure-time").text();
                Event sb_event = new Event(occure_time, in_player+"#"+out_player, team_name, teamside, "9");
                DBUtil.getInstance().insertEvent(match,sb_event);
                fillParametersAndFire(match, sb_event);
            }
            else {
                Event new_event = getNewEventAttr(event_elements.get(j), team_name, teamside, event_id);
                DBUtil.getInstance().insertEvent(match, new_event);
                fillParametersAndFire(match, new_event);
            }
        }
    }

    private void setOccuredEventsNumbers(Elements link, ArrayList<Integer> host_occured_events_numbers, ArrayList<Integer> guest_occured_events_numbers, ArrayList<String> sides, int index) {
        Elements events = link.select("div[class*=event-]");
        for (int j=index+1; j<events.size(); j++){
            for (String side : sides) {
                for (int i = 1; i<11; i++) {
                    boolean event = events.get(j).select(".event-" + i + "." + side + " > .occure-time").isEmpty();
                    if (Objects.equals(side, "right") && !event && !host_occured_events_numbers.contains(i)){
                        host_occured_events_numbers.add(i);
                    }
                    else if (Objects.equals(side, "left") && !event && !guest_occured_events_numbers.contains(i)){
                        guest_occured_events_numbers.add(i);
                    }
                }
            }
        }
    }


    private Event getNewEventAttr(Element event_element, String team, String teamside, int event_type) {
        String doer = event_element.select(" span").text();
        String occure_time = event_element.select(" > .occure-time").text();
        return new Event(occure_time, doer, team, teamside, String.valueOf(event_type));

    }

    private void fillParametersAndFire(Match match, Event event) throws JSONException, IOException {
        ArrayList<String> observers = getObserverTeam(match);
        System.out.println("new Event: " + event.getType());
        for (String observer : observers) {
            fillParams("live_score");
            params.put("host", match.getHost());
            params.put("quest", match.getGuest());
            params.put("observer", observer);
            params.put("host_score", match.getHost_score());
            params.put("quest_score", match.getHost_score());
            params.put("event_kind", event.getType());
            params.put("event_doer", event.getDoer());
            params.put("inFavor_team", event.getTeam());
            params.put("time", event.getMinute());
            params.put("delivered_msg", MessageBank.getDeliveredMessage(match, event));
            fire();
        }
    }

    private ArrayList<String> getObserverTeam(Match match) {
        ArrayList<String> out = new ArrayList<>();
        if (Configures.teams_persian_name.contains(match.getHost())){
            out.add(match.getHost());
        }
        if (Configures.teams_persian_name.contains(match.getGuest())){
            out.add(match.getGuest());
        }
        return out;
    }

    public void makeBriefInfoAndFire(Match match) throws IOException, JSONException {
        ArrayList<String> observers = getObserverTeam(match);
        for (String observer : observers) {
            System.out.println("brief");
            fillParams("terminated_score");
//            params.put("goals_host", getGoalsJson(match, match.getHost()));
//            params.put("goals_quest", getGoalsJson(match, match.getGuest()));
            params.put("observer", observer);
            params.put("delivered_msg", MessageBank.getDeliveredMessage(match, new Event("","","","","end")));
            fire();
        }
    }


    private void fire() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(params.get("sdpURL"));
        List<NameValuePair> urlParameters = new ArrayList();
        for (String paramKey : params.keySet()) {
            urlParameters.add(new BasicNameValuePair(paramKey, params.get(paramKey)));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
        HttpResponse response = client.execute(post);
        System.out.println("Response Code for Service = : " + this.toString() + " "
                + response.getStatusLine().getStatusCode());
    }

    public void run() {
        try {
            parseLiveScores();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fillParams(String applet_name) {
        params = new HashMap<>();
        switch (applet_name){
            case "terminated_score":
                params.put(TriggerCaller.SDPURLKEY, Configures.sdpTriggerFootballFanServiceTerminated);
                break;
            case "live_score":
                params.put(TriggerCaller.SDPURLKEY, Configures.sdpTriggerFootballFanServiceLive);
                break;
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        Configures.setConfigs();
        LiveScoreNotifyService ns = new LiveScoreNotifyService();
        ns.run();
    }
}