<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <el-icon><Menu /></el-icon>
          <span class="title">高校学生管理系统 - 管理员</span>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="36" :src="userInfo?.avatar || ''" icon="el-icon-user-solid" />
            <span class="user-name">{{ userInfo?.name || '管理员' }}</span>
            <el-icon class="icon-arrow-down"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="info">个人信息</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <div class="container">
      <!-- 侧边栏菜单 -->
      <el-aside class="aside" width="240px">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          router
          background-color="#ffffff"
          text-color="#212529"
          active-text-color="#000000"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <el-sub-menu index="1">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/students">
              <template #title>学生管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/teachers">
              <template #title>教师管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/departments">
              <template #title>院系管理</template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>教学管理</span>
            </template>
            <el-menu-item index="/admin/courses">
              <template #title>课程管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/enrollments">
              <template #title>选课管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/timetable">
              <template #title>课表管理</template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="3">
            <template #title>
              <el-icon><Timer /></el-icon>
              <span>考试成绩</span>
            </template>
            <el-menu-item index="/admin/exams">
              <template #title>考试管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/grades">
              <template #title>成绩管理</template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="4">
            <template #title>
              <el-icon><Comment /></el-icon>
              <span>请假通知</span>
            </template>
            <el-menu-item index="/admin/leave">
              <template #title>请假管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/notifications">
              <template #title>
                通知管理
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="item" />
              </template>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </div>
    
    <!-- 确认对话框 -->
    <el-dialog v-model="dialogVisible" title="确认操作" width="30%">
      <span>{{ dialogContent }}</span>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDialog">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Menu, ArrowDown, HomeFilled, User, Document, Timer, Ticket, Comment, Setting } from '@element-plus/icons-vue'

export default {
  name: 'AdminLayout',
  components: {
    Menu,
    ArrowDown,
    HomeFilled,
    User,
    Document,
    Timer,
    Ticket,
    Comment,
    Setting
  },
  data() {
    return {
      userInfo: null,
      unreadCount: 0,
      dialogVisible: false,
      dialogContent: '',
      dialogAction: ''
    }
  },
  
  created() {
    // 设置全局函数来更新未读消息数量
    window.updateGlobalUnreadCount = (count) => {
      this.unreadCount = count;
    };
    // 添加事件监听器，当用户信息更新时自动刷新
    window.addEventListener('userInfoUpdated', () => {
      this.loadUserInfo();
    });
  },
  
  computed: {
    // 计算当前活动菜单
    activeMenu() {
      return this.$route.path;
    }
  },
  
  mounted() {
    console.log('管理员Layout组件已挂载')
    this.loadUserInfo()
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      try {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          this.userInfo = JSON.parse(userInfoStr)
          console.log('管理员Layout加载用户信息:', this.userInfo)
        } else {
          console.warn('Layout: 未找到用户信息')
        }
      } catch (error) {
        console.error('Layout: 加载用户信息失败:', error)
      }
    },
    // 处理下拉菜单命令
    handleCommand(command) {
      switch (command) {
        case 'info':
          this.$router.push('/admin/profile')
          break
        case 'password':
          this.$message('修改密码功能开发中')
          break
        case 'logout':
          this.dialogContent = '确定要退出登录吗？'
          this.dialogAction = 'logout'
          this.dialogVisible = true
          break
      }
    },
    // 确认对话框操作
    confirmDialog() {
      if (this.dialogAction === 'logout') {
        this.logout()
      }
      this.dialogVisible = false
    },
    // 退出登录
    logout() {
      // 清除localStorage中的token和用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      // 跳转到登录页
      this.$router.push('/login')
      
      this.$message.success('退出登录成功')
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 72px !important;
  background-color: var(--background-color-light);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: var(--box-shadow-base);
  border-bottom: 1px solid var(--border-light);
  transition: var(--transition-smooth);
}

.header-left .logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 16px;
  padding: 8px 12px;
  border-radius: 12px;
  transition: var(--transition-fast);
}

.header-left .logo:hover {
  background-color: var(--border-lighter);
}

.header-left .logo .title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.header-right .user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 16px;
  transition: var(--transition-smooth);
  background-color: var(--border-lighter);
  gap: 12px;
}

.header-right .user-info:hover {
  background-color: var(--border-light);
  box-shadow: var(--box-shadow-base);
}

.header-right .user-name {
  margin: 0;
  color: var(--text-regular);
  font-weight: 500;
  font-size: 14px;
}

.icon-arrow-down {
  font-size: 14px;
  color: var(--text-secondary);
  transition: var(--transition-fast);
}

.header-right .user-info:hover .icon-arrow-down {
  color: var(--text-regular);
  transform: translateY(2px);
}

.container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.aside {
  background-color: var(--background-color-light);
  box-shadow: var(--box-shadow-base);
  border-right: 1px solid var(--border-light);
  transition: var(--transition-smooth);
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
  background-color: transparent;
  padding: 16px 0;
  border-radius: 0 16px 16px 0;
}

.el-menu-vertical .el-menu-item, .el-menu-vertical .el-sub-menu__title {
  height: 52px;
  line-height: 52px;
  padding-left: 24px !important;
  border-radius: 0 12px 12px 0;
  margin: 0 8px;
  transition: var(--transition-smooth);
}

.el-menu-vertical .el-menu-item:hover, .el-menu-vertical .el-sub-menu__title:hover {
  background-color: var(--border-lighter);
}

.el-menu-vertical .el-menu-item.is-active {
  background-color: rgba(59, 130, 246, 0.1);
  color: var(--primary-accent);
  font-weight: 600;
}

.main {
  padding: 32px;
  overflow-y: auto;
  background-color: var(--background-color);
  flex: 1;
  min-height: 0;
  border-radius: 16px 0 0 0;
  transition: var(--transition-smooth);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .aside {
    width: 0;
    overflow: hidden;
    box-shadow: none;
  }
  
  .main {
    padding: 20px;
    border-radius: 0;
  }
  
  .header {
    padding: 0 16px;
    height: 64px !important;
  }
  
  .header-left .logo .title {
    font-size: 16px;
  }
}
</style>