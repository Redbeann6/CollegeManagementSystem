<template>
  <el-card class="management-header">
    <h1>请假管理</h1>
  </el-card>
  
  <el-card class="hover-card">
      
      <!-- 筛选和搜索区域 -->
      <div class="search-filters">
        <el-row :gutter="20">
          <el-select v-model="statusFilter" placeholder="申请状态" style="width: 150px;">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="待审批" value="pending"></el-option>
            <el-option label="已批准" value="approved"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
            <el-option label="已撤回" value="canceled"></el-option>
          </el-select>
          <el-select v-model="departmentFilter" placeholder="所属系部" style="width: 150px;">
            <el-option label="全部系部" value=""></el-option>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept"></el-option>
          </el-select>
          <el-select v-model="leaveTypeFilter" placeholder="请假类型" style="width: 120px;">
            <el-option label="全部类型" value=""></el-option>
            <el-option label="病假" value="病假"></el-option>
            <el-option label="事假" value="事假"></el-option>
            <el-option label="公假" value="公假"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            style="width: 260px;"
          ></el-date-picker>
          <el-select v-model="approverFilter" placeholder="审批教师" style="width: 150px;">
            <el-option label="全部教师" value=""></el-option>
            <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id"></el-option>
          </el-select>
          <el-input
            v-model="searchQuery"
            placeholder="搜索学生姓名/学号"
            prefix-icon="el-icon-search"
            style="width: 240px;"
            @keyup.enter="applyFilters"
          ></el-input>
          <el-button type="primary" @click="applyFilters">
            <i class="el-icon-search"></i> 搜索
          </el-button>
          <el-button @click="resetFilters">
            <i class="el-icon-refresh"></i> 重置
          </el-button>
        </el-row>
      </div>
      
      <!-- 功能按钮区域 -->
      <div class="action-buttons">
        <el-button type="primary" @click="exportLeaveData">
          <i class="el-icon-download"></i> 导出数据
        </el-button>
        <el-button @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新数据
        </el-button>
      </div>
      
      <div class="content-section">
      <!-- 统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px;">
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-content">
              <div class="stat-icon el-icon-document-add"></div>
              <div class="stat-info">
                <div class="stat-number">{{ totalRequests }}</div>
                <div class="stat-label">总申请数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card warning" shadow="hover">
            <div class="stats-content">
              <div class="stat-icon el-icon-time"></div>
              <div class="stat-info">
                <div class="stat-number">{{ pendingRequests }}</div>
                <div class="stat-label">待审批</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card success" shadow="hover">
            <div class="stats-content">
              <div class="stat-icon el-icon-circle-check"></div>
              <div class="stat-info">
                <div class="stat-number">{{ approvedRequests }}</div>
                <div class="stat-label">已批准</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
          <el-card class="stats-card danger" shadow="hover">
            <div class="stats-content">
              <div class="stat-icon el-icon-circle-close"></div>
              <div class="stat-info">
                <div class="stat-number">{{ rejectedRequests }}</div>
                <div class="stat-label">已拒绝</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-tabs v-model="activeTab" class="main-tabs" style="margin-bottom: 20px;">
        <el-tab-pane label="列表" name="list">
          <!-- 请假申请列表 -->
          <el-table
            v-loading="loading"
            :data="paginatedRequests"
            style="width: 100%"
            border
            stripe
            class="hover-card"
            @row-click="handleRowClick"
            :row-class-name="tableRowClassName"
          >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="studentName" label="学生姓名" width="120">
              <template #default="scope">
                <div>
                  {{ scope.row.studentName }}
                  <div class="student-id">{{ scope.row.studentId }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="department" label="所属系部" width="120"></el-table-column>
            <el-table-column prop="className" label="班级" width="140"></el-table-column>
            <el-table-column prop="leaveType" label="请假类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTagType(scope.row.leaveType)">
                  {{ scope.row.leaveType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
            <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
            <el-table-column prop="days" label="请假天数" width="100" align="center">
              <template #default="scope">
                <span class="days-count">{{ scope.row.days }}天</span>
              </template>
            </el-table-column>
            <el-table-column prop="statusText" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ scope.row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submissionTime" label="提交时间" width="160">
              <template #default="scope">
                {{ formatDateTime(scope.row.submissionTime) }}
              </template>
            </el-table-column>
            <el-table-column label="审批教师" width="120">
              <template #default="scope">
                <span v-if="scope.row.approverName">{{ scope.row.approverName }}</span>
                <span v-else>待审批</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <div style="display: flex; flex-wrap: nowrap;">
                  <template v-if="scope.row.status === 'pending'">
                    <el-button 
                      size="small" 
                      type="success" 
                      :disabled="!scope.row.id || scope.row.status !== 'pending'"
                      @click.stop="approveRequest(scope.row.id)"
                      style="margin-right: 5px;"
                    >
                      批准
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      :disabled="!scope.row.id || scope.row.status !== 'pending'"
                      @click.stop="rejectRequest(scope.row)"
                      style="margin-right: 5px;"
                    >
                      拒绝
                    </el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredRequests.length"
              :page-size="pageSize"
              :current-page="currentPage"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            ></el-pagination>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="统计分析" name="statistics">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
              <el-card class="chart-card">
                <template #header>
                  <div class="card-header">
                    <span>请假类型分布</span>
                  </div>
                </template>
                <div class="chart-container">
                  <el-empty v-if="!chartData.typeData.length" description="暂无数据"></el-empty>
                  <div v-else ref="typeChart" class="chart"></div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
              <el-card class="chart-card">
                <template #header>
                  <div class="card-header">
                    <span>系部请假统计</span>
                  </div>
                </template>
                <div class="chart-container">
                  <el-empty v-if="!chartData.departmentData.length" description="暂无数据"></el-empty>
                  <div v-else ref="departmentChart" class="chart"></div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-card class="chart-card">
                <template #header>
                  <div class="card-header">
                    <span>请假趋势图</span>
                  </div>
                </template>
                <div class="chart-container">
                  <el-empty v-if="!chartData.trendData.length" description="暂无数据"></el-empty>
                  <div v-else ref="trendChart" class="chart"></div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      <!-- 空状态 -->
      <div v-if="activeTab === 'list' && filteredRequests.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无请假申请记录"></el-empty>
      </div>
    </div>
  </el-card>

  <!-- 请假详情对话框 -->
  <el-dialog
    title="请假申请详情"
    v-model="detailDialogVisible"
    width="700px"
    :close-on-click-modal="false"
  >
      <div v-if="selectedRequest" class="request-detail">
        <div class="detail-header">
          <div class="detail-info">
            <h3>{{ selectedRequest.leaveType }}</h3>
            <el-tag :type="getStatusTagType(selectedRequest.status)" size="large" class="status-tag">
              {{ selectedRequest.statusText }}
            </el-tag>
          </div>
          <div class="detail-time">
            <span>申请时间：{{ formatDateTime(selectedRequest.submissionTime) }}</span>
          </div>
        </div>

        <div class="detail-content">
          <el-descriptions border :column="1" class="detail-descriptions">
            <el-descriptions-item label="申请人">
              <span>{{ selectedRequest.studentName }}</span>
              <span class="student-id">(学号: {{ selectedRequest.studentIdNumber || selectedRequest.studentId }})</span>
            </el-descriptions-item>
            <el-descriptions-item label="所属系部">{{ selectedRequest.department }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ selectedRequest.className }}</el-descriptions-item>
            <el-descriptions-item label="请假时间">{{ selectedRequest.startDate }} 至 {{ selectedRequest.endDate }}</el-descriptions-item>
            <el-descriptions-item label="请假天数">{{ selectedRequest.days }}天</el-descriptions-item>
            <el-descriptions-item label="请假原因">{{ selectedRequest.reason }}</el-descriptions-item>
            <el-descriptions-item label="审批教师" v-if="selectedRequest.approverName">
              {{ selectedRequest.approverName }} ({{ selectedRequest.approverId }})
            </el-descriptions-item>
            <el-descriptions-item label="处理结果" v-if="selectedRequest.status !== 'pending'">
              <div>
                {{ selectedRequest.status === 'approved' ? '已批准' : selectedRequest.status === 'rejected' ? '已拒绝' : '已撤回' }}
                <span v-if="selectedRequest.approvalResultTime">
                  ，处理时间：{{ formatDateTime(selectedRequest.approvalResultTime) }}
                </span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="审批意见" v-if="selectedRequest.approvalComment">
              {{ selectedRequest.approvalComment }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="selectedRequest.attachments && selectedRequest.attachments.length > 0" class="attachments-section">
          <h4>附件</h4>
          <div class="attachments-list">
            <div v-for="(attachment, index) in selectedRequest.attachments" :key="index" class="attachment-item">
              <el-link :href="attachment.url" target="_blank">{{ attachment.name }}</el-link>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <template v-if="selectedRequest && selectedRequest.status === 'pending'">
          <el-button @click="detailDialogVisible = false">取消</el-button>
          <el-button type="success" :disabled="!selectedRequest.id || selectedRequest.status !== 'pending'" @click="approveRequest(selectedRequest.id)">批准</el-button>
          <el-button type="danger" :disabled="!selectedRequest.id || selectedRequest.status !== 'pending'" @click="rejectRequest(selectedRequest)">拒绝</el-button>
        </template>
        <template v-else>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            type="danger"
            @click="rejectRequest(selectedRequest)"
            v-if="selectedRequest.status === 'pending'"
          >
            拒绝
          </el-button>
        </template>
      </div>
    </el-dialog>

    <!-- 拒绝申请对话框 -->
    <el-dialog
      title="拒绝请假申请"
      v-model="rejectDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" :rules="rejectFormRules" ref="rejectForm" label-width="120px">
        <el-form-item label="拒绝原因" prop="comment">
          <el-input v-model="rejectForm.comment" type="textarea" :rows="4" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!currentRejectId" @click="submitReject">确认拒绝</el-button>
      </div>
    </el-dialog>

    <!-- 编辑申请对话框 -->
    <el-dialog
      title="编辑请假申请"
      v-model="editDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editFormRules" ref="editForm" label-width="120px">
        <el-form-item label="学生信息">
          <div>{{ editForm.studentName }} ({{ editForm.studentIdNumber || editForm.studentId }})</div>
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="editForm.leaveType" placeholder="请选择请假类型">
            <el-option label="病假" value="病假"></el-option>
            <el-option label="事假" value="事假"></el-option>
            <el-option label="公假" value="公假"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="editForm.startDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="editForm.endDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="请假天数" prop="days">
          <el-input v-model.number="editForm.days" disabled></el-input>
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="editForm.reason" type="textarea" :rows="4" placeholder="请输入请假原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!currentEditId" @click="submitEdit">保存修改</el-button>
      </div>
    </el-dialog>
</template>

<script>
export default {
  name: 'AdminLeaveRequestManagement',
  data() {
    return {
      loading: false,
      searchQuery: '',
      statusFilter: '',
      departmentFilter: '',
      leaveTypeFilter: '',
      approverFilter: '',
      dateRange: null,
      currentPage: 1,
      pageSize: 10,
      activeTab: 'list',
      requests: [],
      selectedRequests: [],
      detailDialogVisible: false,
      rejectDialogVisible: false,
      editDialogVisible: false,
      selectedRequest: null,
      // 通知相关变量已移除，使用Element Plus全局方法
      rejectForm: {
        comment: ''
      },
      rejectFormRules: {
        comment: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
      },
      editForm: {
        studentName: '',
        studentId: '',
        leaveType: '',
        startDate: '',
        endDate: '',
        days: 1,
        reason: ''
      },
      editFormRules: {
        leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
        startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
        endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
        reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
      },
      currentRejectId: null,
      currentEditId: null,
      departments: ['计算机系', '数学系', '英语系', '物理系', '化学系'],
      teachers: [
        { id: 'T101', name: '张教授' },
        { id: 'T102', name: '李副教授' },
        { id: 'T103', name: '王讲师' },
        { id: 'T104', name: '刘助教' },
        { id: 'T105', name: '陈教授' }
      ],
      chartData: {
        typeData: [],
        departmentData: [],
        trendData: []
      },
      charts: {
        typeChart: null,
        departmentChart: null,
        trendChart: null
      }
    }
  },
  computed: {
    // 过滤后的请假申请列表
    filteredRequests() {
      return this.requests.filter(request => {
        // 搜索过滤
        const matchesSearch = !this.searchQuery || 
          request.studentName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          request.studentId.toLowerCase().includes(this.searchQuery.toLowerCase());
        
        // 状态过滤
        const matchesStatus = !this.statusFilter || request.status === this.statusFilter;
        
        // 系部过滤
        const matchesDepartment = !this.departmentFilter || request.department === this.departmentFilter;
        
        // 类型过滤
        const matchesType = !this.leaveTypeFilter || request.leaveType === this.leaveTypeFilter;
        
        // 审批教师过滤
        const matchesApprover = !this.approverFilter || request.approverId === this.approverFilter;
        
        // 日期范围过滤
        let matchesDate = true;
        if (this.dateRange && this.dateRange.length === 2) {
          const requestDate = new Date(request.startDate);
          const startDate = new Date(this.dateRange[0]);
          const endDate = new Date(this.dateRange[1]);
          matchesDate = requestDate >= startDate && requestDate <= endDate;
        }
        
        return matchesSearch && matchesStatus && matchesDepartment && matchesType && matchesApprover && matchesDate;
      }).sort((a, b) => {
        // 优先显示待审批的请求，然后按提交时间降序排序
        if (a.status !== b.status) {
          return a.status === 'pending' ? -1 : b.status === 'pending' ? 1 : 0;
        }
        return new Date(b.submissionTime) - new Date(a.submissionTime);
      });
    },
    
    // 分页后的请假申请列表
    paginatedRequests() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredRequests.slice(start, end);
    },
    
    // 统计数据
    totalRequests() {
      return this.requests.length;
    },
    
    pendingRequests() {
      return this.requests.filter(req => req.status === 'pending').length;
    },
    
    approvedRequests() {
      return this.requests.filter(req => req.status === 'approved').length;
    },
    
    rejectedRequests() {
      return this.requests.filter(req => req.status === 'rejected').length;
    }
  },
  mounted() {
    this.loadLeaveRequests();
  },
  watch: {
    activeTab(newVal) {
      if (newVal === 'statistics') {
        // 延迟执行以确保DOM已渲染
        this.$nextTick(() => {
          this.initCharts();
        });
      }
    },
    // 当日期变化时更新请假天数
    'editForm.startDate': function() {
      this.calculateDays();
    },
    'editForm.endDate': function() {
      this.calculateDays();
    }
  },
  methods: {
    // 加载请假申请列表
    async loadLeaveRequests() {
      try {
        this.loading = true;
        
        // 从后端API获取真实数据
        const response = await this.$axios.get('/admin/leave-requests');
        
        // 处理返回的数据，使其符合前端格式
        console.log('[DEBUG] Raw response data:', response.data.data);
        this.requests = response.data.data.map(request => {
          console.log('[DEBUG] Processing request:', request);
          // 确保每个请求都有有效的ID
          const id = request.id || Date.now() + Math.floor(Math.random() * 1000);
          return {
            id: id,
            studentName: request.student?.name || request.studentName || '未知学生',
            studentId: request.student?.studentId || request.studentIdNumber || request.studentId || '未知学号',
            department: request.student?.department || request.department || '未知系部',
            className: request.student?.className || request.className || '未知班级',
            leaveType: request.leaveType || '未知类型',
            startDate: request.startDate,
            endDate: request.endDate,
            days: this.calculateDaysBetweenDates(request.startDate, request.endDate) || 1,
            reason: request.reason || request.content || '无原因',
            status: this.normalizeStatusValue(request.status || 'unknown'),
            statusText: this.getStatusText(this.normalizeStatusValue(request.status || 'unknown')),
            submissionTime: request.createdAt || request.createTime || new Date().toISOString(),
            approvalResultTime: request.updatedAt || request.updateTime || null,
            approvalComment: request.remarks || request.remark || request.comment || null,
            approverId: request.handlerId || request.approverId || (request.handler && request.handler.id) || (request.approver && request.approver.id) || null,
            approverName: request.handlerName || request.approverName || (request.handler && request.handler.name) || (request.approver && request.approver.name) || null,
            attachments: request.attachments || []
          };
        });
        console.log('[DEBUG] Processed requests:', this.requests);
        
        // 更新统计图表数据
        this.updateChartData();
      } catch (error) {
        console.error('加载请假申请失败:', error);
        this.requests = [];
        this.updateChartData();
        this.showNotification('加载失败', '加载请假申请数据失败，请检查后端服务', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    // 刷新数据
    refreshData() {
      this.loadLeaveRequests();
    },
    
    // 应用筛选条件
    applyFilters() {
      this.currentPage = 1;
    },
    
    // 重置筛选条件
    resetFilters() {
      this.statusFilter = '';
      this.departmentFilter = '';
      this.leaveTypeFilter = '';
      this.dateRange = [];
      this.approverFilter = '';
      this.searchQuery = '';
      this.currentPage = 1;
      this.loadLeaveRequests();
    },
    
    // 查看请假详情
    viewRequestDetails(request) {
      if (!request || !request.id) {
        this.showNotification('错误', '无效的请假申请数据', 'error');
        return;
      }
      this.selectedRequest = { ...request };
      this.detailDialogVisible = true;
      console.log('查看详情按钮被点击，request数据:', request);
    },
    
    // 批准请假申请
    async approveRequest(requestId) {
      // 检查requestId是否有效
      if (!requestId) {
        this.showNotification('错误', '无效的请假申请ID', 'error');
        return;
      }
      
      this.$confirm('确定要批准此请假申请吗？', '确认批准', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          // 调用后端API处理请假申请
          const response = await this.$axios.put(`/admin/leave-requests/${requestId}/status`, {}, {
            params: {
              status: 'APPROVED',
              remarks: '管理员批准'
            }
          });
          
          if (response.data.success) {
            // 更新本地数据
            const request = this.requests.find(req => req.id === requestId);
            if (request) {
              request.status = 'approved';
              request.statusText = '已批准';
              request.approvalResultTime = new Date().toISOString();
              request.approvalComment = '管理员批准';
              // 获取当前管理员信息
              request.approverId = localStorage.getItem('adminId') || 'A101';
              request.approverName = localStorage.getItem('adminName') || '管理员';
            }
            
            this.detailDialogVisible = false;
            this.updateChartData();
            this.showNotification('审批成功', '请假申请已成功批准', 'success');
            // 重新加载数据以确保显示最新状态
            this.loadLeaveRequests();
          } else {
            this.showNotification('审批失败', response.data.message || '批准请假申请失败', 'error');
          }
        } catch (error) {
          console.error('批准请假申请失败:', error);
          this.showNotification('审批失败', '批准请假申请失败，请稍后重试', 'error');
        }
      });
    },
    
    // 拒绝请假申请
    rejectRequest(request) {
      console.log('拒绝按钮被点击，request数据:', request);
      if (!request || !request.id) {
        this.showNotification('错误', '无效的请假申请数据', 'error');
        return;
      }
      this.selectedRequest = { ...request };
      this.currentRejectId = request.id;
      this.rejectForm.comment = '';
      this.rejectDialogVisible = true;
    },
    
    // 提交拒绝申请
    async submitReject() {
      this.$refs.rejectForm.validate(async (valid) => {
        if (valid) {
          try {
            // 调用后端API处理请假申请
            const response = await this.$axios.put(`/admin/leave-requests/${this.currentRejectId}/status`, {}, {
              params: {
                status: 'REJECTED',
                remarks: this.rejectForm.comment
              }
            });
            
            if (response.data.success) {
              // 更新本地数据
              const request = this.requests.find(req => req.id === this.currentRejectId);
              if (request) {
                request.status = 'rejected';
                request.statusText = '已拒绝';
                request.approvalComment = this.rejectForm.comment;
                request.approvalResultTime = new Date().toISOString();
                // 获取当前管理员信息
                request.approverId = localStorage.getItem('adminId') || 'A101';
                request.approverName = localStorage.getItem('adminName') || '管理员';
              }
              
              this.rejectDialogVisible = false;
              this.detailDialogVisible = false;
              this.updateChartData();
              this.showNotification('审批成功', '请假申请已成功拒绝', 'success');
              // 重新加载数据以确保显示最新状态
              this.loadLeaveRequests();
            } else {
              this.showNotification('审批失败', response.data.message || '拒绝请假申请失败', 'error');
            }
          } catch (error) {
            console.error('拒绝请假申请失败:', error);
            this.showNotification('审批失败', '拒绝请假申请失败，请稍后重试', 'error');
          }
        }
      });
    },
    
    // 编辑请假申请
    editRequest(request) {
      console.log('编辑按钮被点击，request数据:', request);
      if (!request || !request.id) {
        this.showNotification('错误', '无效的请假申请数据', 'error');
        return;
      }
      this.selectedRequest = { ...request };
      this.currentEditId = request.id;
      // 填充编辑表单
      this.editForm = {
        studentName: request.studentName,
        studentId: request.studentId,
        studentIdNumber: request.studentIdNumber,
        leaveType: request.leaveType,
        startDate: request.startDate,
        endDate: request.endDate,
        days: request.days,
        reason: request.reason
      };
      this.editDialogVisible = true;
    },
    
    // 计算请假天数
    calculateDays() {
      if (this.editForm.startDate && this.editForm.endDate) {
        const start = new Date(this.editForm.startDate);
        const end = new Date(this.editForm.endDate);
        if (end >= start) {
          const diffTime = Math.abs(end - start);
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
          this.editForm.days = diffDays;
        }
      }
    },
    
    // 计算两个日期之间的天数
    calculateDaysBetweenDates(startDate, endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      return diffDays;
    },
    
    
    // 提交编辑表单
    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          // 验证日期范围
          const start = new Date(this.editForm.startDate);
          const end = new Date(this.editForm.endDate);
          if (end < start) {
            this.$message.error('结束日期不能早于开始日期');
            return;
          }
          
          try {
            // 准备更新数据
            const updateData = {
              leaveType: this.editForm.leaveType,
              startDate: this.editForm.startDate,
              endDate: this.editForm.endDate,
              days: this.editForm.days,
              reason: this.editForm.reason
            };
            
            // 调用后端API更新数据
            const response = await this.$axios.put(`/api/admin/leave-requests/${this.currentEditId}`, updateData);
            
            if (response.data.success) {
              // 更新本地数据
              const request = this.requests.find(req => req.id === this.currentEditId);
              if (request) {
                Object.assign(request, updateData);
              }
              
              this.editDialogVisible = false;
              this.updateChartData();
              this.showNotification('更新成功', '请假申请已成功更新', 'success');
              // 重新加载数据以确保显示最新状态
              this.loadLeaveRequests();
            } else {
              this.showNotification('更新失败', response.data.message || '更新请假申请失败', 'error');
            }
          } catch (error) {
            console.error('更新请假申请失败:', error);
            this.showNotification('更新失败', '更新请假申请失败，请稍后重试', 'error');
          }
        }
      });
    },
    
    // 处理行点击
    handleRowClick(row) {
      this.viewRequestDetails(row);
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    },
    
    handleCurrentChange(current) {
      this.currentPage = current;
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    
    // 获取请假类型标签样式
    getTypeTagType(type) {
      const typeMap = {
        '病假': 'info',
        '事假': 'warning',
        '公假': 'success',
        '其他': 'primary'
      };
      return typeMap[type] || 'info';
    },
    
    // 标准化状态值
    normalizeStatusValue(status) {
      if (!status) return 'unknown';
      
      // 如果是大写的枚举值，转换为小写
      const upperStatus = status.toString().toUpperCase();
      
      switch(upperStatus) {
        case 'PENDING':
          return 'pending';
        case 'APPROVED':
          return 'approved';
        case 'REJECTED':
          return 'rejected';
        case 'CANCELED':
          return 'canceled';
        default:
          return status.toString().toLowerCase();
      }
    },
    
    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'pending': '待审批',
        'approved': '已批准',
        'rejected': '已拒绝',
        'canceled': '已取消'
      };
      return statusMap[status] || '未知';
    },
    
    // 获取状态标签样式
    getStatusTagType(status) {
      const statusMap = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger',
        'canceled': 'info'
      };
      return statusMap[status] || 'info';
    },
    
    // 显示通知
    showNotification(title, message, type = 'info') {
      this.$message({
        type: type,
        message: message,
        duration: 4000
      });
    },
    
    // 表格行样式
    tableRowClassName({ row }) {
      if (row.status === 'pending') {
        return 'pending-row';
      }
      return '';
    },
    
    // 导出数据
    exportLeaveData() {
      this.$confirm('确定要导出当前筛选条件下的请假申请数据吗？', '确认导出', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        // 实际项目中应该调用后端API导出数据
        this.showNotification('导出成功', '数据正在导出，请稍后查看下载内容', 'success');
      });
    },
    
    // 更新图表数据
    updateChartData() {
      // 请假类型分布数据
      const typeStats = {};
      this.filteredRequests.forEach(req => {
        typeStats[req.leaveType] = (typeStats[req.leaveType] || 0) + 1;
      });
      this.chartData.typeData = Object.keys(typeStats).map(type => ({
        value: typeStats[type],
        name: type
      }));
      
      // 系部分布数据
      const deptStats = {};
      this.filteredRequests.forEach(req => {
        deptStats[req.department] = (deptStats[req.department] || 0) + 1;
      });
      this.chartData.departmentData = Object.keys(deptStats).map(dept => ({
        value: deptStats[dept],
        name: dept
      }));
      
      // 请假趋势数据（按月份）
      const monthStats = {};
      const months = [];
      for (let i = 11; i >= 0; i--) {
        const date = new Date();
        date.setMonth(date.getMonth() - i);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        monthStats[monthKey] = 0;
        months.push(monthKey);
      }
      
      this.filteredRequests.forEach(req => {
        const monthKey = req.startDate.substring(0, 7);
        if (monthStats.hasOwnProperty(monthKey)) {
          monthStats[monthKey]++;
        }
      });
      
      this.chartData.trendData = months.map(month => ({
        month: month,
        count: monthStats[month]
      }));
    },
    
    // 初始化图表
    initCharts() {
      // 这里应该使用ECharts或其他图表库初始化图表
      // 由于没有实际引入图表库，这里只做简单提示
      console.log('图表初始化需要引入ECharts等图表库');
    },
    
    // 生成模拟数据
    getMockLeaveRequests() {
      const now = new Date();
      
      // 生成过去30天到未来14天的随机日期
      const getRandomDate = (daysAgo, daysLater) => {
        const startDate = new Date(now.getTime() - daysAgo * 24 * 60 * 60 * 1000);
        const endDate = new Date(now.getTime() + daysLater * 24 * 60 * 60 * 1000);
        const randomTime = startDate.getTime() + Math.random() * (endDate.getTime() - startDate.getTime());
        return new Date(randomTime).toISOString().split('T')[0];
      };
      
      // 生成随机时间
      const getRandomTime = (dateString) => {
        const baseDate = new Date(dateString);
        const hours = Math.floor(Math.random() * 24);
        const minutes = Math.floor(Math.random() * 60);
        baseDate.setHours(hours, minutes, 0, 0);
        return baseDate.toISOString();
      };
      
      // 生成随机学生信息
      const generateStudent = (index) => {
        const firstNames = ['张', '李', '王', '刘', '陈', '杨', '赵', '黄', '周', '吴'];
        const lastNames = ['伟', '芳', '娜', '秀英', '敏', '静', '强', '磊', '军', '洋'];
        
        const firstName = firstNames[Math.floor(Math.random() * firstNames.length)];
        const lastName = lastNames[Math.floor(Math.random() * lastNames.length)];
        const studentName = firstName + lastName;
        const studentId = '2020' + String(1000 + index).slice(-4);
        
        return { studentName, studentId };
      };
      
      // 生成随机班级和系部
      const departments = ['计算机系', '数学系', '英语系', '物理系', '化学系'];
      const classes = ['计算机科学与技术1班', '计算机科学与技术2班', '软件工程1班', '软件工程2班', '数据科学1班', '人工智能1班'];
      
      // 生成随机请假类型
      const leaveTypes = ['病假', '事假', '公假', '其他'];
      
      // 生成随机请假原因
      const reasons = [
        '感冒发烧，身体不适，需要休息',
        '家中有事，需要回家处理',
        '参加学术会议',
        '身体不适，需要去医院检查',
        '家中长辈生病，需要照顾',
        '参加技能竞赛',
        '办理重要证件',
        '其他个人原因'
      ];
      
      // 生成随机审批意见
      const approvalComments = [
        '情况属实，同意请假',
        '准假，请妥善安排学习',
        '同意，请按时返校',
        '申请理由不足，不予批准',
        '请补充相关证明材料',
        '请假时间较长，建议及时补交作业',
        '批准，请保持通讯畅通',
        '驳回，请提供相关证明'
      ];
      
      // 生成随机教师信息
      const teacherIds = ['T101', 'T102', 'T103', 'T104', 'T105'];
      const teacherNames = ['张教授', '李副教授', '王讲师', '刘助教', '陈教授'];
      
      const mockRequests = [];
      
      // 生成50条模拟数据
      for (let i = 1; i <= 50; i++) {
        const statusOptions = ['pending', 'approved', 'rejected', 'canceled'];
        // 确保有30%的待审批状态
        const status = i <= 15 ? 'pending' : statusOptions[Math.floor(Math.random() * (statusOptions.length - 1) + 1)];
        
        const { studentName, studentId } = generateStudent(i);
        const department = departments[Math.floor(Math.random() * departments.length)];
        const className = classes[Math.floor(Math.random() * classes.length)];
        
        const startDate = getRandomDate(30, 14);
        // 计算结束日期，确保不早于开始日期且不超过开始日期+7天
        const startDateObj = new Date(startDate);
        const endDateObj = new Date(startDateObj.getTime() + Math.floor(Math.random() * 7) * 24 * 60 * 60 * 1000);
        const endDate = endDateObj.toISOString().split('T')[0];
        
        // 计算请假天数
        const days = Math.ceil((endDateObj - startDateObj) / (1000 * 60 * 60 * 24)) + 1;
        
        const submissionTime = getRandomTime(startDate);
        
        // 生成审批结果时间（如果不是待审批状态）
        let approvalResultTime = null;
        let approvalComment = null;
        let approverId = null;
        let approverName = null;
        
        if (status !== 'pending') {
          const submissionDate = new Date(submissionTime);
          // 审批结果时间应该在提交时间之后
          approvalResultTime = new Date(submissionDate.getTime() + Math.floor(Math.random() * 3) * 24 * 60 * 60 * 1000 + Math.floor(Math.random() * 86400000)).toISOString();
          
          if (status !== 'canceled') {
            const teacherIndex = Math.floor(Math.random() * teacherIds.length);
            approverId = teacherIds[teacherIndex];
            approverName = teacherNames[teacherIndex];
            approvalComment = approvalComments[Math.floor(Math.random() * approvalComments.length)];
          }
        }
        
        mockRequests.push({
          id: i,
          studentName: studentName,
          studentId: studentId,
          department: department,
          className: className,
          leaveType: leaveTypes[Math.floor(Math.random() * leaveTypes.length)],
          startDate: startDate,
          endDate: endDate,
          days: days,
          reason: reasons[Math.floor(Math.random() * reasons.length)],
          status: status,
          statusText: status === 'pending' ? '待审批' : 
                     status === 'approved' ? '已批准' : 
                     status === 'rejected' ? '已拒绝' : '已撤回',
          submissionTime: submissionTime,
          approvalResultTime: approvalResultTime,
          approvalComment: approvalComment,
          approverId: approverId,
          approverName: approverName,
          attachments: Math.random() > 0.5 ? [
            { name: '诊断证明.pdf', url: '/mock/diagnosis.pdf' },
            { name: '请假申请书.docx', url: '/mock/application.docx' }
          ] : []
        });
      }
      
      // 优先显示待审批的请求，然后按提交时间降序排序
      return mockRequests.sort((a, b) => {
        if (a.status !== b.status) {
          return a.status === 'pending' ? -1 : b.status === 'pending' ? 1 : 0;
        }
        return new Date(b.submissionTime) - new Date(a.submissionTime);
      });
    }
  }
}
</script>

