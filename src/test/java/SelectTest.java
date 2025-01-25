import org.example.dao.StudentDao;

public class SelectTest {
    public static void main(String[] args) throws Exception {
        StudentDao studentDao = new StudentDao();
        System.out.println(studentDao.selectById(2));
    }
}
