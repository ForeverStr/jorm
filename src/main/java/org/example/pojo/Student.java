package org.example.pojo;

import org.example.annotation.ColumnName;
import org.example.annotation.TableId;
import org.example.annotation.TableName;

/**
 * @author huihui
 * @Project orm
 * @date 2023/6/12 14:42
 */
@TableName(value = "student")
public class Student {
    @TableId //标记主键
    private int id;
    @ColumnName(value = "s_name") //此注解使实体类和数据库中的列名一致
    private String name;
    private String gender;
    private int age;
    @ColumnName(value = "class")
    private String classs;
    private String major;

    public Student() {
    }

    public Student(int id, String name, String gender, int age, String classs, String major) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.classs = classs;
        this.major = major;
    }

    /**
     * 获取
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * 设置
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * 设置
     * @param gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * 获取
     * @return age
     */
    public int getAge() {
        return age;
    }

    /**
     * 设置
     * @param age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * 获取
     * @return classs
     */
    public String getClasss() {
        return classs;
    }

    /**
     * 设置
     * @param classs
     */
    public void setClasss(String classs) {
        this.classs = classs;
    }

    /**
     * 获取
     * @return major
     */
    public String getMajor() {
        return major;
    }

    /**
     * 设置
     * @param major
     */
    public void setMajor(String major) {
        this.major = major;
    }

    public String toString() {
        return "Student{id = " + id + ", name = " + name + ", gender = " + gender + ", age = " + age + ", classs = " + classs + ", major = " + major + "}";
    }
}
