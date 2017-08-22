package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by user on 2/26/2017.
 */
public final class Configures {

    public static Properties prop = new Properties();
    public static String src_url;
    public static String sdpTriggerFootballFanServiceLive;
    public static String sdpTriggerFootballFanServiceTerminated;
    public static String team_fan_endpoint;
    public static String score_notifier_service_runPattern;
    public static int number_of_teams;
    public static int number_of_types;
    public static ArrayList<String> teams_persian_name = new ArrayList<>();
    public static ArrayList<String> types_persian_name = new ArrayList<>();
    public static String sdpTriggerNatchTimeService;

    public static void setConfigs() {
        InputStream input = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            input = loader.getResourceAsStream("config.properties");
//            input = new FileInputStream("config.properties");
            prop.load(input);
            src_url = prop.getProperty("live_score_source_url");
            sdpTriggerFootballFanServiceLive = prop.getProperty("sdpTriggerFootballFanServiceLive");
            sdpTriggerFootballFanServiceTerminated = prop.getProperty("sdpTriggerFootballFanServiceTerminated");
            team_fan_endpoint = prop.getProperty("team_fan_endpoint");
//            sdpTriggerNatchTimeService = prop.getProperty("sdpTriggerNatchTimeService");
            number_of_teams = Integer.parseInt(prop.getProperty("number_of_teams"));
            number_of_types = Integer.parseInt(prop.getProperty("number_of_types"));
            score_notifier_service_runPattern = prop.getProperty("score_notifier_service_runPattern");
            for (int i = 1; i < number_of_teams + 1; i++){
                teams_persian_name.add(prop.getProperty("team" + i));
            }

            for (int i = 1; i < number_of_types + 1; i++){
                types_persian_name.add(prop.getProperty("type" + i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
