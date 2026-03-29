<template>
  <el-card class="management-header">
    <h1>请假管理</h1>
  </el-card>
  
  <el-card class="hover-card">
    <!-- 筛选和搜索区域 -->
    <div class="search-filters">
      <el-row :gutter="20">
        <el-select v-model="filterOptions.status" placeholder="申请状态" style="width: 150px;">
          <el-option label="全部" value="" />
          <el-option label="待审批" value="PENDING" />
          <el-option label="已批准" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已撤销" value="CANCELLED" />
        </el-select>
        <el-select v-model="filterOptions.departmentId" placeholder="所属系部" style="width: 150px;">
          <el-option label="全部" value="" />
          <el-option 
            v-for="dept in departments" 
            :key="dept.id" 
            :label="dept.name" 
            :value="dept.id" 
          />
        </el-select>
        <el-select v-model="filterOptions.type" placeholder="请假类型" style="width: 120px;">
          <el-option label="全部" value="" />
          <el-option label="病假" value="SICK" />
          <el-option label="事假" value="PERSONAL" />
          <el-option label="公假" value="OFFICIAL" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 260px;"
        />
        <el-select v-model="filterOptions.approverId" placeholder="审批教师" style="width: 150px;">
          <el-option label="全部" value="" />
          <el-option 
            v-for="teacher in teachers" 
            :key="teacher.id" 
            :label="teacher.name" 
            :value="teacher.id" 
          />
        </el-select>
        <el-input 
          v-model="filterOptions.studentName" 
          placeholder="搜索学生姓名/学号" 
          prefix-icon="el-icon-search" 
          style="width: 240px;"
          @keyup.enter="searchLeaves"
        />
        <el-button type="primary" @click="searchLeaves">
          <i class="el-icon-search"></i> 搜索
        </el-button>
        <el-button @click="resetFilters">
          <i class="el-icon-refresh"></i> 重置
        </el-button>
      </el-row>
    </div>
      
      <!-- 功能按钮区域 -->
      <div class="action-buttons">
        <el-button type="primary" @click="batchProcess('APPROVED')" :disabled="selectedLeaves.length === 0 || !canBatchProcess">
          <i class="el-icon-check"></i> 批量批准
        </el-button>
        <el-button type="danger" @click="batchProcess('REJECTED')" :disabled="selectedLeaves.length === 0 || !canBatchProcess">
          <i class="el-icon-close"></i> 批量拒绝
        </el-button>
        <el-button type="primary" @click="exportData">
          <i class="el-icon-download"></i> 导出数据
        </el-button>
        <el-button @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新数据
        </el-button>
        <el-button type="info" @click="showStatistics">
          <i class="el-icon-data-line"></i> 请假统计
        </el-button>
        <el-tag type="warning" effect="dark" class="pending-count">待审批: {{ pendingCount }}</el-tag>
      </div>
      
      <!-- 请假列表表格 -->
      <div class="table-section">
        <el-table 
          v-loading="loading" 
          :data="paginatedLeaves" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
          stripe
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="申请ID" width="100" />
          <el-table-column label="学生信息" min-width="160">
            <template #default="scope">
              <div>
                <div><strong>姓名:</strong> {{ scope.row.studentName }}</div>
                <div><strong>学号:</strong> {{ scope.row.studentId }}</div>
                <div><strong>专业:</strong> {{ scope.row.major }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="请假类型" width="100">
            <template #default="scope">
              <el-tag :type="getLeaveTypeTag(scope.row.type)">
                {{ getLeaveTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="请假时间" min-width="180">
            <template #default="scope">
              {{ formatDate(scope.row.startDate) }} 至 {{ formatDate(scope.row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="days" label="请假天数" width="100" />
          <el-table-column prop="reason" label="请假原因" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <div class="reason-container" @click="showFullReason(scope.row.reason)">
                {{ scope.row.reason }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getLeaveStatusTag(scope.row.status)">
                {{ getLeaveStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.submitTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="approverName" label="审批人" width="120" show-overflow-tooltip />
          <el-table-column prop="processTime" label="处理时间" width="180">
            <template #default="scope">
              {{ scope.row.processTime ? formatDateTime(scope.row.processTime) : '--' }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="处理意见" min-width="150" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.remark || '--' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewLeaveDetail(scope.row)"
                style="margin-right: 5px;"
              >
                查看
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                type="success" 
                size="small" 
                @click="approveLeave(scope.row)"
                style="margin-right: 5px;"
              >
                批准
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PENDING'" 
                type="danger" 
                size="small" 
                @click="rejectLeave(scope.row)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页控件 -->
      <div class="pagination-section">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredLeaves.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 请假详情对话框 -->
    <el-dialog 
      title="请假详情" 
      v-model="detailDialogVisible" 
      width="70%"
    >
      <div class="leave-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="100px">
              <el-form-item label="申请ID">
                <span>{{ currentLeave.id }}</span>
              </el-form-item>
              <el-form-item label="学生姓名">
                <span>{{ currentLeave.studentName }}</span>
              </el-form-item>
              <el-form-item label="学号">
                <span>{{ currentLeave.studentId }}</span>
              </el-form-item>
              <el-form-item label="院系">
                <span>{{ getDepartmentName(currentLeave.departmentId) }}</span>
              </el-form-item>
              <el-form-item label="专业">
                <span>{{ currentLeave.major }}</span>
              </el-form-item>
              <el-form-item label="年级">
                <span>{{ currentLeave.grade }}</span>
              </el-form-item>
              <el-form-item label="联系方式">
                <span>{{ currentLeave.contactInfo || '--' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="100px">
              <el-form-item label="请假类型">
                <el-tag :type="getLeaveTypeTag(currentLeave.type)">
                  {{ getLeaveTypeText(currentLeave.type) }}
                </el-tag>
              </el-form-item>
              <el-form-item label="开始日期">
                <span>{{ formatDate(currentLeave.startDate) }}</span>
              </el-form-item>
              <el-form-item label="结束日期">
                <span>{{ formatDate(currentLeave.endDate) }}</span>
              </el-form-item>
              <el-form-item label="请假天数">
                <span>{{ currentLeave.days }}</span>
              </el-form-item>
              <el-form-item label="提交时间">
                <span>{{ formatDateTime(currentLeave.submitTime) }}</span>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="getLeaveStatusTag(currentLeave.status)">
                  {{ getLeaveStatusText(currentLeave.status) }}
                </el-tag>
              </el-form-item>
              <el-form-item label="审批人">
                <span>{{ currentLeave.approverName || '--' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <div class="detail-section">
          <h3>请假原因</h3>
          <div class="reason-content">{{ currentLeave.reason }}</div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="detail-section" v-if="currentLeave.status !== 'PENDING'">
          <h3>处理记录</h3>
          <el-form label-width="100px">
            <el-form-item label="处理时间">
              <span>{{ formatDateTime(currentLeave.processTime) }}</span>
            </el-form-item>
            <el-form-item label="处理结果">
              <el-tag :type="getLeaveStatusTag(currentLeave.status)">
                {{ getLeaveStatusText(currentLeave.status) }}
              </el-tag>
            </el-form-item>
            <el-form-item label="处理意见">
              <div class="remark-content">{{ currentLeave.remark || '无' }}</div>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 审批操作区域（仅对未处理的申请显示） -->
        <div class="approval-section" v-if="currentLeave.status === 'PENDING'">
          <el-divider></el-divider>
          <h3>审批操作</h3>
          <el-form 
            ref="approvalForm" 
            :model="approvalForm" 
            label-width="100px"
          >
            <el-form-item label="处理意见">
              <el-input 
                v-model="approvalForm.remark" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入处理意见（可选）" 
              />
            </el-form-item>
          </el-form>
          <div class="approval-buttons">
            <el-button @click="cancelApproval">取消</el-button>
            <el-button type="success" @click="confirmApprove">批准</el-button>
            <el-button type="danger" @click="confirmReject">拒绝</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量处理对话框 -->
    <el-dialog 
      :title="batchProcessType === 'APPROVED' ? '批量批准' : '批量拒绝'" 
      v-model="batchProcessVisible" 
      width="50%"
    >
      <div class="batch-process">
        <p>您确定要{{ batchProcessType === 'APPROVED' ? '批准' : '拒绝' }}选中的 <strong>{{ selectedLeaves.length }}</strong> 条请假申请吗？</p>
        <el-form 
          ref="batchForm" 
          :model="batchProcessForm" 
          label-width="100px"
        >
          <el-form-item label="处理意见">
            <el-input 
              v-model="batchProcessForm.remark" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入处理意见（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="batchProcessVisible = false">取消</el-button>
        <el-button 
          :type="batchProcessType === 'APPROVED' ? 'success' : 'danger'" 
          @click="confirmBatchProcess"
        >
          确定
        </el-button>
      </div>
    </el-dialog>
    
    <!-- 请假统计对话框 -->
    <el-dialog 
      title="请假统计分析" 
      v-model="statisticsDialogVisible" 
      width="80%"
      :fullscreen="true"
    >
      <div class="statistics-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20" style="margin-bottom: 30px;">
          <el-col :span="6">
            <el-card class="statistics-card">
              <div class="card-content">
                <div class="card-value">{{ totalLeaves }}</div>
                <div class="card-label">总申请数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="statistics-card" type="warning">
              <div class="card-content">
                <div class="card-value">{{ pendingCount }}</div>
                <div class="card-label">待审批</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="statistics-card" type="success">
              <div class="card-content">
                <div class="card-value">{{ approvedCount }}</div>
                <div class="card-label">已批准</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="statistics-card" type="danger">
              <div class="card-content">
                <div class="card-value">{{ rejectedCount }}</div>
                <div class="card-label">已拒绝</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 图表区域 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <div slot="header" class="card-header">
                <span>请假类型分布</span>
              </div>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示请假类型分布图表" />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <div slot="header" class="card-header">
                <span>各系部请假分布</span>
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
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示系部请假分布图表" />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <div slot="header" class="card-header">
                <span>请假趋势分析</span>
                <el-select v-model="chartOptions.trendRange" placeholder="时间范围" style="width: 200px; float: right;">
                  <el-option label="近7天" value="7" />
                  <el-option label="近30天" value="30" />
                  <el-option label="近90天" value="90" />
                  <el-option label="本学期" value="current" />
                </el-select>
              </div>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示请假趋势图表" />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <div slot="header" class="card-header">
                <span>请假时长统计</span>
                <el-select v-model="chartOptions.leaveTypeFilter" placeholder="请假类型" style="width: 200px; float: right;">
                  <el-option label="全部类型" value="all" />
                  <el-option label="病假" value="SICK" />
                  <el-option label="事假" value="PERSONAL" />
                  <el-option label="公假" value="OFFICIAL" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </div>
              <div class="chart-container">
                <!-- 这里可以放置图表组件，如 ECharts -->
                <div class="chart-placeholder">
                  <el-empty description="暂无图表数据，实际项目中这里将显示请假时长统计图表" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
    
    <!-- 查看完整原因对话框 -->
    <el-dialog 
      title="完整请假原因" 
      v-model="fullReasonVisible" 
      width="60%"
    >
      <div class="full-reason-content">
        <pre>{{ fullReason }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'LeaveManagement',
  data() {
    return {
      // 加载状态
      loading: false,
      
      // 筛选条件
      filterOptions: {
        semesterId: '',
        status: '',
        type: '',
        departmentId: '',
        studentName: '',
        studentId: ''
      },
      dateRange: [],
      
      // 数据列表
      leaves: [],
      filteredLeaves: [],
      
      // 分页相关
      currentPage: 1,
      pageSize: 20,
      
      // 选中的记录
      selectedLeaves: [],
      
      // 对话框相关
      detailDialogVisible: false,
      batchProcessVisible: false,
      batchProcessType: '', // 'APPROVED' 或 'REJECTED'
      statisticsDialogVisible: false,
      fullReasonVisible: false,
      
      // 当前查看的请假记录
      currentLeave: {},
      
      // 审批表单
      approvalForm: {
        remark: ''
      },
      
      // 批量处理表单
      batchProcessForm: {
        remark: ''
      },
      
      // 完整原因
      fullReason: '',
      
      // 图表选项
      chartOptions: {
        departmentFilter: 'all',
        trendRange: '30',
        leaveTypeFilter: 'all'
      },
      
      // 辅助数据
      semesters: [],
      departments: [],
      students: []
    }
  },
  computed: {
    // 分页后的数据
    paginatedLeaves() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredLeaves.slice(start, end)
    },
    
    // 统计数据
    totalLeaves() {
      return this.leaves.length
    },
    pendingCount() {
      return this.leaves.filter(l => l.status === 'PENDING').length
    },
    approvedCount() {
      return this.leaves.filter(l => l.status === 'APPROVED').length
    },
    rejectedCount() {
      return this.leaves.filter(l => l.status === 'REJECTED').length
    },
    
    // 是否可以批量处理
    canBatchProcess() {
      return this.selectedLeaves.every(l => l.status === 'PENDING')
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
        // 同时加载所有必要数据
        await Promise.all([
          this.loadSemesters(),
          this.loadDepartments(),
          this.loadStudents(),
          this.loadLeaves()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载学期数据
    async loadSemesters() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/admin/semesters')
      // this.semesters = response.data
      
      // 使用模拟数据
      const now = new Date()
      const year = now.getFullYear()
      this.semesters = [
        { id: 1, name: `${year}-${year + 1}学年第一学期`, isActive: true },
        { id: 2, name: `${year - 1}-${year}学年第二学期`, isActive: false },
        { id: 3, name: `${year - 1}-${year}学年第一学期`, isActive: false }
      ]
    },
    
    // 加载系部数据
    async loadDepartments() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/admin/departments')
      // this.departments = response.data
      
      // 使用模拟数据
      this.departments = [
        { id: 1, name: '计算机学院' },
        { id: 2, name: '电子工程学院' },
        { id: 3, name: '机械工程学院' },
        { id: 4, name: '管理学院' },
        { id: 5, name: '文学院' }
      ]
    },
    
    // 加载学生数据
    async loadStudents() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/admin/students')
      // this.students = response.data
      
      // 使用模拟数据
      this.students = [
        { id: 1, name: '张三', studentId: '2020001', major: '计算机科学与技术', grade: '2020', departmentId: 1 },
        { id: 2, name: '李四', studentId: '2020002', major: '软件工程', grade: '2020', departmentId: 1 },
        { id: 3, name: '王五', studentId: '2020003', major: '网络工程', grade: '2020', departmentId: 1 },
        { id: 4, name: '赵六', studentId: '2021001', major: '计算机科学与技术', grade: '2021', departmentId: 1 },
        { id: 5, name: '钱七', studentId: '2021002', major: '软件工程', grade: '2021', departmentId: 1 },
        { id: 6, name: '孙八', studentId: '2022001', major: '计算机科学与技术', grade: '2022', departmentId: 1 },
        { id: 7, name: '周九', studentId: '2022002', major: '软件工程', grade: '2022', departmentId: 1 },
        { id: 8, name: '吴十', studentId: '2023001', major: '人工智能', grade: '2023', departmentId: 1 },
        { id: 9, name: '郑十一', studentId: '2023002', major: '电子信息工程', grade: '2023', departmentId: 2 },
        { id: 10, name: '王十二', studentId: '2023003', major: '机械设计制造及其自动化', grade: '2023', departmentId: 3 }
      ]
    },
    
    // 加载请假数据
    async loadLeaves() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/admin/leaves')
      // this.leaves = response.data
      
      // 使用模拟数据
      this.leaves = this.generateMockLeaves()
      this.filteredLeaves = [...this.leaves]
    },
    
    // 刷新数据
    refreshData() {
      this.loadLeaves()
      this.selectedLeaves = []
    },
    
    // 搜索请假数据
    searchLeaves() {
      this.currentPage = 1
      
      this.filteredLeaves = this.leaves.filter(leave => {
        // 按学期筛选
        if (this.filterOptions.semesterId && leave.semesterId !== parseInt(this.filterOptions.semesterId)) {
          return false
        }
        
        // 按状态筛选
        if (this.filterOptions.status && leave.status !== this.filterOptions.status) {
          return false
        }
        
        // 按类型筛选
        if (this.filterOptions.type && leave.type !== this.filterOptions.type) {
          return false
        }
        
        // 按系部筛选
        if (this.filterOptions.departmentId && leave.departmentId !== parseInt(this.filterOptions.departmentId)) {
          return false
        }
        
        // 按学生姓名筛选
        if (this.filterOptions.studentName) {
          const keyword = this.filterOptions.studentName.toLowerCase()
          if (!leave.studentName.toLowerCase().includes(keyword)) {
            return false
          }
        }
        
        // 按学生学号筛选
        if (this.filterOptions.studentId) {
          const keyword = this.filterOptions.studentId.toLowerCase()
          if (!leave.studentId.toLowerCase().includes(keyword)) {
            return false
          }
        }
        
        // 按日期范围筛选
        if (this.dateRange && this.dateRange.length === 2) {
          const [startDate, endDate] = this.dateRange
          // 检查请假时间段是否与搜索范围有重叠
          const leaveStart = new Date(leave.startDate)
          const leaveEnd = new Date(leave.endDate)
          const searchStart = new Date(startDate)
          const searchEnd = new Date(endDate)
          
          // 如果请假的开始日期晚于搜索的结束日期，或者请假的结束日期早于搜索的开始日期，则无重叠
          if (leaveStart > searchEnd || leaveEnd < searchStart) {
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
        status: '',
        type: '',
        departmentId: '',
        studentName: '',
        studentId: ''
      }
      this.dateRange = []
      this.filteredLeaves = [...this.leaves]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedLeaves = selection
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.currentPage = 1
    },
    
    handleCurrentChange(newCurrent) {
      this.currentPage = newCurrent
    },
    
    // 查看请假详情
    viewLeaveDetail(leave) {
      this.currentLeave = { ...leave }
      this.approvalForm = { remark: '' }
      this.detailDialogVisible = true
    },
    
    // 显示完整原因
    showFullReason(reason) {
      this.fullReason = reason
      this.fullReasonVisible = true
    },
    
    // 批准请假
    approveLeave(leave) {
      this.currentLeave = { ...leave }
      this.approvalForm = { remark: '' }
      this.detailDialogVisible = true
    },
    
    // 拒绝请假
    rejectLeave(leave) {
      this.currentLeave = { ...leave }
      this.approvalForm = { remark: '' }
      this.detailDialogVisible = true
    },
    
    // 取消审批
    cancelApproval() {
      this.approvalForm = { remark: '' }
      this.detailDialogVisible = false
    },
    
    // 确认批准
    async confirmApprove() {
      try {
        // 实际项目中应该调用后端API
        // await axios.post(`/api/admin/leaves/${this.currentLeave.id}/approve`, {
        //   remark: this.approvalForm.remark
        // })
        
        // 模拟更新
        const index = this.leaves.findIndex(l => l.id === this.currentLeave.id)
        if (index !== -1) {
          this.leaves[index] = {
            ...this.leaves[index],
            status: 'APPROVED',
            remark: this.approvalForm.remark,
            approverName: '管理员', // 实际项目中应使用当前登录用户
            processTime: new Date().toISOString()
          }
        }
        
        this.detailDialogVisible = false
        this.searchLeaves()
        this.$message.success('请假申请已批准')
      } catch (error) {
        console.error('批准请假失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    
    // 确认拒绝
    async confirmReject() {
      try {
        // 实际项目中应该调用后端API
        // await axios.post(`/api/admin/leaves/${this.currentLeave.id}/reject`, {
        //   remark: this.approvalForm.remark
        // })
        
        // 模拟更新
        const index = this.leaves.findIndex(l => l.id === this.currentLeave.id)
        if (index !== -1) {
          this.leaves[index] = {
            ...this.leaves[index],
            status: 'REJECTED',
            remark: this.approvalForm.remark,
            approverName: '管理员', // 实际项目中应使用当前登录用户
            processTime: new Date().toISOString()
          }
        }
        
        this.detailDialogVisible = false
        this.searchLeaves()
        this.$message.success('请假申请已拒绝')
      } catch (error) {
        console.error('拒绝请假失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    
    // 批量处理
    batchProcess(type) {
      if (this.selectedLeaves.length === 0) {
        this.$message.warning('请选择要处理的请假申请')
        return
      }
      
      if (!this.canBatchProcess) {
        this.$message.warning('只能批量处理待审批的请假申请')
        return
      }
      
      this.batchProcessType = type
      this.batchProcessForm = { remark: '' }
      this.batchProcessVisible = true
    },
    
    // 确认批量处理
    async confirmBatchProcess() {
      try {
        // 实际项目中应该调用后端API
        // const ids = this.selectedLeaves.map(l => l.id)
        // await axios.post('/api/admin/leaves/batch-process', {
        //   ids,
        //   status: this.batchProcessType,
        //   remark: this.batchProcessForm.remark
        // })
        
        // 模拟批量更新
        const ids = this.selectedLeaves.map(l => l.id)
        this.leaves = this.leaves.map(leave => {
          if (ids.includes(leave.id)) {
            return {
              ...leave,
              status: this.batchProcessType,
              remark: this.batchProcessForm.remark,
              approverName: '管理员', // 实际项目中应使用当前登录用户
              processTime: new Date().toISOString()
            }
          }
          return leave
        })
        
        this.batchProcessVisible = false
        this.selectedLeaves = []
        this.searchLeaves()
        this.$message.success(`已${this.batchProcessType === 'APPROVED' ? '批准' : '拒绝'}选中的请假申请`)
      } catch (error) {
        console.error('批量处理失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    
    // 导出数据
    async exportData() {
      // 实际项目中应该调用后端API
      // const params = { ...this.filterOptions }
      // if (this.dateRange && this.dateRange.length === 2) {
      //   params.startDate = this.dateRange[0]
      //   params.endDate = this.dateRange[1]
      // }
      // window.location.href = `/admin/leaves/export?${new URLSearchParams(params).toString()}`
      
      this.$message.info('正在导出数据...')
      
      // 模拟导出过程
      setTimeout(() => {
        this.$message.success('数据导出成功')
      }, 2000)
    },
    
    // 显示统计对话框
    showStatistics() {
      this.statisticsDialogVisible = true
    },
    
    // 获取系部名称
    getDepartmentName(departmentId) {
      const department = this.departments.find(d => d.id === departmentId)
      return department ? department.name : '--'
    },
    
    // 获取请假类型标签样式
    getLeaveTypeTag(type) {
      const tagMap = {
        'SICK': 'warning',
        'PERSONAL': 'primary',
        'OFFICIAL': 'success',
        'OTHER': 'info'
      }
      return tagMap[type] || 'info'
    },
    
    // 获取请假类型文本
    getLeaveTypeText(type) {
      const textMap = {
        'SICK': '病假',
        'PERSONAL': '事假',
        'OFFICIAL': '公假',
        'OTHER': '其他'
      }
      return textMap[type] || '未知'
    },
    
    // 获取请假状态标签样式
    getLeaveStatusTag(status) {
      const tagMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'CANCELLED': 'info'
      }
      return tagMap[status] || 'info'
    },
    
    // 获取请假状态文本
    getLeaveStatusText(status) {
      const textMap = {
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝',
        'CANCELLED': '已撤销'
      }
      return textMap[status] || '未知'
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
    },
    
    // 计算请假天数
    calculateDays(startDate, endDate) {
      const start = new Date(startDate)
      const end = new Date(endDate)
      const diffTime = Math.abs(end - start)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1 // +1 包括开始和结束日期
      return diffDays
    },
    
    // 生成模拟请假数据
    generateMockLeaves(count = 40) {
      const leaves = []
      const types = ['SICK', 'PERSONAL', 'OFFICIAL', 'OTHER']
      const statuses = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED']
      const reasons = [
        '身体不适，需要去医院检查',
        '家中有事，需要回家处理',
        '参加重要会议，无法到校上课',
        '感冒发烧，需要休息几天',
        '家里有人生病，需要照顾',
        '参加专业竞赛，需要准备',
        '需要去实习单位报到',
        '需要去外地办理证件',
        '身体原因，需要住院治疗',
        '家中有重要事情需要处理'
      ]
      
      const now = new Date()
      const semesterStart = new Date(now.getFullYear(), 8, 1) // 9月1日
      const semesterEnd = new Date(now.getFullYear(), 11, 31) // 12月31日
      
      for (let i = 1; i <= count; i++) {
        const studentIndex = Math.floor(Math.random() * this.students.length)
        const student = this.students[studentIndex]
        const type = types[Math.floor(Math.random() * types.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const reasonIndex = Math.floor(Math.random() * reasons.length)
        
        // 随机生成请假开始和结束日期
        const randomStart = new Date(semesterStart.getTime() + Math.random() * (semesterEnd.getTime() - semesterStart.getTime()))
        const randomEnd = new Date(randomStart.getTime() + Math.floor(Math.random() * 7) * 24 * 60 * 60 * 1000) // 0-7天
        
        // 确保结束日期不超过学期结束日期
        if (randomEnd > semesterEnd) {
          randomEnd.setTime(semesterEnd.getTime())
        }
        
        // 计算请假天数
        const days = this.calculateDays(randomStart, randomEnd)
        
        // 生成提交时间（请假开始前1-5天）
        const submitTime = new Date(randomStart.getTime() - (Math.floor(Math.random() * 5) + 1) * 24 * 60 * 60 * 1000)
        
        // 如果状态不是待审批，生成处理时间
        let processTime = null
        if (status !== 'PENDING') {
          processTime = new Date(submitTime.getTime() + Math.floor(Math.random() * 72) * 60 * 60 * 1000) // 提交后0-72小时内处理
        }
        
        leaves.push({
          id: Date.now() + i,
          studentName: student.name,
          studentId: student.studentId,
          departmentId: student.departmentId,
          major: student.major,
          grade: student.grade,
          contactInfo: '138' + Math.floor(Math.random() * 100000000).toString().padStart(8, '0'),
          type: type,
          startDate: randomStart.toISOString(),
          endDate: randomEnd.toISOString(),
          days: days,
          reason: reasons[reasonIndex] + (Math.random() > 0.5 ? '。具体情况已经向辅导员说明。' : ''),
          status: status,
          submitTime: submitTime.toISOString(),
          processTime: processTime ? processTime.toISOString() : null,
          approverName: processTime ? '管理员' : null,
          remark: processTime ? (status === 'APPROVED' ? '批准请假' : '不批准请假') : '',
          semesterId: 1 // 默认本学期
        })
      }
      
      // 按提交时间倒序排序
      leaves.sort((a, b) => new Date(b.submitTime) - new Date(a.submitTime))
      
      return leaves
    }
  }
}
</script>

<style scoped>
.management-header {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.management-header h1 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.hover-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-filters {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  width: 100%;
  overflow-x: auto;
}

.search-filters .el-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.action-buttons {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 10px;
  flex-wrap: wrap;
  width: 100%;
  overflow-x: auto;
}

.pending-count {
  margin-left: auto;
}

.table-section {
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.reason-container {
  cursor: pointer;
  color: #409eff;
}

.reason-container:hover {
  text-decoration: underline;
}

/* 详情对话框样式 */
.leave-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.reason-content, .remark-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.approval-section {
  margin-top: 20px;
}

.approval-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.approval-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 批量处理对话框样式 */
.batch-process {
  padding: 10px 0;
}

.batch-process p {
  margin-bottom: 20px;
  color: #606266;
}

/* 统计对话框样式 */
.statistics-content {
  padding: 10px 0;
}

.statistics-card {
  margin-bottom: 20px;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.card-label {
  font-size: 16px;
  color: #606266;
}

.chart-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 完整原因对话框样式 */
.full-reason-content pre {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section .el-col {
    span: 24;
    margin-bottom: 10px;
  }
  
  .action-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-section .el-button, .action-section .el-tag {
    width: 100%;
  }
  
  .pending-count {
    margin-left: 0;
  }
  
  .statistics-content .el-col {
    span: 24;
  }
  
  .leave-detail .el-col {
    span: 24;
    margin-bottom: 10px;
  }
}
</style>