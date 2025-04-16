package org.example.session.factory;

import org.example.core.DataSource;
import org.example.session.DeleteSession;
import org.example.session.FindSession;
import org.example.session.SaveSession;
import org.example.session.UpdateSession;

import java.sql.Connection;

/**
 * 单一操作可直接通过工厂类执行，支持自动事务和和外部传入连接。如：
 * try (SaveSession saver = Jorm.saveSession()) {
 *     saver.save(user);
 * }
 * try (FindSession finder = Jorm.findSession(connection)) {
 *     List<User> users = finder.Where("status", "active").Find(User.class);
 * }
 * @author duyujie
 */
public class Jorm {
    public static SaveSession saveSession() {
        return new SaveSession(DataSource.getConnection());
    }
    public static FindSession findSession() {
        return new FindSession(DataSource.getConnection());
    }
    public static DeleteSession deleteSession() {
        return new DeleteSession(DataSource.getConnection());
    }
    public static UpdateSession updateSession() {
        return new UpdateSession(DataSource.getConnection());
    }
    public static SaveSession saveSession(Connection conn) {
        return new SaveSession(conn);
    }
    public static FindSession findSession(Connection conn) {
        return new FindSession(conn);
    }
    public static DeleteSession deleteSession(Connection conn) {
        return new DeleteSession(conn);
    }
    public static UpdateSession updateSession(Connection conn) {
        return new UpdateSession(conn);
    }
}
