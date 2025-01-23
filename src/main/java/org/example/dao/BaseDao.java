package org.example.dao;

import org.example.annotation.ColumnName;
import org.example.annotation.TableId;
import org.example.annotation.TableName;
import org.example.util.DbUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author duyujie
 */
public class BaseDao<T>{

    //通用的添加操作
    //通用的添加sql语句:insert into 表名(列名,列名...) values(值,值...)
    public int insert(T t) throws Exception{
        StringBuffer sql = new StringBuffer("insert into ");
        //根据对象获取Class反射类
        Class<?> aClass = t.getClass();
        //获取反射类上的注解对象
        TableName annotation = aClass.getAnnotation(TableName.class);
        String tableName = aClass.getSimpleName();
        if(annotation!=null){
            tableName = annotation.value();
        }
//        System.out.println(tableName);
        sql.append(tableName);


        //获取列名
        Field[] declaredFields = aClass.getDeclaredFields();
        //列名
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for(Field field:declaredFields){
            String name = field.getName(); //属性名
            //获取主键的注解
            Annotation annotation2 = field.getAnnotation(TableId.class);
            if(annotation2!=null||name.equals("id")){
                continue;
            }


            //拿到filed的Annotation注解
            ColumnName annotation1 = field.getAnnotation(ColumnName.class);
            if(annotation1!=null){
                name = annotation1.value();
            }
            field.setAccessible(true); //允许访问私有
            Object o = field.get(t);
            columns.add(name);
            values.add("'"+o+"'");//整形的数据加单引号不影响运行
        }
        //列名
        String columnName=columns.toString().replace("[","(").replace("]",")");
        //属性名
        String columnValues=values.toString().replace("[","(").replace("]",")");
        sql.append(columnName);
        sql.append(" values ");
        sql.append(columnValues);

        //System.out.println(sql);

        //执行sql
        Connection connection = DbUtil.getConnect();//获取连接对象
        PreparedStatement ps = connection.prepareStatement(sql.toString());
        int i = ps.executeUpdate();
        System.out.println("添加数据成功");
        return i;
    }

}
