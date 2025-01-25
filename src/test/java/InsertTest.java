import org.example.dao.StudentDao;
import org.example.pojo.Student;

public class InsertTest {
    public static void main(String[] args) throws Exception {
        StudentDao studentDao = new StudentDao();
        Student student = new Student();
        student.setName("刘德华");
        student.setGender("男");
        student.setAge(60);
        student.setClasss("qy165");
        student.setMajor("计算机科学与技术");
        studentDao.insert(student);
    }
}
