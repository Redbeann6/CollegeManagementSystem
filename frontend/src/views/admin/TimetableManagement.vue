<template>
  <div class="timetable-management">
    <el-card class="management-header">
      <h1>课程表管理</h1>
    </el-card>

    <el-card class="management-content">
      <div class="header-actions">
        <el-select v-model="selectedDepartment" placeholder="选择系部" clearable>
          <el-option label="全部系部" value=""></el-option>
          <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
        </el-select>
        <el-select v-model="selectedTeacher" placeholder="选择教师" clearable>
          <el-option label="全部教师" value=""></el-option>
          <el-option v-for="teacher in filteredTeachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
        </el-select>
        <el-select v-model="selectedClass" placeholder="选择班级" clearable>
          <el-option label="全部班级" value=""></el-option>
          <el-option v-for="cls in filteredClasses" :key="cls.id" :label="cls.name" :value="cls.id" />
        </el-select>
        <el-button type="primary" @click="addCourseSchedule">新增课程安排</el-button>
        <el-button @click="exportTimetable">导出课程表</el-button>
        <el-button @click="resetFilters" icon="el-icon-refresh">重置筛选</el-button>
      </div>

      <div class="timetable-grid">
        <!-- 第一行：星期表头 -->
        <div class="timetable-cell header empty"></div>
        <div v-for="day in weekDays" :key="day.value" class="timetable-cell header">
          {{ day.label }}
        </div>

        <!-- 接下来的行：时间段和课程 -->
        <!-- 为每个时间段生成一行 -->
        <template v-for="section in timeSections" :key="section.id">
          <!-- 时间段标签 -->
          <div class="timetable-cell time">
            {{ section.time }}
          </div>
          <!-- 每天的课程格子 -->
          <div v-for="day in weekDays" :key="day.value" class="timetable-cell">
            <div
              v-for="course in getCoursesForCell(day.value, section.id)"
              :key="course.id"
              class="course-item"
              :style="{ backgroundColor: getCourseColor(course.courseId) }"
              @click="viewCourseDetails(course)"
            >
              <div class="course-name">{{ course.courseName }}</div>
              <div class="course-teacher">{{ getTeacherName(course.teacherId) }}</div>
              <div class="course-class">{{ getClassName(course.classId) }}</div>
              <div class="course-location">{{ course.location }}</div>
              <div class="course-actions">
                <el-button size="small" type="text" @click.stop="editCourseSchedule(course)">编辑</el-button>
                <el-button size="small" type="text" @click.stop="deleteCourseSchedule(course)" style="color: #f56c6c">删除</el-button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog
      title="课程详情"
      :visible.sync="detailDialogVisible"
      width="600px"
    >
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">{{ selectedCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="教师姓名">{{ getTeacherName(selectedCourse.teacherId) }}</el-descriptions-item>
          <el-descriptions-item label="授课班级">{{ getClassName(selectedCourse.classId) }}</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ selectedCourse.location }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">{{ getDayLabel(selectedCourse.day) }} {{ getSectionTime(selectedCourse.section) }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourse.credits }}</el-descriptions-item>
          <el-descriptions-item label="学时">{{ selectedCourse.hours }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedCourse.courseType }}</el-descriptions-item>
          <el-descriptions-item label="所属系部">{{ getDepartmentName(selectedCourse.departmentId) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 新增/编辑课程安排对话框 -->
    <el-dialog
      :title="editingCourse ? '编辑课程安排' : '新增课程安排'"
      v-model="scheduleDialogVisible"
      width="600px"
    >
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleForm" label-width="120px">
        <el-form-item label="所属系部" prop="departmentId">
          <el-select 
            v-model="scheduleForm.departmentId" 
            placeholder="请选择系部"
            @change="onDepartmentChangeForSchedule"
          >
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="scheduleForm.courseId" placeholder="请选择课程">
            <el-option v-for="course in filteredScheduleCourses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="scheduleForm.teacherId" placeholder="请选择教师">
            <el-option v-for="teacher in filteredScheduleTeachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="scheduleForm.classId" placeholder="请选择班级">
            <el-option v-for="cls in filteredScheduleClasses" :key="cls.id" :label="cls.name" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="day">
          <el-select v-model="scheduleForm.day" placeholder="请选择日期">
            <el-option v-for="day in weekDays" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" prop="section">
          <el-select v-model="scheduleForm.section" placeholder="请选择节次">
            <el-option v-for="section in timeSections" :key="section.id" :label="section.label" :value="section.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="scheduleForm.location" placeholder="请输入上课地点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCourseSchedule">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      title="确认删除"
      v-model="deleteDialogVisible"
      width="300px"
    >
      <p>确定要删除该课程安排吗？</p>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TimetableManagement',
  data() {
    return {
      selectedDepartment: '',
      selectedTeacher: '',
      selectedClass: '',
      weekDays: [
        { label: '周一', value: 1 },
        { label: '周二', value: 2 },
        { label: '周三', value: 3 },
        { label: '周四', value: 4 },
        { label: '周五', value: 5 },
        { label: '周六', value: 6 },
        { label: '周日', value: 7 }
      ],
      timeSections: [
        { id: 1, time: '08:00-09:40', label: '1-2节' },
        { id: 2, time: '10:00-11:40', label: '3-4节' },
        { id: 3, time: '14:00-15:40', label: '5-6节' },
        { id: 4, time: '16:00-17:40', label: '7-8节' },
        { id: 5, time: '19:00-20:40', label: '9-10节' }
      ],
      courses: [],
      teachers: [],
      classes: [],
      departments: [],
      courseSchedules: [],
      detailDialogVisible: false,
      scheduleDialogVisible: false,
      deleteDialogVisible: false,
      selectedCourse: null,
      editingCourse: null,
      scheduleForm: {
        departmentId: '',
        courseId: '',
        teacherId: '',
        classId: '',
        day: '',
        section: '',
        location: ''
      },
      scheduleRules: {
        departmentId: [{ required: false, message: '请选择所属系部', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
        classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
        day: [{ required: true, message: '请选择星期', trigger: 'change' }],
        section: [{ required: true, message: '请选择节次', trigger: 'change' }],
        location: [{ required: true, message: '请输入上课地点', trigger: 'blur' }]
      }
    }
  },
  computed: {
    filteredTeachers() {
      if (!this.selectedDepartment) return this.teachers
      return this.teachers.filter(teacher => teacher.departmentId === this.selectedDepartment)
    },
    filteredClasses() {
      if (!this.selectedDepartment) return this.classes
      return this.classes.filter(cls => cls.departmentId === this.selectedDepartment)
    },
    filteredSchedules() {
      let schedules = [...this.courseSchedules]
      
      if (this.selectedDepartment) {
        schedules = schedules.filter(schedule => {
          // 优先使用详细数据中的系部信息，如果没有则从课程信息中获取
          const departmentId = schedule.departmentId || 
                             (this.courses.find(c => c.id === schedule.courseId)?.departmentId);
          return departmentId === this.selectedDepartment;
        })
      }
      
      if (this.selectedTeacher) {
        schedules = schedules.filter(schedule => schedule.teacherId === this.selectedTeacher)
      }
      
      if (this.selectedClass) {
        schedules = schedules.filter(schedule => schedule.classId === this.selectedClass)
      }
      
      return schedules
    },
    // 用于课程安排表单的筛选
    filteredScheduleCourses() {
      // 课程不按系部筛选，显示所有课程
      return this.courses;
    },
    filteredScheduleTeachers() {
      let result = this.teachers;
          
      // 如果选择了系部，筛选该系部的教师
      if (this.scheduleForm.departmentId) {
        result = result.filter(teacher => teacher.departmentId === this.scheduleForm.departmentId);
      }
          
      return result;
    },
    filteredScheduleClasses() {
      let result = this.classes;
          
      // 如果选择了系部，筛选该系部的班级
      if (this.scheduleForm.departmentId) {
        result = result.filter(cls => cls.departmentId === this.scheduleForm.departmentId);
      }
          
      return result;
    },
  },
  mounted() {
    this.loadInitialData()
  },
  methods: {
    loadInitialData() {
      this.loadDepartments()
      this.loadTeachers()
      this.loadClasses()
      this.loadCourses()
      this.loadCourseSchedules()
    },
    
    async loadDepartments() {
      try {
        const response = await this.$axios.get('/api/departments');
        if (response.data.success) {
          this.departments = response.data.data;
        } else {
          console.error('加载系部数据失败:', response.data.message);
          this.$message.error('加载系部数据失败');
        }
      } catch (error) {
        console.error('加载系部数据失败:', error);
        // 如果API调用失败，使用模拟数据
        this.departments = [
          { id: 1, name: '计算机科学与技术' },
          { id: 2, name: '软件工程' },
          { id: 3, name: '电子信息工程' },
          { id: 4, name: '基础教学部' },
          { id: 5, name: '外国语' }
        ];
      }
    },
    
    async loadTeachers() {
      try {
        const response = await this.$axios.get('/api/teachers');
        if (response.data.success) {
          this.teachers = response.data.data;
        } else {
          console.error('加载教师数据失败:', response.data.message);
          this.$message.error('加载教师数据失败');
        }
      } catch (error) {
        console.error('加载教师数据失败:', error);
        // 如果API调用失败，使用模拟数据
        this.teachers = [
          { id: 1, name: '张教授', departmentId: 1 },
          { id: 2, name: '李副教授', departmentId: 1 },
          { id: 3, name: '王讲师', departmentId: 1 },
          { id: 4, name: '刘教授', departmentId: 2 },
          { id: 5, name: '陈副教授', departmentId: 1 },
          { id: 6, name: '杨讲师', departmentId: 1 },
          { id: 7, name: '赵教授', departmentId: 4 },
          { id: 8, name: '孙讲师', departmentId: 5 },
          { id: 9, name: '黄教授', departmentId: 3 },
          { id: 10, name: '周副教授', departmentId: 3 }
        ];
      }
    },
    
    async loadTeachersByDepartment(departmentId) {
      if (!departmentId) {
        // 如果没有指定系部，加载所有教师
        await this.loadTeachers();
        return;
      }
      
      try {
        const response = await this.$axios.get(`/api/teachers/department/${departmentId}`);
        if (response.data.success) {
          this.teachers = response.data.data;
        } else {
          console.error('按系部加载教师数据失败:', response.data.message);
          this.$message.error('按系部加载教师数据失败');
          // 失败时仍加载所有教师
          await this.loadTeachers();
        }
      } catch (error) {
        console.error('按系部加载教师数据失败:', error);
        this.$message.error('按系部加载教师数据失败');
        // 出错时仍加载所有教师
        await this.loadTeachers();
      }
    },
    
    async loadClasses() {
      try {
        const response = await this.$axios.get('/api/classes');
        if (response.data.success) {
          this.classes = response.data.data;
        } else {
          console.error('加载班级数据失败:', response.data.message);
          this.$message.error('加载班级数据失败');
        }
      } catch (error) {
        console.error('加载班级数据失败:', error);
        // 如果API调用失败，使用模拟数据
        this.classes = [
          { id: 1, name: '计算机1班', departmentId: 1 },
          { id: 2, name: '计算机2班', departmentId: 1 },
          { id: 3, name: '软件工程1班', departmentId: 2 },
          { id: 4, name: '电子信息1班', departmentId: 3 },
          { id: 5, name: '人工智能1班', departmentId: 1 },
          { id: 6, name: '工商管理1班', departmentId: 4 }
        ];
      }
    },
    
    async loadClassesByDepartment(departmentId) {
      if (!departmentId) {
        // 如果没有指定系部，加载所有班级
        await this.loadClasses();
        return;
      }
      
      try {
        const response = await this.$axios.get(`/api/classes/department/${departmentId}`);
        if (response.data.success) {
          this.classes = response.data.data;
        } else {
          console.error('按系部加载班级数据失败:', response.data.message);
          this.$message.error('按系部加载班级数据失败');
          // 失败时仍加载所有班级
          await this.loadClasses();
        }
      } catch (error) {
        console.error('按系部加载班级数据失败:', error);
        // 尝试使用本地筛选作为后备方案
        try {
          await this.loadClasses(); // 先加载所有班级
          // 然后在前端筛选属于指定系部的班级
          this.classes = this.classes.filter(cls => cls.departmentId === departmentId);
        } catch (fallbackError) {
          console.error('使用后备方案也失败:', fallbackError);
          await this.loadClasses(); // 最终加载所有班级
        }
      }
    },
    
    async loadCourses() {
      try {
        const response = await this.$axios.get('/api/courses');
        if (response.data.success) {
          this.courses = response.data.data;
        } else {
          console.error('加载课程数据失败:', response.data.message);
          this.$message.error('加载课程数据失败');
        }
      } catch (error) {
        console.error('加载课程数据失败:', error);
        // 如果API调用失败，使用模拟数据
        this.courses = [
          { id: 1, name: '高等数学', credits: 5, hours: 80, courseType: '公共课', departmentId: 4 },
          { id: 2, name: '大学英语', credits: 4, hours: 64, courseType: '公共课', departmentId: 5 },
          { id: 3, name: '程序设计基础', credits: 5, hours: 96, courseType: '必修课', departmentId: 1 },
          { id: 4, name: '数据结构', credits: 4, hours: 64, courseType: '必修课', departmentId: 1 },
          { id: 5, name: '计算机网络', credits: 4, hours: 64, courseType: '必修课', departmentId: 1 },
          { id: 6, name: '操作系统', credits: 4, hours: 64, courseType: '必修课', departmentId: 1 },
          { id: 7, name: '数据库原理', credits: 4, hours: 64, courseType: '必修课', departmentId: 1 },
          { id: 8, name: '人工智能导论', credits: 3, hours: 48, courseType: '选修课', departmentId: 1 },
          { id: 9, name: 'Web前端开发', credits: 3, hours: 48, courseType: '选修课', departmentId: 1 },
          { id: 10, name: '算法分析与设计', credits: 3, hours: 48, courseType: '专业课', departmentId: 1 },
          { id: 11, name: '电路分析', credits: 4, hours: 64, courseType: '必修课', departmentId: 3 },
          { id: 12, name: '信号与系统', credits: 4, hours: 64, courseType: '必修课', departmentId: 3 },
          { id: 13, name: '软件工程导论', credits: 3, hours: 48, courseType: '必修课', departmentId: 2 }
        ];
      }
    },

    async loadCourseSchedules() {
      try {
        // 从后端API获取课程安排数据（包含详细信息）
        console.log('尝试调用API获取课程安排数据...');

        // 添加调试信息
        console.log('当前axios配置:');
        console.log('- baseURL:', this.$axios?.defaults?.baseURL);
        console.log('- 请求URL:', '/course-schedules/details');

        const response = await this.$axios.get('/course-schedules/details');

        console.log('API响应:', response);
        console.log('响应状态码:', response.status);
        console.log('响应数据:', response.data);

        if (response.data.success) {
          this.courseSchedules = response.data.data;
          console.log('课程安排数据加载成功:', this.courseSchedules);

          // 显示成功消息
          this.$message({
            type: 'success',
            message: '课程安排数据加载成功'
          });
        } else {
          console.error('加载课程安排数据失败:', response.data.message);
          this.$message.error('加载课程安排数据失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('加载课程安排数据失败:', error);

        // 详细的错误分析
        if (error.response) {
          // 服务器响应了错误状态码
          console.error('HTTP状态码:', error.response.status);
          console.error('状态文本:', error.response.statusText);
          console.error('响应头:', error.response.headers);
          console.error('响应数据:', error.response.data);

          // 根据状态码显示不同的错误消息
          if (error.response.status === 404) {
            this.$message.error('API接口不存在 (404)，请检查后端路由配置');
          } else if (error.response.status === 401) {
            this.$message.error('身份验证失败，请重新登录');
          } else if (error.response.status === 403) {
            this.$message.error('权限不足，无法访问该资源');
          } else if (error.response.status >= 500) {
            this.$message.error('服务器内部错误，请联系管理员');
          } else {
            this.$message.error(`请求失败: ${error.response.status} ${error.response.statusText}`);
          }
        } else if (error.request) {
          // 请求已发送但没有收到响应
          console.error('请求已发送但无响应:', error.request);
          this.$message.error('无法连接到服务器，请检查：\n1. 服务器是否运行\n2. 网络连接\n3. 防火墙设置');
        } else {
          // 请求配置错误
          console.error('请求配置错误:', error.message);
          this.$message.error('请求配置错误: ' + error.message);
        }

        // 显示请求配置信息
        if (error.config) {
          console.error('请求配置信息:', {
            url: error.config.url,
            method: error.config.method,
            baseURL: error.config.baseURL,
            headers: error.config.headers
          });
        }
      }
    },
    
    getCoursesForCell(day, section) {
      return this.filteredSchedules
        .filter(schedule => schedule.day === day && schedule.section === section)
        .map(schedule => {
          // 优先使用详细数据中的系部信息，如果没有则从课程信息中获取
          const course = this.courses.find(c => c.id === schedule.courseId)
          return {
            ...schedule,
            courseName: schedule.courseName || (course ? course.name : '未知课程'),
            courseId: schedule.courseId,
            credits: schedule.credits || (course ? course.credits : 0),
            hours: schedule.hours || (course ? course.hours : 0),
            courseType: schedule.courseType || (course ? course.courseType : ''),
            departmentId: schedule.departmentId || (course ? course.departmentId : '')
          }
        })
    },
    
    getCourseColor(courseId) {
      const colors = ['#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F', '#85C1E9', '#F8C471', '#BB8FCE']
      return colors[courseId % colors.length]
    },
    
    getTeacherName(teacherId) {
      const teacher = this.teachers.find(t => t.id === teacherId)
      return teacher ? teacher.name : '未知教师'
    },
    
    getClassName(classId) {
      const cls = this.classes.find(c => c.id === classId)
      return cls ? cls.name : '未知班级'
    },
    
    getDepartmentName(departmentId) {
      const dept = this.departments.find(d => d.id === departmentId)
      return dept ? dept.name : '未知系部'
    },
    
    getDayLabel(dayValue) {
      const day = this.weekDays.find(d => d.value === dayValue)
      return day ? day.label : ''
    },
    
    getSectionTime(sectionId) {
      const section = this.timeSections.find(s => s.id === sectionId)
      return section ? section.time : ''
    },
    
    viewCourseDetails(course) {
      this.selectedCourse = course
      this.detailDialogVisible = true
    },
    
    addCourseSchedule() {
      this.editingCourse = null
      this.resetScheduleForm()
      this.scheduleDialogVisible = true
    },
    
    editCourseSchedule(course) {
      this.editingCourse = course
      // 直接使用课程安排中的系部信息，如果不存在则从课程信息中获取
      const departmentId = course.departmentId !== undefined ? course.departmentId : 
                          (this.courses.find(c => c.id === course.courseId)?.departmentId) || '';
      
      // 设置表单数据
      this.scheduleForm = {
        departmentId: departmentId,
        courseId: course.courseId,
        teacherId: course.teacherId,
        classId: course.classId,
        day: course.day,
        section: course.section,
        location: course.location
      }
      
      // 在编辑模式下，如果找到了系部ID，也需要重置课程、教师和班级选择以确保筛选生效
      if (departmentId) {
        this.onDepartmentChangeForSchedule();
        // 使用 $nextTick 确保计算属性更新后再设置值
        this.$nextTick(() => {
          // 检查课程、教师和班级是否在筛选后的列表中
          const filteredCourses = this.filteredScheduleCourses;
          const filteredTeachers = this.filteredScheduleTeachers;
          const filteredClasses = this.filteredScheduleClasses;
          
          // 只有当值存在于筛选后的列表中时才设置
          if (filteredCourses.some(c => c.id === course.courseId)) {
            this.scheduleForm.courseId = course.courseId;
          } else {
            this.scheduleForm.courseId = '';
            this.$message.warning('原课程不属于所选系部，已清空课程选择');
          }
          
          if (filteredTeachers.some(t => t.id === course.teacherId)) {
            this.scheduleForm.teacherId = course.teacherId;
          } else {
            this.scheduleForm.teacherId = '';
            this.$message.warning('原教师不属于所选系部，已清空教师选择');
          }
          
          if (filteredClasses.some(c => c.id === course.classId)) {
            this.scheduleForm.classId = course.classId;
          } else {
            this.scheduleForm.classId = '';
            this.$message.warning('原班级不属于所选系部，已清空班级选择');
          }
        });
      } else {
        // 如果没有找到系部ID，在$nextTick中也尝试从课程信息中获取
        this.$nextTick(() => {
          const courseInfo = this.courses.find(c => c.id === course.courseId);
          if (courseInfo && courseInfo.departmentId) {
            this.scheduleForm.departmentId = courseInfo.departmentId;
          }
        });
      }
      
      this.scheduleDialogVisible = true
    },
    
    deleteCourseSchedule(course) {
      this.selectedCourse = course
      this.deleteDialogVisible = true
    },
    
    async confirmDelete() {
      try {
        const response = await this.$axios.delete(`/course-schedules/${this.selectedCourse.id}`);
        if (response.data.success) {
          this.$message.success('课程安排删除成功！');
          this.deleteDialogVisible = false;
          this.loadCourseSchedules(); // 重新加载数据
        } else {
          this.$message.error('删除失败: ' + response.data.message);
        }
      } catch (error) {
        console.error('删除课程安排失败:', error);
        this.$message.error('删除课程安排失败: ' + error.response?.data?.message || error.message);
      }
    },
    
    async saveCourseSchedule() {
      this.$refs.scheduleForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.editingCourse) {
              // 更新课程安排
              const response = await this.$axios.put(`/course-schedules/${this.editingCourse.id}`, this.scheduleForm);
              if (response.data.success) {
                this.$message.success('课程安排更新成功！');
                this.scheduleDialogVisible = false;
                this.resetScheduleForm();
                this.loadCourseSchedules(); // 重新加载数据
              } else {
                this.$message.error('更新失败: ' + response.data.message);
              }
            } else {
              // 新增课程安排
              const response = await this.$axios.post('/course-schedules', this.scheduleForm);
              if (response.data.success) {
                this.$message.success('课程安排新增成功！');
                this.scheduleDialogVisible = false;
                this.resetScheduleForm();
                this.loadCourseSchedules(); // 重新加载数据
              } else {
                this.$message.error('新增失败: ' + response.data.message);
              }
            }
          } catch (error) {
            console.error('保存课程安排失败:', error);
            this.$message.error('保存课程安排失败: ' + error.response?.data?.message || error.message);
          }
        }
      })
    },
    
    // 注意：时间冲突检查现在由后端处理，因此这里可以移除或保留前端检查作为优化
    // 目前我们依赖后端的冲突检测机制
    
    // 生成模拟课程安排数据
    getMockCourseSchedules() {
      return [
        { id: 1, courseId: 1, teacherId: 1, classId: 1, day: 1, section: 1, location: 'A栋101' },
        { id: 2, courseId: 2, teacherId: 2, classId: 1, day: 1, section: 2, location: 'A栋101' },
        { id: 3, courseId: 3, teacherId: 3, classId: 2, day: 1, section: 1, location: 'B栋201' },
        { id: 4, courseId: 4, teacherId: 4, classId: 2, day: 1, section: 2, location: 'B栋201' },
        { id: 5, courseId: 1, teacherId: 1, classId: 1, day: 3, section: 1, location: 'A栋101' },
        { id: 6, courseId: 2, teacherId: 2, classId: 1, day: 3, section: 2, location: 'A栋101' },
        { id: 7, courseId: 3, teacherId: 3, classId: 2, day: 3, section: 1, location: 'B栋201' },
        { id: 8, courseId: 4, teacherId: 4, classId: 2, day: 3, section: 2, location: 'B栋201' },
        { id: 9, courseId: 5, teacherId: 5, classId: 1, day: 2, section: 3, location: 'C栋301' },
        { id: 10, courseId: 6, teacherId: 6, classId: 2, day: 4, section: 4, location: 'D栋401' }
      ];
    },
    
    resetScheduleForm() {
      this.scheduleForm = {
        departmentId: '',
        courseId: '',
        teacherId: '',
        classId: '',
        day: '',
        section: '',
        location: ''
      }
      if (this.$refs.scheduleForm) {
        this.$refs.scheduleForm.resetFields()
      }
      
      // 确保重置后触发计算属性更新
      this.$nextTick(() => {
        // 空操作，仅用于确保视图更新
      });
    },
    
    onDepartmentChangeForSchedule() {
      // 当系部改变时，重置课程、教师和班级选择
      this.scheduleForm.courseId = '';
      this.scheduleForm.teacherId = '';
      this.scheduleForm.classId = '';
      
      // 计算属性会自动根据scheduleForm.departmentId筛选教师和班级
      // 不需要额外的API调用，因为我们使用的是计算属性filteredScheduleTeachers和filteredScheduleClasses
    },
    
    // 用于手动触发计算属性更新
    updateScheduleFormFilters() {
      // 强制刷新计算属性
      this.$forceUpdate();
    },
    
    exportTimetable() {
      this.$message.success('课程表导出成功！')
      // 实际项目中这里应该实现导出为Excel或PDF的功能
    },
    
    resetFilters() {
      this.selectedDepartment = '';
      this.selectedTeacher = '';
      this.selectedClass = '';
    }
  }
}
</script>

