package org.example.session.base;

import org.example.session.DeleteSession;
import org.example.session.FindSession;
import org.example.session.SaveSession;
import org.example.session.UpdateSession;

import java.sql.Connection;

/**
 * 统一入口类，整合所有操作，通过内部委托实现链式连贯性。使用如下
 * <pre>
 * try (JormSession session = new JormSession(connection)) {
 *     session.saveSession().save(user);
 *     List<User> users = session.findSession()
 *                               .Where("age", ">", 18)
 *                               .Order("name DESC")
 *                               .Find(User.class);
 *     session.updateSession().Model(User.class)
 *                     .Where("id", 1)
 *                     .Set("status", "inactive")
 *                     .Update();
 * }
 * </pre>
 * @author duyujie
 * @version 1.0
 * @see SaveSession
 * @see FindSession
 * @see UpdateSession
 * @see DeleteSession
 */
public class JormSession implements AutoCloseable {
    private final Connection connection;
    private SaveSession saveSession;
    private FindSession findSession;
    private UpdateSession updateSession;
    private DeleteSession deleteSession;

    public JormSession(Connection conn) {
        this.connection = conn;
    }

    // 懒加载各会话实例
    public SaveSession saveSession() {
        if (saveSession == null) {
            saveSession = new SaveSession(connection);
        }
        return saveSession;
    }

    public FindSession findSession() {
        if (findSession == null) {
            findSession = new FindSession(connection);
        }
        return findSession;
    }
    public UpdateSession updateSession() {
        if (updateSession == null) {
            updateSession = new UpdateSession(connection);
        }
        return updateSession;
    }
    public DeleteSession deleteSession() {
        if (deleteSession == null) {
            deleteSession = new DeleteSession(connection);
        }
        return deleteSession;
    }

    @Override
    public void close() {
        // 统一关闭所有会话
        if (saveSession != null) saveSession.close();
        if (findSession != null) findSession.close();
        if (updateSession != null) updateSession.close();
        if (deleteSession != null) deleteSession.close();
    }
}
