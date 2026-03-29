<template>
  <div class="student-grade-query">
    <el-card class="exams-header">
      <h1>成绩查询</h1>
      <div class="header-actions">
        <div class="user-info">
          <span>姓名：{{ currentStudent.name }}</span>
          <span>学号：{{ currentStudent.id }}</span>
          <span>班级：{{ currentStudent.className }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="exams-content">
      <div class="search-filters">
        <el-select v-model="semesterFilter" placeholder="选择学期" clearable style="width: 180px; margin-right: 12px;">
          <el-option label="全部学期" value=""></el-option>
          <el-option 
            v-for="semester in semesters" 
            :key="semester.value" 
            :label="semester.label" 
            :value="semester.value"
          ></el-option>
        </el-select>
        <el-select v-model="courseTypeFilter" placeholder="课程类型" clearable style="width: 180px; margin-right: 12px;">
          <el-option label="全部类型" value=""></el-option>
          <el-option label="必修课" value="必修课"></el-option>
          <el-option label="选修课" value="选修课"></el-option>
          <el-option label="公选课" value="公选课"></el-option>
        </el-select>
        <el-select v-model="statusFilter" placeholder="成绩状态" clearable style="width: 180px; margin-right: 12px;">
          <el-option label="全部状态" value=""></el-option>
          <el-option label="已发布" value="published"></el-option>
          <el-option label="未发布" value="unpublished"></el-option>
        </el-select>
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程名称/课程号"
          prefix-icon="el-icon-search"
          @keyup.enter.native="applyFilters"
          style="width: 240px; margin-right: 12px;"
        ></el-input>
        <el-button type="primary" @click="applyFilters" icon="el-icon-search" style="margin-right: 12px;">搜索</el-button>
        <el-button @click="resetFilters" icon="el-icon-refresh">重置</el-button>
      </div>

      <!-- 成绩统计卡片 -->
      <div class="layout-grid grid-4 gap-20 mb-20">
        <el-card class="stats-card hover-card">
          <div class="stats-content">
            <div class="stat-icon el-icon-data-line"></div>
            <div class="stat-info">
              <div class="stat-number">{{ gradeStats.averageScore || 0 }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </div>
        </el-card>
        <el-card class="stats-card success hover-card">
          <div class="stats-content">
            <div class="stat-icon el-icon-circle-check"></div>
            <div class="stat-info">
              <div class="stat-number">{{ gradeStats.passedCount }}</div>
              <div class="stat-label">通过科目</div>
            </div>
          </div>
        </el-card>
        <el-card class="stats-card danger hover-card">
          <div class="stats-content">
            <div class="stat-icon el-icon-circle-close"></div>
            <div class="stat-info">
              <div class="stat-number">{{ gradeStats.failedCount }}</div>
              <div class="stat-label">挂科科目</div>
            </div>
          </div>
        </el-card>
        <el-card class="stats-card info hover-card">
          <div class="stats-content">
            <div class="stat-icon el-icon-book"></div>
            <div class="stat-info">
              <div class="stat-number">{{ gradeStats.totalCredits || 0 }}</div>
              <div class="stat-label">已修学分</div>
            </div>
          </div>
        </el-card>
      </div>

        <!-- 内容标签页 -->
        <el-tabs v-model="activeTab" class="main-tabs">
        <el-tab-pane label="成绩明细" name="grades">
          <div class="table-container mb-20">
            <el-table
              v-loading="loading"
              :data="paginatedGrades"
              style="width: 100%"
              border
              @row-click="handleRowClick"
              class="hover-card table-black-white"
            >
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="courseCode" label="课程号" width="120"></el-table-column>
              <el-table-column prop="courseName" label="课程名称" min-width="180">
                <template #default="scope">
                  <div class="flex items-center gap-10">
                    <span class="text-truncate" :title="scope.row.courseName">{{ scope.row.courseName }}</span>
                    <el-tag v-if="scope.row.courseType" size="small" :type="getCourseTypeTag(scope.row.courseType)">
                      {{ scope.row.courseType }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="credits" label="学分" width="80" align="center"></el-table-column>
              <el-table-column prop="semester" label="学期" width="120"></el-table-column>
              <el-table-column prop="teacherName" label="授课教师" width="120"></el-table-column>
              <el-table-column prop="score" label="最终成绩" width="100" align="center">
                <template #default="scope">
                  <div :class="['score-display', getScoreLevelClass(scope.row.score)]">
                    <template v-if="scope.row.status === 'published' || scope.row.status === 'ATTENDED'">
                      {{ scope.row.score }}
                      <span class="score-level">{{ getScoreLevel(scope.row.score) }}</span>
                    </template>
                    <template v-else>
                      <el-tag type="info" size="small">未发布</el-tag>
                    </template>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="statusText" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusTagType(scope.row.status)">
                    {{ scope.row.statusText }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="publishTime" label="发布时间" width="160">
                <template #default="scope">
                  {{ scope.row.publishTime ? formatDateTime(scope.row.publishTime) : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="viewGradeDetails(scope.row)"
                    icon="el-icon-view"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="filteredGrades.length"
                :page-size.sync="pageSize"
                :current-page.sync="currentPage"
                :page-sizes="[10, 20, 50]"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              ></el-pagination>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="成绩分析" name="analysis">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
              <el-card class="chart-card">
                <div slot="header" class="card-header">
                  <span>成绩分布图</span>
                </div>
                <div class="chart-container">
                  <div v-if="!gradeChartData.scoreDistribution.length" class="empty-chart">
                    <el-empty description="暂无数据"></el-empty>
                  </div>
                  <div v-else class="chart-placeholder">
                    <!-- 这里应该是图表区域 -->
                    <div class="chart-items">
                      <div 
                        v-for="(item, index) in gradeChartData.scoreDistribution" 
                        :key="index"
                        class="chart-item"
                      >
                        <div class="chart-bar" :style="{ height: item.percentage + '%', backgroundColor: item.color }"></div>
                        <div class="chart-label">{{ item.range }}</div>
                        <div class="chart-value">{{ item.count }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
              <el-card class="chart-card">
                <template #header>
                  <div class="card-header">
                    <span>学期成绩趋势</span>
                  </div>
                </template>
                <div class="chart-container">
                  <div v-if="!gradeChartData.semesterTrend.length" class="empty-chart">
                    <el-empty description="暂无数据"></el-empty>
                  </div>
                  <div v-else class="chart-placeholder">
                    <!-- 这里应该是图表区域 -->
                    <div class="trend-chart">
                      <div 
                        v-for="(item, index) in gradeChartData.semesterTrend" 
                        :key="index"
                        class="trend-item"
                      >
                        <div class="trend-point" :style="{ bottom: item.score + '%', backgroundColor: item.color }"></div>
                        <div class="trend-label">{{ item.semester }}</div>
                        <div class="trend-value">{{ item.score.toFixed(1) }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-card class="chart-card">
                <div slot="header" class="card-header">
                  <span>各课程类型成绩统计</span>
                </div>
                <div class="chart-container">
                  <div v-if="!gradeChartData.typeStats.length" class="empty-chart">
                    <el-empty description="暂无数据"></el-empty>
                  </div>
                  <div v-else class="chart-placeholder">
                    <!-- 这里应该是图表区域 -->
                    <div class="type-stats-chart">
                      <div 
                        v-for="(item, index) in gradeChartData.typeStats" 
                        :key="index"
                        class="type-stat-item"
                      >
                        <div class="type-stat-header">
                          <el-tag :type="getCourseTypeTag(item.type)">{{ item.type }}</el-tag>
                          <span class="type-count">{{ item.count }}门</span>
                        </div>
                        <div class="type-stat-body">
                          <div class="type-stat-bar">
                            <div class="type-stat-bar-inner" :style="{ width: '100%' }">
                              <div 
                                class="type-stat-bar-fill passed" 
                                :style="{ width: item.passPercentage + '%' }"
                              ></div>
                              <div 
                                class="type-stat-bar-fill failed" 
                                :style="{ width: item.failPercentage + '%' }"
                              ></div>
                            </div>
                          </div>
                          <div class="type-stat-footer">
                            <span>平均分：{{ item.averageScore.toFixed(1) }}</span>
                            <span>通过率：{{ item.passPercentage }}%</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="成绩单下载" name="download">
          <div class="download-section">
            <el-card class="download-card">
              <div class="download-header">
                <h3>成绩单生成</h3>
                <p>选择要生成成绩单的学期范围</p>
              </div>
              <el-form :model="downloadForm" :rules="downloadFormRules" ref="downloadForm" label-width="120px">
                <el-form-item label="开始学期" prop="startSemester">
                  <el-select v-model="downloadForm.startSemester" placeholder="请选择开始学期">
                    <el-option 
                      v-for="semester in semesters" 
                      :key="semester.value" 
                      :label="semester.label" 
                      :value="semester.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="结束学期" prop="endSemester">
                  <el-select v-model="downloadForm.endSemester" placeholder="请选择结束学期">
                    <el-option 
                      v-for="semester in semesters" 
                      :key="semester.value" 
                      :label="semester.label" 
                      :value="semester.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="成绩类型">
                  <el-checkbox-group v-model="downloadForm.gradeTypes">
                    <el-checkbox label="已发布成绩">已发布成绩</el-checkbox>
                    <el-checkbox label="未发布成绩">未发布成绩</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                <el-form-item label="文件格式">
                  <el-radio-group v-model="downloadForm.format">
                    <el-radio label="pdf">PDF格式</el-radio>
                    <el-radio label="excel">Excel格式</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="generateTranscript" :loading="downloadLoading" style="width: 100%;">
                    <i class="el-icon-document"></i> 生成并下载成绩单
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 空状态 -->
      <div v-if="activeTab === 'grades' && filteredGrades.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无成绩记录"></el-empty>
      </div>
    </el-card>

    <!-- 成绩详情对话框 -->
    <el-dialog
      title="成绩详情"
      :visible.sync="detailDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedGrade" class="grade-detail">
        <div class="detail-header">
          <div class="detail-title">
            <h3>{{ selectedGrade.courseName }}</h3>
            <el-tag v-if="selectedGrade.courseType" size="small" :type="getCourseTypeTag(selectedGrade.courseType)">
              {{ selectedGrade.courseType }}
            </el-tag>
          </div>
          <div class="detail-score">
            <template v-if="selectedGrade.status === 'published' || selectedGrade.status === 'ATTENDED'">
              <div class="final-score" :class="getScoreLevelClass(selectedGrade.score)">
                {{ selectedGrade.score }}
                <span class="score-level">{{ getScoreLevel(selectedGrade.score) }}</span>
              </div>
            </template>
            <template v-else>
              <el-tag type="info">未发布</el-tag>
            </template>
          </div>
        </div>

        <div class="detail-content">
          <el-descriptions border :column="1" class="detail-descriptions">
            <el-descriptions-item label="课程号">{{ selectedGrade.courseCode }}</el-descriptions-item>
            <el-descriptions-item label="学分">{{ selectedGrade.credits }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ selectedGrade.semester }}</el-descriptions-item>
            <el-descriptions-item label="授课教师">{{ selectedGrade.teacherName }}</el-descriptions-item>
            <el-descriptions-item label="总成绩构成">
              <div v-if="selectedGrade.status === 'published' || selectedGrade.status === 'ATTENDED'">
                <div class="score-detail-item">
                  <span>总成绩:</span>
                  <span class="score-detail-value">{{ selectedGrade.score }}分</span>
                </div>
              </div>
              <div v-else>-</div>
            </el-descriptions-item>
            <el-descriptions-item label="成绩状态">{{ selectedGrade.statusText }}</el-descriptions-item>
            <el-descriptions-item label="发布时间" v-if="selectedGrade.publishTime">
              {{ formatDateTime(selectedGrade.publishTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="教师评语" v-if="selectedGrade.feedback">
              {{ selectedGrade.feedback }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
      <div class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </template>
    </el-dialog>


  </div>
</template>

<script>
import { ElNotification } from 'element-plus'

export default {
  name: 'StudentGradeQuery',
  data() {
    return {
      loading: false,
      downloadLoading: false,
      searchQuery: '',
      semesterFilter: '',
      courseTypeFilter: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      activeTab: 'grades',
      grades: [],
      selectedGrade: null,
      detailDialogVisible: false,
      currentStudent: {
        name: '',
        id: '',
        className: ''
      },
      semesters: [
        { label: '2023-2024学年 第2学期', value: '2023-2024-2' },
        { label: '2023-2024学年 第1学期', value: '2023-2024-1' },
        { label: '2022-2023学年 第2学期', value: '2022-2023-2' },
        { label: '2022-2023学年 第1学期', value: '2022-2023-1' },
        { label: '2021-2022学年 第2学期', value: '2021-2022-2' },
        { label: '2021-2022学年 第1学期', value: '2021-2022-1' },
        { label: '2020-2021学年 第2学期', value: '2020-2021-2' },
        { label: '2020-2021学年 第1学期', value: '2020-2021-1' }
      ],
      downloadForm: {
        startSemester: '2020-2021-1',
        endSemester: '2023-2024-2',
        gradeTypes: ['已发布成绩'],
        format: 'pdf'
      },
      downloadFormRules: {
        startSemester: [{ required: true, message: '请选择开始学期', trigger: 'change' }],
        endSemester: [{ required: true, message: '请选择结束学期', trigger: 'change' }]
      },
      gradeStats: {
        averageScore: 0,
        passedCount: 0,
        failedCount: 0,
        totalCredits: 0
      },
      gradeChartData: {
        scoreDistribution: [],
        semesterTrend: [],
        typeStats: []
      }
    }
  },
  computed: {
    // 过滤后的成绩列表
    filteredGrades() {
      return this.grades.filter(grade => {
        // 搜索过滤
        const matchesSearch = !this.searchQuery || 
          grade.courseName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          grade.courseCode.toLowerCase().includes(this.searchQuery.toLowerCase());
        
        // 学期过滤
        const matchesSemester = !this.semesterFilter || grade.semester === this.semesterFilter;
        
        // 课程类型过滤
        const matchesCourseType = !this.courseTypeFilter || grade.courseType === this.courseTypeFilter;
        
        // 状态过滤
        const matchesStatus = !this.statusFilter || grade.status === this.statusFilter;
        
        return matchesSearch && matchesSemester && matchesCourseType && matchesStatus;
      }).sort((a, b) => {
        // 按学期降序排序
        return b.semester.localeCompare(a.semester);
      });
    },
    
    // 分页后的成绩列表
    paginatedGrades() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredGrades.slice(start, end);
    }
  },
  mounted() {
    this.loadGrades();
  },
  watch: {
    // 当筛选条件变化时重新计算统计数据
    filteredGrades: {
      handler() {
        this.updateGradeStats();
        this.updateChartData();
      },
      deep: true
    },
    // 当标签页切换到成绩分析时，重新更新图表数据
    activeTab(newVal) {
      if (newVal === 'analysis') {
        this.updateChartData();
      }
    }
  },
  methods: {
    // 加载当前学生信息
    async loadStudentInfo() {
      try {
        const response = await this.$axios.get('/api/student/profile', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          const student = response.data.data;
          this.currentStudent.name = student.name;
          this.currentStudent.id = student.studentId; // 使用学号而非数据库ID
          this.currentStudent.className = student.className;
        }
      } catch (error) {
        console.error('加载学生信息失败:', error);
        this.showNotification('加载失败', error.response?.data?.message || '加载学生信息失败', 'error');
      }
    },
    
    // 加载成绩数据
    async loadGrades() {
      try {
        this.loading = true;
        
        // 先加载学生信息
        await this.loadStudentInfo();
        
        // 使用真实API数据
        await this.loadRealGrades();
      } catch (error) {
        console.error('加载成绩数据失败:', error);
        this.showNotification('加载失败', error.response?.data?.message || '加载成绩数据失败，请稍后重试', 'error');
      } finally {
        this.loading = false;
      }
    },
    
    // 应用筛选条件
    applyFilters() {
      this.currentPage = 1;
    },
    
    // 重置筛选条件
    resetFilters() {
      this.searchQuery = '';
      this.semesterFilter = '';
      this.courseTypeFilter = '';
      this.statusFilter = '';
      this.currentPage = 1;
    },
    
    // 查看成绩详情
    viewGradeDetails(grade) {
      this.selectedGrade = { ...grade };
      this.detailDialogVisible = true;
    },
    
    // 处理行点击
    handleRowClick(row) {
      this.viewGradeDetails(row);
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
        minute: '2-digit'
      });
    },
    
    // 获取课程类型标签样式
    getCourseTypeTag(type) {
      const typeMap = {
        '必修课': 'warning',
        '选修课': 'success',
        '公选课': 'primary'
      };
      return typeMap[type] || 'default';
    },
    
    // 获取状态标签样式
    getStatusTagType(status) {
      const statusMap = {
        'published': 'success',
        'unpublished': 'info'
      };
      return statusMap[status] || 'default';
    },
    
    // 获取成绩等级
    getScoreLevel(score) {
      if (score >= 90) return '优秀';
      if (score >= 80) return '良好';
      if (score >= 70) return '中等';
      if (score >= 60) return '及格';
      return '不及格';
    },
    
    // 获取成绩等级样式类
    getScoreLevelClass(score) {
      if (score >= 90) return 'score-excellent';
      if (score >= 80) return 'score-good';
      if (score >= 70) return 'score-medium';
      if (score >= 60) return 'score-pass';
      return 'score-fail';
    },
    
    // 显示通知
    showNotification(title, message, type = 'info') {
      ElNotification({
        title: title,
        message: message,
        type: type,
        duration: 4000
      })
    },
    
    // 更新成绩统计数据
    updateGradeStats() {
      // 只统计已发布的成绩
      const publishedGrades = this.filteredGrades.filter(g => g.status === 'published');
      
      if (publishedGrades.length === 0) {
        this.gradeStats = {
          averageScore: 0,
          passedCount: 0,
          failedCount: 0,
          totalCredits: 0
        };
        return;
      }
      
      // 计算平均分
      const totalScore = publishedGrades.reduce((sum, g) => sum + g.finalScore, 0);
      const averageScore = totalScore / publishedGrades.length;
      
      // 计算通过和未通过科目数
      const passedCount = publishedGrades.filter(g => g.finalScore >= 60).length;
      const failedCount = publishedGrades.filter(g => g.finalScore < 60).length;
      
      // 计算已修学分（只计算通过的课程）
      const totalCredits = publishedGrades
        .filter(g => g.finalScore >= 60)
        .reduce((sum, g) => sum + g.credits, 0);
      
      this.gradeStats = {
        averageScore: averageScore.toFixed(1),
        passedCount,
        failedCount,
        totalCredits
      };
    },
    
    // 更新图表数据
    updateChartData() {
      // 只统计已发布的成绩
      const publishedGrades = this.filteredGrades.filter(g => g.status === 'published');
      
      // 成绩分布数据
      const ranges = [
        { range: '0-59', min: 0, max: 59, color: '#f56c6c' },
        { range: '60-69', min: 60, max: 69, color: '#e6a23c' },
        { range: '70-79', min: 70, max: 79, color: '#67c23a' },
        { range: '80-89', min: 80, max: 89, color: '#409eff' },
        { range: '90-100', min: 90, max: 100, color: '#909399' }
      ];
      
      const distribution = ranges.map(range => {
        const count = publishedGrades.filter(g => 
          g.finalScore >= range.min && g.finalScore <= range.max
        ).length;
        return {
          range: range.range,
          count,
          percentage: publishedGrades.length > 0 ? (count / publishedGrades.length * 100) : 0,
          color: range.color
        };
      });
      
      // 学期趋势数据
      const semesterMap = {};
      publishedGrades.forEach(g => {
        if (!semesterMap[g.semester]) {
          semesterMap[g.semester] = { scores: [], count: 0 };
        }
        semesterMap[g.semester].scores.push(g.finalScore);
        semesterMap[g.semester].count++;
      });
      
      const trendData = Object.keys(semesterMap)
        .sort()
        .map(semester => {
          const scores = semesterMap[semester].scores;
          const average = scores.reduce((sum, score) => sum + score, 0) / scores.length;
          // 将平均分映射到0-100的百分比
          const percentageScore = Math.min(100, Math.max(0, average));
          return {
            semester: semester,
            score: percentageScore,
            color: this.getScoreColor(average)
          };
        });
      
      // 课程类型统计数据
      const typeMap = {};
      publishedGrades.forEach(g => {
        if (!typeMap[g.courseType]) {
          typeMap[g.courseType] = { 
            type: g.courseType, 
            scores: [], 
            passed: 0, 
            failed: 0, 
            count: 0 
          };
        }
        typeMap[g.courseType].scores.push(g.finalScore);
        typeMap[g.courseType].count++;
        if (g.finalScore >= 60) {
          typeMap[g.courseType].passed++;
        } else {
          typeMap[g.courseType].failed++;
        }
      });
      
      const typeStats = Object.values(typeMap).map(typeData => {
        const totalScores = typeData.scores.reduce((sum, score) => sum + score, 0);
        const averageScore = totalScores / typeData.scores.length;
        const passPercentage = (typeData.passed / typeData.count) * 100;
        const failPercentage = (typeData.failed / typeData.count) * 100;
        
        return {
          type: typeData.type,
          count: typeData.count,
          averageScore,
          passed: typeData.passed,
          failed: typeData.failed,
          passPercentage: passPercentage.toFixed(1),
          failPercentage: failPercentage.toFixed(1)
        };
      });
      
      this.gradeChartData = {
        scoreDistribution: distribution,
        semesterTrend: trendData,
        typeStats
      };
    },
    
    // 获取成绩颜色
    getScoreColor(score) {
      if (score >= 90) return '#909399';
      if (score >= 80) return '#409eff';
      if (score >= 70) return '#67c23a';
      if (score >= 60) return '#e6a23c';
      return '#f56c6c';
    },
    
    // 加载真实成绩数据
    async loadRealGrades() {
      try {
        const response = await this.$axios.get('/api/student/grades', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        if (response.data.success) {
          // 确保从后端返回的数据格式与前端期望的格式一致
          this.grades = response.data.data || [];
        } else {
          console.error('加载成绩失败:', response.data.message);
          this.showNotification('加载失败', response.data.message || '加载成绩数据失败', 'error');
          this.grades = []; // 使用空数组作为备选
        }
      } catch (error) {
        console.error('加载成绩数据失败:', error);
        this.showNotification('加载失败', error.response?.data?.message || '加载成绩数据失败，请稍后重试', 'error');
        this.grades = []; // 使用空数组作为备选
      }
    },
    
    // 生成并下载成绩单
    async generateTranscript() {
      this.$refs.downloadForm.validate(async (valid) => {
        if (valid) {
          try {
            this.downloadLoading = true;
            
            // 实际项目中应该调用后端API生成并下载成绩单
            // const response = await this.$axios.post('/api/student/transcript/generate', {
            //   studentId: this.currentStudent.id,
            //   startSemester: this.downloadForm.startSemester,
            //   endSemester: this.downloadForm.endSemester,
            //   includeUnpublished: this.downloadForm.gradeTypes.includes('未发布成绩'),
            //   format: this.downloadForm.format
            // }, {
            //   responseType: 'blob'
            // });
            
            // 处理下载逻辑
            // const blob = new Blob([response.data], { type: this.downloadForm.format === 'pdf' ? 'application/pdf' : 'application/vnd.ms-excel' });
            // const url = window.URL.createObjectURL(blob);
            // const link = document.createElement('a');
            // link.href = url;
            // link.download = `成绩单_${this.currentStudent.id}_${this.downloadForm.startSemester}_${this.downloadForm.endSemester}.${this.downloadForm.format}`;
            // document.body.appendChild(link);
            // link.click();
            // window.URL.revokeObjectURL(url);
            // document.body.removeChild(link);
            
            // 模拟下载延迟
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            // 模拟下载成功
            this.showNotification('生成成功', '成绩单已生成并开始下载', 'success');
          } catch (error) {
            console.error('生成成绩单失败:', error);
            this.showNotification('生成失败', error.response?.data?.message || '生成成绩单失败，请稍后重试', 'error');
          } finally {
            this.downloadLoading = false;
          }
        }
      });
    },
    
    // 生成模拟数据
    getMockGrades() {
      // 课程列表
      const courses = [
        { code: 'CS101', name: '计算机导论', type: '必修课', credits: 3 },
        { code: 'CS102', name: '程序设计基础', type: '必修课', credits: 4 },
        { code: 'CS201', name: '数据结构', type: '必修课', credits: 4 },
        { code: 'CS202', name: '计算机组成原理', type: '必修课', credits: 4 },
        { code: 'CS301', name: '操作系统', type: '必修课', credits: 4 },
        { code: 'CS302', name: '计算机网络', type: '必修课', credits: 4 },
        { code: 'CS303', name: '数据库原理', type: '必修课', credits: 4 },
        { code: 'CS401', name: '软件工程', type: '必修课', credits: 3 },
        { code: 'CS402', name: '算法分析与设计', type: '选修课', credits: 3 },
        { code: 'CS403', name: '人工智能导论', type: '选修课', credits: 3 },
        { code: 'CS404', name: '机器学习', type: '选修课', credits: 3 },
        { code: 'CS405', name: '云计算', type: '选修课', credits: 2 },
        { code: 'CS406', name: '移动应用开发', type: '选修课', credits: 3 },
        { code: 'MATH101', name: '高等数学(上)', type: '必修课', credits: 5 },
        { code: 'MATH102', name: '高等数学(下)', type: '必修课', credits: 5 },
        { code: 'MATH201', name: '线性代数', type: '必修课', credits: 4 },
        { code: 'MATH202', name: '概率论与数理统计', type: '必修课', credits: 4 },
        { code: 'ENG101', name: '大学英语(1)', type: '必修课', credits: 4 },
        { code: 'ENG102', name: '大学英语(2)', type: '必修课', credits: 4 },
        { code: 'ENG201', name: '大学英语(3)', type: '必修课', credits: 4 },
        { code: 'GEN101', name: '大学物理(上)', type: '必修课', credits: 4 },
        { code: 'GEN102', name: '大学物理(下)', type: '必修课', credits: 4 },
        { code: 'GEN201', name: '马克思主义基本原理', type: '必修课', credits: 3 },
        { code: 'GEN202', name: '毛泽东思想和中国特色社会主义理论体系概论', type: '必修课', credits: 4 },
        { code: 'PE101', name: '体育(1)', type: '必修课', credits: 1 },
        { code: 'PE102', name: '体育(2)', type: '必修课', credits: 1 },
        { code: 'PE201', name: '体育(3)', type: '必修课', credits: 1 },
        { code: 'PE202', name: '体育(4)', type: '必修课', credits: 1 },
        { code: 'ELECT101', name: '创新创业基础', type: '公选课', credits: 2 },
        { code: 'ELECT102', name: '网络安全基础', type: '公选课', credits: 2 }
      ];
      
      // 教师列表
      const teachers = ['张教授', '李副教授', '王讲师', '刘助教', '陈教授', '赵老师', '杨老师', '黄老师'];
      
      // 学期列表
      const semesters = [
        '2020-2021-1', '2020-2021-2',
        '2021-2022-1', '2021-2022-2',
        '2022-2023-1', '2022-2023-2',
        '2023-2024-1', '2023-2024-2'
      ];
      
      // 评语列表
      const comments = [
        '学习态度认真，表现良好',
        '成绩优秀，继续保持',
        '有进步空间，建议加强练习',
        '基础知识扎实，动手能力强',
        '需要更加努力，按时完成作业',
        '上课积极参与，课后复习认真'
      ];
      
      // 生成成绩记录
      const grades = [];
      let id = 1;
      
      // 为每个学期分配课程
      semesters.forEach((semester, semesterIndex) => {
        // 根据学期索引选择适合的课程
        const coursesForSemester = semesterIndex < 3 ? 
          courses.slice(semesterIndex * 4, semesterIndex * 4 + 4) : 
          courses.slice(semesterIndex * 3, semesterIndex * 3 + 3);
        
        coursesForSemester.forEach(course => {
          // 随机决定成绩是否已发布（最新学期的课程可能未发布）
          const isPublished = semesterIndex < semesters.length - 1 || Math.random() > 0.3;
          
          // 生成最终成绩（60-100之间的随机数，少量不及格）
          const hasPassed = Math.random() > 0.1; // 90%通过率
          const baseScore = hasPassed ? 60 : 0;
          const scoreVariation = hasPassed ? 40 : 60;
          const finalScore = Math.round(baseScore + Math.random() * scoreVariation * 0.8);
          
          // 生成成绩详情（考试、作业、平时成绩等）
          const scoreDetails = isPublished ? [
            { name: '考试成绩', score: Math.round(finalScore * 0.7), weight: 70 },
            { name: '平时成绩', score: Math.round(80 + Math.random() * 20), weight: 30 }
          ] : [];
          
          // 生成发布时间（如果已发布）
          const publishTime = isPublished ? new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString() : null;
          
          grades.push({
            id: id++,
            courseCode: course.code,
            courseName: course.name,
            courseType: course.type,
            credits: course.credits,
            semester: semester,
            teacherName: teachers[Math.floor(Math.random() * teachers.length)],
            finalScore: finalScore,
            scoreDetails: scoreDetails,
            comment: isPublished && Math.random() > 0.3 ? comments[Math.floor(Math.random() * comments.length)] : null,
            status: isPublished ? 'published' : 'unpublished',
            statusText: isPublished ? '已发布' : '未发布',
            publishTime: publishTime
          });
        });
      });
      
      return grades.sort((a, b) => b.semester.localeCompare(a.semester));
    }
  }
}
</script>

<style scoped>
.student-grade-query {
  padding: 20px;
  background-color: var(--background-color);
  min-height: 100vh;
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

.filter-section {
  background-color: var(--background-color-light);
  padding: 16px;
  border-radius: 8px;
  box-shadow: var(--box-shadow-base);
  margin-bottom: 20px;
  border: 1px solid var(--border-light);
}

.content-section {
  background-color: var(--background-color-light);
  border-radius: 8px;
  box-shadow: var(--box-shadow-base);
  padding: 20px;
  min-height: 500px;
  border: 1px solid var(--border-light);
}

.main-tabs {
  margin-bottom: 20px;
}

.stats-card {
  border: none;
  transition: var(--transition-base);
  border-radius: 16px;
  border: 1px solid var(--border-light);
  background-color: var(--background-color-light);
  box-shadow: var(--box-shadow-base);
  overflow: hidden;
}

.stats-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--box-shadow-medium);
}

.stats-card.success .stat-number {
  color: var(--success-color);
}

.stats-card.danger .stat-number {
  color: var(--danger-color);
}

.stats-card.primary .stat-number {
  color: var(--primary-color);
}

.stats-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px;
}

.stat-icon {
  font-size: 36px;
  color: var(--primary-color);
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(59, 130, 246, 0.1);
  border-radius: 50%;
}

.stats-card.success .stat-icon {
  color: var(--success-color);
  background-color: rgba(16, 185, 129, 0.1);
}

.stats-card.danger .stat-icon {
  color: var(--danger-color);
  background-color: rgba(239, 68, 68, 0.1);
}

.stats-card.info .stat-icon {
  color: var(--info-color);
  background-color: rgba(59, 130, 246, 0.1);
}

.stat-info {
  flex: 1;
  margin-left: 24px;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 15px;
  color: var(--text-secondary);
  margin-top: 6px;
  font-weight: 500;
}

.chart-card {
  border-radius: 16px;
  border: 1px solid var(--border-light);
  background-color: var(--background-color-light);
  box-shadow: var(--box-shadow-base);
  transition: var(--transition-base);
}

.chart-card:hover {
  box-shadow: var(--box-shadow-medium);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 20px 24px;
  border-bottom: 2px solid var(--border-lighter);
}

.chart-container {
  padding: 24px;
  min-height: 300px;
}

.empty-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.download-card {
  border-radius: 16px;
  border: 1px solid var(--border-light);
  background-color: var(--background-color-light);
  box-shadow: var(--box-shadow-base);
  transition: var(--transition-base);
  padding: 24px;
}

.download-header h3 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-size: 20px;
}

.download-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-level {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.score-excellent {
  color: var(--success-color);
  font-weight: bold;
}

.score-good {
  color: var(--warning-color);
  font-weight: bold;
}

.score-medium {
  color: var(--info-color);
  font-weight: bold;
}

.score-pass {
  color: var(--text-secondary);
  font-weight: bold;
}

.score-fail {
  color: var(--danger-color);
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .grade-query {
    padding: 10px;
  }

  .stats-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .stat-info {
    margin-left: 0;
  }

  .filter-section {
    padding: 12px;
  }

  .content-section {
    padding: 12px;
  }

  .search-filters .el-row {
    flex-direction: column;
  }

  .search-filters .el-select,
  .search-filters .el-input,
  .search-filters .el-button {
    width: 100% !important;
    margin-bottom: 10px;
  }
}
</style>