<template>
  <div class="leave-requests">
    <div class="header-section">
      <h1>请假申请管理</h1>
      <el-button type="primary" @click="openCreateDialog" class="create-button">
        <i class="el-icon-plus"></i> 提交请假申请
      </el-button>
    </div>

    <div class="filter-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索申请内容或原因"
        prefix-icon="el-icon-search"
        class="search-input"
      ></el-input>
      <el-select v-model="statusFilter" placeholder="申请状态" class="status-select">
        <el-option label="全部状态" value=""></el-option>
        <el-option label="待审批" value="pending"></el-option>
        <el-option label="已批准" value="approved"></el-option>
        <el-option label="已拒绝" value="rejected"></el-option>
        <el-option label="已撤回" value="canceled"></el-option>
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        class="date-picker"
      ></el-date-picker>
    </div>

    <div class="content-section">
      <el-table
        v-loading="loading"
        :data="paginatedRequests"
        style="width: 100%"
        border
        @row-click="handleRowClick"
      >
        <el-table-column prop="leaveType" label="请假类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getTypeTagType(scope.row.leaveType)">
              {{ scope.row.leaveType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
        <el-table-column prop="days" label="请假天数" width="100" align="center">
          <template slot-scope="scope">
            <span class="days-count">{{ scope.row.days }}天</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="请假原因" min-width="200"></el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submissionTime" label="提交时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.submissionTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="approvalResultTime" label="处理时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.approvalResultTime ? formatDateTime(scope.row.approvalResultTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button 
              size="small" 
              type="text" 
              @click="viewRequestDetails(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              size="small" 
              type="danger" 
              text 
              @click="cancelRequest(scope.row.id)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredRequests.length"
          :page-size.sync="pageSize"
          :current-page.sync="currentPage"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>

      <div v-if="filteredRequests.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无请假申请记录"></el-empty>
      </div>
    </div>

    <!-- 创建请假申请对话框 -->
    <el-dialog
      title="提交请假申请"
      :visible.sync="createDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="leaveForm" :rules="leaveFormRules" ref="leaveForm" label-width="120px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="病假" value="病假"></el-option>
            <el-option label="事假" value="事假"></el-option>
            <el-option label="公假" value="公假"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="leaveForm.startDate" type="date" placeholder="选择开始日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="leaveForm.endDate" type="date" placeholder="选择结束日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="leaveForm.reason" type="textarea" :rows="4" placeholder="请详细描述请假原因"></el-input>
        </el-form-item>
        <el-form-item label="附件上传" prop="attachments">
          <el-upload
            class="upload-demo"
            :action="'/api/upload'"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :file-list="fileList"
            list-type="picture"
            accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">
              只能上传jpg/png/pdf/doc/docx文件，单个文件不超过2MB
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelCreate">取消</el-button>
        <el-button type="primary" @click="submitLeaveRequest">提交申请</el-button>
      </div>
    </el-dialog>

    <!-- 请假详情对话框 -->
    <el-dialog
      title="请假申请详情"
      :visible.sync="detailDialogVisible"
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
              <span class="student-id">(学号: {{ selectedRequest.studentId }})</span>
            </el-descriptions-item>
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
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 提示通知 -->
    <el-notification
      v-model="notificationVisible"
      :title="notificationTitle"
      :message="notificationMessage"
      :type="notificationType"
      :duration="4000"
    ></el-notification>
  </div>
</template>

<script>
export default {
  name: 'StudentLeaveRequests',
  data() {
    return {
      loading: false,
      searchQuery: '',
      statusFilter: '',
      dateRange: null,
      currentPage: 1,
      pageSize: 10,
      requests: [],
      createDialogVisible: false,
      detailDialogVisible: false,
      selectedRequest: null,
      fileList: [],
      notificationVisible: false,
      notificationTitle: '',
      notificationMessage: '',
      notificationType: 'success',
      leaveForm: {
        leaveType: '',
        startDate: '',
        endDate: '',
        reason: ''
      },
      leaveFormRules: {
        leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
        startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
        endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
        reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
      }
    }
  },
  computed: {
    // 过滤后的请假申请列表
    filteredRequests() {
      return this.requests.filter(request => {
        // 搜索过滤
        const matchesSearch = !this.searchQuery || 
          request.reason.toLowerCase().includes(this.searchQuery.toLowerCase());
        
        // 状态过滤
        const matchesStatus = !this.statusFilter || request.status === this.statusFilter;
        
        // 日期范围过滤
        let matchesDate = true;
        if (this.dateRange && this.dateRange.length === 2) {
          const requestDate = new Date(request.startDate);
          const startDate = new Date(this.dateRange[0]);
          const endDate = new Date(this.dateRange[1]);
          matchesDate = requestDate >= startDate && requestDate <= endDate;
        }
        
        return matchesSearch && matchesStatus && matchesDate;
      }).sort((a, b) => new Date(b.submissionTime) - new Date(a.submissionTime));
    },
    
    // 分页后的请假申请列表
    paginatedRequests() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredRequests.slice(start, end);
    }
  },
  mounted() {
    this.loadLeaveRequests();
  },
  methods: {
    // 加载请假申请列表
    async loadLeaveRequests() {
      try {
        this.loading = true;
        // 从后端API获取数据
        const response = await this.$axios.get('/api/leave-requests', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        this.requests = response.data.data || [];
      } catch (error) {
        console.error('加载请假申请失败:', error);
        this.requests = [];
        this.showNotification('加载失败', '加载请假申请数据失败，请检查后端服务', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    // 打开创建对话框
    openCreateDialog() {
      this.resetForm();
      this.createDialogVisible = true;
    },
    
    // 重置表单
    resetForm() {
      this.leaveForm = {
        leaveType: '',
        startDate: '',
        endDate: '',
        reason: ''
      };
      this.fileList = [];
      if (this.$refs.leaveForm) {
        this.$refs.leaveForm.resetFields();
      }
    },
    
    // 提交请假申请
    async submitLeaveRequest() {
      this.$refs.leaveForm.validate(async (valid) => {
        if (valid) {
          // 验证日期范围
          if (new Date(this.leaveForm.startDate) > new Date(this.leaveForm.endDate)) {
            this.showNotification('日期错误', '开始日期不能晚于结束日期', 'error');
            return;
          }
          
          try {
            // 计算请假天数
            const startDate = new Date(this.leaveForm.startDate);
            const endDate = new Date(this.leaveForm.endDate);
            const days = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
            
            // 准备上传的附件数据
            const attachments = this.fileList.map(file => ({
              name: file.name,
              url: file.url || file.response?.url || '/mock/attachment.pdf'
            }));
            
            // 准备提交数据
            const leaveData = {
              ...this.leaveForm,
              days: days,
              status: 'pending',
              statusText: '待审批',
              submissionTime: new Date().toISOString(),
              attachments: attachments,
              // 从localStorage获取当前学生信息（实际项目中应该从后端获取）
              studentId: localStorage.getItem('studentId') || '20201001',
              studentName: localStorage.getItem('studentName') || '张三',
              department: localStorage.getItem('department') || '计算机系',
              className: localStorage.getItem('className') || '计算机科学与技术1班'
            };
            
            // 实际项目中应该调用后端API
            // const response = await axios.post('/api/leave-requests', leaveData, {
            //   headers: {
            //     'Authorization': `Bearer ${localStorage.getItem('token')}`
            //   }
            // });
            
            // 模拟添加到列表
            leaveData.id = Date.now(); // 模拟生成ID
            this.requests.unshift(leaveData);
            
            this.createDialogVisible = false;
            this.showNotification('提交成功', '请假申请已成功提交，请等待审批', 'success');
          } catch (error) {
            console.error('提交请假申请失败:', error);
            this.showNotification('提交失败', '提交请假申请失败，请稍后重试', 'error');
          }
        }
      });
    },
    
    // 取消创建
    cancelCreate() {
      this.createDialogVisible = false;
    },
    
    // 查看请假详情
    viewRequestDetails(request) {
      this.selectedRequest = { ...request };
      this.detailDialogVisible = true;
    },
    
    // 撤回请假申请
    cancelRequest(requestId) {
      this.$confirm('确定要撤回此请假申请吗？', '确认撤回', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 实际项目中应该调用后端API
          // await axios.put(`/api/leave-requests/${requestId}/cancel`, {}, {
          //   headers: {
          //     'Authorization': `Bearer ${localStorage.getItem('token')}`
          //   }
          // });
          
          // 模拟更新状态
          const request = this.requests.find(req => req.id === requestId);
          if (request) {
            request.status = 'canceled';
            request.statusText = '已撤回';
          }
          
          this.showNotification('撤回成功', '请假申请已成功撤回', 'success');
        } catch (error) {
          console.error('撤回请假申请失败:', error);
          this.showNotification('撤回失败', '撤回请假申请失败，请稍后重试', 'error');
        }
      });
    },
    
    // 处理上传成功
    handleUploadSuccess(response, file, fileList) {
      // 实际项目中，这里应该处理后端返回的文件URL
      file.url = '/mock/attachment.pdf'; // 模拟文件URL
    },
    
    // 处理文件移除
    handleRemove(file, fileList) {
      console.log('移除文件:', file);
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
      return typeMap[type] || 'default';
    },
    
    // 获取状态标签样式
    getStatusTagType(status) {
      const statusMap = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger',
        'canceled': 'info'
      };
      return statusMap[status] || 'default';
    },
    
    // 显示通知
    showNotification(title, message, type = 'info') {
      this.notificationTitle = title;
      this.notificationMessage = message;
      this.notificationType = type;
      this.notificationVisible = true;
    },
    

  }
}
</script>

<style scoped>
.leave-requests {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.create-button {
  display: flex;
  align-items: center;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.status-select {
  width: 150px;
}

.date-picker {
  width: 250px;
}

.content-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding: 20px;
  min-height: 500px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 60px 0;
}

.days-count {
  color: #409eff;
  font-weight: 500;
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
  border-bottom: 1px solid #ebeef5;
}

.detail-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-info h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.status-tag {
  font-size: 14px;
}

.detail-time {
  color: #909399;
  font-size: 14px;
}

.detail-content {
  margin-top: 16px;
}

.detail-descriptions {
  font-size: 14px;
}

.detail-descriptions .el-descriptions__label {
  color: #606266;
  font-weight: 500;
}

.student-id {
  color: #909399;
  margin-left: 8px;
}

.attachments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.attachments-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.attachments-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.attachment-item {
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .status-select,
  .date-picker {
    width: 100% !important;
  }
}

@media (max-width: 768px) {
  .leave-requests {
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
}
</style>