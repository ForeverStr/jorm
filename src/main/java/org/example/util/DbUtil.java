package org.example.util;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * @author duyujie
 * @Project orm
 * @date 2025/1/23
 */
public class DbUtil {
    private static String driverName = "";
    private static String url = "";
    private static String username = "";
    private static String password = "";

    static {
        try {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream("db.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            driverName=properties.getProperty("jdbc.driverName");
            url=properties.getProperty("jdbc.url");
            username=properties.getProperty("jdbc.username");
            password=properties.getProperty("jdbc.password");
        }catch (Exception e){
            System.out.println("db.properties的属性文件不存在");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        System.out.println(getConnect());
    }

    //获取连接对象
    public static Connection getConnect() throws Exception {
        Class.forName(driverName);
        Connection connection = DriverManager.getConnection(url, username, password);
        return connection;
    }

    //关闭资源
    public static void closeAll(Connection connection, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
