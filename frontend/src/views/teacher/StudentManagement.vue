<template>
  <div class="student-management-container">
    <div class="page-header">
      <h1>学生管理</h1>
      <p>管理和查看学生信息、成绩记录和学习状态</p>
    </div>

    <!-- 主要内容区域 -->
    <el-card class="student-management-card">
      <!-- 筛选搜索区域 -->
      <div class="search-filter-section">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-select v-model="selectedCourse" placeholder="选择课程" clearable @change="onCourseChange">
              <el-option
                v-for="course in teachingCourses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="selectedClass" placeholder="选择班级" clearable>
              <el-option
                v-for="cls in availableClasses"
                :key="cls.id"
                :label="cls.name"
                :value="cls.id"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索学生姓名或学号"
              prefix-icon="el-icon-search"
              clearable
              @keyup.enter="searchStudents"
            ></el-input>
          </el-col>
          <el-col :span="6">
            <div class="action-buttons">
              <el-button type="primary" icon="el-icon-download" @click="exportStudentList">
                导出名单
              </el-button>
              <el-button type="info" icon="el-icon-refresh" @click="refreshStudentList">
                刷新
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 学生统计概览 -->
      <div class="student-stats-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ studentStats.total }}</div>
              <div class="stat-label">总学生数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ studentStats.male }}</div>
              <div class="stat-label">男生</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ studentStats.female }}</div>
              <div class="stat-label">女生</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ studentStats.avgScore.toFixed(1) }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 学生列表表格 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="paginatedStudentData"
          style="width: 100%"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="studentId" label="学号" min-width="120" sortable></el-table-column>
          <el-table-column prop="name" label="姓名" min-width="100" sortable>
            <template #default="scope">
              <el-popover
                placement="top"
                title="学生基本信息"
                width="200"
                trigger="hover"
                content=""
              >
                <template #reference>
                  <div class="student-name-cell">
                    <span :class="['gender-indicator', scope.row.gender]"></span>
                    {{ scope.row.name }}
                  </div>
                </template>
                <div>
                  <p><strong>学号：</strong>{{ scope.row.studentId }}</p>
                  <p><strong>性别：</strong>{{ scope.row.gender === 'male' ? '男' : '女' }}</p>
                  <p><strong>班级：</strong>{{ scope.row.className }}</p>
                  <p><strong>年龄：</strong>{{ scope.row.age }}岁</p>
                </div>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column prop="className" label="班级" min-width="120"></el-table-column>
          <el-table-column prop="gender" label="性别" min-width="80">
            <template #default="scope">
              <el-tag :type="scope.row.gender === 'male' ? 'primary' : 'info'">
                {{ scope.row.gender === 'male' ? '男' : '女' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" min-width="80" sortable></el-table-column>
          <el-table-column prop="major" label="专业" min-width="120"></el-table-column>
          <el-table-column prop="avgScore" label="平均成绩" min-width="100" sortable>
            <template #default="scope">
              <div :class="['score-tag', getScoreLevelClass(scope.row.avgScore)]">
                {{ scope.row.avgScore.toFixed(1) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="courseStatus" label="课程状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.courseStatus)">
                {{ getStatusText(scope.row.courseStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="200">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewStudentDetails(scope.row)">
                <i class="el-icon-view"></i> 详情
              </el-button>
              <el-button type="success" size="small" @click="viewStudentGrades(scope.row)">
                <i class="el-icon-document"></i> 成绩
              </el-button>
              <el-button type="warning" size="small" @click="sendMessage(scope.row)">
                <i class="el-icon-message"></i> 联系
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredStudents.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 学生详情对话框 -->
    <el-dialog
      title="学生详情"
      v-model="showStudentDetailsDialog"
      width="700px"
      @close="handleStudentDetailsDialogClose"
    >
      <div class="student-details-container">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="student-avatar">
              <img :src="currentStudent.avatar" :alt="currentStudent.name" class="avatar-image">
              <div class="student-name-large">{{ currentStudent.name }}</div>
              <div class="student-basic-info">
                <el-tag :type="currentStudent.gender === 'male' ? 'primary' : 'info'" class="gender-tag">
                  {{ currentStudent.gender === 'male' ? '男' : '女' }}
                </el-tag>
                <span class="info-text">{{ currentStudent.age }}岁</span>
                <span class="info-text">{{ currentStudent.major }}</span>
              </div>
            </div>
          </el-col>
          <el-col :span="18">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="学号">{{ currentStudent.studentId }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
              <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
              <el-descriptions-item label="年级">{{ currentStudent.grade }}</el-descriptions-item>
              <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
              <el-descriptions-item label="系部">{{ currentStudent.department }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentStudent.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="电子邮箱">{{ currentStudent.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ formatDate(currentStudent.birthDate) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="入学日期">{{ formatDate(currentStudent.enrollmentDate) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ formatIdCard(currentStudent.idCard) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="家庭住址">{{ currentStudent.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="班主任">{{ currentStudent.teacher || '-' }}</el-descriptions-item>
              <el-descriptions-item label="学习状态">{{ getStatusText(currentStudent.studyStatus) }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <!-- 学习表现区域 -->
        <div class="study-performance-section">
          <h3>学习表现</h3>
          <el-row :gutter="20">
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ studentPerformance.avgScore.toFixed(1) }}</div>
                <div class="stat-label">平均成绩</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ studentPerformance.ranking }}</div>
                <div class="stat-label">班级排名</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ studentPerformance.excellentCount }}</div>
                <div class="stat-label">优秀课程</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ studentPerformance.failCount }}</div>
                <div class="stat-label">不及格课程</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ studentPerformance.attendanceRate.toFixed(1) }}%</div>
                <div class="stat-label">出勤率</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ studentPerformance.credit }}</div>
                <div class="stat-label">已修学分</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 近期成绩趋势图 -->
        <div class="grade-trend-section">
          <h3>近期成绩趋势</h3>
          <div class="chart-container">
            <canvas ref="gradeTrendChart" width="600" height="200"></canvas>
          </div>
        </div>

        <!-- 课程成绩表格 -->
        <div class="course-grades-section">
          <h3>课程成绩</h3>
          <el-table
            :data="currentStudentCourses"
            style="width: 100%"
            border
            height="300"
          >
            <el-table-column prop="courseName" label="课程名称" min-width="150"></el-table-column>
            <el-table-column prop="semester" label="学期" width="100"></el-table-column>
            <el-table-column prop="credit" label="学分" width="80"></el-table-column>
            <el-table-column prop="score" label="成绩" width="80">
              <template #default="scope">
                <div :class="['score-tag', getScoreLevelClass(scope.row.score)]">
                  {{ scope.row.score }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="grade" label="等级" width="80">
              <template #default="scope">
                <el-tag :type="getScoreLevelTagType(scope.row.score)">
                  {{ calculateGradeLevel(scope.row.score) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleStudentDetailsDialogClose">关闭</el-button>
        <el-button type="primary" @click="exportStudentProfile">导出档案</el-button>
        <el-button type="success" @click="viewStudentGrades(currentStudent)">查看所有成绩</el-button>
      </template>
    </el-dialog>

    <!-- 学生成绩对话框 -->
    <el-dialog
      title="学生成绩记录"
      v-model="showStudentGradesDialog"
      width="800px"
      @close="handleStudentGradesDialogClose"
    >
      <div class="student-grades-container">
        <div class="student-info">
          <h3>{{ viewingStudent.name }} ({{ viewingStudent.studentId }})</h3>
          <p>班级：{{ viewingStudent.className }} | 专业：{{ viewingStudent.major }}</p>
        </div>

        <!-- 筛选条件 -->
        <div class="grades-filter-section">
          <el-row :gutter="20" align="middle">
            <el-col :span="8">
              <el-select v-model="selectedGradeSemester" placeholder="选择学期" clearable>
                <el-option label="全部学期" value=""></el-option>
                <el-option label="2021-2022学年第一学期" value="2021-2022-1"></el-option>
                <el-option label="2021-2022学年第二学期" value="2021-2022-2"></el-option>
                <el-option label="2022-2023学年第一学期" value="2022-2023-1"></el-option>
                <el-option label="2022-2023学年第二学期" value="2022-2023-2"></el-option>
                <el-option label="2023-2024学年第一学期" value="2023-2024-1"></el-option>
                <el-option label="2023-2024学年第二学期" value="2023-2024-2"></el-option>
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="selectedGradeType" placeholder="成绩类型" clearable>
                <el-option label="全部成绩" value=""></el-option>
                <el-option label="必修课" value="required"></el-option>
                <el-option label="选修课" value="elective"></el-option>
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-button type="success" @click="exportStudentGrades">
                <i class="el-icon-download"></i> 导出成绩
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 成绩统计 -->
        <div class="student-grade-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value">{{ filteredStudentGrades.length }}</div>
                <div class="stat-label">课程总数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value">{{ studentGradeStats.avgScore.toFixed(1) }}</div>
                <div class="stat-label">平均分</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value">{{ studentGradeStats.totalCredit }}</div>
                <div class="stat-label">已修学分</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card">
                <div class="stat-value">{{ studentGradeStats.passRate.toFixed(1) }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 成绩表格 -->
        <el-table
          :data="filteredStudentGrades"
          style="width: 100%"
          border
          height="500"
        >
          <el-table-column prop="courseName" label="课程名称" min-width="200"></el-table-column>
          <el-table-column prop="semester" label="学期" width="150"></el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.type === 'required' ? 'primary' : 'success'">
                {{ scope.row.type === 'required' ? '必修' : '选修' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="credit" label="学分" width="80"></el-table-column>
          <el-table-column prop="examDate" label="考试日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.examDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="成绩" width="100" sortable>
            <template #default="scope">
              <div :class="['score-tag', getScoreLevelClass(scope.row.score)]">
                {{ scope.row.score }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="等级" width="80">
            <template #default="scope">
              <el-tag :type="getScoreLevelTagType(scope.row.score)">
                {{ calculateGradeLevel(scope.row.score) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 发送消息对话框 -->
    <el-dialog
      title="发送消息"
      v-model="showMessageDialog"
      width="600px"
      @close="handleMessageDialogClose"
    >
      <div class="message-container">
        <div class="recipient-info">
          <p><strong>收件人：</strong>{{ messageRecipient.name }} ({{ messageRecipient.studentId }})</p>
          <p><strong>班级：</strong>{{ messageRecipient.className }}</p>
        </div>
        <el-form :model="messageForm" :rules="messageRules" ref="messageForm" label-width="80px">
          <el-form-item label="消息类型" prop="type">
            <el-radio-group v-model="messageForm.type">
              <el-radio label="grade">成绩提醒</el-radio>
              <el-radio label="attendance">出勤提醒</el-radio>
              <el-radio label="assignment">作业提醒</el-radio>
              <el-radio label="general">一般通知</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标题" prop="title">
            <el-input v-model="messageForm.title" placeholder="请输入消息标题"></el-input>
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="messageForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入消息内容"
            ></el-input>
          </el-form-item>
          <el-form-item label="优先级">
            <el-rate v-model="messageForm.priority" :max="3" show-score score-template="{value}"></el-rate>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="handleMessageDialogClose">取消</el-button>
        <el-button type="primary" @click="sendMessageConfirm">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import Chart from 'chart.js/auto'

export default {
  name: 'TeacherStudentManagement',
  data() {
    return {
      // 筛选条件
      selectedCourse: '',
      selectedClass: '',
      searchQuery: '',
      
      // 分页参数
      currentPage: 1,
      pageSize: 10,
      
      // 数据加载状态
      loading: false,
      
      // 教学课程
      teachingCourses: [],
      
      // 可用班级
      availableClasses: [],
      
      // 学生数据
      studentData: [],
      
      // 选中的学生
      selectedRows: [],
      
      // 弹出框状态
      showStudentDetailsDialog: false,
      showStudentGradesDialog: false,
      showMessageDialog: false,
      
      // 当前学生
      currentStudent: {},
      viewingStudent: {},
      messageRecipient: {},
      
      // 学生表现
      studentPerformance: {
        avgScore: 0,
        ranking: 0,
        excellentCount: 0,
        failCount: 0,
        attendanceRate: 0,
        credit: 0
      },
      
      // 当前学生课程
      currentStudentCourses: [],
      
      // 学生成绩
      studentGrades: [],
      
      // 成绩筛选
      selectedGradeSemester: '',
      selectedGradeType: '',
      
      // 学生成绩统计
      studentGradeStats: {
        avgScore: 0,
        totalCredit: 0,
        passRate: 0
      },
      
      // 消息表单
      messageForm: {
        type: 'general',
        title: '',
        content: '',
        priority: 2
      },
      
      // 消息表单验证规则
      messageRules: {
        title: [
          { required: true, message: '请输入消息标题', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入消息内容', trigger: 'blur' }
        ]
      },
      
      // 学生统计
      studentStats: {
        total: 0,
        male: 0,
        female: 0,
        avgScore: 0
      },
      
      // 图表实例
      gradeTrendChart: null
    }
  },
  
  computed: {
    // 过滤后的学生数据
    filteredStudents() {
      let filtered = [...this.studentData]
      
      // 按班级筛选
      if (this.selectedClass) {
        filtered = filtered.filter(student => student.classId === this.selectedClass)
      }
      
      // 搜索学生
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(student => 
          student.name.toLowerCase().includes(query) || 
          student.studentId.toLowerCase().includes(query)
        )
      }
      
      // 更新统计数据
      this.updateStudentStats(filtered)
      
      return filtered
    },
    
    // 分页后的学生数据
    paginatedStudentData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredStudents.slice(start, end)
    },
    
    // 过滤后的学生成绩
    filteredStudentGrades() {
      let filtered = [...this.studentGrades]
      
      // 按学期筛选
      if (this.selectedGradeSemester) {
        filtered = filtered.filter(grade => grade.semester === this.selectedGradeSemester)
      }
      
      // 按类型筛选
      if (this.selectedGradeType) {
        filtered = filtered.filter(grade => grade.type === this.selectedGradeType)
      }
      
      // 在每次过滤后更新成绩统计
      this.$nextTick(() => {
        this.updateStudentGradeStats(filtered);
      });
      
      return filtered
    }
  },
  
  mounted() {
    // 加载教学课程
    this.loadTeachingCourses()
    // 加载学生数据
    this.loadStudentData()
  },
  
  beforeUnmount() {
    // 销毁图表实例
    if (this.gradeTrendChart) {
      this.gradeTrendChart.destroy()
    }
  },
  
  methods: {
    // 加载教学课程
    async loadTeachingCourses() {
      try {
        const response = await this.$axios.get('/teacher/data/courses', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.teachingCourses = response.data.data || []
        } else {
          console.error('获取教学课程失败:', response.data.message)
          this.$message.error(response.data.message || '获取教学课程失败')
        }
        
        console.log('教学课程加载完成')
      } catch (error) {
        console.error('加载教学课程失败:', error)
        this.$message.error('加载教学课程失败，请稍后重试')
      }
    },
    
    // 加载学生数据
    async loadStudentData() {
      this.loading = true
      try {
        const response = await this.$axios.get('/teacher/data/students', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.studentData = response.data.data || []
        } else {
          console.error('获取学生数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取学生数据失败')
        }
        
        console.log('学生数据加载完成')
      } catch (error) {
        console.error('加载学生数据失败:', error)
        this.$message.error('加载学生数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 课程变更处理
    async onCourseChange(courseId) {
      // 如果选择了课程，则按课程加载学生数据
      if (courseId) {
        await this.loadStudentsByCourse(courseId)
      } else {
        // 如果清空了课程选择，则加载所有学生
        await this.loadStudentData()
      }
      // 加载该课程的班级信息
      this.loadAvailableClasses(courseId)
      this.selectedClass = ''
    },
    
    // 根据课程ID加载学生
    async loadStudentsByCourse(courseId) {
      this.loading = true
      try {
        const response = await this.$axios.get(`/teacher/classes/${courseId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.studentData = response.data.data || []
        } else {
          console.error('获取课程学生失败:', response.data.message)
          this.$message.error(response.data.message || '获取课程学生失败')
        }
        
        console.log('课程学生数据加载完成')
      } catch (error) {
        console.error('加载课程学生失败:', error)
        this.$message.error('加载课程学生失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载可用班级
    async loadAvailableClasses(courseId) {
      try {
        const response = await this.$axios.get(`/teacher/data/classes/${courseId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.availableClasses = response.data.data || []
        } else {
          console.error('获取班级信息失败:', response.data.message)
          this.$message.error(response.data.message || '获取班级信息失败')
          this.availableClasses = []
        }
      } catch (error) {
        console.error('加载班级信息失败:', error)
        this.$message.error('加载班级信息失败，请稍后重试')
        this.availableClasses = []
      }
    },
    
    // 更新学生统计
    updateStudentStats(students) {
      const total = students.length
      const male = students.filter(s => s.gender === 'male').length
      const female = students.filter(s => s.gender === 'female').length
      const avgScore = students.reduce((sum, s) => sum + s.avgScore, 0) / (total || 1)
      
      this.studentStats = { total, male, female, avgScore }
    },
    
    // 搜索学生
    searchStudents() {
      this.currentPage = 1
    },
    
    // 刷新学生列表
    refreshStudentList() {
      this.selectedCourse = ''
      this.selectedClass = ''
      this.searchQuery = ''
      this.currentPage = 1
      this.loadStudentData()
    },
    
    // 查看学生详情
    viewStudentDetails(student) {
      this.currentStudent = student
      // 加载学生表现数据
      this.loadStudentPerformance(student.id)
      // 加载学生课程
      this.loadStudentCourses(student.id)
      // 初始化成绩趋势图
      this.$nextTick(() => {
        this.initGradeTrendChart()
      })
      // 确保在下一个tick中更新对话框状态
      this.$nextTick(() => {
        this.showStudentDetailsDialog = true
      })
    },
    
    // 加载学生表现数据
    async loadStudentPerformance(studentId) {
      try {
        const response = await this.$axios.get(`/teacher/data/students/${studentId}/performance`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.studentPerformance = response.data.data || {
            avgScore: 0,
            ranking: 0,
            excellentCount: 0,
            failCount: 0,
            attendanceRate: 0,
            credit: 0
          }
        } else {
          console.error('获取学生表现数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取学生表现数据失败')
          // 设置默认值
          this.studentPerformance = {
            avgScore: 0,
            ranking: 0,
            excellentCount: 0,
            failCount: 0,
            attendanceRate: 0,
            credit: 0
          }
        }
      } catch (error) {
        console.error('加载学生表现数据失败:', error)
        this.$message.error('加载学生表现数据失败，请稍后重试')
        // 设置默认值
        this.studentPerformance = {
          avgScore: 0,
          ranking: 0,
          excellentCount: 0,
          failCount: 0,
          attendanceRate: 0,
          credit: 0
        }
      }
    },
    
    // 加载学生课程
    async loadStudentCourses(studentId) {
      try {
        const response = await this.$axios.get(`/teacher/data/students/${studentId}/courses`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.currentStudentCourses = response.data.data || []
        } else {
          console.error('获取学生课程失败:', response.data.message)
          this.$message.error(response.data.message || '获取学生课程失败')
          this.currentStudentCourses = []
        }
      } catch (error) {
        console.error('加载学生课程失败:', error)
        this.$message.error('加载学生课程失败，请稍后重试')
        this.currentStudentCourses = []
      }
    },
    
    // 初始化成绩趋势图
    initGradeTrendChart() {
      const ctx = this.$refs.gradeTrendChart
      if (!ctx) return
      
      // 销毁之前的图表
      if (this.gradeTrendChart) {
        this.gradeTrendChart.destroy()
      }
      
      // 创建新图表
      this.gradeTrendChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['2021-2022第一学期', '2021-2022第二学期', '2022-2023第一学期', '2022-2023第二学期', '2023-2024第一学期'],
          datasets: [{
            label: '平均成绩',
            data: [75, 78, 82, 85, 88],
            borderColor: '#409eff',
            backgroundColor: 'rgba(64, 158, 255, 0.1)',
            tension: 0.4,
            fill: true
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              mode: 'index',
              intersect: false
            }
          },
          scales: {
            y: {
              min: 60,
              max: 100,
              ticks: {
                stepSize: 10
              }
            }
          }
        }
      })
    },
    
    // 查看学生成绩
    async viewStudentGrades(student) {
      this.viewingStudent = student
      // 显示加载状态
      this.showStudentGradesDialog = true
      // 如果在详情页，关闭详情页
      this.showStudentDetailsDialog = false
      // 加载学生成绩
      await this.loadStudentGrades(student.id)
    },
    
    // 加载学生成绩
    async loadStudentGrades(studentId) {
      try {
        const response = await this.$axios.get(`/teacher/data/students/${studentId}/grades`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.studentGrades = response.data.data || []
        } else {
          console.error('获取学生成绩失败:', response.data.message)
          this.$message.error(response.data.message || '获取学生成绩失败')
          this.studentGrades = []
        }
      } catch (error) {
        console.error('加载学生成绩失败:', error)
        this.$message.error('加载学生成绩失败，请稍后重试')
        this.studentGrades = []
      }
    },
    
    // 更新学生成绩统计
    updateStudentGradeStats(grades) {
      const total = grades.length
      const sumScore = grades.reduce((sum, grade) => sum + grade.score, 0)
      const totalCredit = grades.reduce((sum, grade) => sum + grade.credit, 0)
      const passCount = grades.filter(grade => grade.score >= 60).length
      
      this.studentGradeStats = {
        avgScore: sumScore / (total || 1),
        totalCredit,
        passRate: (passCount / (total || 1)) * 100
      }
    },
    
    // 发送消息
    sendMessage(student) {
      this.messageRecipient = student
      this.messageForm = {
        type: 'general',
        title: '',
        content: '',
        priority: 2
      }
      // 确保在下一个tick中更新对话框状态
      this.$nextTick(() => {
        this.showMessageDialog = true
      })
    },
    
    // 确认发送消息
    sendMessageConfirm() {
      this.$refs.messageForm.validate((valid) => {
        if (valid) {
          // 实际项目中应该调用后端API
          this.$message.success('消息发送成功')
          this.showMessageDialog = false
        }
      })
    },
    
    // 导出学生名单
    exportStudentList() {
      // 实际项目中应该调用后端API
      this.$message.success('学生名单导出成功')
    },
    
    // 导出学生档案
    exportStudentProfile() {
      // 实际项目中应该调用后端API
      this.$message.success('学生档案导出成功')
    },
    
    // 导出学生成绩
    exportStudentGrades() {
      // 实际项目中应该调用后端API
      this.$message.success('学生成绩导出成功')
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        normal: '正常',
        warning: '预警',
        active: '在读',
        suspended: '休学',
        dropout: '退学',
        graduated: '毕业'
      }
      return statusMap[status] || '未知'
    },
    
    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        normal: 'success',
        warning: 'warning',
        active: 'primary',
        suspended: 'info',
        dropout: 'danger',
        graduated: 'success'
      }
      return statusMap[status] || 'info'
    },
    
    // 计算等级
    calculateGradeLevel(score) {
      if (score >= 90) {
        return '优秀'
      } else if (score >= 80) {
        return '良好'
      } else if (score >= 70) {
        return '中等'
      } else if (score >= 60) {
        return '及格'
      } else {
        return '不及格'
      }
    },
    
    // 获取分数等级标签类型
    getScoreLevelTagType(score) {
      if (score >= 90) {
        return 'success'
      } else if (score >= 80) {
        return 'primary'
      } else if (score >= 70) {
        return 'info'
      } else if (score >= 60) {
        return 'warning'
      } else {
        return 'danger'
      }
    },
    
    // 获取分数等级样式类
    getScoreLevelClass(score) {
      if (score >= 90) {
        return 'score-excellent'
      } else if (score >= 80) {
        return 'score-good'
      } else if (score >= 70) {
        return 'score-average'
      } else if (score >= 60) {
        return 'score-pass'
      } else {
        return 'score-fail'
      }
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    
    // 格式化身份证号（脱敏）
    formatIdCard(idCard) {
      if (!idCard || idCard.length < 10) return ''
      return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    
    // 关闭学生详情对话框
    handleStudentDetailsDialogClose() {
      this.showStudentDetailsDialog = false
      this.currentStudent = {}
      // 销毁图表
      if (this.gradeTrendChart) {
        this.gradeTrendChart.destroy()
        this.gradeTrendChart = null
      }
    },
    
    // 关闭学生成绩对话框
    handleStudentGradesDialogClose() {
      this.showStudentGradesDialog = false
      this.viewingStudent = {}
      this.studentGrades = []
      this.selectedGradeSemester = ''
      this.selectedGradeType = ''
    },
    
    // 关闭消息对话框
    handleMessageDialogClose() {
      this.showMessageDialog = false
      this.messageRecipient = {}
      this.messageForm = {}
    },
    
    // 分页相关方法
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    }
  }
}
</script>

<style scoped>
/* 引入公共样式 */
@import '../../styles/common-styles.css';

/* 本地样式 */
.student-management-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.student-management-card {
  margin-bottom: 20px;
}

/* 学生统计概览 */
.student-stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

/* 表格样式 */
.student-name-cell {
  display: flex;
  align-items: center;
  gap: 5px;
}

.gender-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.gender-indicator.male {
  background-color: #409eff;
}

.gender-indicator.female {
  background-color: #67c23a;
}

/* 成绩标签样式 */
.score-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.score-excellent {
  background-color: #f0f9eb;
  color: #67c23a;
}

.score-good {
  background-color: #ecf5ff;
  color: #409eff;
}

.score-average {
  background-color: #f0f2f5;
  color: #909399;
}

.score-pass {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.score-fail {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 学生详情样式 */
.student-details-container {
  padding: 10px 0;
}

.student-avatar {
  text-align: center;
  padding: 20px;
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f2f5;
}

.student-name-large {
  font-size: 20px;
  font-weight: bold;
  margin-top: 15px;
  margin-bottom: 10px;
  color: #303133;
}

.student-basic-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.gender-tag {
  margin-bottom: 5px;
}

.info-text {
  font-size: 14px;
  color: #606266;
}

.study-performance-section,
.grade-trend-section,
.course-grades-section {
  margin-top: 25px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.study-performance-section h3,
.grade-trend-section h3,
.course-grades-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.chart-container {
  height: 200px;
  background-color: white;
  padding: 15px;
  border-radius: 4px;
}

/* 学生成绩样式 */
.student-grades-container {
  padding: 10px 0;
}

.student-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.student-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.student-info p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.grades-filter-section {
  margin-bottom: 20px;
}

.student-grade-stats {
  margin-bottom: 20px;
}

/* 消息对话框样式 */
.message-container {
  padding: 10px 0;
}

.recipient-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.recipient-info p {
  margin: 5px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .student-stats-overview .el-col,
  .student-grade-stats .el-col,
  .study-performance-section .el-col {
    width: 50%;
    margin-bottom: 15px;
  }
  
  .search-filter-section .el-col,
  .grades-filter-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .search-filter-section .el-col:last-child,
  .grades-filter-section .el-col:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .student-management-container {
    padding: 10px;
  }
  
  .student-stats-overview .el-col,
  .student-grade-stats .el-col,
  .study-performance-section .el-col {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .student-details-container .el-col {
    width: 100%;
  }
  
  .student-avatar {
    padding: 15px 0;
  }
}
</style>