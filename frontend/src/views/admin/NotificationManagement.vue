<template>
  <el-card class="management-header">
    <h1>通知管理</h1>
  </el-card>
  
  <el-card class="hover-card">
    <!-- 筛选和搜索区域 -->
    <div class="search-filters">
      <el-row :gutter="20">
        <el-select v-model="filterOptions.type" placeholder="选择通知类型" style="width: 180px;">
          <el-option label="全部" value="" />
          <el-option label="系统通知" value="SYSTEM" />
          <el-option label="教学通知" value="ACADEMIC" />
          <el-option label="活动通知" value="ACTIVITY" />
          <el-option label="公告" value="ANNOUNCEMENT" />
        </el-select>
        <el-select v-model="filterOptions.status" placeholder="选择状态" style="width: 180px;">
          <el-option label="全部" value="" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
        <el-select v-model="filterOptions.target" placeholder="接收对象" style="width: 180px;">
          <el-option label="全部" value="" />
          <el-option label="全体学生" value="STUDENTS" />
          <el-option label="全体教师" value="TEACHERS" />
          <el-option label="特定系部" value="DEPARTMENTS" />
          <el-option label="特定班级" value="CLASSES" />
          <el-option label="特定用户" value="SPECIFIC_USERS" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 300px;"
        />
        <el-input 
          v-model="filterOptions.keyword" 
          placeholder="输入标题或内容关键词" 
          prefix-icon="el-icon-search" 
          style="width: 280px;"
          @keyup.enter="searchNotifications"
        />
        <el-button type="primary" @click="searchNotifications">
          <i class="el-icon-search"></i> 搜索
        </el-button>
        <el-button @click="resetFilters">
          <i class="el-icon-refresh"></i> 重置
        </el-button>
      </el-row>
    </div>
      
      <!-- 功能按钮区域 -->
      <div class="action-buttons">
        <el-button type="primary" @click="createNotification('PUBLISHED')">
          <i class="el-icon-plus"></i> 发布新通知
        </el-button>
        <el-button type="success" @click="createNotification('DRAFT')">
          <i class="el-icon-edit"></i> 保存为草稿
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedNotifications.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
        <el-button type="primary" @click="batchPublish" :disabled="selectedNotifications.length === 0 || !canBatchPublish">
          <i class="el-icon-check"></i> 批量发布
        </el-button>
        <el-button type="info" @click="exportData">
          <i class="el-icon-download"></i> 导出数据
        </el-button>
        <el-button @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新数据
        </el-button>
      </div>
      
      <!-- 通知列表表格 -->
      <div class="table-section mb-20">
        <el-table 
          v-loading="loading" 
          :data="paginatedNotifications" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
          class="hover-card"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="通知ID" width="100" />
          <el-table-column prop="title" label="标题" min-width="250">
            <template #default="scope">
              <div class="title-cell">
                <div :class="['title-content', { 'unread': scope.row.isRead === false && scope.row.status === 'PUBLISHED' }]">
                  {{ scope.row.title }}
                </div>
                <div class="title-meta">
                  <el-tag :type="getNotificationTypeTag(scope.row.type)">
                    {{ getNotificationTypeText(scope.row.type) }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容预览" min-width="200">
            <template #default="scope">
              <div class="content-preview" @click="viewFullContent(scope.row)">
                {{ getContentPreview(scope.row.content) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="target" label="接收对象" width="120">
            <template #default="scope">
              <el-tag :type="getNotificationTargetTag(scope.row.target)">
                {{ getNotificationTargetText(scope.row.target) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="接收人范围" width="150">
            <template #default="scope">
              <div class="recipient-info" @click="viewRecipients(scope.row)">
                {{ getRecipientPreview(scope.row.recipients) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getNotificationStatusTag(scope.row.status)">
                {{ getNotificationStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publisher" label="发布人" width="120" show-overflow-tooltip />
          <el-table-column prop="publishTime" label="发布时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.publishTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="expireTime" label="过期时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.expireTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="100" />
          <el-table-column label="操作" width="400" fixed="right">
                <template #default="scope">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="viewNotificationDetail(scope.row)"
                    icon="el-icon-view"
                  >
                    查看
                  </el-button>
                  <el-button 
                    v-if="scope.row.status === 'DRAFT'" 
                    type="success" 
                    size="small" 
                    @click="createNotification('PUBLISHED', scope.row)"
                    icon="el-icon-check"
                  >
                    发布
                  </el-button>
                  <el-button 
                    v-if="scope.row.status === 'DRAFT'" 
                    type="warning" 
                    size="small" 
                    @click="createNotification('DRAFT', scope.row)"
                    icon="el-icon-edit"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="deleteNotification(scope.row.id)"
                    icon="el-icon-delete"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredNotifications.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 通知详情对话框 -->
    <el-dialog 
      title="通知详情" 
      v-model="detailDialogVisible" 
      width="70%"
      class="custom-dialog"
    >
      <div class="notification-detail dialog-content">
        <h2 class="detail-title text-center mb-20">{{ currentNotification.title }}</h2>
        <div class="detail-meta mb-15">
          <el-tag :type="getNotificationTypeTag(currentNotification.type)">
            {{ getNotificationTypeText(currentNotification.type) }}
          </el-tag>
          <span class="meta-item">发布时间：{{ formatDateTime(currentNotification.publishTime) }}</span>
          <span class="meta-item">发布人：{{ currentNotification.publisher }}</span>
          <span class="meta-item">过期时间：{{ formatDateTime(currentNotification.expireTime) }}</span>
          <span class="meta-item">浏览量：{{ currentNotification.viewCount || 0 }}</span>
        </div>
        
        <el-divider></el-divider>
        
        <div class="detail-content p-20 bg-white rounded-lg mb-20">
          <div v-html="currentNotification.content"></div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="detail-recipients p-20 bg-white rounded-lg mb-20">
          <h3>接收范围</h3>
          <div class="recipients-info">
            <p><strong>接收对象：</strong>{{ getNotificationTargetText(currentNotification.target) }}</p>
            <p><strong>具体范围：</strong>
              <span class="recipient-list" @click="viewRecipients(currentNotification)">
                {{ getRecipientPreview(currentNotification.recipients) }}
              </span>
            </p>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="detail-actions p-20 bg-white rounded-lg" v-if="currentNotification.status === 'PUBLISHED'">
          <h3>通知状态</h3>
          <div class="status-info">
            <p><strong>状态：</strong>
              <el-tag :type="getNotificationStatusTag(currentNotification.status)">
                {{ getNotificationStatusText(currentNotification.status) }}
              </el-tag>
            </p>
            <p><strong>发布日期：</strong>{{ formatDateTime(currentNotification.publishTime) }}</p>
            <p><strong>过期日期：</strong>{{ formatDateTime(currentNotification.expireTime) }}</p>
            <el-button type="primary" @click="resendNotification">
              <i class="el-icon-refresh-right"></i> 重新发送
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 发布/编辑通知对话框 -->
    <el-dialog 
      :title="editMode ? (notificationForm.status === 'PUBLISHED' ? '发布通知' : '编辑草稿') : '发布新通知'" 
      v-model="editDialogVisible" 
      width="80%"
      :fullscreen="true"
      class="custom-dialog"
    >
      <div class="notification-form p-20">
        <el-form 
          ref="notificationFormRef" 
          :model="notificationForm" 
          :rules="notificationRules" 
          label-width="100px"
        >
          <el-form-item label="通知类型" prop="type" class="mb-15">
            <el-select v-model="notificationForm.type" placeholder="请选择通知类型" style="width: 200px;">
              <el-option label="系统通知" value="SYSTEM" />
              <el-option label="教学通知" value="ACADEMIC" />
              <el-option label="活动通知" value="ACTIVITY" />
              <el-option label="公告" value="ANNOUNCEMENT" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="通知标题" prop="title" class="mb-15">
            <el-input 
              v-model="notificationForm.title" 
              placeholder="请输入通知标题" 
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="接收对象" prop="target" class="mb-15">
            <el-select v-model="notificationForm.target" placeholder="请选择接收对象" style="width: 200px;">
              <el-option label="全体学生" value="STUDENTS" />
              <el-option label="全体教师" value="TEACHERS" />
              <el-option label="特定系部" value="DEPARTMENTS" />
              <el-option label="特定班级" value="CLASSES" />
              <el-option label="特定用户" value="SPECIFIC_USERS" />
            </el-select>
          </el-form-item>
          
          <!-- 特定系部选择 -->
          <el-form-item 
            v-if="notificationForm.target === 'DEPARTMENTS'" 
            label="选择系部" 
            prop="selectedDepartments"
            class="mb-15"
          >
            <el-select 
              v-model="notificationForm.selectedDepartments" 
              multiple 
              placeholder="请选择系部" 
              style="width: 100%;"
            >
              <el-option 
                v-for="dept in departments" 
                :key="dept.id" 
                :label="dept.name" 
                :value="dept.id" 
              />
            </el-select>
          </el-form-item>
          
          <!-- 特定班级选择 -->
          <el-form-item 
            v-if="notificationForm.target === 'CLASSES'" 
            label="选择班级" 
            prop="selectedClasses"
            class="mb-15"
          >
            <el-select 
              v-model="notificationForm.selectedClasses" 
              multiple 
              placeholder="请选择班级" 
              style="width: 100%;"
            >
              <el-option 
                v-for="cls in classes" 
                :key="cls.id" 
                :label="cls.name" 
                :value="cls.id" 
              />
            </el-select>
          </el-form-item>
          
          <!-- 特定用户选择 -->
          <el-form-item 
            v-if="notificationForm.target === 'SPECIFIC_USERS'" 
            label="选择用户" 
            prop="selectedUsers"
            class="mb-15"
          >
            <el-select 
              v-model="notificationForm.selectedUsers" 
              multiple 
              filterable 
              placeholder="请选择用户" 
              style="width: 100%;"
            >
              <el-option 
                v-for="user in users" 
                :key="user.id" 
                :label="user.name + '(' + user.username + ')'" 
                :value="user.id" 
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="发布时间" prop="publishTime" class="mb-15">
            <el-date-picker
              v-model="notificationForm.publishTime"
              type="datetime"
              placeholder="请选择发布时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              default-time="00:00:00"
              style="width: 100%;"
            />
          </el-form-item>
          
          <el-form-item label="过期时间" prop="expireTime" class="mb-15">
            <el-date-picker
              v-model="notificationForm.expireTime"
              type="datetime"
              placeholder="请选择过期时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              default-time="23:59:59"
              style="width: 100%;"
            />
          </el-form-item>
          
          <el-form-item label="通知内容" prop="content" class="mb-15">
            <!-- 这里应该使用富文本编辑器，现在用textarea替代 -->
            <el-input 
              v-model="notificationForm.content" 
              type="textarea" 
              :rows="15" 
              placeholder="请输入通知内容"
            />
            <div class="editor-tips">
              <span class="tips">提示：实际项目中这里应该使用富文本编辑器进行内容编辑</span>
            </div>
          </el-form-item>
          
          <el-form-item label="附件上传" class="mb-15">
            <el-upload
              class="upload-demo"
              drag
              action=""
              :auto-upload="false"
              :on-change="handleFileChange"
              :multiple="true"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              <div class="el-upload__tip" slot="tip">只能上传jpg/png/pdf/xlsx/docx文件，且不超过20MB</div>
            </el-upload>
            <el-tag 
              v-for="(file, index) in notificationForm.attachments" 
              :key="index" 
              closable 
              @close="removeAttachment(index)"
              class="attachment-tag"
            >
              <i class="el-icon-document"></i> {{ file.name }}
            </el-tag>
          </el-form-item>
        </el-form>
      </div>
      
      <div slot="footer" class="dialog-footer text-right">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="confirmEdit">
          {{ editMode ? (notificationForm.status === 'PUBLISHED' ? '更新并发布' : '更新草稿') : (notificationForm.status === 'PUBLISHED' ? '发布' : '保存为草稿') }}
        </el-button>
      </div>
    </el-dialog>
    
    <!-- 批量删除确认对话框 -->
    <el-dialog 
      title="确认删除" 
      v-model="deleteDialogVisible" 
      width="40%"
      class="custom-dialog"
    >
      <div class="delete-dialog p-20">
        <p>您确定要删除选中的 <strong>{{ selectedNotifications.length }}</strong> 条通知吗？</p>
        <p class="delete-warning">此操作不可撤销，删除后数据将无法恢复！</p>
      </div>
      
      <div slot="footer" class="dialog-footer text-right">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
      </div>
    </el-dialog>
    
    <!-- 查看完整内容对话框 -->
    <el-dialog 
      title="通知内容" 
      v-model="fullContentVisible" 
      width="80%"
      class="custom-dialog"
    >
      <div class="full-content p-20 bg-white rounded-lg" v-html="fullContent"></div>
    </el-dialog>
    
    <!-- 接收人范围对话框 -->
    <el-dialog 
      title="接收人范围" 
      v-model="recipientsDialogVisible" 
      width="80%"
      class="custom-dialog"
    >
      <div class="recipients-dialog p-20">
        <h3>通知标题：{{ recipientsNotification.title }}</h3>
        <div class="recipients-info">
          <p><strong>接收对象：</strong>{{ getNotificationTargetText(recipientsNotification.target) }}</p>
          <p><strong>具体范围：</strong></p>
          <div class="recipients-list">
            <template v-if="recipientsNotification.target === 'DEPARTMENTS'">
              <el-tag 
                v-for="deptId in recipientsNotification.recipients.departments" 
                :key="deptId" 
                class="dept-tag"
              >
                {{ getDepartmentName(deptId) }}
              </el-tag>
            </template>
            <template v-else-if="recipientsNotification.target === 'CLASSES'">
              <el-tag 
                v-for="classId in recipientsNotification.recipients.classes" 
                :key="classId" 
                class="class-tag"
              >
                {{ getClassName(classId) }}
              </el-tag>
            </template>
            <template v-else-if="recipientsNotification.target === 'SPECIFIC_USERS'">
              <div class="user-list">
                <el-table :data="getUserDetails(recipientsNotification.recipients.users)" style="width: 100%;">
                  <el-table-column prop="name" label="姓名" width="120" />
                  <el-table-column prop="username" label="账号" width="120" />
                  <el-table-column prop="role" label="角色" width="100" />
                  <el-table-column prop="department" label="所属院系" min-width="150" />
                </el-table>
              </div>
            </template>
            <template v-else>
              <span>{{ getNotificationTargetText(recipientsNotification.target) }}</span>
            </template>
          </div>
        </div>
      </div>
    </el-dialog>
</template>

<script>
export default {
  name: 'NotificationManagement',
  data() {
    return {
      // 加载状态
      loading: false,
      
      // 筛选条件
      filterOptions: {
        type: '',
        status: '',
        target: '',
        keyword: ''
      },
      dateRange: [],
      
      // 数据列表
      notifications: [],
      filteredNotifications: [],
      
      // 分页相关
      currentPage: 1,
      pageSize: 20,
      
      // 选中的记录
      selectedNotifications: [],
      
      // 对话框相关
      detailDialogVisible: false,
      editDialogVisible: false,
      deleteDialogVisible: false,
      fullContentVisible: false,
      recipientsDialogVisible: false,
      editMode: false,
      
      // 当前操作的通知
      currentNotification: {},
      recipientsNotification: {},
      
      // 完整内容
      fullContent: '',
      
      // 通知表单
      notificationForm: {
        id: null,
        type: 'SYSTEM',
        title: '',
        content: '',
        target: 'STUDENTS',
        selectedDepartments: [],
        selectedClasses: [],
        selectedUsers: [],
        publishTime: '',
        expireTime: '',
        status: 'PUBLISHED',
        attachments: []
      },
      
      // 表单验证规则
      notificationRules: {
        type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
        title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
        target: [{ required: true, message: '请选择接收对象', trigger: 'change' }],
        publishTime: [{ required: true, message: '请选择发布时间', trigger: 'change' }],
        expireTime: [{ required: true, message: '请选择过期时间', trigger: 'change' }],
        selectedDepartments: [
          { 
            validator: (rule, value, callback) => {
              if (this.notificationForm.target === 'DEPARTMENTS' && (!value || value.length === 0)) {
                callback(new Error('请至少选择一个系部'))
              } else {
                callback()
              }
            }, 
            trigger: 'change' 
          }
        ],
        selectedClasses: [
          { 
            validator: (rule, value, callback) => {
              if (this.notificationForm.target === 'CLASSES' && (!value || value.length === 0)) {
                callback(new Error('请至少选择一个班级'))
              } else {
                callback()
              }
            }, 
            trigger: 'change' 
          }
        ],
        selectedUsers: [
          { 
            validator: (rule, value, callback) => {
              if (this.notificationForm.target === 'SPECIFIC_USERS' && (!value || value.length === 0)) {
                callback(new Error('请至少选择一个用户'))
              } else {
                callback()
              }
            }, 
            trigger: 'change' 
          }
        ]
      },
      
      // 辅助数据
      departments: [],
      classes: [],
      users: []
    }
  },
  computed: {
    // 分页后的数据
    paginatedNotifications() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredNotifications.slice(start, end)
    },
    
    // 是否可以批量发布
    canBatchPublish() {
      return this.selectedNotifications.every(n => n.status === 'DRAFT')
    }
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    // 解析接收人数据
    parseRecipients(target, targetIds) {
      if (!targetIds) {
        return {};
      }
      
      const ids = Array.isArray(targetIds) ? targetIds : targetIds.split(',').map(id => parseInt(id.trim()));
      
      switch (target) {
        case 'DEPARTMENTS':
          return { departments: ids };
        case 'CLASSES':
          return { classes: ids };
        case 'SPECIFIC_USERS':
          return { users: ids };
        default:
          return {};
      }
    },
    
    // 加载初始数据
    async loadInitialData() {
      this.loading = true
      try {
        // 同时加载所有必要数据
        await Promise.all([
          this.loadDepartments(),
          this.loadClasses(),
          this.loadUsers(),
          this.loadNotifications()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载系部数据
    async loadDepartments() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/departments')
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
    
    // 加载班级数据
    async loadClasses() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/classes')
      // this.classes = response.data
      
      // 使用模拟数据
      this.classes = [
        { id: 1, name: '计算机科学与技术1班', departmentId: 1, grade: '2020' },
        { id: 2, name: '计算机科学与技术2班', departmentId: 1, grade: '2020' },
        { id: 3, name: '软件工程1班', departmentId: 1, grade: '2020' },
        { id: 4, name: '软件工程2班', departmentId: 1, grade: '2020' },
        { id: 5, name: '计算机科学与技术1班', departmentId: 1, grade: '2021' },
        { id: 6, name: '软件工程1班', departmentId: 1, grade: '2021' },
        { id: 7, name: '电子信息工程1班', departmentId: 2, grade: '2020' },
        { id: 8, name: '机械设计制造及其自动化1班', departmentId: 3, grade: '2020' },
        { id: 9, name: '市场营销1班', departmentId: 4, grade: '2020' },
        { id: 10, name: '汉语言文学1班', departmentId: 5, grade: '2020' }
      ]
    },
    
    // 加载用户数据
    async loadUsers() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/users')
      // this.users = response.data
      
      // 使用模拟数据
      this.users = [
        { id: 1, name: '张三', username: 'zhangsan', role: 'STUDENT', departmentId: 1 },
        { id: 2, name: '李四', username: 'lisi', role: 'STUDENT', departmentId: 1 },
        { id: 3, name: '王五', username: 'wangwu', role: 'STUDENT', departmentId: 1 },
        { id: 4, name: '赵六', username: 'zhaoliu', role: 'STUDENT', departmentId: 2 },
        { id: 5, name: '钱七', username: 'qianqi', role: 'TEACHER', departmentId: 1 },
        { id: 6, name: '孙八', username: 'sunba', role: 'TEACHER', departmentId: 2 },
        { id: 7, name: '周九', username: 'zhoujiu', role: 'TEACHER', departmentId: 3 },
        { id: 8, name: '吴十', username: 'wushi', role: 'ADMIN', departmentId: 1 }
      ]
    },
    
    // 加载通知数据
    async loadNotifications() {
      try {
        this.loading = true;
        const response = await this.$axios.get('/api/admin/notifications');
        
        // 处理返回的数据，使其符合前端格式
        // 如果返回的是分页数据，则使用content字段，否则直接使用data
        const notificationsData = response.data.data;
        let notificationList = [];
        
        if (notificationsData && notificationsData.content) {
          // 分页数据结构: { content: [...], totalElements: ..., totalPages: ... }
          notificationList = notificationsData.content;
        } else if (Array.isArray(notificationsData)) {
          // 直接数组结构
          notificationList = notificationsData;
        }
        
        this.notifications = notificationList.map(notification => {
          return {
            id: notification.id,
            title: notification.title,
            content: notification.content,
            type: notification.type,
            target: notification.target,
            recipients: this.parseRecipients(notification.target, notification.targetIds),
            status: notification.status,
            publishTime: notification.publishTime,
            expireTime: notification.expireTime,
            publisher: notification.publisherName,
            viewCount: notification.viewCount,
            createdAt: notification.createdAt,
            updatedAt: notification.updatedAt
          };
        });
        
        this.filteredNotifications = [...this.notifications];
      } catch (error) {
        console.error('加载通知失败:', error);
        this.$message.error('加载通知数据失败，请检查后端服务');
      } finally {
        this.loading = false;
      }
    },
    
    // 刷新数据
    refreshData() {
      this.loadNotifications()
      this.selectedNotifications = []
    },
    
    // 搜索通知
    searchNotifications() {
      this.currentPage = 1
      
      this.filteredNotifications = this.notifications.filter(notification => {
        // 按类型筛选
        if (this.filterOptions.type && notification.type !== this.filterOptions.type) {
          return false
        }
        
        // 按状态筛选
        if (this.filterOptions.status && notification.status !== this.filterOptions.status) {
          return false
        }
        
        // 按目标筛选
        if (this.filterOptions.target && notification.target !== this.filterOptions.target) {
          return false
        }
        
        // 按关键词筛选
        if (this.filterOptions.keyword) {
          const keyword = this.filterOptions.keyword.toLowerCase()
          const title = notification.title.toLowerCase()
          const content = this.stripHtmlTags(notification.content).toLowerCase()
          
          if (!title.includes(keyword) && !content.includes(keyword)) {
            return false
          }
        }
        
        // 按日期范围筛选
        if (this.dateRange && this.dateRange.length === 2) {
          const [startDate, endDate] = this.dateRange
          const publishTime = new Date(notification.publishTime)
          const searchStart = new Date(startDate)
          const searchEnd = new Date(endDate + ' 23:59:59')
          
          if (publishTime < searchStart || publishTime > searchEnd) {
            return false
          }
        }
        
        return true
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.filterOptions = {
        type: '',
        status: '',
        target: '',
        keyword: ''
      }
      this.dateRange = []
      this.filteredNotifications = [...this.notifications]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedNotifications = selection
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.currentPage = 1
    },
    
    handleCurrentChange(newCurrent) {
      this.currentPage = newCurrent
    },
    
    // 查看通知详情
    viewNotificationDetail(notification) {
      this.currentNotification = { ...notification }
      this.detailDialogVisible = true
    },
    
    // 查看完整内容
    viewFullContent(notification) {
      this.fullContent = notification.content
      this.fullContentVisible = true
    },
    
    // 查看接收人范围
    viewRecipients(notification) {
      this.recipientsNotification = { ...notification }
      this.recipientsDialogVisible = true
    },
    
    // 创建/编辑通知
    createNotification(status, notification = null) {
      // 重置表单
      this.resetForm()
      
      // 设置通知状态
      this.notificationForm.status = status
      
      // 如果是编辑模式，填充表单
      if (notification) {
        this.editMode = true
        this.notificationForm.id = notification.id
        this.notificationForm.type = notification.type
        this.notificationForm.title = notification.title
        this.notificationForm.content = notification.content
        this.notificationForm.target = notification.target
        this.notificationForm.publishTime = this.formatDateTimeForForm(notification.publishTime)
        this.notificationForm.expireTime = this.formatDateTimeForForm(notification.expireTime)
        
        // 根据目标类型设置选中的接收对象
        if (notification.target === 'DEPARTMENTS' && notification.recipients && notification.recipients.departments) {
          this.notificationForm.selectedDepartments = [...notification.recipients.departments]
        }
        if (notification.target === 'CLASSES' && notification.recipients && notification.recipients.classes) {
          this.notificationForm.selectedClasses = [...notification.recipients.classes]
        }
        if (notification.target === 'SPECIFIC_USERS' && notification.recipients && notification.recipients.users) {
          this.notificationForm.selectedUsers = [...notification.recipients.users]
        }
        
        // 设置附件
        if (notification.attachments) {
          this.notificationForm.attachments = [...notification.attachments]
        }
      } else {
        this.editMode = false
        // 设置默认时间
        const now = new Date()
        this.notificationForm.publishTime = this.formatDateTimeForForm(now.toISOString())
        
        // 默认过期时间为发布后7天
        const expireTime = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
        this.notificationForm.expireTime = this.formatDateTimeForForm(expireTime.toISOString())
      }
      
      this.editDialogVisible = true
    },
    
    // 重置表单
    resetForm() {
      if (this.$refs.notificationFormRef) {
        this.$refs.notificationFormRef.resetFields()
      }
      
      this.notificationForm = {
        id: null,
        type: 'SYSTEM',
        title: '',
        content: '',
        target: 'STUDENTS',
        selectedDepartments: [],
        selectedClasses: [],
        selectedUsers: [],
        publishTime: '',
        expireTime: '',
        status: 'PUBLISHED',
        attachments: []
      }
    },
    
    // 取消编辑
    cancelEdit() {
      this.editDialogVisible = false
      this.resetForm()
    },
    
    // 确认编辑
    async confirmEdit() {
      this.$refs.notificationFormRef.validate(async (valid) => {
        if (valid) {
          try {
            // 构建接收人数据
            let targetIds = []
            if (this.notificationForm.target === 'DEPARTMENTS') {
              targetIds = this.notificationForm.selectedDepartments
            } else if (this.notificationForm.target === 'CLASSES') {
              targetIds = this.notificationForm.selectedClasses
            } else if (this.notificationForm.target === 'SPECIFIC_USERS') {
              targetIds = this.notificationForm.selectedUsers
            }
            
            // 构建通知数据
            const notificationData = {
              type: this.notificationForm.type,
              title: this.notificationForm.title,
              content: this.notificationForm.content,
              target: this.notificationForm.target,
              targetIds: targetIds,
              publishTime: this.formatDateForBackend(this.notificationForm.publishTime),
              expireTime: this.formatDateForBackend(this.notificationForm.expireTime),
              status: this.notificationForm.status
            }
            
            let response
            if (this.editMode) {
              // 更新现有通知
              response = await this.$axios.put(`/api/admin/notifications/${this.notificationForm.id}`, notificationData)
            } else {
              // 创建新通知
              response = await this.$axios.post('/api/admin/notifications', notificationData)
            }
            
            this.editDialogVisible = false
            this.refreshData()
            this.$message.success(
              notificationData.status === 'PUBLISHED' ? '通知发布成功' : '草稿保存成功'
            )
          } catch (error) {
            console.error('保存通知失败:', error)
            this.$message.error('操作失败，请稍后重试')
          }
        }
      })
    },
    
    // 删除通知
    deleteNotification(id) {
      this.$confirm('确定要删除这条通知吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/api/admin/notifications/${id}`)
          
          this.refreshData()
          this.$message.success('通知删除成功')
        } catch (error) {
          console.error('删除通知失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消删除
      })
    },
    
    // 批量删除确认
    batchDelete() {
      if (this.selectedNotifications.length === 0) {
        this.$message.warning('请选择要删除的通知')
        return
      }
      
      this.deleteDialogVisible = true
    },
    
    // 确认批量删除
    async confirmDelete() {
      try {
        const ids = this.selectedNotifications.map(n => n.id)
        await this.$axios.post('/api/admin/notifications/batch-delete', { ids })
        
        this.deleteDialogVisible = false
        this.selectedNotifications = []
        this.refreshData()
        this.$message.success('通知批量删除成功')
      } catch (error) {
        console.error('批量删除失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    
    // 批量发布
    async batchPublish() {
      if (this.selectedNotifications.length === 0) {
        this.$message.warning('请选择要发布的通知')
        return
      }
      
      if (!this.canBatchPublish) {
        this.$message.warning('只能批量发布草稿状态的通知')
        return
      }
      
      this.$confirm(
        `确定要发布选中的 ${this.selectedNotifications.length} 条通知吗？`,
        '确认发布',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'primary'
        }
      ).then(async () => {
        try {
          const ids = this.selectedNotifications.map(n => n.id)
          await this.$axios.post('/api/admin/notifications/batch-publish', { ids })
          
          this.selectedNotifications = []
          this.refreshData()
          this.$message.success('通知批量发布成功')
        } catch (error) {
          console.error('批量发布失败:', error)
          this.$message.error('操作失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消发布
      })
    },
    
    // 重新发送通知
    async resendNotification() {
      try {
        await this.$axios.post(`/api/admin/notifications/${this.currentNotification.id}/resend`)
        
        this.$message.success('通知已重新发送')
      } catch (error) {
        console.error('重新发送失败:', error)
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
      // window.location.href = `/admin/notifications/export?${new URLSearchParams(params).toString()}`
      
      this.$message.info('正在导出数据...')
      
      // 模拟导出过程
      setTimeout(() => {
        this.$message.success('数据导出成功')
      }, 2000)
    },
    
    // 处理文件上传
    handleFileChange(file, fileList) {
      this.notificationForm.attachments = fileList.map(file => ({
        name: file.name,
        url: '#' // 实际项目中应该上传到服务器并获取URL
      }))
    },
    
    // 移除附件
    removeAttachment(index) {
      this.notificationForm.attachments.splice(index, 1)
    },
    
    // 辅助方法 - 获取系部名称
    getDepartmentName(departmentId) {
      const department = this.departments.find(d => d.id === departmentId)
      return department ? department.name : `系部ID: ${departmentId}`
    },
    
    // 辅助方法 - 获取班级名称
    getClassName(classId) {
      const cls = this.classes.find(c => c.id === classId)
      return cls ? cls.name : `班级ID: ${classId}`
    },
    
    // 辅助方法 - 获取用户详情
    getUserDetails(userIds) {
      return userIds
        .map(id => {
          const user = this.users.find(u => u.id === id)
          if (user) {
            return {
              ...user,
              department: this.getDepartmentName(user.departmentId)
            }
          }
          return { id, name: `用户ID: ${id}`, username: 'unknown', role: 'unknown', department: 'unknown' }
        })
    },
    
    // 辅助方法 - 格式化日期时间
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
    
    // 辅助方法 - 格式化表单用的日期时间
    formatDateTimeForForm(dateString) {
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
    
    // 辅助方法 - 格式化日期为后端可接受的格式
    formatDateForBackend(dateString) {
      if (!dateString) return null;
      
      // 首先尝试直接解析日期字符串
      let date = new Date(dateString);
      
      // 检查日期是否有效
      if (isNaN(date.getTime())) {
        // 如果日期无效，尝试解析可能包含星期信息的字符串
        // 常见格式如 'Wed Jan 15 2026 01:15:17 GMT+0800 (中国标准时间)'
        if (typeof dateString === 'string') {
          // 尝试提取标准日期时间部分
          const dateMatch = dateString.match(/\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}/);
          if (dateMatch) {
            date = new Date(dateMatch[0]);
          } else {
            // 如果还不能解析，尝试其他常见格式
            const isoMatch = dateString.match(/\d{4}-\d{2}-\dT\d{2}:\d{2}:\d{2}.*/);
            if (isoMatch) {
              date = new Date(isoMatch[0]);
            }
          }
        }
      }
      
      // 再次检查日期是否有效
      if (isNaN(date.getTime())) {
        console.error('无法解析日期:', dateString);
        return null;
      }
      
      // 返回ISO格式的日期字符串，这是后端可以处理的标准格式
      return date.toISOString();
    },
    
    // 辅助方法 - 获取通知类型标签样式
    getNotificationTypeTag(type) {
      const tagMap = {
        'SYSTEM': 'info',
        'ACADEMIC': 'primary',
        'ACTIVITY': 'success',
        'ANNOUNCEMENT': 'warning'
      }
      return tagMap[type] || 'info'
    },
    
    // 辅助方法 - 获取通知类型文本
    getNotificationTypeText(type) {
      const textMap = {
        'SYSTEM': '系统通知',
        'ACADEMIC': '教学通知',
        'ACTIVITY': '活动通知',
        'ANNOUNCEMENT': '公告'
      }
      return textMap[type] || '未知'
    },
    
    // 辅助方法 - 获取通知目标标签样式
    getNotificationTargetTag(target) {
      const tagMap = {
        'STUDENTS': 'primary',
        'TEACHERS': 'success',
        'DEPARTMENTS': 'warning',
        'CLASSES': 'info',
        'SPECIFIC_USERS': 'danger'
      }
      return tagMap[target] || 'info'
    },
    
    // 辅助方法 - 获取通知目标文本
    getNotificationTargetText(target) {
      const textMap = {
        'STUDENTS': '全体学生',
        'TEACHERS': '全体教师',
        'DEPARTMENTS': '特定系部',
        'CLASSES': '特定班级',
        'SPECIFIC_USERS': '特定用户'
      }
      return textMap[target] || '未知'
    },
    
    // 辅助方法 - 获取通知状态标签样式
    getNotificationStatusTag(status) {
      const tagMap = {
        'PUBLISHED': 'success',
        'DRAFT': 'info',
        'EXPIRED': 'warning'
      }
      return tagMap[status] || 'info'
    },
    
    // 辅助方法 - 获取通知状态文本
    getNotificationStatusText(status) {
      const textMap = {
        'PUBLISHED': '已发布',
        'DRAFT': '草稿',
        'EXPIRED': '已过期'
      }
      return textMap[status] || '未知'
    },
    
    // 辅助方法 - 去除HTML标签
    stripHtmlTags(html) {
      if (!html) return ''
      return html.replace(/<[^>]*>/g, '')
    },
    
    // 辅助方法 - 获取内容预览
    getContentPreview(content) {
      if (!content) return ''
      const plainText = this.stripHtmlTags(content)
      return plainText.length > 50 ? plainText.substring(0, 50) + '...' : plainText
    },
    
    // 辅助方法 - 获取接收人预览
    getRecipientPreview(recipients) {
      if (!recipients) return ''
      
      if (recipients.departments) {
        return `${recipients.departments.length} 个系部`
      } else if (recipients.classes) {
        return `${recipients.classes.length} 个班级`
      } else if (recipients.users) {
        return `${recipients.users.length} 个用户`
      }
      
      return ''
    },
    
    // 搜索请假（这里应该是搜索通知，但为了代码一致性保留方法名）
    searchLeaves() {
      // 直接调用搜索通知方法
      this.searchNotifications()
    },
    
    // 生成模拟通知数据
    generateMockNotifications(count = 40) {
      const notifications = []
      const types = ['SYSTEM', 'ACADEMIC', 'ACTIVITY', 'ANNOUNCEMENT']
      const statuses = ['PUBLISHED', 'DRAFT', 'EXPIRED']
      const targets = ['STUDENTS', 'TEACHERS', 'DEPARTMENTS', 'CLASSES', 'SPECIFIC_USERS']
      const titles = [
        '关于2024年春季学期选课的通知',
        '期末考试安排公告',
        '校园文化节活动报名通知',
        '系统升级维护通知',
        '奖学金申请通知',
        '学生社团招新公告',
        '校园招聘会安排',
        '关于加强校园安全管理的通知',
        '研究生招生考试报名通知',
        '校园运动会通知',
        '图书馆开放时间调整通知',
        '助学金申请指南',
        '校园网络使用规范通知',
        '学生干部选拔通知',
        '校园讲座预告'
      ]
      
      const contentTemplates = [
        '<p>各位同学：</p><p>根据教学安排，现将相关事项通知如下：</p><ol><li>事项一：内容详情...</li><li>事项二：内容详情...</li><li>事项三：内容详情...</li></ol><p>请相关同学准时参加，遵守纪律。</p>',
        '<p>为丰富校园文化生活，特举办本次活动，欢迎广大师生积极参与。</p><p>活动详情：</p><ul><li>时间：待定</li><li>地点：待定</li><li>参与方式：线上报名</li></ul><p>联系人：XXX</p><p>联系电话：XXXXXXXXXXX</p>',
        '<p>系统将于近期进行升级维护，届时系统可能暂时无法访问，请提前做好准备。</p><p>维护时间：XXXX年XX月XX日 XX:XX - XX:XX</p><p>给您带来的不便，敬请谅解。</p>'
      ]
      
      const now = new Date()
      const semesterStart = new Date(now.getFullYear(), 8, 1) // 9月1日
      const semesterEnd = new Date(now.getFullYear(), 11, 31) // 12月31日
      
      for (let i = 1; i <= count; i++) {
        const type = types[Math.floor(Math.random() * types.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const target = targets[Math.floor(Math.random() * targets.length)]
        const titleIndex = Math.floor(Math.random() * titles.length)
        const contentIndex = Math.floor(Math.random() * contentTemplates.length)
        
        // 随机生成发布时间
        const randomPublish = new Date(semesterStart.getTime() + Math.random() * (semesterEnd.getTime() - semesterStart.getTime()))
        
        // 随机生成过期时间（发布后1-30天）
        const randomExpire = new Date(randomPublish.getTime() + (Math.floor(Math.random() * 30) + 1) * 24 * 60 * 60 * 1000)
        
        // 构建接收人数据
        let recipients = {}
        if (target === 'DEPARTMENTS') {
          // 随机选择1-3个系部
          const randomDeptCount = Math.floor(Math.random() * 3) + 1
          const shuffledDepts = [...this.departments].sort(() => 0.5 - Math.random())
          recipients = { departments: shuffledDepts.slice(0, randomDeptCount).map(d => d.id) }
        } else if (target === 'CLASSES') {
          // 随机选择1-3个班级
          const randomClassCount = Math.floor(Math.random() * 3) + 1
          const shuffledClasses = [...this.classes].sort(() => 0.5 - Math.random())
          recipients = { classes: shuffledClasses.slice(0, randomClassCount).map(c => c.id) }
        } else if (target === 'SPECIFIC_USERS') {
          // 随机选择1-5个用户
          const randomUserCount = Math.floor(Math.random() * 5) + 1
          const shuffledUsers = [...this.users].sort(() => 0.5 - Math.random())
          recipients = { users: shuffledUsers.slice(0, randomUserCount).map(u => u.id) }
        }
        
        notifications.push({
          id: Date.now() + i,
          type: type,
          title: titles[titleIndex] + (i > titles.length ? ` (${i % titles.length + 1})` : ''),
          content: contentTemplates[contentIndex] + `<p>发布日期：${this.formatDateTime(randomPublish.toISOString())}</p>`,
          target: target,
          recipients: recipients,
          status: status,
          publishTime: randomPublish.toISOString(),
          expireTime: randomExpire.toISOString(),
          publisher: '管理员',
          viewCount: Math.floor(Math.random() * 500),
          attachments: Math.random() > 0.7 ? [
            { name: '附件1.pdf', url: '#' },
            { name: '附件2.docx', url: '#' }
          ] : []
        })
      }
      
      // 按发布时间倒序排序
      notifications.sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime))
      
      return notifications
    }
  }
}
</script>

<style scoped>
.notification-management-container {
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

.hover-card {
  border-radius: 8px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.action-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}

.table-section {
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 表格相关样式 */
.title-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-content {
  font-weight: 500;
}

.title-content.unread {
  font-weight: bold;
  color: #409eff;
}

.title-meta {
  display: flex;
  gap: 8px;
}

.content-preview {
  color: #409eff;
  cursor: pointer;
}

.content-preview:hover {
  text-decoration: underline;
}

.recipient-info {
  color: #409eff;
  cursor: pointer;
}

.recipient-info:hover {
  text-decoration: underline;
}

/* 详情对话框样式 */
.notification-detail {
  padding: 10px 0;
}

.detail-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 24px;
  color: #303133;
  text-align: center;
}

.detail-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.meta-item {
  color: #606266;
}

.detail-content {
  margin-bottom: 30px;
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
}

.detail-content p {
  margin-bottom: 15px;
}

.detail-content ul, .detail-content ol {
  margin-bottom: 15px;
  padding-left: 30px;
}

.detail-content li {
  margin-bottom: 8px;
}

.detail-recipients h3, .detail-actions h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.recipients-info {
  line-height: 1.8;
}

.recipient-list {
  color: #409eff;
  cursor: pointer;
}

.recipient-list:hover {
  text-decoration: underline;
}

/* 编辑表单样式 */
.notification-form {
  padding: 10px 0;
}

.editor-tips {
  margin-top: 10px;
  text-align: right;
}

.tips {
  color: #909399;
  font-size: 12px;
}

.attachment-tag {
  margin: 5px;
}

/* 删除对话框样式 */
.delete-dialog {
  padding: 10px 0;
}

.delete-warning {
  color: #f56c6c;
  font-weight: bold;
}

/* 完整内容对话框样式 */
.full-content {
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
}

.full-content p {
  margin-bottom: 15px;
}

.full-content ul, .full-content ol {
  margin-bottom: 15px;
  padding-left: 30px;
}

.full-content li {
  margin-bottom: 8px;
}

/* 接收人对话框样式 */
.recipients-dialog {
  padding: 10px 0;
}

.recipients-dialog h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
}

.recipients-list {
  margin-top: 10px;
}

.dept-tag, .class-tag {
  margin: 5px;
}

.user-list {
  margin-top: 10px;
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
  
  .action-section .el-button {
    width: 100%;
  }
  
  .detail-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .recipients-dialog .el-col {
    span: 24;
  }
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

/* 开关样式 */
.table-section :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.table-section :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
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
</style>