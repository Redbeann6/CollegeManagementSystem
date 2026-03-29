<template>
  <div class="student-notifications">
    <el-card class="notifications-header">
      <h1>通知公告</h1>
      <div class="header-actions">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索通知标题或内容" 
          prefix-icon="el-icon-search"
          style="width: 300px; margin-right: 12px;"
        />
        <el-select 
          v-model="statusFilter" 
          placeholder="筛选状态"
          clearable
          style="width: 150px; margin-right: 12px;"
        >
          <el-option label="全部" value="" />
          <el-option label="未读" value="unread" />
          <el-option label="已读" value="read" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px;"
        />
      </div>
    </el-card>
    
    <el-card class="notifications-content">
      <div class="notifications-stats">
        <div class="stat-item">
          <div class="stat-number">{{ totalNotifications }}</div>
          <div class="stat-label">总通知数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ unreadNotifications }}</div>
          <div class="stat-label">未读通知</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ importantNotifications }}</div>
          <div class="stat-label">重要通知</div>
        </div>
      </div>
      
      <div class="notification-actions">
        <el-button 
          type="primary" 
          size="small"
          @click="markAllAsRead"
          :disabled="!hasUnread"
        >
          全部已读
        </el-button>
        <el-button 
          type="warning" 
          size="small"
          @click="deleteSelectedNotifications"
          :disabled="selectedNotifications.length === 0"
        >
          删除选中
        </el-button>
      </div>
      
      <el-table 
        :data="filteredNotifications" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="通知标题" width="300">
          <template #default="scope">
            <div class="notification-title">
              <span 
                :class="{ 'unread': scope.row.status === 'unread', 'important': scope.row.isImportant }"
              >
                {{ scope.row.title }}
              </span>
              <el-tag 
                v-if="scope.row.isImportant" 
                type="danger" 
                size="small"
                style="margin-left: 8px;"
              >
                重要
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="通知内容" width="400">
          <template #default="scope">
            <div class="notification-content-preview">{{ getContentPreview(scope.row.content) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="sender" label="发布者" width="120" />
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'unread' ? 'primary' : 'info'">
              {{ scope.row.status === 'unread' ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <div class="table-actions">
              <el-button type="primary" size="small" @click="viewNotificationDetails(scope.row)">
                查看详情
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="deleteNotification(scope.row.id)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredNotifications.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 通知详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="notificationDetail?.title || '通知详情'" 
      width="60%"
      @open="onDialogOpen"
    >
      <div class="notification-detail">
        <div class="detail-header">
          <h2>{{ notificationDetail?.title }}</h2>
          <div class="detail-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ notificationDetail?.sender }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(notificationDetail?.createTime) }}
            </span>
          </div>
        </div>
        <div class="detail-content" v-html="notificationDetail?.content"></div>
        <div class="detail-footer" v-if="notificationDetail?.attachments && notificationDetail.attachments.length > 0">
          <h3>附件</h3>
          <ul class="attachment-list">
            <li v-for="(attachment, index) in notificationDetail.attachments" :key="index">
              <el-icon><Document /></el-icon>
              <span>{{ attachment.name }}</span>
              <el-button type="link" size="small">下载</el-button>
            </li>
          </ul>
        </div>
      </div>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog 
      v-model="deleteDialogVisible" 
      title="确认删除" 
      width="30%"
    >
      <span>{{ deleteDialogContent }}</span>
      <template #footer>
        <el-button size="small" @click="deleteDialogVisible = false">取消</el-button>
        <el-button size="small" type="danger" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Clock, Document } from '@element-plus/icons-vue';
import axios from 'axios';

// 生成模拟通知数据
const generateMockNotifications = () => {
  return [
    {
      id: 1,
      title: '新学期开学通知',
      content: '<p>各位同学：</p><p>新学期即将开始，请大家做好以下准备：</p><ul><li>1. 9月1日正式上课</li><li>2. 请提前完成学费缴纳</li><li>3. 携带相关证件到校</li></ul><p>祝大家新学期顺利！</p>',
      sender: '学校办公室',
      createTime: '2024-08-20T09:00:00',
      status: 'unread',
      isImportant: true,
      attachments: []
    },
    {
      id: 2,
      title: '关于选课的重要通知',
      content: '<p>选课系统将于9月5日开放，请同学们及时关注。</p><p>本次选课分为三个阶段：</p><ul><li>第一阶段：9月5日-9月7日（优先选课）</li><li>第二阶段：9月8日-9月10日（常规选课）</li><li>第三阶段：9月11日-9月12日（补选）</li></ul>',
      sender: '教务处',
      createTime: '2024-08-25T14:30:00',
      status: 'unread',
      isImportant: true,
      attachments: []
    },
    {
      id: 3,
      title: '图书馆开放时间调整通知',
      content: '<p>从9月1日起，图书馆开放时间调整为：</p><p>周一至周五：8:00-22:00</p><p>周六至周日：9:00-20:00</p><p>节假日另行通知。</p>',
      sender: '图书馆',
      createTime: '2024-08-28T10:00:00',
      status: 'read',
      isImportant: false,
      attachments: []
    },
    {
      id: 4,
      title: '奖学金评选通知',
      content: '<p>2024-2025学年奖学金评选工作即将开始，请符合条件的同学积极申请。</p><p>申请截止日期：9月15日</p><p>详情请查看学校官网通知。</p>',
      sender: '学生处',
      createTime: '2024-08-30T16:00:00',
      status: 'unread',
      isImportant: true,
      attachments: []
    },
    {
      id: 5,
      title: '校园网使用指南',
      content: '<p>为了更好地服务广大师生，现将校园网使用指南发布如下：</p><ul><li>1. 校园网账号为学号/工号</li><li>2. 初始密码为身份证后六位</li><li>3. 可在校园网自助服务平台修改密码</li></ul><p>如有问题，请联系网络中心：88888888</p>',
      sender: '网络中心',
      createTime: '2024-09-01T09:30:00',
      status: 'read',
      isImportant: false,
      attachments: []
    }
  ];
};

export default {
  name: 'StudentNotifications',
  components: {
    User,
    Clock,
    Document
  },
  setup() {
    // 状态管理
    const searchQuery = ref('');
    const statusFilter = ref('');
    const dateRange = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const selectedNotifications = ref([]);
    const dialogVisible = ref(false);
    const deleteDialogVisible = ref(false);
    const deleteDialogContent = ref('');
    const notificationDetail = ref(null);
    const notifications = ref([]);
    
    // 加载通知数据
    const loadNotifications = async () => {
      try {
        const response = await axios.get('/api/student/notifications', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        notifications.value = response.data.data || [];
      } catch (error) {
        console.error('加载通知失败:', error);
        notifications.value = [];
        ElMessage.error('加载通知数据失败，请检查后端服务');
      }
    };
    
    // 加载未读通知数量
    const loadUnreadCount = async () => {
      try {
        const response = await axios.get('/api/student/notifications/unread-count', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        // 更新全局未读消息数量
        if (window.updateGlobalUnreadCount) {
          window.updateGlobalUnreadCount(response.data.data);
        }
      } catch (error) {
        console.error('加载未读通知数量失败:', error);
      }
    };
    
    // 初始化加载通知
    loadNotifications();
    
    // 加载未读通知数量
    loadUnreadCount();
    
    // 计算属性
    const totalNotifications = computed(() => filteredNotifications.value.length);
    const unreadNotifications = computed(() => 
      notifications.value.filter(notification => notification.status === 'unread').length
    );
    const importantNotifications = computed(() => 
      notifications.value.filter(notification => notification.isImportant).length
    );
    const hasUnread = computed(() => unreadNotifications.value > 0);
    
    // 过滤通知
    const filteredNotifications = computed(() => {
      let result = [...notifications.value];
      
      // 搜索过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(notification => 
          notification.title.toLowerCase().includes(query) || 
          notification.content.toLowerCase().includes(query)
        );
      }
      
      // 状态过滤
      if (statusFilter.value) {
        result = result.filter(notification => notification.status === statusFilter.value);
      }
      
      // 日期范围过滤
      if (dateRange.value && dateRange.value.length === 2) {
        const startDate = new Date(dateRange.value[0]);
        const endDate = new Date(dateRange.value[1]);
        endDate.setHours(23, 59, 59, 999);
        
        result = result.filter(notification => {
          const notificationDate = new Date(notification.createTime);
          return notificationDate >= startDate && notificationDate <= endDate;
        });
      }
      
      // 按时间降序排序
      result.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
      
      return result;
    });
    
    // 方法
    const getContentPreview = (content) => {
      // 移除HTML标签并截取前100个字符
      const plainText = content.replace(/<[^>]+>/g, '');
      return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText;
    };
    
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '';
      const date = new Date(dateTime);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };
    
    const viewNotificationDetails = async (notification) => {
      notificationDetail.value = { ...notification };
      dialogVisible.value = true;
      
      // 标记为已读
      if (notification.status === 'unread') {
        try {
          await axios.post(`/api/student/notifications/${notification.id}/mark-read`, {}, {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          });
          
          // 更新本地状态
          const index = notifications.value.findIndex(n => n.id === notification.id);
          if (index !== -1) {
            notifications.value[index].status = 'read';
            notifications.value[index].isRead = true;
          }
        } catch (error) {
          console.error('标记通知为已读失败:', error);
          ElMessage.error('标记通知为已读失败');
        }
      }
    };
    
    const onDialogOpen = () => {
      // 对话框打开时的操作
    };
    
    const handleSelectionChange = (selection) => {
      selectedNotifications.value = selection;
    };
    
    const markAllAsRead = async () => {
      try {
        // 找到所有未读通知
        const unreadNotifications = notifications.value.filter(notification => notification.status === 'unread');
        
        // 批量更新本地状态
        notifications.value.forEach(notification => {
          if (notification.status === 'unread') {
            notification.status = 'read';
            notification.isRead = true;
          }
        });
        
        // 可以考虑添加一个后端批量标记已读的API，但现在我们只需要刷新未读数量
        await loadUnreadCount(); // 刷新未读数量
        
        ElMessage.success('全部标记为已读成功');
      } catch (error) {
        console.error('标记全部已读失败:', error);
        ElMessage.error('标记全部已读失败');
      }
    };
    
    // 保存要删除的通知ID或选择的通知
    const notificationToDelete = ref(null);
    const selectedToDelete = ref(false);
    
    const deleteNotification = (id) => {
      notificationToDelete.value = id;
      selectedToDelete.value = false;
      deleteDialogContent.value = '确定要删除这条通知吗？';
      deleteDialogVisible.value = true;
    };
    
    const deleteSelectedNotifications = () => {
      if (selectedNotifications.value.length === 0) return;
      
      notificationToDelete.value = null;
      selectedToDelete.value = true;
      deleteDialogContent.value = `确定要删除选中的 ${selectedNotifications.value.length} 条通知吗？`;
      deleteDialogVisible.value = true;
    };
    
    const confirmDelete = () => {
      deleteDialogVisible.value = false;
      
      if (selectedToDelete.value) {
        // 删除选中的通知
        const idsToDelete = selectedNotifications.value.map(n => n.id);
        notifications.value = notifications.value.filter(n => !idsToDelete.includes(n.id));
        selectedNotifications.value = [];
      } else if (notificationToDelete.value) {
        // 删除单个通知
        const index = notifications.value.findIndex(n => n.id === notificationToDelete.value);
        if (index !== -1) {
          notifications.value.splice(index, 1);
        }
      }
      
      // 重置状态
      notificationToDelete.value = null;
      selectedToDelete.value = false;
    };
    
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize;
      currentPage.value = 1;
    };
    
    const handleCurrentChange = (newCurrent) => {
      currentPage.value = newCurrent;
    };
    
    // 点击行查看通知详情
    const handleRowClick = (row) => {
      viewNotificationDetails(row);
    };
    
    // 生命周期
    onMounted(() => {
      // 通知数据已经在setup中通过loadNotifications函数加载
    });
    
    return {
      searchQuery,
      statusFilter,
      dateRange,
      currentPage,
      pageSize,
      selectedNotifications,
      dialogVisible,
      deleteDialogVisible,
      deleteDialogContent,
      notificationDetail,
      notifications,
      notificationToDelete,
      selectedToDelete,
      filteredNotifications,
      totalNotifications,
      unreadNotifications,
      importantNotifications,
      hasUnread,
      getContentPreview,
      formatDateTime,
      viewNotificationDetails,
      onDialogOpen,
      handleSelectionChange,
      markAllAsRead,
      deleteNotification,
      deleteSelectedNotifications,
      confirmDelete,
      handleSizeChange,
      handleCurrentChange,
      handleRowClick
    };
  }
};
</script>

<style scoped>
.student-notifications {
  padding: 20px;
}

.notifications-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notifications-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
}

