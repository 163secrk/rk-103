<template>
  <div class="users-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理系统用户账号、角色和权限</p>
      </div>
      <el-button
        type="primary"
        :icon="Plus"
        @click="handleAdd"
      >
        新增用户
      </el-button>
    </div>

    <el-card class="table-card data-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索用户名、真实姓名、邮箱、电话..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 320px"
        />
        <el-select
          v-model="searchForm.role"
          placeholder="选择角色"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option label="管理员" value="ADMIN" />
          <el-option label="医生" value="DOCTOR" />
          <el-option label="康复师" value="THERAPIST" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        :row-style="{ height: '64px' }"
      >
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="44" :src="row.avatar || getDefaultAvatar(row.username)">
              {{ row.realName?.charAt(0) || row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <span :class="['role-tag', getRoleClass(row.role)]">
              {{ getRoleText(row.role) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" class-name="action-column">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
              <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button type="warning" link :icon="Key" @click="handleResetPassword(row)">重置密码</el-button>
              <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="康复师" value="THERAPIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <el-avatar :size="60" :src="form.avatar || getDefaultAvatar(form.username)">
              {{ form.realName?.charAt(0) || form.username?.charAt(0) }}
            </el-avatar>
            <el-input
              v-model="form.avatar"
              placeholder="输入头像URL"
              style="flex: 1; margin-left: 12px"
            />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="用户详情" width="500px">
      <div class="detail-content" v-if="currentRow">
        <div class="detail-avatar">
          <el-avatar :size="100" :src="currentRow.avatar || getDefaultAvatar(currentRow.username)">
            {{ currentRow.realName?.charAt(0) || currentRow.username?.charAt(0) }}
          </el-avatar>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentRow.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentRow.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <span :class="['role-tag', getRoleClass(currentRow.role)]">
              {{ getRoleText(currentRow.role) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentRow.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentRow.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ currentRow.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(currentRow.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDateTime(currentRow.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="passwordVisible" title="重置密码" width="400px">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordFormRules"
        label-width="90px"
      >
        <el-form-item label="用户名">
          <el-input :value="currentRow?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSubmitLoading" @click="handlePasswordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete, Key
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getUsers, createUser, updateUser, deleteUser, resetUserPassword
} from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const passwordSubmitLoading = ref(false)
const tableData = ref([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  role: ''
})

const dialogVisible = ref(false)
const viewVisible = ref(false)
const passwordVisible = ref(false)
const isEdit = ref(false)
const currentRow = ref(null)
const formRef = ref(null)
const passwordFormRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  password: '',
  role: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  status: 1
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  realName: [
    { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { max: 100, message: '邮箱长度不能超过100个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const passwordFormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const getDefaultAvatar = (username) => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username || 'user'}`
}

const getRoleClass = (role) => {
  const map = {
    ADMIN: 'admin',
    DOCTOR: 'doctor',
    THERAPIST: 'therapist',
    COACH: 'coach',
    ATHLETE: 'athlete'
  }
  return map[role] || 'default'
}

const getRoleText = (role) => {
  const map = {
    ADMIN: '管理员',
    DOCTOR: '医生',
    THERAPIST: '康复师',
    COACH: '教练',
    ATHLETE: '运动员'
  }
  return map[role] || role
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUsers({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      role: searchForm.role
    })
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  pagination.pageNum = 1
  fetchData()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    role: '',
    realName: '',
    email: '',
    phone: '',
    avatar: '',
    status: 1
  })
  formRef.value?.resetFields()
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  currentRow.value = row
  viewVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleResetPassword = (row) => {
  currentRow.value = row
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
  passwordVisible.value = true
}

const handlePasswordSubmit = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordSubmitLoading.value = true
    await resetUserPassword(currentRow.value.id, { newPassword: passwordForm.newPassword })
    ElMessage.success('密码重置成功')
    passwordVisible.value = false
  } finally {
    passwordSubmitLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      const { id, password, username, ...updateData } = form
      await updateUser(form.id, updateData)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.users-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 4px;
    }

    .page-subtitle {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }
  }

  .table-card {
    border-radius: 12px;
    box-shadow: $card-shadow;

    .search-bar {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }

    :deep(.el-table th) {
      background-color: #f8fafc;
      color: #475569;
      font-weight: 600;
    }

    .role-tag {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 600;

      &.admin {
        background: linear-gradient(135deg, #ef4444, #dc2626);
        color: #fff;
      }

      &.doctor {
        background: linear-gradient(135deg, #3b82f6, #2563eb);
        color: #fff;
      }

      &.therapist {
        background: linear-gradient(135deg, #10b981, #059669);
        color: #fff;
      }

      &.coach {
        background: linear-gradient(135deg, #f59e0b, #d97706);
        color: #fff;
      }

      &.athlete {
        background: linear-gradient(135deg, #8b5cf6, #7c3aed);
        color: #fff;
      }

      &.default {
        background: #e2e8f0;
        color: #475569;
      }
    }

    :deep(.action-column) {
      .action-buttons {
        display: flex;
        gap: 4px;
        white-space: nowrap;
        flex-wrap: nowrap;
      }
    }

    .pagination {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }

  .avatar-upload {
    display: flex;
    align-items: center;
    width: 100%;
  }

  .detail-content {
    .detail-avatar {
      display: flex;
      justify-content: center;
      margin-bottom: 20px;
    }
  }
}
</style>
