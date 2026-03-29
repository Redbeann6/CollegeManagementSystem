<template>
  <div class="department-management">
    <el-card class="management-header">
      <h1>系部管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <div class="search-filters">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="输入系部名称/编号搜索" 
          prefix-icon="el-icon-search" 
          style="width: 250px;"
        />
        
        <el-button type="primary" @click="searchDepartments">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增系部
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportDepartments" :disabled="selectedDepartments.length === 0">
          <i class="el-icon-download"></i> 导出选中
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedDepartments.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
      </div>
      
      <div class="department-table">
        <el-table 
          v-loading="loading" 
          :data="paginatedDepartments" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleRowDblclick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="code" label="系部编号" width="120" fixed="left" />
          <el-table-column prop="name" label="系部名称" width="200">
            <template #default="scope">
              <div class="department-name">
                <i class="el-icon-office-building"></i>
                <span>{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="abbreviation" label="系部简称" width="120" />
          <el-table-column prop="dean" label="系主任" width="100" />
          <el-table-column prop="deputyDean" label="副主任" width="100" />
          <el-table-column prop="teacherCount" label="教师人数" width="100">
            <template #default="scope">
              <el-tag size="small" type="primary">
                {{ scope.row.teacherCount }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="studentCount" label="学生人数" width="100">
            <template #default="scope">
              <el-tag size="small" type="success">
                {{ scope.row.studentCount }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="establishDate" label="成立日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.establishDate) }}
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
          <el-table-column prop="description" label="描述" show-overflow-tooltip width="200" />
          <el-table-column label="操作" width="350" fixed="right">
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
                @click="viewTeachers(scope.row)"
                icon="el-icon-user"
                style="margin-right: 5px;"
              >
                教师列表
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteDepartment(scope.row)"
                icon="el-icon-delete"
                :disabled="scope.row.code === 'DEFAULT'"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination" v-if="filteredDepartments.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredDepartments.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <div v-if="filteredDepartments.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无系部数据" />
      </div>
    </el-card>
    
    <!-- 新增/编辑系部对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增系部' : '编辑系部'" 
      v-model="dialogVisible" 
      width="60%"
    >
      <el-form 
        ref="departmentForm" 
        :model="departmentForm" 
        :rules="rules" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="系部编号" prop="code" :disabled="dialogType === 'edit'">
              <el-input v-model="departmentForm.code" placeholder="请输入系部编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系部名称" prop="name">
              <el-input v-model="departmentForm.name" placeholder="请输入系部名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系部简称" prop="abbreviation">
              <el-input v-model="departmentForm.abbreviation" placeholder="请输入系部简称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系主任" prop="dean">
              <el-input v-model="departmentForm.dean" placeholder="请输入系主任姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副主任" prop="deputyDean">
              <el-input v-model="departmentForm.deputyDean" placeholder="请输入副主任姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立日期" prop="establishDate">
              <el-date-picker
                v-model="departmentForm.establishDate"
                type="date"
                placeholder="选择成立日期"
                value-format="YYYY-MM-DD"
                :picker-options="datePickerOptions"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="departmentForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="departmentForm.email" placeholder="请输入电子邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="办公地点" prop="location">
              <el-input v-model="departmentForm.location" placeholder="请输入办公地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="departmentForm.status"
                active-color="#333333"
                inactive-color="#999999"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider>详细信息</el-divider>
        
        <el-form-item label="系部描述" prop="description">
          <el-input 
            v-model="departmentForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入系部描述信息" 
          />
        </el-form-item>
        
        <el-form-item label="发展历程" prop="history">
          <el-input 
            v-model="departmentForm.history" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入系部发展历程" 
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 系部教师列表对话框 -->
    <el-dialog 
      title="系部教师列表" 
      v-model="teachersDialogVisible" 
      width="80%"
      fullscreen
    >
      <div class="teachers-dialog">
        <div class="teachers-header">
          <h3>{{ selectedDepartment?.name }} ({{ selectedDepartment?.code }}) - 教师列表</h3>
          <div class="teachers-stats">
            <el-tag type="info">总教师数：{{ departmentTeachers.length }}</el-tag>
            <el-tag type="success">教授：{{ professorCount }}</el-tag>
            <el-tag type="warning">副教授：{{ associateProfessorCount }}</el-tag>
            <el-tag type="primary">讲师：{{ lecturerCount }}</el-tag>
            <el-tag type="danger">助教：{{ assistantCount }}</el-tag>
          </div>
        </div>
        
        <div class="teachers-search">
          <el-input 
            v-model="teachersSearch" 
            placeholder="搜索教师工号/姓名" 
            prefix-icon="el-icon-search" 
            style="width: 300px;"
            @input="handleTeachersSearch"
          />
          <el-select 
            v-model="teachersTitle" 
            placeholder="选择职称" 
            clearable 
            style="width: 120px; margin-left: 12px;"
            @change="handleTeachersSearch"
          >
            <el-option label="教授" value="教授" />
            <el-option label="副教授" value="副教授" />
            <el-option label="讲师" value="讲师" />
            <el-option label="助教" value="助教" />
          </el-select>
          <el-select 
            v-model="teachersGender" 
            placeholder="选择性别" 
            clearable 
            style="width: 100px; margin-left: 12px;"
            @change="handleTeachersSearch"
          >
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </div>
        
        <el-table 
          :data="filteredDepartmentTeachers" 
          style="width: 100%"
          @selection-change="handleTeachersSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="teacherId" label="工号" width="120" />
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
                {{ scope.row.gender }}
              </el-tag>
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
          <el-table-column prop="office" label="办公地点" width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewTeacherProfile(scope.row)"
                icon="el-icon-user"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="teachers-pagination" v-if="filteredDepartmentTeachers.length > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper, sizes, total"
            :total="filteredDepartmentTeachers.length"
            :page-size="teachersPageSize"
            :current-page="teachersCurrentPage"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleTeachersSizeChange"
            @current-change="handleTeachersCurrentChange"
          />
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入系部" 
      v-model="importDialogVisible" 
      width="50%"
    >
      <div class="import-content">
        <p class="import-hint">请下载模板，填写完整信息后导入系部数据。</p>
        
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
            <el-table-column prop="code" label="系部编号" width="120" />
            <el-table-column prop="name" label="系部名称" width="150" />
            <el-table-column prop="abbreviation" label="系部简称" width="120" />
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
    
    <!-- 统计图表卡片 -->
    <div class="stats-section" v-if="departments.length > 0">
      <el-card class="stats-card">
        <div class="stats-content">
          <!-- 标题和筛选框区域 - 居中显示 -->
          <div class="stats-header-center">
            <h3>系部统计</h3>
            <el-select v-model="statsType" placeholder="选择统计类型" @change="handleStatsChange">
              <el-option label="按教师人数" value="teacher" />
              <el-option label="按学生人数" value="student" />
            </el-select>
          </div>
          
          <!-- 图表容器 - 居中显示 -->
          <div class="chart-container-center">
            <div id="departmentChart" class="chart"></div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'

export default {
  name: 'DepartmentManagement',
  data() {
    return {
      loading: false,
      departments: [],
      selectedDepartments: [],
      filteredDepartments: [],
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        keyword: ''
      },
      dialogVisible: false,
      dialogType: 'add',
      departmentForm: {
        code: '',
        name: '',
        abbreviation: '',
        dean: '',
        deputyDean: '',
        establishDate: '',
        phone: '',
        email: '',
        location: '',
        status: true,
        description: '',
        history: '',
        teacherCount: 0,
        studentCount: 0
      },
      importDialogVisible: false,
      importFileList: [],
      importPreviewData: [],
      teachersDialogVisible: false,
      selectedDepartment: null,
      departmentTeachers: [],
      selectedTeachers: [],
      teachersSearch: '',
      teachersTitle: '',
      teachersGender: '',
      teachersCurrentPage: 1,
      teachersPageSize: 10,
      statsType: 'teacher',
      chartInstance: null,
      rules: {
        code: [
          { required: true, message: '请输入系部编号', trigger: 'blur' },
          { min: 2, max: 20, message: '系部编号长度应为2-20个字符', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入系部名称', trigger: 'blur' },
          { min: 1, max: 50, message: '系部名称长度应为1-50个字符', trigger: 'blur' }
        ],
        abbreviation: [
          { required: true, message: '请输入系部简称', trigger: 'blur' },
          { min: 1, max: 10, message: '系部简称长度应为1-10个字符', trigger: 'blur' }
        ],
        establishDate: [
          { required: true, message: '请选择成立日期', trigger: 'change' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^0\d{2,3}-\d{7,8}$/, message: '请输入正确的固定电话格式，如：010-12345678', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入电子邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
        ],
        location: [
          { required: true, message: '请输入办公地点', trigger: 'blur' }
        ]
      },
      datePickerOptions: {
        disabledDate: (time) => {
          // 完全不限制日期选择，允许所有日期
          return false;
        },
        shortcuts: [
          {
            text: '今天',
            value: () => new Date()
          },
          {
            text: '一周前',
            value: () => {
              const date = new Date()
              date.setTime(date.getTime() - 3600 * 1000 * 24 * 7)
              return date
            }
          },
          {
            text: '一个月前',
            value: () => {
              const date = new Date()
              date.setMonth(date.getMonth() - 1)
              return date
            }
          }
        ],
        // 添加选择器选项，确保可以显示所有年份
        getYear: (date) => date.getFullYear(),
        getMonth: (date) => date.getMonth(),
        getDate: (date) => date.getDate()
      }
    }
  },
  computed: {
    // 分页后的系部数据
    paginatedDepartments() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredDepartments.slice(start, end)
    },
    
    // 过滤后的系部教师数据
    filteredDepartmentTeachers() {
      let teachers = [...this.departmentTeachers]
      
      // 按职称过滤
      if (this.teachersTitle) {
        teachers = teachers.filter(t => t.title === this.teachersTitle)
      }
      
      // 按性别过滤
      if (this.teachersGender) {
        teachers = teachers.filter(t => t.gender === this.teachersGender)
      }
      
      // 按关键词过滤
      if (this.teachersSearch) {
        const keyword = this.teachersSearch.toLowerCase()
        teachers = teachers.filter(t => 
          t.teacherId.toLowerCase().includes(keyword) ||
          t.name.toLowerCase().includes(keyword)
        )
      }
      
      // 分页处理
      const start = (this.teachersCurrentPage - 1) * this.teachersPageSize
      const end = start + this.teachersPageSize
      return teachers.slice(start, end)
    },
    
    // 是否有错误数据
    hasErrors() {
      return this.importPreviewData.some(item => item.error)
    },
    
    // 教授数量
    professorCount() {
      return this.departmentTeachers.filter(t => t.title === '教授').length
    },
    
    // 副教授数量
    associateProfessorCount() {
      return this.departmentTeachers.filter(t => t.title === '副教授').length
    },
    
    // 讲师数量
    lecturerCount() {
      return this.departmentTeachers.filter(t => t.title === '讲师').length
    },
    
    // 助教数量
    assistantCount() {
      return this.departmentTeachers.filter(t => t.title === '助教').length
    }
  },
  mounted() {
    this.loadData()
    // 确保图表容器在DOM中可用后再初始化图表
    this.ensureChartContainer()
  },
  
  beforeUnmount() {
    // 清理图表资源
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
    // 移除窗口大小改变监听器
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize)
    }
  },
  methods: {
    // 确保图表容器在DOM中可用
    ensureChartContainer() {
      this.$nextTick(() => {
        // 检查统计区域是否可见
        const statsElement = document.querySelector('.stats-section');
        if (statsElement && statsElement.offsetWidth > 0 && this.$refs.chartRef) {
          // 如果图表实例不存在，则初始化
          if (!this.chartInstance) {
            this.initChart();
          } else {
            // 如果图表实例存在，尝试重新渲染
            this.updateChart();
          }
        } else {
          // 如果图表容器还不可用或统计区域不可见，延迟重试
          setTimeout(() => {
            this.ensureChartContainer();
          }, 100);
        }
      });
    },
    
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        // 从后端API获取真实数据
        const response = await this.$axios.get('/api/departments')
        if (response.data && response.data.success) {
          this.departments = response.data.data || []
          this.filteredDepartments = [...this.departments]
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
        // 数据加载完成后更新图表
        this.$nextTick(() => {
          this.updateChart()
        })
      }
    },
    
    // 搜索系部
    async searchDepartments() {
      this.currentPage = 1
      if (!this.searchForm.keyword.trim()) {
        this.filteredDepartments = [...this.departments]
        return
      }
      
      try {
        const response = await this.$axios.get(`/api/departments/search/${this.searchForm.keyword}`)
        if (response.data && response.data.success) {
          this.filteredDepartments = response.data.data || []
        }
      } catch (error) {
        console.error('搜索系部失败:', error)
        this.$message.error('搜索系部失败，请稍后重试')
        // 如果搜索失败，仍显示原始数据
        this.filteredDepartments = this.departments.filter(dept => {
          const keyword = this.searchForm.keyword.toLowerCase()
          return !keyword || 
            dept.code.toLowerCase().includes(keyword) ||
            dept.name.toLowerCase().includes(keyword) ||
            dept.abbreviation.toLowerCase().includes(keyword)
        })
      }
    },
    
    // 重置筛选条件
    resetFilters() {
      this.searchForm = {
        keyword: ''
      }
      this.filteredDepartments = [...this.departments]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedDepartments = selection
    },
    
    // 处理表格行双击
    handleRowDblclick(row) {
      this.showEditDialog(row)
    },
    
    // 显示新增对话框
    showAddDialog() {
      this.dialogType = 'add'
      this.resetDepartmentForm()
      this.dialogVisible = true
    },
    
    // 显示编辑对话框
    showEditDialog(department) {
      this.dialogType = 'edit'
      this.departmentForm = {
        ...department,
        // 确保日期格式正确，转换为日期选择器需要的格式
        establishDate: department.establishDate ? department.establishDate.split('T')[0] : ''
      }
      this.dialogVisible = true
    },
    
    // 重置系部表单
    resetDepartmentForm() {
      this.departmentForm = {
        code: '',
        name: '',
        abbreviation: '',
        dean: '',
        deputyDean: '',
        establishDate: '',
        phone: '',
        email: '',
        location: '',
        status: true,
        description: '',
        history: '',
        teacherCount: 0,
        studentCount: 0
      }
      this.$refs.departmentForm && this.$refs.departmentForm.resetFields()
    },
    
    // 提交表单
    async submitForm() {
      this.$refs.departmentForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          // 构建表单数据
          const formData = {
            ...this.departmentForm
            // establishDate已经是YYYY-MM-DD格式，无需转换
          };
          
          if (this.dialogType === 'add') {
            // 调用后端API创建系部
            const response = await this.$axios.post('/api/departments', formData)
            if (response.data && response.data.success) {
              this.$message.success('添加系部成功')
            }
          } else {
            // 调用后端API更新系部
            const response = await this.$axios.put(`/api/departments/${formData.id}`, formData)
            if (response.data && response.data.success) {
              this.$message.success('更新系部成功')
            }
          }
          
          this.dialogVisible = false
          await this.loadData() // 重新加载数据
          this.searchDepartments() // 刷新搜索结果
          this.updateChart() // 更新图表
        } catch (error) {
          console.error('保存系部信息失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 删除系部
    deleteDepartment(department) {
      if (department.code === 'DEFAULT') {
        this.$message.warning('默认系部不允许删除')
        return
      }
      
      this.$confirm(`确定要删除系部 ${department.name} (${department.code}) 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API删除
          const response = await this.$axios.delete(`/api/departments/${department.id}`)
          if (response.data && response.data.success) {
            this.$message.success('删除成功')
            await this.loadData() // 重新加载数据
            this.searchDepartments() // 刷新搜索结果
            this.updateChart() // 更新图表
          }
        } catch (error) {
          console.error('删除系部失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量删除
    batchDelete() {
      // 检查是否包含默认系部
      const hasDefault = this.selectedDepartments.some(d => d.code === 'DEFAULT')
      if (hasDefault) {
        this.$message.warning('不能删除默认系部')
        return
      }
      
      this.$confirm(`确定要删除选中的 ${this.selectedDepartments.length} 个系部吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const departmentIds = this.selectedDepartments.map(d => d.id)
          
          // 调用后端API批量删除
          const response = await this.$axios.delete('/api/departments/batch', {
            data: { ids: departmentIds }
          })
          
          if (response.data && response.data.success) {
            this.$message.success('批量删除成功')
            this.selectedDepartments = []
            await this.loadData() // 重新加载数据
            this.searchDepartments() // 刷新搜索结果
            this.updateChart() // 更新图表
          }
        } catch (error) {
          console.error('批量删除系部失败:', error)
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
          code: 'CS',
          name: '计算机科学与技术系',
          abbreviation: '计算机系',
          error: ''
        },
        {
          code: 'EE',
          name: '电子工程系',
          abbreviation: '电子系',
          error: ''
        },
        {
          code: 'MA',
          name: '数学系',
          abbreviation: '数学',
          error: '系部编号已存在'
        }
      ]
    },
    
    // 确认导入
    async confirmImport() {
      try {
        // 调用后端API导入数据
        const formData = new FormData()
        formData.append('file', this.importFileList[0].raw)
        const response = await this.$axios.post('/api/departments/import', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.data && response.data.success) {
          this.importDialogVisible = false
          await this.loadData() // 重新加载数据
          this.$message.success('批量导入成功')
        }
      } catch (error) {
        console.error('批量导入失败:', error)
        this.$message.error('批量导入失败，请稍后重试')
      }
    },
    
    // 导出系部数据
    exportDepartments() {
      // 调用后端API导出数据
      const selectedIds = this.selectedDepartments.map(d => d.id)
      const url = `/departments/export?ids=${selectedIds.join(',')}`
      window.open(url, '_blank')
    },
    
    // 更新系部状态
    async updateStatus(department) {
      try {
        // 调用后端API更新状态
        const response = await this.$axios.patch(`/api/departments/${department.id}/status`, {
          status: department.status
        })
        
        if (response.data && response.data.success) {
          this.$message.success('状态更新成功')
        }
      } catch (error) {
        console.error('更新状态失败:', error)
        // 恢复原状态
        department.status = !department.status
        this.$message.error('更新状态失败，请稍后重试')
      }
    },
    
    // 查看系部教师列表
    async viewTeachers(department) {
      this.selectedDepartment = department
      this.teachersDialogVisible = true
      this.teachersSearch = ''
      this.teachersTitle = ''
      this.teachersGender = ''
      this.teachersCurrentPage = 1
      
      try {
        // 从后端API获取系部教师数据
        const response = await this.$axios.get(`/api/departments/${department.id}/teachers`)
        if (response.data && response.data.success) {
          this.departmentTeachers = response.data.data || []
        } else {
          // 如果后端没有提供此API，使用模拟数据
          this.departmentTeachers = [
            {
              id: 1,
              teacherId: 'T001',
              name: '张教授',
              gender: '男',
              title: '教授',
              education: '博士',
              phone: '13800138001',
              email: 'zhang@college.edu',
              joinDate: '2020-03-01',
              office: '计算机楼501'
            },
            {
              id: 2,
              teacherId: 'T002',
              name: '李副教授',
              gender: '女',
              title: '副教授',
              education: '博士',
              phone: '13800138002',
              email: 'li@college.edu',
              joinDate: '2021-05-15',
              office: '计算机楼502'
            }
          ]
        }
      } catch (error) {
        console.error('加载系部教师失败:', error)
        // 如果API调用失败，使用模拟数据
        this.departmentTeachers = [
          {
            id: 1,
            teacherId: 'T001',
            name: '张教授',
            gender: '男',
            title: '教授',
            education: '博士',
            phone: '13800138001',
            email: 'zhang@college.edu',
            joinDate: '2020-03-01',
            office: '计算机楼501'
          },
          {
            id: 2,
            teacherId: 'T002',
            name: '李副教授',
            gender: '女',
            title: '副教授',
            education: '博士',
            phone: '13800138002',
            email: 'li@college.edu',
            joinDate: '2021-05-15',
            office: '计算机楼502'
          }
        ]
        this.$message.error('加载系部教师失败，请稍后重试')
      }
    },
    
    // 处理教师搜索
    handleTeachersSearch() {
      this.teachersCurrentPage = 1
    },
    
    // 处理教师选择变化
    handleTeachersSelectionChange(selection) {
      this.selectedTeachers = selection
    },
    
    // 查看系部统计信息
    viewDepartmentStats(department) {
      console.log('查看系部统计信息:', department)
      // 可以跳转到系部详情页面或在对话框中显示详细统计信息
      this.selectedDepartment = department
      this.scrollToStatsSection()
    },
    
    // 滚动到统计区域
    scrollToStatsSection() {
      this.$nextTick(() => {
        const statsElement = document.querySelector('.stats-section')
        if (statsElement) {
          statsElement.scrollIntoView({ behavior: 'smooth' })
        }
      })
    },
    
    // 查看教师详情
    viewTeacherProfile(teacher) {
      console.log('查看教师详情:', teacher)
      // 实际项目中应该跳转到教师详情页或打开详情对话框
      this.$router.push(`/admin/teachers`)
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
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      
      // 如果已经是格式化的字符串（如 'YYYY-MM-DD'），直接返回
      if (typeof dateString === 'string' && /^\d{4}-\d{2}-\d{2}/.test(dateString)) {
        return dateString
      }
      
      // 如果是Date对象或可以转换为Date的值
      let date
      if (dateString instanceof Date) {
        date = dateString
      } else if (typeof dateString === 'string' || typeof dateString === 'number') {
        date = new Date(dateString)
      } else {
        return ''
      }
      
      // 检查日期是否有效
      if (isNaN(date.getTime())) {
        return ''
      }
      
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
    
    handleTeachersSizeChange(size) {
      this.teachersPageSize = size
      this.teachersCurrentPage = 1
    },
    
    handleTeachersCurrentChange(current) {
      this.teachersCurrentPage = current
    },
    
    // 初始化图表
    initChart() {
      try {
        // 使用ID选择器获取图表容器
        const chartElement = document.getElementById('departmentChart');
        if (chartElement) {
          // 确保元素有正确的尺寸
          chartElement.style.width = '100%';
          chartElement.style.height = '400px';
          
          // 销毁之前的实例（如果存在）
          if (this.chartInstance) {
            this.chartInstance.dispose();
          }
          
          // 初始化图表
          this.chartInstance = echarts.init(chartElement);
          this.updateChart();
          
          // 添加窗口大小改变监听器
          this.handleResize = () => {
            if (this.chartInstance) {
              this.chartInstance.resize();
            }
          };
          window.addEventListener('resize', this.handleResize);
        }
      } catch (error) {
        console.error('初始化图表失败:', error);
        // 尝试重新初始化
        setTimeout(() => {
          this.initChart();
        }, 200);
      }
    },
    
    // 更新图表
    updateChart() {
      // 如果图表实例不存在，尝试初始化
      if (!this.chartInstance) {
        this.$nextTick(() => {
          this.initChart();
        });
        return;
      }
      
      // 准备图表数据
      const chartData = this.departments.map(dept => ({
        name: dept.name || '未知系部',
        value: this.statsType === 'teacher' ? (dept.teacherCount || 0) : (dept.studentCount || 0)
      }));
      
      // 创建一个简单的柱状图配置
      const option = {
        title: {
          text: this.statsType === 'teacher' ? '各系部教师人数统计' : '各系部学生人数统计',
          left: 'center',
          top: '5%',
          textStyle: {
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '5%',
          right: '5%',
          bottom: '30%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: chartData.map(item => item.name),
          axisLabel: {
            rotate: 60,
            fontSize: 9,
            formatter: (value) => {
              // 限制标签长度，避免重叠
              return value.length > 12 ? value.substring(0, 12) + '...' : value;
            }
          },
          axisLine: {
            show: true
          },
          axisTick: {
            show: true
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: true
          },
          axisTick: {
            show: true
          },
          splitLine: {
            show: true
          }
        },
        series: [
          {
            name: '人数',
            type: 'bar',
            data: chartData.map(item => item.value),
            color: this.statsType === 'teacher' ? '#409EFF' : '#67C23A',
            barWidth: '45%'
          }
        ]
      };
      
      try {
        // 先清空图表
        this.chartInstance.clear();
        
        // 设置图表选项
        this.chartInstance.setOption(option);
      } catch (error) {
        console.error('更新图表失败:', error);
        // 如果更新失败，尝试重新初始化
        this.initChart();
      }
    },
    
    // 处理窗口大小变化
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },
    
    // 切换统计类型
    handleStatsChange() {
      this.updateChart()
    }
  }
}
</script>

<style scoped>
.department-management {
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
  margin-bottom: 20px;
  border-radius: 8px;
}

.search-filters {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-filters .el-input {
  margin-right: 0;
}



.action-buttons {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.department-table {
  margin-bottom: 20px;
}

.department-name {
  display: flex;
  align-items: center;
}

.department-name i {
  margin-right: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.empty-state {
  padding: 60px 0;
}

.dialog-footer {
  text-align: right;
}

.import-content {
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.import-hint {
  margin-bottom: 0;
  color: #606266;
  font-size: 14px;
}

.template-download {
  margin-bottom: 0;
  text-align: center;
}

.import-upload {
  margin-bottom: 0;
}

.import-preview {
  margin-top: 0;
}

.import-preview h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.error-text {
  color: #666666;
}

.success-text {
  color: #303133;
}

.error-message {
  margin-top: 16px;
}

.stats-section {
  margin-top: 20px;
  position: relative;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-header-center {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  width: 100%;
}

.stats-header-center h3 {
  margin: 0 20px 0 0;
  font-size: 18px;
  font-weight: bold;
}

.stats-header-center .el-select {
  width: 150px;
}

.chart-container-center {
  width: 80%;
  height: 300px;
  margin-top: 20px;
  overflow: visible;
  display: flex;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}

/* 教师列表对话框样式 */
.teachers-dialog {
  padding: 20px 0;
}

.teachers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.teachers-stats {
  display: flex;
  gap: 10px;
}

.teachers-search {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
}

.teachers-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 表格头部样式 */
.department-table :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.department-table :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
}

.department-table :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.department-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.department-table :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.department-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.department-table :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.department-table :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.department-table :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.department-table :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.department-table :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.department-table :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.department-table :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.department-table :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}
</style>