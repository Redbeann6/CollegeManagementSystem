<template>
  <div class="student-leave-application">
    <el-card class="exams-header">
      <h1>请假申请</h1>
      <div class="header-actions">
      </div>
    </el-card>
    
    <el-card class="exams-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="新建请假" name="new">
          <div class="new-application">
            <el-form 
              ref="leaveForm" 
              :model="leaveForm" 
              :rules="rules" 
              label-width="120px"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="请假类型" prop="leaveType">
                    <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型">
                      <el-option label="病假" value="病假" />
                      <el-option label="事假" value="事假" />
                      <el-option label="公假" value="公假" />
                      <el-option label="实习请假" value="实习请假" />
                      <el-option label="其他" value="其他" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开始时间" prop="startTime">
                    <el-date-picker
                      v-model="leaveForm.startTime"
                      type="datetime"
                      placeholder="选择开始时间"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%;"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束时间" prop="endTime">
                    <el-date-picker
                      v-model="leaveForm.endTime"
                      type="datetime"
                      placeholder="选择结束时间"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%;"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="请假时长(天)" prop="duration">
                    <el-input 
                      v-model.number="leaveForm.duration" 
                      placeholder="系统自动计算"
                      disabled
                      style="width: 100%;"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="请假原因" prop="reason">
                    <el-input 
                      v-model="leaveForm.reason" 
                      type="textarea" 
                      :rows="4" 
                      placeholder="请详细描述请假原因"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="证明材料" prop="attachments">
                    <el-upload
                      class="upload-demo"
                      action="#"
                      :auto-upload="false"
                      :on-change="handleFileChange"
                      :file-list="fileList"
                      :multiple="true"
                      accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                      :limit="5"
                    >
                      <el-button size="small" type="primary">点击上传</el-button>
                      <div slot="tip" class="el-upload__tip">
                        支持上传PDF、图片、Word文件，最多上传5个文件，单个文件不超过5MB
                      </div>
                    </el-upload>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="联系人信息">
                    <el-row :gutter="20">
                      <el-col :span="11">
                        <el-form-item prop="contactName">
                          <el-input v-model="leaveForm.contactName" placeholder="联系人姓名" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="11">
                        <el-form-item prop="contactPhone">
                          <el-input v-model="leaveForm.contactPhone" placeholder="联系人电话" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item>
                    <el-checkbox v-model="leaveForm.agreeTerms">
                      本人承诺所提供的请假信息真实有效，如有虚假，愿意承担相应责任
                    </el-checkbox>
                  </el-form-item>
                </el-col>
                <el-col :span="24" style="text-align: center;">
                  <el-form-item>
                    <el-button type="primary" @click="submitApplication">提交申请</el-button>
                    <el-button @click="resetForm">重置表单</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="申请历史" name="history">
          <div class="application-history">
            <div class="history-filters">
              <el-select 
                v-model="historyFilters.status" 
                placeholder="筛选状态" 
                clearable
                style="width: 150px; margin-right: 12px;"
              >
                <el-option label="待审核" value="待审核" />
                <el-option label="已通过" value="已通过" />
                <el-option label="已拒绝" value="已拒绝" />
                <el-option label="已取消" value="已取消" />
              </el-select>
              <el-select 
                v-model="historyFilters.type" 
                placeholder="筛选类型" 
                clearable
                style="width: 150px; margin-right: 12px;"
              >
                <el-option label="病假" value="病假" />
                <el-option label="事假" value="事假" />
                <el-option label="公假" value="公假" />
                <el-option label="实习请假" value="实习请假" />
                <el-option label="其他" value="其他" />
              </el-select>
              <el-date-picker
                v-model="historyFilters.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 300px;"
              />
            </div>
            
            <div class="history-table">
              <el-table 
                :data="filteredApplications" 
                style="width: 100%"
                @row-click="viewApplicationDetail"
              >
                <el-table-column prop="leaveType" label="请假类型" width="120" />
                <el-table-column prop="startTime" label="开始时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.startTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="endTime" label="结束时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.endTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="duration" label="请假时长(天)" width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTag(scope.row.status)">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="applyTime" label="申请时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.applyTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="reviewTime" label="审核时间" width="180">
                  <template #default="scope">
                    {{ scope.row.reviewTime ? formatDateTime(scope.row.reviewTime) : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="reviewer" label="审核人" width="120">
                  <template #default="scope">
                    {{ scope.row.reviewer || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="scope">
                    <template v-if="scope.row.status === '待审核'">
                      <el-button type="danger" size="small" @click.stop="cancelApplication(scope.row.id)">
                        取消
                      </el-button>
                    </template>
                    <template v-else>
                      <el-button type="primary" size="small" @click.stop="viewApplicationDetail(scope.row)">
                        详情
                      </el-button>
                    </template>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="pagination" v-if="filteredApplications.length > 0">
              <el-pagination
                background
                layout="prev, pager, next, jumper, sizes, total"
                :total="filteredApplications.length"
                :page-size="pageSize"
                :current-page="currentPage"
                :page-sizes="[5, 10, 20, 50]"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
            
            <div v-if="filteredApplications.length === 0" class="empty-state">
              <el-empty description="暂无请假申请记录" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 申请详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="selectedApplication?.leaveType || '申请详情'" 
      width="60%"
    >
      <div v-if="selectedApplication" class="application-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-descriptions border :column="2">
            <el-descriptions-item label="申请人">{{ selectedApplication.applicantName }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ selectedApplication.studentId }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ selectedApplication.className }}</el-descriptions-item>
            <el-descriptions-item label="请假类型">{{ selectedApplication.leaveType }}</el-descriptions-item>
            <el-descriptions-item label="请假时长">{{ selectedApplication.duration }}天</el-descriptions-item>
            <el-descriptions-item label="申请状态">
              <el-tag :type="getStatusTag(selectedApplication.status)">
                {{ selectedApplication.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDateTime(selectedApplication.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(selectedApplication.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatDateTime(selectedApplication.applyTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h3>请假原因</h3>
          <div class="reason-content">
            {{ selectedApplication.reason }}
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedApplication.attachments && selectedApplication.attachments.length > 0">
          <h3>证明材料</h3>
          <div class="attachments">
            <div v-if="selectedApplication.attachments && selectedApplication.attachments.length > 0" class="attachment-list">
              <div v-for="(file, index) in selectedApplication.attachments" :key="index" class="attachment-item">
                <el-icon class="attachment-icon"><Document /></el-icon>
                <span class="attachment-name">{{ file.name }}</span>
                <el-button
                  type="text"
                  size="small"
                  @click.stop="downloadAttachment(file)"
                  class="download-button"
                >
                  下载
                </el-button>
              </div>
            </div>
            <div v-else class="no-attachments">暂无证明材料</div>
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedApplication.status !== '待审核' && selectedApplication.status !== '已取消'">
          <h3>审核信息</h3>
          <el-descriptions border :column="2">
            <el-descriptions-item label="审核人">{{ selectedApplication.reviewer || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ formatDateTime(selectedApplication.reviewTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核意见" :span="2">
              <div class="review-comment" v-if="selectedApplication.reviewComment">
                {{ selectedApplication.reviewComment }}
              </div>
              <div v-else class="no-comment">暂无审核意见</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
    
    <!-- 成功提示将通过ElMessage API调用 -->
  </div>
</template>

<script>
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

export default {
  name: 'LeaveApplication',
  components: {
    Document
  },
  data() {
    return {
      activeTab: 'new',
      leaveForm: {
        leaveType: '',
        startTime: '',
        endTime: '',
        duration: 0,
        reason: '',
        contactName: '',
        contactPhone: '',
        agreeTerms: false
      },
      fileList: [],
      applications: [],
      selectedApplication: null,
      detailDialogVisible: false,

      historyFilters: {
        status: '',
        type: '',
        dateRange: null
      },
      currentPage: 1,
      pageSize: 10,
      rules: {
        leaveType: [
          { required: true, message: '请选择请假类型', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请选择开始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' },
          {
            validator: (rule, value, callback) => {
              if (this.leaveForm.startTime && value && new Date(value) <= new Date(this.leaveForm.startTime)) {
                callback(new Error('结束时间必须晚于开始时间'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        reason: [
          { required: true, message: '请描述请假原因', trigger: 'blur' },
          { min: 10, message: '请假原因至少10个字符', trigger: 'blur' }
        ],
        contactName: [
          { required: true, message: '请输入联系人姓名', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: '请输入联系人电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        agreeTerms: [
          { 
            validator: (rule, value, callback) => {
              if (!value) {
                callback(new Error('请确认承诺信息'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ]
      }
    }
  },
  computed: {
    // 根据筛选条件过滤申请记录
    filteredApplications() {
      return this.applications.filter(app => {
        const matchesStatus = !this.historyFilters.status || app.status === this.historyFilters.status
        const matchesType = !this.historyFilters.type || app.leaveType === this.historyFilters.type
        let matchesDate = true
        
        if (this.historyFilters.dateRange && this.historyFilters.dateRange.length === 2) {
          const [startDate, endDate] = this.historyFilters.dateRange
          const appDate = new Date(app.applyTime).toISOString().split('T')[0]
          matchesDate = appDate >= startDate && appDate <= endDate
        }
        
        return matchesStatus && matchesType && matchesDate
      }).sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime))
    },
    
    // 分页后的申请记录
    paginatedApplications() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredApplications.slice(start, end)
    }
  },
  watch: {
    // 监听开始和结束时间变化，自动计算请假时长
    'leaveForm.startTime': {
      handler() {
        this.calculateDuration()
      },
      deep: true
    },
    'leaveForm.endTime': {
      handler() {
        this.calculateDuration()
      },
      deep: true
    },
    
    // 监听筛选条件变化，重置页码
    historyFilters: {
      handler() {
        this.currentPage = 1
      },
      deep: true
    }
  },
  mounted() {
    this.loadApplications()
  },
  methods: {
    // 计算请假时长
    calculateDuration() {
      if (this.leaveForm.startTime && this.leaveForm.endTime) {
        const start = new Date(this.leaveForm.startTime)
        const end = new Date(this.leaveForm.endTime)
        
        if (end > start) {
          const diffTime = Math.abs(end - start)
          const diffDays = diffTime / (1000 * 60 * 60 * 24)
          this.leaveForm.duration = Math.round(diffDays * 10) / 10 // 保留一位小数
        }
      }
    },
    
    // 加载申请历史
    async loadApplications() {
      try {
        const response = await this.$axios.get('/api/student/leave-requests', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          this.applications = response.data.data.map(item => ({
            id: item.id,
            applicantName: item.studentName,
            studentId: item.studentIdNumber,
            className: item.className || '未知班级',
            leaveType: item.leaveType,
            startTime: item.startDate,
            endTime: item.endDate,
            duration: this.calculateDateDiff(item.startDate, item.endDate),
            reason: item.reason,
            contactName: item.emergencyContactName || '',
            contactPhone: item.emergencyContactPhone || '',
            applyTime: item.createdAt,
            status: item.status,
            reviewer: item.handlerName,
            reviewTime: item.updatedAt,
            reviewComment: item.remarks
          }));
        } else {
          console.error('获取请假申请记录失败:', response.data.message);
          ElMessage.error(response.data.message || '获取请假申请记录失败');
          this.applications = [];
        }
      } catch (error) {
        console.error('加载申请记录失败:', error);
        ElMessage.error('加载申请记录失败，请稍后重试');
        this.applications = [];
      }
    },
    
    // 处理文件上传
    handleFileChange(file, fileList) {
      this.fileList = fileList.slice(-5) // 只保留最近5个文件
    },
    
    // 提交申请
    async submitApplication() {
      this.$refs.leaveForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const leaveRequestData = {
            startDate: this.leaveForm.startTime ? this.leaveForm.startTime.split(' ')[0] : null,
            endDate: this.leaveForm.endTime ? this.leaveForm.endTime.split(' ')[0] : null,
            reason: this.leaveForm.reason,
            leaveType: this.leaveForm.leaveType,
            emergencyContactName: this.leaveForm.contactName,
            emergencyContactPhone: this.leaveForm.contactPhone
          };
          
          const response = await this.$axios.post('/leave-requests', leaveRequestData, {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          });
          
          if (response.data.success) {
            // 重新加载申请列表
            this.loadApplications();
            
            // 重置表单
            this.resetForm();
            
            // 显示成功提示
            this.showSuccess('提交成功', '请假申请已成功提交，请等待审核');
          } else {
            ElMessage.error(response.data.message || '提交申请失败');
          }
        } catch (error) {
          console.error('提交申请失败:', error)
          ElMessage.error('提交申请失败，请稍后重试')
        }
      })
    },
    
    // 重置表单
    resetForm() {
      this.leaveForm = {
        leaveType: '',
        startTime: '',
        endTime: '',
        duration: 0,
        reason: '',
        contactName: '',
        contactPhone: '',
        agreeTerms: false
      }
      this.fileList = []
      this.$refs.leaveForm.resetFields()
    },
    
    // 查看申请详情
    viewApplicationDetail(application) {
      this.selectedApplication = application
      this.detailDialogVisible = true
    },
    
    // 取消申请
    async cancelApplication(id) {
      this.$confirm('确定要取消该请假申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.put(`/leave-requests/${id}/cancel`, {}, {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          });
          
          if (response.data.success) {
            // 重新加载申请列表
            this.loadApplications();
            ElMessage.success('申请已取消');
          } else {
            ElMessage.error(response.data.message || '取消申请失败');
          }
        } catch (error) {
          console.error('取消申请失败:', error);
          ElMessage.error('取消申请失败，请稍后重试');
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 下载附件
    downloadAttachment(file) {
      // 实际项目中应该调用后端API下载文件
      ElMessage.info('附件下载功能开发中')
    },
    
    // 显示成功提示
    showSuccess(title, message) {
      ElMessage.success({
        title: title,
        message: message
      })
    },
    
    // 计算日期差
    calculateDateDiff(startDate, endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      const diffTime = Math.abs(end - start);
      const diffDays = diffTime / (1000 * 60 * 60 * 24);
      return Math.round(diffDays * 10) / 10; // 保留一位小数
    },
    
    // 获取状态标签类型
    getStatusTag(status) {
      const tagMap = {
        '待审核': 'warning',
        '已通过': 'success',
        '已拒绝': 'danger',
        '已取消': 'info'
      }
      return tagMap[status] || 'info'
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return ''
      const date = new Date(dateTimeString)
      return date.toLocaleString('zh-CN')
    },
    
    // 获取学生姓名
    getStudentName() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        return userInfo.name || '学生'
      }
      return '学生'
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
    
    // 模拟申请数据
    getMockApplications() {
      const userInfoStr = localStorage.getItem('userInfo')
      const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}
      const name = userInfo.name || '张同学'
      
      return [
        {
          id: 1,
          applicantName: name,
          studentId: '20230001',
          className: '计算机科学1班',
          leaveType: '病假',
          startTime: '2024-01-20T08:00:00',
          endTime: '2024-01-22T18:00:00',
          duration: 2.5,
          reason: '因感冒发烧，需要在家休息几天，医生建议休息至少两天。',
          contactName: '张父',
          contactPhone: '13900139000',
          applyTime: '2024-01-19T10:30:00',
          status: '已通过',
          reviewer: '王老师',
          reviewTime: '2024-01-19T14:20:00',
          reviewComment: '情况属实，同意请假。',
          attachments: [
            {
              name: '诊断证明.pdf',
              size: 1024 * 512,
              url: '#',
              type: 'application/pdf'
            }
          ]
        },
        {
          id: 2,
          applicantName: name,
          studentId: '20230001',
          className: '计算机科学1班',
          leaveType: '事假',
          startTime: '2024-01-15T00:00:00',
          endTime: '2024-01-15T23:59:59',
          duration: 1,
          reason: '家中有事需要处理，需要请假一天。',
          contactName: '张母',
          contactPhone: '13700137000',
          applyTime: '2024-01-12T16:45:00',
          status: '已拒绝',
          reviewer: '王老师',
          reviewTime: '2024-01-12T17:30:00',
          reviewComment: '请假理由不充分，请提供具体情况说明。',
          attachments: []
        },
        {
          id: 3,
          applicantName: name,
          studentId: '20230001',
          className: '计算机科学1班',
          leaveType: '公假',
          startTime: '2024-02-01T08:00:00',
          endTime: '2024-02-05T18:00:00',
          duration: 5,
          reason: '代表学校参加省级计算机竞赛。',
          contactName: '李老师',
          contactPhone: '13600136000',
          applyTime: '2024-01-10T09:15:00',
          status: '待审核',
          reviewer: null,
          reviewTime: null,
          reviewComment: null,
          attachments: [
            {
              name: '竞赛通知.pdf',
              size: 1024 * 768,
              url: '#',
              type: 'application/pdf'
            },
            {
              name: '参赛名单.docx',
              size: 1024 * 384,
              url: '#',
              type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
            }
          ]
        },
        {
          id: 4,
          applicantName: name,
          studentId: '20230001',
          className: '计算机科学1班',
          leaveType: '实习请假',
          startTime: '2023-12-10T00:00:00',
          endTime: '2023-12-20T23:59:59',
          duration: 11,
          reason: '前往XX科技公司进行专业实习，已获得学院批准。',
          contactName: '公司联系人',
          contactPhone: '13500135000',
          applyTime: '2023-11-25T14:20:00',
          status: '已通过',
          reviewer: '王老师',
          reviewTime: '2023-11-25T16:40:00',
          reviewComment: '同意实习请假，请按时完成实习报告。',
          attachments: [
            {
              name: '实习协议.pdf',
              size: 1024 * 1024,
              url: '#',
              type: 'application/pdf'
            },
            {
              name: '实习证明.jpg',
              size: 1024 * 2048,
              url: '#',
              type: 'image/jpeg'
            }
          ]
        },
        {
          id: 5,
          applicantName: name,
          studentId: '20230001',
          className: '计算机科学1班',
          leaveType: '其他',
          startTime: '2023-11-01T08:00:00',
          endTime: '2023-11-01T12:00:00',
          duration: 0.5,
          reason: '参加XX考试报名。',
          contactName: '张同学',
          contactPhone: '13800138000',
          applyTime: '2023-10-28T11:30:00',
          status: '已取消',
          reviewer: null,
          reviewTime: null,
          reviewComment: null,
          attachments: []
        }
      ]
    }
  }
}
</script>

<style scoped>
.student-leave-application {
  padding: 20px;
  background-color: var(--background-color);
}

.exams-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.exams-header h1 {
  margin-bottom: 16px;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.exams-content {
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.new-application {
  padding: 20px 0;
}

.application-history {
  padding: 20px 0;
}

/* 历史筛选器样式优化 */
.history-filters {
  display: flex;
  align-items: center;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.history-table {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #f0f0f0;
}

.history-table .el-table__header th {
  background-color: #f8f9fa !important;
  color: #303133;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 2px solid #e9ecef;
}

.history-table .el-table__row:hover {
  background-color: #f8f9fa !important;
}

.history-table .el-table__row {
  transition: background-color 0.3s ease;
}

/* 空状态样式优化 */
.empty-state {
  padding: 80px 20px;
  text-align: center;
  color: #909399;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
}

.empty-state .el-empty__image {
  width: 120px;
  height: 120px;
}

.empty-state .el-empty__description {
  font-size: 16px;
  color: #606266;
  font-weight: 500;
  margin-top: 20px;
}

/* 分页样式优化 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 28px;
}

.pagination .el-pagination {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 详情页面样式 */
.application-detail {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.detail-section h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 18px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.reason-content {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-line;
  border-left: 4px solid #409eff;
}

.attachments {
  padding: 8px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  gap: 12px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  max-width: 300px;
}

.attachment-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.review-comment {
  padding: 12px;
  background-color: #f0f2f5;
  border-radius: 8px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-line;
}

.no-comment {
  color: #909399;
  font-style: italic;
}

/* 标签页样式优化 */
.tabs-container .el-tabs__header {
  margin-bottom: 24px;
}

.tabs-container .el-tabs__nav-wrap::after {
  background-color: #f0f0f0;
}

.tabs-container .el-tabs__active-bar {
  height: 3px;
  background-color: #409eff;
}

.tabs-container .el-tabs__item {
  font-size: 16px;
  color: #606266;
  padding: 0 24px;
  transition: all 0.3s ease;
}

.tabs-container .el-tabs__item:hover {
  color: #409eff;
}

.tabs-container .el-tabs__item.is-active {
  color: #409eff;
  font-weight: 600;
}

/* 状态标签样式优化 */
.status-tag {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: all 0.3s ease;
}

/* 表单样式优化 */
.el-form {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #f0f0f0;
}

.el-form-item__label {
  font-weight: 500;
  color: #303133;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-button {
  border-radius: 6px;
  padding: 12px 24px;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(64, 158, 255, 0.3);
}

/* 操作按钮样式优化 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons .el-button {
  padding: 4px 12px;
  font-size: 13px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 时长计算提示样式 */
.duration-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #ecf5ff;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.duration-info .el-icon-info {
  color: #409eff;
  font-size: 16px;
}

.duration-info .duration-text {
  color: #409eff;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .leave-application {
    padding: 20px;
  }
  
  .application-header,
  .application-content {
    padding: 24px;
  }
  
  .history-filters {
    gap: 12px;
  }
}

@media (max-width: 1024px) {
  .application-header h1 {
    font-size: 24px;
  }
  
  .detail-section h3 {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .leave-application {
    padding: 16px;
  }
  
  .application-header,
  .application-content {
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
  
  .application-header {
    margin-bottom: 24px;
  }
  
  .application-header h1 {
    font-size: 22px;
  }
  
  .el-form {
    padding: 20px;
  }
  
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .history-filters {
    flex-direction: column;
    align-items: stretch;
    padding: 16px;
  }
  
  .el-select,
  .el-date-picker {
    width: 100% !important;
  }
  
  .tabs-container .el-tabs__item {
    font-size: 14px;
    padding: 0 16px;
  }
  
  .empty-state {
    padding: 60px 16px;
  }
  
  .attachment-item {
    max-width: 100%;
    width: 100%;
  }
  
  .reason-content {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .leave-application {
    padding: 12px;
  }
  
  .application-header,
  .application-content {
    padding: 16px;
    border-radius: 8px;
  }
  
  .application-header h1 {
    font-size: 20px;
  }
  
  .el-form {
    padding: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    padding: 6px 8px;
    font-size: 12px;
  }
  
  .pagination {
    justify-content: center;
  }
}

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>