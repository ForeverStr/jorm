import org.example.dao.StudentDao;
import org.example.pojo.Student;

public class UpdateTest {
    public static void main(String[] args) throws Exception {
        StudentDao studentDao = new StudentDao();
        Student student = new Student(2,"张学友","男",66,"Java五班","通信工程");
        studentDao.update(student);
    }
}
