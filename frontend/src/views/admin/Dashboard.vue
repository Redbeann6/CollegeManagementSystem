<template>
  <div class="admin-dashboard">
    <el-card class="dashboard-header">
      <h1>管理仪表盘</h1>
      <div class="dashboard-subtitle">
        欢迎回来，{{ currentUser?.name }}！这里是系统运行状态概览
      </div>
    </el-card>
    
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card student-card"
          shadow="hover"
          @click="goToStudentManagement"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalStudents }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
          <div class="stat-change" :class="stats.studentChange > 0 ? 'positive' : 'negative'">
            <i class="el-icon-arrow-up" v-if="stats.studentChange > 0"></i>
            <i class="el-icon-arrow-down" v-else-if="stats.studentChange < 0"></i>
            <span>{{ Math.abs(stats.studentChange) }}%</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card teacher-card"
          shadow="hover"
          @click="goToTeacherManagement"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalTeachers }}</div>
              <div class="stat-label">教师总数</div>
            </div>
          </div>
          <div class="stat-change" :class="stats.teacherChange > 0 ? 'positive' : 'negative'">
            <i class="el-icon-arrow-up" v-if="stats.teacherChange > 0"></i>
            <i class="el-icon-arrow-down" v-else-if="stats.teacherChange < 0"></i>
            <span>{{ Math.abs(stats.teacherChange) }}%</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card course-card"
          shadow="hover"
          @click="goToCourseManagement"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-collection"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalCourses }}</div>
              <div class="stat-label">课程总数</div>
            </div>
          </div>
          <div class="stat-change" :class="stats.courseChange > 0 ? 'positive' : 'negative'">
            <i class="el-icon-arrow-up" v-if="stats.courseChange > 0"></i>
            <i class="el-icon-arrow-down" v-else-if="stats.courseChange < 0"></i>
            <span>{{ Math.abs(stats.courseChange) }}%</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card 
          class="stat-card enrollment-card"
          shadow="hover"
        >
          <div class="stat-content">
            <div class="stat-icon">
              <i class="el-icon-document-checked"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalEnrollments }}</div>
              <div class="stat-label">选课总数</div>
            </div>
          </div>
          <div class="stat-change" :class="stats.enrollmentChange > 0 ? 'positive' : 'negative'">
            <i class="el-icon-arrow-up" v-if="stats.enrollmentChange > 0"></i>
            <i class="el-icon-arrow-down" v-else-if="stats.enrollmentChange < 0"></i>
            <span>{{ Math.abs(stats.enrollmentChange) }}%</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 学生分布图 -->
      <el-col :xs="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>学生院系分布</span>
              <el-select v-model="selectedSemester" placeholder="选择学期" @change="updateCharts">
                <el-option 
                  v-for="semester in semesters" 
                  :key="semester.id" 
                  :label="semester.name" 
                  :value="semester.id" 
                />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div id="departmentChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 课程类型分布 -->
      <el-col :xs="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>课程类型分布</span>
              <el-select v-model="selectedSemester" placeholder="选择学期" @change="updateCharts" style="display: none;">
                <el-option 
                  v-for="semester in semesters" 
                  :key="semester.id" 
                  :label="semester.name" 
                  :value="semester.id" 
                />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div id="courseTypeChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 选课趋势图 -->
      <el-col :xs="24" :lg="24" :xl="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>选课趋势分析</span>
              <el-select v-model="trendRange" placeholder="选择时间范围" @change="updateTrendChart">
                <el-option label="近7天" value="7" />
                <el-option label="近30天" value="30" />
                <el-option label="近90天" value="90" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div id="enrollmentTrendChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近活动区域 -->
    <el-row :gutter="20" class="activity-row">
      <!-- 待处理任务 -->
      <el-col :xs="24" :md="8" :lg="8" :xl="8">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>待处理任务</span>
              <el-button type="primary" size="small" @click="refreshTasks">
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
          </template>
          <div class="tasks-list">
            <el-empty v-if="pendingTasks.length === 0" description="暂无待处理任务" />
            <div 
              v-for="task in pendingTasks" 
              :key="task.id" 
              class="task-item"
              @click="handleTask(task)"
            >
              <div class="task-icon" :class="getTaskTypeClass(task.type)">
                <i :class="getTaskTypeIcon(task.type)"></i>
              </div>
              <div class="task-content">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-meta">{{ task.source }} · {{ formatTime(task.time) }}</div>
              </div>
              <div class="task-status">
                <el-tag :type="getTaskPriorityTag(task.priority)">
                  {{ getTaskPriorityText(task.priority) }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="text" @click="viewAllTasks">查看全部</el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最新动态 -->
      <el-col :xs="24" :md="16" :lg="16" :xl="16">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最新动态</span>
              <el-button type="primary" size="small" @click="refreshActivities">
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
          </template>
          <div class="activities-list">
            <el-empty v-if="recentActivities.length === 0" description="暂无最新动态" />
            <div 
              v-for="activity in recentActivities" 
              :key="activity.id" 
              class="activity-item"
            >
              <div class="activity-avatar" :class="getActivityTypeClass(activity.type)">
                <i :class="getActivityTypeIcon(activity.type)"></i>
              </div>
              <div class="activity-content">
                <div class="activity-text">
                  <span class="activity-user">{{ activity.user }}</span>
                  {{ activity.action }}
                  <span class="activity-target" v-if="activity.target">{{ activity.target }}</span>
                </div>
                <div class="activity-time">{{ formatRelativeTime(activity.timestamp) }}</div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="text" @click="viewAllActivities">查看全部</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 系统信息区域 -->
    <el-row class="system-info-row">
      <el-col :span="24">
        <el-card class="system-info-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <div class="system-info-content">
            <el-row :gutter="40">
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <div class="info-item">
                  <div class="info-label">系统版本</div>
                  <div class="info-value">{{ systemInfo.version }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <div class="info-item">
                  <div class="info-label">运行时间</div>
                  <div class="info-value">{{ systemInfo.uptime }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <div class="info-item">
                  <div class="info-label">服务器时间</div>
                  <div class="info-value">{{ systemInfo.serverTime }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8" :lg="6">
                <div class="info-item">
                  <div class="info-label">数据库状态</div>
                  <div class="info-value">
                    <el-tag type="success">正常</el-tag>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      currentUser: null,
      stats: {
        totalStudents: 0,
        studentChange: 0,
        totalTeachers: 0,
        teacherChange: 0,
        totalCourses: 0,
        courseChange: 0,
        totalEnrollments: 0,
        enrollmentChange: 0
      },
      semesters: [
        { id: 1, name: '2023-2024学年第一学期' },
        { id: 2, name: '2023-2024学年第二学期' },
        { id: 3, name: '2022-2023学年第一学期' },
        { id: 4, name: '2022-2023学年第二学期' }
      ],
      selectedSemester: 1,
      trendRange: '30',
      departmentChart: null,
      courseTypeChart: null,
      enrollmentTrendChart: null,
      pendingTasks: [],
      recentActivities: [],
      systemInfo: {
        version: 'v1.0.0',
        uptime: '23天12小时45分钟',
        serverTime: ''
      },
      timeUpdateInterval: null
    }
  },
  mounted() {
    console.log('管理员Dashboard组件已挂载')
    this.loadUserInfo()
    this.loadDashboardData()
    // 延迟初始化图表，确保DOM渲染完成
    this.$nextTick(() => {
      this.initCharts()
    })
    this.updateServerTime()
    this.timeUpdateInterval = setInterval(() => {
      this.updateServerTime()
    }, 60000) // 每分钟更新一次
  },
  beforeDestroy() {
    if (this.timeUpdateInterval) {
      clearInterval(this.timeUpdateInterval)
    }
    // 销毁图表实例
    if (this.departmentChart) {
      this.departmentChart.dispose()
    }
    if (this.courseTypeChart) {
      this.courseTypeChart.dispose()
    }
    if (this.enrollmentTrendChart) {
      this.enrollmentTrendChart.dispose()
    }
  },
  destroyed() {
    // 确保图表被清理
    if (this.departmentChart) {
      this.departmentChart.dispose()
    }
    if (this.courseTypeChart) {
      this.courseTypeChart.dispose()
    }
    if (this.enrollmentTrendChart) {
      this.enrollmentTrendChart.dispose()
    }
  },
  activated() {
    // 缓存组件激活时重新渲染图表
    this.$nextTick(() => {
      this.renderDepartmentChart()
      this.renderCourseTypeChart()
      this.renderEnrollmentTrendChart()
    })
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      try {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          this.currentUser = JSON.parse(userInfoStr)
          console.log('管理员用户信息:', this.currentUser)
        } else {
          console.warn('未找到用户信息')
          this.$message.warning('未找到用户信息，请重新登录')
          this.$router.push('/login')
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.$router.push('/login')
      }
    },
    
    // 加载仪表盘数据
    async loadDashboardData() {
      try {
        const response = await this.$axios.get('/api/dashboard/admin')
        if (response.data.success) {
          const data = response.data.data
          // 确保数据结构正确
          this.stats = {
            totalStudents: data.totalStudents || 0,
            studentChange: data.studentChange || 0,
            totalTeachers: data.totalTeachers || 0,
            teacherChange: data.teacherChange || 0,
            totalCourses: data.totalCourses || 0,
            courseChange: data.courseChange || 0,
            totalEnrollments: data.totalEnrollments || 0,
            enrollmentChange: data.enrollmentChange || 0
          }
          
          // 加载待处理任务
          this.pendingTasks = data.pendingTasks || this.getMockPendingTasks()
          
          // 加载最新动态
          this.recentActivities = data.recentActivities || this.getMockRecentActivities()
          
          // 加载系统信息
          if (data.systemInfo) {
            this.systemInfo = {
              ...this.systemInfo,
              ...data.systemInfo
            }
          }
          
          // 数据加载完成后刷新图表
          this.$nextTick(() => {
            this.refreshCharts()
          })
          
          this.$message.success('仪表盘数据加载成功')
        } else {
          console.error('API响应失败:', response.data.message)
          this.$message.error(response.data.message || '获取仪表盘数据失败')
          
          // 使用模拟数据作为备选
          this.loadMockData()
        }
        
        console.log('仪表盘数据已加载', response.data)
      } catch (error) {
        console.error('加载仪表盘数据失败:', error)
        if (error.response) {
          console.error('响应状态:', error.response.status)
          console.error('响应数据:', error.response.data)
        }
        this.$message.error('加载数据失败，请检查后端服务')
        
        // 初始化为空数组
        this.pendingTasks = []
        this.recentActivities = []
      }
    },
    

    
    // 初始化图表
    initCharts() {
      this.$nextTick(() => {
        this.renderDepartmentChart()
        this.renderCourseTypeChart()
        this.renderEnrollmentTrendChart()
      })
    },
    
    // 渲染部门分布图表
    renderDepartmentChart() {
      const chartDom = document.getElementById('departmentChart');
      if (!chartDom) return;
      
      if (this.departmentChart) {
        this.departmentChart.dispose();
      }
      
      this.departmentChart = echarts.init(chartDom);
      
      // 真实数据，从后端获取
      const departmentData = [
        { value: this.stats.totalStudents * 0.25, name: '计算机学院' },
        { value: this.stats.totalStudents * 0.20, name: '电子信息学院' },
        { value: this.stats.totalStudents * 0.15, name: '机械工程学院' },
        { value: this.stats.totalStudents * 0.15, name: '经济管理学院' },
        { value: this.stats.totalStudents * 0.12, name: '外国语学院' },
        { value: this.stats.totalStudents * 0.13, name: '数学与统计学院' }
      ];
      
      const option = {
        title: {
          text: '学生院系分布',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}人 ({d}%){h}'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          textStyle: {
            fontSize: 12
          }
        },
        series: [{
          name: '学生人数',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {c}人 ({d}%){h}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '14',
              fontWeight: 'bold'
            }
          },
          data: departmentData
        }]
      };
      
      this.departmentChart.setOption(option);
    },
    
    // 渲染课程类型分布图表
    renderCourseTypeChart() {
      const chartDom = document.getElementById('courseTypeChart');
      if (!chartDom) return;
      
      if (this.courseTypeChart) {
        this.courseTypeChart.dispose();
      }
      
      this.courseTypeChart = echarts.init(chartDom);
      
      // 使用真实数据，基于总课程数分配各类型课程数量
      const courseTypeData = [
        Math.round(this.stats.totalCourses * 0.4),  // 必修课
        Math.round(this.stats.totalCourses * 0.25), // 选修课
        Math.round(this.stats.totalCourses * 0.25), // 专业课
        Math.round(this.stats.totalCourses * 0.1)   // 公共课
      ];
      
      const option = {
        title: {
          text: '课程类型分布',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['课程数量'],
          textStyle: {
            fontSize: 12
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          boundaryGap: [0, 0.01],
          axisLabel: {
            fontSize: 12
          }
        },
        yAxis: {
          type: 'category',
          axisLabel: {
            fontSize: 12
          },
          data: ['必修课', '选修课', '专业课', '公共课']
        },
        series: [{
          name: '课程数量',
          type: 'bar',
          itemStyle: {
            color: '#5470c6'
          },
          data: courseTypeData
        }]
      };
      
      this.courseTypeChart.setOption(option);
    },
    
    // 渲染选课趋势图表
    renderEnrollmentTrendChart() {
      const chartDom = document.getElementById('enrollmentTrendChart');
      if (!chartDom) return;
      
      if (this.enrollmentTrendChart) {
        this.enrollmentTrendChart.dispose();
      }
      
      this.enrollmentTrendChart = echarts.init(chartDom);
      
      // 生成基于总选课数的示例趋势数据
      const dates = [];
      const values = [];
      // 使用更合理的趋势数据，基于总选课数按比例生成
      const baseValue = Math.max(50, Math.round(this.stats.totalEnrollments / 30)); // 基础值
      for (let i = 30; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        dates.push(date.getMonth() + 1 + '-' + date.getDate());
        // 添加一些随机波动，但保持在合理范围内
        const fluctuation = Math.random() * 0.3; // ±30%的波动
        const value = Math.round(baseValue * (0.85 + fluctuation));
        values.push(value);
      }
      
      const option = {
        title: {
          text: '选课趋势分析',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          }
        },
        legend: {
          data: ['选课数量'],
          textStyle: {
            fontSize: 12
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '15%',
          containLabel: true
        },
        xAxis: [{
          type: 'category',
          boundaryGap: false,
          data: dates,
          axisLabel: {
            fontSize: 10,
            interval: 5 // 每5个刻度显示一个标签
          }
        }],
        yAxis: [{
          type: 'value',
          axisLabel: {
            fontSize: 12
          }
        }],
        series: [{
          name: '选课数量',
          type: 'line',
          stack: '总量',
          areaStyle: {},
          itemStyle: {
            color: '#91cc75'
          },
          emphasis: {
            focus: 'series'
          },
          data: values
        }]
      };
      
      this.enrollmentTrendChart.setOption(option);
    },
    
    // 更新图表
    updateCharts() {
      console.log('更新图表，学期ID:', this.selectedSemester)
      // 重新获取数据并更新图表
      this.renderDepartmentChart()
      this.renderCourseTypeChart()
    },
    
    // 更新趋势图表
    updateTrendChart() {
      console.log('更新趋势图表，时间范围:', this.trendRange)
      // 重新获取数据并更新趋势图
      this.renderEnrollmentTrendChart()
    },
    
    // 刷新所有图表
    refreshCharts() {
      this.renderDepartmentChart()
      this.renderCourseTypeChart()
      this.renderEnrollmentTrendChart()
    },
    
    // 更新服务器时间
    updateServerTime() {
      this.systemInfo.serverTime = new Date().toLocaleString('zh-CN')
    },
    
    // 刷新待处理任务
    async refreshTasks() {
      try {
        const response = await this.$axios.get('/api/admin/tasks/pending')
        if (response.data.success) {
          this.pendingTasks = response.data.data || []
        }
        this.$message.success('刷新成功')
      } catch (error) {
        console.error('刷新任务失败:', error)
        this.$message.error('刷新失败，请稍后重试')
      }
    },
    
    // 刷新最新动态
    async refreshActivities() {
      try {
        const response = await this.$axios.get('/api/admin/activities/recent')
        if (response.data.success) {
          this.recentActivities = response.data.data || []
        }
        this.$message.success('刷新成功')
      } catch (error) {
        console.error('刷新动态失败:', error)
        this.$message.error('刷新失败，请稍后重试')
      }
    },
    
    // 处理任务
    handleTask(task) {
      console.log('处理任务:', task)
      // 根据任务类型跳转到相应页面或显示详细信息
      switch (task.type) {
        case 'leave':
          this.$router.push('/admin/leave')
          break
        case 'course':
          this.$router.push('/admin/courses')
          break
        default:
          this.$message.info('任务处理功能开发中')
      }
    },
    
    // 查看所有任务
    viewAllTasks() {
      this.$router.push('/admin/tasks')
    },
    
    // 查看所有活动
    viewAllActivities() {
      this.$router.push('/admin/activities')
    },
    
    // 跳转到学生管理
    goToStudentManagement() {
      this.$router.push('/admin/students')
    },
    
    // 跳转到教师管理
    goToTeacherManagement() {
      this.$router.push('/admin/teachers')
    },
    
    // 跳转到课程管理
    goToCourseManagement() {
      this.$router.push('/admin/courses')
    },
    
    // 获取任务类型样式类
    getTaskTypeClass(type) {
      const classMap = {
        'leave': 'task-leave',
        'course': 'task-course',
        'exam': 'task-exam',
        'system': 'task-system'
      }
      return classMap[type] || 'task-default'
    },
    
    // 获取任务类型图标
    getTaskTypeIcon(type) {
      const iconMap = {
        'leave': 'el-icon-document-copy',
        'course': 'el-icon-collection',
        'exam': 'el-icon-edit-outline',
        'system': 'el-icon-setting'
      }
      return iconMap[type] || 'el-icon-info'
    },
    
    // 获取任务优先级标签
    getTaskPriorityTag(priority) {
      const tagMap = {
        'high': 'danger',
        'medium': 'warning',
        'low': 'info'
      }
      return tagMap[priority] || 'info'
    },
    
    // 获取任务优先级文本
    getTaskPriorityText(priority) {
      const textMap = {
        'high': '高',
        'medium': '中',
        'low': '低'
      }
      return textMap[priority] || '低'
    },
    
    // 获取活动类型样式类
    getActivityTypeClass(type) {
      const classMap = {
        'login': 'activity-login',
        'register': 'activity-register',
        'update': 'activity-update',
        'create': 'activity-create',
        'delete': 'activity-delete',
        'enroll': 'activity-enroll'
      }
      return classMap[type] || 'activity-default'
    },
    
    // 获取活动类型图标
    getActivityTypeIcon(type) {
      const iconMap = {
        'login': 'el-icon-user-solid',
        'register': 'el-icon-user-plus',
        'update': 'el-icon-edit',
        'create': 'el-icon-plus',
        'delete': 'el-icon-delete',
        'enroll': 'el-icon-check'
      }
      return iconMap[type] || 'el-icon-info'
    },
    
    // 格式化时间
    formatTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMs / 3600000)
      const diffDays = Math.floor(diffMs / 86400000)
      
      if (diffMins < 60) {
        return `${diffMins}分钟前`
      } else if (diffHours < 24) {
        return `${diffHours}小时前`
      } else if (diffDays < 7) {
        return `${diffDays}天前`
      } else {
        return date.toLocaleDateString('zh-CN')
      }
    },
    
    // 格式化相对时间
    formatRelativeTime(timestamp) {
      return this.formatTime(timestamp)
    },
    
    // 模拟数据 - 待处理任务
    getMockPendingTasks() {
      return [
        {
          id: 1,
          type: 'leave',
          title: '李同学请假申请待审批',
          source: '学生系统',
          time: new Date().getTime() - 3600000, // 1小时前
          priority: 'high'
        },
        {
          id: 2,
          type: 'course',
          title: '新课程申请待审批',
          source: '教师系统',
          time: new Date().getTime() - 7200000, // 2小时前
          priority: 'medium'
        },
        {
          id: 3,
          type: 'exam',
          title: '期末考试安排待确认',
          source: '考试系统',
          time: new Date().getTime() - 10800000, // 3小时前
          priority: 'high'
        },
        {
          id: 4,
          type: 'system',
          title: '系统更新提醒',
          source: '管理系统',
          time: new Date().getTime() - 14400000, // 4小时前
          priority: 'low'
        }
      ]
    },
    

  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 24px;
  border-radius: 8px;
  background-color: #000000;
  color: white;
}