<style scoped>
.leave-request-management-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.fade-in {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.management-header {
  margin-bottom: 24px;
  border-radius: 8px;
}

.management-header h1 {
  margin: 0;
  color: #303133;
}

.hover-card {
  border-radius: 8px;
  transition: all 0.3s ease;
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

.table-section {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.hover-card {
  transition: all 0.3s ease;
}

.hover-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

.mb-20 {
  margin-bottom: 20px;
}

.stats-card {
  border: none;
  transition: all 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stats-card.warning .stat-value {
  color: #000000;
}

.stats-card.success .stat-value {
  color: #000000;
}

.stats-card.danger .stat-value {
  color: #000000;
}

.stats-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-icon {
  font-size: 36px;
  color: #000000;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border-radius: 50%;
  border: 1px solid #000000;
}

.stats-card.warning .stat-icon {
  color: #000000;
  background-color: #ffffff;
}

.stats-card.success .stat-icon {
  color: #000000;
  background-color: #ffffff;
}

.stats-card.danger .stat-icon {
  color: #000000;
  background-color: #ffffff;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #000000;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #000000;
}

.title-cell {
  display: flex;
  justify-content: space-between;
  align-items: center;
  align-items: flex-start;
}

.title-content {
  flex: 1;
}

.title-content.unread {
  font-weight: bold;
  color: #000000;
}

.title-meta {
  margin-left: 10px;
}

.content-preview {
  cursor: pointer;
  color: #000000;
}

.content-preview:hover {
  text-decoration: underline;
}

.recipient-info {
  cursor: pointer;
  color: #000000;
}

.recipient-info:hover {
  text-decoration: underline;
}

.student-id {
  color: #000000;
  font-size: 12px;
  margin-top: 4px;
}

.days-count {
  color: #000000;
  font-weight: 500;
}

/* 表格样式 - 黑白配色 */
.table-section :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.table-section :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.table-section :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
  font-weight: 600;
}

.table-section :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.table-section :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.table-section :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.table-section :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 表格行样式 */
.table-section :deep(.el-table .pending-row) {
  background-color: #f5f5f5;
}

/* 选择框样式 */
.table-section :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.table-section :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.table-section :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.table-section :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.table-section :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

.table-section :deep(.el-tag--success) {
  background-color: #f0f0f0;
}

.table-section :deep(.el-tag--warning) {
  background-color: #f0f0f0;
}

.table-section :deep(.el-tag--primary) {
  background-color: #f0f0f0;
}

/* 按钮样式 */
.table-section :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.table-section :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.table-section :deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.table-section :deep(.el-button--info) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.table-section :deep(.el-button--warning) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

/* 全局按钮样式 */
:deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

:deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

:deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

:deep(.el-button--info) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

:deep(.el-button--warning) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
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

/* 详情对话框样式 */
.request-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #000000;
}

.detail-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-info h3 {
  margin: 0;
  font-size: 20px;
  color: #000000;
}

.status-tag {
  font-size: 14px;
}

.detail-time {
  color: #000000;
  font-size: 14px;
}

.detail-content {
  margin-top: 16px;
}

.detail-descriptions {
  font-size: 14px;
}

.detail-descriptions .el-descriptions__label {
  color: #000000;
  font-weight: 500;
}

.attachments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #000000;
}

.attachments-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #000000;
}

.attachments-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.attachment-item {
  background-color: #ffffff;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #000000;
}

/* 图表样式 */
.chart-card {
  margin-bottom: 20px;
  border: 1px solid #000000;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-section .el-row {
    flex-direction: column;
  }
  
  .filter-section .el-col {
    width: 100%;
    margin-bottom: 12px;
  }
}

@media (max-width: 768px) {
  .admin-leave-request-management {
    padding: 10px;
  }
  
  .header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .header-section h1 {
    font-size: 20px;
  }
  
  .content-section {
    padding: 16px;
  }
  
  .detail-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .stats-content {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
  
  .stat-info {
    text-align: center;
  }
}
</style>