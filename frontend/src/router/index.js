import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      requiresAuth: false,
      title: '登录'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      requiresAuth: false,
      title: '注册'
    }
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ADMIN'],
      title: '管理员控制台'
    },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: () => import('../views/admin/StudentManagement.vue'),
        meta: { title: '学生管理' }
      },
      {
        path: 'teachers',
        name: 'AdminTeachers',
        component: () => import('../views/admin/TeacherManagement.vue'),
        meta: { title: '教师管理' }
      },
      {
        path: 'departments',
        name: 'AdminDepartments',
        component: () => import('../views/admin/DepartmentManagement.vue'),
        meta: { title: '院系管理' }
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('../views/admin/CourseManagement.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'enrollments',
        name: 'AdminEnrollments',
        component: () => import('../views/admin/EnrollmentManagement.vue'),
        meta: { title: '选课管理' }
      },
      {
        path: 'timetable',
        name: 'AdminTimetable',
        component: () => import('../views/admin/TimetableManagement.vue'),
        meta: { title: '课表管理' }
      },
      {
        path: 'exams',
        name: 'AdminExams',
        component: () => import('../views/admin/ExamManagement.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'grades',
        name: 'AdminGrades',
        component: () => import('../views/admin/GradeManagement.vue'),
        meta: { title: '成绩管理' }
      },
      {
        path: 'leave',
        name: 'AdminLeaveRequests',
        component: () => import('../views/admin/LeaveRequestManagement.vue'),
        meta: { title: '请假管理' }
      },
      {
        path: 'notifications',
        name: 'AdminNotifications',
        component: () => import('../views/admin/NotificationManagement.vue'),
        meta: { title: '通知管理' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('../views/admin/Settings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  },
  // 教师路由
  {
    path: '/teacher',
    component: () => import('../layouts/TeacherLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['TEACHER', 'ADMIN'],
      title: '教师工作台'
    },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/teacher/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'courses',
        name: 'TeacherCourses',
        component: () => import('../views/teacher/CourseManagement.vue'),
        meta: { title: '我的课程' }
      },
      {
        path: 'timetable',
        name: 'TeacherTimetable',
        component: () => import('../views/teacher/Timetable.vue'),
        meta: { title: '我的课表' }
      },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: () => import('../views/teacher/StudentManagement.vue'),
        meta: { title: '学生管理' }
      },
      {
        path: 'exams',
        name: 'TeacherExams',
        component: () => import('../views/teacher/ExamManagement.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'grades',
        name: 'TeacherGrades',
        component: () => import('../views/teacher/GradeManagement.vue'),
        meta: { title: '成绩录入' }
      },
      {
        path: 'leave',
        name: 'TeacherLeaveRequests',
        component: () => import('../views/teacher/LeaveRequestManagement.vue'),
        meta: { title: '请假审批' }
      },
      {
        path: 'notifications',
        name: 'TeacherNotifications',
        component: () => import('../views/teacher/NotificationManagement.vue'),
        meta: { title: '通知管理' }
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('../views/teacher/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  },
  // 学生路由
  {
    path: '/student',
    component: () => import('../layouts/StudentLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['STUDENT', 'ADMIN'],
      title: '学生中心'
    },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('../views/student/Courses.vue'),
        meta: { title: '我的课程' }
      },
      {
        path: 'course-selection',
        name: 'StudentCourseSelection',
        component: () => import('../views/student/CourseSelection.vue'),
        meta: { title: '选课' }
      },
      {
        path: 'timetable',
        name: 'StudentTimetable',
        component: () => import('../views/student/Timetable.vue'),
        meta: { title: '我的课表' }
      },
      {
        path: 'exams',
        name: 'StudentExams',
        component: () => import('../views/student/Exams.vue'),
        meta: { title: '考试安排' }
      },
      {
        path: 'grades',
        name: 'StudentGrades',
        component: () => import('../views/student/GradeQuery.vue'),
        meta: { title: '成绩查询' }
      },
      {
        path: 'leave',
        name: 'StudentLeaveRequests',
        component: () => import('../views/student/LeaveApplication.vue'),
        meta: { title: '请假申请' }
      },
      {
        path: 'notifications',
        name: 'StudentNotifications',
        component: () => import('../views/student/Notifications.vue'),
        meta: { title: '通知公告' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue'),
        meta: { title: '个人信息' }
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      requiresAuth: false,
      title: '页面不存在'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 高校学生管理系统` : '高校学生管理系统'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    if (!token || !userInfo) {
      // 未登录，跳转到登录页
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.roles && !to.meta.roles.includes(userInfo.role)) {
      // 没有权限，跳转到对应的首页
      if (userInfo.role === 'ADMIN') {
        next('/admin/dashboard')
      } else if (userInfo.role === 'TEACHER') {
        next('/teacher/dashboard')
      } else if (userInfo.role === 'STUDENT') {
        next('/student/dashboard')
      }
      return
    }
    
    // 已登录且有权限，继续导航
    next()
  } else {
    // 不需要认证的页面，直接导航
    next()
  }
})

export default router