package job;

/**
 * Created by neshati on 2/7/2017.
 * Behpardaz
 */

import core.LiveScoreNotifyService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotifyScoreJob implements Job {
//    private static int c = 0;
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        run();
    }

    private void run() {
//        c++;
        System.out.println(" .NotifyScore Service is going to be called!!");
            LiveScoreNotifyService nsa = new LiveScoreNotifyService();
            try {
                nsa.parseLiveScores();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static void main(String[] args) {
        new NotifyScoreJob().run();
    }
}
