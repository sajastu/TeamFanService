package util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import pojo.Event;
import pojo.Match;

import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.*;
import java.util.*;


/**
 * Created by neshati on 2/5/2017.
 * Behpardaz
 */
public class DBUtil {

    private int MIN_POOL_SIZE;
    private int Acquire_Increment;
    private int MAX_POOL_SIZE;
    private String user;
    private String password;
    private String dbName;
    private String host;
    private String port;
    private String driverName;
    private String connectionString;
    private Connection connection;


    private ComboPooledDataSource cpds = new ComboPooledDataSource();
    Properties prop = new Properties();


    private static DBUtil util;

    public static DBUtil getInstance() {
        if (util == null)
            util = new DBUtil();
        return util;
    }

    public DBUtil() {
        init();
        initConnectionPooling();
        connection = getConnection();
    }

    public static void clearInstance()
    {
        util = null;
    }

    private void init() {
        InputStream input = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            input = loader.getResourceAsStream("config.properties");
//            InputStream input = new FileInputStream("config.properties");
            prop.load(input);
            MIN_POOL_SIZE = Integer.parseInt(prop.getProperty("MIN_POOL_SIZE"));
            Acquire_Increment = Integer.parseInt(prop.getProperty("Acquire_Increment"));
            MAX_POOL_SIZE = Integer.parseInt(prop.getProperty("MAX_POOL_SIZE"));
            user = prop.getProperty("user");
            password = prop.getProperty("password");
            dbName = prop.getProperty("dbName");
            host = prop.getProperty("host");
            port = prop.getProperty("port");
            driverName = prop.getProperty("driverName");

            connectionString = "jdbc:sqlserver://" +
                    host +
                    "\\SQLEXPRESS:" +
                    port +
                    ";databaseName=" +
                    dbName +
                    ";";


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initConnectionPooling() {
        try {
            cpds.setDriverClass(driverName); //loads the jdbc driver
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        cpds.setJdbcUrl(connectionString);
            cpds.setUser(user);
            cpds.setPassword(password);
            cpds.setMinPoolSize(MIN_POOL_SIZE);
            cpds.setAcquireIncrement(Acquire_Increment);
            cpds.setMaxPoolSize(MAX_POOL_SIZE);

    }

    private Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
//
//    public void updateFixtsures(Match match) {
//        // Toto check for batch insert method
//        Connection conn = getConnection();
//        int sum = 0;
//        for (InstagramUser nextFollower : followersEntities) {
//            insertNewUser(conn, nextFollower);
//            sum += insertNewFollower(conn, nextFollower, mainID);
//        }
//        System.out.println("In total " + sum + " followers are added for user " + mainID);
//    }

    public int updateFixtures(Match match) {
        Connection conn = getConnection();
        String match_id = getMatchID(match);
        try {
            PreparedStatement statement
                    = conn.prepareStatement("UPDATE  match SET score_host = ?, score_quest = ? " +
                    "WHERE match_id=?" );

            statement.setString(1, match.getHost_score());
            statement.setString(2, match.getQuest_score());
            statement.setString(3, match_id);
//            statement.setString(4, match.getGuest());
//            statement.setString(5, match.getMinute());
            int result = statement.executeUpdate();
            statement.close();
            return result;
        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return 0;
    }

    public int updateMatchFinished(Match match){
        Connection conn = getConnection();
        String match_id = getMatchID(match);
        try {
            PreparedStatement statement
                    = conn.prepareStatement("UPDATE match SET isFinished = ? " +
                    "WHERE match_id=?");

            statement.setBoolean(1, match.isFinished());
            statement.setString(2, match_id);
            int result = statement.executeUpdate();
            statement.close();
            return result;
        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return 0;
    }

    public Match selectMatch(Match match, boolean finishedCheck) {
        //
        String sql = "SELECT * FROM  match where host = " + "'" + match.getHost() + "'"
                + " and quest= " + "'" + match.getGuest() + "'"
                + " and match_date = " + "'" + match.getTime() + "'"
                + " and isFinished = " + "'" + finishedCheck + "'";

        Match iu = null;
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                iu = new Match(resultSet.getString("host"), resultSet.getString("quest"),
                        resultSet.getString("score_host"), resultSet.getString("score_quest"), resultSet.getString("match_date"));
            }
            statement.close();
            return iu;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getMatchID(Match match){
        String sql = "SELECT * FROM  match where host = " + "'" + match.getHost() + "'"
                + " and quest= " + "'" + match.getGuest() + "'"
                + " and match_date = " + "'" + match.getTime() + "'";
//                + " and isFinished = " + "'" + match.isFinished() + "'";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                return resultSet.getString("match_id");
            }
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Match insertMatch(Match match, boolean notified) {
        Connection conn = getConnection();
        try {
            PreparedStatement statement
                    = conn.prepareStatement("INSERT INTO match VALUES(?,?,?,?,?,?,?)");
            statement.setString(1, match.getHost());
            statement.setString(2, match.getGuest());
            statement.setString(3, match.getHost_score());
            statement.setString(4, match.getQuest_score());
            statement.setString(5, match.getTime());
            statement.setBoolean(6, match.isFinished());
            statement.setBoolean(7, notified);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return match;
    }

    public int checkFinishedMatch(Match match) {
        String sql = "SELECT * FROM  match where host = " + "'" + match.getHost() + "'"
                + " and quest= " + "'" + match.getGuest() + "'"
                + " and match_date = " + "'" + match.getTime() + "'"
                + " and isFinished = " + "'" + match.isFinished() + "'";
        Match iu = null;
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                iu = new Match(resultSet.getString("host"), resultSet.getString("quest"),
                        resultSet.getString("score_host"), resultSet.getString("score_quest"), resultSet.getString("match_date"));
            }
            statement.close();
            if (iu == null) return 0;
            else
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void insertEvent(Match match, Event event) {
        String match_id = getMatchID(match);
        if (match_id.length() != 0) {
            Connection conn = getConnection();
            try {
                PreparedStatement statement
                        = conn.prepareStatement("INSERT INTO events VALUES(?,?,?,?,?,?,?)");
                statement.setInt(1, Integer.parseInt(match_id));
                statement.setString(2, event.getDoer());
                statement.setString(3, event.getTeam());
                statement.setString(4, event.getTeamside());
                statement.setString(5, event.getMinute());
                statement.setString(6, event.getType());
                statement.setBoolean(7, false);
                statement.executeUpdate();
                statement.close();
                conn.close();
            } catch (Exception e) {
                try {
                    assert conn != null;
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

//    public ArrayList<Event> getGoals(Match match, String team_name){
//
//        String match_id = DBUtil.getInstance().getMatchID(match);
//
//        ArrayList<Event> goals = new ArrayList<>();
//
//        String sql = "SELECT * FROM  events where match_id = " + "'" + match_id + "'"
//                + " and team= " + "'" + team_name + "'";
//        Event goal = null;
//        try {
//            Connection connection = getConnection();
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(sql);
//            while (resultSet.next()) {
//                goal = new Event(resultSet.getString("minute"), resultSet.getString("doer"),
//                        resultSet.getString("team"), "GOAL");
//                goals.add(goal);
//            }
//            statement.close();
//            return goals;
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return goals;
//    }


    public boolean isEventAlreadyNotified(Match match, String time_of_score) {
        String match_id = DBUtil.getInstance().getMatchID(match);
        boolean notified=false;
        String sql = "SELECT notified FROM  events where match_id = " + "'" + match_id + "'"
                + " and minute= " + "'" + time_of_score + "'";
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                notified = resultSet.getBoolean("notified");
            }
            statement.close();
            return notified;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notified;
    }


    public void updateEventNotifFlag(Match match, String occure_time) {
        String match_id = DBUtil.getInstance().getMatchID(match);
        Connection conn = getConnection();
        try {
            PreparedStatement statement
                    = conn.prepareStatement("UPDATE events SET notified = 1 " +
                    "WHERE match_id= ? and minute = ?" );

            statement.setString(1, match_id);
            statement.setString(2, occure_time);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public boolean isMatchNotified(Match unholed_match) {
        String match_id = DBUtil.getInstance().getMatchID(unholed_match);
        if (match_id == null) return false;
        boolean notified= false ;
        String sql = "SELECT notified FROM  match where match_id = " + "'" + match_id + "'";
        try {
            Connection connection = getConnection();
            assert connection != null;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                notified = resultSet.getBoolean("notified");
            }
            statement.close();
            return notified;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notified;
    }

    public int updateMatchNotified(Match match, boolean notified) {
        Connection conn = getConnection();
        try {
            PreparedStatement statement
                    = conn.prepareStatement("UPDATE match SET notified = ?" +
                    "WHERE host = ? and quest = ? and match_date = ?" );

            statement.setBoolean(1, notified);
            statement.setString(2, match.getHost());
            statement.setString(3, match.getGuest());
            statement.setString(4, match.getTime());
            int result = statement.executeUpdate();
            statement.close();
            return result;
        } catch (Exception e) {
            try {
                assert conn != null;
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return 0;
    }

    public Event getLastEvent(Match match) {
        String match_id = getMatchID(match);

//        String sql = "SELECT * FROM events WHERE match_id='" + match_id + "' ORDER BY minute DESC LIMIT 1 ";
        String sql = "SELECT tb1.* FROM events tb1 WHERE id = (SELECT MAX(tb3.id) FROM events tb3) AND match_id='" + match_id + "'";
        Event last_event = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                last_event = new Event(resultSet.getString("minute"), resultSet.getString("doer"),
                        resultSet.getString("team"),resultSet.getString("teamside"), resultSet.getString("type"));
            }
            statement.close();
            return last_event;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return last_event;
    }

    public void insertSubstitution() {

    }
}