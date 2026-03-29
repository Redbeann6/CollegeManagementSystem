<template>
  <el-card class="management-header">
    <template #header>
      <div class="card-header">
        <h3>系统设置</h3>
      </div>
    </template>
    <el-tabs v-model="activeTab">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <div class="tab-content">
            <el-form 
              ref="basicForm" 
              :model="basicForm" 
              :rules="basicRules" 
              label-width="120px"
              class="settings-form"
            >
              <div class="form-container">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="系统名称" prop="systemName">
                      <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="系统版本" prop="systemVersion">
                      <el-input v-model="basicForm.systemVersion" placeholder="请输入系统版本" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="学校名称" prop="schoolName">
                      <el-input v-model="basicForm.schoolName" placeholder="请输入学校名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="联系邮箱" prop="contactEmail">
                      <el-input v-model="basicForm.contactEmail" placeholder="请输入联系邮箱" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="联系电话" prop="contactPhone">
                      <el-input v-model="basicForm.contactPhone" placeholder="请输入联系电话" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="学校地址" prop="schoolAddress">
                      <el-input v-model="basicForm.schoolAddress" placeholder="请输入学校地址" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="版权信息" prop="copyright">
                      <el-input 
                        v-model="basicForm.copyright" 
                        type="textarea" 
                        :rows="3" 
                        placeholder="请输入版权信息" 
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="系统描述" prop="systemDescription">
                      <el-input 
                        v-model="basicForm.systemDescription" 
                        type="textarea" 
                        :rows="4" 
                        placeholder="请输入系统描述信息" 
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </el-form>
            
            <div class="form-actions">
              <el-button type="primary" @click="saveBasicSettings" size="default">
                <i class="el-icon-check"></i>
                保存设置
              </el-button>
              <el-button @click="resetBasicSettings" size="default">
                <i class="el-icon-refresh"></i>
                重置
              </el-button>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 学期设置 -->
        <el-tab-pane label="学期设置" name="semester">
          <div class="tab-content">
            <div class="action-buttons">
              <el-button type="primary" @click="addSemester" size="default">
                <i class="el-icon-plus"></i>
                添加学期
              </el-button>
              <el-button @click="refreshSemesterList" size="default">
                <i class="el-icon-refresh"></i>
                刷新列表
              </el-button>
            </div>
            
            <div class="semester-table">
              <el-table 
                v-loading="loading.semester" 
                :data="semesters" 
                style="width: 100%"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="学期名称" width="180" />
                <el-table-column prop="startDate" label="开始日期" width="140">
                  <template #default="scope">
                    {{ formatDate(scope.row.startDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="endDate" label="结束日期" width="140">
                  <template #default="scope">
                    {{ formatDate(scope.row.endDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">
                      {{ scope.row.status === 'ACTIVE' ? '当前学期' : '已结束' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdBy" label="创建人" width="100" />
                <el-table-column prop="createdAt" label="创建时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="editSemester(scope.row)"
                      :disabled="scope.row.status === 'ACTIVE'"
                    >
                      <i class="el-icon-edit"></i>
                      编辑
                    </el-button>
                    <el-button 
                      type="success" 
                      size="small" 
                      @click="setAsActive(scope.row)"
                      :disabled="scope.row.status === 'ACTIVE'"
                    >
                      <i class="el-icon-star-on"></i>
                      设置为当前
                    </el-button>
                    <el-button 
                      type="danger" 
                      size="small" 
                      @click="deleteSemester(scope.row)"
                    >
                      <i class="el-icon-delete"></i>
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 权限设置 -->
        <el-tab-pane label="权限设置" name="permission">
          <div class="tab-content">
            <div class="action-buttons">
              <el-button type="primary" @click="addRole" size="default">
                <i class="el-icon-plus"></i>
                添加角色
              </el-button>
            </div>
            
            <div class="permission-tabs">
              <el-tabs v-model="activeRoleTab" type="card">
                <el-tab-pane 
                  v-for="role in roles" 
                  :key="role.id" 
                  :label="role.name"
                  :name="role.id.toString()"
                >
                  <div class="role-permissions">
                    <h3 class="role-title">{{ role.name }} - 权限设置</h3>
                    <div class="permissions-list">
                      <el-checkbox-group v-model="role.permissions">
                        <el-checkbox 
                          v-for="permission in allPermissions" 
                          :key="permission.code" 
                          :label="permission.code"
                          :disabled="role.id === 1 && permission.code === 'SYSTEM_ADMIN'"
                        >
                          <span class="permission-name">{{ permission.name }}</span>
                          <span class="permission-desc">{{ permission.description }}</span>
                        </el-checkbox>
                      </el-checkbox-group>
                    </div>
                    <div class="permission-actions">
                      <el-button type="primary" @click="saveRolePermissions(role)" size="default">
                        <i class="el-icon-check"></i>
                        保存权限
                      </el-button>
                      <el-button @click="resetRolePermissions(role)" size="default">
                        <i class="el-icon-refresh"></i>
                        重置
                      </el-button>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 数据管理 -->
        <el-tab-pane label="数据管理" name="data">
          <div class="tab-content">
            <el-card class="data-card">
              <div class="data-section">
                <h3 class="section-title">数据备份</h3>
                <p class="section-desc">定期备份系统数据，防止数据丢失。建议每周进行一次完整备份。</p>
                <div class="backup-actions">
                  <el-button type="primary" @click="backupDatabase" size="default">
                    <i class="el-icon-document-copy"></i>
                    立即备份
                  </el-button>
                  <el-select v-model="backupType" placeholder="选择备份类型" style="width: 150px; margin-left: 12px;">
                    <el-option label="完整备份" value="full" />
                    <el-option label="增量备份" value="incremental" />
                  </el-select>
                </div>
                
                <div class="backup-history">
                  <h4>备份历史</h4>
                  <el-table :data="backupHistory" style="width: 100%">
                    <el-table-column prop="id" label="ID" width="80" />
                    <el-table-column prop="type" label="备份类型" width="120" />
                    <el-table-column prop="size" label="文件大小" width="100" />
                    <el-table-column prop="createdAt" label="备份时间" width="180">
                      <template #default="scope">
                        {{ formatDateTime(scope.row.createdAt) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120">
                      <template #default="scope">
                        <el-button type="primary" size="small" @click="downloadBackup(scope.row)">
                          <i class="el-icon-download"></i>
                          下载
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </el-card>
            
            <el-card class="data-card">
              <div class="data-section">
                <h3 class="section-title">数据恢复</h3>
                <p class="section-desc">从备份文件恢复系统数据，恢复操作会覆盖现有数据，请谨慎操作！</p>
                <el-upload
                  class="upload-demo"
                  action="#"
                  :auto-upload="false"
                  :on-change="handleBackupFileChange"
                  :file-list="backupFileList"
                  accept=".bak,.sql"
                  :limit="1"
                >
                  <el-button size="small" type="primary">
                    <i class="el-icon-upload2"></i>
                    上传备份文件
                  </el-button>
                  <div slot="tip" class="el-upload__tip">
                    支持上传.bak和.sql格式的备份文件
                  </div>
                </el-upload>
                <div class="restore-action" v-if="backupFileList.length > 0">
                  <el-button 
                    type="danger" 
                    @click="restoreDatabase"
                    :loading="loading.restore"
                    size="default"
                  >
                    <i class="el-icon-refresh-left"></i>
                    恢复数据
                  </el-button>
                  <el-popconfirm
                    title="确认恢复数据？"
                    confirm-button-text="确认恢复"
                    cancel-button-text="取消"
                    icon="el-icon-warning"
                    icon-color="#f56c6c"
                    @confirm="confirmRestore"
                  >
                    <template #reference>
                      <el-button type="danger" size="default">
                        <i class="el-icon-check"></i>
                        确认恢复
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </el-card>
            
            <el-card class="data-card">
              <div class="data-section">
                <h3 class="section-title">数据清理</h3>
                <p class="section-desc">清理系统中的临时数据、日志等，优化系统性能。</p>
                <el-form :model="cleanupForm">
                  <el-checkbox-group v-model="cleanupForm.cleanupItems">
                    <el-checkbox label="清理操作日志">清理30天前的操作日志</el-checkbox>
                    <el-checkbox label="清理登录日志">清理60天前的登录日志</el-checkbox>
                    <el-checkbox label="清理临时文件">清理上传的临时文件</el-checkbox>
                    <el-checkbox label="清理缓存数据">清理系统缓存数据</el-checkbox>
                  </el-checkbox-group>
                  <div class="cleanup-action">
                    <el-button type="warning" @click="cleanupData" size="default">
                      <i class="el-icon-delete"></i>
                      执行清理
                    </el-button>
                  </div>
                </el-form>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        
        <!-- 日志管理 -->
        <el-tab-pane label="日志管理" name="logs">
          <div class="tab-content">
            <div class="log-filters">
              <el-select v-model="logType" placeholder="选择日志类型" style="width: 150px; margin-right: 12px;">
                <el-option label="操作日志" value="operation" />
                <el-option label="登录日志" value="login" />
                <el-option label="系统日志" value="system" />
              </el-select>
              
              <el-input 
                v-model="logKeyword" 
                placeholder="输入关键字搜索" 
                prefix-icon="el-icon-search" 
                style="width: 250px; margin-right: 12px;"
                @keyup.enter="searchLogs"
              />
              
              <el-date-picker
                v-model="logDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 240px; margin-right: 12px;"
              />
              
              <el-button type="primary" @click="searchLogs" size="default">
                <i class="el-icon-search"></i>
                搜索
              </el-button>
              <el-button @click="resetLogFilters" size="default">
                <i class="el-icon-refresh"></i>
                重置
              </el-button>
            </div>
            
            <div class="log-table">
              <el-table 
                v-loading="loading.logs" 
                :data="filteredLogs" 
                style="width: 100%"
              >
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="type" label="类型" width="100">
                  <template #default="scope">
                    <el-tag :type="getLogTypeTag(scope.row.type)">
                      {{ scope.row.type }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="username" label="操作人" width="120" />
                <el-table-column prop="action" label="操作内容" min-width="250" show-overflow-tooltip />
                <el-table-column prop="ip" label="IP地址" width="140" />
                <el-table-column prop="createdAt" label="操作时间" width="180">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button type="info" size="small" @click="viewLogDetails(scope.row)">
                      <i class="el-icon-view"></i>
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="pagination" v-if="filteredLogs.length > 0">
              <el-pagination
                background
                layout="prev, pager, next, jumper, sizes, total"
                :total="filteredLogs.length"
                :page-size="logPageSize"
                :current-page="logCurrentPage"
                :page-sizes="[10, 20, 50, 100]"
                @size-change="handleLogSizeChange"
                @current-change="handleLogCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    
    <!-- 添加学期对话框 -->
    <el-dialog 
      title="添加学期" 
      v-model="addSemesterDialogVisible" 
      width="50%"
    >
      <el-form 
        ref="semesterForm" 
        :model="semesterForm" 
        :rules="semesterRules" 
        label-width="100px"
      >
        <el-form-item label="学期名称" prop="name">
          <el-input v-model="semesterForm.name" placeholder="例如：2023-2024学年第一学期" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="semesterForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="semesterForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="是否当前学期">
          <el-switch v-model="semesterForm.isActive" />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="addSemesterDialogVisible = false" size="default">取消</el-button>
        <el-button type="primary" @click="submitSemesterForm" size="default">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 添加角色对话框 -->
    <el-dialog 
      title="添加角色" 
      v-model="addRoleDialogVisible" 
      width="50%"
    >
      <el-form 
        ref="roleForm" 
        :model="roleForm" 
        :rules="roleRules" 
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            v-model="roleForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入角色描述" 
          />
        </el-form-item>
        <el-form-item label="初始权限">
          <el-checkbox-group v-model="roleForm.permissions">
            <el-checkbox 
              v-for="permission in allPermissions" 
              :key="permission.code" 
              :label="permission.code"
              :disabled="permission.code === 'SYSTEM_ADMIN'"
            >
              {{ permission.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="addRoleDialogVisible = false" size="default">取消</el-button>
        <el-button type="primary" @click="submitRoleForm" size="default">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 日志详情对话框 -->
    <el-dialog 
      title="日志详情" 
      v-model="logDetailsDialogVisible" 
      width="60%"
    >
      <div v-if="currentLog" class="log-details">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="日志类型">{{ currentLog.type }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentLog.username }}</el-descriptions-item>
          <el-descriptions-item label="操作内容">{{ currentLog.action }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="浏览器信息">{{ currentLog.userAgent || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentLog.status }}</el-descriptions-item>
          <el-descriptions-item label="详细信息" v-if="currentLog.details">
            <pre class="log-details-json">{{ JSON.stringify(currentLog.details, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </el-card>
</template>

<script>
export default {
  name: 'Settings',
  data() {
    return {
      activeTab: 'basic',
      loading: {
        semester: false,
        restore: false,
        logs: false
      },
      
      // 基本设置表单
      basicForm: {
        systemName: '学生管理系统',
        systemVersion: 'v1.0.0',
        schoolName: '某某大学',
        contactEmail: 'admin@example.com',
        contactPhone: '010-12345678',
        schoolAddress: '北京市海淀区某某路100号',
        copyright: '© 2023 学生管理系统 版权所有',
        systemDescription: '这是一个现代化的学生管理系统，提供学生信息管理、课程管理、考试管理等功能。'
      },
      basicRules: {
        systemName: [
          { required: true, message: '请输入系统名称', trigger: 'blur' }
        ],
        systemVersion: [
          { required: true, message: '请输入系统版本', trigger: 'blur' }
        ],
        schoolName: [
          { required: true, message: '请输入学校名称', trigger: 'blur' }
        ],
        contactEmail: [
          { required: true, message: '请输入联系邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      
      // 学期管理
      semesters: [],
      addSemesterDialogVisible: false,
      semesterForm: {
        id: '',
        name: '',
        startDate: '',
        endDate: '',
        isActive: false
      },
      semesterRules: {
        name: [
          { required: true, message: '请输入学期名称', trigger: 'blur' }
        ],
        startDate: [
          { required: true, message: '请选择开始日期', trigger: 'change' }
        ],
        endDate: [
          { required: true, message: '请选择结束日期', trigger: 'change' }
        ]
      },
      
      // 权限管理
      roles: [],
      activeRoleTab: '1',
      addRoleDialogVisible: false,
      roleForm: {
        id: '',
        name: '',
        description: '',
        permissions: []
      },
      roleRules: {
        name: [
          { required: true, message: '请输入角色名称', trigger: 'blur' }
        ]
      },
      allPermissions: [
        { code: 'SYSTEM_ADMIN', name: '系统管理员', description: '系统最高权限' },
        { code: 'STUDENT_MANAGE', name: '学生管理', description: '学生信息的增删改查' },
        { code: 'TEACHER_MANAGE', name: '教师管理', description: '教师信息的增删改查' },
        { code: 'COURSE_MANAGE', name: '课程管理', description: '课程信息的增删改查' },
        { code: 'EXAM_MANAGE', name: '考试管理', description: '考试信息的增删改查' },
        { code: 'GRADE_MANAGE', name: '成绩管理', description: '成绩的录入、修改和查询' },
        { code: 'DEPARTMENT_MANAGE', name: '院系管理', description: '院系信息的增删改查' },
        { code: 'SETTINGS_MANAGE', name: '系统设置', description: '系统配置的修改权限' },
        { code: 'DATA_BACKUP_RESTORE', name: '数据备份恢复', description: '数据备份与恢复权限' },
        { code: 'LOG_VIEW', name: '日志查看', description: '系统日志的查看权限' }
      ],
      
      // 数据管理
      backupType: 'full',
      backupHistory: [],
      backupFileList: [],
      cleanupForm: {
        cleanupItems: []
      },
      
      // 日志管理
      logType: 'operation',
      logKeyword: '',
      logDateRange: [],
      logs: [],
      filteredLogs: [],
      logCurrentPage: 1,
      logPageSize: 10,
      logDetailsDialogVisible: false,
      currentLog: null
    }
  },
  computed: {
    // 分页后的日志数据
    paginatedLogs() {
      const start = (this.logCurrentPage - 1) * this.logPageSize
      const end = start + this.logPageSize
      return this.filteredLogs.slice(start, end)
    }
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    // 加载初始数据
    async loadInitialData() {
      // 加载基本设置
      await this.loadBasicSettings()
      // 加载学期数据
      this.loadSemesters()
      // 加载角色数据
      this.loadRoles()
      // 加载日志数据
      this.loadLogs()
      // 加载备份历史
      this.loadBackupHistory()
    },
    
    // 加载基本设置
    async loadBasicSettings() {
      try {
        const response = await this.$axios.get('/api/admin/settings/basic')
        // 确保使用API返回的数据覆盖表单
        this.basicForm = {
          systemName: response.data.systemName || '',
          systemVersion: response.data.systemVersion || '',
          schoolName: response.data.schoolName || '',
          contactEmail: response.data.contactEmail || '',
          contactPhone: response.data.contactPhone || '',
          schoolAddress: response.data.schoolAddress || '',
          copyright: response.data.copyright || '',
          systemDescription: response.data.systemDescription || ''
        }
      } catch (error) {
        console.error('加载基本设置失败:', error)
        this.$message.error('加载基本设置失败，请稍后重试')
      }
    },
    
    // 保存基本设置
    async saveBasicSettings() {
      this.$refs.basicForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const response = await this.$axios.put('/api/admin/settings/basic', this.basicForm)
          
          if (response.data.success) {
            this.$message.success(response.data.message || '基本设置保存成功')
          } else {
            this.$message.error(response.data.message || '保存失败')
          }
        } catch (error) {
          console.error('保存基本设置失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 重置基本设置
    resetBasicSettings() {
      this.basicForm = {
        systemName: '学生管理系统',
        systemVersion: 'v1.0.0',
        schoolName: '某某大学',
        contactEmail: 'admin@example.com',
        contactPhone: '010-12345678',
        schoolAddress: '北京市海淀区某某路100号',
        copyright: '© 2023 学生管理系统 版权所有',
        systemDescription: '这是一个现代化的学生管理系统，提供学生信息管理、课程管理、考试管理等功能。'
      }
    },
    
    // 加载学期数据
    async loadSemesters() {
      this.loading.semester = true
      try {
        const response = await this.$axios.get('/api/semesters')
        this.semesters = response.data.data || response.data
      } catch (error) {
        console.error('加载学期数据失败:', error)
        this.$message.error('加载学期数据失败，请稍后重试')
      } finally {
        this.loading.semester = false
      }
    },
    
    // 刷新学期列表
    refreshSemesterList() {
      this.loadSemesters()
    },
    
    // 添加学期
    addSemester() {
      this.semesterForm = {
        id: '',
        name: '',
        startDate: '',
        endDate: '',
        isActive: false
      }
      this.addSemesterDialogVisible = true
    },
    
    // 编辑学期
    editSemester(semester) {
      this.semesterForm = { ...semester }
      this.addSemesterDialogVisible = true
    },
    
    // 提交学期表单
    async submitSemesterForm() {
      this.$refs.semesterForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const formData = { ...this.semesterForm }
          
          // 验证结束日期必须大于开始日期
          if (new Date(formData.endDate) <= new Date(formData.startDate)) {
            this.$message.error('结束日期必须大于开始日期')
            return
          }
          
          if (formData.id) {
            // 更新学期
            const response = await this.$axios.put(`/api/semesters/${formData.id}`, formData)
            
            if (response.data.success) {
              // 找到更新后的学期并替换本地数据
              const updatedSemester = response.data.data
              const index = this.semesters.findIndex(s => s.id === formData.id)
              if (index !== -1) {
                this.semesters[index] = updatedSemester
              }
              this.$message.success(response.data.message || '学期更新成功')
            } else {
              this.$message.error(response.data.message || '更新失败')
            }
          } else {
            // 创建学期
            const response = await this.$axios.post('/api/semesters', formData)
            
            if (response.data.success) {
              // 添加新学期到列表
              this.semesters.push(response.data.data)
              this.$message.success(response.data.message || '学期添加成功')
            } else {
              this.$message.error(response.data.message || '添加失败')
            }
          }
          
          this.addSemesterDialogVisible = false
        } catch (error) {
          console.error('保存学期失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 设置为当前学期
    async setAsActive(semester) {
      try {
        // 这里需要调用后端API来设置为当前学期
        // 假设我们可以通过更新学期的isActive字段来实现
        const updatedSemester = { ...semester, isActive: true }
        const response = await this.$axios.put(`/api/semesters/${semester.id}`, updatedSemester)
        
        if (response.data.success) {
          // 更新本地数据
          this.semesters.forEach(s => {
            s.isActive = s.id === semester.id
            s.status = s.id === semester.id ? 'ACTIVE' : 'INACTIVE'
          })
          
          this.$message.success(response.data.message || '设置成功')
        } else {
          this.$message.error(response.data.message || '设置失败')
        }
      } catch (error) {
        console.error('设置当前学期失败:', error)
        this.$message.error('设置失败，请稍后重试')
      }
    },
    
    // 删除学期
    deleteSemester(semester) {
      this.$confirm(`确定要删除学期"${semester.name}"吗？删除后不可恢复！`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/api/semesters/${semester.id}`)
          
          if (response.data.success) {
            // 从本地列表中移除
            const index = this.semesters.findIndex(s => s.id === semester.id)
            if (index !== -1) {
              this.semesters.splice(index, 1)
            }
            
            this.$message.success(response.data.message || '删除成功')
          } else {
            this.$message.error(response.data.message || '删除失败')
          }
        } catch (error) {
          console.error('删除学期失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 加载角色数据
    async loadRoles() {
      try {
        // 使用现有的用户数据作为角色数据
        const response = await this.$axios.get('/api/users')
        const users = response.data.data || response.data
        
        // 提取唯一角色
        const roleMap = {}
        users.forEach(user => {
          const roleKey = user.role
          if (!roleMap[roleKey]) {
            roleMap[roleKey] = {
              id: roleKey,
              name: this.getRoleName(roleKey),
              description: this.getRoleDescription(roleKey),
              permissions: []
            }
          }
        })
        
        this.roles = Object.values(roleMap)
      } catch (error) {
        console.error('加载角色数据失败:', error)
        this.$message.error('加载角色数据失败，请稍后重试')
      }
    },
    
    // 获取角色名称
    getRoleName(roleKey) {
      const roleNames = {
        'ADMIN': '系统管理员',
        'TEACHER': '教师',
        'STUDENT': '学生'
      }
      return roleNames[roleKey] || roleKey
    },
    
    // 获取角色描述
    getRoleDescription(roleKey) {
      const roleDescriptions = {
        'ADMIN': '系统最高权限管理员，可以管理所有功能',
        'TEACHER': '教师角色，可管理课程和学生成绩',
        'STUDENT': '学生角色，可查看个人信息和成绩'
      }
      return roleDescriptions[roleKey] || '未知角色'
    },
    
    // 添加角色
    addRole() {
      this.roleForm = {
        id: '',
        name: '',
        description: '',
        permissions: []
      }
      this.addRoleDialogVisible = true
    },
    
    // 提交角色表单
    submitRoleForm() {
      this.$refs.roleForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const formData = { ...this.roleForm }
          
          if (formData.id) {
            // 更新角色
            // await axios.put(`/api/roles/${formData.id}`, formData, {
            //   headers: {
            //     'Authorization': `Bearer ${localStorage.getItem('token')}`
            //   }
            // })
            
            // 模拟更新
            const index = this.roles.findIndex(r => r.id === formData.id)
            if (index !== -1) {
              this.roles[index] = formData
            }
          } else {
            // 创建角色
            // await axios.post('/api/roles', formData, {
            //   headers: {
            //     'Authorization': `Bearer ${localStorage.getItem('token')}`
            //   }
            // })
            
            // 模拟创建
            formData.id = Date.now()
            this.roles.push(formData)
          }
          
          this.addRoleDialogVisible = false
          this.$message.success(formData.id ? '角色更新成功' : '角色添加成功')
        } catch (error) {
          console.error('保存角色失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 保存角色权限
    async saveRolePermissions(role) {
      try {
        // 角色权限管理需要根据实际的权限系统设计来实现
        // 目前系统基于Spring Security的ROLE_前缀权限控制
        // 可以将权限信息保存到系统设置表中
        const permissionSetting = {
          settingKey: `role_${role.id}_permissions`,
          settingValue: JSON.stringify(role.permissions),
          settingName: `${role.name}权限配置`,
          settingDescription: `${role.name}角色的权限设置`,
          settingType: 'JSON',
          category: 'PERMISSION',
          sortOrder: 1,
          isEnabled: true
        }
        
        const response = await this.$axios.post('/api/admin/settings', permissionSetting)
        
        if (response.data.success) {
          this.$message.success('权限保存成功')
        } else {
          this.$message.error(response.data.message || '权限保存失败')
        }
      } catch (error) {
        console.error('保存角色权限失败:', error)
        this.$message.error('保存失败，请稍后重试')
      }
    },
    
    // 重置角色权限
    resetRolePermissions(role) {
      // 重新加载角色数据
      const originalRole = this.roles.find(r => r.id === role.id)
      if (originalRole) {
        role.permissions = [...originalRole.permissions]
      }
    },
    
    // 加载备份历史
    loadBackupHistory() {
      // 模拟备份历史数据
      const now = new Date()
      const oneDay = 24 * 60 * 60 * 1000
      
      this.backupHistory = [
        {
          id: 1,
          type: '完整备份',
          size: '256MB',
          createdAt: new Date(now - oneDay * 1).toISOString()
        },
        {
          id: 2,
          type: '增量备份',
          size: '56MB',
          createdAt: new Date(now - oneDay * 3).toISOString()
        },
        {
          id: 3,
          type: '完整备份',
          size: '248MB',
          createdAt: new Date(now - oneDay * 7).toISOString()
        }
      ]
    },
    
    // 备份数据库
    async backupDatabase() {
      try {
        // 显示加载状态
        const loading = this.$loading({
          lock: true,
          text: '正在执行备份...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })
        
        // 模拟备份过程
        await new Promise(resolve => setTimeout(resolve, 3000))
        
        // 实际项目中应该调用后端API
        // const response = await axios.post('/admin/data/backup', {
        //   type: this.backupType
        // }, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`
        //   }
        // })
        
        // 添加新的备份记录
        const newBackup = {
          id: Date.now(),
          type: this.backupType === 'full' ? '完整备份' : '增量备份',
          size: this.backupType === 'full' ? '约250MB' : '约60MB',
          createdAt: new Date().toISOString()
        }
        this.backupHistory.unshift(newBackup)
        
        loading.close()
        this.$message.success('数据库备份成功')
      } catch (error) {
        console.error('数据库备份失败:', error)
        this.$message.error('备份失败，请稍后重试')
      }
    },
    
    // 下载备份文件
    downloadBackup(backup) {
      // 实际项目中应该调用后端API下载文件
      this.$message.info('正在下载备份文件...')
    },
    
    // 处理备份文件上传
    handleBackupFileChange(file) {
      this.backupFileList = [file]
    },
    
    // 恢复数据库
    restoreDatabase() {
      this.$confirm('恢复数据库将覆盖所有现有数据，请确认您已做好备份！此操作不可撤销！', '危险提示', {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        this.confirmRestore()
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 确认恢复数据库
    async confirmRestore() {
      if (this.backupFileList.length === 0) {
        this.$message.warning('请先上传备份文件')
        return
      }
      
      this.loading.restore = true
      try {
        // 显示加载状态
        const loading = this.$loading({
          lock: true,
          text: '正在恢复数据，请稍候...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })
        
        // 模拟恢复过程
        await new Promise(resolve => setTimeout(resolve, 5000))
        
        // 实际项目中应该调用后端API
        // const formData = new FormData()
        // formData.append('file', this.backupFileList[0].raw)
        // await axios.post('/admin/data/restore', formData, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`,
        //     'Content-Type': 'multipart/form-data'
        //   }
        // })
        
        loading.close()
        this.backupFileList = []
        this.$message.success('数据恢复成功，请重新登录系统')
        
        // 实际项目中应该跳转到登录页
        // this.$router.push('/login')
      } catch (error) {
        console.error('数据恢复失败:', error)
        this.$message.error('恢复失败，请检查备份文件并重试')
      } finally {
        this.loading.restore = false
      }
    },
    
    // 清理数据
    async cleanupData() {
      if (this.cleanupForm.cleanupItems.length === 0) {
        this.$message.warning('请选择要清理的数据项')
        return
      }
      
      this.$confirm('确定要执行数据清理操作吗？清理后的数据将无法恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 显示加载状态
          const loading = this.$loading({
            lock: true,
            text: '正在清理数据...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          })
          
          // 模拟清理过程
          await new Promise(resolve => setTimeout(resolve, 3000))
          
          // 实际项目中应该调用后端API
          // await axios.post('/admin/data/cleanup', this.cleanupForm, {
          //   headers: {
          //     'Authorization': `Bearer ${localStorage.getItem('token')}`
          //   }
          // })
          
          loading.close()
          this.cleanupForm.cleanupItems = []
          this.$message.success('数据清理完成')
        } catch (error) {
          console.error('数据清理失败:', error)
          this.$message.error('清理失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 加载日志数据
    async loadLogs() {
      this.loading.logs = true
      try {
        // 目前没有日志管理功能，使用空数组
        this.logs = []
        this.filteredLogs = []
      } catch (error) {
        console.error('加载日志数据失败:', error)
        this.logs = []
        this.filteredLogs = []
      } finally {
        this.loading.logs = false
      }
    },
    
    // 搜索日志
    searchLogs() {
      this.logCurrentPage = 1
      this.filteredLogs = this.logs.filter(log => {
        // 类型筛选
        if (this.logType && log.type !== this.logType) {
          return false
        }
        
        // 关键词搜索
        if (this.logKeyword) {
          const keyword = this.logKeyword.toLowerCase()
          if (!log.username.toLowerCase().includes(keyword) && 
              !log.action.toLowerCase().includes(keyword) && 
              !log.ip.toLowerCase().includes(keyword)) {
            return false
          }
        }
        
        // 日期范围筛选
        if (this.logDateRange && this.logDateRange.length === 2) {
          const logDate = new Date(log.createdAt)
          if (logDate < this.logDateRange[0] || logDate > this.logDateRange[1]) {
            return false
          }
        }
        
        return true
      })
    },
    
    // 重置日志筛选条件
    resetLogFilters() {
      this.logKeyword = ''
      this.logDateRange = []
      this.filteredLogs = [...this.logs]
      this.logCurrentPage = 1
    },
    
    // 查看日志详情
    viewLogDetails(log) {
      this.currentLog = { ...log }
      this.logDetailsDialogVisible = true
    },
    
    // 获取日志类型标签
    getLogTypeTag(type) {
      const tagMap = {
        'operation': 'primary',
        'login': 'success',
        'system': 'info'
      }
      return tagMap[type] || 'info'
    },
    
    // 日志分页相关方法
    handleLogSizeChange(newSize) {
      this.logPageSize = newSize
      this.logCurrentPage = 1
    },
    
    handleLogCurrentChange(newCurrent) {
      this.logCurrentPage = newCurrent
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
    

  }
}
</script>

<style scoped>


.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.tab-content {
  padding: 20px 0;
}

.form-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  gap: 12px;
  padding: 20px 0 0;
  border-top: 1px solid #ebeef5;
}

.action-buttons {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.semester-table,
.log-table {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.permission-tabs {
  margin-top: 20px;
}

.role-permissions {
  padding: 20px 0;
}

.role-title {
  margin-bottom: 20px;
  color: #303133;
}

.permissions-list {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.permission-name {
  font-weight: bold;
  margin-right: 10px;
  color: #303133;
}

.permission-desc {
  color: #909399;
  font-size: 12px;
}

.permission-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.data-card {
  margin-bottom: 30px;
}

.data-section {
  padding: 20px 0;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.section-desc {
  margin-bottom: 20px;
  color: #606266;
  line-height: 1.6;
}

.backup-actions {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.backup-history {
  margin-top: 30px;
}

.backup-history h4 {
  margin-bottom: 15px;
  color: #303133;
}

.restore-action {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.cleanup-action {
  margin-top: 20px;
}

.log-filters {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.log-details {
  padding: 10px 0;
}

.log-details-json {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  max-height: 400px;
}


</style>