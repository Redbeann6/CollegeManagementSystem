<template>
  <div class="teacher-exams-management">
    <div class="exams-header">
      <h1>考试管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateExamDialog">
          <i class="el-icon-plus"></i> 创建考试
        </el-button>
        <el-select v-model="courseFilter" placeholder="选择课程" class="filter-select">
          <el-option label="全部课程" value=""></el-option>
          <el-option v-for="course in teacherCourses" :key="course.id" :label="course.name" :value="course.id"></el-option>
        </el-select>
        <el-select v-model="examFilter" placeholder="考试状态" class="filter-select">
          <el-option label="全部状态" value=""></el-option>
          <el-option label="未开始" value="not_started"></el-option>
          <el-option label="进行中" value="ongoing"></el-option>
          <el-option label="已结束" value="completed"></el-option>
        </el-select>
        <el-input
          v-model="searchQuery"
          placeholder="搜索考试名称或课程名称"
          prefix-icon="el-icon-search"
          class="search-input"
        ></el-input>
      </div>
    </div>

    <div class="exams-content">
      <!-- 考试管理标签页 -->
      <el-tabs v-model="activeTab" class="exams-tabs">
        <!-- 我的考试列表 -->
        <el-tab-pane label="我的考试" name="exams">
          <el-table
            v-loading="loading"
            :data="filteredExams"
            style="width: 100%"
            border
          >
            <el-table-column prop="examName" label="考试名称" min-width="180"></el-table-column>
            <el-table-column prop="courseName" label="所属课程" min-width="150"></el-table-column>
            <el-table-column prop="examDate" label="考试日期" min-width="120">
              <template slot-scope="scope">
                {{ formatDate(scope.row.examDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="examTime" label="考试时间" min-width="100"></el-table-column>
            <el-table-column prop="location" label="考试地点" min-width="120"></el-table-column>
            <el-table-column prop="type" label="考试类型" min-width="80"></el-table-column>
            <el-table-column prop="statusText" label="状态" min-width="80">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ scope.row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button size="small" type="primary" @click="viewExamDetails(scope.row.id)">查看详情</el-button>
                <el-button size="small" type="success" @click="manageScores(scope.row.id)" :disabled="!scope.row.status || scope.row.status !== 'completed'">管理成绩</el-button>
                <el-button size="small" type="danger" @click="deleteExam(scope.row.id)" :disabled="scope.row.status === 'ongoing'">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="filteredExams.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无考试数据"></el-empty>
          </div>
        </el-tab-pane>

        <!-- 考试统计分析 -->
        <el-tab-pane label="统计分析" name="stats">
          <div class="stats-container">
            <div class="stats-overview">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-number">{{ totalExams }}</div>
                  <div class="stat-label">总考试数</div>
                </div>
              </el-card>
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-number">{{ upcomingExamsCount }}</div>
                  <div class="stat-label">即将到来的考试</div>
                </div>
              </el-card>
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-number">{{ completedExamsCount }}</div>
                  <div class="stat-label">已完成的考试</div>
                </div>
              </el-card>
            </div>

            <el-card class="chart-card">
              <div slot="header" class="card-header">
                <span>考试成绩分布</span>
                <el-select v-model="selectedExamForChart" placeholder="选择考试">
                  <el-option
                    v-for="exam in completedExams"
                    :key="exam.id"
                    :label="exam.courseName + ' - ' + exam.examName"
                    :value="exam.id"
                  ></el-option>
                </el-select>
              </div>
              <div class="chart-container">
                <div v-if="selectedExamForChart" ref="chartRef" style="height: 300px;"></div>
                <div v-else class="chart-placeholder">
                  请选择一个已完成的考试查看成绩分布
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 考试详情对话框 -->
    <el-dialog
      title="考试详情"
      v-model="examDetailVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedExam" class="exam-detail-content">
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-descriptions border :column="1">
            <el-descriptions-item label="考试名称">{{ selectedExam.examName }}</el-descriptions-item>
            <el-descriptions-item label="所属课程">{{ selectedExam.courseName }}</el-descriptions-item>
            <el-descriptions-item label="考试日期">{{ formatDate(selectedExam.examDate) }}</el-descriptions-item>
            <el-descriptions-item label="考试时间">{{ selectedExam.examTime }}</el-descriptions-item>
            <el-descriptions-item label="考试地点">{{ selectedExam.location }}</el-descriptions-item>
            <el-descriptions-item label="考试时长">{{ selectedExam.duration }}分钟</el-descriptions-item>
            <el-descriptions-item label="考试类型">{{ selectedExam.type }}</el-descriptions-item>
            <el-descriptions-item label="总分">{{ selectedExam.totalPoints }}分</el-descriptions-item>
            <el-descriptions-item label="考试状态"><el-tag :type="getStatusType(selectedExam.status)">{{ selectedExam.statusText }}</el-tag></el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h3>考试说明</h3>
          <div class="exam-description">{{ selectedExam.description }}</div>
        </div>

        <div class="detail-section">
          <h3>考试要求</h3>
          <div class="exam-requirements">
            <ul>
              <li v-for="(req, index) in selectedExam.requirements" :key="index">{{ req }}</li>
            </ul>
          </div>
        </div>

        <div v-if="selectedExam.status === 'completed'" class="detail-section">
          <h3>考试统计</h3>
          <el-descriptions border :column="2">
            <el-descriptions-item label="参考人数">{{ selectedExam.statistics?.totalStudents || 0 }}</el-descriptions-item>
            <el-descriptions-item label="平均分">{{ selectedExam.statistics?.averageScore || 0 }}</el-descriptions-item>
            <el-descriptions-item label="最高分">{{ selectedExam.statistics?.maxScore || 0 }}</el-descriptions-item>
            <el-descriptions-item label="最低分">{{ selectedExam.statistics?.minScore || 0 }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="examDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="openEditExamDialog" v-if="selectedExam && selectedExam.status !== 'ongoing'">编辑</el-button>
      </div>
    </el-dialog>

    <!-- 创建/编辑考试对话框 -->
    <el-dialog
      :title="isEditMode ? '编辑考试' : '创建考试'"
      v-model="createExamVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="examForm" :rules="examFormRules" ref="examForm" label-width="100px">
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="examForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="course in teacherCourses" :key="course.id" :label="course.name" :value="course.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="考试名称" prop="examName">
          <el-input v-model="examForm.examName" placeholder="请输入考试名称"></el-input>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker v-model="examForm.examDate" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="考试时间" prop="examTime">
          <el-time-picker v-model="examForm.examTime" format="HH:mm" placeholder="选择开始时间" style="width: 100%"></el-time-picker>
        </el-form-item>
        <el-form-item label="考试地点" prop="location">
          <el-input v-model="examForm.location" placeholder="请输入考试地点"></el-input>
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="examForm.duration" :min="15" :max="480" placeholder="分钟" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="考试类型" prop="type">
          <el-select v-model="examForm.type" placeholder="请选择考试类型" style="width: 100%">
            <el-option label="闭卷" value="闭卷"></el-option>
            <el-option label="开卷" value="开卷"></el-option>
            <el-option label="上机" value="上机"></el-option>
            <el-option label="实验操作" value="实验操作"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="总分" prop="totalPoints">
          <el-input-number v-model="examForm.totalPoints" :min="50" :max="150" placeholder="分数" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="考试说明" prop="description">
          <el-input v-model="examForm.description" type="textarea" :rows="4" placeholder="请输入考试说明"></el-input>
        </el-form-item>
        <el-form-item label="考试要求" prop="requirements">
          <div v-for="(req, index) in examForm.requirements" :key="index" class="requirement-item">
            <el-input v-model="req" placeholder="请输入要求内容" class="requirement-input"></el-input>
            <el-button type="danger" icon="el-icon-delete" circle size="small" @click="removeRequirement(index)"></el-button>
          </div>
          <el-button type="primary" icon="el-icon-plus" size="small" @click="addRequirement">添加要求</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelCreateExam">取消</el-button>
        <el-button type="primary" @click="submitExamForm">保存</el-button>
      </div>
    </el-dialog>

    <!-- 成绩管理对话框 -->
    <el-dialog
      title="成绩管理"
      v-model="scoreManagementVisible"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentExam" class="score-management-content">
        <div class="exam-info-header">
          <h3>{{ currentExam.courseName }} - {{ currentExam.examName }}</h3>
          <div class="exam-meta">总分：{{ currentExam.totalPoints }}分 | 考试时间：{{ formatDate(currentExam.examDate) }} {{ currentExam.examTime }}</div>
        </div>
        
        <div class="score-actions">
          <el-button type="primary" size="small" @click="importScores">导入成绩</el-button>
          <el-button type="success" size="small" @click="exportScores">导出成绩</el-button>
          <el-button type="warning" size="small" @click="calculateStatistics">计算统计</el-button>
        </div>
        
        <el-table
          v-loading="loadingScores"
          :data="filteredScores"
          style="width: 100%"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
          <el-table-column prop="studentName" label="姓名" width="100"></el-table-column>
          <el-table-column prop="score" label="分数" width="100">
            <template slot-scope="scope">
              <div v-if="!isEditingScore[scope.row.studentId]">
                <span v-if="scope.row.score" :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
                <span v-else class="no-score">未录入</span>
              </div>
              <div v-else>
                <el-input-number
                  v-model="scope.row.score"
                  :min="0"
                  :max="currentExam.totalPoints"
                  :step="0.5"
                  @change="updateScore(scope.row)"
                  @blur="isEditingScore[scope.row.studentId] = false"
                ></el-input-number>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="等级" width="80">
            <template slot-scope="scope">
              <span v-if="scope.row.grade" :class="getGradeClass(scope.row.grade)">{{ scope.row.grade }}</span>
              <span v-else class="no-score">未评定</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template slot-scope="scope">
              <el-button
                type="text"
                @click="startEditingScore(scope.row.studentId)"
                v-if="!isEditingScore[scope.row.studentId]"
              >
                编辑
              </el-button>
              <el-button type="text" @click="viewStudentDetails(scope.row.studentId)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="score-management-footer">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="filteredScores.length"
            :page-size="pageSize"
            :current-page.sync="currentPage"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="scoreManagementVisible = false">关闭</el-button>
        <el-button type="success" @click="saveAllScores" :loading="savingScores">保存全部</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherExamsManagement',
  data() {
    return {
      // 考试数据
      exams: [],
      teacherCourses: [],
      loading: false,
      activeTab: 'exams',
      // 筛选条件
      courseFilter: '',
      examFilter: '',
      searchQuery: '',
      // 对话框状态
      examDetailVisible: false,
      createExamVisible: false,
      scoreManagementVisible: false,
      selectedExam: null,
      currentExam: null,
      isEditMode: false,
      // 表单数据
      examForm: {
        courseId: '',
        examName: '',
        examDate: '',
        examTime: '',
        location: '',
        duration: 120,
        type: '闭卷',
        totalPoints: 100,
        description: '',
        requirements: ['']
      },
      examFormRules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        examName: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
        examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
        examTime: [{ required: true, message: '请选择考试时间', trigger: 'change' }],
        location: [{ required: true, message: '请输入考试地点', trigger: 'blur' }],
        duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }],
        totalPoints: [{ required: true, message: '请输入总分', trigger: 'blur' }],
        description: [{ required: true, message: '请输入考试说明', trigger: 'blur' }]
      },
      // 成绩管理相关
      examScores: [],
      filteredScores: [],
      loadingScores: false,
      savingScores: false,
      currentPage: 1,
      pageSize: 10,
      selectedRows: [],
      isEditingScore: {},
      // 统计分析相关
      selectedExamForChart: '',
      chartInstance: null
    }
  },
  computed: {
    // 过滤后的考试列表
    filteredExams() {
      return this.exams.filter(exam => {
        const matchesCourse = !this.courseFilter || exam.courseId === this.courseFilter
        const matchesStatus = !this.examFilter || exam.status === this.examFilter
        const matchesSearch = !this.searchQuery || 
          exam.examName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          exam.courseName.toLowerCase().includes(this.searchQuery.toLowerCase())
        
        return matchesCourse && matchesStatus && matchesSearch
      }).sort((a, b) => new Date(b.examDate) - new Date(a.examDate))
    },
    // 统计数据
    totalExams() {
      return this.exams.length
    },
    upcomingExamsCount() {
      const now = new Date()
      return this.exams.filter(exam => {
        const examDate = new Date(exam.examDate)
        return examDate >= now && (exam.status === 'not_started' || exam.status === 'ongoing')
      }).length
    },
    completedExamsCount() {
      return this.exams.filter(exam => exam.status === 'completed').length
    },
    // 已完成的考试（用于图表选择）
    completedExams() {
      return this.exams.filter(exam => exam.status === 'completed')
    }
  },
  mounted() {
    this.loadTeacherCourses()
    this.loadExams()
  },
  methods: {
    // 加载教师课程
    async loadTeacherCourses() {
      try {
        const response = await this.$axios.get('/teacher/data/courses', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.teacherCourses = response.data.data || []
        } else {
          console.error('获取教师课程失败:', response.data.message)
          this.$message.error(response.data.message || '获取教师课程失败')
          this.teacherCourses = []
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.$message.error('加载课程数据失败，请稍后重试')
      }
    },
    
    // 加载考试数据
    async loadExams() {
      try {
        this.loading = true
        const response = await this.$axios.get('/teacher/data/exams', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.exams = response.data.data || []
        } else {
          console.error('获取考试数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取考试数据失败')
          this.exams = []
        }
        
        // 如果有已完成的考试，默认选择第一个用于图表展示
        const firstCompletedExam = this.exams.find(exam => exam.status === 'completed')
        if (firstCompletedExam) {
          this.selectedExamForChart = firstCompletedExam.id
        }
      } catch (error) {
        console.error('加载考试失败:', error)
        this.$message.error('加载考试数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 打开创建考试对话框
    openCreateExamDialog() {
      this.isEditMode = false
      this.resetExamForm()
      this.createExamVisible = true
    },
    
    // 打开编辑考试对话框
    openEditExamDialog() {
      if (!this.selectedExam) return
      
      this.isEditMode = true
      this.examForm = {
        ...this.selectedExam,
        requirements: [...this.selectedExam.requirements]
      }
      this.createExamVisible = true
    },
    
    // 重置考试表单
    resetExamForm() {
      this.examForm = {
        courseId: '',
        examName: '',
        examDate: '',
        examTime: '',
        location: '',
        duration: 120,
        type: '闭卷',
        totalPoints: 100,
        description: '',
        requirements: ['']
      }
    },
    
    // 添加考试要求
    addRequirement() {
      this.examForm.requirements.push('')
    },
    
    // 移除考试要求
    removeRequirement(index) {
      if (this.examForm.requirements.length > 1) {
        this.examForm.requirements.splice(index, 1)
      }
    },
    
    // 提交考试表单
    async submitExamForm() {
      this.$refs.examForm.validate(async (valid) => {
        if (valid) {
          try {
            // 过滤空的要求
            const filteredRequirements = this.examForm.requirements.filter(req => req.trim())
            if (filteredRequirements.length === 0) {
              this.$message.error('请至少添加一项考试要求')
              return
            }
            
            // 准备提交数据
            const examData = {
              ...this.examForm,
              requirements: filteredRequirements
            }
            
            // 实际项目中应该调用后端API
            // if (this.isEditMode) {
            //   await axios.put(`/exams/${this.selectedExam.id}`, examData, {
            //     headers: {
            //       'Authorization': `Bearer ${localStorage.getItem('token')}`
            //     }
            //   })
            //   this.$message.success('考试更新成功')
            // } else {
            //   await axios.post('/exams', examData, {
            //     headers: {
            //       'Authorization': `Bearer ${localStorage.getItem('token')}`
            //     }
            //   })
            //   this.$message.success('考试创建成功')
            // }
            
            // 模拟更新数据
            if (this.isEditMode) {
              const index = this.exams.findIndex(e => e.id === this.selectedExam.id)
              if (index !== -1) {
                this.exams[index] = {
                  ...examData,
                  id: this.selectedExam.id,
                  courseName: this.teacherCourses.find(c => c.id === examData.courseId)?.name || '',
                  status: this.selectedExam.status,
                  statusText: this.selectedExam.statusText
                }
              }
            } else {
              const newExam = {
                ...examData,
                id: Date.now(), // 模拟生成ID
                courseName: this.teacherCourses.find(c => c.id === examData.courseId)?.name || '',
                status: 'not_started',
                statusText: '未开始'
              }
              this.exams.unshift(newExam)
            }
            
            this.createExamVisible = false
            this.$message.success(this.isEditMode ? '考试更新成功' : '考试创建成功')
          } catch (error) {
            console.error('保存考试失败:', error)
            this.$message.error('保存考试失败，请稍后重试')
          }
        }
      })
    },
    
    // 取消创建考试
    cancelCreateExam() {
      this.createExamVisible = false
    },
    
    // 查看考试详情
    viewExamDetails(examId) {
      const exam = this.exams.find(e => e.id === examId)
      if (exam) {
        this.selectedExam = { ...exam }
        this.examDetailVisible = true
      }
    },
    
    // 删除考试
    deleteExam(examId) {
      this.$confirm('确定要删除此考试吗？删除后将无法恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 实际项目中应该调用后端API
          // await axios.delete(`/exams/${examId}`, {
          //   headers: {
          //     'Authorization': `Bearer ${localStorage.getItem('token')}`
          //   }
          // })
          
          // 模拟删除
          const index = this.exams.findIndex(e => e.id === examId)
          if (index !== -1) {
            this.exams.splice(index, 1)
          }
          
          this.$message.success('考试删除成功')
        } catch (error) {
          console.error('删除考试失败:', error)
          this.$message.error('删除考试失败，请稍后重试')
        }
      })
    },
    
    // 管理成绩
    async manageScores(examId) {
      try {
        this.loadingScores = true
        const exam = this.exams.find(e => e.id === examId)
        if (!exam) return
        
        this.currentExam = { ...exam }
        
        // 实际项目中应该从后端API获取成绩数据
        // const response = await axios.get(`/exams/${examId}/scores`, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`
        //   }
        // })
        // this.examScores = response.data
        
        // 使用模拟数据
        this.examScores = this.getMockScores(examId)
        this.filteredScores = [...this.examScores]
        this.isEditingScore = {}
        this.currentPage = 1
        this.selectedRows = []
        
        this.scoreManagementVisible = true
      } catch (error) {
        console.error('加载成绩失败:', error)
        this.$message.error('加载成绩数据失败，请稍后重试')
      } finally {
        this.loadingScores = false
      }
    },
    
    // 开始编辑分数
    startEditingScore(studentId) {
      this.isEditingScore[studentId] = true
    },
    
    // 更新分数
    updateScore(row) {
      // 计算等级
      if (row.score !== null && row.score !== undefined) {
        const scoreRatio = row.score / this.currentExam.totalPoints
        if (scoreRatio >= 0.9) row.grade = '优秀'
        else if (scoreRatio >= 0.8) row.grade = '良好'
        else if (scoreRatio >= 0.7) row.grade = '中等'
        else if (scoreRatio >= 0.6) row.grade = '及格'
        else row.grade = '不及格'
      }
    },
    
    // 保存全部成绩
    async saveAllScores() {
      try {
        this.savingScores = true
        
        // 实际项目中应该调用后端API
        // await axios.put(`/exams/${this.currentExam.id}/scores`, this.examScores, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`
        //   }
        // })
        
        // 模拟保存
        this.$message.success('成绩保存成功')
        this.calculateStatistics()
      } catch (error) {
        console.error('保存成绩失败:', error)
        this.$message.error('保存成绩失败，请稍后重试')
      } finally {
        this.savingScores = false
      }
    },
    
    // 计算统计信息
    calculateStatistics() {
      const scoredStudents = this.examScores.filter(s => s.score !== null && s.score !== undefined)
      if (scoredStudents.length === 0) {
        this.$message.warning('暂无成绩数据可统计')
        return
      }
      
      const scores = scoredStudents.map(s => s.score)
      const totalStudents = this.examScores.length
      const averageScore = scores.reduce((sum, score) => sum + score, 0) / scores.length
      const maxScore = Math.max(...scores)
      const minScore = Math.min(...scores)
      
      // 更新考试统计信息
      const examIndex = this.exams.findIndex(e => e.id === this.currentExam.id)
      if (examIndex !== -1) {
        this.exams[examIndex].statistics = {
          totalStudents,
          averageScore: averageScore.toFixed(2),
          maxScore,
          minScore
        }
      }
      
      this.$message.success('统计信息已更新')
    },
    
    // 导入成绩
    importScores() {
      this.$message.info('导入功能开发中...')
    },
    
    // 导出成绩
    exportScores() {
      this.$message.info('导出功能开发中...')
    },
    
    // 查看学生详情
    viewStudentDetails(studentId) {
      this.$message.info('查看学生详情功能开发中...')
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    
    // 处理分页变化
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage
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
    
    // 获取等级样式类
    getGradeClass(grade) {
      const classMap = {
        '优秀': 'score-excellent',
        '良好': 'score-good',
        '中等': 'score-average',
        '及格': 'score-pass',
        '不及格': 'score-fail'
      }
      return classMap[grade] || ''
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    

  }
}
</script>

<style scoped>
.teacher-exams-management {
  padding: 20px;
}

.exams-header {
  margin-bottom: 24px;
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.exams-header h1 {
  margin-bottom: 16px;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-select {
  width: 160px;
}

.search-input {
  width: 240px;
}

.exams-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.exams-tabs {
  height: 100%;
}

.empty-state {
  padding: 60px 0;
}

/* 统计分析样式 */
.stats-container {
  padding: 24px;
}

.stats-overview {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  min-width: 0;
}

.stat-content {
  text-align: center;
  padding: 16px 0;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #26a69a;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.chart-card {
  margin-top: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.chart-container {
  position: relative;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  background-color: #f8f9fa;
  border-radius: 4px;
}

/* 考试详情样式 */
.exam-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 18px;
}

.exam-description,
.exam-requirements {
  line-height: 1.8;
  color: #606266;
}

.exam-requirements ul {
  padding-left: 24px;
  margin: 0;
}

.exam-requirements li {
  margin-bottom: 8px;
}

/* 成绩管理样式 */
.score-management-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.exam-info-header {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.exam-info-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.exam-meta {
  color: #606266;
  font-size: 14px;
}

.score-actions {
  display: flex;
  gap: 12px;
}

.requirement-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.requirement-input {
  flex: 1;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #e6a23c;
  font-weight: bold;
}

.score-average {
  color: #409eff;
  font-weight: bold;
}

.score-pass {
  color: #606266;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.no-score {
  color: #c0c4cc;
}

.score-management-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-exams-management {
    padding: 10px;
  }
  
  .exams-header {
    padding: 16px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-select,
  .search-input {
    width: 100% !important;
  }
  
  .stats-overview {
    flex-direction: column;
  }
  
  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .score-actions {
    flex-wrap: wrap;
  }
  
  .score-actions .el-button {
    flex: 1;
    min-width: 120px;
  }
}
</style>