.notifications-stats {
  display: flex;
  margin-bottom: 20px;
  gap: 20px;
}

.stat-item {
  background: #f7f8fa;
  padding: 15px 20px;
  border-radius: 8px;
  text-align: center;
  min-width: 120px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #1976d2;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.notification-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

/* 统一按钮大小 */
.notification-actions .el-button,
.el-table .el-button {
  width: 100px !important;
  height: 36px !important;
  padding: 0 !important;
  text-align: center !important;
  font-size: 12px !important;
  margin: 0 !important;
  box-sizing: border-box !important;
  line-height: 36px !important;
}

/* 表格操作按钮容器 */
.el-table-column--fixed-right .table-actions {
  display: flex !important;
  flex-direction: column !important;
  align-items: flex-start !important;
  gap: 8px !important;
  width: 100% !important;
  padding: 10px !important;
  margin: 0 !important;
  box-sizing: border-box !important;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-title .unread {
  font-weight: bold;
  color: #303133;
}

.notification-title .important {
  color: #f56c6c;
}

.notification-content-preview {
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 通知详情样式 */
.notification-detail {
  line-height: 1.6;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.detail-header h2 {
  margin: 0 0 15px 0;
  font-size: 20px;
  color: #303133;
}

.detail-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.detail-content {
  margin-bottom: 20px;
  font-size: 15px;
  color: #606266;
  word-wrap: break-word;
}

.detail-footer h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.attachment-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.attachment-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.attachment-list li:last-child {
  border-bottom: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notifications-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
    gap: 10px;
  }
  
  .header-actions > * {
    width: 100%;
  }
  
  .notifications-stats {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .stat-item {
    flex: 1;
    min-width: 100px;
  }
  
  .notification-actions {
    flex-direction: column;
  }
  
  .notification-actions > * {
    width: 100%;
  }
}
</style>