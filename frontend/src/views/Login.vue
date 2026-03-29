<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo">
        <h2>教学综合管理平台</h2>
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="rules" label-width="80px" class="login-form">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username" 
            prefix-icon="el-icon-user" 
            placeholder="请输入用户名" 
            autofocus
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            prefix-icon="el-icon-lock" 
            placeholder="请输入密码" 
            type="password"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="loginForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-button" 
            :loading="loading" 
            @click="handleLogin"
            style="background-color: var(--primary-color); border-color: var(--primary-color); color: var(--text-light);"
          >
            登录
          </el-button>
        </el-form-item>
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        role: 'STUDENT'
      },
      loading: false,
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 1, max: 50, message: '用户名长度在 1 到 50 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 1, message: '密码长度至少为 1 个字符', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    async handleLogin() {
      try {
        // 表单验证
        await this.$refs.loginForm.validate()
        
        this.loading = true
        
        console.log('开始登录，用户名:', this.loginForm.username, '角色:', this.loginForm.role)
        
        // 调用后端API进行登录
        const response = await this.$axios.post('/auth/login', {
          username: this.loginForm.username,
          password: this.loginForm.password,
          role: this.loginForm.role
        })
        
        console.log('登录响应:', response.data)
        
        if (response.data.success) {
          // 保存token和用户信息
          const token = response.data.data.token
          const user = response.data.data.user
          
          localStorage.setItem('token', token)
          localStorage.setItem('userInfo', JSON.stringify(user))
          
          console.log('Token已保存:', token)
          console.log('用户信息已保存:', user)
          
          // 根据角色跳转到对应页面
          this.$message.success('登录成功！正在跳转...')
          
          const role = user.role
          let redirectPath = '/'
          
          if (role === 'ADMIN') {
            redirectPath = '/admin/dashboard'
          } else if (role === 'TEACHER') {
            redirectPath = '/teacher/dashboard'
          } else if (role === 'STUDENT') {
            redirectPath = '/student/dashboard'
          }
          
          console.log('即将跳转到:', redirectPath)
          
          setTimeout(() => {
            this.$router.push(redirectPath).catch(err => {
              console.error('路由跳转失败:', err)
            })
          }, 500)
        } else {
          this.$message.error(response.data.message || '登录失败')
        }
      } catch (error) {
        console.error('登录错误:', error)
        
        let errorMessage = '登录失败，请稍后重试'
        
        if (error.response) {
          // 服务器响应了错误
          console.error('响应错误:', error.response)
          errorMessage = error.response.data?.message || `服务器错误 (${error.response.status})`
        } else if (error.request) {
          // 请求已发送但没有收到响应
          console.error('请求错误:', error.request)
          errorMessage = '无法连接到服务器，请检查后端服务是否启动'
        }
        
        this.$message.error(errorMessage)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--background-color);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 450px;
  background: var(--background-color-light);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 40px;
  animation: fadeIn 0.6s ease-in-out;
  border: 1px solid var(--border-light);
}

.logo h2 {
  text-align: center;
  color: var(--text-primary);
  margin-bottom: 30px;
  font-weight: 600;
  font-size: 24px;
}

.login-form {
  width: 100%;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--text-secondary);
  font-size: 14px;
}

.login-footer a {
  color: var(--primary-color);
  text-decoration: none;
  margin-left: 5px;
}

.login-footer a:hover {
  text-decoration: underline;
  color: var(--primary-color);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>