package pojo;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.Objects;

/**
 * Created by user on 4/30/2017.
 */
public class MessageBank {
    public static String getDeliveredMessage(Match match, Event event){
        String delivered_msg;
        String fixture = "نتیجه کنونی بازی: " + match.getHost_score() + " - " + match.getQuest_score();
        String leading_team = "";
        if (Integer.parseInt(match.getQuest_score()) > Integer.parseInt(match.getHost_score())){
            leading_team = match.getGuest();
        }
        else{
            leading_team = match.getHost();
        }

        if (!Objects.equals(leading_team, ""))
            fixture +=  " به نفع تیم " + leading_team;

        switch (event.getType()){
            case "1": //Ordinary Goal
                delivered_msg = "همین لحظه تیم " + event.getTeam() + " گل زد" + " " + "<br>" +
                        "زننده گل : " + event.getDoer() + " در دقیقه: " + event.getMinute() + "<br>" + fixture;
                break;

            case "2":   //YELLOW CARD
                delivered_msg = "ثانیه هایی پیش " + event.getDoer() + " بازیکن تیم " + event.getTeam() + " در دقیقه "
                        + event.getMinute() + " کارت زرد دریافت کرد." + "<br>";
                break;

            case "3":   //Direct Red
                delivered_msg = "لحظاتی پیش در دقیقه " + event.getMinute() +" ،"+ event.getDoer() +
                        " بازیکن تیم " + event.getTeam() + " با دریافت مستقیم کارت قرمز " +
                                " از بازی اخـراج شد! " +"<br>" + fixture;
                break;

            case "4": //Undirect Red
                delivered_msg = "دقایقی پیش در دقیقه " +event.getMinute() + "، " + event.getDoer() +
                        " بازیکن تیم "+event.getTeam()+ " با دریافت کارت زرد دوم  " + " از زمین مسابقه اخـراج شد! "
                        + "<br>" + fixture;
                break;


            case "6":   //Penalty Goal
                delivered_msg = "همین لحظه تیم " + event.getTeam() + " از روی نقطه پنالتی گل زد " + " " + "<br>" +
                        "زننده پنالتی : " + event.getDoer() + " در دقیقه: " + event.getMinute() + fixture;
                break;

            case "7":   //Self-Goal
                delivered_msg = "همین لحظه تیم " + event.getTeam() + "گل به خودی زد!!" + " " + "<br>" +
                        "زننده گل به خودی : " + event.getDoer() + " در دقیقه: " + event.getMinute() + fixture;
                break;

            case "9":
                delivered_msg = "لحظاتی پیش تغییراتی در ترکیب تیم " + event.getTeam() + " رخ داد." + "<br>"
                        +"در دقیقه " +event.getMinute() + " بازی، "
                        + event.getDoer().split("#")[1] + " جای خود را به " + event.getDoer().split("#")[0] + " داد.";
                break;

            case "12":
                delivered_msg = "در دقیقه  " + event.getMinute() + " ،" + event.getDoer() + " دروازه بان تیم "+ event.getTeam() +
                        " دروازه تیمش را نجات داد! " + "<br>" + fixture;
                break;

            case "start":   //GameStart
                delivered_msg = "بازی تیمهای " + match.getHost() + " و " + match.getGuest() + " ثانیه هایی پیش آغاز شد." + "<br>" +
                        "با گزارش زنده آیوتل همراه باشید.";
                break;

            case "end":     //GameEnd
                delivered_msg = "بازی تیمهای " + match.getHost() + " و " + match.getGuest() + " با نتیجه " + match.getHost_score() + " - "
                        + match.getQuest_score() + " به پایان رسید";
                break;

            default:
                delivered_msg = "";
                break;

        }
        return StringEscapeUtils.unescapeJava(StringEscapeUtils.escapeJava(delivered_msg));
    }
}
