<template>
  <div class="grade-management">
    <el-card class="management-header">
      <h1>成绩管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <div class="search-filters">
        <el-input 
          v-model="searchForm.keyword" 
          placeholder="输入学生学号/姓名/课程名称搜索" 
          prefix-icon="el-icon-search" 
          style="width: 300px; margin-right: 12px;"
          @keyup.enter="searchGrades"
        />
        
        <el-select 
          v-model="searchForm.courseId" 
          placeholder="选择课程" 
          clearable 
          style="width: 180px; margin-right: 12px;"
          filterable
        >
          <el-option 
            v-for="course in courses" 
            :key="course.id" 
            :label="course.name" 
            :value="course.id" 
          />
        </el-select>
        
        <el-select 
          v-model="searchForm.examId" 
          placeholder="选择考试" 
          clearable 
          style="width: 200px; margin-right: 12px;"
          filterable
        >
          <el-option 
            v-for="exam in exams" 
            :key="exam.id" 
            :label="exam.name" 
            :value="exam.id" 
          />
        </el-select>
        
        <el-select 
          v-model="searchForm.departmentId" 
          placeholder="选择院系" 
          clearable 
          style="width: 150px; margin-right: 12px;"
        >
          <el-option 
            v-for="dept in departments" 
            :key="dept.id" 
            :label="dept.name" 
            :value="dept.id" 
          />
        </el-select>
        
        <el-select 
          v-model="searchForm.gradeStatus" 
          placeholder="成绩状态" 
          clearable 
          style="width: 120px; margin-right: 12px;"
        >
          <el-option label="已录入" value="ENTERED" />
          <el-option label="待录入" value="PENDING" />
          <el-option label="未参加" value="NOT_ATTENDED" />
        </el-select>
        
        <el-button type="primary" @click="searchGrades">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="enterGrades">
          <i class="el-icon-edit"></i> 录入成绩
        </el-button>
        <el-button type="success" @click="batchImport">
          <i class="el-icon-upload"></i> 批量导入
        </el-button>
        <el-button type="info" @click="exportGrades" :disabled="selectedGrades.length === 0">
          <i class="el-icon-download"></i> 导出选中
        </el-button>
        <el-button type="warning" @click="viewGradeAnalysis">
          <i class="el-icon-data-analysis"></i> 成绩分析
        </el-button>
        <el-button type="danger" @click="batchDelete" :disabled="selectedGrades.length === 0">
          <i class="el-icon-delete"></i> 批量删除
        </el-button>
      </div>
      
      <div class="grade-table">
        <el-table 
          v-loading="loading" 
          :data="paginatedGrades" 
          style="width: 100%"
          @selection-change="handleSelectionChange"
          @row-dblclick="handleRowDblclick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="studentIdNumber" label="学号" width="100" />
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
          <el-table-column prop="courseName" label="课程名称" width="150" />
          <el-table-column prop="examName" label="考试名称" width="180" />
          <el-table-column prop="score" label="得分" width="80" :sortable="true">
            <template #default="scope">
              <el-tag :type="getScoreTag(scope.row.score, scope.row.totalMarks)">
                {{ scope.row.score !== null ? scope.row.score : '--' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalMarks" label="总分" width="80" />
          <el-table-column prop="percentage" label="百分比" width="80" :sortable="true">
            <template #default="scope">
              <span v-if="scope.row.percentage !== null && scope.row.percentage !== undefined">{{ scope.row.percentage.toFixed(1) }}%</span>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="等级" width="80">
            <template #default="scope">
              <el-tag 
                v-if="scope.row.grade" 
                :type="getGradeTag(scope.row.grade)"
              >
                {{ scope.row.grade }}
              </el-tag>
              <span v-else>--</span>
            </template>
          </el-table-column>
          <el-table-column prop="department" label="院系" width="100" />
          <el-table-column prop="examDate" label="考试日期" width="140">
            <template #default="scope">
              {{ formatDate(scope.row.examDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="entryDate" label="录入日期" width="140">
            <template #default="scope">
              {{ formatDateTime(scope.row.entryDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="entryBy" label="录入人" width="100" />
          <el-table-column label="操作" width="350" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="editGrade(scope.row)"
                icon="el-icon-edit"
                style="margin-right: 5px;"
              >
                编辑
              </el-button>
              <el-button 
                type="info" 
                size="small" 
                @click="viewGradeDetails(scope.row)"
                icon="el-icon-view"
                style="margin-right: 5px;"
              >
                详情
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteGrade(scope.row)"
                icon="el-icon-delete"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination" v-if="filteredGrades.length > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, sizes, total"
          :total="filteredGrades.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
      
      <div v-if="filteredGrades.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无成绩数据"></el-empty>
      </div>
    </el-card>
    
    <!-- 录入成绩对话框 -->
    <el-dialog 
      title="批量录入成绩" 
      v-model="enterGradesDialogVisible" 
      width="80%"
      fullscreen
    >
      <div class="enter-grades-content">
        <div class="select-exam">
          <el-form label-width="80px">
            <el-form-item label="选择考试">
              <el-select 
                v-model="selectedExamId" 
                placeholder="请选择要录入成绩的考试" 
                filterable
                @change="loadExamStudents"
              >
                <el-option 
                  v-for="exam in pendingExams" 
                  :key="exam.id" 
                  :label="`${exam.courseName} - ${exam.name}`" 
                  :value="exam.id" 
                />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        
        <div v-if="currentExam && examStudents.length > 0" class="exam-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="考试名称">{{ currentExam.name }}</el-descriptions-item>
            <el-descriptions-item label="所属课程">{{ currentExam.courseName }}</el-descriptions-item>
            <el-descriptions-item label="考试类型">{{ getExamTypeName(currentExam.examType) }}</el-descriptions-item>
            <el-descriptions-item label="总分">{{ currentExam.totalMarks }}</el-descriptions-item>
            <el-descriptions-item label="考试时间">{{ formatDateTime(currentExam.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="学生总数">{{ examStudents.length }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-if="examStudents.length > 0" class="students-table">
          <el-table 
            :data="examStudents" 
            style="width: 100%"
            :row-key="row => row.studentIdNumber"
          >
            <el-table-column prop="studentIdNumber" label="学号" width="100" />
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
            <el-table-column prop="department" label="院系" width="100" />
            <el-table-column prop="score" label="成绩" width="100">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.score"
                  :min="0"
                  :max="currentExam.totalMarks"
                  :step="1"
                  size="small"
                  placeholder="请输入"
                />
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" width="150">
              <template #default="scope">
                <el-input
                  v-model="scope.row.remark"
                  placeholder="输入备注"
                  size="small"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-if="examStudents.length > 0" class="enter-grades-actions">
          <el-button type="warning" @click="autoGenerateScores">
            <i class="el-icon-random"></i> 自动生成
          </el-button>
          <el-button type="primary" @click="saveEnteredGrades">
            <i class="el-icon-check"></i> 保存成绩
          </el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 编辑成绩对话框 -->
    <el-dialog 
      title="编辑成绩" 
      v-model="editGradeDialogVisible" 
      width="50%"
    >
      <el-form 
        ref="gradeForm" 
        :model="gradeForm" 
        :rules="gradeRules" 
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生姓名">
              <el-input v-model="gradeForm.studentName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="gradeForm.studentIdNumber" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称">
              <el-input v-model="gradeForm.courseName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试名称">
              <el-input v-model="gradeForm.examName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试总分">
              <el-input v-model.number="gradeForm.totalMarks" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="得分" prop="score">
              <el-input-number
                v-model.number="gradeForm.score"
                :min="0"
                :max="gradeForm.totalMarks"
                :step="1"
                placeholder="请输入成绩"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input 
                v-model="gradeForm.remark" 
                type="textarea" 
                :rows="3" 
                placeholder="请输入备注信息" 
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editGradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGradeForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 成绩详情对话框 -->
    <el-dialog 
      title="成绩详情" 
      v-model="detailsDialogVisible" 
      width="60%"
    >
      <div v-if="currentGrade" class="grade-details">
        <el-row :gutter="20">
          <el-col :span="24">
            <h3 class="grade-title">
              {{ currentGrade.courseName }} - {{ currentGrade.examName }}
            </h3>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="学生姓名">{{ currentGrade.studentName }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ currentGrade.studentIdNumber }}</el-descriptions-item>
              <el-descriptions-item label="院系">{{ currentGrade.department }}</el-descriptions-item>
              <el-descriptions-item label="考试日期">{{ formatDate(currentGrade.examDate) }}</el-descriptions-item>
              <el-descriptions-item label="录入日期">{{ formatDateTime(currentGrade.entryDate) }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="得分">
                <el-tag :type="getScoreTag(currentGrade.score, currentGrade.totalMarks)" size="large">
                  {{ currentGrade.score }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="总分">{{ currentGrade.totalMarks }}</el-descriptions-item>
              <el-descriptions-item label="百分比">{{ currentGrade.percentage.toFixed(1) }}%</el-descriptions-item>
              <el-descriptions-item label="等级">
                <el-tag :type="getGradeTag(currentGrade.grade)" size="large">
                  {{ currentGrade.grade }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="24" class="mt-4">
            <el-divider>备注信息</el-divider>
            <div class="remark-content">{{ currentGrade.remark || '无' }}</div>
          </el-col>
          
          <el-col :span="24" class="mt-4">
            <el-button type="primary" @click="printGradeDetails">打印成绩单</el-button>
            <el-button type="info" @click="viewStudentProfile(currentGrade)">查看学生信息</el-button>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
    
    <!-- 批量导入对话框 -->
    <el-dialog 
      title="批量导入成绩" 
      v-model="importDialogVisible" 
      width="50%"
    >
      <div class="import-content">
        <p class="import-hint">请下载模板，填写完整信息后导入成绩数据。</p>
        
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
            <template #tip>
            <div class="el-upload__tip">
              支持上传Excel文件，仅支持.xlsx和.xls格式
            </div>
          </template>
          </el-upload>
        </div>
        
        <div v-if="importPreviewData.length > 0" class="import-preview">
          <h3>数据预览</h3>
          <el-table :data="importPreviewData" style="width: 100%" height="300">
            <el-table-column prop="studentId" label="学号" width="120" />
            <el-table-column prop="studentName" label="学生姓名" width="100" />
            <el-table-column prop="courseName" label="课程名称" width="150" />
            <el-table-column prop="examName" label="考试名称" width="150" />
            <el-table-column prop="score" label="成绩" width="80" />
            <el-table-column prop="error" label="错误信息" width="200">
              <template #default="scope">
                <span v-if="scope.row.error" class="error-text">{{ scope.row.error }}</span>
                <span v-else class="success-text">验证通过</span>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-if="hasImportErrors" class="error-message">
            <el-alert
              title="部分数据验证失败，请检查后重新导入"
              type="warning"
              description="请查看错误信息列了解具体问题"
              show-icon
              :closable="false"
            ></el-alert>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmImport" 
            :disabled="importFileList.length === 0 || hasImportErrors"
          >
            确认导入
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 成绩分析对话框 -->
    <el-dialog 
      title="成绩分析" 
      v-model="analysisDialogVisible" 
      width="85%"
      fullscreen
    >
      <div class="analysis-content">
        <div class="analysis-filters">
          <el-form label-width="80px" inline>
            <el-form-item label="选择课程">
              <el-select 
                v-model="analysisForm.courseId" 
                placeholder="请选择课程" 
                clearable
                @change="loadCourseExams"
              >
                <el-option 
                  v-for="course in courses" 
                  :key="course.id" 
                  :label="course.name" 
                  :value="course.id" 
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="选择考试">
              <el-select 
                v-model="analysisForm.examId" 
                placeholder="请选择考试" 
                clearable
                @change="analyzeGrades"
              >
                <el-option 
                  v-for="exam in courseExams" 
                  :key="exam.id" 
                  :label="exam.name" 
                  :value="exam.id" 
                />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="analyzeGrades">开始分析</el-button>
              <el-button @click="exportAnalysisReport">导出分析报告</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div v-if="analysisData && analysisData.basicStats" class="analysis-stats">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">参与人数</div>
                  <div class="stats-value">{{ analysisData.basicStats.totalCount }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">平均分</div>
                  <div class="stats-value">{{ analysisData.basicStats.averageScore.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">最高分</div>
                  <div class="stats-value">{{ analysisData.basicStats.maxScore }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">最低分</div>
                  <div class="stats-value">{{ analysisData.basicStats.minScore }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">及格率</div>
                  <div class="stats-value">{{ analysisData.basicStats.passRate.toFixed(1) }}%</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">优秀率</div>
                  <div class="stats-value">{{ analysisData.basicStats.excellentRate.toFixed(1) }}%</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">良好率</div>
                  <div class="stats-value">{{ analysisData.basicStats.goodRate.toFixed(1) }}%</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stats-card" shadow="hover">
                <div class="stats-item">
                  <div class="stats-label">中等率</div>
                  <div class="stats-value">{{ analysisData.basicStats.averageRate.toFixed(1) }}%</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <div v-if="analysisData" class="analysis-charts">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="chart-card" shadow="hover">
                <div slot="header" class="chart-header">
                  <span>分数段分布</span>
                </div>
                <div class="chart-content">
                  <el-table :data="analysisData.scoreDistribution" style="width: 100%">
                    <el-table-column prop="range" label="分数段" width="120" />
                    <el-table-column prop="count" label="人数" width="100" />
                    <el-table-column prop="percentage" label="占比" width="100">
                      <template #default="scope">
                        {{ scope.row.percentage.toFixed(1) }}%
                      </template>
                    </el-table-column>
                    <el-table-column prop="range" label="分布图" show-overflow-tooltip>
                      <template #default="scope">
                        <el-progress 
                          :percentage="scope.row.percentage" 
                          :color="getScoreRangeColor(scope.row.range)"
                          :status="getScoreRangeStatus(scope.row.range)"
                        />
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="chart-card" shadow="hover">
                <div slot="header" class="chart-header">
                  <span>等级分布</span>
                </div>
                <div class="chart-content">
                  <el-table :data="analysisData.gradeDistribution" style="width: 100%">
                    <el-table-column prop="grade" label="等级" width="100">
                      <template #default="scope">
                        <el-tag :type="getGradeTag(scope.row.grade)">
                          {{ scope.row.grade }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="count" label="人数" width="100" />
                    <el-table-column prop="percentage" label="占比" width="100">
                      <template #default="scope">
                        {{ scope.row.percentage.toFixed(1) }}%
                      </template>
                    </el-table-column>
                    <el-table-column prop="grade" label="分布图" show-overflow-tooltip>
                      <template #default="scope">
                        <el-progress 
                          :percentage="scope.row.percentage" 
                          :color="getGradeColor(scope.row.grade)"
                        />
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-card>
            </el-col>
            <el-col :span="24">
              <el-card class="chart-card" shadow="hover">
                <div slot="header" class="chart-header">
                  <span>院系成绩分布</span>
                </div>
                <div class="chart-content">
                  <el-table :data="analysisData.departmentDistribution" style="width: 100%">
                    <el-table-column prop="department" label="院系" width="120" />
                    <el-table-column prop="count" label="人数" width="100" />
                    <el-table-column prop="averageScore" label="平均分" width="100">
                      <template #default="scope">
                        {{ scope.row.averageScore.toFixed(2) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="passRate" label="及格率" width="100">
                      <template #default="scope">
                        {{ scope.row.passRate.toFixed(1) }}%
                      </template>
                    </el-table-column>
                    <el-table-column prop="excellentRate" label="优秀率" width="100">
                      <template #default="scope">
                        {{ scope.row.excellentRate.toFixed(1) }}%
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <div v-if="analysisData && analysisData.topStudents.length > 0" class="analysis-top-students">
          <el-card class="analysis-card" shadow="hover">
            <div slot="header" class="analysis-card-header">
              <span>成绩排行榜</span>
            </div>
            <div class="top-students-list">
              <el-table :data="analysisData.topStudents" style="width: 100%">
                <el-table-column label="排名" width="80">
                  <template #default="scope">
                    <el-tag 
                      :type="getRankTag(scope.$index + 1)" 
                      v-if="scope.$index < 3"
                    >
                      {{ scope.$index + 1 }}
                    </el-tag>
                    <span v-else>{{ scope.$index + 1 }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="studentName" label="学生姓名" width="120">
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
                <el-table-column prop="studentId" label="学号" width="120" />
                <el-table-column prop="department" label="院系" width="100" />
                <el-table-column prop="score" label="成绩" width="100">
                  <template #default="scope">
                    <el-tag :type="getScoreTag(scope.row.score, analysisData.basicStats.totalMarks)" size="large">
                      {{ scope.row.score }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="percentage" label="百分比" width="100">
                  <template #default="scope">
                    {{ scope.row.percentage.toFixed(1) }}%
                  </template>
                </el-table-column>
                <el-table-column prop="grade" label="等级" width="80">
                  <template #default="scope">
                    <el-tag :type="getGradeTag(scope.row.grade)" size="small">
                      {{ scope.row.grade }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </div>
        
        <div v-if="analysisData && analysisData.weakStudents.length > 0" class="analysis-weak-students">
          <el-card class="analysis-card" shadow="hover">
            <div slot="header" class="analysis-card-header">
              <span>需要关注的学生</span>
            </div>
            <div class="weak-students-list">
              <el-table :data="analysisData.weakStudents" style="width: 100%">
                <el-table-column prop="studentName" label="学生姓名" width="120">
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
                <el-table-column prop="studentId" label="学号" width="120" />
                <el-table-column prop="department" label="院系" width="100" />
                <el-table-column prop="score" label="成绩" width="100">
                  <template #default="scope">
                    <el-tag :type="getScoreTag(scope.row.score, analysisData.basicStats.totalMarks)" size="large">
                      {{ scope.row.score }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="percentage" label="百分比" width="100">
                  <template #default="scope">
                    {{ scope.row.percentage.toFixed(1) }}%
                  </template>
                </el-table-column>
                <el-table-column prop="grade" label="等级" width="80">
                  <template #default="scope">
                    <el-tag :type="getGradeTag(scope.row.grade)" size="small">
                      {{ scope.row.grade }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
              </el-table>
            </div>
          </el-card>
        </div>
        
        <div v-if="!analysisData" class="analysis-empty">
          <el-empty description="请选择课程和考试进行成绩分析" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'GradeManagement',
  data() {
    return {
      loading: false,
      grades: [],
      courses: [],
      exams: [],
      pendingExams: [],
      departments: [],
      selectedGrades: [],
      filteredGrades: [],
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        keyword: '',
        courseId: '',
        examId: '',
        departmentId: '',
        gradeStatus: ''
      },
      // 录入成绩相关
      enterGradesDialogVisible: false,
      selectedExamId: null,
      currentExam: null,
      examStudents: [],
      // 编辑成绩相关
      editGradeDialogVisible: false,
      gradeForm: {
        id: '',
        studentId: '',
        studentName: '',
        courseId: '',
        courseName: '',
        examId: '',
        examName: '',
        score: null,
        totalMarks: 100,
        percentage: null,
        grade: '',

        department: '',
        examDate: '',
        entryDate: '',
        entryBy: '',
        remark: ''
      },
      // 成绩详情相关
      detailsDialogVisible: false,
      currentGrade: null,
      // 批量导入相关
      importDialogVisible: false,
      importFileList: [],
      importPreviewData: [],
      // 成绩分析相关
      analysisDialogVisible: false,
      analysisForm: {
        courseId: '',
        examId: ''
      },
      courseExams: [],
      analysisData: null,
      // 表单验证规则
      gradeRules: {
        score: [
          { required: true, message: '请输入成绩', trigger: 'blur' },
          { type: 'number', message: '成绩必须为数字', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 分页后的成绩数据
    paginatedGrades() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredGrades.slice(start, end)
    },
    
    // 是否有导入错误
    hasImportErrors() {
      return this.importPreviewData.some(item => item.error)
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
          this.loadGrades(),
          this.loadCourses(),
          this.loadExams(),
          this.loadDepartments()
        ])
        
        // 过滤出已完成或进行中的考试
        this.pendingExams = this.exams.filter(e => e.status === 'COMPLETED' || e.status === 'IN_PROGRESS')
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 加载成绩列表
    async loadGrades() {
      try {
        const response = await this.$axios.get('/grades')
        if (response.data.success) {
          this.grades = response.data.data
          this.filteredGrades = [...this.grades]
        } else {
          this.$message.error(response.data.message || '加载成绩失败')
        }
      } catch (error) {
        console.error('加载成绩失败:', error)
        this.$message.error('加载成绩失败，请检查网络连接')
      }
    },
    
    // 加载课程列表
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/courses')
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
    
    // 加载考试列表
    async loadExams() {
      try {
        const response = await this.$axios.get('/api/exams')
        if (response.data.success) {
          this.exams = response.data.data
        } else {
          this.$message.error(response.data.message || '加载考试失败')
        }
      } catch (error) {
        console.error('加载考试失败:', error)
        this.$message.error('加载考试失败，请检查网络连接')
      }
    },
    
    // 加载院系列表
    async loadDepartments() {
      try {
        const response = await this.$axios.get('/api/departments')
        if (response.data.success) {
          this.departments = response.data.data
        } else {
          this.$message.error(response.data.message || '加载院系失败')
        }
      } catch (error) {
        console.error('加载院系失败:', error)
        this.$message.error('加载院系失败，请检查网络连接')
      }
    },
    
    // 搜索成绩
    searchGrades() {
      this.currentPage = 1
      this.filteredGrades = this.grades.filter(grade => {
        // 关键词搜索
        const keyword = this.searchForm.keyword.toLowerCase()
        if (keyword && 
            !grade.studentId.toLowerCase().includes(keyword) && 
            !grade.studentName.toLowerCase().includes(keyword) && 
            !grade.courseName.toLowerCase().includes(keyword) &&
            !(grade.entryBy && grade.entryBy.toLowerCase().includes(keyword))) {
          return false
        }
        
        // 课程筛选
        if (this.searchForm.courseId && grade.courseId !== this.searchForm.courseId) {
          return false
        }
        
        // 考试筛选
        if (this.searchForm.examId && grade.examId !== this.searchForm.examId) {
          return false
        }
        
        // 院系筛选
        if (this.searchForm.departmentId && grade.departmentId && grade.departmentId !== this.searchForm.departmentId) {
          return false
        }
        
        // 状态筛选
        if (this.searchForm.gradeStatus) {
          if (this.searchForm.gradeStatus === 'ENTERED' && grade.score === null) return false
          if (this.searchForm.gradeStatus === 'PENDING' && grade.score !== null) return false
          if (this.searchForm.gradeStatus === 'NOT_ATTENDED' && grade.status !== 'NOT_ATTENDED') return false
        }
        
        return true
      })
    },
    
    // 重置筛选条件
    resetFilters() {
      this.searchForm = {
        keyword: '',
        courseId: '',
        examId: '',
        departmentId: '',
        gradeStatus: ''
      }
      this.filteredGrades = [...this.grades]
      this.currentPage = 1
    },
    
    // 处理表格选择变化
    handleSelectionChange(selection) {
      this.selectedGrades = selection
    },
    
    // 处理表格行双击
    handleRowDblclick(row) {
      this.viewGradeDetails(row)
    },
    
    // 录入成绩
    enterGrades() {
      this.enterGradesDialogVisible = true
      this.selectedExamId = null
      this.currentExam = null
      this.examStudents = []
    },
    
    // 加载考试学生信息
    async loadExamStudents() {
      this.currentExam = this.exams.find(e => e.id === this.selectedExamId)
      if (this.currentExam) {
        try {
          const response = await this.$axios.get(`/api/exams/${this.selectedExamId}/students`)
          if (response.data.success) {
            this.examStudents = response.data.data
          } else {
            this.$message.error(response.data.message || '加载考试学生失败')
          }
        } catch (error) {
          console.error('加载考试学生失败:', error)
          this.$message.error('加载考试学生失败，请检查网络连接')
        }
      }
    },
    
    // 自动生成成绩
    autoGenerateScores() {
      if (!this.currentExam) return
      
      this.examStudents.forEach(student => {
        // 生成正态分布的成绩，平均分在65-85之间
        const mean = 75
        const stdDev = 15
        let score = Math.round((Math.random() * 2 - 1.96) * stdDev + mean)
        // 确保分数在合理范围内
        score = Math.max(0, Math.min(this.currentExam.totalMarks, score))
        
        // 5%的概率没有参加考试
        if (Math.random() < 0.05) {
          student.score = null
          student.remark = '缺考'
        } else {
          student.score = score
          if (!student.remark) {
            student.remark = ''
          }
        }
      })
    },
    
    // 保存录入的成绩
    async saveEnteredGrades() {
      if (!this.currentExam || this.examStudents.length === 0) return
      
      try {
        // 过滤出有成绩的记录
        const validStudents = this.examStudents.filter(s => s.score !== null)
        
        if (validStudents.length === 0) {
          this.$message.warning('请先录入成绩')
          return
        }
        
        const response = await this.$axios.post('/api/grades/batch', {
          examId: this.currentExam.id,
          students: validStudents
        })
        
        if (response.data.success) {
          // 重新加载成绩数据
          await this.loadGrades()
          this.enterGradesDialogVisible = false
          this.$message.success(`成功录入 ${validStudents.length} 条成绩记录`)
        } else {
          this.$message.error(response.data.message || '录入成绩失败')
        }
      } catch (error) {
        console.error('保存成绩失败:', error)
        this.$message.error('保存成绩失败，请稍后重试')
      }
    },
    
    // 编辑成绩
    editGrade(grade) {
      // 确保score是数字类型
      const scoreValue = grade.score !== undefined && grade.score !== null && grade.score !== ''
        ? Number(grade.score)
        : null;
      
      this.gradeForm = { 
        ...grade,
        score: isNaN(scoreValue) ? null : scoreValue
      }
      this.editGradeDialogVisible = true
      // 清除表单验证状态，避免编辑时显示之前的验证错误
      this.$nextTick(() => {
        if (this.$refs.gradeForm) {
          this.$refs.gradeForm.clearValidate();
        }
      })
    },
    
    // 提交成绩表单
    async submitGradeForm() {
      try {
        // 清除表单验证状态
        if (this.$refs.gradeForm) {
          this.$refs.gradeForm.clearValidate();
        }
        
        // 直接获取并处理成绩输入
        let score = this.gradeForm.score;
        console.log('原始成绩值:', score, '类型:', typeof score);
        
        // 处理空值
        if (score === '' || score === null || score === undefined) {
          this.$message.error('请输入成绩');
          return;
        }
        
        // 强制转换为数字
        if (typeof score !== 'number') {
          score = Number(score);
        }
        
        // 检查是否为有效数字
        if (isNaN(score)) {
          this.$message.error('成绩必须是有效数字');
          return;
        }
        
        // 检查范围
        if (score < 0) {
          this.$message.error('成绩不能为负数');
          return;
        }
        if (score > this.gradeForm.totalMarks) {
          this.$message.error(`成绩不能超过${this.gradeForm.totalMarks}`);
          return;
        }
        
        // 这里是关键：确保gradeForm.score是一个有效的数字
        // 因为表单验证是基于gradeForm.score的值进行的
        this.gradeForm.score = score;
        console.log('处理后的成绩值:', this.gradeForm.score, '类型:', typeof this.gradeForm.score);
        
        // 现在进行表单验证
        await this.$refs.gradeForm.validate();
        
        const formData = { ...this.gradeForm }
        
        // 不重新计算百分比和等级，让后端处理
        // 只发送必要的字段给后端
        const payload = {
          id: formData.id,
          score: score,
          studentId: formData.studentId,
          courseId: formData.courseId,
          examId: formData.examId,
          feedback: formData.feedback || formData.remark
        }
        
        const response = await this.$axios.put(`/grades/${formData.id}`, payload)
        if (response.data.success) {
          const index = this.grades.findIndex(g => g.id === formData.id)
          if (index !== -1) {
            this.grades[index] = response.data.data
          }
          
          this.editGradeDialogVisible = false
          this.searchGrades() // 刷新搜索结果
          this.$message.success('成绩更新成功')
        } else {
          this.$message.error(response.data.message || '成绩更新失败')
        }
      } catch (error) {
        console.error('更新成绩失败:', error);
        // 如果是验证错误，不显示通用错误消息
        if (error !== true && error !== false && !error.toString().includes('validate')) {
          this.$message.error('更新成绩失败，请稍后重试');
        }
      }
    },
    
    // 查看成绩详情
    viewGradeDetails(grade) {
      this.currentGrade = { ...grade }
      this.detailsDialogVisible = true
    },
    
    // 打印成绩详情
    printGradeDetails() {
      // 实际项目中应该实现打印功能
      window.print()
    },
    
    // 查看学生信息
    viewStudentProfile(grade) {
      console.log('查看学生信息:', grade)
      // 实际项目中应该跳转到学生详情页
      this.$router.push(`/admin/students`)
    },
    
    // 删除成绩
    deleteGrade(grade) {
      this.$confirm(`确定要删除 ${grade.studentName} 在 ${grade.courseName} 的 ${grade.examName} 成绩吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/grades/${grade.id}`)
          if (response.data.success) {
            const index = this.grades.findIndex(g => g.id === grade.id)
            if (index !== -1) {
              this.grades.splice(index, 1)
            }
            
            this.searchGrades() // 刷新搜索结果
            this.$message.success('删除成功')
          } else {
            this.$message.error(response.data.message || '删除成绩失败')
          }
        } catch (error) {
          console.error('删除成绩失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {
        // 用户取消操作
      })
    },
    
    // 批量删除
    batchDelete() {
      this.$confirm(`确定要删除选中的 ${this.selectedGrades.length} 条成绩记录吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const gradeIds = this.selectedGrades.map(g => g.id)
          
          const response = await this.$axios.delete('/grades/batch', {
            data: { gradeIds }
          })
          
          if (response.data.success) {
            this.grades = this.grades.filter(g => !gradeIds.includes(g.id))
            
            this.selectedGrades = []
            this.searchGrades() // 刷新搜索结果
            this.$message.success('批量删除成功')
          } else {
            this.$message.error(response.data.message || '批量删除失败')
          }
        } catch (error) {
          console.error('批量删除成绩失败:', error)
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
          studentId: '20210001',
          studentName: '张三',
          courseName: '计算机网络',
          examName: '期中考试',
          score: 85,
          error: ''
        },
        {
          studentId: '20210002',
          studentName: '李四',
          courseName: '计算机网络',
          examName: '期中考试',
          score: 92,
          error: ''
        },
        {
          studentId: '20210003',
          studentName: '王五',
          courseName: '计算机网络',
          examName: '期中考试',
          score: 78,
          error: ''
        },
        {
          studentId: '20210004',
          studentName: '赵六',
          courseName: '计算机网络',
          examName: '期中考试',
          score: 60,
          error: ''
        },
        {
          studentId: '20210005',
          studentName: '钱七',
          courseName: '计算机网络',
          examName: '期中考试',
          score: 105,
          error: '分数超出总分'
        }
      ]
    },
    
    // 确认导入
    async confirmImport() {
      try {
        // 实际项目中应该调用后端API导入数据
        // const formData = new FormData()
        // formData.append('file', this.importFileList[0].raw)
        // const response = await axios.post('/api/admin/grades/import', formData, {
        //   headers: {
        //     'Authorization': `Bearer ${localStorage.getItem('token')}`,
        //     'Content-Type': 'multipart/form-data'
        //   }
        // })
        // this.grades.push(...response.data)
        
        // 模拟导入成功
        this.importDialogVisible = false
        this.loadData() // 重新加载数据
        this.$message.success('批量导入成功')
      } catch (error) {
        console.error('批量导入失败:', error)
        this.$message.error('批量导入失败，请稍后重试')
      }
    },
    
    // 导出成绩数据
    exportGrades() {
      // 实际项目中应该调用后端API导出数据
      this.$message.info('导出功能开发中')
    },
    
    // 查看成绩分析
    viewGradeAnalysis() {
      this.analysisDialogVisible = true
      this.analysisForm = {
        courseId: '',
        examId: ''
      }
      this.courseExams = []
      this.analysisData = null
    },
    
    // 加载课程考试列表
    loadCourseExams() {
      this.courseExams = this.exams.filter(e => e.courseId === this.analysisForm.courseId)
      this.analysisForm.examId = ''
      this.analysisData = null
    },
    
    // 分析成绩
    analyzeGrades() {
      if (!this.analysisForm.courseId || !this.analysisForm.examId) {
        this.$message.warning('请选择课程和考试')
        return
      }
      
      // 获取所选考试的成绩
      const examGrades = this.grades.filter(g => g.examId === this.analysisForm.examId)
      const exam = this.exams.find(e => e.id === this.analysisForm.examId)
      
      if (examGrades.length === 0) {
        this.$message.warning('该考试暂无成绩数据')
        this.analysisData = null
        return
      }
      
      // 生成分析数据
      this.analysisData = this.generateAnalysisData(examGrades, exam)
    },
    
    // 导出分析报告
    exportAnalysisReport() {
      // 实际项目中应该调用后端API导出分析报告
      this.$message.info('导出分析报告功能开发中')
    },
    
    // 生成分析数据
    generateAnalysisData(grades, exam) {
      const validGrades = grades.filter(g => g.score !== null)
      const totalStudents = validGrades.length
      
      // 基本统计
      const scores = validGrades.map(g => g.score)
      const averageScore = scores.reduce((sum, score) => sum + score, 0) / totalStudents
      const maxScore = Math.max(...scores)
      const minScore = Math.min(...scores)
      const passThreshold = exam.totalMarks * 0.6
      const excellentThreshold = exam.totalMarks * 0.9
      const goodThreshold = exam.totalMarks * 0.8
      const averageThreshold = exam.totalMarks * 0.7
      
      const passCount = validGrades.filter(g => g.score >= passThreshold).length
      const excellentCount = validGrades.filter(g => g.score >= excellentThreshold).length
      const goodCount = validGrades.filter(g => g.score >= goodThreshold && g.score < excellentThreshold).length
      const averageCount = validGrades.filter(g => g.score >= averageThreshold && g.score < goodThreshold).length
      const failCount = validGrades.filter(g => g.score < passThreshold).length
      
      const passRate = (passCount / totalStudents) * 100
      const excellentRate = (excellentCount / totalStudents) * 100
      const goodRate = (goodCount / totalStudents) * 100
      const averageRate = (averageCount / totalStudents) * 100
      
      // 分数段分布
      const scoreDistribution = [
        { range: '0-59', min: 0, max: 59 },
        { range: '60-69', min: 60, max: 69 },
        { range: '70-79', min: 70, max: 79 },
        { range: '80-89', min: 80, max: 89 },
        { range: '90-100', min: 90, max: 100 }
      ].map(item => {
        const count = validGrades.filter(g => 
          g.score >= item.min && g.score <= item.max
        ).length
        return {
          range: item.range,
          count,
          percentage: (count / totalStudents) * 100
        }
      })
      
      // 等级分布
      const gradeDistribution = [
        { grade: '优秀', min: excellentThreshold, max: exam.totalMarks },
        { grade: '良好', min: goodThreshold, max: excellentThreshold - 1 },
        { grade: '中等', min: averageThreshold, max: goodThreshold - 1 },
        { grade: '及格', min: passThreshold, max: averageThreshold - 1 },
        { grade: '不及格', min: 0, max: passThreshold - 1 }
      ].map(item => {
        const count = validGrades.filter(g => 
          g.score >= item.min && g.score <= item.max
        ).length
        return {
          grade: item.grade,
          count,
          percentage: (count / totalStudents) * 100
        }
      })
      
      // 按院系分组统计
      const departmentMap = {}
      validGrades.forEach(grade => {
        if (!departmentMap[grade.department]) {
          departmentMap[grade.department] = {
            department: grade.department,
            scores: [],
            count: 0
          }
        }
        departmentMap[grade.department].scores.push(grade.score)
        departmentMap[grade.department].count++
      })
      
      const departmentDistribution = Object.values(departmentMap).map(dept => {
        const deptAvgScore = dept.scores.reduce((sum, score) => sum + score, 0) / dept.count
        const deptPassCount = dept.scores.filter(s => s >= passThreshold).length
        const deptExcellentCount = dept.scores.filter(s => s >= excellentThreshold).length
        
        return {
          department: dept.department,
          count: dept.count,
          averageScore: deptAvgScore,
          passRate: (deptPassCount / dept.count) * 100,
          excellentRate: (deptExcellentCount / dept.count) * 100
        }
      }).sort((a, b) => b.averageScore - a.averageScore)
      
      // 成绩排行前10名
      const topStudents = [...validGrades]
        .sort((a, b) => b.score - a.score)
        .slice(0, 10)
      
      // 需要关注的学生（不及格）
      const weakStudents = validGrades
        .filter(g => g.score < passThreshold)
        .sort((a, b) => a.score - b.score)
        .map(g => ({
          ...g,
          remark: this.generateWeakStudentRemark(g)
        }))
      
      return {
        basicStats: {
          totalCount: totalStudents,
          averageScore,
          maxScore,
          minScore,
          passRate,
          excellentRate,
          goodRate,
          averageRate,
          totalMarks: exam.totalMarks
        },
        scoreDistribution,
        gradeDistribution,
        departmentDistribution,
        topStudents,
        weakStudents
      }
    },
    
    // 生成差生备注
    generateWeakStudentRemark(grade) {
      const percentage = grade.percentage
      if (percentage < 30) return '基础薄弱，需要特别辅导'
      if (percentage < 45) return '学习困难，建议课后补习'
      if (percentage < 60) return '及格边缘，需要加强训练'
      return ''
    },
    
    // 获取成绩标签样式
    getScoreTag(score, totalMarks) {
      if (score === null) return 'info'
      const percentage = (score / totalMarks) * 100
      if (percentage >= 90) return 'success'
      if (percentage >= 80) return 'primary'
      if (percentage >= 70) return 'warning'
      if (percentage >= 60) return 'info'
      return 'danger'
    },
    
    // 获取等级标签样式
    getGradeTag(grade) {
      const tagMap = {
        '优秀': 'success',
        '良好': 'primary',
        '中等': 'warning',
        '及格': 'info',
        '不及格': 'danger'
      }
      return tagMap[grade] || 'info'
    },
    
    // 获取等级颜色
    getGradeColor(grade) {
      const colorMap = {
        '优秀': '#67c23a',
        '良好': '#409eff',
        '中等': '#e6a23c',
        '及格': '#909399',
        '不及格': '#f56c6c'
      }
      return colorMap[grade] || '#909399'
    },
    
    // 获取分数段标签样式
    getScoreRangeTag(range) {
      const rangeMap = {
        '0-59': 'danger',
        '60-69': 'info',
        '70-79': 'warning',
        '80-89': 'primary',
        '90-100': 'success'
      }
      return rangeMap[range] || 'info'
    },
    
    // 获取分数段颜色
    getScoreRangeColor(range) {
      const colorMap = {
        '0-59': '#f56c6c',
        '60-69': '#909399',
        '70-79': '#e6a23c',
        '80-89': '#409eff',
        '90-100': '#67c23a'
      }
      return colorMap[range] || '#909399'
    },
    
    // 获取分数段状态
    getScoreRangeStatus(range) {
      if (range === '0-59') return 'exception'
      return ''
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
    
    // 计算成绩等级
    calculateGrade(percentage) {
      if (percentage >= 90) return '优秀'
      if (percentage >= 80) return '良好'
      if (percentage >= 70) return '中等'
      if (percentage >= 60) return '及格'
      return '不及格'
    },
    
    // 获取成绩标签样式
    getGradeTag(grade) {
      const tagMap = {
        '优秀': 'success',
        '良好': 'primary',
        '中等': 'info',
        '及格': 'warning',
        '不及格': 'danger'
      }
      return tagMap[grade] || 'info'
    },
    
    // 获取状态标签样式
    getStatusTag(status) {
      const tagMap = {
        'ENTERED': 'success',
        'PENDING': 'warning',
        'NOT_ATTENDED': 'danger'
      }
      return tagMap[status] || 'info'
    },
    
    // 获取状态名称
    getStatusName(status) {
      const statusMap = {
        'ENTERED': '已录入',
        'PENDING': '待录入',
        'NOT_ATTENDED': '未参加'
      }
      return statusMap[status] || status
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
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    handleCurrentChange(current) {
      this.currentPage = current
    },
  }
}
</script>
<style scoped>
.grade-management {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  vertical-align: middle;
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

.grade-table {
  margin-bottom: 20px;
}

.empty-state {
  padding: 60px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 导入对话框样式 */
.import-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.import-hint {
  color: #606266;
  font-size: 14px;
}

.template-download {
  margin-bottom: 10px;
}

.import-preview h3 {
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 16px;
  border-left: 4px solid var(--primary-color);
  padding-left: 12px;
}

.error-text {
  color: #666666;
}

.success-text {
  color: var(--text-primary);
}

.error-message {
  margin-top: 16px;
}

/* 表格样式 - 黑白配色 */
.grade-table :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.grade-table :deep(.el-table__header-wrapper .el-table__header) {
  background-color: #000000;
}

.grade-table :deep(.el-table__header-wrapper .el-table__header th) {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
}

.grade-table :deep(.el-table__body-wrapper .el-table__body) {
  background-color: #ffffff;
}

.grade-table :deep(.el-table__body-wrapper .el-table__body tr) {
  background-color: #ffffff;
}

.grade-table :deep(.el-table__body-wrapper .el-table__body tr:hover) {
  background-color: #f5f5f5;
}

.grade-table :deep(.el-table__body-wrapper .el-table__body td) {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.grade-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.grade-table :deep(.el-checkbox__inner:hover) {
  border-color: #000000;
}

/* 标签样式 */
.grade-table :deep(.el-tag) {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.grade-table :deep(.el-tag--info) {
  background-color: #f0f0f0;
}

.grade-table :deep(.el-tag--danger) {
  background-color: #f0f0f0;
}

.grade-table :deep(.el-tag--success) {
  background-color: #f0f0f0;
}

.grade-table :deep(.el-tag--warning) {
  background-color: #f0f0f0;
}

.grade-table :deep(.el-tag--primary) {
  background-color: #f0f0f0;
}

/* 开关样式 */
.grade-table :deep(.el-switch__core) {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.grade-table :deep(.el-switch.is-checked .el-switch__core) {
  background-color: #000000;
}

/* 按钮样式 */
.grade-table :deep(.el-button--primary) {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.grade-table :deep(.el-button--danger) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.grade-table :deep(.el-button--success) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.grade-table :deep(.el-button--info) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.grade-table :deep(.el-button--warning) {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .grade-management {
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
