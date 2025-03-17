package org.example.test;

import org.example.core.JormSession;
import org.example.entity.User;
import org.example.query.Query;
import org.junit.jupiter.api.Test;

import java.util.List;
public class BatchUpdateTest {

    // 批量更新
    @Test
    void testCrudOperations() {
        try (JormSession session = new JormSession()) {
            session.beginTransaction();
            try {
                Query<User> query = new Query<>(User.class, session)
                        .where("status = :status")
                        .param("status", "active");
                List<User> usersToUpdate = query.list();
                usersToUpdate.forEach(user -> user.setStatus("inactive"));
                session.batchUpdate(usersToUpdate);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException("Batch update failed", e);
            }
        }
    }

}
