<template>
  <div class="notification-management-container">
    <div class="page-header">
      <h1>通知管理</h1>
      <p>查看和管理系统通知</p>
    </div>

    <!-- 主要内容区域 -->
    <el-card class="notification-management-card">
      <!-- 筛选搜索区域 -->
      <div class="search-filter-section">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-select v-model="selectedStatus" placeholder="通知状态" clearable>
              <el-option label="全部状态" value=""></el-option>
              <el-option label="未读" value="unread"></el-option>
              <el-option label="已读" value="read"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="selectedType" placeholder="通知类型" clearable>
              <el-option label="全部类型" value=""></el-option>
              <el-option label="系统通知" value="system"></el-option>
              <el-option label="课程通知" value="course"></el-option>
              <el-option label="考试通知" value="exam"></el-option>
              <el-option label="其他通知" value="other"></el-option>
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
              placeholder="搜索通知标题或内容"
              prefix-icon="el-icon-search"
              clearable
              @keyup.enter="searchNotifications"
            ></el-input>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="12">
            <el-select v-model="selectedCourse" placeholder="相关课程" clearable>
              <el-option label="全部课程" value=""></el-option>
              <el-option
                v-for="course in teachingCourses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="12" class="text-right">
            <div class="action-buttons">
              <el-button type="primary" icon="el-icon-check" @click="markAllAsRead" :disabled="!hasUnreadNotifications">
                全部标记为已读
              </el-button>
              <el-button type="danger" icon="el-icon-delete" @click="batchDelete" :disabled="selectedNotifications.length === 0">
                批量删除
              </el-button>
              <el-button type="info" icon="el-icon-download" @click="exportNotifications">
                导出通知
              </el-button>
              <el-button type="info" icon="el-icon-refresh" @click="refreshNotifications">
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
              <div class="stat-value">{{ notificationStats.total }}</div>
              <div class="stat-label">总通知数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card warning">
              <div class="stat-value">{{ notificationStats.unread }}</div>
              <div class="stat-label">未读通知</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card success">
              <div class="stat-value">{{ notificationStats.system }}</div>
              <div class="stat-label">系统通知</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card info">
              <div class="stat-value">{{ notificationStats.course }}</div>
              <div class="stat-label">课程通知</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 通知列表 -->
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="paginatedNotifications"
          style="width: 100%"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-badge :value="scope.row.status === 'unread' ? '未读' : ''" :hidden="scope.row.status !== 'unread'">
                <span class="status-dot" :class="scope.row.status"></span>
              </el-badge>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="通知标题" min-width="200">
            <template #default="scope">
              <span class="notification-title" :class="{ 'unread': scope.row.status === 'unread' }" @click="viewNotificationDetails(scope.row)">
                {{ scope.row.title }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="通知类型" width="100">
            <template #default="scope">
              <el-tag :type="getNotificationTypeTagType(scope.row.type)">
                {{ getNotificationTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sourceCourse" label="相关课程" width="120"></el-table-column>
          <el-table-column prop="sender" label="发送者" width="100"></el-table-column>
          <el-table-column prop="sendTime" label="发送时间" width="180"></el-table-column>
          <el-table-column prop="readTime" label="阅读时间" width="180" v-if="selectedStatus === 'read' || !selectedStatus"></el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewNotificationDetails(scope.row)">
                <i class="el-icon-view"></i> 查看
              </el-button>
              <el-button type="success" size="small" @click="markAsRead(scope.row)" v-if="scope.row.status === 'unread'">
                <i class="el-icon-check"></i> 标记已读
              </el-button>
              <el-button type="danger" size="small" @click="deleteNotification(scope.row)">
                <i class="el-icon-delete"></i> 删除
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
          :total="filteredNotifications.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog
      :title="currentNotification.title || '通知详情'"
      v-model="showDetailsDialog"
      width="700px"
      @close="handleDetailsDialogClose"
    >
      <div class="notification-details-container">
        <!-- 通知头部信息 -->
        <div class="notification-header">
          <div class="header-info">
            <el-tag :type="getNotificationTypeTagType(currentNotification.type)">
              {{ getNotificationTypeText(currentNotification.type) }}
            </el-tag>
            <span class="sender-info">发送者：{{ currentNotification.sender }}</span>
            <span class="send-time">发送时间：{{ currentNotification.sendTime }}</span>
            <span class="source-course" v-if="currentNotification.sourceCourse">
              相关课程：{{ currentNotification.sourceCourse }}
            </span>
          </div>
          <el-divider></el-divider>
        </div>

        <!-- 通知内容 -->
        <div class="notification-content" v-html="currentNotification.content"></div>

        <!-- 通知附件 -->
        <div class="attachments-section" v-if="currentNotification.attachments && currentNotification.attachments.length > 0">
          <h3>附件</h3>
          <el-upload
            action=""
            :limit="5"
            :file-list="attachmentFileList"
            :auto-upload="false"
            :disabled="true"
            list-type="text"
          >
          </el-upload>
        </div>

        <!-- 通知操作记录 -->
        <div class="notification-actions" v-if="currentNotification.status === 'read'">
          <el-divider></el-divider>
          <div class="action-info">
            <span>已读时间：{{ currentNotification.readTime }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleDetailsDialogClose">关闭</el-button>
        <el-button type="primary" @click="replyNotification" v-if="currentNotification.replyEnabled">
          回复
        </el-button>
      </template>
    </el-dialog>

    <!-- 回复通知对话框 -->
    <el-dialog
      title="回复通知"
      v-model="showReplyDialog"
      width="600px"
    >
      <div class="reply-container">
        <el-form :model="replyForm" :rules="replyRules" ref="replyForm" label-width="80px">
          <el-form-item label="回复内容" prop="content">
            <el-input
              v-model="replyForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入回复内容"
            ></el-input>
          </el-form-item>
          <el-form-item label="附件">
            <el-upload
              action=""
              :limit="3"
              :file-list="replyFileList"
              :auto-upload="false"
              list-type="text"
            >
              <el-button size="small" type="primary">
                <i class="el-icon-upload2"></i> 选择文件
              </el-button>
              <div slot="tip" class="el-upload__tip">
                最多只能上传3个文件
              </div>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="handleReplyDialogClose">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>

    <!-- 批量操作确认对话框 -->
    <el-dialog
      title="批量删除"
      v-model="showBatchDeleteDialog"
      width="500px"
    >
      <div class="batch-delete-container">
        <p class="delete-info">
          您确定要删除选中的 <span class="highlight">{{ selectedNotifications.length }}</span> 条通知吗？
          <br>
          <span class="warning-text">删除后将无法恢复，请谨慎操作！</span>
        </p>
      </div>

      <template #footer>
        <el-button @click="showBatchDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmBatchDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherNotificationManagement',
  data() {
    return {
      // 筛选条件
      selectedStatus: '',
      selectedType: '',
      selectedCourse: '',
      dateRange: [],
      searchQuery: '',
      
      // 分页参数
      currentPage: 1,
      pageSize: 10,
      
      // 数据加载状态
      loading: false,
      
      // 教学课程
      teachingCourses: [],
      
      // 通知数据
      notifications: [],
      
      // 选中的通知
      selectedNotifications: [],
      
      // 弹出框状态
      showDetailsDialog: false,
      showReplyDialog: false,
      showBatchDeleteDialog: false,
      
      // 当前通知
      currentNotification: {},
      
      // 回复表单
      replyForm: {
        content: ''
      },
      
      // 回复表单验证规则
      replyRules: {
        content: [
          { required: true, message: '请输入回复内容', trigger: 'blur' },
          { min: 5, max: 500, message: '回复内容长度在 5 到 500 个字符', trigger: 'blur' }
        ]
      },
      
      // 附件文件列表
      attachmentFileList: [],
      replyFileList: [],
      
      // 统计数据
      notificationStats: {
        total: 0,
        unread: 0,
        system: 0,
        course: 0
      }
    }
  },
  
  computed: {
    // 过滤后的通知
    filteredNotifications() {
      let filtered = [...this.notifications]
      
      // 按状态筛选
      if (this.selectedStatus) {
        filtered = filtered.filter(notification => notification.status === this.selectedStatus)
      }
      
      // 按类型筛选
      if (this.selectedType) {
        filtered = filtered.filter(notification => notification.type === this.selectedType)
      }
      
      // 按课程筛选
      if (this.selectedCourse) {
        filtered = filtered.filter(notification => notification.courseId === this.selectedCourse)
      }
      
      // 按日期范围筛选
      if (this.dateRange && this.dateRange.length === 2) {
        filtered = filtered.filter(notification => {
          const notificationDate = new Date(notification.sendTime.split(' ')[0])
          const startDate = new Date(this.dateRange[0])
          const endDate = new Date(this.dateRange[1])
          return notificationDate >= startDate && notificationDate <= endDate
        })
      }
      
      // 搜索通知
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(notification => 
          notification.title.toLowerCase().includes(query) || 
          notification.content.toLowerCase().includes(query)
        )
      }
      
      // 更新统计数据
      this.updateNotificationStats(filtered)
      
      // 按发送时间降序排序
      return filtered.sort((a, b) => new Date(b.sendTime) - new Date(a.sendTime))
    },
    
    // 分页后的通知
    paginatedNotifications() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredNotifications.slice(start, end)
    },
    
    // 是否有未读通知
    hasUnreadNotifications() {
      return this.notifications.some(notification => notification.status === 'unread')
    }
  },
  
  mounted() {
    // 加载教学课程
    this.loadTeachingCourses()
    // 加载通知
    this.loadNotifications()
  },
  
  methods: {
    // 加载教学课程
    async loadTeachingCourses() {
      try {
        const response = await this.$axios.get('/teacher/data/courses', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.teachingCourses = response.data.data || []
          console.log('教学课程加载完成')
        } else {
          console.error('获取课程列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取课程列表失败')
        }
      } catch (error) {
        console.error('加载教学课程失败:', error)
        this.$message.error('加载教学课程失败，请稍后重试')
      }
    },
    
    async loadNotifications() {
      this.loading = true
      try {
        const response = await this.$axios.get('/teacher/notifications', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.notifications = response.data.data || []
          console.log('通知加载完成')
        } else {
          console.error('获取通知失败:', response.data.message)
          this.$message.error(response.data.message || '获取通知失败')
        }
      } catch (error) {
        console.error('加载通知失败:', error)
        this.$message.error('加载通知失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    

    
    // 生成随机时间
    generateRandomTime(startDate, endDate) {
      const start = new Date(startDate)
      const end = new Date(endDate)
      const randomDate = new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()))
      const hours = Math.floor(Math.random() * 24)
      const minutes = Math.floor(Math.random() * 60)
      const seconds = Math.floor(Math.random() * 60)
      return `${randomDate.toISOString().split('T')[0]} ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    },
    
    // 生成在某个时间之后的随机时间
    generateRandomTimeAfter(baseTime) {
      const base = new Date(baseTime)
      const hoursOffset = Math.floor(Math.random() * 48) + 1 // 1-48小时后
      const randomDate = new Date(base.getTime() + hoursOffset * 60 * 60 * 1000)
      const hours = Math.floor(Math.random() * 24)
      const minutes = Math.floor(Math.random() * 60)
      const seconds = Math.floor(Math.random() * 60)
      return `${randomDate.toISOString().split('T')[0]} ${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    },
    
    // 生成通知标题
    generateNotificationTitle(type) {
      const titles = {
        system: [
          '系统更新通知',
          '教师信息更新提醒',
          '新功能上线通知',
          '系统维护公告',
          '紧急通知：系统安全提示'
        ],
        course: [
          '课程内容更新提醒',
          '作业提交截止时间提醒',
          '课程调课通知',
          '实验课安排变更',
          '教学大纲修订通知'
        ],
        exam: [
          '期中考试安排通知',
          '期末考试成绩录入提醒',
          '考试时间变更通知',
          '实验考试准备事项',
          '考试监考安排'
        ],
        other: [
          '会议通知',
          '学术讲座安排',
          '教学竞赛报名通知',
          '教师培训通知',
          '年度教学评价提醒'
        ]
      }
      const typeTitles = titles[type] || titles.other
      return typeTitles[Math.floor(Math.random() * typeTitles.length)]
    },
    
    // 生成通知内容
    generateNotificationContent(type) {
      const contents = {
        system: [
          '<p>尊敬的老师：</p><p>系统将于本周末（周六00:00-周日06:00）进行定期维护，请提前做好准备，避免影响您的正常工作。维护期间，系统可能会暂时无法访问。</p><p>感谢您的理解与支持！</p>',
          '<p>尊敬的老师：</p><p>您的教师信息已成功更新，请登录系统查看详情。如有疑问，请联系系统管理员。</p>',
          '<p>尊敬的老师：</p><p>系统已上线新功能，包括成绩批量导入导出、学生出勤统计等。请查看系统帮助文档了解详细功能介绍。</p>'
        ],
        course: [
          '<p>尊敬的老师：</p><p>您教授的《数据结构》课程内容已更新，请登录课程管理页面查看最新的教学资料。</p>',
          '<p>尊敬的老师：</p><p>本周作业提交截止时间为周五晚上23:59，请提醒学生按时提交。</p>',
          '<p>尊敬的老师：</p><p>因教室调整，原定周四下午的数据结构课程将调整至周五下午，上课地点不变。</p>'
        ],
        exam: [
          '<p>尊敬的老师：</p><p>期中考试安排如下：<br>1. 考试时间：下周三上午9:00-11:00<br>2. 考试地点：教学楼B201<br>3. 请提前15分钟到达考场准备监考。</p>',
          '<p>尊敬的老师：</p><p>期末考试成绩录入系统已开放，请于下周五前完成所有学生的成绩录入工作。</p>',
          '<p>尊敬的老师：</p><p>实验考试将于下周一至周五进行，请安排好学生的考试分组和时间。</p>'
        ],
        other: [
          '<p>尊敬的老师：</p><p>教师工作会议将于本周五下午2:30在行政楼301会议室召开，请准时参加。</p>',
          '<p>尊敬的老师：</p><p>下周将举办人工智能学术讲座，由知名专家主讲，欢迎参加。具体时间地点详见附件。</p>',
          '<p>尊敬的老师：</p><p>年度教学评价工作已开始，请登录系统完成自评和互评工作。</p>'
        ]
      }
      const typeContents = contents[type] || contents.other
      return typeContents[Math.floor(Math.random() * typeContents.length)]
    },
    
    // 生成发送者
    generateSender(type) {
      if (type === 'system') {
        return '系统管理员'
      } else {
        const senders = ['教务处', '系主任', '教学秘书', '系统管理员']
        return senders[Math.floor(Math.random() * senders.length)]
      }
    },
    
    // 更新通知统计
    updateNotificationStats(notifications) {
      const total = notifications.length
      const unread = notifications.filter(n => n.status === 'unread').length
      const system = notifications.filter(n => n.type === 'system').length
      const course = notifications.filter(n => n.type === 'course').length
      
      this.notificationStats = { total, unread, system, course }
    },
    
    // 搜索通知
    searchNotifications() {
      this.currentPage = 1
    },
    
    // 刷新通知列表
    refreshNotifications() {
      this.selectedStatus = ''
      this.selectedType = ''
      this.selectedCourse = ''
      this.dateRange = []
      this.searchQuery = ''
      this.currentPage = 1
      this.loadNotifications()
    },
    
    // 查看通知详情
    viewNotificationDetails(notification) {
      this.currentNotification = { ...notification }
      // 初始化附件列表
      this.attachmentFileList = notification.attachments || []
      // 自动标记为已读
      if (notification.status === 'unread') {
        this.markAsRead(notification, false) // 静默标记已读，不显示消息提示
      }
      this.showDetailsDialog = true
    },
    
    // 标记为已读
    markAsRead(notification, showMessage = true) {
      try {
        // 实际项目中应该调用后端API
        // await axios.put(`/notifications/${notification.id}/read`)
        
        // 更新本地数据
        const notif = this.notifications.find(n => n.id === notification.id)
        if (notif) {
          notif.status = 'read'
          notif.readTime = new Date().toLocaleString()
        }
        
        if (showMessage) {
          this.$message.success('已标记为已读')
        }
      } catch (error) {
        console.error('标记已读失败:', error)
        this.$message.error('标记失败，请稍后重试')
      }
    },
    
    // 全部标记为已读
    markAllAsRead() {
      this.$confirm('确定要将所有未读通知标记为已读吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        try {
          // 实际项目中应该调用后端API
          // await axios.put('/notifications/read-all')
          
          // 更新本地数据
          this.notifications.forEach(notification => {
            if (notification.status === 'unread') {
              notification.status = 'read'
              notification.readTime = new Date().toLocaleString()
            }
          })
          
          this.$message.success('已将所有未读通知标记为已读')
        } catch (error) {
          console.error('批量标记已读失败:', error)
          this.$message.error('操作失败，请稍后重试')
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    
    // 删除通知
    deleteNotification(notification) {
      this.$confirm(`确定要删除通知「${notification.title}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.removeNotification(notification.id)
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    
    // 批量删除
    batchDelete() {
      this.showBatchDeleteDialog = true
    },
    
    // 确认批量删除
    confirmBatchDelete() {
      const ids = this.selectedNotifications.map(n => n.id)
      try {
        // 实际项目中应该调用后端API
        // await axios.delete('/notifications/batch', { data: { ids } })
        
        // 更新本地数据
        ids.forEach(id => this.removeNotification(id))
        
        this.$message.success('已成功删除选中的通知')
        this.showBatchDeleteDialog = false
        this.selectedNotifications = []
      } catch (error) {
        console.error('批量删除失败:', error)
        this.$message.error('删除失败，请稍后重试')
      }
    },
    
    // 从列表中移除通知
    removeNotification(id) {
      const index = this.notifications.findIndex(n => n.id === id)
      if (index > -1) {
        this.notifications.splice(index, 1)
      }
    },
    
    // 导出通知
    exportNotifications() {
      // 实际项目中应该调用后端API
      this.$message.success('通知导出成功')
    },
    
    // 回复通知
    replyNotification() {
      this.replyForm = { content: '' }
      this.replyFileList = []
      this.showReplyDialog = true
    },
    
    // 提交回复
    submitReply() {
      this.$refs.replyForm.validate((valid) => {
        if (valid) {
          try {
            // 实际项目中应该调用后端API
            // await axios.post(`/notifications/${this.currentNotification.id}/reply`, {
            //   content: this.replyForm.content,
            //   attachments: this.replyFileList
            // })
            
            this.$message.success('回复成功')
            this.handleReplyDialogClose()
            this.handleDetailsDialogClose()
          } catch (error) {
            console.error('回复失败:', error)
            this.$message.error('回复失败，请稍后重试')
          }
        }
      })
    },
    
    // 获取通知类型文本
    getNotificationTypeText(type) {
      const typeMap = {
        system: '系统',
        course: '课程',
        exam: '考试',
        other: '其他'
      }
      return typeMap[type] || '未知'
    },
    
    // 获取通知类型标签类型
    getNotificationTypeTagType(type) {
      const typeMap = {
        system: 'primary',
        course: 'success',
        exam: 'warning',
        other: 'info'
      }
      return typeMap[type] || 'info'
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedNotifications = selection
    },
    
    // 关闭详情对话框
    handleDetailsDialogClose() {
      this.showDetailsDialog = false
      this.currentNotification = {}
      this.attachmentFileList = []
    },
    
    // 关闭回复对话框
    handleReplyDialogClose() {
      this.showReplyDialog = false
      this.replyForm = { content: '' }
      this.replyFileList = []
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
.notification-management-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.notification-management-card {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* 回复样式 */
.reply-container {
  padding: 10px 0;
}

/* 批量删除样式 */
.batch-delete-container {
  padding: 10px 0;
}

.delete-info {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.highlight {
  color: #f56c6c;
  font-weight: bold;
}

.warning-text {
  color: #f56c6c;
  font-size: 14px;
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
  .notification-management-container {
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
  
  .header-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>