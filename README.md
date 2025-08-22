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
- **自动事务**: 默认模式，自动提交
- **手动事务**: 精确控制事务边界
- **声明式事务**: 基于注解的事务管理

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
    <version>1.0.4</version>
</dependency>
```

**Spring Boot Starter**:
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
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

jorm:
  cache:
    redis:
      enabled: true
      default-expiration: 3600
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

### 事务管理

**手动事务**:
```java
Connection conn = TransactionManager.begin();
try (JormSession session = new JormSession(conn)) {
    // 业务操作
    TransactionManager.commit();
} catch (Exception e) {
    TransactionManager.rollback();
} finally {
    TransactionManager.release();
}
```

**声明式事务**:
```java
new TransactionTemplate().execute(() -> {
    // 事务内的操作
    return null;
});
```

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

## 配置选项

### 数据源配置
```yaml
jorm:
  maximum-pool-size: 10
  minimum-idle: 2
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
```

### 缓存配置
```yaml
jorm:
  cache:
    redis:
      enabled: true
      default-expiration: 3600
      key-prefix: "jorm:cache:"
      use-key-prefix: true
      cache-null-values: false
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
