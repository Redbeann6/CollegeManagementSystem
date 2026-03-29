<template>
  <div class="student-exams">
    <el-card class="exams-header">
      <h1>考试安排</h1>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索课程名称或考试名称" 
          prefix-icon="el-icon-search"
          style="width: 300px; margin-right: 12px;"
        />
        <el-select 
          v-model="examFilter" 
          placeholder="筛选考试状态"
          clearable
          style="width: 150px; margin-right: 12px;"
        >
          <el-option label="全部" value="" />
          <el-option label="未开始" value="not_started" />
          <el-option label="进行中" value="ongoing" />
          <el-option label="已结束" value="completed" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px;"
        />
      </div>
    </el-card>
    
    <el-card class="exams-content">
      <div class="exams-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="近期考试" name="upcoming">
            <div class="exam-list">
              <el-table :data="upcomingExams" style="width: 100%" class="table-black-white" @row-click="handleRowClick">
                <el-table-column prop="courseName" label="课程名称" />
                <el-table-column prop="examName" label="考试名称" />
                <el-table-column prop="examDate" label="考试日期">
                  <template #default="scope">
                    {{ formatDate(scope.row.examDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="examTime" label="考试时间" />
                <el-table-column prop="location" label="考试地点" />
                <el-table-column prop="duration" label="考试时长">
                  <template #default="scope">
                    {{ scope.row.duration }}分钟
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ scope.row.statusText }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="历史考试" name="history">
            <div class="exam-list">
              <el-table :data="historyExams" style="width: 100%" class="table-black-white" @row-click="handleRowClick">
                <el-table-column prop="courseName" label="课程名称" width="140" />
                <el-table-column prop="examName" label="考试名称" width="140" />
                <el-table-column prop="examDate" label="考试日期" width="120">
                  <template #default="scope">
                    {{ formatDate(scope.row.examDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="examTime" label="考试时间" width="120" />
                <el-table-column prop="location" label="考试地点" width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ scope.row.statusText }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="score" label="成绩" width="100">
                  <template #default="scope">
                    <span v-if="scope.row.score" :class="getScoreClass(scope.row.score)">
                      {{ scope.row.score }}
                    </span>
                    <span v-else class="no-score">-</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="我的日历" name="calendar">
            <div class="calendar-container">
              <div class="calendar-stats">
                <div class="stat-item">
                  <div class="stat-number">{{ examsThisWeek }}</div>
                  <div class="stat-label">本周考试</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ examsThisMonth }}</div>
                  <div class="stat-label">本月考试</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ examsNextMonth }}</div>
                  <div class="stat-label">下月考试</div>
                </div>
              </div>
              
              <!-- 简化的日历视图 -->
              <div class="simple-calendar">
                <div class="calendar-header">
                  <el-button type="text" @click="prevMonth">
                    <el-icon><ArrowLeft /></el-icon>
                  </el-button>
                  <h3>{{ currentMonthText }}</h3>
                  <el-button type="text" @click="nextMonth">
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
                
                <div class="calendar-body">
                  <!-- 星期标题 -->
                  <div class="calendar-weekdays">
                    <div v-for="day in weekDays" :key="day" class="weekday">{{ day }}</div>
                  </div>
                  
                  <!-- 日历格子 -->
                  <div class="calendar-days">
                    <!-- 上个月的最后几天 -->
                    <div v-for="(day, index) in prevMonthDays" :key="`prev-${index}`" class="calendar-day other-month">
                      {{ day }}
                    </div>
                    
                    <!-- 当月的天数 -->
                    <div 
                      v-for="day in currentMonthDays" 
                      :key="day"
                      class="calendar-day"
                      :class="{
                        'today': isToday(day),
                        'has-exam': hasExamOnDay(day)
                      }"
                      @click="selectDate(day)"
                    >
                      {{ day }}
                      <div v-if="hasExamOnDay(day)" class="exam-indicator"></div>
                    </div>
                    
                    <!-- 下个月的前几天 -->
                    <div v-for="(day, index) in nextMonthDays" :key="`next-${index}`" class="calendar-day other-month">
                      {{ day }}
                    </div>
                  </div>
                </div>
                
                <!-- 选中日期的考试详情 -->
                <div v-if="selectedDate" class="selected-date-exams">
                  <h4>{{ formatFullDate(selectedDate) }} 的考试安排</h4>
                  <div v-if="getExamsOnDate(selectedDate).length > 0" class="date-exams-list">
                    <el-card 
                      v-for="exam in getExamsOnDate(selectedDate)" 
                      :key="exam.id"
                      class="date-exam-card"
                    >
                      <div class="date-exam-content">
                        <div class="exam-time">{{ exam.examTime }}</div>
                        <div class="exam-info">
                          <div class="exam-name">{{ exam.examName }}</div>
                          <div class="course-name">{{ exam.courseName }}</div>
                          <div class="exam-location">{{ exam.location }}</div>
                        </div>
                      </div>
                    </el-card>
                  </div>
                  <el-empty v-else description="当日无考试安排" />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div v-if="filteredExams.length === 0 && activeTab !== 'calendar'" class="empty-state">
        <el-empty description="暂无考试数据" />
      </div>
    </el-card>
    
    <!-- 考试详情对话框 -->
    <el-dialog 
      v-model="examDetailVisible" 
      :title="selectedExam?.examName || '考试详情'" 
      width="60%"
    >
      <div v-if="selectedExam" class="exam-detail-content">
        <div class="detail-section">
          <h3>考试信息</h3>
          <el-descriptions border :column="{xs: 1, sm: 2, md: 3}">
            <el-descriptions-item label="考试名称">{{ selectedExam.examName }}</el-descriptions-item>
            <el-descriptions-item label="所属课程">{{ selectedExam.courseName }}</el-descriptions-item>
            <el-descriptions-item label="考试日期">{{ formatDate(selectedExam.examDate) }}</el-descriptions-item>
            <el-descriptions-item label="考试时间">{{ selectedExam.examTime }}</el-descriptions-item>
            <el-descriptions-item label="考试地点">{{ selectedExam.location }}</el-descriptions-item>
            <el-descriptions-item label="考试时长">{{ selectedExam.duration }}分钟</el-descriptions-item>
            <el-descriptions-item label="考试类型">{{ selectedExam.type }}</el-descriptions-item>
            <el-descriptions-item label="考试状态">
              <el-tag :type="getStatusType(selectedExam.status)">
                {{ selectedExam.statusText }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总分值">{{ selectedExam.totalPoints }}分</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h3>考试说明</h3>
          <div class="exam-description">
            {{ selectedExam.description }}
          </div>
        </div>
        
        <div class="detail-section">
          <h3>考试要求</h3>
          <div class="exam-requirements">
            <ul>
              <li v-for="(requirement, index) in selectedExam.requirements" :key="index">
                {{ requirement }}
              </li>
            </ul>
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedExam.score">
          <h3>成绩信息</h3>
          <div class="score-info">
            <div class="score-main">
              <div class="score-value">{{ selectedExam.score }}</div>
              <div class="score-grade">{{ selectedExam.grade }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'StudentExams',
  components: {
    ArrowLeft,
    ArrowRight
  },
  data() {
    return {
      searchQuery: '',
      examFilter: '',
      dateRange: null,
      activeTab: 'upcoming',
      exams: [],
      selectedExam: null,
      examDetailVisible: false,
      // 日历相关数据
      currentDate: new Date(),
      weekDays: ['日', '一', '二', '三', '四', '五', '六'],
      selectedDate: null,
      // 考试统计
      examsThisWeek: 0,
      examsThisMonth: 0,
      examsNextMonth: 0
    }
  },
  computed: {
    // 根据搜索和筛选条件过滤考试
    filteredExams() {
      return this.exams.filter(exam => {
        const matchesSearch = 
          !this.searchQuery || 
          exam.courseName.toLowerCase().includes(this.searchQuery.toLowerCase()) || 
          exam.examName.toLowerCase().includes(this.searchQuery.toLowerCase())
        
        const matchesFilter = 
          !this.examFilter || 
          exam.status === this.examFilter
        
        const matchesDateRange = 
          !this.dateRange || 
          (exam.examDate >= this.formatDateString(this.dateRange[0]) && 
           exam.examDate <= this.formatDateString(this.dateRange[1]))
        
        return matchesSearch && matchesFilter && matchesDateRange
      })
    },
    
    // 近期考试（未开始和进行中的考试）
    upcomingExams() {
      return this.filteredExams.filter(exam => 
        exam.status === 'not_started' || exam.status === 'ongoing'
      ).sort((a, b) => new Date(a.examDate) - new Date(b.examDate))
    },
    
    // 历史考试（已结束的考试）
    historyExams() {
      return this.filteredExams.filter(exam => 
        exam.status === 'completed'
      ).sort((a, b) => new Date(b.examDate) - new Date(a.examDate))
    },
    
    // 当前月份文本
    currentMonthText() {
      return `${this.currentDate.getFullYear()}年${this.currentDate.getMonth() + 1}月`
    },
    
    // 当前月份的天数
    currentMonthDays() {
      const year = this.currentDate.getFullYear()
      const month = this.currentDate.getMonth()
      const daysInMonth = new Date(year, month + 1, 0).getDate()
      return Array.from({ length: daysInMonth }, (_, i) => i + 1)
    },
    
    // 上个月的最后几天（用于填充日历第一行）
    prevMonthDays() {
      const year = this.currentDate.getFullYear()
      const month = this.currentDate.getMonth()
      const firstDayOfMonth = new Date(year, month, 1).getDay() // 0-6, 0是星期日
      const prevMonthLastDay = new Date(year, month, 0).getDate()
      const prevMonthDaysCount = firstDayOfMonth
      return Array.from({ length: prevMonthDaysCount }, (_, i) => prevMonthLastDay - prevMonthDaysCount + i + 1)
    },
    
    // 下个月的前几天（用于填充日历最后一行）
    nextMonthDays() {
      const totalDays = this.prevMonthDays.length + this.currentMonthDays.length
      const nextMonthDaysCount = totalDays % 7 === 0 ? 0 : 7 - (totalDays % 7)
      return Array.from({ length: nextMonthDaysCount }, (_, i) => i + 1)
    }
  },
  mounted() {
    this.loadExams()
  },
  methods: {
    // 加载考试数据
    async loadExams() {
      try {
        // 从后端API获取真实数据
        const response = await axios.get('/api/student/exams', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.exams = response.data.data || response.data
        this.updateExamStats()
      } catch (error) {
        console.error('加载考试失败:', error)
        this.$message.error('加载考试数据失败，请稍后重试')
      }
    },
    
    // 更新考试统计信息
    updateExamStats() {
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const weekLater = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
      const monthLater = new Date(now.getFullYear(), now.getMonth() + 1, now.getDate())
      const nextMonthLater = new Date(now.getFullYear(), now.getMonth() + 2, now.getDate())
      
      this.examsThisWeek = this.exams.filter(exam => {
        const examDate = new Date(exam.examDate)
        return examDate >= today && examDate < weekLater
      }).length
      
      this.examsThisMonth = this.exams.filter(exam => {
        const examDate = new Date(exam.examDate)
        return examDate >= today && examDate < monthLater
      }).length
      
      this.examsNextMonth = this.exams.filter(exam => {
        const examDate = new Date(exam.examDate)
        return examDate >= monthLater && examDate < nextMonthLater
      }).length
    },
    
    // 处理标签页切换
    handleTabClick(tab) {
      this.activeTab = tab.paneName
      if (tab.paneName === 'calendar') {
        this.updateCalendarData()
      }
    },
    
    // 处理表格行点击事件
    handleRowClick(row) {
      this.viewExamDetails(row.id);
    },
    
    // 查看考试详情
    viewExamDetails(examId) {
      const exam = this.exams.find(e => e.id === examId)
      if (exam) {
        this.selectedExam = { ...exam }
        this.examDetailVisible = true
      }
    },
    
    // 查看考试成绩
    viewExamResult(examId) {
      this.$router.push(`/student/results/${examId}`)
    },
    
    // 获取状态对应的标签类型
    getStatusType(status) {
      const typeMap = {
        'not_started': 'info',
        'ongoing': 'warning',
        'completed': 'success'
      }
      return typeMap[status] || 'info'
    },
    
    // 获取分数样式类
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-average'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    
    // 格式化完整日期（带星期）
    formatFullDate(day) {
      const date = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), day)
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
      return date.toLocaleDateString('zh-CN', options)
    },
    
    // 格式化日期字符串（用于比较）
    formatDateString(date) {
      return date.toISOString().split('T')[0]
    },
    
    // 日历相关方法
    prevMonth() {
      this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1)
      this.selectedDate = null
    },
    
    nextMonth() {
      this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1)
      this.selectedDate = null
    },
    
    isToday(day) {
      const today = new Date()
      return today.getFullYear() === this.currentDate.getFullYear() &&
             today.getMonth() === this.currentDate.getMonth() &&
             today.getDate() === day
    },
    
    hasExamOnDay(day) {
      const targetDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), day).toISOString().split('T')[0]
      return this.exams.some(exam => exam.examDate === targetDate)
    },
    
    selectDate(day) {
      this.selectedDate = day
    },
    
    getExamsOnDate(day) {
      const targetDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth(), day).toISOString().split('T')[0]
      return this.exams.filter(exam => exam.examDate === targetDate)
    },
    
    updateCalendarData() {
      // 更新日历视图所需的数据
      // 这里可以添加更多的日历相关计算
    },
    
    // 模拟考试数据
    getMockExams() {
      const now = new Date()
      const today = now.toISOString().split('T')[0]
      const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000).toISOString().split('T')[0]
      const dayAfterTomorrow = new Date(now.getTime() + 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
      const lastWeek = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
      const twoWeeksAgo = new Date(now.getTime() - 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
      
      return [
        {
          id: 1,
          examName: '期中考试',
          courseName: '高等数学',
          examDate: tomorrow,
          examTime: '09:00-11:00',
          location: 'A栋101',
          duration: 120,
          type: '闭卷',
          status: 'not_started',
          statusText: '未开始',
          totalPoints: 100,
          description: '本次考试涵盖高等数学第一章至第五章的内容，重点考察极限、导数和微分的基本概念和应用。',
          requirements: [
            '请携带学生证和身份证入场',
            '严禁携带手机、计算器等电子设备',
            '考试开始30分钟后禁止入场',
            '考试结束前30分钟内不得交卷'
          ],
          score: null,
          grade: null
        },
        {
          id: 2,
          examName: '单元测验',
          courseName: '大学英语',
          examDate: dayAfterTomorrow,
          examTime: '14:00-15:00',
          location: 'B栋201',
          duration: 60,
          type: '闭卷',
          status: 'not_started',
          statusText: '未开始',
          totalPoints: 50,
          description: '本次测验主要考察前三个单元的词汇、语法和阅读理解能力。',
          requirements: [
            '请携带2B铅笔和橡皮',
            '考试期间保持安静',
            '独立完成，严禁作弊'
          ],
          score: null,
          grade: null
        },
        {
          id: 3,
          examName: '编程实践考核',
          courseName: '程序设计基础',
          examDate: today,
          examTime: '13:30-17:30',
          location: 'C栋计算机实验室',
          duration: 240,
          type: '上机',
          status: 'ongoing',
          statusText: '进行中',
          totalPoints: 100,
          description: '本次实践考核要求完成一个小型应用程序的开发，考察代码编写能力和问题解决能力。',
          requirements: [
            '请携带学生证',
            '只能使用指定的开发环境',
            '独立完成，不得抄袭'
          ],
          score: null,
          grade: null
        },
        {
          id: 4,
          examName: '期中考试',
          courseName: '物理实验',
          examDate: lastWeek,
          examTime: '09:00-12:00',
          location: '实验楼101',
          duration: 180,
          type: '实验操作',
          status: 'completed',
          statusText: '已结束',
          totalPoints: 100,
          description: '本次实验考核要求完成指定的物理实验，并提交实验报告。',
          requirements: [
            '请提前15分钟到达实验室',
            '严格按照实验流程操作',
            '注意安全，遵守实验室规定'
          ],
          score: 92,
          grade: '优秀'
        },
        {
          id: 5,
          examName: '期中考试',
          courseName: '线性代数',
          examDate: twoWeeksAgo,
          examTime: '10:00-12:00',
          location: 'A栋201',
          duration: 120,
          type: '闭卷',
          status: 'completed',
          statusText: '已结束',
          totalPoints: 100,
          description: '本次考试涵盖线性代数第一章至第四章的内容，重点考察矩阵运算、线性方程组和向量空间的基本概念。',
          requirements: [
            '请携带学生证和身份证入场',
            '可以携带计算器（非编程计算器）',
            '考试开始30分钟后禁止入场'
          ],
          score: 85,
          grade: '良好'
        }
      ]
    }
  }
}
</script>

