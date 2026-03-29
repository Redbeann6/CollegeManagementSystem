import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import axios from 'axios'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/css/global.css'
import { ElMessage, ElNotification } from 'element-plus'

// 创建Vue应用实例
const app = createApp(App)

// 配置axios
axios.defaults.baseURL = '/api'
axios.defaults.timeout = 10000

// 模拟登录功能 - 确保用户已经登录
async function mockLogin() {
  try {
    // 检查是否已经登录
    const existingToken = localStorage.getItem('token')
    if (existingToken) {
      console.log('用户已登录，跳过模拟登录')
      return
    }

    // 使用管理员账号模拟登录
    console.log('开始模拟登录...')
    const response = await axios.post('/api/auth/login', {
      username: 'admin',
      password: 'admin123',
      role: 'ADMIN'
    })

    if (response.data.success) {
      // 保存token和用户信息
      const token = response.data.data.token
      const user = response.data.data.user
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(user))
      console.log('模拟登录成功！Token已保存:', token)
    } else {
      console.error('模拟登录失败:', response.data.message)
    }
  } catch (error) {
    console.error('模拟登录错误:', error)
  }
}
// 请求拦截器 - 添加JWT令牌
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('[Axios Request] URL:', config.url)
    console.log('[Axios Request] Token from localStorage:', token)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('[Axios Request] Authorization header set:', config.headers.Authorization)
    }
    return config
  },
  error => {
    console.error('[Axios Request Error]:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误和刷新令牌
axios.interceptors.response.use(
  response => {
    return response
  },
  async error => {
    const originalRequest = error.config
    
    // 处理401错误 - 尝试刷新令牌
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
          // 没有刷新令牌，跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('refreshToken')
          router.push('/login')
          return Promise.reject(error)
        }
        
        // 尝试刷新令牌
        const response = await axios.post('/auth/refresh', null, {
          params: { refreshToken }
        })
        
        if (response.data && response.data.data) {
          // 保存新的令牌
          localStorage.setItem('token', response.data.data)
          
          // 更新请求头并重试请求
          originalRequest.headers.Authorization = `Bearer ${response.data.data}`
          return axios(originalRequest)
        }
      } catch (refreshError) {
        // 刷新令牌失败，跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('refreshToken')
        router.push('/login')
        return Promise.reject(refreshError)
      }
    }
    
    // 处理其他错误
    return Promise.reject(error)
  }
)

// 添加全局属性
app.config.globalProperties.$axios = axios
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$notify = ElNotification

// 配置Element Plus主题颜色 - 黑白配色
const elementPlusConfig = {
  theme: {
    primary: '#000000',
    success: '#333333',
    warning: '#555555',
    danger: '#333333',
    info: '#333333'
  }
}

// 使用插件
app.use(router)
app.use(ElementPlus, elementPlusConfig)

// 启用模拟登录功能
mockLogin()

// 挂载应用
app.mount('#app')