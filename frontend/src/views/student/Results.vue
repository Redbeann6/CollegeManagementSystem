<template>
  <div class="student-results">
    <el-card class="results-header">
      <h1>成绩查询</h1>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索课程名称或考试名称" 
          prefix-icon="el-icon-search"
          style="width: 300px; margin-right: 12px;"
        />
        <el-select 
          v-model="semesterFilter" 
          placeholder="选择学期"
          clearable
          style="width: 150px; margin-right: 12px;"
        >
          <el-option label="全部" value="" />
          <el-option label="2023-2024 第一学期" value="2023-2024-1" />
          <el-option label="2023-2024 第二学期" value="2023-2024-2" />
          <el-option label="2022-2023 第一学期" value="2022-2023-1" />
          <el-option label="2022-2023 第二学期" value="2022-2023-2" />
        </el-select>
        <el-select 
          v-model="courseFilter" 
          placeholder="筛选课程类型"
          clearable
          style="width: 150px;"
        >
          <el-option label="全部" value="" />
          <el-option label="必修课" value="必修" />
          <el-option label="选修课" value="选修" />
          <el-option label="实验课" value="实验" />
        </el-select>
      </div>
    </el-card>
    
    <el-card class="results-content">
      <div class="results-stats">
        <div class="stat-item">
          <div class="stat-number">{{ totalCourses }}</div>
          <div class="stat-label">已修课程</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ passedCourses }}</div>
          <div class="stat-label">及格课程</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ failedCourses }}</div>
          <div class="stat-label">不及格课程</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ avgScore.toFixed(1) }}</div>
          <div class="stat-label">平均成绩</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ gpa.toFixed(2) }}</div>
          <div class="stat-label">平均学分绩点</div>
        </div>
      </div>
      
      <div class="results-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="成绩列表" name="list">
            <div class="results-list">
              <el-table :data="filteredResults" style="width: 100%">
                <el-table-column prop="semester" label="学期" width="150" />
                <el-table-column prop="courseName" label="课程名称" width="200" />
                <el-table-column prop="courseType" label="课程类型" width="100">
                  <template #default="scope">
                    <el-tag :type="getCourseTypeTag(scope.row.courseType)">
                      {{ scope.row.courseType }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="credits" label="学分" width="80" />
                <el-table-column prop="examName" label="考试名称" width="150" />
                <el-table-column prop="score" label="分数" width="80">
                  <template #default="scope">
                    <span :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="grade" label="等级" width="80">
                  <template #default="scope">
                    <el-tag :type="getGradeType(scope.row.grade)">
                      {{ scope.row.grade }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="point" label="绩点" width="80">
                  <template #default="scope">
                    <span :class="getPointClass(scope.row.point)">{{ scope.row.point }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="examDate" label="考试日期" width="150">
                  <template #default="scope">
                    {{ formatDate(scope.row.examDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="teacherName" label="授课教师" width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click="viewResultDetails(scope.row.id)">
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="成绩统计" name="stats">
            <div class="stats-container">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card class="chart-card">
                    <template #header>
                      <div class="card-header">
                        <span>成绩分布</span>
                      </div>
                    </template>
                    <div class="chart-content">
                      <div class="score-distribution">
                        <div 
                          v-for="(count, range) in scoreDistribution" 
                          :key="range"
                          class="distribution-bar"
                        >
                          <div class="bar" :style="{ height: getBarHeight(count) + '%' }"></div>
                          <div class="range-text">{{ range }}</div>
                          <div class="count-text">{{ count }}门</div>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="chart-card">
                    <template #header>
                      <div class="card-header">
                        <span>等级分布</span>
                      </div>
                    </template>
                    <div class="chart-content">
                      <div class="grade-distribution">
                        <div 
                          v-for="(count, grade) in gradeDistribution" 
                          :key="grade"
                          class="grade-pie"
                          :class="`grade-${grade}`"
                        >
                          <div class="pie-inner">
                            <div class="grade-text">{{ grade }}</div>
                            <div class="count-text">{{ count }}门</div>
                          </div>
                          <div class="percentage-text">{{ getPercentage(count) }}%</div>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
              
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card class="chart-card">
                    <template #header>
                      <div class="card-header">
                        <span>学期平均成绩趋势</span>
                      </div>
                    </template>
                    <div class="chart-content">
                      <div class="trend-chart">
                        <div class="trend-line">
                          <div 
                            v-for="(semester, index) in semesterTrend" 
                            :key="semester.semester"
                            class="trend-point"
                            :style="{ left: getPointPosition(index), bottom: semester.avgScore + '%' }"
                          >
                            <div class="point-value">{{ semester.avgScore }}</div>
                          </div>
                          <svg class="trend-curve" viewBox="0 0 100 100">
                            <path 
                              :d="getTrendPath()" 
                              fill="none" 
                              stroke="#26a69a" 
                              stroke-width="2"
                            />
                          </svg>
                        </div>
                        <div class="semester-labels">
                          <div 
                            v-for="(semester, index) in semesterTrend" 
                            :key="semester.semester"
                            class="semester-label"
                            :style="{ left: getPointPosition(index) }"
                          >
                            {{ semester.semesterShort }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="chart-card">
                    <template #header>
                      <div class="card-header">
                        <span>课程类型成绩对比</span>
                      </div>
                    </template>
                    <div class="chart-content">
                      <div class="type-comparison">
                        <div 
                          v-for="(info, type) in typeComparison" 
                          :key="type"
                          class="type-bar"
                        >
                          <div class="bar-label">{{ type }}</div>
                          <div class="bar-container">
                            <div 
                              class="bar" 
                              :style="{ width: getTypeBarWidth(info.avgScore) + '%' }"
                            >
                              <span class="bar-value">{{ info.avgScore.toFixed(1) }}</span>
                            </div>
                          </div>
                          <div class="course-count">{{ info.count }}门</div>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="成绩单" name="report">
            <div class="report-container">
              <div class="report-header">
                <h2>学生成绩单</h2>
                <div class="report-info">
                  <div class="info-item">
                    <span class="info-label">学生姓名：</span>
                    <span class="info-value">{{ studentInfo?.name || '学生' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">学号：</span>
                    <span class="info-value">{{ studentInfo?.studentId || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">班级：</span>
                    <span class="info-value">{{ studentInfo?.className || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">查询学期：</span>
                    <span class="info-value">{{ selectedSemester || '全部学期' }}</span>
                  </div>
                </div>
              </div>
              
              <div class="report-content">
                <table class="report-table">
                  <thead>
                    <tr>
                      <th>序号</th>
                      <th>课程名称</th>
                      <th>课程类型</th>
                      <th>学分</th>
                      <th>考试名称</th>
                      <th>考试日期</th>
                      <th>成绩</th>
                      <th>等级</th>
                      <th>绩点</th>
                      <th>授课教师</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(result, index) in filteredResults" :key="result.id">
                      <td>{{ index + 1 }}</td>
                      <td>{{ result.courseName }}</td>
                      <td>{{ result.courseType }}</td>
                      <td>{{ result.credits }}</td>
                      <td>{{ result.examName }}</td>
                      <td>{{ formatDate(result.examDate) }}</td>
                      <td :class="getScoreClass(result.score)">{{ result.score }}</td>
                      <td>{{ result.grade }}</td>
                      <td>{{ result.point }}</td>
                      <td>{{ result.teacherName }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <div class="report-footer">
                <div class="footer-stats">
                  <div class="stat-item">
                    <span class="stat-label">总学分：</span>
                    <span class="stat-value">{{ totalCredits }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">获得学分：</span>
                    <span class="stat-value">{{ earnedCredits }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">平均成绩：</span>
                    <span class="stat-value">{{ avgScore.toFixed(1) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">平均学分绩点：</span>
                    <span class="stat-value">{{ gpa.toFixed(2) }}</span>
                  </div>
                </div>
                
                <div class="footer-actions">
                  <el-button type="primary" @click="exportReport">导出成绩单</el-button>
                  <el-button @click="printReport">打印成绩单</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div v-if="filteredResults.length === 0 && activeTab !== 'stats' && activeTab !== 'report'" class="empty-state">
        <el-empty description="暂无成绩数据" />
      </div>
    </el-card>
    
    <!-- 成绩详情对话框 -->
    <el-dialog 
      v-model="resultDetailVisible" 
      :title="selectedResult?.courseName || '成绩详情'" 
      width="60%"
    >
      <div v-if="selectedResult" class="result-detail-content">
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-descriptions border :column="{xs: 1, sm: 2, md: 3}">
            <el-descriptions-item label="课程名称">{{ selectedResult.courseName }}</el-descriptions-item>
            <el-descriptions-item label="考试名称">{{ selectedResult.examName }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ selectedResult.semester }}</el-descriptions-item>
            <el-descriptions-item label="课程类型">{{ selectedResult.courseType }}</el-descriptions-item>
            <el-descriptions-item label="学分">{{ selectedResult.credits }}</el-descriptions-item>
            <el-descriptions-item label="考试日期">{{ formatDate(selectedResult.examDate) }}</el-descriptions-item>
            <el-descriptions-item label="授课教师">{{ selectedResult.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="考试地点">{{ selectedResult.location }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h3>成绩详情</h3>
          <div class="score-breakdown">
            <div class="score-main">
              <div class="score-value">{{ selectedResult.score }}</div>
              <div class="score-grade">{{ selectedResult.grade }}</div>
              <div class="score-point">绩点：{{ selectedResult.point }}</div>
            </div>
            
            <div class="score-components">
              <h4>成绩构成</h4>
              <el-descriptions border :column="{xs: 1, sm: 2}">
                <el-descriptions-item label="平时成绩">{{ selectedResult.attendanceScore }}分 ({{ selectedResult.attendanceWeight }}%)</el-descriptions-item>
                <el-descriptions-item label="期中成绩">{{ selectedResult.midtermScore }}分 ({{ selectedResult.midtermWeight }}%)</el-descriptions-item>
                <el-descriptions-item label="期末成绩">{{ selectedResult.finalScore }}分 ({{ selectedResult.finalWeight }}%)</el-descriptions-item>
                <el-descriptions-item label="实验成绩">{{ selectedResult.experimentScore }}分 ({{ selectedResult.experimentWeight }}%)</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedResult.comment">
          <h3>教师评语</h3>
          <div class="teacher-comment">
            {{ selectedResult.comment }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StudentResults',
  data() {
    return {
      searchQuery: '',
      semesterFilter: '',
      courseFilter: '',
      activeTab: 'list',
      results: [],
      selectedResult: null,
      resultDetailVisible: false,
      studentInfo: null,
      selectedSemester: '全部学期'
    }
  },
  computed: {
    // 根据搜索和筛选条件过滤成绩
    filteredResults() {
      return this.results.filter(result => {
        const matchesSearch = 
          !this.searchQuery || 
          result.courseName.toLowerCase().includes(this.searchQuery.toLowerCase()) || 
          result.examName.toLowerCase().includes(this.searchQuery.toLowerCase())
        
        const matchesSemester = 
          !this.semesterFilter || 
          result.semester === this.semesterFilter
        
        const matchesCourseType = 
          !this.courseFilter || 
          result.courseType === this.courseFilter
        
        return matchesSearch && matchesSemester && matchesCourseType
      }).sort((a, b) => new Date(b.examDate) - new Date(a.examDate))
    },
    
    // 统计信息
    totalCourses() {
      return this.filteredResults.length
    },
    
    passedCourses() {
      return this.filteredResults.filter(r => r.score >= 60).length
    },
    
    failedCourses() {
      return this.filteredResults.filter(r => r.score < 60).length
    },
    
    avgScore() {
      if (this.totalCourses === 0) return 0
      const sum = this.filteredResults.reduce((acc, curr) => acc + curr.score, 0)
      return sum / this.totalCourses
    },
    
    totalCredits() {
      return this.filteredResults.reduce((acc, curr) => acc + curr.credits, 0)
    },
    
    earnedCredits() {
      return this.filteredResults.filter(r => r.score >= 60).reduce((acc, curr) => acc + curr.credits, 0)
    },
    
    gpa() {
      if (this.totalCourses === 0) return 0
      const sum = this.filteredResults.reduce((acc, curr) => acc + (curr.point * curr.credits), 0)
      const totalCreditPoints = this.filteredResults.reduce((acc, curr) => acc + curr.credits, 0)
      return totalCreditPoints > 0 ? sum / totalCreditPoints : 0
    },
    
    // 成绩分布统计
    scoreDistribution() {
      const distribution = {
        '90-100': 0,
        '80-89': 0,
        '70-79': 0,
        '60-69': 0,
        '0-59': 0
      }
      
      this.filteredResults.forEach(result => {
        if (result.score >= 90) distribution['90-100']++
        else if (result.score >= 80) distribution['80-89']++
        else if (result.score >= 70) distribution['70-79']++
        else if (result.score >= 60) distribution['60-69']++
        else distribution['0-59']++
      })
      
      return distribution
    },
    
    // 等级分布统计
    gradeDistribution() {
      const distribution = {
        '优秀': 0,
        '良好': 0,
        '中等': 0,
        '及格': 0,
        '不及格': 0
      }
      
      this.filteredResults.forEach(result => {
        if (distribution.hasOwnProperty(result.grade)) {
          distribution[result.grade]++
        }
      })
      
      return distribution
    },
    
    // 学期平均成绩趋势
    semesterTrend() {
      const trend = []
      const semesters = {} // 按学期分组
      
      // 按学期分组成绩
      this.results.forEach(result => {
        if (!semesters[result.semester]) {
          semesters[result.semester] = []
        }
        semesters[result.semester].push(result.score)
      })
      
      // 计算每个学期的平均成绩
      Object.keys(semesters).sort().forEach(semester => {
        const scores = semesters[semester]
        const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length
        trend.push({
          semester,
          semesterShort: semester.replace('20', '').replace('第一学期', '春').replace('第二学期', '秋'),
          avgScore: Math.min(100, (avgScore / 100) * 80) // 归一化到0-100范围，以便图表显示
        })
      })
      
      return trend
    },
    
    // 课程类型成绩对比
    typeComparison() {
      const comparison = {}
      
      // 按课程类型分组成绩
      this.filteredResults.forEach(result => {
        if (!comparison[result.courseType]) {
          comparison[result.courseType] = {
            scores: [],
            count: 0
          }
        }
        comparison[result.courseType].scores.push(result.score)
        comparison[result.courseType].count++
      })
      
      // 计算每种类型的平均成绩
      Object.keys(comparison).forEach(type => {
        const scores = comparison[type].scores
        const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length
        comparison[type].avgScore = avgScore
      })
      
      return comparison
    }
  },
  mounted() {
    this.loadResults()
    this.loadStudentInfo()
  },
  methods: {
    // 加载成绩数据
    async loadResults() {
      try {
        const response = await this.$axios.get('/api/student/results', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          this.results = response.data.data.map(result => ({
            id: result.id,
            courseName: result.course.name,
            courseType: result.course.courseType,
            credits: result.course.credits,
            examName: result.exam.title,
            semester: result.exam.semester,
            examDate: result.exam.examTime,
            score: result.score,
            grade: result.grade,
            point: this.calculateGradePoint(result.score),
            teacherName: result.course.teacher.name,
            location: result.exam.location,
            attendanceScore: result.attendanceScore,
            attendanceWeight: result.attendanceWeight || 0,
            midtermScore: result.midtermScore,
            midtermWeight: result.midtermWeight || 0,
            finalScore: result.finalScore,
            finalWeight: result.finalWeight || 0,
            experimentScore: result.experimentScore,
            experimentWeight: result.experimentWeight || 0,
            comment: result.feedback
          }));
        } else {
          console.error('加载成绩失败:', response.data.message);
          this.$message.error(response.data.message || '加载成绩数据失败');
          this.results = [];
        }
      } catch (error) {
        console.error('加载成绩数据失败:', error);
        this.$message.error('加载成绩数据失败，请稍后重试');
        this.results = [];
      }
    },
    
    // 加载学生信息
    async loadStudentInfo() {
      try {
        const response = await this.$axios.get('/api/auth/current-user', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          const user = response.data.data;
          this.studentInfo = {
            name: user.name,
            studentId: user.studentId,
            className: user.className
          };
        } else {
          console.error('加载学生信息失败:', response.data.message);
          this.$message.error(response.data.message || '加载学生信息失败');
        }
      } catch (error) {
        console.error('加载学生信息失败:', error);
        this.$message.error('加载学生信息失败，请稍后重试');
      }
    },
    
    // 查看成绩详情
    viewResultDetails(resultId) {
      const result = this.results.find(r => r.id === resultId)
      if (result) {
        this.selectedResult = { ...result }
        this.resultDetailVisible = true
      }
    },
    
    // 导出成绩单
    exportReport() {
      this.$message.success('成绩单导出功能开发中')
    },
    
    // 打印成绩单
    printReport() {
      window.print()
    },
    
    // 获取课程类型对应的标签类型
    getCourseTypeTag(type) {
      const typeMap = {
        '必修': 'primary',
        '选修': 'success',
        '实验': 'info'
      }
      return typeMap[type] || 'info'
    },
    
    // 获取等级对应的标签类型
    getGradeType(grade) {
      const typeMap = {
        '优秀': 'success',
        '良好': 'warning',
        '中等': 'info',
        '及格': 'primary',
        '不及格': 'danger'
      }
      return typeMap[grade] || 'info'
    },
    
    // 获取分数样式类
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-average'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },
    
    // 获取绩点样式类
    getPointClass(point) {
      if (point >= 4.0) return 'point-excellent'
      if (point >= 3.0) return 'point-good'
      if (point >= 2.0) return 'point-average'
      if (point >= 1.0) return 'point-pass'
      return 'point-fail'
    },
    
    // 计算绩点
    calculateGradePoint(score) {
      if (score >= 95) return 4.0;
      if (score >= 90) return 3.7;
      if (score >= 85) return 3.3;
      if (score >= 80) return 3.0;
      if (score >= 75) return 2.7;
      if (score >= 70) return 2.3;
      if (score >= 65) return 2.0;
      if (score >= 60) return 1.7;
      return 0.0;
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    
    // 统计图表相关方法
    getBarHeight(count) {
      const maxCount = Math.max(...Object.values(this.scoreDistribution))
      return maxCount > 0 ? (count / maxCount) * 100 : 0
    },
    
    getPercentage(count) {
      const total = this.totalCourses
      return total > 0 ? Math.round((count / total) * 100) : 0
    },
    
    getPointPosition(index) {
      const total = this.semesterTrend.length - 1
      return total > 0 ? (index / total) * 100 : 50
    },
    
    getTrendPath() {
      if (this.semesterTrend.length < 2) return ''
      
      let path = ''
      this.semesterTrend.forEach((point, index) => {
        const x = this.getPointPosition(index)
        const y = 100 - point.avgScore // 反转y轴，因为SVG的y轴向下
        
        if (index === 0) {
          path = `M ${x} ${y}`
        } else {
          // 使用平滑曲线连接点
          const prevX = this.getPointPosition(index - 1)
          const prevY = 100 - this.semesterTrend[index - 1].avgScore
          const cpX1 = prevX + (x - prevX) / 3
          const cpY1 = prevY
          const cpX2 = x - (x - prevX) / 3
          const cpY2 = y
          path += ` C ${cpX1} ${cpY1}, ${cpX2} ${cpY2}, ${x} ${y}`
        }
      })
      
      return path
    },
    
    getTypeBarWidth(avgScore) {
      return Math.min(100, (avgScore / 100) * 100)
    },
    
    // 模拟成绩数据
    getMockResults() {
      return [
        {
          id: 1,
          courseName: '高等数学',
          courseType: '必修',
          credits: 5,
          examName: '期中考试',
          semester: '2023-2024 第一学期',
          examDate: '2024-01-10',
          score: 85,
          grade: '良好',
          point: 3.5,
          teacherName: '张教授',
          location: 'A栋101',
          attendanceScore: 90,
          attendanceWeight: 20,
          midtermScore: 82,
          midtermWeight: 30,
          finalScore: 86,
          finalWeight: 50,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生学习态度认真，作业完成质量较高，建议继续保持。'
        },
        {
          id: 2,
          courseName: '大学英语',
          courseType: '必修',
          credits: 4,
          examName: '期中考试',
          semester: '2023-2024 第一学期',
          examDate: '2024-01-12',
          score: 78,
          grade: '中等',
          point: 2.8,
          teacherName: '李老师',
          location: 'B栋201',
          attendanceScore: 85,
          attendanceWeight: 20,
          midtermScore: 75,
          midtermWeight: 30,
          finalScore: 79,
          finalWeight: 50,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生基础知识掌握较好，但口语表达能力有待提高。'
        },
        {
          id: 3,
          courseName: '程序设计基础',
          courseType: '必修',
          credits: 5,
          examName: '期中考试',
          semester: '2023-2024 第一学期',
          examDate: '2024-01-08',
          score: 92,
          grade: '优秀',
          point: 4.2,
          teacherName: '王老师',
          location: 'C栋301',
          attendanceScore: 95,
          attendanceWeight: 20,
          midtermScore: 90,
          midtermWeight: 30,
          finalScore: 93,
          finalWeight: 50,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生编程能力突出，逻辑思维清晰，建议继续深入学习。'
        },
        {
          id: 4,
          courseName: '物理实验',
          courseType: '实验',
          credits: 2,
          examName: '实验考核',
          semester: '2023-2024 第一学期',
          examDate: '2024-01-05',
          score: 88,
          grade: '良好',
          point: 3.8,
          teacherName: '刘教授',
          location: '实验楼101',
          attendanceScore: 90,
          attendanceWeight: 30,
          midtermScore: null,
          midtermWeight: 0,
          finalScore: null,
          finalWeight: 0,
          experimentScore: 88,
          experimentWeight: 70,
          comment: '该生实验操作规范，报告撰写认真，观察能力较强。'
        },
        {
          id: 5,
          courseName: '线性代数',
          courseType: '必修',
          credits: 4,
          examName: '期末考试',
          semester: '2022-2023 第二学期',
          examDate: '2023-07-15',
          score: 82,
          grade: '良好',
          point: 3.2,
          teacherName: '张教授',
          location: 'A栋201',
          attendanceScore: 85,
          attendanceWeight: 20,
          midtermScore: 78,
          midtermWeight: 30,
          finalScore: 84,
          finalWeight: 50,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生概念理解透彻，但解题速度需要提高。'
        },
        {
          id: 6,
          courseName: '计算机导论',
          courseType: '必修',
          credits: 3,
          examName: '期末考试',
          semester: '2022-2023 第二学期',
          examDate: '2023-07-10',
          score: 89,
          grade: '良好',
          point: 3.9,
          teacherName: '赵老师',
          location: 'C栋101',
          attendanceScore: 90,
          attendanceWeight: 20,
          midtermScore: 85,
          midtermWeight: 30,
          finalScore: 92,
          finalWeight: 50,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生学习热情高，课堂参与度好，基础扎实。'
        },
        {
          id: 7,
          courseName: '大学物理',
          courseType: '必修',
          credits: 4,
          examName: '期末考试',
          semester: '2022-2023 第二学期',
          examDate: '2023-07-12',
          score: 76,
          grade: '中等',
          point: 2.6,
          teacherName: '孙老师',
          location: 'B栋101',
          attendanceScore: 85,
          attendanceWeight: 20,
          midtermScore: 72,
          midtermWeight: 30,
          finalScore: 78,
          finalWeight: 50,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生需要加强对物理概念的理解，多做习题巩固。'
        },
        {
          id: 8,
          courseName: '中国近现代史',
          courseType: '必修',
          credits: 2,
          examName: '期末考试',
          semester: '2022-2023 第一学期',
          examDate: '2023-01-15',
          score: 95,
          grade: '优秀',
          point: 4.5,
          teacherName: '周老师',
          location: 'D栋301',
          attendanceScore: 95,
          attendanceWeight: 30,
          midtermScore: 92,
          midtermWeight: 30,
          finalScore: 98,
          finalWeight: 40,
          experimentScore: null,
          experimentWeight: 0,
          comment: '该生历史知识丰富，分析能力强，论文质量高。'
        }
      ]
    }
  }
}
</script>

<style scoped>
.student-results {
  padding: 20px;
}

.results-header {
  margin-bottom: 24px;
  border-radius: 8px;
}

.results-header h1 {
  margin-bottom: 16px;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.results-content {
  border-radius: 8px;
}

.results-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #26a69a;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
}

.results-list {
  padding: 16px 0;
}

.empty-state {
  padding: 60px 0;
}

/* 统计图表样式 */
.stats-container {
  padding: 16px 0;
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-content {
  padding: 16px 0;
}

/* 成绩分布 */
.score-distribution {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
}

.distribution-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 80px;
}

.distribution-bar .bar {
  width: 40px;
  background-color: #26a69a;
  border-radius: 4px 4px 0 0;
  margin-bottom: 8px;
  transition: height 0.3s ease;
}

.range-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.count-text {
  font-size: 12px;
  color: #909399;
}

/* 等级分布 */
.grade-distribution {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16px;
}

.grade-pie {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.grade-优秀 {
  background-color: #67c23a;
}

.grade-良好 {
  background-color: #e6a23c;
}

.grade-中等 {
  background-color: #409eff;
}

.grade-及格 {
  background-color: #909399;
}

.grade-不及格 {
  background-color: #f56c6c;
}

.pie-inner {
  text-align: center;
}

.grade-text {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}

.percentage-text {
  position: absolute;
  top: 100%;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

/* 趋势图 */
.trend-chart {
  height: 200px;
  position: relative;
}

.trend-line {
  position: relative;
  height: 160px;
  border-bottom: 1px solid #e4e7ed;
  border-left: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.trend-point {
  position: absolute;
  width: 12px;
  height: 12px;
  background-color: #26a69a;
  border-radius: 50%;
  transform: translate(-50%, 50%);
}

.point-value {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #606266;
}

.trend-curve {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.semester-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

.semester-label {
  font-size: 12px;
  color: #606266;
  transform: translateX(-50%);
}

/* 课程类型对比 */
.type-comparison {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.type-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-label {
  width: 60px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}

.bar-container {
  flex: 1;
  height: 30px;
  background-color: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.type-bar .bar {
  height: 100%;
  background-color: #26a69a;
  transition: width 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
}

.course-count {
  width: 60px;
  text-align: left;
  font-size: 14px;
  color: #909399;
}

/* 成绩单样式 */
.report-container {
  padding: 20px;
}

.report-header {
  text-align: center;
  margin-bottom: 30px;
}

.report-header h2 {
  color: #303133;
  margin-bottom: 20px;
}

.report-info {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  color: #606266;
}

.info-value {
  color: #303133;
}

.report-content {
  margin-bottom: 30px;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
}

.report-table th {
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  padding: 12px;
  text-align: center;
  font-weight: 500;
  color: #303133;
}

.report-table td {
  border: 1px solid #ebeef5;
  padding: 12px;
  text-align: center;
  color: #606266;
}

.report-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.footer-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.footer-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-stats .stat-label {
  color: #606266;
}

.footer-stats .stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #26a69a;
}

/* 成绩详情样式 */
.result-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 18px;
}

.score-breakdown {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.score-main {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #26a69a;
}

.score-grade {
  font-size: 24px;
  color: #606266;
}

.score-point {
  font-size: 18px;
  color: #909399;
}

.teacher-comment {
  padding: 16px;
  background-color: #f0f2f5;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
}

/* 分数和绩点样式类 */
.score-excellent,
.point-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good,
.point-good {
  color: #e6a23c;
  font-weight: bold;
}

.score-average,
.point-average {
  color: #409eff;
  font-weight: bold;
}

.score-pass,
.point-pass {
  color: #606266;
  font-weight: bold;
}

.score-fail,
.point-fail {
  color: #f56c6c;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-results {
    padding: 10px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-input,
  .el-select {
    width: 100% !important;
  }
  
  .results-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .stat-item {
    flex: 1 1 calc(50% - 16px);
  }
  
  .score-distribution {
    flex-wrap: wrap;
    height: auto;
    gap: 8px;
  }
  
  .distribution-bar {
    flex: 1 1 calc(33.333% - 8px);
    max-width: none;
    height: 150px;
  }
  
  .grade-distribution {
    gap: 8px;
  }
  
  .grade-pie {
    width: 80px;
    height: 80px;
  }
  
  .report-info {
    flex-direction: column;
    align-items: center;
  }
  
  .info-item {
    width: 100%;
    justify-content: center;
  }
  
  .report-table {
    font-size: 12px;
  }
  
  .report-table th,
  .report-table td {
    padding: 8px 4px;
  }
  
  .report-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .footer-stats {
    justify-content: center;
  }
  
  .footer-actions {
    text-align: center;
  }
  
  .score-main {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>