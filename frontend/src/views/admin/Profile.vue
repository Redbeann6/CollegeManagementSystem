<template>
  <div class="profile-management">
    <div class="profile-header">
      <h1>个人信息</h1>
    </div>
    
    <div class="profile-content">
      <el-card class="profile-card">
        <div class="profile-avatar-section">
          <el-upload
            class="avatar-uploader"
            action="/api/profile/avatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
          >
            <img v-if="profile.avatar" :src="profile.avatar" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div class="avatar-text">点击上传头像</div>
        </div>
        
        <el-form :model="profile" :rules="rules" ref="profileForm" label-width="100px" class="profile-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="profile.username" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="profile.name" placeholder="请输入姓名"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profile.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input v-model="profile.phone" placeholder="请输入电话"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="角色" prop="role">
                <el-input v-model="profile.role" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="profile.gender" placeholder="请选择性别" style="width: 100%;">
                  <el-option label="男" value="男"></el-option>
                  <el-option label="女" value="女"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" v-if="profile.role === 'STUDENT'">
            <el-col :span="12">
              <el-form-item label="学号" prop="studentId">
                <el-input v-model="profile.studentId" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="班级" prop="className">
                <el-input v-model="profile.className" placeholder="请输入班级"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" v-if="profile.role === 'STUDENT'">
            <el-col :span="12">
              <el-form-item label="专业" prop="major">
                <el-input v-model="profile.major" placeholder="请输入专业"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="地址" prop="address">
                <el-input v-model="profile.address" placeholder="请输入地址"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" v-if="profile.role === 'TEACHER'">
            <el-col :span="12">
              <el-form-item label="工号" prop="teacherId">
                <el-input v-model="profile.teacherId" disabled></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职称" prop="title">
                <el-input v-model="profile.title" placeholder="请输入职称"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" v-if="profile.role === 'TEACHER'">
            <el-col :span="12">
              <el-form-item label="学历" prop="education">
                <el-input v-model="profile.education" placeholder="请输入学历"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="办公室" prop="office">
                <el-input v-model="profile.office" placeholder="请输入办公室"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" v-if="profile.role === 'TEACHER'">
            <el-col :span="12">
              <el-form-item label="办公电话" prop="officePhone">
                <el-input v-model="profile.officePhone" placeholder="请输入办公电话"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机" prop="mobilePhone">
                <el-input v-model="profile.mobilePhone" placeholder="请输入手机"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item>
            <el-button type="primary" @click="updateProfile" :loading="saving">保存修改</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminProfile',
  data() {
    return {
      profile: {
        id: null,
        username: '',
        name: '',
        email: '',
        phone: '',
        avatar: '',
        role: '',
        // 学生字段
        studentId: '',
        className: '',
        major: '',
        gender: '',
        address: '',
        idCard: '',
        politicalStatus: '',
        // 教师字段
        teacherId: '',
        title: '',
        education: '',
        office: '',
        officePhone: '',
        mobilePhone: ''
      },
      saving: false,
      rules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ]
      },
      uploadHeaders: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  },
  mounted() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const response = await this.$axios.get('/profile')
        if (response.data.success) {
          this.profile = { ...this.profile, ...response.data.data }
        } else {
          this.$message.error(response.data.message || '获取个人信息失败')
        }
      } catch (error) {
        console.error('加载个人信息失败:', error)
        this.$message.error('加载个人信息失败，请稍后重试')
      }
    },
    async updateProfile() {
      this.saving = true
      try {
        const response = await this.$axios.put('/profile', this.profile)
        if (response.data.success) {
          this.$message.success(response.data.message || '更新个人信息成功')
          this.loadProfile()
        } else {
          this.$message.error(response.data.message || '更新个人信息失败')
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        this.$message.error('更新个人信息失败，请稍后重试')
      } finally {
        this.saving = false
      }
    },
    resetForm() {
      this.loadProfile()
    },
    handleAvatarSuccess(res, file) {
      if (res.success) {
        this.profile.avatar = res.data;
        this.$message.success('头像上传成功');
        // 重新加载个人信息以确保头像更新
        this.loadProfile().then(() => {
          // 更新localStorage中的用户信息，这样右上角的头像也会同步更新
          try {
            const currentUserInfo = JSON.parse(localStorage.getItem('userInfo'));
            if (currentUserInfo) {
              currentUserInfo.avatar = this.profile.avatar;
              localStorage.setItem('userInfo', JSON.stringify(currentUserInfo));
              // 触发全局事件，通知其他组件更新用户信息
              const event = new Event('userInfoUpdated');
              window.dispatchEvent(event);
            }
          } catch (error) {
            console.error('更新localStorage中的用户信息失败:', error);
          }
        });
      } else {
        this.$message.error(res.message || '头像上传失败');
      }
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error('头像图片只能是 JPG/PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('头像图片大小不能超过 2MB!');
      }
      return isJPG && isLt2M;
    }
  }
}
</script>

<style scoped>
.profile-management {
  padding: 20px;
  background-color: var(--background-color);
}

.profile-header {
  margin-bottom: 24px;
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.profile-header h1 {
  margin: 0;
  color: var(--text-primary);
}

.profile-content {
  border-radius: 16px;
  background-color: var(--background-color-light);
  border: 1px solid var(--border-light);
  box-shadow: var(--box-shadow-base);
  padding: 24px;
}

.profile-card {
  border: none;
  box-shadow: none;
  background: transparent;
}

.profile-avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: inline-block;
  margin-bottom: 10px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-text {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.profile-form {
  max-width: 800px;
  margin: 0 auto;
}
</style>