<template>
  <div class="student-management">
    <el-card class="management-header">
      <h1>学生管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <div class="search-filters">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="输入学号/姓名搜索" 
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
          v-model="searchForm.major" 
          placeholder="选择专业" 
          clearable 
          style="width: 150px; margin-right: 12px;"
        >
          <el-option v-for="major in majors" :key="major.id" :label="major.name" :value="major.id" />
        </el-select>
        
        <el-select 
          v-model="searchForm.classId" 
          placeholder="选择班级" 
          clearable 
          style="width: 150px; margin-right: 12px;"
        >
          <el-option v-for="cls in classes" :key="cls.id" :label="cls.name" :value="cls.id" />
        </el-select>
        
        <el-button type="primary" @click="searchStudents">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增学生
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportStudents" :disabled="selectedStudents.length === 0">
          <i class="el-icon-download"></i> 导出选中
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedStudents.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
      </div>
      
      <div class="student-table">
        <el-table 
          v-loading="loading" 
          :data="paginatedStudents" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleRowDblclick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="studentId" label="学号" width="120" fixed="left" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              <el-tag :type="formatGenderDisplay(scope.row.gender) === '男' ? 'info' : 'danger'" size="small">
                {{ formatGenderDisplay(scope.row.gender) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="birthDate" label="出生日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.birthDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="departmentName" label="系部" width="120" />
          <el-table-column prop="majorName" label="专业" width="120" />
          <el-table-column prop="className" label="班级" width="120" />
          <el-table-column prop="admissionYear" label="入学年份" width="100" />
          <el-table-column prop="phone" label="联系电话" width="140" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-color="#333333"
                inactive-color="#999999"
                active-text="在读"
                inactive-text="休学"
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
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteStudent(scope.row)"
                icon="el-icon-delete"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination" v-if="filteredStudents.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredStudents.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <div v-if="filteredStudents.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无学生数据" />
      </div>
    </el-card>
    
    <!-- 新增/编辑学生对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增学生' : '编辑学生'" 
      v-model="dialogVisible" 
      width="60%"
    >
      <el-form 
        ref="studentForm" 
        :model="studentForm" 
        :rules="rules" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentId" :disabled="dialogType === 'edit'">
              <el-input v-model="studentForm.studentId" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="studentForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="studentForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="studentForm.birthDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系部" prop="departmentId">
              <el-select 
                v-model="studentForm.departmentId" 
                placeholder="请选择系部" 
                @change="onDepartmentChange"
              >
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="majorId">
              <el-select 
                v-model="studentForm.majorId" 
                placeholder="请选择专业" 
                @change="onMajorChange"
              >
                <el-option v-for="major in filteredMajors" :key="major.id" :label="major.name" :value="major.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="studentForm.classId" placeholder="请选择班级">
                <el-option v-for="cls in filteredClasses" :key="cls.id" :label="cls.name" :value="cls.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入学年份" prop="admissionYear">
              <el-input-number 
                v-model="studentForm.admissionYear" 
                :min="2000" 
                :max="new Date().getFullYear()" 
                :step="1" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="studentForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="studentForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="家庭地址" prop="address">
              <el-input 
                v-model="studentForm.address" 
                type="textarea" 
                :rows="2" 
                placeholder="请输入家庭地址" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="studentForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌" prop="politicalStatus">
              <el-select v-model="studentForm.politicalStatus" placeholder="请选择政治面貌">
                <el-option label="群众" value="群众" />
                <el-option label="团员" value="团员" />
                <el-option label="党员" value="党员" />
                <el-option label="预备党员" value="预备党员" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入学生" 
      v-model="importDialogVisible" 
      width="50%"
    >
      <div class="import-content">
        <p class="import-hint">请下载模板，填写完整信息后导入学生数据。</p>
        
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
            <el-table-column prop="studentId" label="学号" width="120" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="gender" label="性别" width="80" />
            <el-table-column prop="departmentName" label="系部" width="120" />
            <el-table-column prop="majorName" label="专业" width="120" />
            <el-table-column prop="className" label="班级" width="120" />
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
  name: 'StudentManagement',
  data() {
    return {
      loading: false,
      students: [],
      selectedStudents: [],
      filteredStudents: [],
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        keyword: '',
        department: '',
        major: '',
        classId: ''
      },
      departments: [],
      majors: [],
      classes: [],
      dialogVisible: false,
      dialogType: 'add',
      studentForm: {
        studentId: '',
        name: '',
        gender: '男',
        birthDate: '',
        departmentId: '',
        majorId: '',
        classId: '',
        admissionYear: new Date().getFullYear(),
        phone: '',
        email: '',
        address: '',
        idCard: '',
        politicalStatus: '群众',
        status: true
      },
      importDialogVisible: false,
      importFileList: [],
      importPreviewData: [],
      successDialogVisible: false,
      rules: {
        studentId: [
          { required: true, message: '请输入学号', trigger: 'blur' },
          { min: 8, max: 20, message: '学号长度应为8-20个字符', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '姓名长度应为2-20个字符', trigger: 'blur' }
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        birthDate: [
          { required: true, message: '请选择出生日期', trigger: 'change' }
        ],
        departmentId: [
          { required: true, message: '请选择系部', trigger: 'change' }
        ],
        majorId: [
          { required: true, message: '请选择专业', trigger: 'change' }
        ],
        classId: [
          { required: true, message: '请选择班级', trigger: 'change' }
        ],
        admissionYear: [
          { required: true, message: '请输入入学年份', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        idCard: [
          { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/, message: '请输入正确的身份证号码', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 分页后的学生数据
    paginatedStudents() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredStudents.slice(start, end)
    },
    
    // 根据系部过滤专业
    filteredMajors() {
      if (!this.studentForm.departmentId) return this.majors
      return this.majors.filter(major => major.departmentId === this.studentForm.departmentId)
    },
    
    // 根据专业过滤班级
    filteredClasses() {
      if (!this.studentForm.majorId) {
        // 如果没有选择专业，显示所有班级
        return this.classes
      }
      // 如果选择了专业，只显示该专业下的班级
      return this.classes.filter(cls => cls.majorId === this.studentForm.majorId)
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
          this.loadStudents(),
          this.loadDepartments(),
          this.loadMajors(),
          this.loadClasses()
        ])
        
        // 初始化过滤后的数据
        this.filteredStudents = [...this.students]
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载学生列表
    async loadStudents() {
      try {
        const response = await this.$axios.get('/api/students')
        if (response.data && response.data.success) {
          // 将后端返回的enabled字段映射为前端使用的status字段
          this.students = (response.data.data || []).map(student => ({
            ...student,
            status: student.enabled
          }))
        }
      } catch (error) {
        console.error('加载学生列表失败:', error)
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
    
    // 加载专业列表
    async loadMajors() {
      try {
        const response = await this.$axios.get('/api/majors')
        if (response.data && response.data.success) {
          this.majors = response.data.data || []
        }
      } catch (error) {
        console.error('加载专业列表失败:', error)
        throw error
      }
    },
    
    // 加载班级列表
    async loadClasses() {
      try {
        const response = await this.$axios.get('/api/classes')
        if (response.data && response.data.success) {
          this.classes = response.data.data || []
        }
      } catch (error) {
        console.error('加载班级列表失败:', error)
        throw error
      }
    },
    
    // 搜索学生
    searchStudents() {
      this.currentPage = 1
      this.filteredStudents = this.students.filter(student => {
        const matchesKeyword = !this.searchForm.keyword || 
          student.studentId.toLowerCase().includes(this.searchForm.keyword.toLowerCase()) ||
          student.name.toLowerCase().includes(this.searchForm.keyword.toLowerCase())
          
        const matchesDepartment = !this.searchForm.department || 
          student.departmentId === this.searchForm.department
          
        const matchesMajor = !this.searchForm.major || 
          student.majorId === this.searchForm.major
          
        const matchesClass = !this.searchForm.classId || 
          student.classId === this.searchForm.classId
          
        return matchesKeyword && matchesDepartment && matchesMajor && matchesClass
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.searchForm = {
        keyword: '',
        department: '',
        major: '',
        classId: ''
      }
      this.filteredStudents = [...this.students]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedStudents = selection
    },
    
    // 处理表格行双击
    handleRowDblclick(row) {
      this.showEditDialog(row)
    },
    
    // 显示新增对话框
    showAddDialog() {
      this.dialogType = 'add'
      this.resetStudentForm()
      this.dialogVisible = true
    },
    
    // 显示编辑对话框
    showEditDialog(student) {
      this.dialogType = 'edit'
      // 处理日期格式，兼容字符串和Date对象
      let formattedBirthDate = ''
      if (student.birthDate) {
        if (typeof student.birthDate === 'string') {
          // 如果是ISO字符串格式，提取日期部分
          formattedBirthDate = student.birthDate.includes('T') ? student.birthDate.split('T')[0] : student.birthDate
        } else if (student.birthDate instanceof Date) {
          // 如果是Date对象，格式化为YYYY-MM-DD
          formattedBirthDate = student.birthDate.toISOString().split('T')[0]
        } else if (student.birthDate && typeof student.birthDate === 'object') {
          // 如果是其他对象形式的日期（如LocalDate对象序列化后）
          if (student.birthDate.year && student.birthDate.month && student.birthDate.day) {
            // LocalDate格式化为 YYYY-MM-DD
            formattedBirthDate = `${student.birthDate.year}-${String(student.birthDate.month).padStart(2, '0')}-${String(student.birthDate.day).padStart(2, '0')} `
          } else {
            // 尝试直接转换为字符串
            const dateStr = String(student.birthDate)
            if (dateStr && dateStr !== 'null' && dateStr !== 'undefined' && dateStr !== '[object Object]') {
              formattedBirthDate = dateStr.includes('T') ? dateStr.split('T')[0] : dateStr
            }
          }
        } else {
          // 其他情况，尝试转换为字符串并处理
          const dateStr = String(student.birthDate)
          if (dateStr && dateStr !== 'null' && dateStr !== 'undefined') {
            formattedBirthDate = dateStr.includes('T') ? dateStr.split('T')[0] : dateStr
          }
        }
      }
          
      // 处理性别字段，将后端的male/female转换为前端的男/女
      let formattedGender = student.gender
      if (student.gender === 'male') {
        formattedGender = '男'
      } else if (student.gender === 'female') {
        formattedGender = '女'
      }
          
      this.studentForm = {
        ...student,
        gender: formattedGender,
        birthDate: formattedBirthDate
      }
      this.dialogVisible = true
    },
    
    // 重置学生表单
    resetStudentForm() {
      this.studentForm = {
        studentId: '',
        name: '',
        gender: '男',
        birthDate: '',
        departmentId: '',
        majorId: '',
        classId: '',
        admissionYear: new Date().getFullYear(),
        phone: '',
        email: '',
        address: '',
        idCard: '',
        politicalStatus: '群众',
        status: true
      }
      this.$refs.studentForm && this.$refs.studentForm.resetFields()
    },
    
    // 提交表单
    async submitForm() {
      this.$refs.studentForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          // 复制表单数据并确保字段正确
          const formData = { 
            ...this.studentForm,
            // 确保入学年份是数字类型
            admissionYear: Number(this.studentForm.admissionYear)
          }
          
          if (this.dialogType === 'add') {
            // 调用后端API创建学生
            const response = await this.$axios.post('/api/students', formData)
            if (response.data && response.data.success) {
              this.$message.success('添加学生成功')
            }
          } else {
            // 调用后端API更新学生
            const response = await this.$axios.put(`/api/students/${formData.id}`, formData)
            if (response.data && response.data.success) {
              this.$message.success('更新学生成功')
            }
          }
          
          this.dialogVisible = false
          await this.loadStudents() // 重新加载数据
          this.searchStudents() // 刷新搜索结果
        } catch (error) {
          console.error('保存学生信息失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 删除学生
    deleteStudent(student) {
      this.$confirm(`确定要删除学生 ${student.name} (${student.studentId}) 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API删除
          const response = await this.$axios.delete(`/api/students/${student.id}`)
          if (response.data && response.data.success) {
            this.$message.success('删除成功')
            await this.loadStudents() // 重新加载数据
            this.searchStudents() // 刷新搜索结果
          }
        } catch (error) {
          console.error('删除学生失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量删除
    batchDelete() {
      this.$confirm(`确定要删除选中的 ${this.selectedStudents.length} 名学生吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const studentIds = this.selectedStudents.map(s => s.id) // 使用数据库ID而不是学号
          
          // 调用后端API批量删除
          const response = await this.$axios.delete('/api/students', {
            data: { ids: studentIds }
          })
          
          if (response.data && response.data.success) {
            this.$message.success('批量删除成功')
            await this.loadStudents() // 重新加载数据
            this.selectedStudents = []
            this.searchStudents() // 刷新搜索结果
          }
        } catch (error) {
          console.error('批量删除学生失败:', error)
          this.$message.error('批量删除失败，请稍后重试')
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
          studentId: '20230010',
          name: '李四',
          gender: '男',
          departmentName: '计算机科学与技术',
          majorName: '计算机科学',
          className: '计算机科学1班',
          error: ''
        },
        {
          studentId: '20230011',
          name: '王五',
          gender: '女',
          departmentName: '电子工程',
          majorName: '电子信息工程',
          className: '电子信息1班',
          error: '学号已存在'
        },
        {
          studentId: '20230012',
          name: '赵六',
          gender: '男',
          departmentName: '计算机科学与技术',
          majorName: '软件工程',
          className: '软件工程2班',
          error: ''
        }
      ]
    },
    
    // 确认导入
    async confirmImport() {
      try {
        // 实际项目中应该调用后端API导入数据
        // const formData = new FormData()
        // formData.append('file', this.importFileList[0].raw)
        // await axios.post('/api/students/import', formData, {
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
    
    // 导出学生数据
    exportStudents() {
      // 实际项目中应该调用后端API导出数据
      this.$message.info('导出功能开发中')
    },
    
    // 更新学生状态
    async updateStatus(student) {
      try {
        const response = await this.$axios.patch(`/api/students/${student.id}/status`, {
          enabled: student.status
        })
        // 更新本地数据中的状态
        student.enabled = student.status
        this.$message.success('状态更新成功')
      } catch (error) {
        console.error('更新状态失败:', error)
        // 恢复原状态
        student.status = !student.status
        this.$message.error('更新状态失败，请稍后重试')
      }
    },
    
    // 系部变化时重置专业和班级
    onDepartmentChange() {
      this.studentForm.majorId = ''
      this.studentForm.classId = ''
    },
    
    // 专业变化时重置班级
    onMajorChange() {
      this.studentForm.classId = ''
    },
    
    // 显示成功消息
    showSuccessMessage() {
      this.successDialogVisible = true
    },
    
    // 格式化性别显示
    formatGenderDisplay(genderValue) {
      if (genderValue === 'male') {
        return '男'
      } else if (genderValue === 'female') {
        return '女'
      }
      return genderValue // 如果不是male或female，返回原值
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
    

  }
}
</script>

<style scoped>
.student-management {
  padding: 20px;
}

.management-header {
  margin-bottom: 24px;
  border-radius: 8px;
}

.management-header h1 {
  margin: 0;
  color: #303133;
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

.student-table {
  margin-bottom: 20px;
}

.empty-state {
  padding: 60px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
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
  color: var(--text-primary);
  font-size: 16px;
  border-left: 4px solid var(--primary-color);
  padding-left: 12px;
}

.error-text {
  color: #666666;
}

.success-text {
  color: var(--text-primary);
}

.error-message {
  margin-top: 16px;
}

/* 表格样式 - 黑白配色 */
.student-table :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.student-table :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.student-table :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
}

.student-table :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.student-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.student-table :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.student-table :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.student-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.student-table :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.student-table :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.student-table :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.student-table :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.student-table :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.student-table :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.student-table :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.student-table :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-management {
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
}
</style>