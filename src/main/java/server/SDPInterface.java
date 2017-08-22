package server;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.Configures;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;


/**
 * Created by user on 2/26/2017.
 */
@Path("/fan_sport")
public class SDPInterface {

    @GET
    @Produces("application/json")
    @Path("/teamnames")
    public String getTeamName() throws JSONException {
        // Return some cliched textual content
        JSONArray array = new JSONArray();
        array.put(getJsonObject("اوساسونا", Configures.teams_persian_name.get(0)));
        array.put(getJsonObject("استقلال", Configures.teams_persian_name.get(1)));
        array.put(getJsonObject("تراکتورسازی", Configures.teams_persian_name.get(2)));
        array.put(getJsonObject("سپاهان", Configures.teams_persian_name.get(3)));
        return array.toString();
    }

    @GET
    @Produces("application/json")
    @Path("/typenames")
    public String getEventTypes() throws JSONException {
        // Return some cliched textual content
        JSONArray array = new JSONArray();
        array.put(getJsonObject("گلهای بازی", Configures.types_persian_name.get(0)));
        return array.toString();
    }

    private JSONObject getJsonObject(String team_code, String name) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("Code", team_code);
        json.put("Title", name);
        return json;
    }

//    public static void main(String[] args) throws IOException {
//        Configures.setConfigs();
//        HttpServer server = HttpServerFactory.create(Configures.team_fan_endpoint);
//        server.start();
//        System.out.println("Server running");
//        System.out.println("Hit return to stop...");
//        System.in.read();
//        System.out.println("Stopping server");
//        server.stop(0);
//        System.out.println("Server stopped");
//    }

}