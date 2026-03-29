大学管理系统数据库设置说明
=================================

1. 文件说明：
   - complete_college_database.sql : 完整的数据库创建脚本，包含所有表结构和示例数据
   - create_database.bat : Windows批处理脚本（适用于CMD）
   - create_database.ps1 : Windows PowerShell脚本
   - DATABASE_STRUCTURE.md : 数据库结构说明文档

2. 执行步骤：
   - 确保MySQL服务已启动
   - 修改脚本中的MySQL连接参数（用户名、密码等）如果需要
   - 运行 create_database.bat 或 create_database.ps1 脚本
   - 脚本将自动创建数据库并导入所有数据

3. 默认连接参数：
   - 主机: localhost
   - 端口: 3306
   - 用户名: root
   - 密码: 123123
   - 数据库名: college_management

4. 系统用户信息：
   - 管理员: 用户名=admin, 密码=123123
   - 教师示例: 用户名=teacher1 到 teacher10, 密码=123123
   - 学生示例: 用户名=student1 到 student10, 密码=123123

5. 注意事项：
   - 所有用户密码都设置为123123
   - 数据库包含所有必要的表和至少10条示例数据
   - 包含完整的外键关系和约束
   - 支持系统的全部功能模块