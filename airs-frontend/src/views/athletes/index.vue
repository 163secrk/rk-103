<template>
  <div class="athletes-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">运动员管理</h2>
        <p class="page-subtitle">管理球队运动员信息和健康状态</p>
      </div>
      <el-button
        v-if="canEdit"
        type="primary"
        :icon="Plus"
        @click="handleAdd"
      >
        新增运动员
      </el-button>
    </div>

    <el-card class="table-card data-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索姓名、球衣号、位置..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 320px"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="fetchData">重置</el-button>
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
            <el-avatar :size="44" :src="row.avatar || getDefaultAvatar(row.name)">
              {{ row.name?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="jerseyNumber" label="球衣号" width="100">
          <template #default="{ row }">
            <span class="jersey-number">#{{ row.jerseyNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="位置" min-width="100" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column label="年龄" width="80">
          <template #default="{ row }">
            {{ getAge(row.birthDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="height" label="身高" width="100" />
        <el-table-column prop="weight" label="体重" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" class-name="action-column">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
              <el-button
                v-if="canEdit"
                type="primary"
                link
                :icon="Edit"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canEdit"
                type="danger"
                link
                :icon="Delete"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
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
        label-width="80px"
      >
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="form.avatar || getDefaultAvatar(form.name)">
              {{ form.name?.charAt(0) }}
            </el-avatar>
            <el-input
              v-model="form.avatar"
              placeholder="输入头像URL"
              style="flex: 1; margin-left: 12px"
            />
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="球衣号" prop="jerseyNumber">
          <el-input v-model="form.jerseyNumber" placeholder="请输入球衣号" />
        </el-form-item>
        <el-form-item label="位置" prop="position">
          <el-select v-model="form.position" placeholder="请选择位置" style="width: 100%">
            <el-option label="前锋" value="前锋" />
            <el-option label="中场" value="中场" />
            <el-option label="后卫" value="后卫" />
            <el-option label="守门员" value="守门员" />
            <el-option label="中锋" value="中锋" />
            <el-option label="大前锋" value="大前锋" />
            <el-option label="小前锋" value="小前锋" />
            <el-option label="得分后卫" value="得分后卫" />
            <el-option label="控球后卫" value="控球后卫" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="身高" prop="height">
              <el-input v-model="form.height" placeholder="如：185cm" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重" prop="weight">
              <el-input v-model="form.weight" placeholder="如：75kg" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="健康" value="HEALTHY" />
            <el-option label="伤停" value="INJURED" />
            <el-option label="康复中" value="REHAB" />
            <el-option label="可参赛" value="AVAILABLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="运动员详情" width="500px">
      <div class="detail-content" v-if="currentRow">
        <div class="detail-avatar">
          <el-avatar :size="100" :src="currentRow.avatar || getDefaultAvatar(currentRow.name)">
            {{ currentRow.name?.charAt(0) }}
          </el-avatar>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="球衣号">#{{ currentRow.jerseyNumber }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ currentRow.position }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentRow.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ getAge(currentRow.birthDate) }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentRow.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="身高">{{ currentRow.height }}</el-descriptions-item>
          <el-descriptions-item label="体重">{{ currentRow.weight }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="2">
            <span :class="['status-tag', getStatusClass(currentRow.status)]">
              {{ getStatusText(currentRow.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete
} from '@element-plus/icons-vue'
import { getAthletes, createAthlete, updateAthlete, deleteAthlete } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canEdit = computed(() => userStore.hasRole(['ADMIN', 'DOCTOR']))

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const tableData = ref([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const currentRow = ref(null)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  jerseyNumber: '',
  position: '',
  gender: '男',
  birthDate: '',
  height: '',
  weight: '',
  avatar: '',
  status: 'HEALTHY',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  jerseyNumber: [{ required: true, message: '请输入球衣号', trigger: 'blur' }],
  position: [{ required: true, message: '请选择位置', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑运动员' : '新增运动员')

const getDefaultAvatar = (name) => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${name || 'athlete'}`
}

const getStatusClass = (status) => {
  const map = {
    HEALTHY: 'healthy',
    AVAILABLE: 'available',
    INJURED: 'injured',
    MILD_INJURY: 'injured',
    MODERATE_INJURY: 'injured',
    SERIOUS_INJURY: 'injured',
    SEVERE_INJURY: 'injured',
    REHAB: 'rehab',
    RECOVERING: 'rehab'
  }
  return map[status] || 'healthy'
}

const getStatusText = (status) => {
  const map = {
    HEALTHY: '健康',
    AVAILABLE: '可参赛',
    INJURED: '伤停',
    MILD_INJURY: '轻伤',
    MODERATE_INJURY: '伤停',
    SERIOUS_INJURY: '重伤',
    SEVERE_INJURY: '重伤',
    REHAB: '康复中',
    RECOVERING: '康复中'
  }
  return map[status] || '健康'
}

const getAge = (birthDate) => {
  if (!birthDate) return '-'
  const birth = new Date(birthDate)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return age + '岁'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAthletes({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value
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

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    jerseyNumber: '',
    position: '',
    gender: '男',
    birthDate: '',
    height: '',
    weight: '',
    avatar: '',
    status: 'HEALTHY',
    remark: ''
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
  ElMessageBox.confirm(`确定要删除运动员 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteAthlete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateAthlete(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createAthlete(form)
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
.athletes-page {
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
    }

    :deep(.el-table th) {
      background-color: #f8fafc;
      color: #475569;
      font-weight: 600;
    }

    .jersey-number {
      display: inline-block;
      padding: 2px 10px;
      background: linear-gradient(135deg, $primary-color, $accent-green);
      color: #fff;
      border-radius: 4px;
      font-weight: 600;
      font-size: 13px;
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
