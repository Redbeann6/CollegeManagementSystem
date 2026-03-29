package com.college.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    @Transactional
    public void fixForeignKeyConstraint() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // 修改leave_requests表的handler_id外键约束，从teachers表改为users表
            statement.execute("ALTER TABLE leave_requests DROP FOREIGN KEY leave_requests_ibfk_2;");
            statement.execute("ALTER TABLE leave_requests ADD CONSTRAINT leave_requests_ibfk_2 FOREIGN KEY (handler_id) REFERENCES users(id) ON DELETE SET NULL;");
            System.out.println("Successfully fixed foreign key constraint!");
        } catch (Exception e) {
            // 如果外键约束已经存在或者其他错误，只记录日志，不影响应用启动
            System.err.println("Error fixing foreign key constraint: " + e.getMessage());
        }
    }
}