<style scoped>
.student-exams {
  padding: 20px;
  background-color: var(--background-color);
}

.exams-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.exams-header h1 {
  margin-bottom: 16px;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.exams-content {
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.exam-list {
  padding: 16px 0;
}

.empty-state {
  padding: 60px 0;
}

/* 日历样式 */
.simple-calendar {
  background-color: var(--background-color-light);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--box-shadow-base);
  border: 1px solid var(--border-light);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.calendar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.calendar-body {
  margin-bottom: 24px;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  padding: 8px;
  font-weight: 500;
  color: var(--text-secondary);
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  position: relative;
  min-height: 60px;
  padding: 8px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition-base);
  background-color: var(--background-color-light);
}

.calendar-day:hover {
  background-color: var(--background-color);
  border-color: var(--border-dark);
  transition: var(--transition-base);
}

.calendar-day:hover {
  border-color: var(--primary-color);
  background-color: var(--background-color);
  transform: translateY(-2px);
  box-shadow: var(--box-shadow-light);
}

.calendar-day.other-month {
  color: var(--text-placeholder);
  background-color: var(--background-color-dark);
}

.calendar-day.today {
  background-color: var(--background-color);
  border-color: var(--primary-color);
  font-weight: 600;
  position: relative;
}

.calendar-day.today::after {
  content: '';
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  background-color: var(--primary-color);
  border-radius: 50%;
}

.calendar-day.has-exam {
  border-color: var(--primary-color);
  background-color: var(--background-color);
  position: relative;
  overflow: hidden;
}

.calendar-day.has-exam::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 16px 16px 0;
  border-color: transparent var(--primary-color) transparent transparent;
}

