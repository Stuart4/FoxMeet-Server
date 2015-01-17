import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class Server implements Runnable{

    private Socket sock;


    public Server(Socket sock) {
        this.sock = sock;
    }

    public void run() {

        try {

            Connection connection =  getConnection("jdbc:mysql://localhost:3306/FoxMeet", "root", "6thfloorhomies");
            PrintWriter pw = new PrintWriter(sock.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String command = br.readLine();
            String[] commands = command.split(",");

            PreparedStatement preparedStatement;
            ResultSet res;
            double id = 0;

            System.out.println(command);

            preparedStatement = connection.prepareStatement("SELECT userID FROM Users WHERE emailID = ?;");
            preparedStatement.setString(1 , commands[0].trim());
            res = preparedStatement.executeQuery();
            String ids;
            try {
                while(res.next()) {
                    ids = res.getString(1);
                    id = Double.parseDouble(ids);
                }

            } catch (Exception e) {
                preparedStatement = connection.prepareStatement("SELECT max(userID) FROM Users;");
                res = preparedStatement.executeQuery();
                while (res.next())
                    id =(res.getDouble("max(userID)")) + 1;
                preparedStatement = connection.prepareStatement("INSERT INTO Users VALUES ( ? , ?);");
                preparedStatement.setString(1 , String.valueOf(id));
                preparedStatement.setString(2 , commands[0]);
                preparedStatement.executeUpdate();
            }
            System.out.println(id);

            if (commands[1].equals("E")) {
                preparedStatement = connection.prepareStatement(" SELECT eventID ");
            }




        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception fuckOff) {
            System.err.println("error");
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
            main(args);
        }
    }
}