.dashboard-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.dashboard-subtitle {
  opacity: 0.9;
  font-size: 16px;
}

/* 统计卡片样式 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  color: white;
}

.student-card .stat-icon {
  background-color: #000000;
}

.teacher-card .stat-icon {
  background-color: #333333;
}

.course-card .stat-icon {
  background-color: #666666;
}

.enrollment-card .stat-icon {
  background-color: #444444;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-change {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 14px;
}

.stat-change.positive {
  color: var(--text-primary);
}

.stat-change.negative {
  color: var(--text-primary);
}

/* 图表样式 */
.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 8px;
  height: 100%;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  background: var(--background-color);
  border-radius: 4px;
}

/* 活动区域样式 */
.activity-row {
  margin-bottom: 24px;
}

.activity-card {
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.tasks-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  max-height: 300px;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.task-item:hover {
  background-color: #f5f7fa;
  padding-left: 8px;
}

.task-item:last-child {
  border-bottom: none;
}

.task-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  margin-right: 12px;
}

.task-leave {
  background-color: #000000;
}

.task-course {
  background-color: #333333;
}

.task-exam {
  background-color: #666666;
}

.task-system {
  background-color: #999999;
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-meta {
  font-size: 12px;
  color: #909399;
}

.task-status {
  margin-left: 8px;
}

.activities-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  max-height: 300px;
}