.exam-indicator {
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  background-color: var(--primary-color);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: translateX(-50%) scale(1); opacity: 1; }
  50% { transform: translateX(-50%) scale(1.2); opacity: 0.8; }
  100% { transform: translateX(-50%) scale(1); opacity: 1; }
}

/* 选中日期的考试详情样式优化 */
.selected-date-exams {
  margin-top: 24px;
  background-color: var(--background-color);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--border-light);
}

.selected-date-exams h4 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-date-exams h4::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 18px;
  background-color: var(--primary-color);
  border-radius: 1.5px;
}

.date-exam-card {
  margin-bottom: 16px;
  transition: var(--transition-base);
  border-radius: 8px !important;
  border: 1px solid var(--border-light) !important;
  overflow: hidden;
  background-color: var(--background-color-light);
}

.date-exam-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--box-shadow-medium);
  border-color: var(--primary-color) !important;
}

.date-exam-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.exam-time {
  min-width: 120px;
  font-weight: 600;
  color: var(--primary-color);
  font-size: 16px;
  padding: 6px 12px;
  background-color: var(--background-color);
  border-radius: 6px;
  text-align: center;
}

.exam-info {
  flex: 1;
}

.exam-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  font-size: 16px;
}

.course-name {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.exam-location {
  color: var(--text-placeholder);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 考试详情样式优化 */
.exam-detail-content {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.detail-section h3 {
  margin-bottom: 18px;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-section h3::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 20px;
  background-color: var(--primary-color);
  border-radius: 2px;
}

.exam-description,
.exam-requirements {
  line-height: 1.8;
  color: var(--text-secondary);
  font-size: 15px;
}

.exam-requirements ul {
  padding-left: 24px;
  margin: 0;
}

.exam-requirements li {
  margin-bottom: 10px;
  position: relative;
}

.exam-requirements li::marker {
  color: var(--primary-color);
  font-weight: bold;
}

/* 成绩信息样式优化 */
.score-info {
  padding: 24px;
  background-color: var(--background-color);
  border-radius: 16px;
  border: 1px solid var(--border-light);
  text-align: center;
}

.score-main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  margin-bottom: 20px;
}

.score-value {
  font-size: 56px;
  font-weight: 700;
  color: var(--primary-color);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: var(--transition-base);
}

.score-value:hover {
  transform: scale(1.05);
}

.score-grade {
  font-size: 28px;
  color: var(--text-secondary);
  font-weight: 600;
}

/* 分数等级样式优化 */
.score-excellent {
  color: var(--success-color);
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(51, 51, 51, 0.2);
}

.score-good {
  color: var(--warning-color);
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(85, 85, 85, 0.2);
}

.score-average {
  color: var(--info-color);
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(51, 51, 51, 0.2);
}

.score-pass {
  color: var(--text-secondary);
  font-weight: 700;
}

.score-fail {
  color: var(--danger-color);
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(51, 51, 51, 0.2);
}

.no-score {
  color: var(--text-placeholder);
  font-style: italic;
}

/* 成绩反馈信息 */
.score-feedback {
  margin-top: 16px;
  padding: 12px 20px;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  color: var(--primary-color);
  font-size: 14px;
}

/* 响应式设计优化 */
@media (max-width: 1200px) {
  .student-exams {
    padding: 16px;
  }
  
  .exams-tabs .el-tabs__header {
    padding: 0 16px;
  }
  
  .calendar-stats {
    gap: 20px;
  }
  
  .stat-item .stat-number {
    font-size: 32px;
  }
}

@media (max-width: 1024px) {
  .exams-header {
    padding: 16px 20px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .stat-item {
    flex: 1 1 calc(33.333% - 13.333px);
  }
}

@media (max-width: 768px) {
  .student-exams {
    padding: 12px;
  }
  
  .exams-header {
    padding: 16px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-input,
  .el-select,
  .el-date-picker {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .exams-tabs .el-tabs__item {
    font-size: 15px;
    padding: 0 16px;
  }
  
  .calendar-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .stat-item {
    flex: 1 1 calc(50% - 16px);
  }
  
  .stat-item .stat-number {
    font-size: 28px;
  }
  
  .calendar-day {
    min-height: 40px;
    padding: 4px;
    font-size: 14px;
  }
  
  .date-exam-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .exam-time {
    min-width: auto;
    width: 100%;
    text-align: center;
  }
  
  .score-main {
    flex-direction: column;
    gap: 16px;
  }
  
  .score-value {
    font-size: 48px;
  }
  
  .score-grade {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .student-exams {
    padding: 8px;
  }
  
  .exams-header {
    padding: 12px;
  }
  
  .exams-header h1 {
    font-size: 20px;
  }
  
  .stat-item {
    flex: 1 1 100%;
  }
  
  .calendar-day {
    min-height: 36px;
    font-size: 13px;
    padding: 2px;
  }
  
  .score-value {
    font-size: 40px;
  }
}

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--border-lighter);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-dark);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}
</style>