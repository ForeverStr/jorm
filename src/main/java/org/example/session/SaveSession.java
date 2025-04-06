package org.example.session;

import org.example.base.BaseSession;
import org.example.core.DataSource;
import org.example.sqlBuilder.SaveBuilder;
import org.example.util.SessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
/**
 * 必须通过 try-with-resources 使用本类，例如：
 * try (SaveSession session = new SaveSession()) {
 *     session.save(entity);
 * }
 */
public class SaveSession extends BaseSession<SaveSession> {
    private static final Logger log = LoggerFactory.getLogger(SaveSession.class);

    // 自动事务模式
    public SaveSession() {
        super();
    }

    // 显示事务模式
    public SaveSession(Connection externalConn) {
        super(externalConn);
    }

    public <T> void save(T entity) {
        checkIfClosed();
        try {
            String sql = SaveBuilder.buildInsert(entity.getClass());
            log.info("预编译前的sql：{}",sql);
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
            {
                log.info("预编译后的sql：{}",stmt);
                SessionHelper.setInsertParameters(stmt, entity);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    SessionHelper.setIdValue(entity, rs.getLong(1));
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Save failed", e);
        }
    }

    @Override
    protected SaveSession self() {
        return this;
    }
}
