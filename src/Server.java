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
            String command = br.readLine().trim();

            PreparedStatement preparedStatement;
            ResultSet res;
            int id = 0;

            preparedStatement = connection.prepareStatement("SELECT userID FROM Users WHERE emailID = ?;");
            preparedStatement.setString(1, command.trim());
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
                preparedStatement = connection.prepareStatement("INSERT INTO Users VALUES ( ? ,  ?);");
                preparedStatement.setString(1, String.valueOf(id));
                preparedStatement.setString(2, command);
                preparedStatement.executeUpdate();
            }
            System.out.print(id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception fuckOff) {
            System.err.println("There is an error! You Motherfucking fool");
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
            System.err.println("There is an error! I honestly don't care.");
        }
    }
}
