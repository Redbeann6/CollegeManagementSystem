<template>
  <div class="grade-management-container">
    <div class="page-header">
      <h1>成绩管理</h1>
      <p>管理学生成绩、进行成绩统计与分析</p>
    </div>

    <!-- 主要内容区域 -->
    <el-card class="grade-management-card">
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
            <el-select v-model="selectedExamType" placeholder="考试类型" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="期中考试" value="midterm"></el-option>
              <el-option label="期末考试" value="final"></el-option>
              <el-option label="平时成绩" value="daily"></el-option>
              <el-option label="实验成绩" value="experiment"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="studentSearchQuery"
              placeholder="搜索学生姓名或学号"
              prefix-icon="el-icon-search"
              clearable
            ></el-input>
          </el-col>
          <el-col :span="6">
            <div class="action-buttons">
              <el-button type="info" icon="el-icon-refresh" @click="refreshGradeList">
                刷新
              </el-button>
              <el-button type="success" icon="el-icon-download" @click="exportGrades">
                导出成绩
              </el-button>
              <el-upload
                class="upload-demo"
                action=""
                :on-change="handleImportGrades"
                :auto-upload="false"
                accept=".xlsx,.xls"
                :show-file-list="false"
              >
                <el-button type="warning" icon="el-icon-upload2">
                  导入成绩
                </el-button>
              </el-upload>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 成绩统计概览 -->
      <div v-if="selectedCourse" class="grade-stats-overview">
        <el-row :gutter="20">
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value">{{ gradeStats.average.toFixed(1) }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value">{{ gradeStats.max }}</div>
              <div class="stat-label">最高分</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value">{{ gradeStats.min }}</div>
              <div class="stat-label">最低分</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value">{{ gradeStats.passRate.toFixed(1) }}%</div>
              <div class="stat-label">及格率</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value">{{ gradeStats.excellentRate.toFixed(1) }}%</div>
              <div class="stat-label">优秀率</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-card">
              <div class="stat-value">{{ gradeStats.stdDev.toFixed(1) }}</div>
              <div class="stat-label">标准差</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 成绩操作按钮 -->
      <div v-if="selectedCourse" class="grade-action-section">
        <el-row :gutter="20" align="middle">
          <el-col>
            <div class="action-buttons">
              <el-button type="primary" @click="batchEditGrades">
                <i class="el-icon-edit"></i> 批量编辑
              </el-button>
              <el-button type="success" @click="calculateFinalGrades">
                <i class="el-icon-s-data"></i> 计算总成绩
              </el-button>
              <el-button type="warning" @click="generateGradeReport">
                <i class="el-icon-document"></i> 生成成绩单
              </el-button>
              <el-button type="danger" @click="clearGrades">
                <i class="el-icon-delete"></i> 清空成绩
              </el-button>
            </div>
          </el-col>
          <el-col :span="8" class="grade-settings">
            <el-button type="text" @click="showGradeSettings = true">
              <i class="el-icon-setting"></i> 成绩计算设置
            </el-button>
            <el-popover
              v-model="showGradeDistribution"
              placement="top"
              width="600"
              trigger="click"
            >
              <template #reference>
                <el-button type="text">
                  <i class="el-icon-chart"></i> 成绩分布
                </el-button>
              </template>
              <div class="grade-distribution-chart">
                <canvas id="gradeDistributionChart" width="600" height="300"></canvas>
              </div>
            </el-popover>
          </el-col>
        </el-row>
      </div>

      <!-- 成绩列表表格 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="filteredGradeData"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="studentIdNumber" label="学号" width="120" sortable></el-table-column>
          <el-table-column prop="studentName" label="姓名" width="100"></el-table-column>
          <el-table-column prop="examName" label="考试名称" width="120"></el-table-column>
          <el-table-column prop="semester" label="学期" width="120"></el-table-column>
          <el-table-column prop="score" label="成绩" width="100" sortable>
            <template #default="scope">
              <div :class="['total-score', getScoreLevelClass(scope.row.score)]">
                {{ scope.row.score !== null ? scope.row.score : '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="等级" width="80">
            <template #default="scope">
              <el-tag :type="getGradeLevelType(scope.row.score)">
                {{ scope.row.grade || calculateGradeLevel(scope.row.score) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="feedback" label="备注" min-width="120">
            <template #default="scope">
              <el-input
                v-model="scope.row.feedback"
                type="textarea"
                :rows="2"
                size="small"
                @change="updateRemark(scope.row)"
              ></el-input>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="info" size="small" @click="viewScoreDetails(scope.row)">
                详情
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
          :total="totalStudents"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 批量编辑对话框 -->
    <el-dialog
      title="批量编辑成绩"
      v-model="showBatchEditDialog"
      width="800px"
      @close="handleBatchEditClose"
    >
      <div class="batch-edit-content">
        <el-form :model="batchEditForm" :rules="batchEditRules" ref="batchEditForm">
          <el-form-item label="编辑类型" prop="editType">
            <el-radio-group v-model="batchEditForm.editType">
              <el-radio label="set">设置固定值</el-radio>
              <el-radio label="add">加分</el-radio>
              <el-radio label="subtract">减分</el-radio>
              <el-radio label="multiply">乘以系数</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="成绩类型" prop="scoreType">
            <el-select v-model="batchEditForm.scoreType" placeholder="选择成绩类型">
              <el-option label="平时成绩" value="dailyScore"></el-option>
              <el-option label="期中成绩" value="midtermScore"></el-option>
              <el-option label="期末成绩" value="finalScore"></el-option>
              <el-option label="实验成绩" value="experimentScore"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="数值" prop="value">
            <el-input-number
              v-model="batchEditForm.value"
              :min="batchEditForm.editType === 'multiply' ? 0 : -100"
              :max="batchEditForm.editType === 'multiply' ? 2 : 100"
              :step="batchEditForm.editType === 'multiply' ? 0.1 : 1"
              placeholder="请输入数值"
            ></el-input-number>
          </el-form-item>
          
          <el-form-item label="适用范围">
            <el-radio-group v-model="batchEditForm.scope">
              <el-radio label="selected">已选中的学生</el-radio>
              <el-radio label="all">全部学生</el-radio>
              <el-radio label="condition">按条件筛选</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item v-if="batchEditForm.scope === 'condition'" label="筛选条件">
            <el-select v-model="batchEditForm.conditionType" placeholder="选择条件类型">
              <el-option label="成绩大于" value="gt"></el-option>
              <el-option label="成绩小于" value="lt"></el-option>
              <el-option label="成绩等于" value="eq"></el-option>
              <el-option label="成绩在区间内" value="between"></el-option>
              <el-option label="成绩为空" value="null"></el-option>
            </el-select>
            <div v-if="batchEditForm.conditionType !== 'null'" class="condition-values">
              <el-input-number
                v-if="batchEditForm.conditionType !== 'between'"
                v-model="batchEditForm.conditionValue"
                :min="0"
                :max="100"
                style="width: 120px;"
              ></el-input-number>
              <div v-else>
                <el-input-number
                  v-model="batchEditForm.conditionMin"
                  :min="0"
                  :max="100"
                  style="width: 120px; margin-right: 10px;"
                ></el-input-number>
                <span>至</span>
                <el-input-number
                  v-model="batchEditForm.conditionMax"
                  :min="0"
                  :max="100"
                  style="width: 120px; margin-left: 10px;"
                ></el-input-number>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="handleBatchEditClose">取消</el-button>
        <el-button type="primary" @click="confirmBatchEdit">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 成绩设置对话框 -->
    <el-dialog
      title="成绩计算设置"
      v-model="showGradeSettings"
      width="600px"
      @close="handleSettingsClose"
    >
      <div class="settings-content">
        <el-form :model="gradeSettingsForm" :rules="gradeSettingsRules" ref="gradeSettingsForm">
          <el-form-item label="平时成绩权重" prop="dailyWeight">
            <el-slider v-model="gradeSettingsForm.dailyWeight" :min="0" :max="100" :step="5"></el-slider>
            <div class="weight-value">{{ gradeSettingsForm.dailyWeight }}%</div>
          </el-form-item>
          
          <el-form-item label="期中成绩权重" prop="midtermWeight">
            <el-slider v-model="gradeSettingsForm.midtermWeight" :min="0" :max="100" :step="5"></el-slider>
            <div class="weight-value">{{ gradeSettingsForm.midtermWeight }}%</div>
          </el-form-item>
          
          <el-form-item label="期末成绩权重" prop="finalWeight">
            <el-slider v-model="gradeSettingsForm.finalWeight" :min="0" :max="100" :step="5"></el-slider>
            <div class="weight-value">{{ gradeSettingsForm.finalWeight }}%</div>
          </el-form-item>
          
          <el-form-item label="实验成绩权重" prop="experimentWeight">
            <el-slider v-model="gradeSettingsForm.experimentWeight" :min="0" :max="100" :step="5"></el-slider>
            <div class="weight-value">{{ gradeSettingsForm.experimentWeight }}%</div>
          </el-form-item>
          
          <el-form-item>
            <div class="weight-sum">总权重: {{ gradeSettingsForm.dailyWeight + gradeSettingsForm.midtermWeight + gradeSettingsForm.finalWeight + gradeSettingsForm.experimentWeight }}%</div>
            <div v-if="gradeSettingsForm.dailyWeight + gradeSettingsForm.midtermWeight + gradeSettingsForm.finalWeight + gradeSettingsForm.experimentWeight !== 100" class="weight-error">总权重必须等于100%</div>
          </el-form-item>
          
          <el-divider>等级评定标准</el-divider>
          
          <el-form-item label="优秀" prop="excellentThreshold">
            <el-input-number
              v-model="gradeSettingsForm.excellentThreshold"
              :min="0"
              :max="100"
              placeholder="优秀分数线"
            ></el-input-number>
          </el-form-item>
          
          <el-form-item label="良好" prop="goodThreshold">
            <el-input-number
              v-model="gradeSettingsForm.goodThreshold"
              :min="0"
              :max="100"
              placeholder="良好分数线"
            ></el-input-number>
          </el-form-item>
          
          <el-form-item label="及格" prop="passThreshold">
            <el-input-number
              v-model="gradeSettingsForm.passThreshold"
              :min="0"
              :max="100"
              placeholder="及格分数线"
            ></el-input-number>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="handleSettingsClose">取消</el-button>
        <el-button type="primary" @click="saveGradeSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 成绩分布弹出框 -->
    <el-dialog
      title="成绩分布图"
      v-model="showGradeChartDialog"
      width="800px"
    >
      <div class="grade-chart-container">
        <div class="chart-section">
          <h3>分数段分布</h3>
          <canvas id="gradeSegmentChart" width="800" height="400"></canvas>
        </div>
        <div class="chart-section">
          <h3>成绩统计详情</h3>
          <div class="grade-stats-detail">
            <div class="stat-row">
              <div class="stat-label">平均分:</div>
              <div class="stat-value">{{ gradeStats.average.toFixed(2) }}</div>
            </div>
            <div class="stat-row">
              <div class="stat-label">中位数:</div>
              <div class="stat-value">{{ gradeStats.median }}</div>
            </div>
            <div class="stat-row">
              <div class="stat-label">标准差:</div>
              <div class="stat-value">{{ gradeStats.stdDev.toFixed(2) }}</div>
            </div>
            <div class="stat-row">
              <div class="stat-label">偏度:</div>
              <div class="stat-value">{{ gradeStats.skewness.toFixed(2) }}</div>
            </div>
            <div class="stat-row">
              <div class="stat-label">峰度:</div>
              <div class="stat-value">{{ gradeStats.kurtosis.toFixed(2) }}</div>
            </div>
          </div>
          <div class="grade-segment-stats">
            <div class="segment-item" v-for="segment in gradeSegments" :key="segment.label">
              <div class="segment-label">{{ segment.label }}</div>
              <div class="segment-bar">
                <div 
                  class="segment-fill" 
                  :style="{ width: segment.percentage + '%', backgroundColor: segment.color }"
                ></div>
              </div>
              <div class="segment-count">{{ segment.count }}人 ({{ segment.percentage.toFixed(1) }}%)</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherGradeManagement',
  data() {
    return {
      // 筛选条件
      selectedCourse: '',
      selectedExamType: '',
      studentSearchQuery: '',
      
      // 分页参数
      currentPage: 1,
      pageSize: 10,
      totalStudents: 0,
      
      // 数据加载状态
      loading: false,
      
      // 教学课程
      teachingCourses: [],
      
      // 成绩数据
      gradeData: [],
      
      // 选中的行
      selectedRows: [],
      
      // 编辑状态
      editingRowId: null,
      editForm: {
        dailyScore: null,
        midtermScore: null,
        finalScore: null,
        experimentScore: null
      },
      
      // 成绩统计
      gradeStats: {
        average: 0,
        max: 0,
        min: 0,
        passRate: 0,
        excellentRate: 0,
        stdDev: 0,
        median: 0,
        skewness: 0,
        kurtosis: 0
      },
      
      // 成绩分布
      gradeSegments: [],
      
      // 弹出框状态
      showBatchEditDialog: false,
      showGradeSettings: false,
      showGradeDistribution: false,
      showGradeChartDialog: false,
      
      // 批量编辑表单
      batchEditForm: {
        editType: 'set',
        scoreType: 'dailyScore',
        value: 0,
        scope: 'all',
        conditionType: 'gt',
        conditionValue: 0,
        conditionMin: 0,
        conditionMax: 0
      },
      
      // 批量编辑验证规则
      batchEditRules: {
        editType: [
          { required: true, message: '请选择编辑类型', trigger: 'change' }
        ],
        scoreType: [
          { required: true, message: '请选择成绩类型', trigger: 'change' }
        ],
        value: [
          { required: true, message: '请输入数值', trigger: 'blur' }
        ]
      },
      
      // 成绩设置表单
      gradeSettingsForm: {
        dailyWeight: 20,
        midtermWeight: 30,
        finalWeight: 40,
        experimentWeight: 10,
        excellentThreshold: 90,
        goodThreshold: 80,
        passThreshold: 60
      },
      
      // 成绩设置验证规则
      gradeSettingsRules: {
        dailyWeight: [
          { required: true, message: '请设置平时成绩权重', trigger: 'blur' }
        ],
        midtermWeight: [
          { required: true, message: '请设置期中成绩权重', trigger: 'blur' }
        ],
        finalWeight: [
          { required: true, message: '请设置期末成绩权重', trigger: 'blur' }
        ],
        excellentThreshold: [
          { required: true, message: '请设置优秀分数线', trigger: 'blur' }
        ],
        goodThreshold: [
          { required: true, message: '请设置良好分数线', trigger: 'blur' }
        ],
        passThreshold: [
          { required: true, message: '请设置及格分数线', trigger: 'blur' }
        ]
      },
      
      // Chart实例
      gradeChart: null
    }
  },
  
  computed: {
    // 过滤后的成绩数据
    filteredGradeData() {
      let filtered = [...this.gradeData]
      
      // 按学生姓名或学号搜索
      if (this.studentSearchQuery) {
        const query = this.studentSearchQuery.toLowerCase()
        filtered = filtered.filter(item => 
          item.studentName.toLowerCase().includes(query) || 
          item.studentId.toLowerCase().includes(query)
        )
      }
      
      // 更新总数
      this.totalStudents = filtered.length
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return filtered.slice(start, end)
    }
  },
  
  mounted() {
    // 加载教学课程
    this.loadTeachingCourses()
    
    // 监听成绩分布弹出框显示
    this.$watch('showGradeDistribution', (newVal) => {
      if (newVal && this.selectedCourse) {
        this.showGradeChartDialog = true
        this.showGradeDistribution = false
      }
    })
  },
  
  beforeDestroy() {
    // 销毁Chart实例
    if (this.gradeChart) {
      this.gradeChart.destroy()
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
          
          // 默认选择第一个课程
          if (this.teachingCourses.length > 0) {
            this.selectedCourse = this.teachingCourses[0].id
            this.onCourseChange(this.selectedCourse)
          }
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
    
    // 课程变更处理
    onCourseChange(courseId) {
      if (courseId) {
        this.loadGradeData(courseId)
      }
    },
    
    // 加载成绩数据
    async loadGradeData(courseId) {
      this.loading = true
      try {
        const response = await this.$axios.get(`/teacher/data/classes/${courseId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          // 将课程学生数据转换为成绩数据格式
          this.gradeData = response.data.data.map(student => ({
            id: student.id,
            studentId: student.studentId,
            studentName: student.name,
            className: student.className,
            dailyScore: student.dailyScore || null,
            midtermScore: student.midtermScore || null,
            finalScore: student.finalScore || null,
            experimentScore: student.experimentScore || null,
            totalScore: student.totalScore || null,
            score: student.score || null, // 使用通用score字段
            grade: student.grade || null,
            feedback: student.comment || student.feedback || '',
            examName: student.examName || '课程成绩',
            semester: student.semester || '未知学期'
          }))
        } else {
          console.error('获取成绩数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取成绩数据失败')
          this.gradeData = []
        }
        
        // 计算成绩统计
        this.calculateGradeStats()
        
        // 初始化成绩分布图
        this.$nextTick(() => {
          this.initGradeDistributionChart()
        })
        
        console.log('成绩数据加载完成')
      } catch (error) {
        console.error('加载成绩数据失败:', error)
        this.$message.error('加载成绩数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    

    
    // 计算成绩统计
    calculateGradeStats() {
      const validScores = this.gradeData
        .map(item => item.totalScore)
        .filter(score => score !== null && !isNaN(score))
      
      if (validScores.length === 0) {
        this.gradeStats = {
          average: 0,
          max: 0,
          min: 0,
          passRate: 0,
          excellentRate: 0,
          stdDev: 0,
          median: 0,
          skewness: 0,
          kurtosis: 0
        }
        return
      }
      
      // 计算平均分
      const average = validScores.reduce((sum, score) => sum + score, 0) / validScores.length
      
      // 计算最高分
      const max = Math.max(...validScores)
      
      // 计算最低分
      const min = Math.min(...validScores)
      
      // 计算及格率
      const passCount = validScores.filter(score => score >= this.gradeSettingsForm.passThreshold).length
      const passRate = (passCount / validScores.length) * 100
      
      // 计算优秀率
      const excellentCount = validScores.filter(score => score >= this.gradeSettingsForm.excellentThreshold).length
      const excellentRate = (excellentCount / validScores.length) * 100
      
      // 计算标准差
      const squaredDiffs = validScores.map(score => Math.pow(score - average, 2))
      const variance = squaredDiffs.reduce((sum, diff) => sum + diff, 0) / validScores.length
      const stdDev = Math.sqrt(variance)
      
      // 计算中位数
      const sortedScores = [...validScores].sort((a, b) => a - b)
      const medianIndex = Math.floor(sortedScores.length / 2)
      const median = sortedScores.length % 2 === 0
        ? (sortedScores[medianIndex - 1] + sortedScores[medianIndex]) / 2
        : sortedScores[medianIndex]
      
      // 计算偏度
      const cubedDiffs = validScores.map(score => Math.pow((score - average) / stdDev, 3))
      const skewness = cubedDiffs.reduce((sum, diff) => sum + diff, 0) / validScores.length
      
      // 计算峰度
      const fourthPowerDiffs = validScores.map(score => Math.pow((score - average) / stdDev, 4))
      const kurtosis = fourthPowerDiffs.reduce((sum, diff) => sum + diff, 0) / validScores.length - 3
      
      this.gradeStats = {
        average,
        max,
        min,
        passRate,
        excellentRate,
        stdDev,
        median: Math.round(median),
        skewness,
        kurtosis
      }
      
      // 计算分数段分布
      this.calculateGradeSegments(validScores)
    },
    
    // 计算分数段分布
    calculateGradeSegments(scores) {
      const totalCount = scores.length
      
      this.gradeSegments = [
        {
          label: '90-100分',
          count: scores.filter(s => s >= 90).length,
          color: '#67c23a'
        },
        {
          label: '80-89分',
          count: scores.filter(s => s >= 80 && s < 90).length,
          color: '#409eff'
        },
        {
          label: '70-79分',
          count: scores.filter(s => s >= 70 && s < 80).length,
          color: '#e6a23c'
        },
        {
          label: '60-69分',
          count: scores.filter(s => s >= 60 && s < 70).length,
          color: '#f56c6c'
        },
        {
          label: '0-59分',
          count: scores.filter(s => s < 60).length,
          color: '#909399'
        }
      ]
      
      // 计算百分比
      this.gradeSegments.forEach(segment => {
        segment.percentage = totalCount > 0 ? (segment.count / totalCount) * 100 : 0
      })
    },
    
    // 初始化成绩分布图
    initGradeDistributionChart() {
      const ctx = document.getElementById('gradeSegmentChart')
      if (!ctx) return
      
      // 销毁旧的Chart实例
      if (this.gradeChart) {
        this.gradeChart.destroy()
      }
      
      // 准备图表数据
      const labels = this.gradeSegments.map(segment => segment.label)
      const data = this.gradeSegments.map(segment => segment.count)
      const backgroundColors = this.gradeSegments.map(segment => segment.color)
      
      // 创建Chart实例
      this.gradeChart = new window.Chart(ctx, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: '学生人数',
            data: data,
            backgroundColor: backgroundColors,
            borderWidth: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0
              }
            }
          },
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  const segment = this.chart.data.datasets[0];
                  const value = context.raw;
                  const percentage = ((value / context.chart.data.datasets[0].data.reduce((a, b) => a + b, 0)) * 100).toFixed(1);
                  return `${value}人 (${percentage}%)`;
                }
              }
            }
          }
        }
      })
    },
    
    // 开始编辑成绩
    startEditing(row) {
      this.editingRowId = row.id
      this.editForm = {
        dailyScore: row.dailyScore,
        midtermScore: row.midtermScore,
        finalScore: row.finalScore,
        experimentScore: row.experimentScore
      }
    },
    
    // 处理分数变化
    handleScoreChange() {
      // 实时计算总成绩
      const row = this.gradeData.find(r => r.id === this.editingRowId)
      if (row) {
        const dailyScore = this.editForm.dailyScore || 0
        const midtermScore = this.editForm.midtermScore || 0
        const finalScore = this.editForm.finalScore || 0
        const experimentScore = this.editForm.experimentScore || 0
        
        row.totalScore = Math.round(
          dailyScore * this.gradeSettingsForm.dailyWeight / 100 +
          midtermScore * this.gradeSettingsForm.midtermWeight / 100 +
          finalScore * this.gradeSettingsForm.finalWeight / 100 +
          experimentScore * this.gradeSettingsForm.experimentWeight / 100
        )
      }
    },
    
    // 保存成绩
    saveScore() {
      const row = this.gradeData.find(r => r.id === this.editingRowId)
      if (row) {
        row.dailyScore = this.editForm.dailyScore
        row.midtermScore = this.editForm.midtermScore
        row.finalScore = this.editForm.finalScore
        row.experimentScore = this.editForm.experimentScore
        
        // 实际项目中应该调用后端API保存成绩
        // axios.put(`/grades/${row.id}`, row)
        
        this.$message.success('成绩保存成功')
        this.cancelEdit()
        this.calculateGradeStats()
      }
    },
    
    // 取消编辑
    cancelEdit() {
      this.editingRowId = null
      this.editForm = {
        dailyScore: null,
        midtermScore: null,
        finalScore: null,
        experimentScore: null
      }
    },
    
    // 查看成绩详情
    viewScoreDetails(row) {
      // 这里可以添加查看成绩详情的逻辑，比如打开详情对话框
      this.$message.info(`查看${row.studentName}的成绩详情`)
    },
    
    // 更新备注
    updateRemark(row) {
      // 实际项目中应该调用后端API更新备注
      // axios.put(`/grades/${row.id}/remark`, { remark: row.remark })
    },
    
    // 清空学生成绩
    clearStudentScore(row) {
      this.$confirm(`确定要清空 ${row.studentName} 的所有成绩吗？`, '确认清空', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        row.dailyScore = null
        row.midtermScore = null
        row.finalScore = null
        row.experimentScore = null
        row.totalScore = null
        
        // 实际项目中应该调用后端API清空成绩
        // axios.delete(`/grades/${row.id}`)
        
        this.$message.success('成绩已清空')
        this.calculateGradeStats()
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 批量编辑成绩
    batchEditGrades() {
      if (this.selectedCourse) {
        this.batchEditForm = {
          editType: 'set',
          scoreType: 'dailyScore',
          value: 0,
          scope: this.selectedRows.length > 0 ? 'selected' : 'all',
          conditionType: 'gt',
          conditionValue: 0,
          conditionMin: 0,
          conditionMax: 0
        }
        this.showBatchEditDialog = true
      }
    },
    
    // 确认批量编辑
    confirmBatchEdit() {
      this.$refs.batchEditForm.validate((valid) => {
        if (valid) {
          // 获取要编辑的学生
          let studentsToEdit = [...this.gradeData]
          
          // 按范围筛选
          if (this.batchEditForm.scope === 'selected') {
            studentsToEdit = this.selectedRows
          } else if (this.batchEditForm.scope === 'condition') {
            studentsToEdit = this.gradeData.filter(row => {
              const score = row[this.batchEditForm.scoreType]
              if (this.batchEditForm.conditionType === 'null') {
                return score === null || score === undefined
              } else if (this.batchEditForm.conditionType === 'between') {
                return score >= this.batchEditForm.conditionMin && score <= this.batchEditForm.conditionMax
              } else if (this.batchEditForm.conditionType === 'gt') {
                return score > this.batchEditForm.conditionValue
              } else if (this.batchEditForm.conditionType === 'lt') {
                return score < this.batchEditForm.conditionValue
              } else if (this.batchEditForm.conditionType === 'eq') {
                return score === this.batchEditForm.conditionValue
              }
              return true
            })
          }
          
          // 执行批量编辑
          studentsToEdit.forEach(student => {
            let currentScore = student[this.batchEditForm.scoreType] || 0
            let newScore = currentScore
            
            switch (this.batchEditForm.editType) {
              case 'set':
                newScore = this.batchEditForm.value
                break
              case 'add':
                newScore = currentScore + this.batchEditForm.value
                break
              case 'subtract':
                newScore = currentScore - this.batchEditForm.value
                break
              case 'multiply':
                newScore = currentScore * this.batchEditForm.value
                break
            }
            
            // 确保分数在0-100范围内
            student[this.batchEditForm.scoreType] = Math.max(0, Math.min(100, Math.round(newScore)))
          })
          
          // 重新计算总成绩
          this.recalculateAllTotalScores()
          
          // 实际项目中应该调用后端API批量更新成绩
          // axios.put('/grades/batch', { students: studentsToEdit })
          
          this.$message.success(`成功更新 ${studentsToEdit.length} 名学生的成绩`)
          this.showBatchEditDialog = false
          this.calculateGradeStats()
        }
      })
    },
    
    // 重新计算所有学生的总成绩
    recalculateAllTotalScores() {
      this.gradeData.forEach(student => {
        const dailyScore = student.dailyScore || 0
        const midtermScore = student.midtermScore || 0
        const finalScore = student.finalScore || 0
        const experimentScore = student.experimentScore || 0
        
        student.totalScore = Math.round(
          dailyScore * this.gradeSettingsForm.dailyWeight / 100 +
          midtermScore * this.gradeSettingsForm.midtermWeight / 100 +
          finalScore * this.gradeSettingsForm.finalWeight / 100 +
          experimentScore * this.gradeSettingsForm.experimentWeight / 100
        )
      })
    },
    
    // 计算总成绩
    calculateFinalGrades() {
      this.$confirm('确定要重新计算所有学生的总成绩吗？', '确认计算', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.recalculateAllTotalScores()
        
        // 实际项目中应该调用后端API
        // axios.post(`/courses/${this.selectedCourse}/grades/calculate`)
        
        this.$message.success('总成绩计算完成')
        this.calculateGradeStats()
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 生成成绩单
    generateGradeReport() {
      // 实际项目中应该调用后端API生成并下载成绩单
      this.$message.success('成绩单生成成功，正在下载...')
    },
    
    // 清空成绩
    clearGrades() {
      this.$confirm('确定要清空所有学生的成绩吗？此操作不可恢复！', '确认清空', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        this.gradeData.forEach(student => {
          student.dailyScore = null
          student.midtermScore = null
          student.finalScore = null
          student.experimentScore = null
          student.totalScore = null
        })
        
        // 实际项目中应该调用后端API
        // axios.delete(`/courses/${this.selectedCourse}/grades`)
        
        this.$message.success('所有成绩已清空')
        this.calculateGradeStats()
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 保存成绩设置
    saveGradeSettings() {
      this.$refs.gradeSettingsForm.validate((valid) => {
        if (valid) {
          // 检查权重总和
          const totalWeight = this.gradeSettingsForm.dailyWeight + 
                            this.gradeSettingsForm.midtermWeight + 
                            this.gradeSettingsForm.finalWeight + 
                            this.gradeSettingsForm.experimentWeight
          
          if (totalWeight !== 100) {
            this.$message.error('总权重必须等于100%')
            return
          }
          
          // 保存设置
          // 实际项目中应该调用后端API
          // axios.put(`/courses/${this.selectedCourse}/grade-settings`, this.gradeSettingsForm)
          
          // 重新计算总成绩
          this.recalculateAllTotalScores()
          
          this.$message.success('成绩设置保存成功')
          this.showGradeSettings = false
          this.calculateGradeStats()
        }
      })
    },
    
    // 导出成绩
    exportGrades() {
      // 实际项目中应该调用后端API导出成绩
      this.$message.success('成绩导出成功，正在下载...')
    },
    
    // 导入成绩
    handleImportGrades(file) {
      // 实际项目中应该处理文件上传和数据导入
      this.$message.success('成绩导入成功')
      this.loadGradeData(this.selectedCourse)
    },
    
    // 刷新成绩列表
    refreshGradeList() {
      this.studentSearchQuery = ''
      this.currentPage = 1
      this.loadGradeData(this.selectedCourse)
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    
    // 关闭批量编辑对话框
    handleBatchEditClose() {
      this.showBatchEditDialog = false
      this.$refs.batchEditForm && this.$refs.batchEditForm.resetFields()
    },
    
    // 关闭设置对话框
    handleSettingsClose() {
      this.showGradeSettings = false
      this.$refs.gradeSettingsForm && this.$refs.gradeSettingsForm.resetFields()
    },
    
    // 计算等级
    calculateGradeLevel(score) {
      if (score === null || score === undefined || isNaN(score)) {
        return '-'
      }
      
      if (score >= this.gradeSettingsForm.excellentThreshold) {
        return '优秀'
      } else if (score >= this.gradeSettingsForm.goodThreshold) {
        return '良好'
      } else if (score >= this.gradeSettingsForm.passThreshold) {
        return '及格'
      } else {
        return '不及格'
      }
    },
    
    // 获取等级类型
    getGradeLevelType(score) {
      if (score === null || score === undefined || isNaN(score)) {
        return 'info'
      }
      
      if (score >= this.gradeSettingsForm.excellentThreshold) {
        return 'success'
      } else if (score >= this.gradeSettingsForm.goodThreshold) {
        return 'primary'
      } else if (score >= this.gradeSettingsForm.passThreshold) {
        return 'warning'
      } else {
        return 'danger'
      }
    },
    
    // 获取分数等级样式类
    getScoreLevelClass(score) {
      if (score === null || score === undefined || isNaN(score)) {
        return 'score-default'
      }
      
      if (score >= this.gradeSettingsForm.excellentThreshold) {
        return 'score-excellent'
      } else if (score >= this.gradeSettingsForm.goodThreshold) {
        return 'score-good'
      } else if (score >= this.gradeSettingsForm.passThreshold) {
        return 'score-pass'
      } else {
        return 'score-fail'
      }
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
.grade-management-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.grade-management-card {
  margin-bottom: 20px;
}

/* 成绩统计概览 */
.grade-stats-overview {
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

/* 成绩操作区域 */
.grade-action-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.grade-settings {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

/* 表格样式 */
.score-cell {
  cursor: pointer;
  color: #409eff;
}

.score-cell:hover {
  color: #66b1ff;
}

.score-editor {
  display: flex;
  justify-content: center;
}

.total-score {
  font-weight: bold;
}

.score-excellent {
  color: #67c23a;
}

.score-good {
  color: #409eff;
}

.score-pass {
  color: #e6a23c;
}

.score-fail {
  color: #f56c6c;
}

.score-default {
  color: #909399;
}

/* 批量编辑对话框 */
.batch-edit-content {
  max-height: 400px;
  overflow-y: auto;
}

.condition-values {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

/* 设置对话框 */
.settings-content {
  max-height: 400px;
  overflow-y: auto;
}

.weight-value {
  text-align: center;
  font-weight: bold;
  color: #409eff;
  margin-top: 5px;
}

.weight-sum {
  text-align: center;
  font-weight: bold;
  margin: 10px 0;
}

.weight-error {
  text-align: center;
  color: #f56c6c;
  font-size: 14px;
}

/* 成绩分布图 */
.grade-chart-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.grade-stats-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-row .stat-label {
  font-weight: bold;
  color: #606266;
}

.stat-row .stat-value {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.grade-segment-stats {
  margin-top: 20px;
}

.segment-item {
  margin-bottom: 15px;
}

.segment-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.segment-bar {
  height: 10px;
  background-color: #ebeef5;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 5px;
}

.segment-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s ease;
}

.segment-count {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .grade-stats-overview .el-col {
    width: 50%;
    margin-bottom: 15px;
  }
  
  .search-filter-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .search-filter-section .el-col:last-child {
    margin-bottom: 0;
  }
  
  .grade-action-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .grade-action-section .el-col:last-child {
    margin-bottom: 0;
  }
  
  .grade-settings {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .grade-management-container {
    padding: 10px;
  }
  
  .grade-stats-overview .el-col {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .grade-stats-detail {
    grid-template-columns: 1fr;
  }
}
</style>