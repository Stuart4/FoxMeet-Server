import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Temp {
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/FoxMeet", "root", "6thfloorhomies");
        Statement stmt = connection.createStatement();
        System.out.print(stmt.executeUpdate("SELECT * from users;"));
        stmt.close();
        connection.close();
    }
}
