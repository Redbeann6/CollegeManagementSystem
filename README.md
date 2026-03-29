# 高校学生管理系统

这是一个基于Vue 3和Spring Boot的高校学生管理系统，支持管理员、教师和学生三种角色。

## 技术栈

### 前端
- Vue 3
- Element Plus
- Vue Router
- Axios

### 后端
- Spring Boot 2.7.5
- Spring Security
- Spring Data JPA
- MySQL
- JWT

## 项目结构

```
├── frontend/         # 前端代码
│   ├── src/          # 源代码
│   ├── index.html    # HTML入口
│   ├── package.json  # 依赖配置
│   └── vite.config.js # Vite配置
├── management-system/ # 后端代码
│   ├── src/          # 源代码
│   ├── lib/          # 依赖jar包
│   └── pom.xml       # Maven配置
└── .gitignore        # Git忽略规则
```

## 主要功能

### 管理员
- 学生管理
- 教师管理
- 课程管理
- 院系管理
- 考试管理
- 成绩管理
- 请假管理
- 通知管理

### 教师
- 课程管理
- 学生管理
- 考试管理
- 成绩录入
- 请假审批
- 通知管理

### 学生
- 课程查看
- 选课
- 考试安排查看
- 成绩查询
- 请假申请
- 通知查看

## 安装和运行

### 前端
```bash
cd frontend
npm install
npm run dev
```

### 后端
```bash
cd management-system
mvn clean install
mvn spring-boot:run
```

## 数据库配置

1. 创建MySQL数据库
2. 执行`college_management.sql`文件初始化数据库结构
3. 在后端配置文件中修改数据库连接信息

## 注意事项

- 默认管理员账号：admin/admin123
- 首次运行前请确保数据库已正确配置
- 开发环境下，前端运行在http://localhost:3000，后端运行在http://localhost:8080