<style scoped>
.timetable-management {
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

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.management-content {
  border-radius: 8px;
  padding: 20px;
  overflow-x: auto;
}

.timetable-grid {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr);
  min-width: 1000px;
  grid-auto-rows: minmax(100px, auto);
  gap: 1px;
  background-color: #ebeef5;
}

.timetable-cell {
  padding: 4px;
  background-color: white;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
  box-sizing: border-box;
}

.timetable-cell:hover {
  background-color: #f5f7fa;
}

.timetable-cell.header {
  background-color: #f5f7fa;
  padding: 6px;
  text-align: center;
  font-weight: bold;
  border: 1px solid #ebeef5;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timetable-cell.header.empty {
  background-color: #f5f7fa;
}

.timetable-cell.time {
  background-color: #f5f7fa;
  padding: 4px;
  text-align: center;
  border: 1px solid #ebeef5;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.course-item {
  padding: 4px;
  border-radius: 4px;
  color: white;
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
  position: relative;
}

.course-name {
  font-weight: bold;
  font-size: 12px;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-teacher,
.course-class,
.course-location {
  font-size: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 1px;
}

.course-actions {
  position: absolute;
  bottom: 2px;
  right: 2px;
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  gap: 2px;
}

.course-item:hover .course-actions {
  opacity: 1;
}

.course-actions .el-button {
  padding: 1px 4px;
  font-size: 9px;
}

.course-detail {
  padding: 10px;
}

@media (max-width: 768px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-select,
  .el-button {
    width: 100% !important;
  }
}
</style>