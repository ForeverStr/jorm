# JORM - Java ORM Framework

[![Maven Central](https://img.shields.io/maven-central/v/io.github.foreverstr/jorm.svg)](https://search.maven.org/artifact/io.github.foreverstr/jorm)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

## 概述

JORM 是一个灵感来源于 GORM 的轻量级 Java ORM 框架，提供了简洁的 API 和强大的数据库操作能力。框架包含核心模块和 Spring Boot Starter，可独立使用或与 Spring Boot 无缝集成。

## 核心特性

### 🚀 简洁的链式 API
```java
// 查询示例
List<User> users = new FindSession()
    .Where("age", ">", 18)
    .Where("status", "active")
    .Order("name DESC")
    .Limit(10)
    .Find(User.class);

// 更新示例
new UpdateSession()
    .Model(User.class)
    .Where("id", 1)
    .Set("name", "John")
    .Set("age", 25)
    .Update();
```

### 🔄 多种事务管理方式
- **编程式事务**: 面向使用核心模块的用户，提供轻量且可选的事务方案
```java
// 1. 自动事务（单会话操作）
try (SaveSession session = new SaveSession()) {
        // 默认开启自动事务，操作完成自动提交
        session.save(user);
}
// 2. 手动事务管理
Connection conn = TransactionManager.begin();
try (JormSession session = new JormSession(conn)) {
        // 业务操作
        session.save(user1);
    session.save(user2);
    TransactionManager.commit();
} catch (Exception e) {
        TransactionManager.rollback();
    throw new RuntimeException("事务执行失败", e);
} finally {
        TransactionManager.release(); // 必须释放连接，否则会造成连接泄漏
}
// 3. 自定义TransactionTemplate简化
        new TransactionTemplate().execute(() -> {
        // 事务内的操作
        try (SaveSession session = new SaveSession()) {
        session.save(user1);
        session.save(user2);
    }
            return null;
            }); 
```
- **声明式事务**: 面向使用starter模块的Spring用户
```java
@Service
public class UserService {
    @Transactional// jorm会自动加入spring事务
    public void batchOperation() {
        try (FindSession findSession = new FindSession();
             UpdateSession updateSession = new UpdateSession()) {

            // 查询和更新都在同一个Spring事务内
            List<User> users = findSession.Where("status", "active").Find(User.class);
            for (User user : users) {
                updateSession.Model(User.class)
                        .Where("id", user.getId())
                        .Set("age", user.getAge() + 1)
                        .Update();
            }
        }
    }
}
```

### ⚡ 二级缓存支持
集成 Redis 作为二级缓存，大幅提升查询性能：
```yaml
jorm:
  cache:
    redis:
      enabled: true
      default-expiration: 3600
      key-prefix: "jorm:cache:"
```

### 📊 丰富的查询功能
- 条件查询（WHERE）
- 分组查询（GROUP BY）
- 聚合查询（HAVING）
- 排序（ORDER BY）
- 分页（LIMIT）

### 🛡 完善的异常处理
统一的异常体系，提供清晰的错误码和错误信息：
```java
public enum ErrorCode {
    RESULT_MAPPING_FAILED("10001", "结果映射失败"),
    DUPLICATE_KEY("10015", "主键冲突"),
    TRANSACTION_COMMIT_FAILED("30002", "事务提交失败")
    // ... 40+ 错误码
}
```

## 快速开始

### 1. 添加依赖

**核心框架**:
```xml
<dependency>
    <groupId>io.github.foreverstr</groupId>
    <artifactId>jorm</artifactId>
    <version>1.0.8</version>
</dependency>
```

**Starter模块**:
```xml
<dependency>
    <groupId>io.github.foreverstr</groupId>
    <artifactId>jorm-spring-boot-starter</artifactId>
    <version>1.0.5</version>
</dependency>
```

### 2. 配置数据源

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:tc:mysql:8.0:///orm?TC_INITSCRIPT=schema.sql
    username: root
    password: root
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

jorm:
  jdbc-url: ${spring.datasource.url}
  username: ${spring.datasource.username}
  password: ${spring.datasource.password}
  driver-class-name: ${spring.datasource.driver-class-name}
  maximum-pool-size: 5
  minimum-idle: 1

  cache:
    redis:
      enabled: true
      default-expiration: 60
      key-prefix: jorm:test:
      use-key-prefix: true
      cache-null-values: false

logging:
  level:
    io.github.foreverstr: DEBUG
    org.springframework.jdbc: DEBUG
    org.testcontainers: INFO
```

### 3. 定义实体类

```java
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_name", nullable = false)
    private String name;
    
    private int age;
    private String status;
    
    @Aggregation
    private int totalAge; // 聚合字段，不持久化
    
    // 构造方法、getter、setter
}
```

### 4. 使用示例

**基本 CRUD 操作**:
```java
// 保存
try (SaveSession session = new SaveSession()) {
    User user = new User("John", 25, "active");
    session.save(user);
}

// 查询
try (FindSession session = new FindSession()) {
    List<User> users = session.Where("status", "active")
                             .Order("age DESC")
                             .Find(User.class);
}

// 更新
try (UpdateSession session = new UpdateSession()) {
    session.Model(User.class)
           .Where("id", 1)
           .Set("name", "John Updated")
           .Update();
}

// 删除
try (DeleteSession session = new DeleteSession()) {
    session.Where("id", 1).Delete(User.class);
}
```

**Spring Boot 集成**:
```java
@Autowired
private JormSession jormSession;

public void doBusiness() {
    jormSession.saveSession().save(user);
    List<User> users = jormSession.findSession()
        .Where("age", ">", 18)
        .Find(User.class);
}
```

## 高级特性

### 批量操作

```java
// 批量插入
List<User> users = Arrays.asList(user1, user2, user3);
List<Long> ids = saveSession.batchSave(users);

// 批量删除
deleteSession.Delete(users);
```

### 缓存管理

```java
// 手动清理缓存
CacheManager.getSecondLevelCache().clearRegion(User.class.getName());

// 禁用缓存
CacheManager.setCacheEnabled(false);
```
## 版本要求

- Java 11+
- MySQL 5.7+ / PostgreSQL / H2（测试）
- Spring Boot 2.7.x（可选）

## 贡献

欢迎提交 Issue 和 Pull Request！请确保代码符合项目的代码规范。

## 许可证

Apache License 2.0

## 支持

如有问题，请通过以下方式联系：
- GitHub Issues: [https://github.com/ForeverStr/jorm/issues](https://github.com/ForeverStr/jorm/issues)
- 邮箱: wy1903265502@163.com

---

**JORM** - 让 Java 数据库操作更简单！
