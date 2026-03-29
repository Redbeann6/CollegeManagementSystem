<template>
  <div class="teacher-management">
    <el-card class="management-header">
      <h1>教师管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <div class="search-filters">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="输入教师工号/姓名搜索" 
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
          v-model="searchForm.title" 
          placeholder="选择职称" 
          clearable 
          style="width: 120px; margin-right: 12px;"
        >
          <el-option label="教授" value="教授" />
          <el-option label="副教授" value="副教授" />
          <el-option label="讲师" value="讲师" />
          <el-option label="助教" value="助教" />
        </el-select>
        
        <el-select 
          v-model="searchForm.status" 
          placeholder="选择状态" 
          clearable 
          style="width: 120px; margin-right: 12px;"
        >
          <el-option :label="'启用'" :value="true" />
          <el-option :label="'禁用'" :value="false" />
        </el-select>
        
        <el-button type="primary" @click="searchTeachers">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增教师
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportTeachers" :disabled="selectedTeachers.length === 0">
          <i class="el-icon-download"></i> 导出选中
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedTeachers.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
      </div>
      
      <div class="teacher-table">
        <el-table 
          v-loading="loading" 
          :data="paginatedTeachers" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleRowDblclick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="teacherId" label="工号" width="120" fixed="left" />
          <el-table-column prop="name" label="姓名" width="100">
            <template #default="scope">
              <div class="user-info">
                <img 
                  :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                  :alt="scope.row.name" 
                  class="avatar"
                />
                <span>{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.gender === '男' ? 'info' : 'danger'" size="small">
                {{ scope.row.gender || '未设置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="departmentName" label="所属系部" width="150">
            <template #default="scope">
              {{ getDepartmentName(scope.row.departmentId) }}
            </template>
          </el-table-column>
          <el-table-column prop="title" label="职称" width="100">
            <template #default="scope">
              <el-tag :type="getTitleTag(scope.row.title)">
                {{ scope.row.title }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="education" label="学历" width="100" />
          <el-table-column prop="phone" label="联系电话" width="120" />
          <el-table-column prop="email" label="电子邮箱" width="180" />
          <el-table-column prop="joinDate" label="入职日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.joinDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-color="#333333"
                inactive-color="#999999"
                active-text="启用"
                inactive-text="禁用"
                @change="updateStatus(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="420" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="showEditDialog(scope.row)"
                icon="el-icon-edit"
              >
                编辑
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="viewCourses(scope.row)"
                icon="el-icon-collection"
              >
                课程管理
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="resetPassword(scope.row)"
                icon="el-icon-key"
              >
                重置密码
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteTeacher(scope.row)"
                icon="el-icon-delete"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination" v-if="filteredTeachers.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredTeachers.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <div v-if="filteredTeachers.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无教师数据" />
      </div>
    </el-card>
    
    <!-- 新增/编辑教师对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增教师' : '编辑教师'" 
      v-model="dialogVisible" 
      width="70%"
    >
      <el-form 
        ref="teacherForm" 
        :model="teacherForm" 
        :rules="rules" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="teacherId" :disabled="dialogType === 'edit'">
              <el-input v-model="teacherForm.teacherId" placeholder="请输入教师工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="teacherForm.username" placeholder="请输入登录用户名" :disabled="dialogType === 'edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="teacherForm.name" placeholder="请输入教师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="teacherForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="teacherForm.birthDate"
                type="date"
                placeholder="选择出生日期"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属系部" prop="departmentId">
              <el-select v-model="teacherForm.departmentId" placeholder="请选择所属系部">
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="teacherForm.title" placeholder="请选择职称">
                <el-option label="教授" value="教授" />
                <el-option label="副教授" value="副教授" />
                <el-option label="讲师" value="讲师" />
                <el-option label="助教" value="助教" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历" prop="education">
              <el-select v-model="teacherForm.education" placeholder="请选择学历">
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
                <el-option label="博士后" value="博士后" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="teacherForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="teacherForm.email" placeholder="请输入电子邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="joinDate">
              <el-date-picker
                v-model="teacherForm.joinDate"
                type="date"
                placeholder="选择入职日期"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="teacherForm.status"
                active-color="#13ce66"
                inactive-color="#ff4949"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="dialogType === 'add'">
            <el-form-item label="初始密码" prop="password">
              <el-input 
                v-model="teacherForm.password" 
                type="password" 
                placeholder="请输入初始密码（不输入则默认为123456）" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider>详细信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="毕业院校" prop="graduateSchool">
              <el-input v-model="teacherForm.graduateSchool" placeholder="请输入毕业院校" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="专业方向" prop="major">
              <el-input v-model="teacherForm.major" placeholder="请输入专业方向" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="个人简介" prop="biography">
              <el-input 
                v-model="teacherForm.biography" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入个人简介" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider>联系方式</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="办公地点" prop="office">
              <el-input v-model="teacherForm.office" placeholder="请输入办公地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="办公电话" prop="officePhone">
              <el-input v-model="teacherForm.officePhone" placeholder="请输入办公电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="移动电话" prop="mobilePhone">
              <el-input v-model="teacherForm.mobilePhone" placeholder="请输入移动电话" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="通讯地址" prop="address">
              <el-input 
                v-model="teacherForm.address" 
                type="textarea" 
                :rows="2" 
                placeholder="请输入通讯地址" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 教师课程管理对话框 -->
    <el-dialog 
      title="教师授课课程" 
      v-model="coursesDialogVisible" 
      width="80%"
      fullscreen
    >
      <div class="courses-dialog">
        <div class="courses-header">
          <h3>{{ selectedTeacher?.name }} ({{ selectedTeacher?.teacherId }}) - 授课课程</h3>
          <div class="courses-stats">
            <el-tag type="info">总课程数：{{ teacherCourses.length }}</el-tag>
          </div>
        </div>
        
        <div class="courses-search">
          <el-input 
            v-model="coursesSearch" 
            placeholder="搜索课程代码/名称" 
            prefix-icon="el-icon-search" 
            style="width: 300px;"
            @input="handleCoursesSearch"
          />
          <el-select 
            v-model="coursesSemester" 
            placeholder="选择学期" 
            clearable 
            style="width: 200px; margin-left: 12px;"
            @change="handleCoursesSearch"
          >
            <el-option v-for="semester in semesters" :key="semester.id" :label="semester.name" :value="semester.id" />
          </el-select>
        </div>
        
        <el-table 
          :data="filteredTeacherCourses" 
          style="width: 100%"
          @selection-change="handleCoursesSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="courseCode" label="课程代码" width="120" />
          <el-table-column prop="courseName" label="课程名称" width="200" />
          <el-table-column prop="courseType" label="课程类型" width="100">
            <template #default="scope">
              <el-tag :type="getCourseTypeTag(scope.row.courseType)">
                {{ scope.row.courseType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="credits" label="学分" width="80" />
          <el-table-column prop="hours" label="课时" width="80" />
          <el-table-column prop="semesterName" label="学期" width="150" />
          <el-table-column prop="classroom" label="上课地点" width="120" />
          <el-table-column prop="schedule" label="上课时间" width="180" />
          <el-table-column prop="maxStudents" label="最大选课数" width="120" />
          <el-table-column prop="currentStudents" label="当前选课数" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewCourseStudents(scope.row)"
                icon="el-icon-user"
              >
                查看学生
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="courses-pagination" v-if="filteredTeacherCourses.length > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper, sizes, total"
            :total="filteredTeacherCourses.length"
            :page-size="coursesPageSize"
            :current-page="coursesCurrentPage"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleCoursesSizeChange"
            @current-change="handleCoursesCurrentChange"
          />
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入教师" 
      v-model="importDialogVisible" 
      width="50%"
    >
      <div class="import-content">
        <p class="import-hint">请下载模板，填写完整信息后导入教师数据。</p>
        
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
            <el-table-column prop="teacherId" label="工号" width="120" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="departmentName" label="所属系部" width="150" />
            <el-table-column prop="title" label="职称" width="100" />
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
  name: 'TeacherManagement',
  data() {
    return {
      loading: false,
      teachers: [],
      selectedTeachers: [],
      filteredTeachers: [],
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        keyword: '',
        department: '',
        title: '',
        status: ''
      },
      departments: [],
      semesters: [],
      dialogVisible: false,
      dialogType: 'add',
      teacherForm: {
        teacherId: '',
        name: '',
        gender: '男',
        birthDate: '',
        departmentId: '',
        departmentName: '',
        title: '讲师',
        education: '硕士',
        phone: '',
        email: '',
        joinDate: '',
        status: true,
        password: '',
        graduateSchool: '',
        major: '',
        biography: '',
        office: '',
        officePhone: '',
        mobilePhone: '',
        address: '',
        avatar: ''
      },
      importDialogVisible: false,
      importFileList: [],
      importPreviewData: [],
      coursesDialogVisible: false,
      selectedTeacher: null,
      teacherCourses: [],
      selectedCourses: [],
      coursesSearch: '',
      coursesSemester: '',
      coursesCurrentPage: 1,
      coursesPageSize: 10,
      rules: {
        teacherId: [
          { required: true, message: '请输入教师工号', trigger: 'blur' },
          { min: 3, max: 20, message: '工号长度应为3-20个字符', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '请输入登录用户名', trigger: 'blur' },
          { min: 3, max: 50, message: '用户名长度应为3-50个字符', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入教师姓名', trigger: 'blur' },
          { min: 1, max: 20, message: '姓名长度应为1-20个字符', trigger: 'blur' }
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        departmentId: [
          { required: true, message: '请选择所属系部', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请选择职称', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入电子邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
        ],
        joinDate: [
          { required: true, message: '请选择入职日期', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    // 分页后的教师数据
    paginatedTeachers() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredTeachers.slice(start, end)
    },
    
    // 过滤后的教师课程数据
    filteredTeacherCourses() {
      let courses = [...this.teacherCourses]
      
      // 按学期过滤
      if (this.coursesSemester) {
        courses = courses.filter(c => c.semesterId === this.coursesSemester)
      }
      
      // 按关键词过滤
      if (this.coursesSearch) {
        const keyword = this.coursesSearch.toLowerCase()
        courses = courses.filter(c => 
          c.courseCode.toLowerCase().includes(keyword) ||
          c.courseName.toLowerCase().includes(keyword)
        )
      }
      
      // 分页处理
      const start = (this.coursesCurrentPage - 1) * this.coursesPageSize
      const end = start + this.coursesPageSize
      return courses.slice(start, end)
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
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        // 从后端API获取真实数据
        await Promise.all([
          this.loadTeachers(),
          this.loadDepartments()
        ])
        
        // 初始化过滤后的数据
        this.filteredTeachers = [...this.teachers]
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载教师列表
    async loadTeachers() {
      try {
        const response = await this.$axios.get('/api/teachers')
        if (response.data && response.data.success) {
          // 将后端返回的TeacherDTO数据转换为表格所需的格式
          console.log('Teacher data from backend:', response.data.data?.[0] || {});
          this.teachers = (response.data.data || []).map(teacher => ({
            ...teacher,
            teacherId: teacher.teacherId, // 直接使用teacherId字段
            departmentId: teacher.departmentId,
            departmentName: teacher.department || '未分配',
            gender: teacher.gender || '未设置',
            title: teacher.title || '未设置',
            education: teacher.education || '未设置',
            joinDate: teacher.joinDate || null, // 保持日期格式
            phone: teacher.phone || '',
            email: teacher.email || '',
            status: teacher.enabled !== undefined ? teacher.enabled : true,
            graduateSchool: teacher.graduateSchool || '',
            major: teacher.major || '',
            biography: teacher.biography || '',
            office: teacher.office || '',
            officePhone: teacher.officePhone || '',
            mobilePhone: teacher.mobilePhone || '',
            address: teacher.address || ''
          }))
        }
      } catch (error) {
        console.error('加载教师列表失败:', error)
        throw error
      }
    },
    
    // 加载院系列表
    async loadDepartments() {
      try {
        const response = await this.$axios.get('/api/departments')
        if (response.data && response.data.success) {
          this.departments = response.data.data || []
        }
      } catch (error) {
        console.error('加载院系列表失败:', error)
        throw error
      }
    },
    
    // 搜索教师
    searchTeachers() {
      this.currentPage = 1
      this.filteredTeachers = this.teachers.filter(teacher => {
        const matchesKeyword = !this.searchForm.keyword || 
          teacher.teacherId.toLowerCase().includes(this.searchForm.keyword.toLowerCase()) ||
          teacher.name.toLowerCase().includes(this.searchForm.keyword.toLowerCase())
          
        const matchesDepartment = !this.searchForm.department || 
          teacher.departmentId === this.searchForm.department
          
        const matchesTitle = !this.searchForm.title || 
          teacher.title === this.searchForm.title
          
        const matchesStatus = this.searchForm.status === '' || 
          teacher.status === (this.searchForm.status === 'true')
          
        return matchesKeyword && matchesDepartment && matchesTitle && matchesStatus
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.searchForm = {
        keyword: '',
        department: '',
        title: '',
        status: ''
      }
      this.filteredTeachers = [...this.teachers]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedTeachers = selection
    },
    
    // 处理表格行双击
    handleRowDblclick(row) {
      this.showEditDialog(row)
    },
    
    // 显示新增对话框
    showAddDialog() {
      this.dialogType = 'add'
      this.resetTeacherForm()
      this.dialogVisible = true
    },
    
    // 显示编辑对话框
    showEditDialog(teacher) {
      this.dialogType = 'edit'
      this.teacherForm = { 
        ...teacher,
        teacherId: teacher.teacherId, // 使用正确的字段名
        username: teacher.username
      }
      // 确保日期格式为Date对象，以便日期选择器正确显示
      console.log('Teacher birthDate from API:', teacher.birthDate);
      console.log('Teacher joinDate from API:', teacher.joinDate);
      
      if (teacher.birthDate) {
        this.teacherForm.birthDate = new Date(teacher.birthDate)
      }
      if (teacher.joinDate) {
        this.teacherForm.joinDate = new Date(teacher.joinDate)
      }
      this.dialogVisible = true
    },
    
    // 重置教师表单
    resetTeacherForm() {
      this.teacherForm = {
        teacherId: '',
        username: '',
        name: '',
        gender: '男',
        birthDate: '',
        departmentId: '',
        departmentName: '',
        title: '讲师',
        education: '硕士',
        phone: '',
        email: '',
        joinDate: '',
        status: true,
        password: '',
        graduateSchool: '',
        major: '',
        biography: '',
        office: '',
        officePhone: '',
        mobilePhone: '',
        address: '',
        avatar: ''
      }
      this.$refs.teacherForm && this.$refs.teacherForm.resetFields()
    },
    
    // 提交表单
    async submitForm() {
      this.$refs.teacherForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          // 创建一个帮助函数来格式化日期
          const formatDateForBackend = (dateStr) => {
            if (!dateStr) return null;
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) return null;
            return date.toISOString().split('T')[0]; // YYYY-MM-DD format
          };
          
          // 构建表单数据，只包含后端需要的字段
          const formData = {
            teacherId: this.teacherForm.teacherId,
            username: this.teacherForm.username,
            name: this.teacherForm.name,
            gender: this.teacherForm.gender,
            birthDate: formatDateForBackend(this.teacherForm.birthDate),
            departmentId: this.teacherForm.departmentId,
            title: this.teacherForm.title,
            education: this.teacherForm.education,
            phone: this.teacherForm.phone,
            email: this.teacherForm.email,
            joinDate: formatDateForBackend(this.teacherForm.joinDate),
            enabled: this.teacherForm.status, // 注意：后端使用enabled字段
            graduateSchool: this.teacherForm.graduateSchool,
            major: this.teacherForm.major,
            biography: this.teacherForm.biography,
            office: this.teacherForm.office,
            officePhone: this.teacherForm.officePhone,
            mobilePhone: this.teacherForm.mobilePhone,
            address: this.teacherForm.address,
            avatar: this.teacherForm.avatar
          }
          
          // 只有在新增时才包含密码字段
          if (this.dialogType === 'add' && this.teacherForm.password) {
            formData.password = this.teacherForm.password
          }
          console.log('Form data being sent to backend:', formData);
          
          if (this.dialogType === 'add') {
            // 调用后端API创建教师
            const response = await this.$axios.post('/api/teachers', formData)
            if (response.data && response.data.success) {
              this.$message.success('添加教师成功')
            }
          } else {
            // 调用后端API更新教师 - 只更新特定字段，不包括密码
            const response = await this.$axios.put(`/api/teachers/${this.teacherForm.id}`, formData)
            if (response.data && response.data.success) {
              this.$message.success('更新教师成功')
            }
          }
          
          this.dialogVisible = false
          await this.loadTeachers() // 重新加载数据
          this.searchTeachers() // 刷新搜索结果
        } catch (error) {
          console.error('保存教师信息失败:', error)
          if (error.response) {
            console.error('Server response data:', JSON.stringify(error.response.data, null, 2))
            console.error('Server response status:', error.response.status)
            console.error('Server response headers:', error.response.headers)
            // 显示更详细的错误信息
            const errorMessage = error.response.data?.message || error.response.data?.error || '服务器内部错误'
            this.$message.error(`保存失败: ${errorMessage}`)
          } else {
            this.$message.error('保存失败，请检查信息后重试')
          }
        }
      })
    },
    
    // 删除教师
    deleteTeacher(teacher) {
      this.$confirm(`确定要删除教师 ${teacher.name} (${teacher.teacherId}) 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API删除 - 使用数据库ID而不是工号
          const response = await this.$axios.delete(`/api/teachers/${teacher.id}`)
          if (response.data && response.data.success) {
            this.$message.success('删除成功')
            await this.loadTeachers() // 重新加载数据
            this.searchTeachers() // 刷新搜索结果
          }
        } catch (error) {
          console.error('删除教师失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量删除
    batchDelete() {
      this.$confirm(`确定要删除选中的 ${this.selectedTeachers.length} 位教师吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const teacherIds = this.selectedTeachers.map(t => t.id) // 使用数据库ID而不是工号
          
          // 调用后端API批量删除
          const response = await this.$axios.delete('/api/teachers/batch', {
            data: { ids: teacherIds }
          })
          
          if (response.data && response.data.success) {
            this.$message.success('批量删除成功')
            this.selectedTeachers = []
            await this.loadTeachers() // 重新加载数据
            this.searchTeachers() // 刷新搜索结果
          }
        } catch (error) {
          console.error('批量删除教师失败:', error)
          this.$message.error('批量删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 重置密码
    resetPassword(teacher) {
      this.$confirm(`确定要将教师 ${teacher.name} (${teacher.teacherId}) 的密码重置为默认密码吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API重置密码 - 使用数据库ID而不是工号
          const response = await this.$axios.post(`/api/teachers/${teacher.id}/reset-password`, {})
          
          if (response.data && response.data.success) {
            this.$message.success(`密码已重置为默认值：123456`)
          }
        } catch (error) {
          console.error('重置密码失败:', error)
          this.$message.error('重置密码失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量导入
    batchImport() {
      this.importDialogVisible = true
      this.importFileList = []
      this.importPreviewData = []
    },
    
    // 下载导入模板
    downloadTemplate() {
      // 实际项目中应该调用后端API下载模板
      this.$message.info('导入模板下载功能开发中')
    },
    
    // 处理文件上传
    handleFileChange(file) {
      this.importFileList = [file]
      // 读取文件并预览数据
      this.previewImportData(file)
    },
    
    // 预览导入数据
    previewImportData(file) {
      // 模拟解析Excel文件
      this.importPreviewData = [
        {
          teacherId: 'T2024001',
          name: '赵教授',
          departmentName: '计算机科学与技术',
          title: '教授',
          error: ''
        },
        {
          teacherId: 'T2024002',
          name: '钱副教授',
          departmentName: '电子工程',
          title: '副教授',
          error: ''
        },
        {
          teacherId: 'T2024003',
          name: '孙讲师',
          departmentName: '数学',
          title: '讲师',
          error: '系部不存在'
        }
      ]
    },
    
    // 确认导入
    async confirmImport() {
      try {
        // 调用后端API导入数据
        const formData = new FormData()
        formData.append('file', this.importFileList[0].raw)
        const response = await this.$axios.post('/api/teachers/import', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.data && response.data.success) {
          this.importDialogVisible = false
          await this.loadTeachers() // 重新加载数据
          this.$message.success('批量导入成功')
        }
      } catch (error) {
        console.error('批量导入失败:', error)
        this.$message.error('批量导入失败，请稍后重试')
      }
    },
    
    // 导出教师数据
    exportTeachers() {
      // 调用后端API导出数据
      const selectedIds = this.selectedTeachers.map(t => t.teacherId)
      const url = `/api/teachers/export?ids=${selectedIds.join(',')}`
      window.open(url, '_blank')
    },
    
    // 更新教师状态
    async updateStatus(teacher) {
      try {
        // 调用后端API更新状态 - 使用数据库ID而不是工号
        const response = await this.$axios.patch(`/api/teachers/${teacher.id}`, {
          enabled: teacher.status
        })
        
        if (response.data && response.data.success) {
          this.$message.success('状态更新成功')
        } else {
          // 如果后端返回失败，恢复原状态
          teacher.status = !teacher.status
          this.$message.error(response.data?.message || '更新状态失败')
        }
      } catch (error) {
        console.error('更新状态失败:', error)
        // 恢复原状态
        teacher.status = !teacher.status
        this.$message.error('更新状态失败，请稍后重试')
      }
    },
    
    // 查看教师课程
    async viewCourses(teacher) {
      this.selectedTeacher = teacher
      this.coursesDialogVisible = true
      this.coursesSearch = ''
      this.coursesSemester = ''
      this.coursesCurrentPage = 1
      
      try {
        // 从后端API获取教师授课课程 - 使用数据库ID而不是工号
        const response = await this.$axios.get(`/api/teachers/${teacher.id}/courses`)
        if (response.data && response.data.success) {
          this.teacherCourses = response.data.data || []
        }
      } catch (error) {
        console.error('加载教师课程失败:', error)
        this.$message.error('加载教师课程失败，请稍后重试')
      }
    },
    
    // 处理教师课程搜索
    handleCoursesSearch() {
      this.coursesCurrentPage = 1
    },
    
    // 处理教师课程选择变化
    handleCoursesSelectionChange(selection) {
      this.selectedCourses = selection
    },
    
    // 查看课程学生列表
    viewCourseStudents(course) {
      console.log('查看课程学生:', course)
      this.$router.push(`/admin/courses`)
    },
    
    // 获取职称标签样式
    getTitleTag(title) {
      const tagMap = {
        '教授': 'danger',
        '副教授': 'warning',
        '讲师': 'info',
        '助教': 'success'
      }
      return tagMap[title] || 'info'
    },
    
    // 根据系部ID获取系部名称
    getDepartmentName(deptId) {
      if (!deptId) return '未分配'
      const dept = this.departments.find(d => d.id === deptId)
      return dept ? dept.name : '未分配'
    },
    
    // 获取课程类型标签样式
    getCourseTypeTag(type) {
      const tagMap = {
        '必修课': 'danger',
        '选修课': 'success',
        '公共课': 'info',
        '专业课': 'warning'
      }
      return tagMap[type] || 'info'
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
    
    handleCoursesSizeChange(size) {
      this.coursesPageSize = size
      this.coursesCurrentPage = 1
    },
    
    handleCoursesCurrentChange(current) {
      this.coursesCurrentPage = current
    },
    
  }
}
</script>

<style scoped>
.teacher-management {
  padding: 20px;
}

.management-header {
  margin-bottom: 24px;
  border-radius: 8px;
}

.management-header h1 {
  margin: 0;
  color: var(--text-primary);
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

.teacher-table {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
  object-fit: cover;
}

.empty-state {
  padding: 60px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 教师课程对话框样式 */
.courses-dialog {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
}

.courses-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.courses-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-weight: 600;
}

.courses-stats {
  display: flex;
  gap: 12px;
}

.courses-search {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.courses-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 导入对话框样式 */
.import-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.import-hint {
  color: #606266;
  font-size: 14px;
}

.template-download {
  margin-bottom: 10px;
}

.import-preview h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
  border-left: 4px solid #000000;
  padding-left: 12px;
}

/* 表格样式 - 黑白配色 */
.teacher-table :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.teacher-table :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.teacher-table :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
  font-weight: 600;
}

.teacher-table :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.teacher-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.teacher-table :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.teacher-table :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.teacher-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.teacher-table :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.teacher-table :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.teacher-table :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.teacher-table :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

.teacher-table :deep(.el-tag--success) {
  background-color: #f0f0f0;
}

.teacher-table :deep(.el-tag--warning) {
  background-color: #f0f0f0;
}

.teacher-table :deep(.el-tag--primary) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.teacher-table :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.teacher-table :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.teacher-table :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.teacher-table :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.teacher-table :deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.teacher-table :deep(.el-button--info) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.teacher-table :deep(.el-button--warning) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.error-text {
  color: #f56c6c;
}

.success-text {
  color: #67c23a;
}

.error-message {
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-management {
    padding: 10px;
  }
  
  .search-filters,
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-input,
  .el-select,
  .el-button {
    width: 100% !important;
  }
  
  .pagination {
    justify-content: center;
  }
  
  .courses-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .courses-search {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
}
</style>