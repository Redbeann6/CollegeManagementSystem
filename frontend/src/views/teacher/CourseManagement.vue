<template>
  <div class="course-management-container">
    <div class="page-header">
      <h1>课程管理</h1>
      <p>管理您负责的课程及相关教学工作</p>
    </div>

    <!-- 课程列表区域 -->
    <el-card class="course-list-card">
      <template #header>
        <div class="card-header">
          <span><i class="el-icon-document"></i> 我的课程</span>
          <el-button type="primary" @click="handleCreateCourse">
            <i class="el-icon-plus"></i> 创建新课程
          </el-button>
        </div>
      </template>

      <!-- 搜索筛选区域 -->
      <div class="search-filter-section">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索课程名称或编号"
              prefix-icon="el-icon-search"
              clearable
              @change="searchCourses"
            ></el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedSemester" placeholder="选择学期" clearable @change="searchCourses">
              <el-option
                v-for="semester in semesters"
                :key="semester.value"
                :label="semester.label"
                :value="semester.value"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedDepartment" placeholder="选择系部" clearable @change="searchCourses">
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedStatus" placeholder="课程状态" clearable @change="searchCourses">
              <el-option label="全部" value=""></el-option>
              <el-option label="进行中" value="ongoing"></el-option>
              <el-option label="已结束" value="completed"></el-option>
              <el-option label="未开始" value="notStarted"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <div class="action-buttons">
              <el-button type="info" icon="el-icon-refresh" @click="refreshCourseList">
                刷新列表
              </el-button>
              <el-button type="success" icon="el-icon-download" @click="exportCourseData">
                导出数据
              </el-button>
              <el-upload
                class="upload-demo"
                action=""
                :on-change="handleImportCourse"
                :auto-upload="false"
                accept=".xlsx,.xls"
                :show-file-list="false"
              >
                <el-button type="warning" icon="el-icon-upload2">
                  导入课程
                </el-button>
              </el-upload>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 课程列表表格 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="coursesData"
          style="width: 100%"
          @row-click="handleRowClick"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="课程编号" width="120" sortable></el-table-column>
          <el-table-column prop="name" label="课程名称" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="course-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="credit" label="学分" width="80" sortable></el-table-column>
          <el-table-column prop="hours" label="学时" width="80" sortable></el-table-column>
          <el-table-column prop="semester" label="学期" width="120"></el-table-column>
          <el-table-column prop="departmentName" label="所属系部" width="120"></el-table-column>
          <el-table-column prop="studentsCount" label="学生人数" width="100" sortable></el-table-column>
          <el-table-column prop="status" label="课程状态" width="100">
            <template #default="{ row }">
              <el-tag
                :type="getCourseStatusType(row.status)"
                size="small"
              >
                {{ getCourseStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" sortable></el-table-column>
          <el-table-column prop="endDate" label="结束日期" width="120" sortable></el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewCourseDetails(row.id)">
                详情
              </el-button>
              <el-button type="success" size="small" @click="manageStudents(row.id)">
                学生管理
              </el-button>
              <el-button type="info" size="small" @click="updateCourse(row.id)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteCourse(row.id)">
                删除
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
          :total="totalCourses"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 课程详情抽屉 -->
    <el-drawer
      title="课程详情"
      v-model="showCourseDetail"
      size="70%"
      direction="rtl"
    >
      <div v-if="currentCourse" class="course-detail-content">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card>
              <template #header>
                <div class="section-title">基本信息</div>
              </template>
              <div class="detail-info">
                <div class="info-row">
                  <div class="info-label">课程编号：</div>
                  <div class="info-value">{{ currentCourse.id }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">课程名称：</div>
                  <div class="info-value">{{ currentCourse.name }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">课程类型：</div>
                  <div class="info-value">{{ currentCourse.courseType }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">学分：</div>
                  <div class="info-value">{{ currentCourse.credit }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">学时：</div>
                  <div class="info-value">{{ currentCourse.hours }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">理论学时：</div>
                  <div class="info-value">{{ currentCourse.theoryHours }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">实践学时：</div>
                  <div class="info-value">{{ currentCourse.practiceHours }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">所属系部：</div>
                  <div class="info-value">{{ currentCourse.departmentName }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">开课学期：</div>
                  <div class="info-value">{{ currentCourse.semester }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">授课教师：</div>
                  <div class="info-value">{{ currentCourse.teacherName }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">课程状态：</div>
                  <div class="info-value">
                    <el-tag
                      :type="getCourseStatusType(currentCourse.status)"
                    >
                      {{ getCourseStatusText(currentCourse.status) }}
                    </el-tag>
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">开始日期：</div>
                  <div class="info-value">{{ currentCourse.startDate }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">结束日期：</div>
                  <div class="info-value">{{ currentCourse.endDate }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">创建时间：</div>
                  <div class="info-value">{{ currentCourse.createTime }}</div>
                </div>
              </div>
            </el-card>

            <el-card class="mt-20">
              <template #header>
                <div class="section-title">课程简介</div>
              </template>
              <div class="course-description">
                {{ currentCourse.description }}
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="section-title">课程统计</div>
              </template>
              <div class="course-stats">
                <div class="stat-item">
                  <div class="stat-number">{{ currentCourse.studentsCount }}</div>
                  <div class="stat-label">选课学生</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ currentCourse.totalLessons }}</div>
                  <div class="stat-label">总课时</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ currentCourse.completedLessons }}</div>
                  <div class="stat-label">已完成课时</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ currentCourse.averageScore || '暂无' }}</div>
                  <div class="stat-label">平均分</div>
                </div>
              </div>
            </el-card>

            <el-card class="mt-20">
              <template #header>
                <div class="section-title">近期活动</div>
              </template>
              <div class="recent-activities">
                <div v-if="currentCourse.activities && currentCourse.activities.length === 0" class="empty-state">
                  <p>暂无近期活动</p>
                </div>
                <div v-else v-for="(activity, index) in currentCourse.activities" :key="index" class="activity-item">
                  <div class="activity-icon">
                    <i :class="getActivityIcon(activity.type)"></i>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ activity.title }}</div>
                    <div class="activity-time">{{ activity.time }}</div>
                  </div>
                </div>
              </div>
            </el-card>

            <el-card class="mt-20">
              <template #header>
                <div class="section-title">操作</div>
              </template>
              <div class="action-section">
                <el-button type="primary" icon="el-icon-edit" @click="updateCourse(currentCourse.id)" class="w-full mb-2">
                  编辑课程
                </el-button>
                <el-button type="success" icon="el-icon-users" @click="manageStudents(currentCourse.id)" class="w-full mb-2">
                  管理学生 ({{ currentCourse.studentsCount }})
                </el-button>
                <el-button type="info" icon="el-icon-document" @click="viewTeachingPlan" class="w-full mb-2">
                  教学计划
                </el-button>
                <el-button type="warning" icon="el-icon-edit-outline" @click="manageGrades" class="w-full mb-2">
                  成绩管理
                </el-button>
                <el-button type="danger" icon="el-icon-delete" @click="deleteCourse(currentCourse.id)" class="w-full">
                  删除课程
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-drawer>

    <!-- 创建/编辑课程对话框 -->
    <el-dialog
      :title="isEditMode ? '编辑课程' : '创建课程'"
      v-model="showCreateDialog"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        :model="courseForm"
        :rules="courseRules"
        ref="courseForm"
        label-width="120px"
      >
        <el-form-item label="课程编号" prop="id">
          <el-input v-model="courseForm.id" :disabled="isEditMode" placeholder="请输入课程编号"></el-input>
        </el-form-item>
        
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" maxlength="50"></el-input>
        </el-form-item>
        
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="courseForm.courseType" placeholder="请选择课程类型">
            <el-option label="必修课" value="compulsory"></el-option>
            <el-option label="选修课" value="elective"></el-option>
            <el-option label="专业基础课" value="professionalBasic"></el-option>
            <el-option label="专业课" value="professional"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :min="0.5" :max="10" :step="0.5" placeholder="请输入学分"></el-input-number>
        </el-form-item>
        
        <el-form-item label="总学时" prop="hours">
          <el-input-number v-model="courseForm.hours" :min="1" :max="200" placeholder="请输入总学时"></el-input-number>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="理论学时" prop="theoryHours">
              <el-input-number v-model="courseForm.theoryHours" :min="0" :max="200" placeholder="请输入理论学时"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实践学时" prop="practiceHours">
              <el-input-number v-model="courseForm.practiceHours" :min="0" :max="200" placeholder="请输入实践学时"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="所属系部" prop="departmentId">
          <el-select v-model="courseForm.departmentId" placeholder="请选择所属系部">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="开课学期" prop="semester">
          <el-select v-model="courseForm.semester" placeholder="请选择开课学期">
            <el-option
              v-for="semester in semesters"
              :key="semester.value"
              :label="semester.label"
              :value="semester.value"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="courseForm.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="courseForm.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="课程简介" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            placeholder="请输入课程简介"
            :rows="4"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        
        <el-form-item label="考核方式" prop="assessmentMethod">
          <el-radio-group v-model="courseForm.assessmentMethod">
            <el-radio label="考试">考试</el-radio>
            <el-radio label="考查">考查</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="submitCourseForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog
      :title="'学生管理 - ' + selectedCourseName"
      v-model="showStudentManagement"
      width="80%"
    >
      <div class="student-management-content">
        <!-- 学生搜索筛选 -->
        <div class="student-search-section">
          <el-row :gutter="20" align="middle">
            <el-col :span="6">
              <el-input
                v-model="studentSearchQuery"
                placeholder="搜索学生姓名或学号"
                prefix-icon="el-icon-search"
                clearable
              ></el-input>
            </el-col>
            <el-col :span="4">
              <el-select v-model="studentSearchClass" placeholder="选择班级" clearable>
                <el-option
                  v-for="cls in classOptions"
                  :key="cls.value"
                  :label="cls.label"
                  :value="cls.value"
                ></el-option>
              </el-select>
            </el-col>
            <el-col :span="6">
              <div class="action-buttons">
                <el-button type="info" icon="el-icon-refresh" @click="refreshStudentList">
                  刷新列表
                </el-button>
                <el-button type="success" icon="el-icon-download" @click="exportStudentData">
                  导出名单
                </el-button>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 学生列表表格 -->
        <div class="table-container">
          <el-table
            v-loading="studentLoading"
            :data="studentsData"
            style="width: 100%"
            @selection-change="handleStudentSelectionChange"
          >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="studentId" label="学号" width="120" sortable></el-table-column>
            <el-table-column prop="name" label="姓名" width="100"></el-table-column>
            <el-table-column prop="className" label="班级" width="120"></el-table-column>
            <el-table-column prop="department" label="系部" width="120"></el-table-column>
            <el-table-column prop="grade" label="年级" width="100"></el-table-column>
            <el-table-column prop="gender" label="性别" width="80">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.gender === '男' ? 'primary' : 'danger'">
                  {{ scope.row.gender }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="成绩" width="100" sortable>
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.score"
                  :min="0"
                  :max="100"
                  @change="updateStudentScore(scope.row)"
                  size="small"
                ></el-input-number>
              </template>
            </el-table-column>
            <el-table-column prop="attendanceRate" label="出勤率" width="100" sortable>
              <template #default="scope">
                <el-progress :percentage="scope.row.attendanceRate" :show-text="true" :stroke-width="6"></el-progress>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === '正常' ? 'success' : 'warning'"
                  size="small"
                >
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewStudentDetails(scope.row.studentId)">
                  详情
                </el-button>
                <el-button type="danger" size="small" @click="removeStudentFromCourse(scope.row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 学生列表分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model="studentCurrentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="studentPageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalStudents"
            @size-change="handleStudentSizeChange"
            @current-change="handleStudentCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherCourseManagement',
  data() {
    return {
      // 搜索筛选条件
      searchQuery: '',
      selectedSemester: '',
      selectedDepartment: '',
      selectedStatus: '',
      
      // 分页参数
      currentPage: 1,
      pageSize: 10,
      totalCourses: 0,
      
      // 数据加载状态
      loading: false,
      
      // 课程数据
      courses: [],
      
      // 课程详情
      showCourseDetail: false,
      currentCourse: null,
      
      // 创建/编辑对话框
      showCreateDialog: false,
      isEditMode: false,
      
      // 课程表单
      courseForm: {
        id: '',
        name: '',
        courseType: '',
        credit: 0,
        hours: 0,
        theoryHours: 0,
        practiceHours: 0,
        departmentId: '',
        semester: '',
        startDate: '',
        endDate: '',
        description: '',
        assessmentMethod: '考试'
      },
      
      // 课程表单验证规则
      courseRules: {
        id: [
          { required: true, message: '请输入课程编号', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入课程名称', trigger: 'blur' }
        ],
        courseType: [
          { required: true, message: '请选择课程类型', trigger: 'change' }
        ],
        credit: [
          { required: true, message: '请输入学分', trigger: 'blur' }
        ],
        hours: [
          { required: true, message: '请输入总学时', trigger: 'blur' }
        ],
        departmentId: [
          { required: true, message: '请选择所属系部', trigger: 'change' }
        ],
        semester: [
          { required: true, message: '请选择开课学期', trigger: 'change' }
        ],
        startDate: [
          { required: true, message: '请选择开始日期', trigger: 'change' }
        ],
        endDate: [
          { required: true, message: '请选择结束日期', trigger: 'change' }
        ]
      },
      
      // 学期选项
      semesters: [],
      
      // 系部选项
      departments: [],
      
      // 班级选项
      classOptions: [],
      
      // 学生管理
      showStudentManagement: false,
      selectedCourseId: '',
      selectedCourseName: '',
      studentSearchQuery: '',
      studentSearchClass: '',
      studentLoading: false,
      studentsData: [],
      studentCurrentPage: 1,
      studentPageSize: 10,
      totalStudents: 0,
      
      // 选中的数据
      selectedCourses: [],
      selectedStudents: []
    }
  },
  
  computed: {
    // 课程数据（已由后端筛选）
    coursesData() {
      // 更新总数
      this.totalCourses = this.courses.length
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.courses.slice(start, end)
    }
  },
  
  mounted() {
    // 加载课程数据
    this.loadCourses()
  },
  
  methods: {
    // 加载课程数据
    async loadCourses() {
      this.loading = true
      try {
        // 构建查询参数
        const params = {}
        if (this.selectedDepartment) params.departmentId = this.selectedDepartment
        if (this.selectedSemester) params.semesterId = this.selectedSemester
        if (this.searchQuery) params.keyword = this.searchQuery
        
        // 调用后端API获取教师课程列表
        const response = await this.$axios.get('/teacher/data/courses', {
          params,
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        if (response.data.success) {
          // 标准化课程数据，确保必需字段存在
          this.courses = response.data.data.map(course => this.normalizeCourseData(course))
        } else {
          this.$message.error(response.data.message || '加载课程数据失败')
        }
      } catch (error) {
        console.error('加载课程数据失败:', error)
        this.$message.error('加载课程数据失败，请检查网络连接')
      } finally {
        this.loading = false
      }
    },
    
    // 标准化课程数据，确保必需字段存在
    normalizeCourseData(course) {
      return {
        ...course,
        // 确保departmentId存在，如果只有departmentName则转换
        departmentId: course.departmentId || this.getDepartmentIdByName(course.departmentName),
        // 确保semester存在，如果只有semesterName则转换
        semester: course.semester || this.getSemesterValueByName(course.semesterName)
      }
    },
    

    

    
    // 刷新课程列表
    refreshCourseList() {
      this.searchQuery = ''
      this.selectedSemester = ''
      this.selectedDepartment = ''
      this.selectedStatus = ''
      this.currentPage = 1
      this.loadCourses()
    },
    
    // 搜索课程
    searchCourses() {
      this.currentPage = 1
      this.loadCourses()
    },
    
    // 导出课程数据
    exportCourseData() {
      // 实际项目中应该调用后端API导出数据
      this.$message.success('课程数据导出成功')
    },
    
    // 导入课程数据
    handleImportCourse(file) {
      // 实际项目中应该处理文件上传和数据导入
      this.$message.success('课程数据导入成功')
    },
    
    // 处理行点击
    handleRowClick(row) {
      this.viewCourseDetails(row.id)
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedCourses = selection
    },
    
    // 查看课程详情
    viewCourseDetails(courseId) {
      const course = this.courses.find(c => c.id === courseId)
      if (course) {
        this.currentCourse = { ...course }
        this.showCourseDetail = true
      }
    },
    
    // 管理学生
    manageStudents(courseId) {
      const course = this.courses.find(c => c.id === courseId)
      if (course) {
        this.selectedCourseId = courseId
        this.selectedCourseName = course.name
        this.loadStudents(courseId)
        this.showStudentManagement = true
      }
    },
    
    // 加载学生数据
    async loadStudents(courseId) {
      this.studentLoading = true
      try {
        const response = await this.$axios.get(`/teacher/data/classes/${courseId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.studentsData = response.data.data || []
          this.totalStudents = this.studentsData.length
        } else {
          console.error('获取学生数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取学生数据失败')
          this.studentsData = []
          this.totalStudents = 0
        }
      } catch (error) {
        console.error('加载学生数据失败:', error)
        this.$message.error('加载学生数据失败，请稍后重试')
        this.studentsData = []
        this.totalStudents = 0
      } finally {
        this.studentLoading = false
      }
    },
    
    // 刷新学生列表
    refreshStudentList() {
      this.studentSearchQuery = ''
      this.studentSearchClass = ''
      this.studentCurrentPage = 1
      this.loadStudents(this.selectedCourseId)
    },
    
    // 导出学生数据
    exportStudentData() {
      // 实际项目中应该调用后端API导出数据
      this.$message.success('学生数据导出成功')
    },
    
    // 处理学生选择变化
    handleStudentSelectionChange(selection) {
      this.selectedStudents = selection
    },
    
    // 更新学生成绩
    updateStudentScore(student) {
      // 实际项目中应该调用后端API更新成绩
      this.$message.success('成绩更新成功')
    },
    
    // 查看学生详情
    viewStudentDetails(studentId) {
      // 实际项目中应该跳转到学生详情页面或显示学生详情对话框
      this.$message.info(`查看学生${studentId}的详细信息`)
    },
    
    // 从课程中移除学生
    removeStudentFromCourse(student) {
      this.$confirm(`确定要将学生 ${student.name} 从课程中移除吗？`, '确认移除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 实际项目中应该调用后端API移除学生
        const index = this.studentsData.findIndex(s => s.studentId === student.studentId)
        if (index > -1) {
          this.studentsData.splice(index, 1)
          this.totalStudents--
        }
        this.$message.success('学生已移除')
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 创建新课程
    handleCreateCourse() {
      this.isEditMode = false
      this.resetCourseForm()
      this.showCreateDialog = true
    },
    
    // 编辑课程
    async updateCourse(courseId) {
      try {
        // 从后端获取课程详情
        const response = await this.$axios.get(`/teacher/data/courses/${courseId}`)
        if (response.data.success) {
          const course = response.data.data
          this.isEditMode = true
          // 创建表单数据副本
          this.courseForm = { ...course }
          // 确保departmentId字段被正确设置（如果不存在则通过查找部门名称对应的ID）
          if (!this.courseForm.departmentId && course.departmentName) {
            this.courseForm.departmentId = this.getDepartmentIdByName(course.departmentName)
          }
          // 确保semester字段被正确设置（如果不存在则通过查找学期名称对应的值）
          if (!this.courseForm.semester && course.semesterName) {
            this.courseForm.semester = this.getSemesterValueByName(course.semesterName)
          }
          // 也检查是否有semester字段直接可用（根据模拟数据，这是更常见的情况）
          if (!this.courseForm.semester && course.semester) {
            this.courseForm.semester = course.semester
          }
          this.showCreateDialog = true
        } else {
          this.$message.error(response.data.message || '获取课程信息失败')
        }
      } catch (error) {
        console.error('获取课程信息失败:', error)
        this.$message.error('获取课程信息失败，请稍后重试')
      }
    },
    
    // 删除课程
    async deleteCourse(courseId) {
      this.$confirm('确定要删除该课程吗？删除后将无法恢复！', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/teacher/data/courses/${courseId}`)
          if (response.data.success) {
            // 从前端数组中移除课程
            const index = this.courses.findIndex(c => c.id === courseId)
            if (index > -1) {
              this.courses.splice(index, 1)
              this.$message.success('课程删除成功')
            }
            // 如果当前正在查看该课程的详情，关闭详情抽屉
            if (this.showCourseDetail && this.currentCourse && this.currentCourse.id === courseId) {
              this.showCourseDetail = false
            }
            // 刷新课程列表
            this.loadCourses()
          } else {
            this.$message.error(response.data.message || '删除课程失败')
          }
        } catch (error) {
          console.error('删除课程失败:', error)
          this.$message.error('删除课程失败，请稍后重试')
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 重置课程表单
    resetCourseForm() {
      this.courseForm = {
        id: '',
        name: '',
        courseType: '',
        credit: 0,
        hours: 0,
        theoryHours: 0,
        practiceHours: 0,
        departmentId: '',
        semester: '',
        startDate: '',
        endDate: '',
        description: '',
        assessmentMethod: '考试'
      }
    },
    
    // 处理对话框关闭
    handleDialogClose() {
      this.showCreateDialog = false
      this.$refs.courseForm && this.$refs.courseForm.resetFields()
    },
    
    // 提交课程表单
    async submitCourseForm() {
      this.$refs.courseForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.isEditMode) {
              // 编辑课程
              const response = await this.$axios.put(`/teacher/data/courses/${this.courseForm.id}`, this.courseForm)
              if (response.data.success) {
                // 更新前端数组中的课程
                const index = this.courses.findIndex(c => c.id === this.courseForm.id)
                if (index > -1) {
                  this.courses.splice(index, 1, { ...this.courseForm, departmentName: this.getDepartmentName(this.courseForm.departmentId) })
                }
                this.$message.success('课程更新成功')
                // 刷新课程列表
                this.loadCourses()
              } else {
                this.$message.error(response.data.message || '更新课程失败')
              }
            } else {
              // 创建课程
              const response = await this.$axios.post('/teacher/data/courses', this.courseForm)
              if (response.data.success) {
                this.$message.success('课程创建成功')
                // 刷新课程列表
                this.loadCourses()
              } else {
                this.$message.error(response.data.message || '创建课程失败')
              }
            }
            this.showCreateDialog = false
          } catch (error) {
            console.error('提交课程表单失败:', error)
            this.$message.error(this.isEditMode ? '更新课程失败，请稍后重试' : '创建课程失败，请稍后重试')
          }
        }
      })
    },
    
    // 查看教学计划
    viewTeachingPlan() {
      // 实际项目中应该跳转到教学计划页面
      this.$message.info('查看教学计划')
    },
    
    // 管理成绩
    manageGrades() {
      // 实际项目中应该跳转到成绩管理页面
      this.$message.info('管理课程成绩')
    },
    
    // 获取系部名称
    getDepartmentName(departmentId) {
      const department = this.departments.find(d => d.id === departmentId)
      return department ? department.name : ''
    },
    
    // 根据系部名称获取系部ID
    getDepartmentIdByName(departmentName) {
      if (!departmentName) return ''
      const department = this.departments.find(d => d.name === departmentName)
      return department ? department.id : ''
    },
    
    // 根据学期名称获取学期值
    getSemesterValueByName(semesterName) {
      if (!semesterName) return ''
      const semester = this.semesters.find(s => s.label === semesterName)
      return semester ? semester.value : ''
    },
    
    // 获取课程状态类型
    getCourseStatusType(status) {
      const statusMap = {
        'ongoing': 'success',
        'completed': 'info',
        'notStarted': 'warning'
      }
      return statusMap[status] || 'primary'
    },
    
    // 获取课程状态文本
    getCourseStatusText(status) {
      const statusMap = {
        'ongoing': '进行中',
        'completed': '已结束',
        'notStarted': '未开始'
      }
      return statusMap[status] || status
    },
    
    // 获取活动图标
    getActivityIcon(type) {
      const iconMap = {
        'assignment': 'el-icon-document',
        'exam': 'el-icon-edit-outline',
        'class': 'el-icon-time',
        'experiment': 'el-icon-setting',
        'project': 'el-icon-connection'
      }
      return iconMap[type] || 'el-icon-info'
    },
    
    // 分页相关方法
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
    
    handleStudentSizeChange(size) {
      this.studentPageSize = size
      this.studentCurrentPage = 1
    },
    
    handleStudentCurrentChange(current) {
      this.studentCurrentPage = current
    }
  }
}
</script>

<style scoped>
/* 引入公共样式 */
@import '../../styles/common-styles.css';

/* 本地样式 */
.course-management-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.course-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: bold;
  font-size: 16px;
}

.course-name {
  font-weight: bold;
  color: #409eff;
}

/* 课程详情样式 */
.course-detail-content {
  padding: 10px 0;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.detail-info {
  padding: 10px 0;
}

.info-row {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.info-label {
  width: 100px;
  font-weight: bold;
  color: #606266;
}

.info-value {
  flex: 1;
  color: #303133;
}

.course-description {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

/* 课程统计样式 */
.course-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

/* 近期活动样式 */
.recent-activities {
  padding: 10px 0;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.activity-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 操作区域 */
.action-section {
  padding: 10px 0;
}

.action-section .el-button {
  margin-bottom: 10px;
}

/* 学生管理样式 */
.student-management-content {
  padding: 10px 0;
}

.student-search-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-filter-section .el-col,
  .student-search-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .search-filter-section .el-col:last-child,
  .student-search-section .el-col:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .course-management-container {
    padding: 10px;
  }
  
  .info-row {
    flex-direction: column;
  }
  
  .info-label {
    width: 100%;
    margin-bottom: 5px;
  }
  
  .course-stats {
    grid-template-columns: 1fr;
  }
  
  .activity-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .activity-icon {
    margin-bottom: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>