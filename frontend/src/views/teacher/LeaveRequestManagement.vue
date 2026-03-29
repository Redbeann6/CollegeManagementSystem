<template>
  <div class="leave-request-management-container">
    <div class="page-header">
      <h1>请假审批</h1>
      <p>管理和审批学生的请假申请</p>
    </div>

    <!-- 主要内容区域 -->
    <el-card class="leave-request-management-card">
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
            <el-select v-model="selectedStatus" placeholder="审批状态" clearable>
              <el-option label="全部状态" value=""></el-option>
              <el-option label="待审批" value="pending"></el-option>
              <el-option label="已批准" value="approved"></el-option>
              <el-option label="已拒绝" value="rejected"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索学生姓名或学号"
              prefix-icon="el-icon-search"
              clearable
              @keyup.enter="searchRequests"
            ></el-input>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 15px;">
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
            <el-select v-model="selectedLeaveType" placeholder="请假类型" clearable>
              <el-option label="全部类型" value=""></el-option>
              <el-option label="病假" value="sick"></el-option>
              <el-option label="事假" value="personal"></el-option>
              <el-option label="公假" value="official"></el-option>
              <el-option label="其他" value="other"></el-option>
            </el-select>
          </el-col>
          <el-col :span="12" class="text-right">
            <div class="action-buttons">
              <el-button type="primary" icon="el-icon-check" @click="batchApprove" :disabled="selectedRequests.length === 0">
                批量批准
              </el-button>
              <el-button type="danger" icon="el-icon-close" @click="batchReject" :disabled="selectedRequests.length === 0">
                批量拒绝
              </el-button>
              <el-button type="info" icon="el-icon-download" @click="exportRequests">
                导出数据
              </el-button>
              <el-button type="info" icon="el-icon-refresh" @click="refreshRequests">
                刷新
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 统计概览 -->
      <div class="stats-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ leaveStats.total }}</div>
              <div class="stat-label">总申请数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card warning">
              <div class="stat-value">{{ leaveStats.pending }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card success">
              <div class="stat-value">{{ leaveStats.approved }}</div>
              <div class="stat-label">已批准</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card danger">
              <div class="stat-value">{{ leaveStats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 请假申请列表 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="paginatedRequests"
          style="width: 100%"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="studentName" label="学生姓名" width="120">
            <template #default="scope">
              <span>{{ scope && scope.row ? scope.row.studentName : '' }}</span>
              <span class="student-id">({{ scope && scope.row ? (scope.row.studentIdNumber || scope.row.student_id || scope.row.studentId) : '' }})</span>
            </template>
          </el-table-column>
          <el-table-column prop="className" label="班级" width="120"></el-table-column>
          <el-table-column prop="leaveType" label="请假类型" width="100">
            <template #default="scope">
              <el-tag :type="scope && scope.row ? getLeaveTypeTagType(scope.row.leaveType) : 'info'">
                {{ scope && scope.row ? getLeaveTypeText(scope.row.leaveType) : '' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
          <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
          <el-table-column label="请假天数" width="100">
            <template #default="scope">
              {{ scope && scope.row ? calculateLeaveDays(scope.row.startDate, scope.row.endDate) : 0 }}天
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope && scope.row ? getStatusTagType(scope.row.status) : 'info'">
                {{ scope && scope.row ? getStatusText(scope.row.status) : '' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template #default="scope">
              {{ scope && scope.row ? (scope.row.createdAt || scope.row.created_at || scope.row.submit_time || scope.row.submitTime || '') : '' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <template v-if="scope && scope.row">
                <el-button type="primary" size="small" @click="viewRequestDetails(scope.row)" v-if="scope.row.status === 'pending'">
                  <i class="el-icon-view"></i> 详情
                </el-button>
                <el-button type="success" size="small" :disabled="!scope.row.id || scope.row.status !== 'pending'" @click="approveRequest(scope.row)" v-if="scope.row.status === 'pending'">
                  <i class="el-icon-check"></i> 批准
                </el-button>
                <el-button type="danger" size="small" :disabled="!scope.row.id || scope.row.status !== 'pending'" @click="rejectRequest(scope.row)" v-if="scope.row.status === 'pending'">
                  <i class="el-icon-close"></i> 拒绝
                </el-button>
                <el-button type="info" size="small" @click="viewRequestDetails(scope.row)" v-else>
                  <i class="el-icon-document"></i> 查看
                </el-button>
              </template>
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
          :total="filteredRequests.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 请假详情对话框 -->
    <el-dialog
      title="请假详情"
      v-model="showDetailsDialog"
      width="600px"
      @close="handleDetailsDialogClose"
    >
      <div class="leave-details-container">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="学生信息">
                <div class="student-info">
                  <span class="student-name">{{ currentRequest ? currentRequest.studentName : '' }}</span>
                  <span class="student-id">({{ currentRequest ? (currentRequest.studentIdNumber || currentRequest.student_id || currentRequest.studentId) : '' }})</span>
                  <span class="class-name">{{ currentRequest ? currentRequest.className : '' }}</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="请假类型">
                <el-tag :type="currentRequest ? getLeaveTypeTagType(currentRequest.leaveType) : 'info'">
                  {{ currentRequest ? getLeaveTypeText(currentRequest.leaveType) : '' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="请假时间">
                {{ currentRequest ? currentRequest.startDate : '' }} 至 {{ currentRequest ? currentRequest.endDate : '' }}
                <span class="days-count">({{ currentRequest ? calculateLeaveDays(currentRequest.startDate, currentRequest.endDate) : 0 }}天)</span>
              </el-descriptions-item>
              <el-descriptions-item label="请假原因">{{ currentRequest ? currentRequest.reason : '' }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">{{ currentRequest ? (currentRequest.createdAt || currentRequest.created_at || currentRequest.submit_time || currentRequest.submitTime || '') : '' }}</el-descriptions-item>
              <el-descriptions-item label="审批状态" v-if="currentRequest && currentRequest.status !== 'pending'">
                <el-tag :type="currentRequest ? getStatusTagType(currentRequest.status) : 'info'">
                  {{ currentRequest ? getStatusText(currentRequest.status) : '' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="审批意见" v-if="currentRequest && currentRequest.approvalComment">
                {{ currentRequest.approvalComment }}
              </el-descriptions-item>
              <el-descriptions-item label="审批时间" v-if="currentRequest && currentRequest.approvalTime">
                {{ currentRequest.approvalTime }}
              </el-descriptions-item>
              <el-descriptions-item label="审批人" v-if="currentRequest && currentRequest.approver">
                {{ currentRequest.approver }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <!-- 审批表单 -->
        <div class="approval-form" v-if="currentRequest && currentRequest.status === 'pending'">
          <h3>审批意见</h3>
          <el-form :model="approvalForm" :rules="approvalRules" ref="approvalForm" label-width="80px">
            <el-form-item label="处理方式">
              <el-radio-group v-model="approvalForm.action">
                <el-radio label="approve">批准</el-radio>
                <el-radio label="reject">拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批意见" prop="comment">
              <el-input
                v-model="approvalForm.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入审批意见"
              ></el-input>
            </el-form-item>
          </el-form>
        </div>

        <!-- 附件预览 -->
        <div class="attachments-section" v-if="currentRequest && currentRequest.attachments && currentRequest.attachments.length > 0">
          <h3>附件</h3>
          <el-upload
            action=""
            :limit="5"
            :file-list="attachmentFileList"
            :auto-upload="false"
            :disabled="true"
            list-type="picture-card"
          >
          </el-upload>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleDetailsDialogClose">关闭</el-button>
        <el-button type="success" :disabled="!currentRequest || !currentRequest.id || currentRequest.status !== 'pending'" @click="submitApproval" v-if="currentRequest && currentRequest.status === 'pending'">
          提交审批
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量操作确认对话框 -->
    <el-dialog
      title="批量处理"
      v-model="showBatchActionDialog"
      width="500px"
      @close="handleBatchActionDialogClose"
    >
      <div class="batch-action-container">
        <p class="batch-info">
          您确定要{{ batchAction === 'approve' ? '批准' : '拒绝' }}选中的 <span class="highlight">{{ selectedRequests.length }}</span> 条请假申请吗？
        </p>
        <el-form :model="batchApprovalForm" :rules="batchApprovalRules" ref="batchApprovalForm" label-width="80px">
          <el-form-item label="审批意见" prop="comment">
            <el-input
              v-model="batchApprovalForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审批意见（可选）"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="handleBatchActionDialogClose">取消</el-button>
        <el-button type="primary" :disabled="selectedRequests.length === 0" @click="confirmBatchAction">
          确认{{ batchAction === 'approve' ? '批准' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherLeaveRequestManagement',
  data() {
    return {
      // 筛选条件
      selectedCourse: '',
      selectedClass: '',
      selectedStatus: '',
      selectedLeaveType: '',
      dateRange: [],
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
      
      // 请假申请数据
      leaveRequests: [],
      
      // 选中的申请
      selectedRequests: [],
      
      // 弹出框状态
      showDetailsDialog: false,
      showBatchActionDialog: false,
      
      // 当前请求
      currentRequest: {},
      
      // 审批表单
      approvalForm: {
        action: 'approve',
        comment: ''
      },
      
      // 审批表单验证规则
      approvalRules: {
        comment: [
          { required: true, message: '请输入审批意见', trigger: 'blur' }
        ]
      },
      
      // 批量操作
      batchAction: '',
      
      // 批量审批表单
      batchApprovalForm: {
        comment: ''
      },
      
      // 批量审批表单验证规则
      batchApprovalRules: {
        comment: [] // 批量操作意见可选
      },
      
      // 附件文件列表
      attachmentFileList: [],
      
      // 统计数据
      leaveStats: {
        total: 0,
        pending: 0,
        approved: 0,
        rejected: 0
      }
    }
  },
  
  computed: {
    // 过滤后的请假申请
    filteredRequests() {
      let filtered = [...this.leaveRequests]
      
      // 按课程筛选
      if (this.selectedCourse) {
        filtered = filtered.filter(request => request.courseId === this.selectedCourse)
      }
      
      // 按班级筛选
      if (this.selectedClass) {
        filtered = filtered.filter(request => request.classId === this.selectedClass)
      }
      
      // 按状态筛选
      if (this.selectedStatus) {
        filtered = filtered.filter(request => request.status === this.selectedStatus)
      }
      
      // 按请假类型筛选
      if (this.selectedLeaveType) {
        filtered = filtered.filter(request => request.leaveType === this.selectedLeaveType)
      }
      
      // 按日期范围筛选
      if (this.dateRange && this.dateRange.length === 2) {
        filtered = filtered.filter(request => {
          const requestDate = new Date(request.startDate)
          const startDate = new Date(this.dateRange[0])
          const endDate = new Date(this.dateRange[1])
          return requestDate >= startDate && requestDate <= endDate
        })
      }
      
      // 搜索学生
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(request => 
          request.studentName.toLowerCase().includes(query) || 
          request.studentId.toLowerCase().includes(query)
        )
      }
      
      // 更新统计数据
      this.updateLeaveStats(filtered)
      
      return filtered
    },
    
    // 分页后的请假申请
    paginatedRequests() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredRequests.slice(start, end)
    }
  },
  
  mounted() {
    // 加载教学课程
    this.loadTeachingCourses()
    // 加载请假申请
    this.loadLeaveRequests()
  },
  
  methods: {
    // 加载教学课程
    async loadTeachingCourses() {
      try {
        const response = await this.$axios.get('/teacher/data/courses')
        this.teachingCourses = response.data.data || []
        
        console.log('教学课程加载完成')
      } catch (error) {
        console.error('加载教学课程失败:', error)
        this.teachingCourses = []
        this.$message.error('加载教学课程失败，请检查后端服务')
      }
    },
    
    // 加载请假申请
    async loadLeaveRequests() {
      this.loading = true
      try {
        const response = await this.$axios.get('/leave-requests')
        this.leaveRequests = response.data.data || []
        
        console.log('请假申请加载完成')
      } catch (error) {
        console.error('加载请假申请失败:', error)
        this.leaveRequests = []
        this.$message.error('加载请假申请失败，请检查后端服务')
      } finally {
        this.loading = false
      }
    },
    

    // 课程变更处理
    onCourseChange(courseId) {
      // 加载该课程的班级信息
      this.loadClassByCourse(courseId)
      this.selectedClass = ''
    },
    
    // 根据课程ID加载班级信息
    async loadClassByCourse(courseId) {
      try {
        const response = await this.$axios.get(`/teacher/data/classes/${courseId}`)
        this.availableClasses = response.data.data || []
      } catch (error) {
        console.error('加载班级信息失败:', error)
        this.availableClasses = []
        this.$message.error('加载班级信息失败，请检查后端服务')
      }
    },
    
    // 更新请假统计
    updateLeaveStats(requests) {
      const total = requests.length
      const pending = requests.filter(r => r.status === 'pending' || r.status === 'PENDING' || r.status === '待审批').length
      const approved = requests.filter(r => r.status === 'approved' || r.status === 'APPROVED' || r.status === '已批准').length
      const rejected = requests.filter(r => r.status === 'rejected' || r.status === 'REJECTED' || r.status === '已拒绝').length
      
      this.leaveStats = { total, pending, approved, rejected }
    },
    
    // 搜索请假申请
    searchRequests() {
      this.currentPage = 1
    },
    
    // 刷新请假申请列表
    refreshRequests() {
      this.selectedCourse = ''
      this.selectedClass = ''
      this.selectedStatus = ''
      this.selectedLeaveType = ''
      this.dateRange = []
      this.searchQuery = ''
      this.currentPage = 1
      this.loadLeaveRequests()
    },
    
    // 计算请假天数
    calculateLeaveDays(startDate, endDate) {
      const start = new Date(startDate)
      const end = new Date(endDate)
      const diffTime = Math.abs(end - start)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
      return diffDays
    },
    
    // 查看请假详情
    viewRequestDetails(request) {
      this.currentRequest = { ...request }
      // 初始化附件列表
      this.attachmentFileList = request.attachments || []
      // 初始化审批表单
      this.approvalForm = {
        action: 'approve',
        comment: ''
      }
      this.showDetailsDialog = true
    },
    
    // 批准请假
    approveRequest(request) {
      this.$confirm(`确定要批准 ${request.studentName} 的请假申请吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.processRequest(request.id, 'approved', '同意请假')
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    
    // 拒绝请假
    rejectRequest(request) {
      this.$confirm(`确定要拒绝 ${request.studentName} 的请假申请吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.processRequest(request.id, 'rejected', '请假理由不充分')
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    
    // 提交审批
    submitApproval() {
      this.$refs.approvalForm.validate((valid) => {
        if (valid) {
          const action = this.approvalForm.action
          this.processRequest(this.currentRequest.id, action, this.approvalForm.comment)
          this.showDetailsDialog = false
        }
      })
    },
    
    // 处理请假申请
    async processRequest(requestId, status, comment) {
      try {
        // 调用后端API处理请假申请
        const response = await this.$axios.put(`/leave-requests/${requestId}/process`, {}, {
          params: {
            status: status === 'approved' ? 'APPROVED' : 'REJECTED',
            comment: comment
          }
        });
        
        if (response.data.success) {
          // 更新本地数据
          const request = this.leaveRequests.find(r => r.id === requestId)
          if (request) {
            request.status = status;
            request.statusText = status === 'approved' ? '已批准' : '已拒绝';
            request.approvalComment = comment;
            request.approvalTime = new Date().toLocaleString();
            // 实际应该从登录信息获取当前用户
            request.approver = localStorage.getItem('teacherName') || '当前教师';
          }
          
          this.$message.success(`请假申请已${status === 'approved' ? '批准' : '拒绝'}`);
          // 重新加载数据以确保显示最新状态
          this.loadLeaveRequests();
        } else {
          this.$message.error(response.data.message || `处理失败`);
        }
      } catch (error) {
        console.error('处理请假申请失败:', error);
        this.$message.error('处理失败，请稍后重试');
      }
    },
    
    // 批量批准
    batchApprove() {
      this.batchAction = 'approve'
      this.batchApprovalForm = { comment: '' }
      this.showBatchActionDialog = true
    },
    
    // 批量拒绝
    batchReject() {
      this.batchAction = 'reject'
      this.batchApprovalForm = { comment: '' }
      this.showBatchActionDialog = true
    },
    
    // 确认批量操作
    async confirmBatchAction() {
      try {
        // 实际项目中应该调用后端API
        // await axios.put('/leave-requests/batch-process', {
        //   ids: this.selectedRequests.map(r => r.id),
        //   status: this.batchAction,
        //   comment: this.batchApprovalForm.comment
        // })
        
        // 更新本地数据
        this.selectedRequests.forEach(request => {
          const r = this.leaveRequests.find(req => req.id === request.id)
          if (r) {
            r.status = this.batchAction
            r.approvalComment = this.batchApprovalForm.comment
            r.approvalTime = new Date().toLocaleString()
            r.approver = '当前用户'
          }
        })
        
        this.$message.success(`已成功${this.batchAction === 'approve' ? '批准' : '拒绝'}选中的请假申请`)
        this.showBatchActionDialog = false
        this.selectedRequests = []
      } catch (error) {
        console.error('批量处理失败:', error)
        this.$message.error('批量处理失败，请稍后重试')
      }
    },
    
    // 导出数据
    exportRequests() {
      // 实际项目中应该调用后端API
      this.$message.success('数据导出成功')
    },
    
    // 获取请假类型文本
    getLeaveTypeText(type) {
      const typeMap = {
        sick: '病假',
        personal: '事假',
        official: '公假',
        other: '其他',
        SICK: '病假',
        PERSONAL: '事假',
        OFFICIAL: '公假',
        OTHER: '其他',
        病假: '病假',
        事假: '事假',
        公假: '公假',
        其他: '其他'
      }
      return typeMap[type] || '未知'
    },
    
    // 获取请假类型标签类型
    getLeaveTypeTagType(type) {
      const typeMap = {
        sick: 'info',
        personal: 'warning',
        official: 'primary',
        other: 'info',
        SICK: 'info',
        PERSONAL: 'warning',
        OFFICIAL: 'primary',
        OTHER: 'info',
        病假: 'info',
        事假: 'warning',
        公假: 'primary',
        其他: 'info'
      }
      return typeMap[type] || 'info'
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        pending: '待审批',
        approved: '已批准',
        rejected: '已拒绝',
        cancelled: '已取消',
        PENDING: '待审批',
        APPROVED: '已批准',
        REJECTED: '已拒绝',
        CANCELLED: '已取消',
        待审批: '待审批',
        已批准: '已批准',
        已拒绝: '已拒绝',
        已取消: '已取消'
      }
      return statusMap[status] || '未知'
    },
    
    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        pending: 'warning',
        approved: 'success',
        rejected: 'danger',
        cancelled: 'info',
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'danger',
        CANCELLED: 'info',
        待审批: 'warning',
        已批准: 'success',
        已拒绝: 'danger',
        已取消: 'info'
      }
      return statusMap[status] || 'info'
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedRequests = selection
    },
    
    // 关闭详情对话框
    handleDetailsDialogClose() {
      this.showDetailsDialog = false
      this.currentRequest = {}
      this.attachmentFileList = []
    },
    
    // 关闭批量操作对话框
    handleBatchActionDialogClose() {
      this.showBatchActionDialog = false
      this.batchAction = ''
      this.batchApprovalForm = {}
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
.leave-request-management-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.leave-request-management-card {
  margin-bottom: 20px;
}

/* 统计概览 */
.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card.warning .stat-value {
  color: #e6a23c;
}

.stat-card.success .stat-value {
  color: #67c23a;
}

.stat-card.danger .stat-value {
  color: #f56c6c;
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
.student-id {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
}

/* 请假详情样式 */
.leave-details-container {
  padding: 10px 0;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.class-name {
  font-size: 14px;
  color: #606266;
}

.days-count {
  margin-left: 10px;
  font-size: 14px;
  color: #909399;
}

.approval-form {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.approval-form h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.attachments-section {
  margin-top: 20px;
}

.attachments-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

/* 批量操作样式 */
.batch-action-container {
  padding: 10px 0;
}

.batch-info {
  margin-bottom: 20px;
  font-size: 16px;
  color: #303133;
}

.highlight {
  color: #409eff;
  font-weight: bold;
}

/* 禁用按钮样式 */
:deep(.el-button.is-disabled) {
  background-color: #f5f5f5;
  border-color: #dcdfe6;
  color: #c0c4cc;
  cursor: not-allowed;
}

:deep(.el-button.is-disabled:hover) {
  background-color: #f5f5f5;
  border-color: #dcdfe6;
  color: #c0c4cc;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-overview .el-col {
    width: 50%;
    margin-bottom: 15px;
  }
  
  .search-filter-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .leave-request-management-container {
    padding: 10px;
  }
  
  .stats-overview .el-col {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    margin-bottom: 5px;
  }
}
</style>