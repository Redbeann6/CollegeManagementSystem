<template>
  <div class="exam-management">
    <el-card class="management-header">
      <h1>考试管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <div class="search-filters">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="输入考试名称/课程名称搜索" 
          prefix-icon="el-icon-search" 
          style="width: 250px; margin-right: 12px;"
          @keyup.enter="searchExams"
        />
        
        <el-select 
          v-model="searchForm.courseId" 
          placeholder="选择课程" 
          clearable 
          style="width: 200px; margin-right: 12px;"
        >
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
        
        <el-select 
          v-model="searchForm.status" 
          placeholder="考试状态" 
          clearable 
          style="width: 120px; margin-right: 12px;"
        >
          <el-option label="未开始" value="NOT_STARTED" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已结束" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        
        <el-button type="primary" @click="searchExams">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增考试
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportExams" :disabled="selectedExams.length === 0">
          <i class="el-icon-download"></i> 导出选中
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedExams.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
      </div>
      
      <div class="exam-table">
        <el-table 
          v-loading="loading" 
          :data="paginatedExams" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleRowDblclick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="考试名称" width="150" />
          <el-table-column prop="courseName" label="所属课程" width="180" />
          <el-table-column prop="examType" label="考试类型" width="100">
            <template #default="scope">
              <el-tag :type="getExamTypeTag(scope.row.examType)">
                {{ getExamTypeName(scope.row.examType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalMarks" label="总分" width="80" />
          <el-table-column prop="duration" label="时长(分钟)" width="100" />
          <el-table-column prop="startTime" label="开始时间" width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="考试地点" width="120" show-overflow-tooltip />
          <el-table-column prop="teacherName" label="监考教师" width="100" />
          <el-table-column prop="studentCount" label="参考人数" width="100">
            <template #default="scope">
              <el-tag size="small" type="primary">
                {{ scope.row.studentCount || 0 }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ getStatusName(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="420" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="showEditDialog(scope.row)"
                icon="el-icon-edit"
                :disabled="scope.row.status === 'COMPLETED'"
              >
                编辑
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                @click="viewExamDetails(scope.row)"
                icon="el-icon-view"
              >
                详情
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="viewExamResults(scope.row)"
                icon="el-icon-data-line"
                :disabled="scope.row.status !== 'COMPLETED'"
              >
                成绩
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteExam(scope.row)"
                icon="el-icon-delete"
                :disabled="scope.row.status === 'IN_PROGRESS' || scope.row.status === 'COMPLETED'"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination" v-if="filteredExams.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredExams.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <div v-if="filteredExams.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无考试数据" />
      </div>
    </el-card>
    
    <!-- 新增/编辑考试对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增考试' : '编辑考试'" 
      v-model="dialogVisible" 
      width="60%"
    >
      <el-form 
        ref="examForm" 
        :model="examForm" 
        :rules="rules" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试名称" prop="name">
              <el-input v-model="examForm.name" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select 
                v-model="examForm.courseId" 
                placeholder="请选择课程" 
                filterable
              >
                <el-option 
                  v-for="course in courses" 
                  :key="course.id" 
                  :label="course.name" 
                  :value="course.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试类型" prop="examType">
              <el-select v-model="examForm.examType" placeholder="请选择考试类型">
                <el-option label="期中考试" value="MIDTERM" />
                <el-option label="期末考试" value="FINAL" />
                <el-option label="测验" value="QUIZ" />
                <el-option label="实验报告" value="LAB_REPORT" />
                <el-option label="论文" value="PAPER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总分" prop="totalMarks">
              <el-input 
                v-model.number="examForm.totalMarks" 
                type="number" 
                min="1" 
                placeholder="请输入总分"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长(分钟)" prop="duration">
              <el-input 
                v-model.number="examForm.duration" 
                type="number" 
                min="5" 
                placeholder="请输入考试时长"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监考教师" prop="teacherId">
              <el-select 
                v-model="examForm.teacherId" 
                placeholder="请选择监考教师" 
                filterable
              >
                <el-option 
                  v-for="teacher in teachers" 
                  :key="teacher.id" 
                  :label="teacher.name" 
                  :value="teacher.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="考试地点" prop="location">
              <el-input v-model="examForm.location" placeholder="请输入考试地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="examForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%;"
                :disabled="dialogType === 'edit' && examForm.status !== 'NOT_STARTED'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="examForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%;"
                :disabled="dialogType === 'edit' && examForm.status !== 'NOT_STARTED'"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider>考试说明</el-divider>
        
        <el-form-item label="考试说明" prop="description">
          <el-input 
            v-model="examForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入考试说明信息" 
          />
        </el-form-item>
        
        <el-form-item label="注意事项" prop="notes">
          <el-input 
            v-model="examForm.notes" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入考试注意事项" 
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 考试详情对话框 -->
    <el-dialog 
      title="考试详情" 
      v-model="detailsDialogVisible" 
      width="70%"
    >
      <div v-if="currentExam" class="exam-details">
        <el-row :gutter="20">
          <el-col :span="24">
            <h3 class="exam-title">{{ currentExam.name }}</h3>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="所属课程">{{ currentExam.courseName }}</el-descriptions-item>
              <el-descriptions-item label="考试类型">{{ getExamTypeName(currentExam.examType) }}</el-descriptions-item>
              <el-descriptions-item label="总分">{{ currentExam.totalMarks }}</el-descriptions-item>
              <el-descriptions-item label="时长">{{ currentExam.duration }} 分钟</el-descriptions-item>
              <el-descriptions-item label="考试状态">
                <el-tag :type="getStatusTag(currentExam.status)">
                  {{ getStatusName(currentExam.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="开始时间">{{ formatDateTime(currentExam.startTime) }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ formatDateTime(currentExam.endTime) }}</el-descriptions-item>
              <el-descriptions-item label="考试地点">{{ currentExam.location }}</el-descriptions-item>
              <el-descriptions-item label="监考教师">{{ currentExam.teacherName }}</el-descriptions-item>
              <el-descriptions-item label="参考人数">{{ currentExam.studentCount || 0 }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="24" class="mt-4">
            <el-divider>考试说明</el-divider>
            <div class="description-content">{{ currentExam.description || '无' }}</div>
          </el-col>
          
          <el-col :span="24" class="mt-4">
            <el-divider>注意事项</el-divider>
            <div class="notes-content">{{ currentExam.notes || '无' }}</div>
          </el-col>
          
          <el-col :span="24" class="mt-4">
            <el-button type="primary" @click="printExamDetails">打印考试详情</el-button>
            <el-button type="info" @click="viewExamResults(currentExam)" :disabled="currentExam.status !== 'COMPLETED'">查看成绩</el-button>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
    
    <!-- 考试成绩对话框 -->
    <el-dialog 
      :title="`${currentExam?.name} - 考试成绩`" 
      v-model="resultsDialogVisible" 
      width="80%"
      fullscreen
    >
      <div v-if="currentExam" class="exam-results">
        <div class="results-header">
          <h3>{{ currentExam.name }} - {{ currentExam.courseName }}</h3>
          <div class="results-stats">
            <el-tag type="info">参考人数：{{ examResults.length }}</el-tag>
            <el-tag type="success">平均分：{{ averageScore.toFixed(2) }}</el-tag>
            <el-tag type="warning">最高分：{{ maxScore || 0 }}</el-tag>
            <el-tag type="danger">最低分：{{ minScore || 0 }}</el-tag>
            <el-tag type="primary">及格率：{{ passRate.toFixed(1) }}%</el-tag>
          </div>
        </div>
        
        <div class="results-search">
          <el-input 
            v-model="resultsSearch" 
            placeholder="搜索学生学号/姓名" 
            prefix-icon="el-icon-search" 
            style="width: 300px;"
            @input="handleResultsSearch"
          />
          <el-select 
            v-model="resultsStatus" 
            placeholder="选择考试状态" 
            clearable 
            style="width: 120px; margin-left: 12px;"
            @change="handleResultsSearch"
          >
            <el-option label="已参加" value="ATTENDED" />
            <el-option label="缺考" value="ABSENT" />
            <el-option label="作弊" value="CHEATED" />
          </el-select>
        </div>
        
        <div class="results-table">
          <el-table 
            :data="paginatedExamResults" 
            style="width: 100%"
            @selection-change="handleResultsSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="studentIdNumber" label="学号" width="120" />
            <el-table-column prop="studentName" label="学生姓名" width="100">
              <template #default="scope">
                <div class="user-info">
                  <img 
                    :src="scope.row.studentAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                    :alt="scope.row.studentName" 
                    class="avatar"
                  />
                  <span>{{ scope.row.studentName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="得分" width="100" :sortable="true">
              <template #default="scope">
                <el-tag :type="getScoreTag(scope.row.score, currentExam.totalMarks)">
                  {{ scope.row.score || '--' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="考试状态" width="100">
              <template #default="scope">
                <el-tag :type="getExamStatusTag(scope.row.status)">
                  {{ getExamStatusName(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="examDate" label="考试时间" width="160">
              <template #default="scope">
                {{ formatDateTime(scope.row.examDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="feedback" label="备注" width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="editScore(scope.row)"
                  icon="el-icon-edit"
                >
                  编辑
                </el-button>
                <el-button 
                  type="info" 
                  size="small" 
                  @click="viewStudentProfile(scope.row)"
                  icon="el-icon-user"
                >
                  学生
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="results-pagination" v-if="filteredExamResults.length > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper, sizes, total"
            :total="filteredExamResults.length"
            :page-size="resultsPageSize"
            :current-page="resultsCurrentPage"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleResultsSizeChange"
            @current-change="handleResultsCurrentChange"
          />
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入考试" 
      v-model="importDialogVisible" 
      width="50%"
    >
      <div class="import-content">
        <p class="import-hint">请下载模板，填写完整信息后导入考试数据。</p>
        
        <div class="template-download">
          <el-button type="primary" @click="downloadTemplate">
            <i class="el-icon-download"></i> 下载导入模板
          </el-button>
        </div>
        
        <div class="import-upload">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="importFileList"
            accept=".xlsx,.xls"
            :limit="1"
          >
            <el-button size="small" type="primary">点击上传文件</el-button>
            <div slot="tip" class="el-upload__tip">
              支持上传Excel文件，仅支持.xlsx和.xls格式
            </div>
          </el-upload>
        </div>
        
        <div v-if="importPreviewData.length > 0" class="import-preview">
          <h3>数据预览</h3>
          <el-table :data="importPreviewData" style="width: 100%" height="300">
            <el-table-column prop="name" label="考试名称" width="180" />
            <el-table-column prop="courseName" label="所属课程" width="150" />
            <el-table-column prop="examType" label="考试类型" width="100" />
            <el-table-column prop="error" label="错误信息" width="200">
              <template #default="scope">
                <span v-if="scope.row.error" class="error-text">{{ scope.row.error }}</span>
                <span v-else class="success-text">验证通过</span>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="hasErrors" class="error-message">
            <el-alert
              title="部分数据验证失败，请检查后重新导入"
              type="warning"
              description="请查看错误信息列了解具体问题"
              show-icon
              :closable="false"
            />
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmImport" 
          :disabled="importFileList.length === 0 || hasErrors"
        >
          确认导入
        </el-button>
      </div>
    </el-dialog>
    
    <!-- 编辑成绩对话框 -->
    <el-dialog 
      title="编辑成绩" 
      v-model="editScoreDialogVisible" 
      width="40%"
    >
      <el-form 
        ref="scoreForm" 
        :model="currentScoreForm" 
        :rules="scoreRules" 
        label-width="100px"
      >
        <el-form-item label="学生姓名">
          <el-input v-model="currentScoreForm.studentName" disabled />
        </el-form-item>
        
        <el-form-item label="学号">
          <el-input v-model="currentScoreForm.studentIdNumber" disabled />
        </el-form-item>
        
        <el-form-item label="考试成绩" prop="score">
          <el-input 
            v-model.number="currentScoreForm.score" 
            type="number" 
            :min="0" 
            :max="currentExam?.totalMarks || 100"
            placeholder="请输入考试成绩"
          />
        </el-form-item>
        
        <el-form-item label="考试状态" prop="status">
          <el-select v-model="currentScoreForm.status" placeholder="请选择考试状态">
            <el-option label="已参加" value="ATTENDED" />
            <el-option label="缺考" value="ABSENT" />
            <el-option label="作弊" value="CHEATED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注" prop="feedback">
          <el-input v-model="currentScoreForm.feedback" type="textarea" rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="editScoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScoreForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ExamManagement',
  data() {
    return {
      loading: false,
      exams: [],
      courses: [],
      teachers: [],
      selectedExams: [],
      filteredExams: [],
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        keyword: '',
        courseId: '',
        status: ''
      },
      dialogVisible: false,
      dialogType: 'add',
      examForm: {
        name: '',
        courseId: '',
        examType: 'MIDTERM',
        totalMarks: 100,
        duration: 90,
        teacherId: '',
        location: '',
        startTime: '',
        endTime: '',
        description: '',
        notes: '',
        status: 'NOT_STARTED',
        courseName: '',
        teacherName: '',
        studentCount: 0
      },
      detailsDialogVisible: false,
      currentExam: null,
      resultsDialogVisible: false,
      examResults: [],
      selectedResults: [],
      resultsSearch: '',
      resultsStatus: '',
      resultsCurrentPage: 1,
      resultsPageSize: 10,
      importDialogVisible: false,
      importFileList: [],
      importPreviewData: [],
      editScoreDialogVisible: false,
      currentScoreForm: {
        id: '',
        studentId: '',
        studentName: '',
        score: null,
        status: 'ATTENDED',
        remark: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入考试名称', trigger: 'blur' },
          { min: 1, max: 100, message: '考试名称长度应为1-100个字符', trigger: 'blur' }
        ],
        courseId: [
          { required: true, message: '请选择所属课程', trigger: 'change' }
        ],
        examType: [
          { required: true, message: '请选择考试类型', trigger: 'change' }
        ],
        totalMarks: [
          { required: true, message: '请输入总分', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              const numValue = Number(value);
              if (isNaN(numValue)) {
                callback(new Error('总分必须为数字'));
              } else if (numValue < 1) {
                callback(new Error('总分必须大于0'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        duration: [
          { required: true, message: '请输入考试时长', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              const numValue = Number(value);
              if (isNaN(numValue)) {
                callback(new Error('考试时长必须为数字'));
              } else if (numValue < 5) {
                callback(new Error('考试时长至少为5分钟'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
        teacherId: [
          { required: true, message: '请选择监考教师', trigger: 'change' }
        ],
        location: [
          { required: true, message: '请输入考试地点', trigger: 'blur' }
        ],
        startTime: [
          { required: true, message: '请选择开始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' }
        ]
      },
      scoreRules: {
        score: [
          { required: true, message: '请输入考试成绩', trigger: 'blur' },
          { type: 'number', message: '考试成绩必须为数字', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 分页后的考试数据
    paginatedExams() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredExams.slice(start, end)
    },
    
    // 过滤后的考试成绩数据
    filteredExamResults() {
      let results = [...this.examResults]
      
      // 按状态过滤
      if (this.resultsStatus) {
        results = results.filter(r => r.status === this.resultsStatus)
      }
      
      // 按关键词过滤
      if (this.resultsSearch) {
        const keyword = this.resultsSearch.toLowerCase()
        results = results.filter(r => 
          r.studentId.toLowerCase().includes(keyword) ||
          r.studentName.toLowerCase().includes(keyword)
        )
      }
      
      // 按成绩排序
      results.sort((a, b) => {
        if (a.score === null && b.score === null) return 0
        if (a.score === null) return 1
        if (b.score === null) return -1
        return b.score - a.score
      })
      
      // 设置排名
      results.forEach((result, index) => {
        if (result.score !== null) {
          result.rank = index + 1
        }
      })
      
      return results
    },
    
    // 分页后的考试成绩数据
    paginatedExamResults() {
      const start = (this.resultsCurrentPage - 1) * this.resultsPageSize
      const end = start + this.resultsPageSize
      return this.filteredExamResults.slice(start, end)
    },
    
    // 是否有错误数据
    hasErrors() {
      return this.importPreviewData.some(item => item.error)
    },
    
    // 平均分
    averageScore() {
      const validScores = this.examResults.filter(r => r.score !== null).map(r => r.score)
      if (validScores.length === 0) return 0
      return validScores.reduce((sum, score) => sum + score, 0) / validScores.length
    },
    
    // 最高分
    maxScore() {
      const validScores = this.examResults.filter(r => r.score !== null).map(r => r.score)
      if (validScores.length === 0) return null
      return Math.max(...validScores)
    },
    
    // 最低分
    minScore() {
      const validScores = this.examResults.filter(r => r.score !== null).map(r => r.score)
      if (validScores.length === 0) return null
      return Math.min(...validScores)
    },
    
    // 及格率
    passRate() {
      const total = this.examResults.filter(r => r.status === 'ATTENDED').length
      if (total === 0) return 0
      const passed = this.examResults.filter(r => r.status === 'ATTENDED' && r.score >= this.currentExam?.totalMarks * 0.6).length
      return (passed / total) * 100
    }
  },
  watch: {
    // 监听考试表单中数值字段的变化，确保它们始终是数字类型
    'examForm.totalMarks': {
      handler(newValue) {
        if (newValue !== undefined && newValue !== null && newValue !== '') {
          // 确保数值字段为数字类型
          this.examForm.totalMarks = Number(newValue);
          // 更新表单验证
          this.$nextTick(() => {
            this.$refs.examForm?.clearValidate?.('totalMarks');
          });
        }
      },
      immediate: false
    },
    'examForm.duration': {
      handler(newValue) {
        if (newValue !== undefined && newValue !== null && newValue !== '') {
          // 确保数值字段为数字类型
          this.examForm.duration = Number(newValue);
          // 更新表单验证
          this.$nextTick(() => {
            this.$refs.examForm?.clearValidate?.('duration');
          });
        }
      },
      immediate: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        // 从后端API获取数据
        await Promise.all([
          this.loadExams(),
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
    
    // 加载考试列表
    async loadExams() {
      try {
        const response = await this.$axios.get('/api/admin/exams')
        if (response.data.success) {
          // 确保数值字段为正确的数据类型
          this.exams = response.data.data.map(exam => ({
            ...exam,
            totalMarks: Number(exam.totalMarks),
            duration: Number(exam.duration)
          }))
          this.filteredExams = [...this.exams]
        } else {
          this.$message.error(response.data.message || '加载考试失败')
        }
      } catch (error) {
        console.error('加载考试失败:', error)
        this.$message.error('加载考试失败，请检查网络连接')
      }
    },
    
    // 加载课程列表
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/admin/courses')
        if (response.data.success) {
          this.courses = response.data.data
        } else {
          this.$message.error(response.data.message || '加载课程失败')
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.$message.error('加载课程失败，请检查网络连接')
      }
    },
    
    // 加载教师列表
    async loadTeachers() {
      try {
        const response = await this.$axios.get('/api/admin/teachers')
        if (response.data.success) {
          this.teachers = response.data.data
        } else {
          this.$message.error(response.data.message || '加载教师失败')
        }
      } catch (error) {
        console.error('加载教师失败:', error)
        this.$message.error('加载教师失败，请检查网络连接')
      }
    },
    
    // 搜索考试
    searchExams() {
      this.currentPage = 1
      this.filteredExams = this.exams.filter(exam => {
        // 关键词搜索
        const keyword = this.searchForm.keyword.toLowerCase()
        if (keyword && !exam.name.toLowerCase().includes(keyword) && !exam.courseName.toLowerCase().includes(keyword)) {
          return false
        }
        
        // 课程筛选
        if (this.searchForm.courseId && exam.courseId !== this.searchForm.courseId) {
          return false
        }
        
        // 状态筛选
        if (this.searchForm.status && exam.status !== this.searchForm.status) {
          return false
        }
        
        return true
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.searchForm = {
        keyword: '',
        courseId: '',
        status: ''
      }
      this.filteredExams = [...this.exams]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedExams = selection
    },
    
    // 处理表格行双击
    handleRowDblclick(row) {
      this.showEditDialog(row)
    },
    
    // 显示新增对话框
    showAddDialog() {
      this.dialogType = 'add'
      this.resetExamForm()
      this.dialogVisible = true
    },
    
    // 显示编辑对话框
    showEditDialog(exam) {
      this.dialogType = 'edit'
      this.examForm = { 
        ...exam,
        // 确保数值字段为正确的数据类型
        totalMarks: Number(exam.totalMarks),
        duration: Number(exam.duration),
        // 确保teacherId字段被正确设置，用于编辑时的教师选择
        teacherId: exam.teacherId || exam.creatorId || '',
        // 确保时间字段正确处理
        startTime: exam.startTime ? new Date(exam.startTime) : null,
        endTime: exam.endTime ? new Date(exam.endTime) : null
      }
      this.dialogVisible = true
    },
    
    // 重置考试表单
    resetExamForm() {
      this.examForm = {
        name: '',
        courseId: '',
        examType: 'MIDTERM',
        totalMarks: 100,  // 确保是数字类型
        duration: 90,    // 确保是数字类型
        teacherId: '',
        location: '',
        startTime: '',
        endTime: '',
        description: '',
        notes: '',
        status: 'NOT_STARTED',
        courseName: '',
        teacherName: '',
        studentCount: 0
      }
      this.$refs.examForm && this.$refs.examForm.resetFields()
    },
    
    // 提交表单
    async submitForm() {
      this.$refs.examForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          // 确保数值字段为正确的数据类型，并处理时间字段
          const formData = { 
            ...this.examForm,
            totalMarks: Number(this.examForm.totalMarks),
            duration: Number(this.examForm.duration),
            // 确保时间字段格式正确
            startTime: this.examForm.startTime ? new Date(this.examForm.startTime).toISOString() : null,
            endTime: this.examForm.endTime ? new Date(this.examForm.endTime).toISOString() : null
          }
          
          // 额外验证关键字段
          if (!formData.courseId || formData.courseId <= 0) {
            this.$message.error('请选择有效的课程');
            return;
          }
          
          if (!formData.teacherId || formData.teacherId <= 0) {
            this.$message.error('请选择有效的教师');
            return;
          }
          
          // 获取课程名称和教师名称
          const course = this.courses.find(c => c.id === formData.courseId)
          const teacher = this.teachers.find(t => t.id === formData.teacherId)
          formData.courseName = course?.name || ''
          formData.teacherName = teacher?.name || ''
          
          if (this.dialogType === 'add') {
            const response = await this.$axios.post('/api/admin/exams', formData)
            if (response.data.success) {
              this.exams.unshift(response.data.data)
              this.dialogVisible = false
              this.searchExams() // 刷新搜索结果
              this.$message.success('考试创建成功')
            } else {
              this.$message.error(response.data.message || '考试创建失败')
            }
          } else {
            const response = await this.$axios.put(`/api/admin/exams/${formData.id}`, formData)
            if (response.data.success) {
              const index = this.exams.findIndex(e => e.id === formData.id)
              if (index !== -1) {
                this.exams[index] = response.data.data
              }
              this.dialogVisible = false
              this.searchExams() // 刷新搜索结果
              this.$message.success('考试更新成功')
            } else {
              this.$message.error(response.data.message || '考试更新失败')
            }
          }
        } catch (error) {
          console.error('保存考试信息失败:', error)
          this.$message.error('保存失败，请稍后重试')
        }
      })
    },
    
    // 删除考试
    async deleteExam(exam) {
      if (exam.status === 'IN_PROGRESS' || exam.status === 'COMPLETED') {
        this.$message.warning('正在进行或已结束的考试不允许删除')
        return
      }
      
      this.$confirm(`确定要删除考试 ${exam.name} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/api/admin/exams/${exam.id}`)
          if (response.data.success) {
            const index = this.exams.findIndex(e => e.id === exam.id)
            if (index !== -1) {
              this.exams.splice(index, 1)
            }
            this.searchExams() // 刷新搜索结果
            this.$message.success('考试删除成功')
          } else {
            this.$message.error(response.data.message || '考试删除失败')
          }
        } catch (error) {
          console.error('删除考试失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量删除
    async batchDelete() {
      // 检查是否包含正在进行或已结束的考试
      const invalidExams = this.selectedExams.filter(e => e.status === 'IN_PROGRESS' || e.status === 'COMPLETED')
      if (invalidExams.length > 0) {
        this.$message.warning('不能删除正在进行或已结束的考试')
        return
      }
      
      this.$confirm(`确定要删除选中的 ${this.selectedExams.length} 个考试吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const examIds = this.selectedExams.map(e => e.id)
          
          const response = await this.$axios.delete('/api/admin/exams/batch', {
            data: { examIds }
          })
          
          if (response.data.success) {
            this.exams = this.exams.filter(e => !examIds.includes(e.id))
            
            this.selectedExams = []
            this.searchExams() // 刷新搜索结果
            this.$message.success('批量删除成功')
          } else {
            this.$message.error(response.data.message || '批量删除失败')
          }
        } catch (error) {
          console.error('批量删除考试失败:', error)
          this.$message.error('批量删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量导入
    batchImport() {
      this.importDialogVisible = true
      this.importFileList = []
      this.importPreviewData = []
    },
    
    // 下载导入模板
    downloadTemplate() {
      // 实际项目中应该调用后端API下载模板
      this.$message.info('导入模板下载功能开发中')
    },
    
    // 处理文件上传
    handleFileChange(file) {
      this.importFileList = [file]
      // 读取文件并预览数据
      this.previewImportData(file)
    },
    
    // 预览导入数据
    previewImportData(file) {
      // 模拟解析Excel文件
      this.importPreviewData = [
        {
          name: '计算机网络期中考试',
          courseName: '计算机网络',
          examType: '期中考试',
          error: ''
        },
        {
          name: '数据结构期末考试',
          courseName: '数据结构',
          examType: '期末考试',
          error: ''
        },
        {
          name: '高等数学测验',
          courseName: '高等数学',
          examType: '测验',
          error: '考试名称已存在'
        }
      ]
    },
    
    // 确认导入
    async confirmImport() {
      try {
        // 实际项目中应该调用后端API导入数据
        // const formData = new FormData()
        // formData.append('file', this.importFileList[0].raw)
        // const response = await axios.post('/api/admin/exams/import', formData, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`,
        //     'Content-Type': 'multipart/form-data'
        //   }
        // })
        // this.exams.push(...response.data)
        
        // 模拟导入成功
        this.importDialogVisible = false
        this.loadData() // 重新加载数据
        this.$message.success('批量导入成功')
      } catch (error) {
        console.error('批量导入失败:', error)
        this.$message.error('批量导入失败，请稍后重试')
      }
    },
    
    // 导出考试数据
    exportExams() {
      // 实际项目中应该调用后端API导出数据
      this.$message.info('导出功能开发中')
    },
    
    // 查看考试详情
    viewExamDetails(exam) {
      this.currentExam = { ...exam }
      this.detailsDialogVisible = true
    },
    
    // 打印考试详情
    printExamDetails() {
      // 实际项目中应该实现打印功能
      window.print()
    },
    
    // 查看考试成绩
    async viewExamResults(exam) {
      this.currentExam = { ...exam }
      this.resultsDialogVisible = true
      this.resultsSearch = ''
      this.resultsStatus = ''
      this.resultsCurrentPage = 1
      
      try {
        const response = await this.$axios.get(`/api/exam-results/exam/${exam.id}`)
        if (response.data.success) {
          this.examResults = response.data.data
        } else {
          this.$message.error(response.data.message || '加载考试成绩失败')
        }
      } catch (error) {
        console.error('加载考试成绩失败:', error)
        this.$message.error('加载考试成绩失败，请稍后重试')
      }
    },
    
    // 处理成绩搜索
    handleResultsSearch() {
      this.resultsCurrentPage = 1
    },
    
    // 处理成绩选择变化
    handleResultsSelectionChange(selection) {
      this.selectedResults = selection
    },
    
    // 编辑成绩
    editScore(result) {
      this.currentScoreForm = {
        id: result.id,
        studentId: result.studentId,
        studentIdNumber: result.studentIdNumber,
        studentName: result.studentName,
        score: result.score,
        status: result.status,
        feedback: result.feedback || ''
      }
      this.editScoreDialogVisible = true
    },
    
    // 提交成绩表单
    submitScoreForm() {
      // 手动验证成绩是否为负数
      const score = this.currentScoreForm.score
      if (score < 0) {
        this.$message.error('考试成绩不能为负数')
        return
      }
      
      // 检查是否超过总分
      if (this.currentExam && score > this.currentExam.totalMarks) {
        this.$message.error(`考试成绩不能超过${this.currentExam.totalMarks}`)
        return
      }
      
      this.$refs.scoreForm.validate(async (valid) => {
        if (!valid) return
        
        try {
          const response = await this.$axios.put(`/api/exam-results/${this.currentScoreForm.id}`, this.currentScoreForm)
          if (response.data.success) {
            const index = this.examResults.findIndex(r => r.id === this.currentScoreForm.id)
            if (index !== -1) {
              this.examResults[index] = { ...this.currentScoreForm }
            }
            
            this.editScoreDialogVisible = false
            this.$message.success('成绩更新成功')
          } else {
            this.$message.error(response.data.message || '成绩更新失败')
          }
        } catch (error) {
          console.error('更新成绩失败:', error)
          this.$message.error('更新成绩失败，请稍后重试')
        }
      })
    },
    
    // 查看学生详情
    viewStudentProfile(result) {
      console.log('查看学生详情:', result)
      // 实际项目中应该跳转到学生详情页
      this.$router.push(`/admin/students`)
    },
    
    // 获取考试类型标签样式
    getExamTypeTag(type) {
      const tagMap = {
        'MIDTERM': 'warning',
        'FINAL': 'danger',
        'QUIZ': 'info',
        'LAB_REPORT': 'success',
        'PAPER': 'primary'
      }
      return tagMap[type] || 'info'
    },
    
    // 获取考试类型名称
    getExamTypeName(type) {
      const typeMap = {
        'MIDTERM': '期中考试',
        'FINAL': '期末考试',
        'QUIZ': '测验',
        'LAB_REPORT': '实验报告',
        'PAPER': '论文'
      }
      return typeMap[type] || type
    },
    
    // 获取状态标签样式
    getStatusTag(status) {
      const tagMap = {
        'NOT_STARTED': 'info',
        'IN_PROGRESS': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return tagMap[status] || 'info'
    },
    
    // 获取状态名称
    getStatusName(status) {
      const statusMap = {
        'NOT_STARTED': '未开始',
        'IN_PROGRESS': '进行中',
        'COMPLETED': '已结束',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },
    
    // 获取考试状态标签样式
    getExamStatusTag(status) {
      const tagMap = {
        'ATTENDED': 'success',
        'ABSENT': 'danger',
        'CHEATED': 'warning'
      }
      return tagMap[status] || 'info'
    },
    
    // 获取考试状态名称
    getExamStatusName(status) {
      const statusMap = {
        'ATTENDED': '已参加',
        'ABSENT': '缺考',
        'CHEATED': '作弊'
      }
      return statusMap[status] || status
    },
    
    // 获取成绩标签样式
    getScoreTag(score, totalMarks) {
      if (score === null) return 'info'
      const passMark = totalMarks * 0.6
      if (score >= passMark) return 'success'
      return 'danger'
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return ''
      const date = new Date(dateTimeString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
    
    handleResultsSizeChange(size) {
      this.resultsPageSize = size
      this.resultsCurrentPage = 1
    },
    
    handleResultsCurrentChange(current) {
      this.resultsCurrentPage = current
    }
  }
}
</script>

<style>
/* 增强禁用按钮的灰色样式 */
el-button.is-disabled,
el-button.is-disabled:hover,
el-button.is-disabled:focus {
  opacity: 0.6;
  filter: grayscale(100%);
  cursor: not-allowed;
}

/* 特别针对编辑按钮的禁用样式 */
el-button[type="primary"].is-disabled {
  background-color: #cccccc;
  border-color: #cccccc;
  color: #666666;
}
</style>

<style scoped>
.exam-management {
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

.management-content {
  border-radius: 8px;
}

.search-filters {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.action-buttons {
  display: flex;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.exam-table {
  margin-bottom: 20px;
}

.exam-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.empty-state {
  padding: 40px 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
}

.exam-details {
  padding: 10px 0;
}

.exam-title {
  margin-bottom: 20px;
  color: #333;
  font-size: 20px;
  text-align: center;
}

.description-content,
.notes-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #666;
}

.mt-4 {
  margin-top: 16px;
}

.exam-results {
  padding: 10px 0;
}

.results-header {
  margin-bottom: 20px;
}

.results-header h3 {
  margin-bottom: 10px;
  color: #333;
  font-size: 18px;
}

.results-stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.results-search {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
}

.results-table {
  margin-bottom: 20px;
}

.results-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.import-content {
  padding: 10px 0;
}

.import-hint {
  margin-bottom: 16px;
  color: #666;
}

.template-download {
  margin-bottom: 20px;
}

.import-upload {
  margin-bottom: 20px;
}

.import-preview h3 {
  margin-bottom: 10px;
  color: #333;
  font-size: 16px;
}

.error-text {
  color: #f56c6c;
}

.success-text {
  color: #67c23a;
}

.error-message {
  margin-top: 10px;
}

/* 表格样式 - 黑白配色 */
.exam-table :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.exam-table :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.exam-table :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
  font-weight: 600;
}

.exam-table :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.exam-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.exam-table :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.exam-table :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.exam-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.exam-table :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.exam-table :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.exam-table :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.exam-table :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

.exam-table :deep(.el-tag--success) {
  background-color: #f0f0f0;
}

.exam-table :deep(.el-tag--warning) {
  background-color: #f0f0f0;
}

.exam-table :deep(.el-tag--primary) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.exam-table :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.exam-table :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.exam-table :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.exam-table :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.exam-table :deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.exam-table :deep(.el-button--info) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.exam-table :deep(.el-button--warning) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-management {
    padding: 10px;
  }
  
  .search-filters,
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-input,
  .el-select,
  .el-button {
    width: 100% !important;
  }
  
  .pagination {
    justify-content: center;
  }
}
</style>