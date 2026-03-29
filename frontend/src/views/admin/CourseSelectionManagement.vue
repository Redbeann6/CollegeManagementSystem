<template>
  <div class="course-selection-management">
    <el-card class="management-header">
      <h1>选课管理</h1>
    </el-card>
    
    <el-card class="management-content">
      <el-tabs v-model="activeTab">
      <!-- 选课设置 -->
      <el-tab-pane label="选课设置" name="settings">
        <el-card class="settings-card">
          <h3>当前学期选课设置</h3>
          <el-form :model="selectionSettings" :rules="settingsRules" ref="settingsForm" label-width="150px">
            <el-form-item label="学期名称" prop="semester">
              <el-input v-model="selectionSettings.semester" placeholder="请输入学期名称" />
            </el-form-item>
            <el-form-item label="选课开始时间" prop="startTime">
              <el-date-picker
                v-model="selectionSettings.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="选课结束时间" prop="endTime">
              <el-date-picker
                v-model="selectionSettings.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="补退选开始时间" prop="makeupStartTime">
              <el-date-picker
                v-model="selectionSettings.makeupStartTime"
                type="datetime"
                placeholder="选择补退选开始时间"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="补退选结束时间" prop="makeupEndTime">
              <el-date-picker
                v-model="selectionSettings.makeupEndTime"
                type="datetime"
                placeholder="选择补退选结束时间"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="选课状态" prop="status">
              <el-select v-model="selectionSettings.status" placeholder="请选择选课状态">
                <el-option label="未开始" value="before" />
                <el-option label="选课中" value="during" />
                <el-option label="补退选中" value="makeup" />
                <el-option label="已结束" value="after" />
              </el-select>
            </el-form-item>
            <el-form-item label="是否允许超选">
              <el-switch v-model="selectionSettings.allowOverSelect" />
            </el-form-item>
            <el-form-item label="每生最大选课数">
              <el-input-number v-model="selectionSettings.maxCoursesPerStudent" :min="1" :max="50" />
            </el-form-item>
          </el-form>
          <div class="settings-actions">
            <el-button type="primary" @click="saveSettings" size="small">保存设置</el-button>
            <el-button @click="resetSettings" size="small" style="margin-left: 10px;">重置</el-button>
          </div>
        </el-card>

        <el-card class="course-settings-card" style="margin-top: 20px;">
          <h3>课程选课设置</h3>
          <div class="search-filters mb-15">
            <el-input
              v-model="courseFilter.keyword"
              placeholder="搜索课程名称/编号"
              style="width: 300px;"
              @keyup.enter.native="searchCourses"
            >
              <el-button slot="append" icon="el-icon-search" @click="searchCourses"></el-button>
            </el-input>
            <el-button type="primary" @click="batchSetCourses" icon="el-icon-setting" size="small" style="margin-left: 10px;">批量设置</el-button>
          </div>
          <div class="table-container">
          <el-table :data="paginatedCourses" border style="width: 100%" @selection-change="handleCourseSelectionChange" class="hover-card">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="id" label="课程编号" width="120"></el-table-column>
            <el-table-column prop="name" label="课程名称" min-width="180"></el-table-column>
            <el-table-column label="教师" min-width="100">
              <template slot-scope="scope">
                {{ getTeacherName(scope.row.teacherId) }}
              </template>
            </el-table-column>
            <el-table-column prop="credits" label="学分" width="80"></el-table-column>
            <el-table-column prop="maxStudents" label="选课名额" width="100">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.maxStudents"
                  :min="0"
                  :max="200"
                  size="small"
                  @change="updateCourseMaxStudents(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="是否开放选课" width="120">
              <template slot-scope="scope">
                <el-switch
                  v-model="scope.row.isOpen"
                  @change="updateCourseStatus(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="420" fixed="right">
              <template slot-scope="scope">
                <el-button type="primary" size="small" @click="viewCourseStudents(scope.row)" icon="el-icon-view">查看学生</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
          <div class="pagination-container">
            <el-pagination
              background
              layout="prev, pager, next, jumper, sizes, total"
              :total="filteredCourses.length"
              :page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              v-model="currentPage"
              v-model:page-size="pageSize"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 选课审核 -->
      <el-tab-pane label="选课审核" name="approval">
        <el-card class="approval-card">
          <div class="search-filters mb-15">
            <el-select v-model="approvalFilter.courseId" placeholder="选择课程" clearable style="width: 180px;">
              <el-option label="全部课程" value=""></el-option>
              <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
            </el-select>
            <el-select v-model="approvalFilter.status" placeholder="选择状态" clearable style="width: 140px;">
              <el-option label="全部状态" value=""></el-option>
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-input
              v-model="approvalFilter.keyword"
              placeholder="搜索学生姓名/学号"
              style="width: 280px;"
              @keyup.enter.native="searchApplications"
            >
              <el-button slot="append" icon="el-icon-search" @click="searchApplications"></el-button>
            </el-input>
            <el-button type="primary" @click="batchApprove" icon="el-icon-check" size="small" style="margin-left: 10px;">批量通过</el-button>
            <el-button type="danger" @click="batchReject" icon="el-icon-close" size="small" style="margin-left: 10px;">批量拒绝</el-button>
          </div>
          <div class="table-container">
          <el-table :data="paginatedApplications" border style="width: 100%" @selection-change="handleApplicationSelectionChange" class="hover-card">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
            <el-table-column prop="studentName" label="学生姓名" width="100"></el-table-column>
            <el-table-column prop="courseId" label="课程编号" width="100"></el-table-column>
            <el-table-column label="课程名称" min-width="180">
              <template slot-scope="scope">
                {{ getCourseName(scope.row.courseId) }}
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="180">
              <template slot-scope="scope">
                {{ formatDate(scope.row.applyTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
            <el-table-column label="操作" width="420" fixed="right">
              <template slot-scope="scope">
                <el-button
                  type="primary" 
                  size="small" 
                  @click="approveApplication(scope.row)"
                  :disabled="scope.row.status !== 'pending'"
                  icon="el-icon-check"
                >
                  通过
                </el-button>
                <el-button
                  type="danger" 
                  size="small" 
                  @click="rejectApplication(scope.row)"
                  :disabled="scope.row.status !== 'pending'"
                  icon="el-icon-close"
                >
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
          <div class="pagination">
            <el-pagination
              background
              layout="prev, pager, next, jumper, sizes"
              :total="filteredApplications.length"
              :page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              v-model="currentPage"
              v-model:page-size="pageSize"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 选课统计 -->
      <el-tab-pane label="选课统计" name="stats">
        <el-card class="stats-card">
          <div class="stats-overview">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <div class="stats-item total-courses">
                  <div class="stats-value">{{ courses.length }}</div>
                  <div class="stats-label">总课程数</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <div class="stats-item total-students">
                  <div class="stats-value">{{ totalStudents }}</div>
                  <div class="stats-label">参与选课学生</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <div class="stats-item total-selections">
                  <div class="stats-value">{{ totalSelections }}</div>
                  <div class="stats-label">总选课人次</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="stats-content">
            <el-row :gutter="20">
              <el-col :xs="24" :md="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>选课情况分布</span>
                    </div>
                  </template>
                  <div class="chart-container">
                    <div v-for="course in topCourses" :key="course.id" class="course-stats-item">
                      <div class="course-name">{{ course.name }}</div>
                      <el-progress :percentage="course.selectionRate" :show-text="false">
                        <span class="progress-text">{{ course.selectedCount }}/{{ course.maxStudents }}</span>
                      </el-progress>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-card class="chart-card">
                  <template #header>
                    <div class="card-header">
                      <span>未选满课程统计</span>
                    </div>
                  </template>
                  <div class="chart-container">
                    <div v-for="(count, status) in courseStatusCount" :key="status" class="status-stats-item">
                      <span>{{ status }}:</span>
                      <el-progress type="circle" :percentage="(count / courses.length * 100).toFixed(1)" :width="80"></el-progress>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <div class="export-actions">
            <el-button type="primary" @click="exportStats">导出统计数据</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    </el-card>

    <!-- 学生列表对话框 -->
    <el-dialog
      :title="`${selectedCourse?.name} - 选课学生列表`"
      :visible.sync="studentListDialogVisible"
      width="800px"
      class="custom-dialog"
    >
      <div class="student-list-dialog">
        <el-input
          v-model="studentListFilter"
          placeholder="搜索学生姓名/学号"
          style="margin-bottom: 15px; width: 200px; margin-right: 10px;"
          @keyup.enter.native="searchStudentList"
        >
          <el-button slot="append" icon="el-icon-search" @click="searchStudentList"></el-button>
        </el-input>
        <el-select
          v-model="studentListMajorFilter"
          placeholder="按专业查询"
          style="margin-bottom: 15px; width: 180px; margin-right: 10px;"
          clearable
          @change="searchStudentList"
        >
          <el-option
            v-for="major in majors"
            :key="major"
            :label="major"
            :value="major"
          />
        </el-select>
        <el-button type="info" @click="clearStudentFilters" icon="el-icon-refresh" style="margin-bottom: 15px;">重置</el-button>
        <div class="table-container">
        <el-table :data="filteredCourseStudents" border style="width: 100%" class="hover-card">
          <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
          <el-table-column prop="name" label="姓名" width="100"></el-table-column>
          <el-table-column prop="className" label="班级" min-width="120"></el-table-column>
          <el-table-column prop="department" label="系部" min-width="150"></el-table-column>
          <el-table-column prop="selectionTime" label="选课时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.selectionTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
              <template slot-scope="scope">
                <el-button type="danger" size="small" @click="removeStudentFromCourse(scope.row)" icon="el-icon-delete">移除</el-button>
              </template>
            </el-table-column>
        </el-table>
      </div>
        <div class="dialog-pagination">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="filteredCourseStudents.length"
            :page-size="10"
            v-model="studentListPage"
            @current-change="handleStudentListPageChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 批量设置对话框 -->
    <el-dialog
      title="批量设置课程选课参数"
      :visible.sync="batchSetDialogVisible"
      width="600px"
      class="custom-dialog"
    >
      <el-form :model="batchSetForm" :rules="batchSetRules" ref="batchSetForm" label-width="120px">
        <el-form-item label="选课名额" prop="maxStudents">
          <el-input-number v-model="batchSetForm.maxStudents" :min="0" :max="200" />
        </el-form-item>
        <el-form-item label="开放选课">
          <el-radio-group v-model="batchSetForm.isOpen">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" style="justify-content: flex-end;">
        <el-button @click="batchSetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchSet" icon="el-icon-check" size="small">确认设置</el-button>
      </div>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      title="拒绝原因"
      :visible.sync="rejectReasonDialogVisible"
      width="400px"
      class="custom-dialog"
    >
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectForm" label-width="80px">
        <el-form-item label="原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" style="justify-content: flex-end;">
        <el-button @click="rejectReasonDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" icon="el-icon-close" size="small">确认拒绝</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CourseSelectionManagement',
  data() {
    return {
      activeTab: 'settings',
      selectionSettings: {
        semester: '2024-2025学年第一学期',
        startTime: '2024-09-01T08:00:00',
        endTime: '2024-09-10T20:00:00',
        makeupStartTime: '2024-09-15T08:00:00',
        makeupEndTime: '2024-09-20T20:00:00',
        status: 'during',
        allowOverSelect: false,
        maxCoursesPerStudent: 20
      },
      settingsRules: {
        semester: [{ required: true, message: '请输入学期名称', trigger: 'blur' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      },
      courses: [],
      teachers: [],
      students: [],
      courseStudents: [],
      courseFilter: {
        keyword: ''
      },
      approvalFilter: {
        courseId: '',
        status: '',
        keyword: ''
      },
      applications: [],
      selectedCourses: [],
      selectedApplications: [],
      selectedCourse: null,
      studentListDialogVisible: false,
      studentListFilter: '',
      studentListPage: 1,
      batchSetDialogVisible: false,
      batchSetForm: {
        maxStudents: 0,
        isOpen: true
      },
      batchSetRules: {
        maxStudents: [{ required: true, message: '请输入选课名额', trigger: 'blur' }]
      },
      rejectReasonDialogVisible: false,
      rejectForm: {
        reason: ''
      },
      rejectRules: {
        reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
      },
      currentPage: 1,
      pageSize: 10
    }
  },
  computed: {
    filteredCourses() {
      return this.courses.filter(course => {
        if (!this.courseFilter.keyword) return true
        const keyword = this.courseFilter.keyword.toLowerCase()
        return course.id.toLowerCase().includes(keyword) || course.name.toLowerCase().includes(keyword)
      })
    },
    paginatedCourses() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredCourses.slice(start, end)
    },
    filteredApplications() {
      return this.applications.filter(app => {
        if (this.approvalFilter.courseId && app.courseId !== this.approvalFilter.courseId) return false
        if (this.approvalFilter.status && app.status !== this.approvalFilter.status) return false
        if (this.approvalFilter.keyword) {
          const keyword = this.approvalFilter.keyword.toLowerCase()
          return app.studentId.toLowerCase().includes(keyword) || app.studentName.toLowerCase().includes(keyword)
        }
        return true
      })
    },
    paginatedApplications() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredApplications.slice(start, end)
    },
    filteredCourseStudents() {
      if (!this.selectedCourse) return []
      
      let students = this.courseStudents.filter(s => s.courseId === this.selectedCourse.id)
      
      if (this.studentListFilter) {
        const keyword = this.studentListFilter.toLowerCase()
        students = students.filter(s => 
          s.studentId.toLowerCase().includes(keyword) ||
          s.name.toLowerCase().includes(keyword)
        )
      }
      
      if (this.studentListMajorFilter) {
        students = students.filter(s => 
          s.department && s.department.toLowerCase().includes(this.studentListMajorFilter.toLowerCase())
        )
      }
      
      return students
    },
    totalStudents() {
      const studentIds = new Set(this.courseStudents.map(s => s.studentId))
      return studentIds.size
    },
    totalSelections() {
      return this.courseStudents.length
    },
    topCourses() {
      return this.courses
        .map(course => {
          const selectedCount = this.courseStudents.filter(s => s.courseId === course.id).length
          return {
            ...course,
            selectedCount,
            selectionRate: course.maxStudents > 0 ? (selectedCount / course.maxStudents * 100) : 0
          }
        })
        .sort((a, b) => b.selectionRate - a.selectionRate)
        .slice(0, 5)
    },
    courseStatusCount() {
      const count = {
        '已满': 0,
        '接近满额': 0,
        '正常': 0
      }
      
      this.courses.forEach(course => {
        const selectedCount = this.courseStudents.filter(s => s.courseId === course.id).length
        const rate = course.maxStudents > 0 ? (selectedCount / course.maxStudents * 100) : 0
        
        if (rate >= 100) {
          count['已满']++
        } else if (rate >= 80) {
          count['接近满额']++
        } else {
          count['正常']++
        }
      })
      
      return count
    }
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    loadInitialData() {
      this.loadTeachers()
      this.loadCourses()
      this.loadCourseStudents()
      this.loadApplications()
    },
    
    loadTeachers() {
      this.teachers = [
        { id: 1, name: '张教授', departmentId: 1 },
        { id: 2, name: '李副教授', departmentId: 1 },
        { id: 3, name: '王讲师', departmentId: 1 },
        { id: 4, name: '刘教授', departmentId: 2 },
        { id: 5, name: '陈副教授', departmentId: 1 },
        { id: 6, name: '杨讲师', departmentId: 1 },
        { id: 7, name: '赵教授', departmentId: 4 },
        { id: 8, name: '孙讲师', departmentId: 5 }
      ]
    },
    
    loadCourses() {
      this.courses = [
        {
          id: 'CS001',
          name: '高等数学',
          credits: 5,
          teacherId: 7,
          maxStudents: 100,
          isOpen: true
        },
        {
          id: 'CS002',
          name: '大学英语',
          credits: 4,
          teacherId: 8,
          maxStudents: 80,
          isOpen: true
        },
        {
          id: 'CS003',
          name: '程序设计基础',
          credits: 5,
          teacherId: 3,
          maxStudents: 60,
          isOpen: true
        },
        {
          id: 'CS004',
          name: '数据结构',
          credits: 4,
          teacherId: 2,
          maxStudents: 50,
          isOpen: true
        },
        {
          id: 'CS005',
          name: '计算机网络',
          credits: 4,
          teacherId: 4,
          maxStudents: 50,
          isOpen: true
        },
        {
          id: 'CS006',
          name: '操作系统',
          credits: 4,
          teacherId: 3,
          maxStudents: 45,
          isOpen: true
        },
        {
          id: 'CS007',
          name: '数据库原理',
          credits: 4,
          teacherId: 5,
          maxStudents: 50,
          isOpen: true
        },
        {
          id: 'CS008',
          name: '人工智能导论',
          credits: 3,
          teacherId: 1,
          maxStudents: 30,
          isOpen: true
        },
        {
          id: 'CS009',
          name: 'Web前端开发',
          credits: 3,
          teacherId: 6,
          maxStudents: 35,
          isOpen: true
        },
        {
          id: 'CS010',
          name: '算法分析与设计',
          credits: 3,
          teacherId: 1,
          maxStudents: 40,
          isOpen: true
        },
        {
          id: 'CS011',
          name: '编译原理',
          credits: 4,
          teacherId: 2,
          maxStudents: 30,
          isOpen: false
        },
        {
          id: 'CS012',
          name: '计算机图形学',
          credits: 3,
          teacherId: 5,
          maxStudents: 25,
          isOpen: true
        }
      ]
    },
    
    loadCourseStudents() {
      // 模拟选课学生数据
      this.courseStudents = [
        { studentId: '202401001', name: '张三', className: '计算机1班', department: '计算机科学与技术', courseId: 'CS003', selectionTime: '2024-09-01 08:30:00', status: '已确认' },
        { studentId: '202401002', name: '李四', className: '计算机1班', department: '计算机科学与技术', courseId: 'CS003', selectionTime: '2024-09-01 08:35:00', status: '已确认' },
        { studentId: '202401003', name: '王五', className: '计算机1班', department: '计算机科学与技术', courseId: 'CS003', selectionTime: '2024-09-01 08:40:00', status: '已确认' },
        { studentId: '202401004', name: '赵六', className: '计算机1班', department: '计算机科学与技术', courseId: 'CS004', selectionTime: '2024-09-01 08:45:00', status: '已确认' },
        { studentId: '202401005', name: '孙七', className: '计算机1班', department: '计算机科学与技术', courseId: 'CS004', selectionTime: '2024-09-01 08:50:00', status: '已确认' },
        { studentId: '202401006', name: '周八', className: '计算机1班', department: '软件工程', courseId: 'CS001', selectionTime: '2024-09-01 08:55:00', status: '已确认' },
        { studentId: '202401007', name: '吴九', className: '软件工程1班', department: '软件工程', courseId: 'CS001', selectionTime: '2024-09-01 09:00:00', status: '已确认' },
        { studentId: '202401008', name: '郑十', className: '电子信息1班', department: '电子信息工程', courseId: 'CS002', selectionTime: '2024-09-01 09:05:00', status: '已确认' },
        { studentId: '202401009', name: '钱一', className: '计算机2班', department: '计算机科学与技术', courseId: 'CS002', selectionTime: '2024-09-01 09:10:00', status: '已确认' },
        { studentId: '202401010', name: '孙二', className: '软件工程2班', department: '软件工程', courseId: 'CS005', selectionTime: '2024-09-01 09:15:00', status: '已确认' },
        { studentId: '202401011', name: '周十一', className: '计算机3班', department: '计算机科学与技术', courseId: 'CS006', selectionTime: '2024-09-01 09:20:00', status: '已确认' },
        { studentId: '202401012', name: '吴十二', className: '计算机4班', department: '计算机科学与技术', courseId: 'CS008', selectionTime: '2024-09-01 09:25:00', status: '已确认' },
        { studentId: '202401013', name: '郑十三', className: '软件工程3班', department: '软件工程', courseId: 'CS009', selectionTime: '2024-09-01 09:30:00', status: '已确认' },
        { studentId: '202401014', name: '钱十四', className: '计算机5班', department: '计算机科学与技术', courseId: 'CS010', selectionTime: '2024-09-01 09:35:00', status: '已确认' },
        { studentId: '202401015', name: '孙十五', className: '计算机6班', department: '计算机科学与技术', courseId: 'CS012', selectionTime: '2024-09-01 09:40:00', status: '已确认' },
        // 更多学生数据...
      ]
      
      // 初始化专业列表
      this.majors = [...new Set(this.courseStudents.map(s => s.department).filter(Boolean))]
    },
    
    loadApplications() {
      // 模拟选课申请数据
      this.applications = [
        { id: 1, studentId: '202401020', studentName: '李二十', courseId: 'CS007', applyTime: '2024-09-01 10:00:00', status: 'pending', remark: '' },
        { id: 2, studentId: '202401021', studentName: '王二一', courseId: 'CS007', applyTime: '2024-09-01 10:05:00', status: 'pending', remark: '' },
        { id: 3, studentId: '202401022', studentName: '赵二二', courseId: 'CS008', applyTime: '2024-09-01 10:10:00', status: 'approved', remark: '' },
        { id: 4, studentId: '202401023', studentName: '孙二三', courseId: 'CS008', applyTime: '2024-09-01 10:15:00', status: 'rejected', remark: '名额已满' },
        { id: 5, studentId: '202401024', studentName: '周二四', courseId: 'CS009', applyTime: '2024-09-01 10:20:00', status: 'pending', remark: '' }
      ]
    },
    
    getTeacherName(teacherId) {
      const teacher = this.teachers.find(t => t.id === teacherId)
      return teacher ? teacher.name : '未知教师'
    },
    
    getCourseName(courseId) {
      const course = this.courses.find(c => c.id === courseId)
      return course ? course.name : '未知课程'
    },
    
    getStatusType(status) {
      const typeMap = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger',
        '已确认': 'success',
        '待审核': 'warning',
        '已拒绝': 'danger'
      }
      return typeMap[status] || 'info'
    },
    
    getStatusText(status) {
      const textMap = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已拒绝'
      }
      return textMap[status] || status
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString()
    },
    
    saveSettings() {
      this.$refs.settingsForm.validate((valid) => {
        if (valid) {
          this.$message.success('选课设置保存成功！')
        }
      })
    },
    
    resetSettings() {
      this.$refs.settingsForm.resetFields()
    },
    
    searchCourses() {
      this.currentPage = 1
    },
    
    updateCourseMaxStudents(course) {
      this.$message.success(`已更新课程 ${course.name} 的选课名额为 ${course.maxStudents}`)
    },
    
    updateCourseStatus(course) {
      const status = course.isOpen ? '开放' : '关闭'
      this.$message.success(`已${status}课程 ${course.name} 的选课`)
    },
    
    viewCourseStudents(course) {
      this.selectedCourse = course
      this.studentListDialogVisible = true
    },
    
    removeStudentFromCourse(student) {
      this.$confirm('确定要将该学生从课程中移除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.courseStudents = this.courseStudents.filter(s => !(s.studentId === student.studentId && s.courseId === this.selectedCourse.id))
        this.$message.success('已移除该学生！')
      })
    },
    
    searchStudentList() {
      this.studentListPage = 1
    },
    
    clearStudentFilters() {
      this.studentListFilter = ''
      this.studentListMajorFilter = ''
    },
    
    handleStudentListPageChange(page) {
      this.studentListPage = page
    },
    
    batchSetCourses() {
      if (this.selectedCourses.length === 0) {
        this.$message.warning('请先选择要设置的课程！')
        return
      }
      this.batchSetDialogVisible = true
    },
    
    confirmBatchSet() {
      this.$refs.batchSetForm.validate((valid) => {
        if (valid) {
          this.selectedCourses.forEach(course => {
            const index = this.courses.findIndex(c => c.id === course.id)
            if (index !== -1) {
              this.courses[index].maxStudents = this.batchSetForm.maxStudents
              this.courses[index].isOpen = this.batchSetForm.isOpen
            }
          })
          this.batchSetDialogVisible = false
          this.selectedCourses = []
          this.$message.success(`已成功批量设置 ${this.selectedCourses.length} 门课程！`)
        }
      })
    },
    
    searchApplications() {
      this.currentPage = 1
    },
    
    approveApplication(application) {
      this.$confirm('确定要通过该选课申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        application.status = 'approved'
        this.$message.success('申请已通过！')
      })
    },
    
    rejectApplication(application) {
      this.currentRejectApplication = application
      this.rejectForm.reason = ''
      this.rejectReasonDialogVisible = true
    },
    
    confirmReject() {
      this.$refs.rejectForm.validate((valid) => {
        if (valid) {
          this.currentRejectApplication.status = 'rejected'
          this.currentRejectApplication.remark = this.rejectForm.reason
          this.rejectReasonDialogVisible = false
          this.$message.success('申请已拒绝！')
        }
      })
    },
    
    batchApprove() {
      if (this.selectedApplications.length === 0) {
        this.$message.warning('请先选择要通过的申请！')
        return
      }
      
      this.$confirm(`确定要批量通过 ${this.selectedApplications.length} 个选课申请吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.selectedApplications.forEach(app => {
          app.status = 'approved'
        })
        this.selectedApplications = []
        this.$message.success('批量通过成功！')
      })
    },
    
    batchReject() {
      if (this.selectedApplications.length === 0) {
        this.$message.warning('请先选择要拒绝的申请！')
        return
      }
      
      this.rejectForm.reason = ''
      this.currentRejectApplications = this.selectedApplications
      this.rejectReasonDialogVisible = true
    },
    
    handleCourseSelectionChange(selection) {
      this.selectedCourses = selection
    },
    
    handleApplicationSelectionChange(selection) {
      this.selectedApplications = selection
    },
    
    handlePageChange(page) {
      this.currentPage = page
    },
    
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    
    exportStats() {
      this.$message.success('统计数据导出成功！')
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  background-color: #ffffff;
  min-height: calc(100vh - 40px);
}

.settings-card,
.approval-card,
.stats-card,
.course-settings-card {
  margin-bottom: 20px;
  border-radius: 8px;
  border: 1px solid #000000;
  background-color: #ffffff;
  box-shadow: none;
}

.settings-card h3,
.course-settings-card h3 {
  margin: 0 0 20px 0;
  color: #000000;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #000000;
  padding-bottom: 10px;
}

.settings-actions {
  margin-top: 20px;
  text-align: right;
}

.search-filters {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 15px;
  border: 1px solid #000000;
  border-radius: 8px;
  background-color: #ffffff;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.dialog-pagination {
  text-align: right;
  margin-top: 15px;
}

.student-list-dialog {
  max-height: 500px;
  overflow-y: auto;
}

.progress-text {
  margin-left: 10px;
  font-size: 12px;
}

.stats-overview {
  margin-bottom: 30px;
}

.stats-item {
  background-color: #ffffff;
  border: 1px solid #000000;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  transition: transform 0.2s;
}

.stats-item:hover {
  transform: translateY(-2px);
}

.total-courses {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.total-students {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.total-selections {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.stats-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #606266;
}

.chart-card {
  margin-bottom: 20px;
  border-radius: 8px;
  border: 1px solid #000000;
  background-color: #ffffff;
  box-shadow: none;
}

.chart-container {
  padding: 10px;
}

.course-stats-item,
.course-item {
  margin-bottom: 12px;
}

.course-name {
  font-size: 14px;
  margin-bottom: 4px;
  font-weight: 500;
}

.status-stats-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.export-actions {
  margin-top: 20px;
  text-align: center;
}

.custom-dialog .el-dialog__header {
  border-bottom: 1px solid #000000;
  padding-bottom: 15px;
  background-color: #000000;
  color: #ffffff;
}

.custom-dialog .el-dialog__body {
  padding-top: 20px;
  padding-bottom: 20px;
  background-color: #ffffff;
}

.custom-dialog .el-dialog__footer {
  border-top: 1px solid #000000;
  padding-top: 15px;
  padding-bottom: 15px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  background-color: #ffffff;
}

@media (max-width: 768px) {
  .search-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-select,
  .el-input {
    width: 100% !important;
  }
  
  .settings-actions {
    text-align: center;
  }
  
  .stats-overview .el-col {
    margin-bottom: 15px;
  }
}

/* 表格样式 - 黑白配色 */
.table-container :deep(.el-table) {
  background-color: #ffffff;
  border: 1px solid #000000;
}

.hover-card .el-table__header-wrapper .el-table__header {
  background-color: #000000;
}

.hover-card .el-table__header-wrapper .el-table__header th {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 1px solid #ffffff;
}

.hover-card .el-table__body-wrapper .el-table__body {
  background-color: #ffffff;
}

.hover-card .el-table__body-wrapper .el-table__body tr {
  background-color: #ffffff;
}

.hover-card .el-table__body-wrapper .el-table__body tr:hover {
  background-color: #f5f5f5;
}

.hover-card .el-table__body-wrapper .el-table__body td {
  color: #000000;
  border-bottom: 1px solid #000000;
}

/* 选择框样式 */
.hover-card .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #000000;
  border-color: #000000;
}

.hover-card .el-checkbox__inner:hover {
  border-color: #000000;
}

/* 标签样式 */
.hover-card .el-tag {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.hover-card .el-tag--info {
  background-color: #f0f0f0;
}

.hover-card .el-tag--success {
  background-color: #f0f0f0;
}

.hover-card .el-tag--warning {
  background-color: #f0f0f0;
}

.hover-card .el-tag--danger {
  background-color: #f0f0f0;
}

/* 开关样式 */
.hover-card .el-switch__core {
  border: 1px solid #000000;
  background-color: #ffffff;
}

.hover-card .el-switch.is-checked .el-switch__core {
  background-color: #000000;
}

/* 按钮样式 */
.hover-card .el-button--primary {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.hover-card .el-button--danger {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.hover-card .el-button--danger:hover {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.hover-card .el-button--success {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.hover-card .el-button--success:hover {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.hover-card .el-button--warning {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.hover-card .el-button--warning:hover {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

/* 输入框样式 */
.hover-card .el-input__inner {
  border: 1px solid #000000;
  background-color: #ffffff;
  color: #000000;
}

.hover-card .el-input__inner:focus {
  border-color: #000000;
}

.hover-card .el-select .el-input__inner {
  border: 1px solid #000000;
}

/* 分页样式 */
.hover-card .el-pagination .btn-next,
.hover-card .el-pagination .btn-prev,
.hover-card .el-pagination .el-pager li {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
}

.hover-card .el-pagination .el-pager li.active {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.hover-card .el-pagination .el-pagination__total {
  color: #000000;
}

.hover-card .el-pagination .el-pagination__jump {
  color: #000000;
}

.hover-card .el-pagination .el-pagination__sizes .el-input__inner {
  border: 1px solid #000000;
}

/* 按钮样式优化 */
.el-button {
  border-radius: 4px;
}

.el-button + .el-button {
  margin-left: 10px;
}

/* 输入框样式优化 */
.el-input,
.el-select {
  border-radius: 4px;
}

/* 卡片样式优化 */
.management-content :deep(.el-card__header) {
  border-bottom: 1px solid #000000;
}

.management-content :deep(.el-card) {
  border: 1px solid #000000;
}
</style>
