# MySQL数据库设置指南

由于命令行连接MySQL服务器遇到了认证问题，我们可以使用MySQL Workbench来执行SQL脚本。以下是详细步骤：

## 步骤1：启动MySQL Workbench

1. 在开始菜单中搜索并打开"MySQL Workbench"
2. 你应该会看到一个MySQL连接列表

## 步骤2：连接到MySQL服务器

1. 点击现有的连接（通常命名为"Local Instance MySQL80"）
2. 如果是第一次连接，可能会提示输入密码
   - 尝试不输入密码直接点击"OK"
   - 如果失败，尝试输入你在安装MySQL时设置的密码

## 步骤3：执行SQL脚本

1. 成功连接后，点击顶部菜单栏的"File" -> "Open SQL Script"
2. 导航到以下路径并选择文件：
   ```
   d:\mywork\StudentManagement\CollegeManagementSystem\management-system\complete_data.sql
   ```
3. 点击顶部的执行按钮（闪电图标）或按`Ctrl+Shift+Enter`执行整个脚本
4. 等待执行完成，你应该会看到成功的消息

## 步骤4：验证数据

执行完成后，可以运行以下查询来验证数据是否已正确插入：

```sql
-- 查看所有表的记录数
SELECT 'users' AS table_name, COUNT(*) AS record_count FROM users UNION ALL
SELECT 'teachers' AS table_name, COUNT(*) AS record_count FROM teachers UNION ALL
SELECT 'students' AS table_name, COUNT(*) AS record_count FROM students UNION ALL
SELECT 'courses' AS table_name, COUNT(*) AS record_count FROM courses UNION ALL
SELECT 'enrollments' AS table_name, COUNT(*) AS record_count FROM enrollments UNION ALL
SELECT 'exams' AS table_name, COUNT(*) AS record_count FROM exams UNION ALL
SELECT 'exam_results' AS table_name, COUNT(*) AS record_count FROM exam_results;
```

## 注意事项

1. 如果在执行脚本时遇到"1062 - Duplicate entry"错误，这是正常的，因为脚本中使用了`INSERT IGNORE`语句来处理重复数据
2. 所有用户的密码都是`admin123`（已通过BCrypt加密存储）
3. 脚本会创建一个名为`college_management`的数据库（如果不存在）并使用它

## 替代方法：使用命令行

如果你仍然想使用命令行，可以尝试以下方法重置root密码：

1. 停止MySQL服务：`net stop MySQL80`
2. 以跳过授权表的方式启动MySQL：
   ```
   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --skip-grant-tables
   ```
3. 重新打开一个命令行窗口，连接到MySQL：
   ```
   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root
   ```
4. 重置密码：
   ```sql
   FLUSH PRIVILEGES;
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
   ```
5. 退出MySQL并停止以跳过授权表方式运行的MySQL进程
6. 重新启动MySQL服务：`net start MySQL80`

然后你可以使用新密码来执行SQL脚本：

```
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -h localhost -u root --password=new_password < d:\mywork\StudentManagement\CollegeManagementSystem\management-system\complete_data.sql
```
