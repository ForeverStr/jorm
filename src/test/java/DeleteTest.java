import org.example.dao.StudentDao;

public class DeleteTest {
    public static void main(String[] args) throws Exception {
        StudentDao studentDao = new StudentDao();
        studentDao.delete(1);
    }
}
