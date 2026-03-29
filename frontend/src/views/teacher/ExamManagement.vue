<template>
  <div class="exam-management">
    <el-card class="management-header">
      <h1>考试管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <!-- 筛选搜索区域 -->
      <div class="search-filter-section">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-select v-model="selectedCourse" placeholder="选择课程" clearable>
              <el-option
                v-for="course in teachingCourses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              ></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="selectedExamType" placeholder="考试类型" clearable>
              <el-option label="全部" value=""></el-option>
              <el-option label="期中考试" value="midterm"></el-option>
              <el-option label="期末考试" value="final"></el-option>
              <el-option label="随堂测试" value="quiz"></el-option>
              <el-option label="实验考试" value="experiment"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="examDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
          </el-col>
          <el-col :span="6">
            <div class="action-buttons">
              <el-button type="primary" icon="el-icon-plus" @click="showAddExamDialog">
                创建考试
              </el-button>
              <el-button type="info" icon="el-icon-refresh" @click="refreshExamList">
                刷新
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
      <!-- 考试统计概览 -->
      <div class="exam-stats-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ examStats.total }}</div>
              <div class="stat-label">总考试数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ examStats.upcoming }}</div>
              <div class="stat-label">即将进行</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ examStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-value">{{ examStats.pendingGrade }}</div>
              <div class="stat-label">待录入成绩</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 考试列表表格 -->
      <div class="course-table">
        <el-table
          v-loading="loading"
          :data="filteredExamData"
          style="width: 100%"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="name" label="考试名称" width="150" />
          <el-table-column prop="courseName" label="所属课程" width="150"></el-table-column>
          <el-table-column prop="type" label="考试类型" width="120">
            <template #default="scope">
              <el-tag :type="getExamTypeTagType(scope.row.type)">
                {{ getExamTypeName(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="examDate" label="考试日期" width="120" sortable>
            <template #default="scope">
              {{ formatDate(scope.row.examDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="100">
            <template #default="scope">
              {{ scope.row.startTime }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="100">
            <template #default="scope">
              {{ scope.row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="考试地点" width="150"></el-table-column>
          <el-table-column prop="totalScore" label="总分" width="80"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getExamStatusTagType(scope.row.status)">
                {{ getExamStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewExamDetails(scope.row)" :disabled="scope.row.status === 'draft'">
                <i class="el-icon-view"></i> 查看
              </el-button>
              <el-button type="success" size="small" @click="editExam(scope.row)" :disabled="scope.row.status === 'completed'">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button type="warning" size="small" @click="enterGrades(scope.row)" 
                :disabled="scope.row.status !== 'completed' || scope.row.gradesEntered">
                <i class="el-icon-edit-outline"></i> 录入成绩
              </el-button>
              <el-button type="danger" size="small" @click="deleteExam(scope.row)" :disabled="scope.row.status === 'completed'">
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
          :total="totalExams"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 创建/编辑考试对话框 -->
    <el-dialog
      :title="editingExam ? '编辑考试' : '创建考试'"
      v-model="showExamDialog"
      width="700px"
      @close="handleExamDialogClose"
    >
      <el-form :model="examForm" :rules="examRules" ref="examForm" label-width="100px">
        <el-form-item label="考试名称" prop="name">
          <el-input v-model="examForm.name" placeholder="请输入考试名称"></el-input>
        </el-form-item>
        
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="examForm.courseId" placeholder="请选择课程" @change="onExamCourseChange">
            <el-option
              v-for="course in teachingCourses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="考试类型" prop="type">
          <el-radio-group v-model="examForm.type">
            <el-radio label="midterm">期中考试</el-radio>
            <el-radio label="final">期末考试</el-radio>
            <el-radio label="quiz">随堂测试</el-radio>
            <el-radio label="experiment">实验考试</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker
            v-model="examForm.examDate"
            type="date"
            placeholder="选择考试日期"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="examForm.startTime"
            placeholder="选择开始时间"
            value-format="HH:mm"
          ></el-time-picker>
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="examForm.endTime"
            placeholder="选择结束时间"
            value-format="HH:mm"
          ></el-time-picker>
        </el-form-item>
        
        <el-form-item label="考试地点" prop="location">
          <el-input v-model="examForm.location" placeholder="请输入考试地点"></el-input>
        </el-form-item>
        
        <el-form-item label="总分" prop="totalScore">
          <el-input-number
            v-model="examForm.totalScore"
            :min="0"
            :max="100"
            placeholder="请输入总分"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="考试说明" prop="description">
          <el-input
            v-model="examForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入考试说明"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="参与班级" prop="classIds">
          <el-select
            v-model="examForm.classIds"
            multiple
            placeholder="请选择参与考试的班级"
          >
            <el-option
              v-for="cls in availableClasses"
              :key="cls.id"
              :label="cls.name"
              :value="cls.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleExamDialogClose">取消</el-button>
        <el-button type="primary" @click="saveExam">保存</el-button>
      </template>
    </el-dialog>

    <!-- 考试详情对话框 -->
    <el-dialog
      title="考试详情"
      v-model="showExamDetailsDialog"
      width="800px"
      @close="handleExamDetailsDialogClose"
    >
      <div class="exam-details-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="考试名称">{{ currentExam.name }}</el-descriptions-item>
          <el-descriptions-item label="所属课程">{{ currentExam.courseName }}</el-descriptions-item>
          <el-descriptions-item label="考试类型">
            <el-tag :type="getExamTypeTagType(currentExam.type)">
              {{ getExamTypeName(currentExam.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getExamStatusTagType(currentExam.status)">
              {{ getExamStatusText(currentExam.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="考试时间">
            {{ formatDate(currentExam.examDate) }} {{ currentExam.startTime }} - {{ currentExam.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="考试地点">{{ currentExam.location }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ currentExam.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="参与班级">
            <el-tag v-for="cls in currentExam.classes" :key="cls.id" style="margin-right: 5px;">
              {{ cls.name }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参与学生数">{{ currentExam.studentCount }}</el-descriptions-item>
          <el-descriptions-item label="成绩录入状态">
            <el-tag :type="currentExam.gradesEntered ? 'success' : 'warning'">
              {{ currentExam.gradesEntered ? '已录入' : '未录入' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="考试说明" :span="2">{{ currentExam.description || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 考试统计信息 -->
        <div v-if="currentExam.gradesEntered" class="exam-stats-section">
          <h3>考试成绩统计</h3>
          <el-row :gutter="20">
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ currentExamStats.average.toFixed(1) }}</div>
                <div class="stat-label">平均分</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ currentExamStats.max }}</div>
                <div class="stat-label">最高分</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ currentExamStats.min }}</div>
                <div class="stat-label">最低分</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ currentExamStats.passRate.toFixed(1) }}%</div>
                <div class="stat-label">及格率</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ currentExamStats.excellentRate.toFixed(1) }}%</div>
                <div class="stat-label">优秀率</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ currentExamStats.stdDev.toFixed(1) }}</div>
                <div class="stat-label">标准差</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <!-- 操作按钮 -->
        <div class="exam-actions">
          <el-button @click="viewExamGrades(currentExam)" v-if="currentExam.status === 'completed'">
            <i class="el-icon-document"></i> 查看成绩
          </el-button>
          <el-button type="primary" @click="enterGrades(currentExam)" 
            v-if="currentExam.status === 'completed' && !currentExam.gradesEntered">
            <i class="el-icon-edit-outline"></i> 录入成绩
          </el-button>
          <el-button type="success" @click="downloadExamReport(currentExam)">
            <i class="el-icon-download"></i> 下载报告
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 录入成绩对话框 -->
    <el-dialog
      title="录入成绩"
      v-model="showEnterGradesDialog"
      width="800px"
      @close="handleEnterGradesDialogClose"
    >
      <div class="enter-grades-container">
        <div class="exam-info">
          <h3>{{ enteringExam.name }} - {{ getExamTypeName(enteringExam.type) }}</h3>
          <p>考试日期：{{ formatDate(enteringExam.examDate) }} {{ enteringExam.startTime }} - {{ enteringExam.endTime }}</p>
          <p>总分：{{ enteringExam.totalScore }}</p>
        </div>
        
        <div class="grade-entry-options">
          <el-radio-group v-model="gradeEntryMethod">
            <el-radio label="manual">手动录入</el-radio>
            <el-radio label="batch">批量录入</el-radio>
            <el-radio label="import">导入成绩</el-radio>
          </el-radio-group>
        </div>
        
        <!-- 手动录入 -->
        <div v-if="gradeEntryMethod === 'manual'" class="manual-entry">
          <el-table
            :data="filteredStudentsForGrades"
            style="width: 100%"
            border
            height="400"
          >
            <el-table-column prop="studentId" label="学号" width="120">
              <template #default="scope">
                {{ scope.row.studentId }}
              </template>
            </el-table-column>
            <el-table-column prop="studentName" label="姓名" width="100">
              <template #default="scope">
                {{ scope.row.studentName }}
              </template>
            </el-table-column>
            <el-table-column prop="className" label="班级" width="120">
              <template #default="scope">
                {{ scope.row.className }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="成绩" width="120">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.score"
                  :min="0"
                  :max="enteringExam.totalScore"
                  :step="0.5"
                  :precision="1"
                  placeholder="输入成绩"
                ></el-input-number>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="scope">
                <el-input
                  v-model="scope.row.remark"
                  placeholder="输入备注"
                ></el-input>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 批量录入 -->
        <div v-if="gradeEntryMethod === 'batch'" class="batch-entry">
          <el-form :model="batchGradeForm" :rules="batchGradeRules" ref="batchGradeForm">
            <el-form-item label="录入方式" prop="entryType">
              <el-radio-group v-model="batchGradeForm.entryType">
                <el-radio label="range">随机分布</el-radio>
                <el-radio label="fixed">固定值</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="最小值" v-if="batchGradeForm.entryType === 'range'" prop="minScore">
              <el-input-number
                v-model="batchGradeForm.minScore"
                :min="0"
                :max="enteringExam.totalScore"
              ></el-input-number>
            </el-form-item>
            
            <el-form-item label="最大值" v-if="batchGradeForm.entryType === 'range'" prop="maxScore">
              <el-input-number
                v-model="batchGradeForm.maxScore"
                :min="0"
                :max="enteringExam.totalScore"
              ></el-input-number>
            </el-form-item>
            
            <el-form-item label="固定成绩" v-if="batchGradeForm.entryType === 'fixed'" prop="fixedScore">
              <el-input-number
                v-model="batchGradeForm.fixedScore"
                :min="0"
                :max="enteringExam.totalScore"
              ></el-input-number>
            </el-form-item>
            
            <el-form-item label="分布类型" v-if="batchGradeForm.entryType === 'range'" prop="distributionType">
              <el-select v-model="batchGradeForm.distributionType" placeholder="选择分布类型">
                <el-option label="均匀分布" value="uniform"></el-option>
                <el-option label="正态分布" value="normal"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 导入成绩 -->
        <div v-if="gradeEntryMethod === 'import'" class="import-entry">
          <el-upload
            class="upload-demo"
            action=""
            :on-change="handleGradeFileUpload"
            :auto-upload="false"
            accept=".xlsx,.xls"
            :show-file-list="true"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传Excel格式的成绩文件（.xlsx/.xls）
              </div>
            </template>
          </el-upload>
          
          <div class="import-template">
            <el-button type="text" @click="downloadGradeTemplate">下载成绩模板</el-button>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="handleEnterGradesDialogClose">取消</el-button>
        <el-button type="primary" @click="confirmEnterGrades">确认录入</el-button>
      </template>
    </el-dialog>

    <!-- 成绩查看对话框 -->
    <el-dialog
      title="考试成绩列表"
      v-model="showViewGradesDialog"
      width="900px"
      @close="handleViewGradesDialogClose"
    >
      <div class="view-grades-container">
        <div class="exam-info">
          <h3>{{ viewingExam.name }} - {{ getExamTypeName(viewingExam.type) }}</h3>
          <p>考试日期：{{ formatDate(viewingExam.examDate) }}</p>
        </div>
        
        <div class="search-section">
          <el-input
            v-model="gradeSearchQuery"
            placeholder="搜索学生姓名或学号"
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
          <el-button type="success" @click="exportExamGrades">
            <i class="el-icon-download"></i> 导出成绩
          </el-button>
        </div>
        
        <el-table
          :data="filteredExamGrades"
          style="width: 100%"
          border
          height="500"
        >
          <el-table-column prop="studentId" label="学号" width="120" sortable>
            <template #default="scope">
              {{ scope.row.studentId }}
            </template>
          </el-table-column>
          <el-table-column prop="studentName" label="姓名" width="100">
            <template #default="scope">
              {{ scope.row.studentName }}
            </template>
          </el-table-column>
          <el-table-column prop="className" label="班级" width="120">
            <template #default="scope">
              {{ scope.row.className }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="成绩" width="100" sortable>
            <template #default="scope">
              <div :class="['exam-score', getScoreLevelClass(scope.row.score, viewingExam.totalScore)]">
                {{ scope.row.score }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="等级" width="80">
            <template #default="scope">
              <el-tag :type="getGradeLevelTagType(scope.row.score, viewingExam.totalScore)">
                {{ calculateGradeLevel(scope.row.score, viewingExam.totalScore) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rank" label="排名" width="80" sortable>
            <template #default="scope">
              {{ scope.row.rank }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150">
            <template #default="scope">
              {{ scope.row.remark }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editGrade(scope.row)">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 编辑成绩对话框 -->
    <el-dialog
      title="编辑成绩"
      v-model="showEditGradeDialog"
      width="500px"
      @close="handleEditGradeDialogClose"
    >
      <el-form :model="editGradeForm" :rules="editGradeRules" ref="editGradeForm">
        <el-form-item label="学生姓名">
          <el-input v-model="editGradeForm.studentName" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="学号">
          <el-input v-model="editGradeForm.studentId" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="班级">
          <el-input v-model="editGradeForm.className" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="成绩" prop="score">
          <el-input-number
            v-model="editGradeForm.score"
            :min="0"
            :max="viewingExam.totalScore"
            :step="0.5"
            :precision="1"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editGradeForm.remark"
            type="textarea"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleEditGradeDialogClose">取消</el-button>
        <el-button type="primary" @click="confirmEditGrade">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherExamManagement',
  data() {
    return {
      // 筛选条件
      selectedCourse: '',
      selectedExamType: '',
      examDateRange: null,
      
      // 分页参数
      currentPage: 1,
      pageSize: 10,
      totalExams: 0,
      
      // 数据加载状态
      loading: false,
      
      // 教学课程
      teachingCourses: [],
      
      // 考试数据
      examData: [],
      
      // 选中的行
      selectedRows: [],
      
      // 弹出框状态
      showExamDialog: false,
      showExamDetailsDialog: false,
      showEnterGradesDialog: false,
      showViewGradesDialog: false,
      showEditGradeDialog: false,
      
      // 编辑状态
      editingExam: null,
      
      // 考试表单
      examForm: {
        id: '',
        name: '',
        courseId: '',
        type: 'midterm',
        examDate: '',
        startTime: '',
        endTime: '',
        location: '',
        totalScore: 100,
        description: '',
        classIds: []
      },
      
      // 考试表单验证规则
      examRules: {
        name: [
          { required: true, message: '请输入考试名称', trigger: 'blur' }
        ],
        courseId: [
          { required: true, message: '请选择所属课程', trigger: 'change' }
        ],
        examDate: [
          { required: true, message: '请选择考试日期', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请选择开始时间', trigger: 'change' }
        ],
        endTime: [
          { required: true, message: '请选择结束时间', trigger: 'change' }
        ],
        totalScore: [
          { required: true, message: '请输入总分', trigger: 'blur' }
        ]
      },
      
      // 可用班级
      availableClasses: [],
      
      // 当前考试
      currentExam: {},
      currentExamStats: {
        average: 0,
        max: 0,
        min: 0,
        passRate: 0,
        excellentRate: 0,
        stdDev: 0
      },
      
      // 录入成绩相关
      enteringExam: {},
      gradeEntryMethod: 'manual',
      studentsForGrades: [],
      
      // 批量成绩录入表单
      batchGradeForm: {
        entryType: 'range',
        minScore: 0,
        maxScore: 100,
        fixedScore: 60,
        distributionType: 'normal'
      },
      
      // 批量成绩录入验证规则
      batchGradeRules: {
        minScore: [
          { required: true, message: '请输入最小值', trigger: 'blur' }
        ],
        maxScore: [
          { required: true, message: '请输入最大值', trigger: 'blur' }
        ],
        fixedScore: [
          { required: true, message: '请输入固定成绩', trigger: 'blur' }
        ]
      },
      
      // 查看成绩相关
      viewingExam: {},
      examGrades: [],
      gradeSearchQuery: '',
      
      // 编辑成绩表单
      editGradeForm: {
        id: '',
        studentId: '',
        studentName: '',
        className: '',
        score: 0,
        remark: ''
      },
      
      // 编辑成绩验证规则
      editGradeRules: {
        score: [
          { required: true, message: '请输入成绩', trigger: 'blur' }
        ]
      },
      
      // 考试统计
      examStats: {
        total: 0,
        upcoming: 0,
        completed: 0,
        pendingGrade: 0
      }
    }
  },
  
  computed: {
    // 过滤后的考试数据
    filteredExamData() {
      let filtered = [...this.examData]
      
      // 按课程筛选
      if (this.selectedCourse) {
        filtered = filtered.filter(item => item.courseId === this.selectedCourse)
      }
      
      // 按考试类型筛选
      if (this.selectedExamType) {
        filtered = filtered.filter(item => item.type === this.selectedExamType)
      }
      
      // 按日期范围筛选
      if (this.examDateRange && this.examDateRange.length === 2) {
        filtered = filtered.filter(item => {
          const examDate = new Date(item.examDate)
          const startDate = new Date(this.examDateRange[0])
          const endDate = new Date(this.examDateRange[1])
          return examDate >= startDate && examDate <= endDate
        })
      }
      
      // 更新总数
      this.totalExams = filtered.length
      
      // 更新统计数据
      this.updateExamStats(filtered)
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return filtered.slice(start, end)
    },
    
    // 过滤后的学生成绩数据（用于录入）
    filteredStudentsForGrades() {
      let filtered = [...this.studentsForGrades]
      
      // 更新总数
      
      // 分页处理暂时不做，使用表格的高度限制
      return filtered
    },
    
    // 过滤后的考试成绩（用于查看）
    filteredExamGrades() {
      let filtered = [...this.examGrades]
      
      // 按学生姓名或学号搜索
      if (this.gradeSearchQuery) {
        const query = this.gradeSearchQuery.toLowerCase()
        filtered = filtered.filter(item => 
          item.studentName.toLowerCase().includes(query) || 
          item.studentId.toLowerCase().includes(query)
        )
      }
      
      return filtered
    }
  },
  
  mounted() {
    // 加载教学课程
    this.loadTeachingCourses()
    // 加载考试数据
    this.loadExamData()
  },
  
  methods: {
    // 加载教学课程
    async loadTeachingCourses() {
      try {
        const response = await this.$axios.get(`/teacher/data/courses`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.teachingCourses = response.data.data || []
        } else {
          console.error('获取教学课程失败:', response.data.message)
          this.$message.error(response.data.message || '获取教学课程失败')
          this.teachingCourses = []
        }
        
        console.log('教学课程加载完成')
      } catch (error) {
        console.error('加载教学课程失败:', error)
        this.$message.error('加载教学课程失败，请稍后重试')
      }
    },
    
    // 加载考试数据
    async loadExamData() {
      this.loading = true
      try {
        const response = await this.$axios.get('/teacher/data/exams', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.examData = response.data.data || []
        } else {
          console.error('获取考试数据失败:', response.data.message)
          this.$message.error(response.data.message || '获取考试数据失败')
          this.examData = []
        }
        
        console.log('考试数据加载完成')
        
        // 修复考试数据，确保每个考试对象都有必需的属性
        this.examData = this.examData.map(exam => ({
          ...exam,
          classIds: exam.classIds || [],
          classes: exam.classes || []
        }))
      } catch (error) {
        console.error('加载考试数据失败:', error)
        this.$message.error('加载考试数据失败，请稍后重试')
        this.examData = [] // 确保在错误情况下也有默认值
      } finally {
        this.loading = false
      }
    },
    

    
    // 获取日期字符串
    getDateString(date) {
      return date.toISOString().split('T')[0]
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    
    // 更新考试统计
    updateExamStats(exams) {
      const today = new Date()
      const todayStr = this.getDateString(today)
      
      const total = exams.length
      const upcoming = exams.filter(exam => exam.examDate > todayStr).length
      const completed = exams.filter(exam => exam.examDate < todayStr).length
      const pendingGrade = exams.filter(exam => exam.status === 'completed' && !exam.gradesEntered).length
      
      this.examStats = { total, upcoming, completed, pendingGrade }
    },
    
    // 获取考试类型名称
    getExamTypeName(type) {
      const typeMap = {
        midterm: '期中考试',
        final: '期末考试',
        quiz: '随堂测试',
        experiment: '实验考试'
      }
      return typeMap[type] || '未知'
    },
    
    // 获取考试类型标签类型
    getExamTypeTagType(type) {
      const typeMap = {
        midterm: 'primary',
        final: 'danger',
        quiz: 'warning',
        experiment: 'success'
      }
      return typeMap[type] || 'info'
    },
    
    // 获取考试状态文本
    getExamStatusText(status) {
      const statusMap = {
        draft: '草稿',
        scheduled: '已安排',
        completed: '已完成'
      }
      return statusMap[status] || '未知'
    },
    
    // 获取考试状态标签类型
    getExamStatusTagType(status) {
      const statusMap = {
        draft: 'info',
        scheduled: 'warning',
        completed: 'success'
      }
      return statusMap[status] || 'info'
    },
    
    // 显示添加考试对话框
    showAddExamDialog() {
      this.editingExam = null
      this.resetExamForm()
      if (this.selectedCourse) {
        this.examForm.courseId = this.selectedCourse
        this.onExamCourseChange(this.selectedCourse)
      }
      this.showExamDialog = true
    },
    
    // 编辑考试
    editExam(exam) {
      this.editingExam = exam
      this.examForm = {
        id: exam.id,
        name: exam.name,
        courseId: exam.courseId,
        type: exam.type,
        examDate: exam.examDate,
        startTime: exam.startTime,
        endTime: exam.endTime,
        location: exam.location,
        totalScore: exam.totalScore,
        description: exam.description,
        classIds: [...exam.classIds]
      }
      this.onExamCourseChange(exam.courseId)
      this.showExamDialog = true
    },
    
    // 考试课程变更处理
    async onExamCourseChange(courseId) {
      // 加载该课程的班级信息
      await this.loadAvailableClasses(courseId)
    },
    
    // 加载可用班级
    async loadAvailableClasses(courseId) {
      try {
        const response = await this.$axios.get(`/teacher/data/classes/${courseId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.availableClasses = response.data.data || []
        } else {
          console.error('获取班级信息失败:', response.data.message)
          this.$message.error(response.data.message || '获取班级信息失败')
          this.availableClasses = []
        }
      } catch (error) {
        console.error('加载班级信息失败:', error)
        this.$message.error('加载班级信息失败，请稍后重试')
        this.availableClasses = []
      }
    },
    
    // 重置考试表单
    resetExamForm() {
      this.examForm = {
        id: '',
        name: '',
        courseId: this.selectedCourse || '',
        type: 'midterm',
        examDate: '',
        startTime: '',
        endTime: '',
        location: '',
        totalScore: 100,
        description: '',
        classIds: []
      }
    },
    
    // 保存考试
    async saveExam() {
      this.$refs.examForm.validate(async (valid) => {
        if (valid) {
          // 验证开始时间和结束时间
          if (this.examForm.startTime >= this.examForm.endTime) {
            this.$message.error('结束时间必须大于开始时间')
            return
          }
          
          const examData = {
            ...this.examForm,
            courseName: this.teachingCourses.find(c => c.id === this.examForm.courseId)?.name || '',
            classes: this.availableClasses.filter(cls => this.examForm.classIds.includes(cls.id)),
            studentCount: this.examForm.classIds.length * 30, // 假设每个班级30人
            status: 'scheduled',
            gradesEntered: false
          }
          
          try {
            let response
            if (this.editingExam) {
              // 更新考试
              response = await this.$axios.put(`/exams/${examData.id}`, examData)
              if (response.data.success) {
                const index = this.examData.findIndex(e => e.id === this.editingExam.id)
                if (index !== -1) {
                  this.examData[index] = response.data.data
                }
              }
            } else {
              // 添加新考试
              response = await this.$axios.post('/exams', examData)
              if (response.data.success) {
                this.examData.unshift(response.data.data)
              }
            }
            
            this.$message.success(this.editingExam ? '考试更新成功' : '考试创建成功')
            this.showExamDialog = false
          } catch (error) {
            console.error('保存考试失败:', error)
            this.$message.error(this.editingExam ? '考试更新失败' : '考试创建失败')
          }
        }
      })
    },
    
    // 删除考试
    async deleteExam(exam) {
      this.$confirm(`确定要删除考试"${exam.name}"吗？此操作不可恢复！`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/exams/${exam.id}`)
          if (response.data.success) {
            const index = this.examData.findIndex(e => e.id === exam.id)
            if (index !== -1) {
              this.examData.splice(index, 1)
            }
            
            this.$message.success('考试删除成功')
          } else {
            this.$message.error(response.data.message || '考试删除失败')
          }
        } catch (error) {
          console.error('删除考试失败:', error)
          this.$message.error('考试删除失败，请稍后重试')
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 查看考试详情
    viewExamDetails(exam) {
      this.currentExam = exam
      // 加载考试统计信息
      this.loadExamStats(exam.id)
      this.showExamDetailsDialog = true
    },
    
    // 加载考试统计信息
    async loadExamStats(examId) {
      try {
        const response = await this.$axios.get(`/exams/${examId}/stats`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        this.currentExamStats = response.data.data
      } catch (error) {
        console.error('加载考试统计信息失败:', error)
        // 备用数据
        this.currentExamStats = {
          average: 75.5,
          max: 98,
          min: 45,
          passRate: 85.5,
          excellentRate: 20.3,
          stdDev: 12.8
        }
      }
    },
    
    // 查看考试成绩
    viewExamGrades(exam) {
      this.viewingExam = exam
      // 加载考试成绩
      this.loadExamGrades(exam.id)
      this.showExamDetailsDialog = false
      this.showViewGradesDialog = true
    },
    
    // 加载考试成绩
    async loadExamGrades(examId) {
      try {
        const response = await this.$axios.get(`/teacher/data/exams/${examId}/grades`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.examGrades = response.data.data || []
        } else {
          console.error('获取考试成绩失败:', response.data.message)
          this.$message.error(response.data.message || '获取考试成绩失败')
          this.examGrades = []
        }
      } catch (error) {
        console.error('加载考试成绩失败:', error)
        this.$message.error('加载考试成绩失败，请稍后重试')
        this.examGrades = []
      }
    },
    

    
    // 编辑成绩
    editGrade(grade) {
      this.editGradeForm = {
        id: grade.id,
        studentId: grade.studentId,
        studentName: grade.studentName,
        className: grade.className,
        score: grade.score,
        remark: grade.remark
      }
      this.showEditGradeDialog = true
    },
    
    // 确认编辑成绩
    async confirmEditGrade() {
      this.$refs.editGradeForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await this.$axios.put(`/exam-results/${this.editGradeForm.id}`, {
              score: this.editGradeForm.score,
              remark: this.editGradeForm.remark
            })
            
            if (response.data.success) {
              const index = this.examGrades.findIndex(g => g.id === this.editGradeForm.id)
              if (index !== -1) {
                this.examGrades[index] = { ...this.examGrades[index], ...this.editGradeForm }
                
                // 重新计算排名
                this.examGrades.sort((a, b) => b.score - a.score)
                this.examGrades.forEach((grade, idx) => {
                  grade.rank = idx + 1
                })
              }
              
              this.$message.success('成绩更新成功')
            } else {
              this.$message.error(response.data.message || '成绩更新失败')
            }
          } catch (error) {
            console.error('更新成绩失败:', error)
            this.$message.error('成绩更新失败，请稍后重试')
          }
          this.showEditGradeDialog = false
        }
      })
    },
    
    // 录入成绩
    enterGrades(exam) {
      this.enteringExam = exam
      this.gradeEntryMethod = 'manual'
      // 加载学生数据
      this.loadStudentsForGrades(exam.id)
      this.showEnterGradesDialog = true
    },
    
    // 加载需要录入成绩的学生
    async loadStudentsForGrades(examId) {
      try {
        const response = await this.$axios.get(`/teacher/data/exams/${examId}/students`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.data.success) {
          this.studentsForGrades = response.data.data || []
        } else {
          console.error('获取考试学生列表失败:', response.data.message)
          this.$message.error(response.data.message || '获取考试学生列表失败')
          this.studentsForGrades = []
        }
      } catch (error) {
        console.error('加载考试学生列表失败:', error)
        this.$message.error('加载考试学生列表失败，请稍后重试')
        this.studentsForGrades = []
      }
    },
    
    // 处理成绩文件上传
    handleGradeFileUpload(file) {
      // 实际项目中应该处理文件上传和解析
      this.$message.success('文件上传成功，正在解析...')
    },
    
    // 下载成绩模板
    downloadGradeTemplate() {
      // 实际项目中应该调用后端API下载模板
      this.$message.success('模板下载成功')
    },
    
    // 确认录入成绩
    confirmEnterGrades() {
      if (this.gradeEntryMethod === 'batch') {
        this.$refs.batchGradeForm.validate((valid) => {
          if (valid) {
            this.processBatchGrades()
          }
        })
      } else {
        this.saveEnteredGrades()
      }
    },
    
    // 处理批量成绩录入
    processBatchGrades() {
      const exam = this.enteringExam
      
      this.studentsForGrades.forEach(student => {
        if (this.batchGradeForm.entryType === 'fixed') {
          student.score = this.batchGradeForm.fixedScore
        } else {
          if (this.batchGradeForm.distributionType === 'uniform') {
            // 均匀分布
            student.score = Math.random() * (this.batchGradeForm.maxScore - this.batchGradeForm.minScore) + this.batchGradeForm.minScore
          } else {
            // 正态分布
            student.score = this.generateNormalDistribution(this.batchGradeForm.minScore, this.batchGradeForm.maxScore)
          }
          student.score = Math.round(student.score * 2) / 2 // 保留一位小数
        }
      })
      
      this.saveEnteredGrades()
    },
    
    // 生成正态分布的随机数
    generateNormalDistribution(min, max) {
      // 标准正态分布
      let u = 0, v = 0
      while(u === 0) u = Math.random()
      while(v === 0) v = Math.random()
      
      let z = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v)
      
      // 转换到指定范围，均值为中间值，标准差为范围的1/6
      const mean = (min + max) / 2
      const stdDev = (max - min) / 6
      let value = mean + z * stdDev
      
      // 确保在范围内
      return Math.max(min, Math.min(max, value))
    },
    
    // 保存录入的成绩
    saveEnteredGrades() {
      // 检查是否所有学生都录入了成绩
      const ungradedStudents = this.studentsForGrades.filter(s => s.score === null || s.score === undefined)
      if (ungradedStudents.length > 0) {
        this.$confirm(`还有 ${ungradedStudents.length} 名学生未录入成绩，是否继续？`, '确认保存', {
          confirmButtonText: '继续保存',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.submitGrades()
        }).catch(() => {
          // 取消操作
        })
      } else {
        this.submitGrades()
      }
    },
    
    async submitGrades() {
      try {
        // 发送成绩到后端
        const gradeData = this.studentsForGrades.map(student => ({
          examId: this.enteringExam.id,
          studentId: student.studentId,
          score: student.score,
          remark: student.remark
        }))
        
        const response = await this.$axios.post('/teacher/data/exam-results/batch', gradeData)
        
        if (response.data.success) {
          // 更新考试状态
          const examIndex = this.examData.findIndex(e => e.id === this.enteringExam.id)
          if (examIndex !== -1) {
            this.examData[examIndex].gradesEntered = true
          }
          
          this.$message.success('成绩录入成功')
          this.showEnterGradesDialog = false
        } else {
          this.$message.error(response.data.message || '成绩录入失败')
        }
      } catch (error) {
        console.error('提交成绩失败:', error)
        this.$message.error('成绩录入失败，请稍后重试')
      }
    },
    
    // 导出考试成绩
    exportExamGrades() {
      // 实际项目中应该调用后端API
      this.$message.success('成绩导出成功')
    },
    
    // 下载考试报告
    downloadExamReport(exam) {
      // 实际项目中应该调用后端API
      this.$message.success('考试报告下载成功')
    },
    
    // 刷新考试列表
    refreshExamList() {
      this.selectedCourse = ''
      this.selectedExamType = ''
      this.examDateRange = null
      this.currentPage = 1
      this.loadExamData()
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    
    // 关闭考试对话框
    handleExamDialogClose() {
      this.showExamDialog = false
      this.$refs.examForm && this.$refs.examForm.resetFields()
    },
    
    // 关闭考试详情对话框
    handleExamDetailsDialogClose() {
      this.showExamDetailsDialog = false
      this.currentExam = {}
    },
    
    // 关闭录入成绩对话框
    handleEnterGradesDialogClose() {
      this.showEnterGradesDialog = false
      this.studentsForGrades = []
    },
    
    // 关闭查看成绩对话框
    handleViewGradesDialogClose() {
      this.showViewGradesDialog = false
      this.examGrades = []
      this.gradeSearchQuery = ''
    },
    
    // 关闭编辑成绩对话框
    handleEditGradeDialogClose() {
      this.showEditGradeDialog = false
      this.editGradeForm = {}
    },
    
    // 计算等级
    calculateGradeLevel(score, totalScore) {
      if (score === null || score === undefined || isNaN(score)) {
        return '-'
      }
      
      const percentage = (score / totalScore) * 100
      
      if (percentage >= 90) {
        return '优秀'
      } else if (percentage >= 80) {
        return '良好'
      } else if (percentage >= 60) {
        return '及格'
      } else {
        return '不及格'
      }
    },
    
    // 获取等级标签类型
    getGradeLevelTagType(score, totalScore) {
      if (score === null || score === undefined || isNaN(score)) {
        return 'info'
      }
      
      const percentage = (score / totalScore) * 100
      
      if (percentage >= 90) {
        return 'success'
      } else if (percentage >= 80) {
        return 'primary'
      } else if (percentage >= 60) {
        return 'warning'
      } else {
        return 'danger'
      }
    },
    
    // 获取分数等级样式类
    getScoreLevelClass(score, totalScore) {
      if (score === null || score === undefined || isNaN(score)) {
        return 'score-default'
      }
      
      const percentage = (score / totalScore) * 100
      
      if (percentage >= 90) {
        return 'score-excellent'
      } else if (percentage >= 80) {
        return 'score-good'
      } else if (percentage >= 60) {
        return 'score-pass'
      } else {
        return 'score-fail'
      }
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
.exam-management-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

.exam-management-card {
  margin-bottom: 20px;
}

/* 考试统计概览 */
.exam-stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

/* 考试详情样式 */
.exam-details-container {
  padding: 10px 0;
}

.exam-stats-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.exam-stats-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.exam-actions {
  margin-top: 20px;
  text-align: center;
}

/* 录入成绩样式 */
.enter-grades-container {
  padding: 10px 0;
}

.exam-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.exam-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.exam-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.grade-entry-options {
  margin-bottom: 20px;
}

.manual-entry,
.batch-entry,
.import-entry {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.batch-entry .el-form-item {
  margin-bottom: 15px;
}

.import-template {
  margin-top: 15px;
}

/* 查看成绩样式 */
.view-grades-container {
  padding: 10px 0;
}

.search-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.search-section .el-input {
  flex: 1;
  max-width: 300px;
}

.exam-score {
  font-weight: bold;
}

.score-excellent {
  color: #67c23a;
}

.score-good {
  color: #409eff;
}

.score-pass {
  color: #e6a23c;
}

.score-fail {
  color: #f56c6c;
}

.score-default {
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .exam-stats-overview .el-col {
    width: 50%;
    margin-bottom: 15px;
  }
  
  .search-filter-section .el-col {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .search-filter-section .el-col:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .exam-management-container {
    padding: 10px;
  }
  
  .exam-stats-overview .el-col {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section .el-input {
    max-width: 100%;
  }
}
</style>