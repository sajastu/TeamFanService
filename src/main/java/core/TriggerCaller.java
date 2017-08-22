package core;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by neshati on 2/7/2017.
 * Behpardaz
 */
public abstract class TriggerCaller implements Serializable {
    public static String SDPURLKEY = "sdpURL";
    public HashMap<String, String> params;      // params should include sdpURL
    public abstract void run();
    public abstract void fillParams(String typeOfApplet);
}