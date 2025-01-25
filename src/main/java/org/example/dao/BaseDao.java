package org.example.dao;

import org.example.annotation.ColumnName;
import org.example.annotation.TableName;
import org.example.annotation.TableId;
import org.example.util.DbUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huihui
 */
public class BaseDao<T>{

    Class<T> clazz;
    public BaseDao(){
        //获取BaseDao子类的反射类对象
        Class<? extends BaseDao> aClass = this.getClass(); //getClass是this对象的反射类
        //获取当前Dao子类的父类的反射类
        ParameterizedType parameterizedType = (ParameterizedType) aClass.getGenericSuperclass();
        //得到泛型的反射类
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        clazz= (Class<T>) actualTypeArguments[0];
        //System.out.println(clazz);
    }

    //根据主键查询一条记录
    //通用的查询(一条)sql语句
    public T selectById(Object id) throws Exception{
        StringBuffer sql = new StringBuffer("select * from ");
        TableName annotation = clazz.getAnnotation(TableName.class);
        String tableName = clazz.getSimpleName();
        if(annotation!=null){
            tableName = annotation.value();
        }
        sql.append(tableName+" where ");
        //获取主键名
        Field[] fields = clazz.getDeclaredFields();//获取私有、公有
        for (Field field:fields){
            TableId tableId = field.getAnnotation(TableId.class);
            if(tableId!=null){
                sql.append(tableId.value()+"='"+id+"'");
                continue;
            }
        }
        //执行sql
        Connection connect = DbUtil.getConnect();
        PreparedStatement ps = connect.prepareStatement(sql.toString());
        ResultSet rs = ps.executeQuery();
        T t = null;
        while (rs.next()){
            //根据泛型类获得实体类对象
            t = clazz.newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();
            for(Field field: declaredFields){
                field.setAccessible(true);
                String name = field.getName();
                ColumnName columnName = field.getAnnotation(ColumnName.class);
                TableId tableId = field.getAnnotation(TableId.class);
                if(columnName!=null){
                    name = columnName.value();
                }
                if(tableId!=null){
                    name = tableId.value();
                }
                Object v = rs.getObject(name);
                field.set(t,v);
            }
        }
        return t;
    }




    //通用的删除操作
    //通用的删除sql语句:delete from 表名 where 主键=值
    public  int delete(Object id) throws Exception{
        StringBuffer sql = new StringBuffer("delete from ");
        TableName annotation = clazz.getAnnotation(TableName.class);
        String tableName = clazz.getSimpleName();
        if(annotation!=null){
            tableName = annotation.value();
        }
        sql.append(tableName+" where ");
        //获取主键名
        Field[] fields = clazz.getDeclaredFields();//获取私有、公有
        for (Field field:fields){
            TableId tableId = field.getAnnotation(TableId.class);
            if(tableId!=null){
                sql.append(tableId.value()+"='"+id+"'");
                continue;
            }
        }
        //System.out.println(sql);
        //执行sql
        Connection connection = DbUtil.getConnect();//获取连接对象
        PreparedStatement ps = connection.prepareStatement(sql.toString());
        int i = ps.executeUpdate();
        System.out.println("删除数据成功");
        return i;

    }


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



    //修改方法
    //通用的修改sql语句:update 表明 set 列名=值,列名=值... where 主键名=值;
    public int update(T t) throws Exception{
        StringBuffer sql = new StringBuffer("update ");
        //获取实体类的反射类
        Class<?> aClass = t.getClass();
        //获取反射类上指定的注解对象
        TableName annotation = aClass.getAnnotation(TableName.class);
        String tableName = aClass.getSimpleName();
        if(annotation!=null){
            tableName = annotation.value();
        }

        sql.append(tableName+ " set ");
        //获取所有的Field
        Field[] declaredFields = aClass.getDeclaredFields();
        String where = " where ";
        for (Field field:declaredFields) {
            field.setAccessible(true);//允许访问私有
            //列名
            String name = field.getName();
            TableId tableId = field.getAnnotation(TableId.class);
            //判断是否为主键
            if(name.equals("id")){
                where = where + "id='"+field.get(t)+"'";
                continue;
            }
            if(tableId!=null){
                where = where + tableId.value()+" '"+field.get(t)+"'";
                continue;
            }
            ColumnName columnName = field.getAnnotation(ColumnName.class);
            if(columnName!=null){
                name = columnName.value();
            }
            //列值
            String value = "'"+field.get(t)+"'";
            sql.append(name+"="+value+",");
        }
        //System.out.println(where);
        String sql2 = sql.toString().substring(0,sql.length()-1)+where;
        //System.out.println(sql2);

        //执行sql
        Connection connection = DbUtil.getConnect();//获取连接对象
        PreparedStatement ps = connection.prepareStatement(sql2);
        int i = ps.executeUpdate();
        System.out.println("修改数据成功");
        return i;
    }
}