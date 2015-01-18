import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.DriverManager.getConnection;

public class Server implements Runnable{

    private Socket sock;


    public Server(Socket sock) {
        this.sock = sock;
    }

    public void run() {

        Connection connection = null;
        PrintWriter pw = null;
        BufferedReader br = null;
        PreparedStatement preparedStatement;
        ResultSet res;
        int id = 0;
        String email = "";
        ArrayList<Integer> event_id = new ArrayList<Integer>();
        ArrayList<Integer> poll_id = new ArrayList<Integer>();

        try {
            connection = getConnection("jdbc:mysql://localhost:3306/FoxMeet", "root", "6thfloorhomies");
            pw = new PrintWriter(sock.getOutputStream());
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            email = br.readLine().trim();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            System.err.println("Some weird reader/writer issue");
            return;
        }

       if (email.charAt(0) == '@') {
            ArrayList<Integer> client_id = new ArrayList<Integer>();
            String commands[] = email.split(",");
            String date = commands[2] +"/" +commands[3] +"/"+ commands[4];
            for (int i = 7 ; i < commands.length ; i++) {
                try {
                    preparedStatement = connection.prepareStatement("SELECT userID FROM Users WHERE emailID = ?;");
                    preparedStatement.setString(1, commands[i].trim());
                    res = preparedStatement.executeQuery();
                    String ids;
                    while(res.next()) {
                        ids = res.getString(1);
                        id = Integer.parseInt(ids);
                    }
                    if (id == 0) {
                        preparedStatement = connection.prepareStatement("SELECT max(userID) FROM Users;");
                        res = preparedStatement.executeQuery();
                        while (res.next())
                            id = ((res.getInt("max(userID)")) + 1);
                        preparedStatement = connection.prepareStatement("INSERT INTO Users VALUES (?, ?);");
                        preparedStatement.setString(1, String.valueOf(id));
                        preparedStatement.setString(2, email);
                        preparedStatement.executeUpdate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                client_id.add(id);
            }
            try {
                preparedStatement = connection.prepareStatement("SELECT max(eventID) FROM Events;");
                res = preparedStatement.executeQuery();
                while (res.next())
                    id = ((res.getInt("max(eventID)")) + 1);
                preparedStatement = connection.prepareStatement("INSERT INTO Events VALUES (?,?,?,?,?,?);");
                preparedStatement.setString(1, String.valueOf(id));
                preparedStatement.setString(2, commands[6]);
                preparedStatement.setString(3, null);
                preparedStatement.setString(4, null);
                preparedStatement.setString(5, commands[5]);
                preparedStatement.setString(6, date);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (int i = 0 ; i < client_id.size() ; i++) {
                try {
                    preparedStatement = connection.prepareStatement("INSERT INTO Event_Att VALUES (?,?,?);");
                    preparedStatement.setString(1, String.valueOf(client_id.get(i)));
                    preparedStatement.setString(2 , String.valueOf(id));
                    preparedStatement.setString(3 , String.valueOf(0));
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                br.close();
                connection.close();
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
           try {
               pw.close();
               br.close();
               connection.close();
               sock.close();
           }catch (Exception e) {
               e.printStackTrace();
           }
            return;
        }

        // Accepting the initial email address and initializing new members. COnverting the email address into user-ids.
        try {
           // email = br.readLine().trim();
            System.out.println(email);
            preparedStatement = connection.prepareStatement("SELECT userID FROM Users WHERE emailID = ?;");
            preparedStatement.setString(1, email.trim());
            res = preparedStatement.executeQuery();
            String ids;
            while(res.next()) {
                ids = res.getString(1);
                id = Integer.parseInt(ids);
            }
            if (id == 0) {
                preparedStatement = connection.prepareStatement("SELECT max(userID) FROM Users;");
                res = preparedStatement.executeQuery();
                while (res.next())
                    id = ((res.getInt("max(userID)")) + 1);
                preparedStatement = connection.prepareStatement("INSERT INTO Users VALUES (?, ?);");
                preparedStatement.setString(1, String.valueOf(id));
                preparedStatement.setString(2, email);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                pw.println("Error! Please try again later!");
                pw.flush();
                pw.close();
                br.close();
                connection.close();
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return;

        } catch (Exception fuckOff) {
            fuckOff.printStackTrace();
            try {
                pw.println("Error! Please try again later!");
                pw.flush();
                pw.close();
                br.close();
                connection.close();
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return;
        }

        // Trying to find events and sorting them into unconfirmed and unconfirmed events
        try {
            preparedStatement = connection.prepareStatement("SELECT event_ID , voted FROM Event_Att WHERE Event_Att.user_ID = ?");
            preparedStatement.setString(1, String.valueOf(id));
            res = preparedStatement.executeQuery();
            String ids = "";
            int temp = 0;

            while(res.next()) {
                ids = res.getString(1);
                temp = Integer.parseInt(ids);
                if (temp == 0)
                    break;
                if (res.getBoolean(2))
                    event_id.add(temp);
                else
                    poll_id.add(temp);
            }

            // Checking the event_ids and sending them to the device

            if (!event_id.isEmpty()) {
                String resp = "";
                for (int i = 0 ; i < event_id.size() ; i++) {
                    preparedStatement = connection.prepareStatement("SELECT * FROM Events WHERE eventID = ?;");
                    preparedStatement.setString(1 , String.valueOf(event_id.get(i)));
                    res = preparedStatement.executeQuery();
                    while(res.next()) {
                        resp += res.getString(1) + "," + res.getString(2) + ",";
                        String t = res.getString(3);
                        if (t == null)
                            resp += "Deciding,Deciding," + res.getString(5) + ",";
                        else
                            resp += t + "," + res.getString(4) + "," + res.getString(5) + ",";
                    }

                    preparedStatement = connection.prepareStatement("SELECT user_ID  FROM Event_Att WHERE Event_Att.event_ID = ?");
                    preparedStatement.setString(1 , String.valueOf(event_id.get(i)));
                    res = preparedStatement.executeQuery();
                    while(res.next()) {
                        PreparedStatement ps = connection.prepareStatement("SELECT emailID FROM Users WHERE userID = ?;" );
                        ps.setString(1 , res.getString(1));
                        ResultSet rs = ps.executeQuery();
                        while (rs.next())
                            resp += rs.getString(1) + ",";
                    }
                    resp += ";";
                }
                System.out.println(resp);
                pw.println(resp);
                pw.flush();
            }


//NEed to send the time slots, not done yet.

           else if (!poll_id.isEmpty()) {
                String resp = "";
                for (int i = 0 ; i < poll_id.size() ; i++) {
                    preparedStatement = connection.prepareStatement("SELECT * FROM Events WHERE eventID = ?;");
                    preparedStatement.setString(1 , String.valueOf(poll_id.get(i)));
                    res = preparedStatement.executeQuery();

                    while(res.next()) {

                        resp += res.getString(1) + "," + res.getString(2) + ",";

                        PreparedStatement ps =  connection.prepareStatement("SELECT COUNT(*) FROM Event_T"+poll_id.get(i));
                        ResultSet rs = ps.executeQuery();
                        while (rs.next())
                            resp += rs.getString(1) + ",";
                        ps = connection.prepareStatement("SELECT * FROM Event_T"+poll_id.get(i));
                        rs = ps.executeQuery();
                        while (rs.next())
                            resp += rs.getString(1) + "," + rs.getString(2) + ",";
                        resp += res.getString(5) + ",";

                    }

                    preparedStatement = connection.prepareStatement("SELECT user_ID  FROM Event_Att WHERE Event_Att.event_ID = ?");
                    preparedStatement.setString(1 , String.valueOf(poll_id.get(i)));
                    res = preparedStatement.executeQuery();
                    while(res.next()) {
                        PreparedStatement ps = connection.prepareStatement("SELECT emailID FROM Users WHERE userID = ?;" );
                        ps.setString(1 , res.getString(1));
                        ResultSet rs = ps.executeQuery();
                        while (rs.next())
                            resp += rs.getString(1) + ",";
                    }
                    resp += ";";
                }
                System.out.println(resp);
                pw.println(resp);
                pw.flush();
            } else {
                pw.println("No events found;");
            }
            pw.close();
            br.close();
            connection.close();
            sock.close();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                pw.println("Error! Please try again later!");
                pw.flush();
                pw.close();
                br.close();
                connection.close();
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return;
        } catch (Exception fuckOff) {
            fuckOff.printStackTrace();
            try {
                pw.println("Error! Please try again later!");
                pw.flush();
                pw.close();
                br.close();
                connection.close();
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws SQLException {
        try {
            int port = 1080;
            ServerSocket ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            Socket sock;

            while (true) {
                sock = ss.accept();
                sock.setReuseAddress(true);
                new Thread(new Server(sock)).run();
            }

        } catch (Exception dontCare) {
            dontCare.printStackTrace();
        }
    }
}
