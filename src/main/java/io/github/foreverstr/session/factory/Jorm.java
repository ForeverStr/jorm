package io.github.foreverstr.session.factory;

import javax.sql.DataSource;
import io.github.foreverstr.exception.ErrorCode;
import io.github.foreverstr.exception.JormException;
import io.github.foreverstr.session.DeleteSession;
import io.github.foreverstr.session.FindSession;
import io.github.foreverstr.session.SaveSession;
import io.github.foreverstr.session.UpdateSession;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 单一操作可直接通过工厂类执行，支持自动事务和和外部传入连接。
 * @author duyujie
 */
public class Jorm {
    // 静态变量保存 Spring 托管的 DataSource
    private static DataSource dataSource;

    // 由 Starter 模块调用此方法注入 DataSource
    public static void setDataSource(DataSource dataSource) {
        Jorm.dataSource = dataSource;
    }
    public static SaveSession saveSession() {
        return new SaveSession(getConnection());
    }
    public static FindSession findSession() {
        return new FindSession(getConnection());
    }
    public static DeleteSession deleteSession() {
        return new DeleteSession(getConnection());
    }
    public static UpdateSession updateSession() {
        return new UpdateSession(getConnection());
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

    // 统一的连接获取方法（处理异常）
    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new JormException(ErrorCode.CONNECTION_ERROR, e);
        }
    }
}
