<template>
  <div class="register-container">
    <div class="register-box">
      <div class="logo">
        <h2>账号注册</h2>
      </div>
      <el-form ref="registerForm" :model="registerForm" :rules="rules" label-width="100px" class="register-form">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="registerForm.username" 
            prefix-icon="el-icon-user" 
            placeholder="请输入用户名"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            prefix-icon="el-icon-lock" 
            placeholder="请输入密码" 
            type="password"
            show-password
            maxlength="50"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            prefix-icon="el-icon-lock" 
            placeholder="请再次输入密码" 
            type="password"
            show-password
            maxlength="50"
          ></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="registerForm.name" 
            prefix-icon="el-icon-document-copy" 
            placeholder="请输入真实姓名"
            maxlength="50"
          ></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT"></el-option>
            <el-option label="教师" value="TEACHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="registerForm.role === 'STUDENT'" label="学号" prop="studentId">
          <el-input 
            v-model="registerForm.studentId" 
            prefix-icon="el-icon-document-checked" 
            placeholder="请输入学号"
            maxlength="20"
          ></el-input>
        </el-form-item>
        <el-form-item v-if="registerForm.role === 'TEACHER'" label="工号" prop="teacherId">
          <el-input 
            v-model="registerForm.teacherId" 
            prefix-icon="el-icon-document-checked" 
            placeholder="请输入工号"
            maxlength="20"
          ></el-input>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            prefix-icon="el-icon-mobile-phone" 
            placeholder="请输入手机号码"
            maxlength="20"
          ></el-input>
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input 
            v-model="registerForm.email" 
            prefix-icon="el-icon-message" 
            placeholder="请输入电子邮箱"
            maxlength="100"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="register-button" 
            :loading="loading" 
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    // 密码确认验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    // 电子邮箱验证规则
    const validateEmail = (rule, value, callback) => {
      if (!value) {
        callback()
      } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(value)) {
          callback(new Error('请输入有效的电子邮箱地址'))
        } else {
          callback()
        }
      }
    }
    
    return {
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        name: '',
        role: 'STUDENT',
        studentId: '',
        teacherId: '',
        phone: '',
        email: ''
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
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 1, max: 50, message: '姓名长度在 1 到 50 个字符', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        studentId: [
          { required: (rule, value, callback) => {
              return this.registerForm.role === 'STUDENT' && !value
            }, 
            message: '请输入学号', 
            trigger: 'blur' 
          }
        ],
        teacherId: [
          { required: (rule, value, callback) => {
              return this.registerForm.role === 'TEACHER' && !value
            }, 
            message: '请输入工号', 
            trigger: 'blur' 
          }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { min: 1, max: 20, message: '手机号码长度在 1 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { validator: validateEmail, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async handleRegister() {
      try {
        // 表单验证
        await this.$refs.registerForm.validate()
        
        this.loading = true
        
        // 准备注册数据
        const registerData = {
          username: this.registerForm.username,
          password: this.registerForm.password,
          name: this.registerForm.name,
          role: this.registerForm.role,
          phone: this.registerForm.phone,
          email: this.registerForm.email
        }
        
        // 根据角色添加特定字段
        if (this.registerForm.role === 'STUDENT') {
          registerData.studentId = this.registerForm.studentId
        } else if (this.registerForm.role === 'TEACHER') {
          registerData.teacherId = this.registerForm.teacherId
        }
        
        // 调用注册接口
        const response = await this.$axios.post('/auth/register', registerData)
        
        if (response.data.success) {
          this.$message.success('注册成功，请登录')
          setTimeout(() => {
            this.$router.push('/login')
          }, 1500)
        } else {
          this.$message.error(response.data.message || '注册失败')
        }
      } catch (error) {
        this.$message.error(error.response?.data?.message || '注册失败，请稍后重试')
        console.error('注册错误:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 550px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  animation: fadeIn 0.6s ease-in-out;
}

.logo h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-weight: 600;
}

.register-form {
  width: 100%;
}

.register-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
  font-size: 14px;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
}

.register-footer a:hover {
  text-decoration: underline;
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