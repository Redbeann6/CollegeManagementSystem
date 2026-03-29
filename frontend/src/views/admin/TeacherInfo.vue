<template>
  <div class="teacher-info-container">
    <el-card class="management-header">
      <h1>教师信息管理</h1>
    </el-card>
    
    <el-card class="teacher-content">
      <!-- 筛选和搜索区域 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="5">
            <el-select v-model="filterOptions.departmentId" placeholder="选择系部" style="width: 100%;">
              <el-option label="全部" value="" />
              <el-option 
                v-for="dept in departments" 
                :key="dept.id" 
                :label="dept.name" 
                :value="dept.id" 
              />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select v-model="filterOptions.title" placeholder="职称" style="width: 100%;">
              <el-option label="全部" value="" />
              <el-option label="助教" value="ASSISTANT" />
              <el-option label="讲师" value="LECTURER" />
              <el-option label="副教授" value="ASSOCIATE_PROFESSOR" />
              <el-option label="教授" value="PROFESSOR" />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select v-model="filterOptions.education" placeholder="学历" style="width: 100%;">
              <el-option label="全部" value="" />
              <el-option label="本科" value="BACHELOR" />
              <el-option label="硕士" value="MASTER" />
              <el-option label="博士" value="DOCTOR" />
            </el-select>
          </el-col>
          <el-col :span="9">
            <el-input 
              v-model="filterOptions.keyword" 
              placeholder="输入姓名、工号、手机号或邮箱关键词" 
              prefix-icon="el-icon-search" 
              style="width: 100%;"
              @keyup.enter="searchTeachers"
            >
              <el-button 
                slot="append" 
                icon="el-icon-search" 
                @click="searchTeachers"
              />
            </el-input>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="5">
            <el-select v-model="filterOptions.gender" placeholder="性别" style="width: 100%;">
              <el-option label="全部" value="" />
              <el-option label="男" value="MALE" />
              <el-option label="女" value="FEMALE" />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select v-model="filterOptions.status" placeholder="工作状态" style="width: 100%;">
              <el-option label="全部" value="" />
              <el-option label="在职" value="ACTIVE" />
              <el-option label="休假" value="LEAVE" />
              <el-option label="离职" value="INACTIVE" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button @click="resetFilters" style="width: 100%;">
              <i class="el-icon-refresh"></i> 重置
            </el-button>
          </el-col>
        </el-row>
      </div>
      
      <!-- 功能按钮区域 -->
      <div class="action-section">
        <el-button type="primary" @click="openCreateTeacherDialog">
          <i class="el-icon-plus"></i> 添加教师
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedTeachers.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload2"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportData" :disabled="filteredTeachers.length === 0">
          <i class="el-icon-download"></i> 导出数据
        </el-button>
        <el-button @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新数据
        </el-button>
        <el-button type="warning" @click="printTeacherCards">
          <i class="el-icon-printer"></i> 打印教师卡片
        </el-button>
      </div>
      
      <!-- 教师列表表格 -->
      <div class="table-section">
        <el-table 
          v-loading="loading" 
          :data="paginatedTeachers" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="教师ID" width="100" />
          <el-table-column prop="name" label="姓名" width="100">
            <template #default="scope">
              <div class="name-cell">
                <el-avatar :src="scope.row.avatar || defaultAvatar" size="small" style="margin-right: 8px;">
                  {{ getInitials(scope.row.name) }}
                </el-avatar>
                {{ scope.row.name }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="employeeId" label="工号" width="120" />
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              <el-tag :type="getGenderTag(scope.row.gender)">
                {{ getGenderText(scope.row.gender) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="birthDate" label="出生日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.birthDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="department" label="所属系部" width="150">
            <template #default="scope">
              {{ getDepartmentName(scope.row.departmentId) }}
            </template>
          </el-table-column>
          <el-table-column prop="title" label="职称" width="120">
            <template #default="scope">
              <el-tag :type="getTitleTag(scope.row.title)">
                {{ getTitleText(scope.row.title) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="education" label="学历" width="100">
            <template #default="scope">
              <el-tag :type="getEducationTag(scope.row.education)">
                {{ getEducationText(scope.row.education) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phoneNumber" label="联系电话" width="120" show-overflow-tooltip />
          <el-table-column prop="email" label="电子邮箱" width="180" show-overflow-tooltip />
          <el-table-column prop="hireDate" label="入职日期" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.hireDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewTeacherDetail(scope.row)"
              >
                详情
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="editTeacher(scope.row)"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteTeacher(scope.row.id)"
              >
                删除
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
          :total="filteredTeachers.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 教师详情对话框 -->
    <el-dialog 
      title="教师详情" 
      v-model="detailDialogVisible" 
      width="70%"
    >
      <div class="teacher-detail">
        <!-- 基本信息区域 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>基本信息</h2>
          </div>
          
          <div class="detail-content">
            <div class="detail-avatar">
              <el-avatar :src="currentTeacher.avatar || defaultAvatar" size="100">
                {{ getInitials(currentTeacher.name) }}
              </el-avatar>
            </div>
            
            <div class="detail-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form label-width="120px" :model="currentTeacher">
                    <el-form-item label="姓名">
                      <span>{{ currentTeacher.name }}</span>
                    </el-form-item>
                    <el-form-item label="工号">
                      <span>{{ currentTeacher.employeeId }}</span>
                    </el-form-item>
                    <el-form-item label="性别">
                      <span>{{ getGenderText(currentTeacher.gender) }}</span>
                    </el-form-item>
                    <el-form-item label="出生日期">
                      <span>{{ formatDate(currentTeacher.birthDate) }}</span>
                    </el-form-item>
                    <el-form-item label="民族">
                      <span>{{ currentTeacher.nationality || '-' }}</span>
                    </el-form-item>
                    <el-form-item label="政治面貌">
                      <span>{{ currentTeacher.politicalStatus || '-' }}</span>
                    </el-form-item>
                  </el-form>
                </el-col>
                <el-col :span="12">
                  <el-form label-width="120px" :model="currentTeacher">
                    <el-form-item label="联系电话">
                      <span>{{ currentTeacher.phoneNumber }}</span>
                    </el-form-item>
                    <el-form-item label="电子邮箱">
                      <span>{{ currentTeacher.email }}</span>
                    </el-form-item>
                    <el-form-item label="身份证号">
                      <span>{{ currentTeacher.idNumber || '-' }}</span>
                    </el-form-item>
                    <el-form-item label="地址">
                      <span>{{ currentTeacher.address || '-' }}</span>
                    </el-form-item>
                    <el-form-item label="工作状态">
                      <el-tag :type="getStatusTag(currentTeacher.status)">
                        {{ getStatusText(currentTeacher.status) }}
                      </el-tag>
                    </el-form-item>
                  </el-form>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
        
        <!-- 工作信息区域 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>工作信息</h2>
          </div>
          
          <div class="detail-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form label-width="120px" :model="currentTeacher">
                  <el-form-item label="所属系部">
                    <span>{{ getDepartmentName(currentTeacher.departmentId) }}</span>
                  </el-form-item>
                  <el-form-item label="职称">
                    <span>{{ getTitleText(currentTeacher.title) }}</span>
                  </el-form-item>
                  <el-form-item label="职务">
                    <span>{{ currentTeacher.position || '-' }}</span>
                  </el-form-item>
                  <el-form-item label="入职日期">
                    <span>{{ formatDate(currentTeacher.hireDate) }}</span>
                  </el-form-item>
                </el-form>
              </el-col>
              <el-col :span="12">
                <el-form label-width="120px" :model="currentTeacher">
                  <el-form-item label="学历">
                    <span>{{ getEducationText(currentTeacher.education) }}</span>
                  </el-form-item>
                  <el-form-item label="毕业院校">
                    <span>{{ currentTeacher.graduationSchool || '-' }}</span>
                  </el-form-item>
                  <el-form-item label="专业">
                    <span>{{ currentTeacher.major || '-' }}</span>
                  </el-form-item>
                  <el-form-item label="最高学历毕业时间">
                    <span>{{ formatDate(currentTeacher.graduationDate) }}</span>
                  </el-form-item>
                </el-form>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <!-- 教学信息区域 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>教学信息</h2>
          </div>
          
          <div class="detail-content">
            <el-form label-width="120px" :model="currentTeacher">
              <el-form-item label="主讲课程">
                <div class="courses-list">
                  <el-tag 
                    v-for="course in currentTeacher.courses || []" 
                    :key="course.id" 
                    class="course-tag"
                  >
                    {{ course.name }}
                  </el-tag>
                  <span v-if="!currentTeacher.courses || currentTeacher.courses.length === 0">暂无</span>
                </div>
              </el-form-item>
              <el-form-item label="教学方向">
                <span>{{ currentTeacher.teachingDirection || '-' }}</span>
              </el-form-item>
              <el-form-item label="研究方向">
                <span>{{ currentTeacher.researchDirection || '-' }}</span>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <!-- 账号信息区域 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>账号信息</h2>
          </div>
          
          <div class="detail-content">
            <el-form label-width="120px" :model="currentTeacher">
              <el-form-item label="用户名">
                <span>{{ currentTeacher.username }}</span>
              </el-form-item>
              <el-form-item label="账号创建时间">
                <span>{{ formatDateTime(currentTeacher.createdAt) }}</span>
              </el-form-item>
              <el-form-item label="最后登录时间">
                <span>{{ formatDateTime(currentTeacher.lastLoginAt) || '-' }}</span>
              </el-form-item>
              <el-form-item label="账号状态">
                <el-tag :type="currentTeacher.accountStatus === 'ENABLED' ? 'success' : 'danger'">
                  {{ currentTeacher.accountStatus === 'ENABLED' ? '启用' : '禁用' }}
                </el-tag>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editTeacher(currentTeacher)">编辑</el-button>
      </div>
    </el-dialog>
    
    <!-- 添加/编辑教师对话框 -->
    <el-dialog 
      :title="editMode ? '编辑教师信息' : '添加教师'" 
      v-model="editDialogVisible" 
      width="80%"
      :fullscreen="true"
    >
      <div class="teacher-form">
        <el-form 
          ref="teacherFormRef" 
          :model="teacherForm" 
          :rules="teacherRules" 
          label-width="120px"
          class="form-container"
        >
          <!-- 基本信息卡片 -->
          <el-card class="form-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="teacherForm.name" placeholder="请输入教师姓名" />
                </el-form-item>
                
                <el-form-item label="工号" prop="employeeId">
                  <el-input v-model="teacherForm.employeeId" placeholder="请输入工号" />
                </el-form-item>
                
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="teacherForm.gender">
                    <el-radio label="MALE">男</el-radio>
                    <el-radio label="FEMALE">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="出生日期" prop="birthDate">
                  <el-date-picker
                    v-model="teacherForm.birthDate"
                    type="date"
                    placeholder="请选择出生日期"
                    value-format="yyyy-MM-dd"
                    style="width: 100%;"
                  />
                </el-form-item>
                
                <el-form-item label="民族">
                  <el-input v-model="teacherForm.nationality" placeholder="请输入民族" />
                </el-form-item>
                
                <el-form-item label="政治面貌">
                  <el-input v-model="teacherForm.politicalStatus" placeholder="请输入政治面貌" />
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="联系电话" prop="phoneNumber">
                  <el-input v-model="teacherForm.phoneNumber" placeholder="请输入联系电话" />
                </el-form-item>
                
                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="teacherForm.email" placeholder="请输入电子邮箱" />
                </el-form-item>
                
                <el-form-item label="身份证号">
                  <el-input v-model="teacherForm.idNumber" placeholder="请输入身份证号" />
                </el-form-item>
                
                <el-form-item label="地址">
                  <el-input v-model="teacherForm.address" placeholder="请输入地址" />
                </el-form-item>
                
                <el-form-item label="工作状态" prop="status">
                  <el-select v-model="teacherForm.status" placeholder="请选择工作状态">
                    <el-option label="在职" value="ACTIVE" />
                    <el-option label="休假" value="LEAVE" />
                    <el-option label="离职" value="INACTIVE" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
          
          <!-- 工作信息卡片 -->
          <el-card class="form-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>工作信息</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所属系部" prop="departmentId">
                  <el-select v-model="teacherForm.departmentId" placeholder="请选择系部">
                    <el-option 
                      v-for="dept in departments" 
                      :key="dept.id" 
                      :label="dept.name" 
                      :value="dept.id" 
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="职称" prop="title">
                  <el-select v-model="teacherForm.title" placeholder="请选择职称">
                    <el-option label="助教" value="ASSISTANT" />
                    <el-option label="讲师" value="LECTURER" />
                    <el-option label="副教授" value="ASSOCIATE_PROFESSOR" />
                    <el-option label="教授" value="PROFESSOR" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="职务">
                  <el-input v-model="teacherForm.position" placeholder="请输入职务" />
                </el-form-item>
                
                <el-form-item label="入职日期" prop="hireDate">
                  <el-date-picker
                    v-model="teacherForm.hireDate"
                    type="date"
                    placeholder="请选择入职日期"
                    value-format="yyyy-MM-dd"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="学历" prop="education">
                  <el-select v-model="teacherForm.education" placeholder="请选择学历">
                    <el-option label="本科" value="BACHELOR" />
                    <el-option label="硕士" value="MASTER" />
                    <el-option label="博士" value="DOCTOR" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="毕业院校">
                  <el-input v-model="teacherForm.graduationSchool" placeholder="请输入毕业院校" />
                </el-form-item>
                
                <el-form-item label="专业">
                  <el-input v-model="teacherForm.major" placeholder="请输入专业" />
                </el-form-item>
                
                <el-form-item label="最高学历毕业时间">
                  <el-date-picker
                    v-model="teacherForm.graduationDate"
                    type="date"
                    placeholder="请选择毕业时间"
                    value-format="yyyy-MM-dd"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
          
          <!-- 教学信息卡片 -->
          <el-card class="form-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>教学信息</span>
              </div>
            </template>
            
            <el-form-item label="主讲课程">
              <el-select 
                v-model="teacherForm.courseIds" 
                multiple 
                placeholder="请选择主讲课程" 
                style="width: 100%;"
              >
                <el-option 
                  v-for="course in courses" 
                  :key="course.id" 
                  :label="course.name" 
                  :value="course.id" 
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="教学方向">
              <el-input v-model="teacherForm.teachingDirection" placeholder="请输入教学方向" />
            </el-form-item>
            
            <el-form-item label="研究方向">
              <el-input v-model="teacherForm.researchDirection" placeholder="请输入研究方向" />
            </el-form-item>
          </el-card>
          
          <!-- 账号信息卡片 -->
          <el-card class="form-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>账号信息</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="teacherForm.username" placeholder="请输入用户名" />
                </el-form-item>
                
                <el-form-item 
                  v-if="!editMode" 
                  label="初始密码" 
                  prop="password"
                >
                  <el-input v-model="teacherForm.password" placeholder="请输入初始密码" show-password />
                </el-form-item>
                
                <el-form-item 
                  v-if="editMode" 
                  label="修改密码" 
                  tooltip="不修改请留空"
                >
                  <el-input v-model="teacherForm.password" placeholder="请输入新密码" show-password />
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item label="账号状态" prop="accountStatus">
                  <el-select v-model="teacherForm.accountStatus" placeholder="请选择账号状态">
                    <el-option label="启用" value="ENABLED" />
                    <el-option label="禁用" value="DISABLED" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="头像上传">
                  <el-upload
                    class="avatar-uploader"
                    action=""
                    :show-file-list="false"
                    :on-change="handleAvatarChange"
                    :before-upload="beforeAvatarUpload"
                    :auto-upload="false"
                  >
                    <img v-if="teacherForm.avatar" :src="teacherForm.avatar" class="avatar" />
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                  </el-upload>
                  <div class="avatar-tips">点击上传头像，建议尺寸200x200px，支持JPG、PNG格式</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
        </el-form>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="confirmEdit">保存</el-button>
      </div>
    </el-dialog>
    
    <!-- 批量删除确认对话框 -->
    <el-dialog 
      title="确认删除" 
      v-model="deleteDialogVisible" 
      width="40%"
    >
      <div class="delete-dialog">
        <p>您确定要删除选中的 <strong>{{ selectedTeachers.length }}</strong> 名教师吗？</p>
        <p class="delete-warning">此操作不可撤销，删除后数据将无法恢复！</p>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入教师信息" 
      v-model="importDialogVisible" 
      width="60%"
    >
      <div class="import-dialog">
        <el-form label-width="150px">
          <el-form-item label="下载模板">
            <el-button type="primary" @click="downloadTemplate">
              <i class="el-icon-download"></i> 下载Excel模板
            </el-button>
          </el-form-item>
          
          <el-form-item label="上传文件">
            <el-upload
              class="upload-demo"
              drag
              action=""
              :auto-upload="false"
              :before-upload="beforeImportUpload"
              :on-change="handleImportFileChange"
              accept=".xlsx,.xls"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              <div class="el-upload__tip" slot="tip">只能上传xlsx/xls文件，且不超过10MB</div>
            </el-upload>
            
            <div v-if="importFile" class="file-info">
              <el-tag closable @close="removeImportFile">{{ importFile.name }}</el-tag>
            </div>
          </el-form-item>
          
          <el-form-item label="导入选项">
            <el-checkbox v-model="importOptions.updateIfExist">存在则更新</el-checkbox>
          </el-form-item>
        </el-form>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelImport">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmImport" 
          :disabled="!importFile"
        >
          <i class="el-icon-upload2"></i> 开始导入
        </el-button>
      </div>
    </el-dialog>
    
    <!-- 导入结果对话框 -->
    <el-dialog 
      title="导入结果" 
      v-model="importResultDialogVisible" 
      width="60%"
    >
      <div class="import-result">
        <div class="result-summary">
          <div class="result-item" :class="{ 'success': importResult.successCount > 0, 'error': importResult.errorCount > 0 }">
            <i class="el-icon-check" v-if="importResult.successCount > 0"></i>
            <i class="el-icon-close" v-else></i>
            成功导入: <span class="count">{{ importResult.successCount }}</span>
          </div>
          <div class="result-item" :class="{ 'error': importResult.errorCount > 0 }">
            <i class="el-icon-close" v-if="importResult.errorCount > 0"></i>
            <i class="el-icon-check" v-else></i>
            导入失败: <span class="count">{{ importResult.errorCount }}</span>
          </div>
        </div>
        
        <div class="result-detail" v-if="importResult.errorMessages && importResult.errorMessages.length > 0">
          <h3>错误详情</h3>
          <el-table :data="importResult.errorMessages" style="width: 100%;">
            <el-table-column prop="rowIndex" label="行号" width="80" />
            <el-table-column prop="message" label="错误信息" />
          </el-table>
          
          <div class="result-actions">
            <el-button type="primary" @click="downloadErrorLog">
              <i class="el-icon-download"></i> 下载错误日志
            </el-button>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="importResultDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherInfo',
  data() {
    return {
      // 加载状态
      loading: false,
      
      // 筛选条件
      filterOptions: {
        departmentId: '',
        title: '',
        education: '',
        gender: '',
        status: '',
        keyword: ''
      },
      
      // 数据列表
      teachers: [],
      filteredTeachers: [],
      departments: [],
      courses: [],
      
      // 分页相关
      currentPage: 1,
      pageSize: 20,
      
      // 选中的记录
      selectedTeachers: [],
      
      // 对话框相关
      detailDialogVisible: false,
      editDialogVisible: false,
      deleteDialogVisible: false,
      importDialogVisible: false,
      importResultDialogVisible: false,
      editMode: false,
      
      // 当前操作的教师
      currentTeacher: {},
      
      // 默认头像
      defaultAvatar: '',
      
      // 教师表单
      teacherForm: {
        id: null,
        name: '',
        employeeId: '',
        gender: 'MALE',
        birthDate: '',
        nationality: '',
        politicalStatus: '',
        phoneNumber: '',
        email: '',
        idNumber: '',
        address: '',
        status: 'ACTIVE',
        departmentId: '',
        title: 'LECTURER',
        position: '',
        hireDate: '',
        education: 'BACHELOR',
        graduationSchool: '',
        major: '',
        graduationDate: '',
        teachingDirection: '',
        researchDirection: '',
        courseIds: [],
        username: '',
        password: '',
        accountStatus: 'ENABLED',
        avatar: ''
      },
      
      // 表单验证规则
      teacherRules: {
        name: [{ required: true, message: '请输入教师姓名', trigger: 'blur' }],
        employeeId: [
          { required: true, message: '请输入工号', trigger: 'blur' },
          { validator: this.validateEmployeeId, trigger: 'blur' }
        ],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
        phoneNumber: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入电子邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入有效的电子邮箱', trigger: 'blur' }
        ],
        status: [{ required: true, message: '请选择工作状态', trigger: 'change' }],
        departmentId: [{ required: true, message: '请选择所属系部', trigger: 'change' }],
        title: [{ required: true, message: '请选择职称', trigger: 'change' }],
        hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
        education: [{ required: true, message: '请选择学历', trigger: 'change' }],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
          { validator: this.validateUsername, trigger: 'blur' }
        ],
        password: [
          { 
            validator: (rule, value, callback) => {
              if (!this.editMode && !value) {
                callback(new Error('请输入初始密码'))
              } else if (value && value.length < 6) {
                callback(new Error('密码长度至少为6位'))
              } else {
                callback()
              }
            }, 
            trigger: 'blur' 
          }
        ],
        accountStatus: [{ required: true, message: '请选择账号状态', trigger: 'change' }]
      },
      
      // 导入相关
      importFile: null,
      importOptions: {
        updateIfExist: true
      },
      importResult: {
        successCount: 0,
        errorCount: 0,
        errorMessages: []
      }
    }
  },
  computed: {
    // 分页后的数据
    paginatedTeachers() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredTeachers.slice(start, end)
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
          this.loadDepartments(),
          this.loadCourses(),
          this.loadTeachers()
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
    
    // 加载课程数据
    async loadCourses() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/courses')
      // this.courses = response.data
      
      // 使用模拟数据
      this.courses = [
        { id: 1, name: '数据结构', departmentId: 1, credit: 3, hours: 48 },
        { id: 2, name: '算法分析与设计', departmentId: 1, credit: 4, hours: 64 },
        { id: 3, name: '计算机网络', departmentId: 1, credit: 4, hours: 64 },
        { id: 4, name: '操作系统', departmentId: 1, credit: 4, hours: 64 },
        { id: 5, name: '数据库原理', departmentId: 1, credit: 4, hours: 64 },
        { id: 6, name: '软件工程', departmentId: 1, credit: 4, hours: 64 },
        { id: 7, name: '计算机组成原理', departmentId: 1, credit: 4, hours: 64 },
        { id: 8, name: '编程语言', departmentId: 1, credit: 3, hours: 48 },
        { id: 9, name: '电子电路', departmentId: 2, credit: 4, hours: 64 },
        { id: 10, name: '机械设计', departmentId: 3, credit: 4, hours: 64 },
        { id: 11, name: '管理学原理', departmentId: 4, credit: 3, hours: 48 },
        { id: 12, name: '文学理论', departmentId: 5, credit: 3, hours: 48 }
      ]
    },
    
    // 加载教师数据
    async loadTeachers() {
      // 实际项目中应该调用后端API
      // const response = await axios.get('/api/teachers')
      // this.teachers = response.data
      
      // 使用模拟数据
      this.teachers = this.generateMockTeachers()
      this.filteredTeachers = [...this.teachers]
    },
    
    // 刷新数据
    refreshData() {
      this.loadTeachers()
      this.selectedTeachers = []
    },
    
    // 搜索教师
    searchTeachers() {
      this.currentPage = 1
      
      this.filteredTeachers = this.teachers.filter(teacher => {
        // 按系部筛选
        if (this.filterOptions.departmentId && teacher.departmentId !== this.filterOptions.departmentId) {
          return false
        }
        
        // 按职称筛选
        if (this.filterOptions.title && teacher.title !== this.filterOptions.title) {
          return false
        }
        
        // 按学历筛选
        if (this.filterOptions.education && teacher.education !== this.filterOptions.education) {
          return false
        }
        
        // 按性别筛选
        if (this.filterOptions.gender && teacher.gender !== this.filterOptions.gender) {
          return false
        }
        
        // 按状态筛选
        if (this.filterOptions.status && teacher.status !== this.filterOptions.status) {
          return false
        }
        
        // 按关键词筛选
        if (this.filterOptions.keyword) {
          const keyword = this.filterOptions.keyword.toLowerCase()
          
          return teacher.name.toLowerCase().includes(keyword) ||
                 teacher.employeeId.toLowerCase().includes(keyword) ||
                 (teacher.phoneNumber && teacher.phoneNumber.includes(keyword)) ||
                 (teacher.email && teacher.email.toLowerCase().includes(keyword))
        }
        
        return true
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.filterOptions = {
        departmentId: '',
        title: '',
        education: '',
        gender: '',
        status: '',
        keyword: ''
      }
      this.filteredTeachers = [...this.teachers]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedTeachers = selection
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.currentPage = 1
    },
    
    handleCurrentChange(newCurrent) {
      this.currentPage = newCurrent
    },
    
    // 查看教师详情
    viewTeacherDetail(teacher) {
      this.currentTeacher = { ...teacher }
      this.detailDialogVisible = true
    },
    
    // 打开添加教师对话框
    openCreateTeacherDialog() {
      this.resetForm()
      this.editMode = false
      this.editDialogVisible = true
    },
    
    // 编辑教师
    editTeacher(teacher) {
      this.resetForm()
      this.editMode = true
      
      // 填充表单数据
      this.teacherForm.id = teacher.id
      this.teacherForm.name = teacher.name
      this.teacherForm.employeeId = teacher.employeeId
      this.teacherForm.gender = teacher.gender
      this.teacherForm.birthDate = this.formatDate(teacher.birthDate)
      this.teacherForm.nationality = teacher.nationality || ''
      this.teacherForm.politicalStatus = teacher.politicalStatus || ''
      this.teacherForm.phoneNumber = teacher.phoneNumber
      this.teacherForm.email = teacher.email
      this.teacherForm.idNumber = teacher.idNumber || ''
      this.teacherForm.address = teacher.address || ''
      this.teacherForm.status = teacher.status
      this.teacherForm.departmentId = teacher.departmentId
      this.teacherForm.title = teacher.title
      this.teacherForm.position = teacher.position || ''
      this.teacherForm.hireDate = this.formatDate(teacher.hireDate)
      this.teacherForm.education = teacher.education
      this.teacherForm.graduationSchool = teacher.graduationSchool || ''
      this.teacherForm.major = teacher.major || ''
      this.teacherForm.graduationDate = teacher.graduationDate ? this.formatDate(teacher.graduationDate) : ''
      this.teacherForm.teachingDirection = teacher.teachingDirection || ''
      this.teacherForm.researchDirection = teacher.researchDirection || ''
      this.teacherForm.username = teacher.username
      this.teacherForm.accountStatus = teacher.accountStatus
      this.teacherForm.avatar = teacher.avatar || ''
      
      // 设置课程ID
      if (teacher.courses && teacher.courses.length > 0) {
        this.teacherForm.courseIds = teacher.courses.map(course => course.id)
      }
      
      this.editDialogVisible = true
    },
    
    // 重置表单
    resetForm() {
      if (this.$refs.teacherFormRef) {
        this.$refs.teacherFormRef.resetFields()
      }
      
      this.teacherForm = {
        id: null,
        name: '',
        employeeId: '',
        gender: 'MALE',
        birthDate: '',
        nationality: '',
        politicalStatus: '',
        phoneNumber: '',
        email: '',
        idNumber: '',
        address: '',
        status: 'ACTIVE',
        departmentId: '',
        title: 'LECTURER',
        position: '',
        hireDate: '',
        education: 'BACHELOR',
        graduationSchool: '',
        major: '',
        graduationDate: '',
        teachingDirection: '',
        researchDirection: '',
        courseIds: [],
        username: '',
        password: '',
        accountStatus: 'ENABLED',
        avatar: ''
      }
    },
    
    // 取消编辑
    cancelEdit() {
      this.editDialogVisible = false
      this.resetForm()
    },
    
    // 确认编辑
    async confirmEdit() {
      this.$refs.teacherFormRef.validate(async (valid) => {
        if (valid) {
          try {
            // 构建教师数据
            const teacherData = {
              ...this.teacherForm
            }
            
            // 实际项目中应该调用后端API
            // let response
            // if (teacherData.id) {
            //   response = await axios.put(`/api/teachers/${teacherData.id}`,  teacherData)
            // } else {
            //   response = await axios.post('/api/teachers', teacherData)
            // }
            
            // 模拟保存数据
            if (this.editMode) {
              const index = this.teachers.findIndex(t => t.id === teacherData.id)
              if (index !== -1) {
                // 构建课程对象
                const courses = teacherData.courseIds.map(id => {
                  const course = this.courses.find(c => c.id === id)
                  return course || { id, name: `课程${id}` }
                })
                
                this.teachers[index] = {
                  ...teacherData,
                  courses
                }
              }
            } else {
              // 构建课程对象
              const courses = teacherData.courseIds.map(id => {
                const course = this.courses.find(c => c.id === id)
                return course || { id, name: `课程${id}` }
              })
              
              this.teachers.unshift({
                ...teacherData,
                id: Date.now(),
                createdAt: new Date().toISOString(),
                lastLoginAt: null,
                courses
              })
            }
            
            this.editDialogVisible = false
            this.searchTeachers()
            this.$message.success(this.editMode ? '教师信息更新成功' : '教师添加成功')
          } catch (error) {
            console.error('保存教师信息失败:', error)
            this.$message.error('操作失败，请稍后重试')
          }
        }
      })
    },
    
    // 删除教师
    deleteTeacher(id) {
      this.$confirm('确定要删除这名教师吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 实际项目中应该调用后端API
          // await axios.delete(`/api/teachers/${id}`)
          
          // 模拟删除
          this.teachers = this.teachers.filter(t => t.id !== id)
          this.filteredTeachers = this.filteredTeachers.filter(t => t.id !== id)
          this.$message.success('教师删除成功')
        } catch (error) {
          console.error('删除教师失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消删除
      })
    },
    
    // 批量删除确认
    batchDelete() {
      if (this.selectedTeachers.length === 0) {
        this.$message.warning('请选择要删除的教师')
        return
      }
      
      this.deleteDialogVisible = true
    },
    
    // 确认批量删除
    async confirmDelete() {
      try {
        // 实际项目中应该调用后端API
        // const ids = this.selectedTeachers.map(t => t.id)
        // await axios.post('/api/teachers/batch-delete', { ids })
        
        // 模拟批量删除
        const ids = this.selectedTeachers.map(t => t.id)
        this.teachers = this.teachers.filter(t => !ids.includes(t.id))
        this.filteredTeachers = this.filteredTeachers.filter(t => !ids.includes(t.id))
        
        this.deleteDialogVisible = false
        this.selectedTeachers = []
        this.$message.success('教师批量删除成功')
      } catch (error) {
        console.error('批量删除失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    
    // 批量导入
    batchImport() {
      this.resetImportData()
      this.importDialogVisible = true
    },
    
    // 重置导入数据
    resetImportData() {
      this.importFile = null
      this.importOptions = {
        updateIfExist: true
      }
      this.importResult = {
        successCount: 0,
        errorCount: 0,
        errorMessages: []
      }
    },
    
    // 下载模板
    downloadTemplate() {
      // 实际项目中应该调用后端API下载模板
      // window.location.href = '/admin/teachers/export-template'
      
      this.$message.info('正在下载模板...')
      
      // 模拟下载
      setTimeout(() => {
        this.$message.success('模板下载成功')
      }, 1000)
    },
    
    // 处理导入文件变化
    handleImportFileChange(file, fileList) {
      this.importFile = file
    },
    
    // 移除导入文件
    removeImportFile() {
      this.importFile = null
    },
    
    // 文件上传前校验
    beforeImportUpload(file) {
      const isExcel = file.type === 'application/vnd.ms-excel' || 
                     file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      const isLt10M = file.size / 1024 / 1024 < 10
      
      if (!isExcel) {
        this.$message.error('只能上传Excel文件!')
      }
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB!')
      }
      
      return isExcel && isLt10M
    },
    
    // 处理头像变化
    handleAvatarChange(file, fileList) {
      // 实际项目中应该上传到服务器并获取URL
      // 这里简单处理，将文件对象的本地URL作为头像URL
      this.teacherForm.avatar = URL.createObjectURL(file.raw)
    },
    
    // 头像上传前校验
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg'
      const isPNG = file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isJPG && !isPNG) {
        this.$message.error('只能上传JPG/PNG格式的图片!')
      }
      if (!isLt2M) {
        this.$message.error('头像大小不能超过2MB!')
      }
      
      return isJPG && isLt2M || isPNG && isLt2M
    },
    
    // 取消导入
    cancelImport() {
      this.importDialogVisible = false
      this.resetImportData()
    },
    
    // 确认导入
    async confirmImport() {
      if (!this.importFile) {
        this.$message.warning('请选择要导入的文件')
        return
      }
      
      try {
        this.importDialogVisible = false
        this.loading = true
        
        // 实际项目中应该调用后端API上传文件
        // const formData = new FormData()
        // formData.append('file', this.importFile.raw)
        // formData.append('updateIfExist', this.importOptions.updateIfExist)
        // const response = await axios.post('/api/teachers/batch-import', formData)
        // this.importResult = response.data
        
        // 模拟导入过程
        setTimeout(() => {
          this.importResult = {
            successCount: Math.floor(Math.random() * 20) + 5,
            errorCount: Math.floor(Math.random() * 5),
            errorMessages: [
              { rowIndex: 5, message: '工号重复' },
              { rowIndex: 8, message: '邮箱格式不正确' },
              { rowIndex: 12, message: '所属系部不存在' }
            ].slice(0, Math.floor(Math.random() * 3))
          }
          
          this.importResultDialogVisible = true
          this.loading = false
        }, 2000)
      } catch (error) {
        console.error('导入失败:', error)
        this.$message.error('导入失败，请稍后重试')
        this.loading = false
      }
    },
    
    // 下载错误日志
    downloadErrorLog() {
      // 实际项目中应该调用后端API下载错误日志
      // window.location.href = '/api/admin/teachers/export-error-log'
      
      this.$message.info('正在下载错误日志...')
      
      // 模拟下载
      setTimeout(() => {
        this.$message.success('错误日志下载成功')
      }, 1000)
    },
    
    // 导出数据
    async exportData() {
      // 实际项目中应该调用后端API
      // const params = { ...this.filterOptions }
      // window.location.href = `/api/admin/teachers/export?${new URLSearchParams(params).toString()}`
      
      this.$message.info('正在导出数据...')
      
      // 模拟导出过程
      setTimeout(() => {
        this.$message.success('数据导出成功')
      }, 1500)
    },
    
    // 打印教师卡片
    printTeacherCards() {
      if (this.selectedTeachers.length === 0) {
        this.$message.warning('请选择要打印的教师')
        return
      }
      
      this.$confirm(
        `确定要打印选中的 ${this.selectedTeachers.length} 名教师的信息卡片吗？`,
        '确认打印',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        // 实际项目中应该调用打印功能
        this.$message.info('正在生成打印内容...')
        
        // 模拟打印
        setTimeout(() => {
          this.$message.success('打印任务已提交')
        }, 1000)
      }).catch(() => {
        // 用户取消打印
      })
    },
    
    // 辅助方法 - 工号验证
    validateEmployeeId(rule, value, callback) {
      // 检查工号是否已存在
      const existingTeacher = this.teachers.find(
        t => t.employeeId === value && t.id !== this.teacherForm.id
      )
      
      if (existingTeacher) {
        callback(new Error('工号已存在'))
      } else {
        callback()
      }
    },
    
    // 辅助方法 - 用户名验证
    validateUsername(rule, value, callback) {
      // 检查用户名是否已存在
      const existingTeacher = this.teachers.find(
        t => t.username === value && t.id !== this.teacherForm.id
      )
      
      if (existingTeacher) {
        callback(new Error('用户名已存在'))
      } else {
        callback()
      }
    },
    
    // 辅助方法 - 获取系部名称
    getDepartmentName(departmentId) {
      const department = this.departments.find(d => d.id === departmentId)
      return department ? department.name : `系部ID: ${departmentId}`
    },
    
    // 辅助方法 - 获取性别文本
    getGenderText(gender) {
      const textMap = {
        'MALE': '男',
        'FEMALE': '女'
      }
      return textMap[gender] || '未知'
    },
    
    // 辅助方法 - 获取性别标签样式
    getGenderTag(gender) {
      return gender === 'MALE' ? 'primary' : 'success'
    },
    
    // 辅助方法 - 获取职称文本
    getTitleText(title) {
      const textMap = {
        'ASSISTANT': '助教',
        'LECTURER': '讲师',
        'ASSOCIATE_PROFESSOR': '副教授',
        'PROFESSOR': '教授'
      }
      return textMap[title] || '未知'
    },
    
    // 辅助方法 - 获取职称标签样式
    getTitleTag(title) {
      const tagMap = {
        'ASSISTANT': 'info',
        'LECTURER': 'primary',
        'ASSOCIATE_PROFESSOR': 'success',
        'PROFESSOR': 'warning'
      }
      return tagMap[title] || 'info'
    },
    
    // 辅助方法 - 获取学历文本
    getEducationText(education) {
      const textMap = {
        'BACHELOR': '本科',
        'MASTER': '硕士',
        'DOCTOR': '博士'
      }
      return textMap[education] || '未知'
    },
    
    // 辅助方法 - 获取学历标签样式
    getEducationTag(education) {
      const tagMap = {
        'BACHELOR': 'info',
        'MASTER': 'primary',
        'DOCTOR': 'success'
      }
      return tagMap[education] || 'info'
    },
    
    // 辅助方法 - 获取状态文本
    getStatusText(status) {
      const textMap = {
        'ACTIVE': '在职',
        'LEAVE': '休假',
        'INACTIVE': '离职'
      }
      return textMap[status] || '未知'
    },
    
    // 辅助方法 - 获取状态标签样式
    getStatusTag(status) {
      const tagMap = {
        'ACTIVE': 'success',
        'LEAVE': 'warning',
        'INACTIVE': 'danger'
      }
      return tagMap[status] || 'info'
    },
    
    // 辅助方法 - 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
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
    
    // 辅助方法 - 获取姓名首字母
    getInitials(name) {
      if (!name) return ''
      return name.charAt(0)
    },
    
    // 生成模拟教师数据
    generateMockTeachers(count = 40) {
      const teachers = []
      const genders = ['MALE', 'FEMALE']
      const titles = ['ASSISTANT', 'LECTURER', 'ASSOCIATE_PROFESSOR', 'PROFESSOR']
      const educations = ['BACHELOR', 'MASTER', 'DOCTOR']
      const statuses = ['ACTIVE', 'LEAVE', 'INACTIVE']
      const accountStatuses = ['ENABLED', 'DISABLED']
      const names = [
        '张教授', '李讲师', '王博士', '赵副教授', '钱助教',
        '孙老师', '周教授', '吴讲师', '郑博士', '陈副教授',
        '杨助教', '黄老师', '朱教授', '秦讲师', '尤博士',
        '许副教授', '何助教', '施老师', '张教授', '孔讲师',
        '曹博士', '严副教授', '华助教', '金老师', '魏教授',
        '陶讲师', '姜博士', '戚副教授', '谢助教', '邹老师',
        '喻教授', '柏讲师', '水博士', '窦副教授', '章助教',
        '云老师', '苏教授', '潘讲师', '葛博士', '奚副教授'
      ]
      
      const politicalStatuses = ['中共党员', '共青团员', '群众']
      
      // 随机生成日期的辅助函数
      const randomDate = (start, end) => {
        return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()))
      }
      
      const now = new Date()
      const minBirthDate = new Date(now.getFullYear() - 65, 0, 1) // 65年前
      const maxBirthDate = new Date(now.getFullYear() - 25, 11, 31) // 25年前
      const minHireDate = new Date(now.getFullYear() - 40, 0, 1) // 40年前
      const maxHireDate = new Date() // 当前日期
      
      for (let i = 1; i <= count; i++) {
        const gender = genders[Math.floor(Math.random() * genders.length)]
        const title = titles[Math.floor(Math.random() * titles.length)]
        const education = educations[Math.floor(Math.random() * educations.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const accountStatus = accountStatuses[Math.floor(Math.random() * accountStatuses.length)]
        const departmentId = this.departments[Math.floor(Math.random() * this.departments.length)].id
        const birthDate = randomDate(minBirthDate, maxBirthDate)
        const hireDate = randomDate(minHireDate, maxHireDate)
        
        // 随机选择1-3门课程
        const randomCourseCount = Math.floor(Math.random() * 3) + 1
        const shuffledCourses = [...this.courses.filter(c => c.departmentId === departmentId)].sort(() => 0.5 - Math.random())
        const assignedCourses = shuffledCourses.slice(0, randomCourseCount)
        
        // 随机生成手机号
        const phoneNumber = '1' + ['3', '5', '7', '8', '9'][Math.floor(Math.random() * 5)] + Math.floor(Math.random() * 1000000000).toString().padStart(9, '0')
        
        // 生成邮箱
        const emailPrefix = names[i-1].replace(/[^\u4e00-\u9fa5]/g, '').toLowerCase()
        const email = emailPrefix + i + '@example.edu.cn'
        
        // 随机生成用户名
        const username = emailPrefix + Math.floor(Math.random() * 100)
        
        // 生成工号
        const employeeId = 'T' + now.getFullYear().toString().substr(2) + String(i).padStart(4, '0')
        
        // 随机生成日期
        const graduationDate = randomDate(minHireDate, maxHireDate)
        
        teachers.push({
          id: i,
          name: names[i-1],
          employeeId,
          gender,
          birthDate: birthDate.toISOString(),
          nationality: '汉',
          politicalStatus: politicalStatuses[Math.floor(Math.random() * politicalStatuses.length)],
          phoneNumber,
          email,
          idNumber: '110101' + birthDate.getFullYear().toString() + 
                   String(birthDate.getMonth() + 1).padStart(2, '0') + 
                   String(birthDate.getDate()).padStart(2, '0') + 
                   Math.floor(Math.random() * 10000).toString().padStart(4, '0'),
          address: '北京市海淀区',
          status,
          departmentId,
          title,
          position: '',
          hireDate: hireDate.toISOString(),
          education,
          graduationSchool: 'XX大学',
          major: '计算机科学与技术',
          graduationDate: graduationDate.toISOString(),
          teachingDirection: '计算机科学',
          researchDirection: '人工智能',
          courses: assignedCourses,
          username,
          accountStatus,
          createdAt: hireDate.toISOString(),
          lastLoginAt: Math.random() > 0.3 ? randomDate(hireDate, now).toISOString() : null,
          avatar: ''
        })
      }
      
      return teachers
    }
  }
}
</script>

<style scoped>
.teacher-info-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.management-header {
  margin-bottom: 20px;
  text-align: center;
}

.management-header h1 {
  color: #303133;
  margin: 0;
}

.teacher-content {
  background-color: #fff;
}

/* 筛选区域样式 */
.filter-section {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

/* 功能按钮区域样式 */
.action-section {
  padding: 20px 20px 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 表格区域样式 */
.table-section {
  padding: 20px;
  overflow-x: auto;
}

/* 分页控件样式 */
.pagination-section {
  padding: 0 20px 20px;
  text-align: right;
}

/* 姓名单元格样式 */
.name-cell {
  display: flex;
  align-items: center;
}

/* 详情对话框样式 */
.teacher-detail {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 10px;
}

.detail-section {
  margin-bottom: 30px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.section-header {
  background-color: #f5f7fa;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h2 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.detail-content {
  padding: 20px;
}

.detail-avatar {
  text-align: center;
  margin-bottom: 20px;
}

.detail-info {
  background-color: #fff;
}

/* 课程标签样式 */
.courses-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.course-tag {
  margin-bottom: 5px;
}

/* 表单样式 */
.teacher-form {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 10px;
}

.form-container {
  padding: 10px 0;
}

.form-card {
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header span {
  font-weight: bold;
}

/* 头像上传样式 */
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}

.avatar-tips {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

/* 删除对话框样式 */
.delete-dialog {
  padding: 20px 0;
}

.delete-warning {
  color: #f56c6c;
  font-weight: bold;
  margin-top: 10px;
}

/* 导入对话框样式 */
.import-dialog {
  padding: 10px 0;
}

.file-info {
  margin-top: 10px;
}

/* 导入结果样式 */
.import-result {
  padding: 10px 0;
}

.result-summary {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  padding: 10px 20px;
  border-radius: 4px;
}

.result-item.success {
  background-color: #f0f9ff;
  color: #67c23a;
}

.result-item.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.result-item .count {
  font-weight: bold;
  font-size: 20px;
}

.result-detail {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.result-detail h3 {
  margin-bottom: 15px;
  color: #303133;
}

.result-actions {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-info-container {
    padding: 10px;
  }
  
  .filter-section {
    padding: 10px;
  }
  
  .action-section {
    padding: 10px;
    flex-direction: column;
  }
  
  .table-section {
    padding: 10px;
  }
  
  .pagination-section {
    padding: 10px;
  }
  
  .detail-content {
    padding: 10px;
  }
  
  .result-summary {
    flex-direction: column;
    gap: 10px;
  }
}

/* 打印样式 */
@media print {
  .teacher-info-container {
    padding: 0;
    background-color: #fff;
  }
  
  .action-section,
  .pagination-section,
  .filter-section {
    display: none;
  }
  
  .management-header,
  .teacher-content {
    box-shadow: none;
    border: none;
  }
  
  .teacher-detail {
    max-height: none;
    overflow-y: visible;
  }
}
</style>