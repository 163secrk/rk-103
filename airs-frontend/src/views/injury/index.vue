<template>
  <div class="injury-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">伤病记录</h2>
        <p class="page-subtitle">管理运动员伤病信息和治疗记录</p>
      </div>
      <el-button
        v-if="canEdit"
        type="primary"
        :icon="Plus"
        @click="handleAdd"
      >
        新增记录
      </el-button>
    </div>

    <el-card class="table-card data-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索运动员姓名、受伤部位、伤病类型..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 320px"
        />
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option label="进行中" value="ACTIVE" />
          <el-option label="已康复" value="RECOVERED" />
          <el-option label="康复中" value="REHAB" />
        </el-select>
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
        <el-table-column label="运动员" min-width="120">
          <template #default="{ row }">
            <div class="athlete-info">
              <el-avatar :size="36" :src="getDefaultAvatar(row.athleteName)">
                {{ row.athleteName?.charAt(0) }}
              </el-avatar>
              <span class="athlete-name">{{ row.athleteName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="bodyPart" label="受伤部位" min-width="100" />
        <el-table-column prop="injuryType" label="伤病类型" min-width="100" />
        <el-table-column label="严重程度" width="100">
          <template #default="{ row }">
            <span :class="['severity-tag', getSeverityClass(row.severity)]">
              {{ row.severity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="injuryDate" label="受伤日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="doctorName" label="主治医生" min-width="100" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right" class-name="action-column">
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
      width="680px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item label="运动员" prop="athleteId">
              <el-select
                v-model="form.athleteId"
                placeholder="请选择运动员"
                style="width: 100%"
                filterable
                @change="handleAthleteChange"
              >
                <el-option
                  v-for="athlete in athleteList"
                  :key="athlete.id"
                  :label="`${athlete.name} (${athlete.jerseyNumber || '无编号'})`"
                  :value="athlete.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="受伤部位" prop="bodyPart">
              <el-select v-model="form.bodyPart" placeholder="请选择受伤部位" style="width: 100%">
                <el-option label="头部" value="头部" />
                <el-option label="肩部" value="肩部" />
                <el-option label="手臂" value="手臂" />
                <el-option label="肘部" value="肘部" />
                <el-option label="手腕" value="手腕" />
                <el-option label="手部" value="手部" />
                <el-option label="胸部" value="胸部" />
                <el-option label="背部" value="背部" />
                <el-option label="腰部" value="腰部" />
                <el-option label="腹部" value="腹部" />
                <el-option label="髋部" value="髋部" />
                <el-option label="大腿" value="大腿" />
                <el-option label="膝盖" value="膝盖" />
                <el-option label="小腿" value="小腿" />
                <el-option label="脚踝" value="脚踝" />
                <el-option label="足部" value="足部" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="伤病类型" prop="injuryType">
              <el-select v-model="form.injuryType" placeholder="请选择伤病类型" style="width: 100%">
                <el-option label="拉伤" value="拉伤" />
                <el-option label="扭伤" value="扭伤" />
                <el-option label="骨折" value="骨折" />
                <el-option label="脱臼" value="脱臼" />
                <el-option label="挫伤" value="挫伤" />
                <el-option label="撕裂" value="撕裂" />
                <el-option label="肌腱炎" value="肌腱炎" />
                <el-option label="滑囊炎" value="滑囊炎" />
                <el-option label="疲劳性损伤" value="疲劳性损伤" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="严重程度" prop="severity">
              <el-radio-group v-model="form.severity">
                <el-radio value="轻度">轻度</el-radio>
                <el-radio value="中度">中度</el-radio>
                <el-radio value="重度">重度</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="受伤日期" prop="injuryDate">
              <el-date-picker
                v-model="form.injuryDate"
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
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="进行中" value="ACTIVE" />
                <el-option label="康复中" value="REHAB" />
                <el-option label="已康复" value="RECOVERED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主治医生" prop="doctorId">
              <el-select
                v-model="form.doctorId"
                placeholder="请选择主治医生"
                style="width: 100%"
                filterable
                @change="handleDoctorChange"
              >
                <el-option
                  v-for="doctor in doctorList"
                  :key="doctor.id"
                  :label="doctor.realName || doctor.username"
                  :value="doctor.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="伤病描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述伤病情况"
          />
        </el-form-item>
        <el-form-item label="治疗方案" prop="treatment">
          <el-input
            v-model="form.treatment"
            type="textarea"
            :rows="3"
            placeholder="请输入治疗方案"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="伤病记录详情" width="600px">
      <div class="detail-content" v-if="currentRow">
        <div class="detail-header">
          <div class="detail-avatar">
            <el-avatar :size="80" :src="getDefaultAvatar(currentRow.athleteName)">
              {{ currentRow.athleteName?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="detail-title">
            <h3>{{ currentRow.athleteName }}</h3>
            <p>
              <span :class="['severity-tag', getSeverityClass(currentRow.severity)]">
                {{ currentRow.severity }}
              </span>
              <span :class="['status-tag', getStatusClass(currentRow.status)]" style="margin-left: 8px">
                {{ getStatusText(currentRow.status) }}
              </span>
            </p>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="受伤部位">{{ currentRow.bodyPart }}</el-descriptions-item>
          <el-descriptions-item label="伤病类型">{{ currentRow.injuryType }}</el-descriptions-item>
          <el-descriptions-item label="受伤日期">{{ currentRow.injuryDate }}</el-descriptions-item>
          <el-descriptions-item label="主治医生">{{ currentRow.doctorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentRow.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(currentRow.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="伤病描述" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="治疗方案" :span="2">{{ currentRow.treatment || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-actions" v-if="canEdit">
          <el-button type="primary" @click="handleCreatePlan(currentRow)">
            创建康复计划
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete
} from '@element-plus/icons-vue'
import {
  getInjuryRecords, createInjuryRecord, updateInjuryRecord, deleteInjuryRecord, getAthleteList, getDoctorList
} from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const canEdit = computed(() => userStore.hasRole(['ADMIN', 'DOCTOR']))

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const tableData = ref([])
const athleteList = ref([])
const doctorList = ref([])
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
  athleteId: null,
  athleteName: '',
  bodyPart: '',
  injuryType: '',
  severity: '轻度',
  description: '',
  injuryDate: '',
  status: 'ACTIVE',
  treatment: '',
  doctorId: null,
  doctorName: ''
})

const formRules = {
  athleteId: [{ required: true, message: '请选择运动员', trigger: 'change' }],
  bodyPart: [{ required: true, message: '请选择受伤部位', trigger: 'change' }],
  injuryType: [{ required: true, message: '请选择伤病类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  injuryDate: [{ required: true, message: '请选择受伤日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑伤病记录' : '新增伤病记录')

const getDefaultAvatar = (name) => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${name || 'athlete'}`
}

const getSeverityClass = (severity) => {
  const map = {
    '轻度': 'mild',
    '中度': 'moderate',
    '重度': 'severe'
  }
  return map[severity] || 'mild'
}

const getStatusClass = (status) => {
  const map = {
    ACTIVE: 'injured',
    REHAB: 'rehab',
    RECOVERED: 'healthy'
  }
  return map[status] || 'healthy'
}

const getStatusText = (status) => {
  const map = {
    ACTIVE: '进行中',
    REHAB: '康复中',
    RECOVERED: '已康复'
  }
  return map[status] || '未知'
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

const fetchAthleteList = async () => {
  try {
    const res = await getAthleteList()
    athleteList.value = res || []
  } catch (e) {
    console.error('获取运动员列表失败', e)
  }
}

const fetchDoctorList = async () => {
  try {
    const res = await getDoctorList()
    doctorList.value = res || []
  } catch (e) {
    console.error('获取医生列表失败', e)
  }
}

const handleAthleteChange = (athleteId) => {
  const athlete = athleteList.value.find(a => a.id === athleteId)
  if (athlete) {
    form.athleteName = athlete.name
  }
}

const handleDoctorChange = (doctorId) => {
  const doctor = doctorList.value.find(d => d.id === doctorId)
  if (doctor) {
    form.doctorName = doctor.realName || doctor.username
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const keyword = statusFilter.value
      ? `${searchKeyword.value} ${statusFilter.value}`.trim()
      : searchKeyword.value
    const res = await getInjuryRecords({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: keyword || undefined
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
    athleteId: null,
    athleteName: '',
    bodyPart: '',
    injuryType: '',
    severity: '轻度',
    description: '',
    injuryDate: '',
    status: 'ACTIVE',
    treatment: '',
    doctorId: null,
    doctorName: ''
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
  if (row.doctorId) {
    handleDoctorChange(row.doctorId)
  }
  dialogVisible.value = true
}

const handleView = (row) => {
  currentRow.value = row
  viewVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除 ${row.athleteName} 的伤病记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteInjuryRecord(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateInjuryRecord(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createInjuryRecord(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleCreatePlan = (row) => {
  viewVisible.value = false
  router.push({
    path: '/rehabilitation',
    query: { injuryId: row.id, athleteName: row.athleteName }
  })
}

onMounted(() => {
  fetchData()
  fetchAthleteList()
  fetchDoctorList()
})
</script>

<style scoped lang="scss">
.injury-page {
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

    .athlete-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .athlete-name {
        font-weight: 500;
        color: #1e293b;
      }
    }

    .severity-tag {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;

      &.mild {
        background-color: #f0f9eb;
        color: #67c23a;
      }

      &.moderate {
        background-color: #fdf6ec;
        color: #e6a23c;
      }

      &.severe {
        background-color: #fef0f0;
        color: #f56c6c;
      }
    }

    .status-tag {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;

      &.healthy {
        background-color: #f0f9eb;
        color: #67c23a;
      }

      &.injured {
        background-color: #fef0f0;
        color: #f56c6c;
      }

      &.rehab {
        background-color: #fdf6ec;
        color: #e6a23c;
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

  .detail-content {
    .detail-header {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #e2e8f0;

      .detail-title {
        h3 {
          margin: 0 0 8px;
          font-size: 20px;
          color: #1e293b;
        }

        p {
          margin: 0;
        }
      }
    }

    .detail-actions {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
