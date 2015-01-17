import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class Temp {
    public static void main(String[] args) throws SQLException {
        PreparedStatement preparedStatement;
        Connection connection =  getConnection("jdbc:mysql://localhost:3306/FoxMeet", "root", "6thfloorhomies");
        preparedStatement = connection.prepareStatement("INSERT INTO Event_T1 VALUES (321223 , 431232 , 0)");
        preparedStatement.executeUpdate();

    }
}
