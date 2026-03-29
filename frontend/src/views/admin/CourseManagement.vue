<template>
  <div class="course-management">
    <el-card class="management-header">
      <h1>课程管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <div class="search-filters">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="输入课程代码/名称搜索" 
          prefix-icon="el-icon-search" 
          style="width: 250px; margin-right: 12px;"
        />
        
        <el-select 
          v-model="searchForm.department" 
          placeholder="选择系部" 
          clearable 
          style="width: 150px; margin-right: 12px;"
        >
          <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
        </el-select>
        
        <el-select 
          v-model="searchForm.teacher" 
          placeholder="选择教师" 
          clearable 
          style="width: 150px; margin-right: 12px;"
        >
          <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
        </el-select>
        
        <el-select 
          v-model="searchForm.semester" 
          placeholder="选择学期" 
          clearable 
          style="width: 150px; margin-right: 12px;"
        >
          <el-option v-for="semester in semesters" :key="semester.id" :label="semester.name" :value="semester.id" />
        </el-select>
        
        <el-button type="primary" @click="searchCourses">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增课程
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportCourses" :disabled="selectedCourses.length === 0">
          <i class="el-icon-download"></i> 导出选中
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedCourses.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
      </div>
      
      <div class="course-table">
        <el-table 
          v-loading="loading" 
          :data="paginatedCourses" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleRowDblclick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="courseCode" label="课程代码" width="100" fixed="left" />
          <el-table-column prop="name" label="课程名称" width="150" />
          <el-table-column prop="courseType" label="课程类型" width="100">
            <template #default="scope">
              <el-tag :type="getCourseTypeTag(scope.row.courseType)">
                {{ scope.row.courseType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="credits" label="学分" width="80" />
          <el-table-column prop="hours" label="课时" width="80" />
          <el-table-column prop="departmentName" label="所属系部" width="120" />
          <el-table-column prop="teacherName" label="任课教师" width="100" />
          <el-table-column prop="semesterName" label="开设学期" width="150" />
          <el-table-column prop="classroom" label="上课地点" width="100" />
          <el-table-column prop="schedule" label="上课时间" width="120" />
          <el-table-column prop="maxStudents" label="最大选课数" width="100" />
          <el-table-column prop="studentCount" label="当前选课数" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.studentCount >= scope.row.maxStudents ? 'danger' : 'success'" size="small">
                {{ scope.row.studentCount }}/{{ scope.row.maxStudents }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-color="#333333"
                inactive-color="#999999"
                active-text="启用"
                inactive-text="停用"
                @change="updateStatus(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="showEditDialog(scope.row)"
                icon="el-icon-edit"
                style="margin-right: 5px;"
              >
                编辑
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                @click="viewStudents(scope.row)"
                icon="el-icon-user"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination" v-if="filteredCourses.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredCourses.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <div v-if="filteredCourses.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课程数据" />
      </div>
    </el-card>
    
    <!-- 新增/编辑课程对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增课程' : '编辑课程'" 
      v-model="dialogVisible" 
      width="60%"
    >
      <el-form 
        ref="courseForm" 
        :model="courseForm" 
        :rules="rules" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程代码" prop="courseCode" :disabled="dialogType === 'edit'">
              <el-input v-model="courseForm.courseCode" placeholder="请输入课程代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类型" prop="courseType">
              <el-select v-model="courseForm.courseType" placeholder="请选择课程类型">
                <el-option label="必修课" value="必修课" />
                <el-option label="选修课" value="选修课" />
                <el-option label="公共课" value="公共课" />
                <el-option label="专业课" value="专业课" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" prop="credits">
              <el-input-number 
                v-model="courseForm.credits" 
                :min="1" 
                :max="10" 
                :step="1" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课时" prop="hours">
              <el-input-number 
                v-model="courseForm.hours" 
                :min="1" 
                :max="120" 
                :step="1" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属系部" prop="departmentId">
              <el-select v-model="courseForm.departmentId" placeholder="请选择所属系部">
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任课教师" prop="teacherId">
              <el-select v-model="courseForm.teacherId" placeholder="请选择任课教师">
                <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开设学期" prop="semesterId">
              <el-select v-model="courseForm.semesterId" placeholder="请选择开设学期">
                <el-option v-for="semester in semesters" :key="semester.id" :label="semester.name" :value="semester.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课地点" prop="classroom">
              <el-input v-model="courseForm.classroom" placeholder="请输入上课地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课时间" prop="schedule">
              <el-input v-model="courseForm.schedule" placeholder="请输入上课时间安排" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大选课数" prop="maxStudents">
              <el-input-number 
                v-model="courseForm.maxStudents" 
                :min="1" 
                :max="200" 
                :step="1" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="courseForm.status"
                active-color="#333333"
                inactive-color="#999999"
                active-text="启用"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider>详细信息</el-divider>
        
        <el-form-item label="课程描述" prop="description">
          <el-input 
            v-model="courseForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入课程描述信息" 
          />
        </el-form-item>
        
        <el-form-item label="先修课程" prop="prerequisites">
          <el-select 
            v-model="courseForm.prerequisites" 
            multiple 
            placeholder="请选择先修课程" 
            style="width: 100%;"
          >
            <el-option 
              v-for="course in availablePrerequisites" 
              :key="course.courseCode" 
              :label="`${course.courseCode} - ${course.name}`" 
              :value="course.courseCode" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 选课学生对话框 -->
    <el-dialog 
      title="选课学生列表" 
      v-model="studentsDialogVisible" 
      width="80%"
      fullscreen
    >
      <div class="students-dialog">
        <div class="students-header">
          <h3>{{ selectedCourse?.name }} ({{ selectedCourse?.courseCode }}) - 选课学生</h3>
          <div class="students-stats">
            <el-tag type="info">总选课数：{{ courseStudents.length }}</el-tag>
            <el-tag type="success">已分配名额：{{ selectedCourse?.studentCount }}</el-tag>
            <el-tag type="warning">剩余名额：{{ getRemainingQuota() }}</el-tag>
          </div>
        </div>
        
        <div class="students-search">
          <el-input 
            v-model="studentsSearch" 
            placeholder="搜索学号/姓名" 
            prefix-icon="el-icon-search" 
            style="width: 250px;"
            @input="handleStudentsSearch"
          />
        </div>
        
        <el-table 
          :data="filteredCourseStudents" 
          style="width: 100%"
          @selection-change="handleStudentsSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="studentId" label="学号" width="120" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.gender === '男' ? 'info' : 'danger'" size="small">
                {{ scope.row.gender }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column prop="majorName" label="专业" width="120" />
          <el-table-column prop="enrollTime" label="选课时间" width="150">
            <template #default="scope">
              {{ formatDateTime(scope.row.enrollTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="成绩" width="100">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.score" 
                :precision="1" 
                :min="0" 
                :max="100" 
                @change="updateScore(scope.row)"
                :disabled="scope.row.score && scope.row.score > 0"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                type="danger" 
                size="small" 
                @click="removeStudent(scope.row)"
                icon="el-icon-delete"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="students-pagination" v-if="filteredCourseStudents.length > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper, sizes, total"
            :total="filteredCourseStudents.length"
            :page-size="studentsPageSize"
            :current-page="studentsCurrentPage"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleStudentsSizeChange"
            @current-change="handleStudentsCurrentChange"
          />
        </div>
        
        <div class="dialog-footer" style="margin-top: 20px;">
          <el-button @click="studentsDialogVisible = false">关闭</el-button>
          <el-button type="info" @click="exportCourseStudents" :disabled="courseStudents.length === 0">
            导出选课名单
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入课程" 
      v-model="importDialogVisible" 
      width="50%"
    >
      <div class="import-content">
        <p class="import-hint">请下载模板，填写完整信息后导入课程数据。</p>
        
        <div class="template-download">
          <el-button type="primary" @click="downloadTemplate">
            <i class="el-icon-download"></i> 下载导入模板
          </el-button>
        </div>
        
        <div class="import-upload">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="importFileList"
            accept=".xlsx,.xls"
            :limit="1"
          >
            <el-button size="small" type="primary">点击上传文件</el-button>
            <div slot="tip" class="el-upload__tip">
              支持上传Excel文件，仅支持.xlsx和.xls格式
            </div>
          </el-upload>
        </div>
        
        <div v-if="importPreviewData.length > 0" class="import-preview">
          <h3>数据预览</h3>
          <el-table :data="importPreviewData" style="width: 100%" height="300">
            <el-table-column prop="courseCode" label="课程代码" width="120" />
            <el-table-column prop="name" label="课程名称" width="150" />
            <el-table-column prop="teacherName" label="教师姓名" width="120" />
            <el-table-column prop="error" label="错误信息" width="200">
              <template #default="scope">
                <span v-if="scope.row.error" class="error-text">{{ scope.row.error }}</span>
                <span v-else class="success-text">验证通过</span>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="hasErrors" class="error-message">
            <el-alert
              title="部分数据验证失败，请检查后重新导入"
              type="warning"
              description="请查看错误信息列了解具体问题"
              show-icon
              :closable="false"
            />
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmImport" 
          :disabled="importFileList.length === 0 || hasErrors"
        >
          确认导入
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CourseManagement',
  data() {
    return {
      loading: false,
      courses: [],
      selectedCourses: [],
      filteredCourses: [],
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        keyword: '',
        department: '',
        teacher: '',
        semester: ''
      },
      departments: [],
      teachers: [],
      semesters: [],
      dialogVisible: false,
      dialogType: 'add',
      courseForm: {
        courseCode: '',
        name: '',
        courseType: '必修课',
        credits: 3,
        hours: 48,
        departmentId: '',
        teacherId: '',
        semesterId: '',
        classroom: '',
        schedule: '',
        maxStudents: 50,
        studentCount: 0,
        status: true,
        description: '',
        prerequisites: []
      },
      importDialogVisible: false,
      importFileList: [],
      importPreviewData: [],
      studentsDialogVisible: false,
      selectedCourse: null,
      courseStudents: [],
      selectedStudents: [],
      studentsSearch: '',
      studentsCurrentPage: 1,
      studentsPageSize: 10,
      rules: {
        courseCode: [
          { required: true, message: '请输入课程代码', trigger: 'blur' },
          { min: 4, max: 20, message: '课程代码长度应为4-20个字符', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入课程名称', trigger: 'blur' },
          { min: 2, max: 50, message: '课程名称长度应为2-50个字符', trigger: 'blur' }
        ],
        courseType: [
          { required: true, message: '请选择课程类型', trigger: 'change' }
        ],
        credits: [
          { required: true, message: '请输入学分', trigger: 'blur' }
        ],
        hours: [
          { required: true, message: '请输入课时', trigger: 'blur' }
        ],
        departmentId: [
          { required: true, message: '请选择所属系部', trigger: 'change' }
        ],
        teacherId: [
          { required: true, message: '请选择任课教师', trigger: 'change' }
        ],
        semesterId: [
          { required: true, message: '请选择开设学期', trigger: 'change' }
        ],
        maxStudents: [
          { required: true, message: '请输入最大选课人数', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 分页后的课程数据
    paginatedCourses() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredCourses.slice(start, end)
    },
    
    // 过滤后的选课学生数据
    filteredCourseStudents() {
      let students = [...this.courseStudents]
      if (this.studentsSearch) {
        const keyword = this.studentsSearch.toLowerCase()
        students = students.filter(s => 
          s.studentId.toLowerCase().includes(keyword) ||
          s.name.toLowerCase().includes(keyword)
        )
      }
      
      // 分页处理
      const start = (this.studentsCurrentPage - 1) * this.studentsPageSize
      const end = start + this.studentsPageSize
      return students.slice(start, end)
    },
    
    // 可选为先修课程的课程列表
    availablePrerequisites() {
      // 排除当前编辑的课程（如果是编辑模式）
      if (this.dialogType === 'edit') {
        return this.courses.filter(c => c.courseCode !== this.courseForm.courseCode)
      }
      return this.courses
    },
    
    // 是否有错误数据
    hasErrors() {
      return this.importPreviewData.some(item => item.error)
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    // 根据系部名称获取系部ID
    getDepartmentIdByName(departmentName) {
      if (!departmentName) return '';
      const department = this.departments.find(d => d.name === departmentName);
      return department ? department.id : '';
    },
    
    // 根据学期名称获取学期ID
    getSemesterIdByName(semesterName) {
      if (!semesterName) return '';
      const semester = this.semesters.find(s => s.name === semesterName);
      return semester ? semester.id : '';
    },
    
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        // 从后端API获取数据
        await Promise.all([
          this.loadCourses(),
          this.loadDepartments(),
          this.loadTeachers(),
          this.loadSemesters()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载课程列表
    async loadCourses() {
      try {
        // 构建查询参数
        const params = {}
        if (this.searchForm.department) params.departmentId = this.searchForm.department
        if (this.searchForm.teacher) params.teacherId = this.searchForm.teacher
        if (this.searchForm.semester) params.semesterId = this.searchForm.semester
        if (this.searchForm.keyword) params.keyword = this.searchForm.keyword
        
        const response = await this.$axios.get('/api/courses', { params })
        if (response.data.success) {
          this.courses = response.data.data
          this.filteredCourses = [...this.courses]
        } else {
          this.$message.error(response.data.message || '加载课程失败')
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.courses = []
        this.filteredCourses = []
        this.$message.error('加载课程失败，请检查后端服务')
      }
    },
    
    // 加载系部列表
    async loadDepartments() {
      try {
        const response = await this.$axios.get('/api/departments')
        if (response.data.success) {
          this.departments = response.data.data
        } else {
          this.$message.error(response.data.message || '加载系部失败')
        }
      } catch (error) {
        console.error('加载系部失败:', error)
        this.departments = []
        this.$message.error('加载系部失败，请检查后端服务')
      }
    },
    
    // 加载教师列表
    async loadTeachers() {
      try {
        const response = await this.$axios.get('/api/teachers')
        if (response.data.success) {
          this.teachers = response.data.data
        } else {
          this.$message.error(response.data.message || '加载教师失败')
        }
      } catch (error) {
        console.error('加载教师失败:', error)
        this.teachers = []
        this.$message.error('加载教师失败，请检查后端服务')
      }
    },
    
    // 加载学期列表
    async loadSemesters() {
      try {
        const response = await this.$axios.get('/api/semesters')
        if (response.data.success) {
          this.semesters = response.data.data
        } else {
          this.$message.error(response.data.message || '加载学期失败')
        }
      } catch (error) {
        console.error('加载学期失败:', error)
        this.semesters = []
        this.$message.error('加载学期失败，请检查后端服务')
      }
    },
    
    searchCourses() {
      this.currentPage = 1
      this.loadCourses() // 调用后端API进行筛选
    },
    
    resetFilters() {
      this.searchForm = {
        keyword: '',
        department: '',
        teacher: '',
        semester: ''
      }
      this.loadCourses() // 重新加载所有课程
      this.currentPage = 1
    },
    
    handleSelectionChange(selection) {
      this.selectedCourses = selection
    },
    
    handleRowDblclick(row) {
      this.showEditDialog(row)
    },
    
    showAddDialog() {
      this.dialogType = 'add'
      this.resetCourseForm()
      this.dialogVisible = true
    },
    
    showEditDialog(course) {
      this.dialogType = 'edit'
      // 确保字段名匹配并处理可能的数据格式差异
      this.courseForm = {
        ...course,
        name: course.name,
        hours: course.totalHours || course.hours,
        // 确保departmentId正确设置，如果原始数据中没有departmentId但有departmentName，则进行转换
        departmentId: course.departmentId || this.getDepartmentIdByName(course.departmentName),
        teacherId: course.teacherId,
        // 确保semesterId正确设置，如果原始数据中没有semesterId但有semesterName，则进行转换
        semesterId: course.semesterId || this.getSemesterIdByName(course.semesterName)
      }
      this.dialogVisible = true
    },
    
    resetCourseForm() {
      this.courseForm = {
        courseCode: '',
        name: '',
        courseType: '必修课',
        credits: 3,
        totalHours: 48,
        hours: 48,
        departmentId: '',
        teacherId: '',
        semesterId: '',
        classroom: '',
        dayOfWeek: '',
        startTime: null,
        endTime: null,
        maxStudents: 50,
        status: true,
        description: '',
        prerequisites: []
      }
      this.$refs.courseForm && this.$refs.courseForm.resetFields()
    },
    
    async submitForm() {
      this.$refs.courseForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const formData = { ...this.courseForm }
          
          if (this.dialogType === 'add') {
            const response = await this.$axios.post('/api/courses', formData)
            if (response.data.success) {
              this.courses.unshift(response.data.data)
              this.dialogVisible = false
              this.searchCourses() // 刷新搜索结果
              this.$message.success('课程创建成功')
            } else {
              this.$message.error(response.data.message || '课程创建失败')
            }
          } else {
            const response = await this.$axios.put(`/api/courses/${formData.id}`, formData)
            if (response.data.success) {
              const index = this.courses.findIndex(c => c.id === formData.id)
              if (index !== -1) {
                this.courses[index] = response.data.data
              }
              this.dialogVisible = false
              this.searchCourses() // 刷新搜索结果
              this.$message.success('课程更新成功')
            } else {
              this.$message.error(response.data.message || '课程更新失败')
            }
          }
        } catch (error) {
          console.error('保存课程信息失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    deleteCourse(course) {
      this.$confirm(`确定要删除课程 ${course.name} (${course.courseCode}) 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/api/courses/${course.id}`)
          if (response.data.success) {
            const index = this.courses.findIndex(c => c.id === course.id)
            if (index !== -1) {
              this.courses.splice(index, 1)
            }
            
            this.searchCourses() // 刷新搜索结果
            this.$message.success('课程删除成功')
          } else {
            this.$message.error(response.data.message || '课程删除失败')
          }
        } catch (error) {
          console.error('删除课程失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    batchDelete() {
      this.$confirm(`确定要删除选中的 ${this.selectedCourses.length} 门课程吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const courseIds = this.selectedCourses.map(c => c.id)
          
          const response = await this.$axios.delete('/api/courses/batch', {
            data: { courseIds }
          })
          
          if (response.data.success) {
            this.courses = this.courses.filter(c => !courseIds.includes(c.id))
            
            this.selectedCourses = []
            this.searchCourses() // 刷新搜索结果
            this.$message.success('批量删除成功')
          } else {
            this.$message.error(response.data.message || '批量删除失败')
          }
        } catch (error) {
          console.error('批量删除课程失败:', error)
          this.$message.error('批量删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    batchImport() {
      this.importDialogVisible = true
      this.importFileList = []
      this.importPreviewData = []
    },
    
    downloadTemplate() {
      // 实际项目中应该调用后端API下载模板
      this.$message.info('导入模板下载功能开发中')
    },
    
    handleFileChange(file) {
      this.importFileList = [file]
      // 读取文件并预览数据
      this.previewImportData(file)
    },
    
    previewImportData(file) {
      // 模拟解析Excel文件
      this.importPreviewData = [
        {
          courseCode: 'CS401',
          name: '数据结构与算法',
          courseType: '必修课',
          credits: 4,
          teacherName: '李教授',
          error: ''
        },
        {
          courseCode: 'CS402',
          name: '操作系统原理',
          courseType: '必修课',
          credits: 4,
          teacherName: '王副教授',
          error: '课程代码已存在'
        },
        {
          courseCode: 'CS403',
          name: '计算机网络',
          courseType: '必修课',
          credits: 4,
          teacherName: '张讲师',
          error: ''
        }
      ]
    },
    
    async confirmImport() {
      try {
        // 实际项目中应该调用后端API导入数据
        // const formData = new FormData()
        // formData.append('file', this.importFileList[0].raw)
        // await axios.post('/api/admin/courses/import', formData, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`,
        //     'Content-Type': 'multipart/form-data'
        //   }
        // })
        
        // 模拟导入成功
        this.importDialogVisible = false
        this.loadData() // 重新加载数据
        this.$message.success('批量导入成功')
      } catch (error) {
        console.error('批量导入失败:', error)
        this.$message.error('批量导入失败，请稍后重试')
      }
    },
    
    exportCourses() {
      // 实际项目中应该调用后端API导出数据
      this.$message.info('导出功能开发中')
    },
    
    async updateStatus(course) {
      try {
        // 实际项目中应该调用后端API
        // await axios.patch(`/api/admin/courses/${course.courseCode}/status`, {
        //   status: course.status
        // }, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`
        //   }
        // })
        
        this.$message.success('状态更新成功')
      } catch (error) {
        console.error('更新状态失败:', error)
        // 恢复原状态
        course.status = !course.status
        this.$message.error('更新状态失败，请稍后重试')
      }
    },
    
    async viewStudents(course) {
      this.selectedCourse = course
      this.studentsDialogVisible = true
      this.studentsSearch = ''
      this.studentsCurrentPage = 1
      
      try {
        // 实际项目中应该从后端API获取选课学生数据
        // const response = await axios.get(`/api/admin/courses/${course.courseCode}/students`, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`
        //   }
        // })
        // this.courseStudents = response.data
        // 使用实际API获取学生列表
        const response = await this.$axios.get(`/api/courses/${course.id}/students`)
        if (response.data.success) {
          this.courseStudents = response.data.data
        } else {
          this.courseStudents = []
        }
      } catch (error) {
        console.error('加载选课学生失败:', error)
        this.courseStudents = []
        this.$message.error('加载选课学生失败，请稍后重试')
      }
    },
    
    handleStudentsSearch() {
      this.studentsCurrentPage = 1
    },
    
    handleStudentsSelectionChange(selection) {
      this.selectedStudents = selection
    },
    
    removeStudent(student) {
      this.$confirm(`确定要取消学生 ${student.name} (${student.studentId}) 的选课吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 实际项目中应该调用后端API
          // await axios.delete(`/api/admin/courses/${this.selectedCourse.courseCode}/students/${student.studentId}`, {
          //   headers: {
          //     'Authorization': `Bearer ${localStorage.getItem('token')}`
          //   }
          // })
          
          // 模拟移除
          const index = this.courseStudents.findIndex(s => s.studentId === student.studentId)
          if (index !== -1) {
            this.courseStudents.splice(index, 1)
          }
          
          // 更新课程当前选课人数
          const courseIndex = this.courses.findIndex(c => c.courseCode === this.selectedCourse.courseCode)
          if (courseIndex !== -1) {
            this.courses[courseIndex].studentCount = Math.max(0, this.courses[courseIndex].studentCount - 1)
            this.selectedCourse.studentCount = this.courses[courseIndex].studentCount
          }
          
          this.$message.success('取消选课成功')
        } catch (error) {
          console.error('取消选课失败:', error)
          this.$message.error('取消选课失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    async updateScore(student) {
      if (student.score === null || student.score === undefined) return
      
      try {
        // 实际项目中应该调用后端API
        // await axios.patch(`/api/admin/courses/${this.selectedCourse.courseCode}/students/${student.studentId}/score`, {
        //   score: student.score
        // }, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`
        //   }
        // })
        
        this.$message.success('成绩更新成功')
      } catch (error) {
        console.error('更新成绩失败:', error)
        this.$message.error('更新成绩失败，请稍后重试')
      }
    },
    
    exportCourseStudents() {
      // 实际项目中应该调用后端API导出数据
      this.$message.info('导出功能开发中')
    },
    
    getCourseTypeTag(type) {
      const tagMap = {
        '必修课': 'danger',
        '选修课': 'success',
        '公共课': 'info',
        '专业课': 'warning'
      }
      return tagMap[type] || 'info'
    },
    
    getRemainingQuota() {
      if (!this.selectedCourse) return 0
      return Math.max(0, this.selectedCourse.maxStudents - this.selectedCourse.studentCount)
    },
    
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return ''
      const date = new Date(dateTimeString)
      return date.toLocaleString('zh-CN')
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
    
    handleStudentsSizeChange(size) {
      this.studentsPageSize = size
      this.studentsCurrentPage = 1
    },
    
    handleStudentsCurrentChange(current) {
      this.studentsCurrentPage = current
    },
    
  }
}
</script>

<style scoped>
.course-management {
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

.search-filters {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.action-buttons {
  display: flex;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.course-table {
  margin-bottom: 20px;
}

/* 表格样式 - 黑白配色 */
.course-table :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.course-table :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.course-table :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
  font-weight: 600;
}

.course-table :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.course-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.course-table :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.course-table :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.course-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.course-table :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.course-table :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.course-table :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.course-table :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.course-table :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.course-table :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.course-table :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.course-table :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.course-table :deep(.el-button--danger:hover) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

/* 学生对话框样式 */
.students-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-weight: 600;
}

.students-dialog {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
}

.students-search {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.students-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.students-stats {
  display: flex;
  gap: 12px;
}
</style>