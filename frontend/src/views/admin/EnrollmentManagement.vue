<template>
  <div class="enrollment-management">
    <el-card class="management-header">
      <h1>选课管理</h1>
    </el-card>
    
    <el-card class="management-content">
    
    <div class="content-wrapper">
      <!-- 筛选和搜索区域 -->
      <div class="search-filters mb-15">
        <el-select v-model="filterOptions.semesterId" placeholder="选择学期" clearable style="width: 120px; margin-right: 12px;">
              <el-option 
                v-for="semester in semesters" 
                :key="semester.id" 
                :label="semester.name" 
                :value="semester.id" 
              />
            </el-select>
            
            <el-select v-model="filterOptions.courseId" placeholder="选择课程" clearable style="width: 120px; margin-right: 12px;">
              <el-option 
                v-for="course in courses" 
                :key="course.id" 
                :label="course.name" 
                :value="course.id" 
              />
            </el-select>
            
            <el-select v-model="filterOptions.major" placeholder="按专业查询" clearable style="width: 150px; margin-right: 12px;">
              <el-option 
                v-for="major in majors" 
                :key="major" 
                :label="major" 
                :value="major" 
              />
            </el-select>
            
            <el-input 
              v-model="filterOptions.studentName" 
              placeholder="输入学生姓名" 
              prefix-icon="el-icon-user" 
              style="width: 150px; margin-right: 12px;"
              @keyup.enter="searchEnrollments"
            />
            
            <el-input 
              v-model="filterOptions.studentId" 
              placeholder="输入学生学号" 
              prefix-icon="el-icon-document" 
              style="width: 150px; margin-right: 12px;"
              @keyup.enter="searchEnrollments"
            />
            
            <el-input 
              v-model="filterOptions.teacherName" 
              placeholder="输入教师姓名" 
              prefix-icon="el-icon-user-solid" 
              style="width: 150px; margin-right: 12px;"
              @keyup.enter="searchEnrollments"
            />
            
            <el-button type="primary" @click="searchEnrollments" style="margin-right: 12px;">
              <i class="el-icon-search"></i> 搜索
            </el-button>
            <el-button @click="resetFilters">
              <i class="el-icon-refresh"></i> 重置
            </el-button>
      </div>
      
      <!-- 功能按钮区域 -->
      <div class="action-section mb-15">
        <el-button type="primary" @click="addEnrollment">
          <i class="el-icon-plus"></i> 新增选课
        </el-button>
        <el-button type="success" @click="batchImport" style="margin-left: 10px;">
          <i class="el-icon-upload2"></i> 批量导入
        </el-button>
        <el-button type="info" @click="batchExport" style="margin-left: 10px;">
          <i class="el-icon-download"></i> 批量导出
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedEnrollments.length === 0" style="margin-left: 10px;">
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedEnrollments.length }})
        </el-button>
        <el-divider direction="vertical" style="margin: 0 10px;"></el-divider>
        <el-button type="info" @click="showStatistics">
          <i class="el-icon-data-line"></i> 选课统计
        </el-button>
      </div>
      
      <!-- 选课列表表格 -->
      <div class="table-section mb-15">
        <div class="table-container">
        <el-table 
          v-loading="loading" 
          :data="paginatedEnrollments" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
          class="hover-card"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="选课ID" width="100" />
          <el-table-column label="学生信息" min-width="160">
            <template #default="scope">
              <div>
                <div><strong>姓名:</strong> {{ scope.row.studentName }}</div>
                <div><strong>学号:</strong> {{ scope.row.studentIdValue }}</div>
                <div><strong>专业:</strong> {{ scope.row.major || '未设置' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="课程信息" min-width="160">
            <template #default="scope">
              <div>
                <div><strong>名称:</strong> {{ scope.row.courseName }}</div>
                <div><strong>编号:</strong> {{ scope.row.courseCode }}</div>
                <div><strong>学分:</strong> {{ scope.row.credits || '未设置' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="教师" width="100" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.teacherName }}
            </template>
          </el-table-column>
          <el-table-column label="学期" width="150">
            <template #default="scope">
              {{ scope.row.semesterName }}
            </template>
          </el-table-column>
          <el-table-column label="选课时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.enrollDate) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getEnrollmentStatusTag(scope.row.status)">
                {{ getEnrollmentStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="成绩" width="80" v-if="showGrades">
            <template #default="scope">
              <span v-if="scope.row.score !== null">{{ scope.row.score }}</span>
              <span v-else class="text-muted">--</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="420" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="editEnrollment(scope.row)"
                :disabled="scope.row.status === 'COMPLETED'"
                icon="el-icon-edit"
                style="margin-left: 10px;"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteEnrollment(scope.row)"
                :disabled="scope.row.status === 'COMPLETED'"
                icon="el-icon-delete"
                style="margin-left: 10px;"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>
      </div>
      
      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredEnrollments.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 添加/编辑选课对话框 -->
    <el-dialog 
      :title="isEditMode ? '编辑选课' : '添加选课'" 
      v-model="dialogVisible" 
      width="60%"
      class="custom-dialog"
    >
      <el-form 
        ref="enrollmentForm" 
        :model="enrollmentForm" 
        :rules="enrollmentRules" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学期" prop="semesterId">
              <el-select v-model="enrollmentForm.semesterId" placeholder="选择学期" style="width: 100%;">
                <el-option 
                  v-for="semester in semesters" 
                  :key="semester.id" 
                  :label="semester.name" 
                  :value="semester.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="enrollmentForm.courseId" placeholder="选择课程" style="width: 100%;">
                <el-option 
                  v-for="course in courses" 
                  :key="course.id" 
                  :label="`${course.name} (${course.code})`" 
                  :value="course.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生" prop="studentId">
              <el-select v-model="enrollmentForm.studentId" placeholder="选择学生" style="width: 100%;">
                <el-option 
                  v-for="student in students" 
                  :key="student.id" 
                  :label="`${student.name} (${student.studentId})`" 
                  :value="student.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选课状态" prop="status">
              <el-select v-model="enrollmentForm.status" placeholder="选择状态" style="width: 100%;">
                <el-option label="正常" value="NORMAL" />
                <el-option label="已退课" value="WITHDRAWN" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已挂科" value="FAILED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选课时间" prop="enrollDate">
              <el-date-picker
                v-model="enrollmentForm.enrollDate"
                type="datetime"
                placeholder="选择选课时间"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成绩" prop="score">
              <el-input 
                v-model.number="enrollmentForm.score" 
                type="number" 
                placeholder="输入成绩（可选）" 
                :min="0" 
                :max="100"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input 
                v-model="enrollmentForm.remark" 
                type="textarea" 
                :rows="3" 
                placeholder="输入备注信息（可选）" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="dialogVisible = false" size="small">取消</el-button>
          <el-button type="primary" @click="submitEnrollmentForm" size="small" style="margin-left: 10px;">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入选课数据" 
      v-model="importDialogVisible" 
      width="60%"
      class="custom-dialog"
    >
      <div class="import-section">
        <div class="import-info">
          <h4>导入说明：</h4>
          <ol>
            <li>请下载导入模板，按照模板格式填写选课数据</li>
            <li>确保学生ID和课程ID在系统中已存在</li>
            <li>导入文件支持 .xlsx 和 .xls 格式</li>
            <li>单次导入最多支持1000条数据</li>
          </ol>
        </div>
        
        <div class="template-download">
          <el-button type="primary" @click="downloadTemplate" size="small">
            <i class="el-icon-download"></i> 下载导入模板
          </el-button>
        </div>
        
        <el-upload
          class="upload-demo"
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="importFileList"
          accept=".xlsx,.xls"
          :limit="1"
        >
          <el-button size="small" type="primary">选择文件</el-button>
          <div class="el-upload__tip">
            请选择 .xlsx 或 .xls 格式的文件
          </div>
        </el-upload>
        
        <div class="import-action" v-if="importFileList.length > 0">
          <el-button type="primary" @click="confirmImport" size="small">
            <i class="el-icon-upload2"></i> 确认导入
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="importDialogVisible = false" size="small">取消</el-button>
          <el-button type="primary" @click="confirmImport" size="small" style="margin-left: 10px;">确认导入</el-button>
        </div>
      </template>
    </el-dialog>
    </el-card>
    
    <!-- 选课统计对话框 -->
    <el-dialog 
      title="选课统计分析" 
      v-model="statisticsDialogVisible" 
      width="80%"
      :fullscreen="true"
      class="custom-dialog"
    >
      <div class="statistics-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20" style="margin-bottom: 30px;">
          <el-col :span="6">
            <el-card class="statistics-card">
              <div class="card-content">
                <div class="card-value">{{ totalEnrollments }}</div>
                <div class="card-label">总选课数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="statistics-card">
              <div class="card-content">
                <div class="card-value">{{ activeEnrollments }}</div>
                <div class="card-label">有效选课数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="statistics-card">
              <div class="card-content">
                <div class="card-value">{{ withdrawnEnrollments }}</div>
                <div class="card-label">已退课数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="statistics-card">
              <div class="card-content">
                <div class="card-value">{{ completedEnrollments }}</div>
                <div class="card-label">已完成课程数</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 图表区域 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>各课程选课人数统计</span>
                  <el-select v-model="chartOptions.courseFilter" placeholder="筛选课程" style="width: 200px; float: right;">
                    <el-option label="全部课程" value="all" />
                    <el-option 
                      v-for="course in courses" 
                      :key="course.id" 
                      :label="course.name" 
                      :value="course.id" 
                    />
                  </el-select>
                </div>
              </template>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示选课统计图表" />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>各系部选课分布</span>
                  <el-select v-model="chartOptions.departmentFilter" placeholder="筛选系部" style="width: 200px; float: right;">
                    <el-option label="全部系部" value="all" />
                    <el-option 
                      v-for="dept in departments" 
                      :key="dept.id" 
                      :label="dept.name" 
                      :value="dept.id" 
                    />
                  </el-select>
                </div>
              </template>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示系部分布图表" />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>各年级选课情况</span>
                </div>
              </template>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示年级选课图表" />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>选课趋势分析</span>
                  <el-select v-model="chartOptions.trendRange" placeholder="时间范围" style="width: 200px; float: right;">
                    <el-option label="近7天" value="7" />
                    <el-option label="近30天" value="30" />
                    <el-option label="近90天" value="90" />
                    <el-option label="本学期" value="current" />
                  </el-select>
                </div>
              </template>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示选课趋势图表" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <template #footer>
        <div style="text-align: right;">
          <el-button @click="statisticsDialogVisible = false" size="small">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'EnrollmentManagement',
  data() {
    return {
      // 加载状态
      loading: false,
      
      // 筛选条件
      filterOptions: {
        semesterId: '',
        courseId: '',
        major: '',
        studentName: '',
        studentId: '',
        teacherName: ''
      },
      
      // 数据列表
      enrollments: [],
      filteredEnrollments: [],
      
      // 分页相关
      currentPage: 1,
      pageSize: 20,
      
      // 选中的记录
      selectedEnrollments: [],
      
      // 对话框相关
      dialogVisible: false,
      isEditMode: false,
      
      // 表单数据
      enrollmentForm: {
        id: '',
        semesterId: '',
        courseId: '',
        studentId: '',
        status: 'NORMAL',
        enrollDate: new Date(),
        score: null,
        remark: ''
      },
      enrollmentRules: {
        semesterId: [
          { required: true, message: '请选择学期', trigger: 'change' }
        ],
        courseId: [
          { required: true, message: '请选择课程', trigger: 'change' }
        ],
        studentId: [
          { required: true, message: '请选择学生', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ],
        enrollDate: [
          { required: true, message: '请选择选课时间', trigger: 'change' }
        ]
      },
      
      // 批量导入相关
      importDialogVisible: false,
      importFileList: [],
      
      // 统计对话框相关
      statisticsDialogVisible: false,
      chartOptions: {
        courseFilter: 'all',
        departmentFilter: 'all',
        trendRange: '30'
      },
      
      // 显示成绩列
      showGrades: true,
      
      // 辅助数据
      semesters: [],
      courses: [],
      students: [],
      departments: [],
      grades: [],
      majors: []
    }
  },
  computed: {
    // 分页后的数据
    paginatedEnrollments() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredEnrollments.slice(start, end)
    },
    
    // 统计数据
    totalEnrollments() {
      return this.enrollments.length
    },
    activeEnrollments() {
      return this.enrollments.filter(e => e.status === 'NORMAL').length
    },
    withdrawnEnrollments() {
      return this.enrollments.filter(e => e.status === 'WITHDRAWN').length
    },
    completedEnrollments() {
      return this.enrollments.filter(e => e.status === 'COMPLETED').length
    }
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    // 加载初始数据
    async loadInitialData() {
      this.loading = true
      try {
        // 按顺序加载数据，因为某些数据可能依赖其他数据
        await this.loadSemesters();
        await this.loadDepartments();
        await this.loadStudents();
        await this.loadCourses();
        await this.loadGrades();
        await this.loadEnrollments();
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载学期数据
    async loadSemesters() {
      const response = await this.$axios.get('/api/semesters');
      this.semesters = response.data.data;
    },
    
    // 加载课程数据
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/courses');
        // 确保课程数据格式正确
        this.courses = Array.isArray(response.data.data) ? response.data.data : [];
      } catch (error) {
        console.error('加载课程数据失败:', error);
        this.courses = [];
        this.$message.error('加载课程数据失败');
      }
    },
    
    // 加载学生数据
    async loadStudents() {
      try {
        const response = await this.$axios.get('/api/students');
        // 确保学生数据格式正确，并确保每个学生都有studentId字段
        let studentData = Array.isArray(response.data.data) ? response.data.data : [];
        
        // 确保每个学生对象都有studentId字段，优先级：studentId > student_id > 学号相关字段
        studentData = studentData.map(student => {
          // 检查可能的学号字段名
          const studentId = student.studentId || student.student_id || student.studentNumber || student.no || student.number;
          return {
            ...student,
            // 如果找到了学号字段就使用它，否则保持原样
            studentId: studentId
          };
        });
        
        this.students = studentData;
        // 初始化专业列表
        this.updateMajorsList();
      } catch (error) {
        console.error('加载学生数据失败:', error);
        this.students = [];
        this.$message.error('加载学生数据失败');
      }
    },
    
    // 更新专业列表
    updateMajorsList() {
      // 从学生数据中提取唯一的专业列表
      const uniqueMajors = [...new Set(this.students.map(student => student.major).filter(major => major))];
      this.majors = uniqueMajors;
    },
    
    // 加载系部数据
    async loadDepartments() {
      try {
        const response = await this.$axios.get('/api/departments');
        // 确保系部数据格式正确
        this.departments = Array.isArray(response.data.data) ? response.data.data : [];
      } catch (error) {
        console.error('加载系部数据失败:', error);
        this.departments = [];
        this.$message.error('加载系部数据失败');
      }
    },
    
    // 加载年级数据
    async loadGrades() {
      try {
        const response = await this.$axios.get('/grades');
        // 确保年级数据格式正确
        this.grades = Array.isArray(response.data.data) ? response.data.data : [];
      } catch (error) {
        console.error('加载年级数据失败:', error);
        this.grades = [];
        this.$message.error('加载年级数据失败');
      }
    },
    
    // 加载选课数据
    async loadEnrollments() {
      try {
        // 只传递后端支持的过滤参数
        const params = {};
        if (this.filterOptions.semesterId) params.semesterId = this.filterOptions.semesterId;
        if (this.filterOptions.courseId) params.courseId = this.filterOptions.courseId;
        if (this.filterOptions.studentId) params.studentId = this.filterOptions.studentId;
        
        const response = await this.$axios.get('/api/enrollments', { params });
        // 将学生信息合并到选课记录中
        this.enrollments = response.data.data.map(enrollment => {
          const student = this.students.find(s => s.id === enrollment.studentId);
          return {
            ...enrollment,
            studentName: student?.name || enrollment.studentName,
            studentIdValue: student?.studentId || student?.student_id || student?.id,
            major: student?.major || enrollment.major
          };
        });
        
        // 确保专业列表已更新
        this.updateMajorsList();
        
        // 在前端进行进一步筛选
        this.searchEnrollments();
      } catch (error) {
        console.error('加载选课数据失败:', error);
        this.$message.error('加载选课数据失败，请稍后重试');
      }
    },
    
    // 刷新数据
    refreshData() {
      this.loadEnrollments()
      this.selectedEnrollments = []
    },
    
    // 搜索选课数据
    searchEnrollments() {
      this.currentPage = 1
      
      this.filteredEnrollments = this.enrollments.filter(enrollment => {
        // 按学期筛选
        if (this.filterOptions.semesterId && enrollment.semesterId !== parseInt(this.filterOptions.semesterId)) {
          return false
        }
        
        // 按课程筛选
        if (this.filterOptions.courseId && enrollment.courseId !== parseInt(this.filterOptions.courseId)) {
          return false
        }
        
        // 按专业筛选
        if (this.filterOptions.major) {
          if (enrollment.major !== this.filterOptions.major) {
            return false
          }
        }
        
        // 按学生姓名筛选
        if (this.filterOptions.studentName) {
          const keyword = this.filterOptions.studentName.toLowerCase()
          if (!enrollment.studentName.toLowerCase().includes(keyword)) {
            return false
          }
        }
        
        // 按学生学号筛选
        if (this.filterOptions.studentId) {
          const keyword = this.filterOptions.studentId.toLowerCase()
          // 通过关联的学生对象查找学号
          const student = this.students.find(s => s.id === enrollment.studentId)
          if (!student || !student.studentId.toLowerCase().includes(keyword)) {
            return false
          }
        }
        
        // 按教师姓名筛选
        if (this.filterOptions.teacherName) {
          const keyword = this.filterOptions.teacherName.toLowerCase()
          if (!enrollment.teacherName.toLowerCase().includes(keyword)) {
            return false
          }
        }
        
        return true
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.filterOptions = {
        semesterId: '',
        courseId: '',
        major: '',
        studentName: '',
        studentId: '',
        teacherName: ''
      }
      this.filteredEnrollments = [...this.enrollments]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedEnrollments = selection
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.currentPage = 1
    },
    
    handleCurrentChange(newCurrent) {
      this.currentPage = newCurrent
    },
    
    // 编辑选课记录
    editEnrollment(enrollment) {
      this.isEditMode = true
      // 复制选课对象并处理日期格式
      this.enrollmentForm = { 
        ...enrollment,
        // 如果enrollDate是字符串，则转换为Date对象用于日期选择器
        enrollDate: enrollment.enrollDate ? (typeof enrollment.enrollDate === 'string' || typeof enrollment.enrollDate === 'number' ? new Date(enrollment.enrollDate) : enrollment.enrollDate) : new Date()
      }
      this.dialogVisible = true
    },
    
    // 添加选课记录
    addEnrollment() {
      this.isEditMode = false
      this.enrollmentForm = {
        id: '',
        semesterId: '',
        courseId: '',
        studentId: '',
        status: 'NORMAL',
        enrollDate: new Date(),
        score: null,
        remark: ''
      }
      this.dialogVisible = true
    },
    
    // 提交选课表单
    submitEnrollmentForm() {
      this.$refs.enrollmentForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          // 复制表单数据并格式化日期
          const formData = { ...this.enrollmentForm }
          
          // 格式化日期为ISO字符串格式，如果日期存在
          if (formData.enrollDate instanceof Date) {
            formData.enrollDate = formData.enrollDate.toISOString();
          }
          
          // 检查是否已存在相同的选课记录
          const exists = this.enrollments.some(e => 
            e.courseId === formData.courseId && 
            e.studentId === formData.studentId && 
            e.semesterId === formData.semesterId &&
            (!this.isEditMode || e.id !== formData.id)
          )
          
          if (exists) {
            this.$message.error('该学生在此学期已选修此课程')
            return
          }
          
          if (this.isEditMode) {
            // 更新选课记录
            await this.$axios.put(`/api/enrollments/${formData.id}`, formData);
          } else {
            // 创建选课记录
            await this.$axios.post('/api/enrollments', formData);
          }
          // 重新加载数据以获取完整信息
          await this.loadEnrollments();
          
          this.dialogVisible = false
          this.searchEnrollments()
          this.$message.success(this.isEditMode ? '编辑成功' : '添加成功')
        } catch (error) {
          console.error('保存选课记录失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 删除选课记录
    deleteEnrollment(enrollment) {
      this.$confirm(`确定要删除学生"${enrollment.studentName || '未知'}"的课程"${enrollment.courseName || '未知'}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API删除记录
          await this.$axios.delete(`/api/enrollments/${enrollment.id}`);
          
          // 重新加载数据
          await this.loadEnrollments();
          this.$message.success('删除成功')
        } catch (error) {
          console.error('删除选课记录失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量删除选课记录
    batchDelete() {
      if (this.selectedEnrollments.length === 0) {
        this.$message.warning('请选择要删除的记录')
        return
      }
      
      const completedRecords = this.selectedEnrollments.filter(e => e.status === 'COMPLETED')
      if (completedRecords.length > 0) {
        this.$message.warning('已完成的选课记录不能删除')
        return
      }
      
      this.$confirm(`确定要删除选中的${this.selectedEnrollments.length}条记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API批量删除
          const ids = this.selectedEnrollments.map(e => e.id)
          await this.$axios.delete('/api/enrollments/batch', { data: { enrollmentIds: ids } });
          
          // 重新加载数据
          await this.loadEnrollments();
          this.selectedEnrollments = []
          this.$message.success('批量删除成功')
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量导入
    batchImport() {
      this.importDialogVisible = true
      this.importFileList = []
    },
    
    // 下载导入模板
    downloadTemplate() {
      this.$message.info('下载功能开发中');
    },
    
    // 处理文件上传
    handleFileChange(file) {
      this.importFileList = [file]
    },
    
    // 确认导入
    async confirmImport() {
      if (this.importFileList.length === 0) {
        this.$message.warning('请先选择要导入的文件')
        return
      }
      
      // 显示加载状态
      const loading = this.$loading({
        lock: true,
        text: '正在导入数据，请稍候...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      
      try {
          // 调用后端API导入数据
          const formData = new FormData();
          formData.append('file', this.importFileList[0].raw);
          const response = await this.$axios.post('/api/enrollments/import', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          
          loading.close();
          this.importDialogVisible = false;
          // 重新加载数据
          await this.loadEnrollments();
          this.$message.success('数据导入成功');
        } catch (error) {
        console.error('导入数据失败:', error)
        loading.close()
        this.$message.error('导入失败，请检查文件格式并重试')
      }
    },
    
    // 批量导出
    async batchExport() {
      try {
        this.$message.info('正在导出数据...');
        // 只传递后端支持的过滤参数
        const params = {};
        if (this.filterOptions.semesterId) params.semesterId = this.filterOptions.semesterId;
        if (this.filterOptions.courseId) params.courseId = this.filterOptions.courseId;
        if (this.filterOptions.studentId) params.studentId = this.filterOptions.studentId;
        
        const token = localStorage.getItem('token');
        
        // 使用axios获取文件流
        const response = await this.$axios({
          url: '/enrollments/export',
          method: 'GET',
          params: params,
          responseType: 'blob'
        });
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `选课数据_${new Date().toLocaleDateString()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(link.href);
      } catch (error) {
        console.error('导出数据失败:', error);
        this.$message.error('导出失败，请稍后重试');
      }
    },
    
    // 显示统计对话框
    showStatistics() {
      this.statisticsDialogVisible = true
    },
    
    // 获取选课状态标签样式
    getEnrollmentStatusTag(status) {
      const tagMap = {
        'NORMAL': 'primary',
        'WITHDRAWN': 'danger',
        'COMPLETED': 'success',
        'FAILED': 'warning'
      }
      return tagMap[status] || 'info'
    },
    
    // 获取选课状态文本
    getEnrollmentStatusText(status) {
      const textMap = {
        'NORMAL': '正常',
        'WITHDRAWN': '已退课',
        'COMPLETED': '已完成',
        'FAILED': '已挂科'
      }
      return textMap[status] || '未知'
    },
    
    // 格式化日期时间
    formatDateTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
  }
}
</script>

<style scoped>
/* 选课管理界面黑白配色主题 */
.enrollment-management {
  padding: 20px;
}

.management-header {
  margin-bottom: 24px;
  border-radius: 8px;
}

.management-header h1 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.management-content {
  border-radius: 8px;
}

/* 表格样式 - 黑白配色 */
.table-container :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.table-container :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.table-container :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
  font-weight: 600;
}

.table-container :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.table-container :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.table-container :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.table-container :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.table-container :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.table-container :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.table-container :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.table-container :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.table-container :deep(.el-tag--success) {
  background-color: #f0f0f0;
}

.table-container :deep(.el-tag--warning) {
  background-color: #f0f0f0;
}

.table-container :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.table-container :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.table-container :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.table-container :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.table-container :deep(.el-button--danger:hover) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

/* 输入框样式 */
.table-container :deep(.el-input__inner) {
  border: 1px solid #000000;
  background-color: #ffffff;
  color: #000000;
}

.table-container :deep(.el-input__inner:focus) {
  border-color: #000000;
}

.table-container :deep(.el-select .el-input__inner) {
  border: 1px solid #000000;
}

/* 分页样式 */
.table-container :deep(.el-pagination .btn-next),
.table-container :deep(.el-pagination .btn-prev),
.table-container :deep(.el-pagination .el-pager li) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.table-container :deep(.el-pagination .el-pager li.active) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.table-container :deep(.el-pagination .el-pagination__total) {
  color: #000000;
}

.table-container :deep(.el-pagination .el-pagination__jump) {
  color: #000000;
}

.table-container :deep(.el-pagination .el-pagination__sizes .el-input__inner) {
  border: 1px solid #000000;
}

/* 全局按钮样式 */
.management-content :deep(.el-button--primary) {
  background-color: #409EFF;
  border-color: #409EFF;
  color: #ffffff;
}

.management-content :deep(.el-button--success) {
  background-color: #67C23A;
  border-color: #67C23A;
  color: #ffffff;
}

.management-content :deep(.el-button--info) {
  background-color: #909399;
  border-color: #909399;
  color: #ffffff;
}

.management-content :deep(.el-button--danger) {
  background-color: #F56C6C;
  border-color: #F56C6C;
  color: #ffffff;
}

.management-content :deep(.el-button--warning) {
  background-color: #E6A23C;
  border-color: #E6A23C;
  color: #ffffff;
}

.management-content :deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
  color: #ffffff;
}

.management-content :deep(.el-button--success:hover),
.management-content :deep(.el-button--info:hover),
.management-content :deep(.el-button--danger:hover),
.management-content :deep(.el-button--warning:hover) {
  background-color: #606266;
  border-color: #606266;
  color: #ffffff;
}

/* 表格内按钮样式 - 与课程管理页面保持一致 */
.table-container :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.table-container :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.table-container :deep(.el-button--primary:hover) {
  background-color: #333333;
  border-color: #333333;
  color: #ffffff;
}

.table-container :deep(.el-button--danger:hover) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

/* 禁用状态按钮样式 */
.table-container :deep(.el-button.is-disabled),
.table-container :deep(.el-button--primary.is-disabled),
.table-container :deep(.el-button--danger.is-disabled) {
  background-color: #dcdfe6;
  border-color: #dcdfe6;
  color: #a8abb2;
  cursor: not-allowed;
}

/* 全局标签样式 */
.management-content :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.management-content :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.management-content :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

.management-content :deep(.el-tag--success) {
  background-color: #f0f0f0;
}

.management-content :deep(.el-tag--warning) {
  background-color: #f0f0f0;
}

/* 全局开关样式 */
.management-content :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.management-content :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 全局选择器样式 */
.management-content :deep(.el-select .el-input__inner) {
  border: 1px solid #000000;
}

.management-content :deep(.el-select .el-input__inner:focus) {
  border-color: #000000;
}

/* 全局输入框样式 */
.management-content :deep(.el-input__inner) {
  outline: none;
  box-shadow: none;
}

.management-content :deep(.el-input__inner:focus),
.management-content :deep(.el-input__wrapper.is-focus) {
  outline: none;
}

/* 全局数字输入框样式 */
.management-content :deep(.el-input-number__decrease),
.management-content :deep(.el-input-number__increase) {
  border: 1px solid #000000;
  background-color: #ffffff;
  color: #000000;
}

/* 搜索框行样式 */
.search-inputs {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

/* 卡片样式优化 */
.management-content :deep(.el-card__header) {
  border-bottom: 1px solid #000000;
}

.management-content :deep(.el-card) {
  border: 1px solid #000000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section .el-col {
    flex: 0 0 100%;
    margin-bottom: 10px;
  }
  
  .action-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-section .el-button {
    width: 100%;
  }
  
  .statistics-content .el-col {
    flex: 0 0 100%;
  }
}
</style>