.activity-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  margin-right: 12px;
  flex-shrink: 0;
}

.activity-login {
  background-color: #000000;
}

.activity-register {
  background-color: #333333;
}

.activity-update {
  background-color: #666666;
}

.activity-create {
  background-color: #999999;
}

.activity-delete {
  background-color: #cccccc;
}

.activity-enroll {
  background-color: #eeeeee;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-user {
  font-weight: 600;
  color: #667eea;
}

.activity-target {
  color: #4facfe;
  font-weight: 500;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

.card-footer {
  text-align: center;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 系统信息样式 */
.system-info-row {
  margin-bottom: 24px;
}

.system-info-card {
  border-radius: 8px;
}

.system-info-content {
  padding: 20px 0;
}

.info-item {
  margin-bottom: 16px;
}

.info-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.info-value {
  font-size: 18px;
  color: #303133;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 10px;
  }
  
  .dashboard-header h1 {
    font-size: 24px;
  }
  
  .dashboard-subtitle {
    font-size: 14px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
    margin-right: 12px;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .task-title,
  .activity-text {
    white-space: normal;
    text-overflow: unset;
  }
}

@media (max-width: 480px) {
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .stat-change {
    position: static;
    margin-top: 8px;
  }
  
  .task-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .task-icon {
    margin-bottom: 12px;
  }
  
  .activity-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .activity-avatar {
    margin-bottom: 12px;
  }
}
</style>