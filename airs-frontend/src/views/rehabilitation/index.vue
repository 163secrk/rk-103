<template>
  <div class="rehabilitation-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">康复计划</h2>
        <p class="page-subtitle">制定和管理运动员康复训练计划</p>
      </div>
      <el-button
        v-if="canEdit"
        type="primary"
        :icon="Plus"
        @click="handleAdd"
      >
        新增计划
      </el-button>
    </div>

    <el-card class="table-card data-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索计划名称、治疗师..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 320px"
        />
        <el-select
          v-model="selectedInjury"
          placeholder="选择伤病记录"
          clearable
          filterable
          style="width: 240px"
          @change="fetchPlans"
        >
          <el-option
            v-for="item in injuryList"
            :key="item.id"
            :label="`${item.athleteName} - ${item.bodyPart}${item.injuryType}`"
            :value="item.id"
          />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
      </div>

      <el-alert
        v-if="selectedInjury && plans.length === 0 && !loading"
        title="该伤病记录暂无康复计划"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />

      <div v-loading="loading" class="plans-list">
        <el-card
          v-for="plan in plans"
          :key="plan.id"
          class="plan-card"
          shadow="hover"
        >
          <div class="plan-header">
            <div class="plan-info">
              <h3 class="plan-name">{{ plan.planName || '康复计划' }}</h3>
              <div class="plan-meta">
                <el-tag :type="getStatusType(plan.status)" size="small">
                  {{ getStatusText(plan.status) }}
                </el-tag>
                <span class="meta-item">
                  <el-icon><User /></el-icon>
                  {{ plan.therapistName || '-' }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ getTotalDays(plan.phases) }}天
                </span>
                <span class="meta-item">
                  <el-icon><List /></el-icon>
                  {{ plan.phases?.length || 0 }}个阶段
                </span>
              </div>
            </div>
            <div class="plan-actions">
              <el-button type="primary" link :icon="View" @click="handleView(plan)">查看</el-button>
              <el-button
                v-if="canEdit"
                type="primary"
                link
                :icon="Edit"
                @click="handleEdit(plan)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canEdit"
                type="danger"
                link
                :icon="Delete"
                @click="handleDelete(plan)"
              >
                删除
              </el-button>
            </div>
          </div>

          <div class="plan-description" v-if="plan.description">
            {{ plan.description }}
          </div>

          <div class="phases-timeline">
            <div
              v-for="(phase, index) in plan.phases"
              :key="phase.id || index"
              class="phase-item"
            >
              <div class="phase-header">
                <div class="phase-order">第{{ phase.phaseOrder }}阶段</div>
                <div class="phase-type">{{ getPhaseTypeText(phase.phaseType) }}</div>
                <div class="phase-name">{{ phase.phaseName }}</div>
              </div>
              <div class="phase-info">
                <el-tag size="small" type="info">
                  RPE上限: {{ phase.rpeUpperLimit || '-' }}
                </el-tag>
                <el-tag size="small" type="warning">
                  预计: {{ phase.estimatedDays || '-' }}天
                </el-tag>
              </div>
              <div class="phase-goals" v-if="phase.goals">
                <strong>目标：</strong>{{ phase.goals }}
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :close-on-click-modal="false"
      class="plan-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="关联伤病" prop="injuryRecordId">
              <el-select
                v-model="form.injuryRecordId"
                placeholder="请选择伤病记录"
                filterable
                style="width: 100%"
                @change="onInjuryChange"
              >
                <el-option
                  v-for="item in injuryList"
                  :key="item.id"
                  :label="`${item.athleteName} - ${item.bodyPart}${item.injuryType}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="form.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="治疗师" prop="therapistName">
              <el-input v-model="form.therapistName" placeholder="请输入治疗师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="进行中" value="ACTIVE" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已暂停" value="PAUSED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="计划描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入计划描述"
          />
        </el-form-item>

        <el-form-item label="康复阶段" prop="phases" class="phases-form-item">
          <div class="phases-editor">
            <div
              v-for="(phase, index) in form.phases"
              :key="phase.id || `phase-${index}`"
              class="phase-editor-card"
            >
              <div class="phase-editor-header">
                <span class="phase-editor-title">第 {{ phase.phaseOrder }} 阶段</span>
                <el-button
                  v-if="canEdit && form.phases.length > 1"
                  type="danger"
                  link
                  :icon="Delete"
                  @click="removePhase(index)"
                >
                  删除
                </el-button>
              </div>
              <el-row :gutter="12">
                <el-col :span="8">
                  <el-form-item
                    :prop="`phases.${index}.phaseType`"
                    :rules="{ required: true, message: '请选择阶段类型', trigger: 'change' }"
                    label="阶段类型"
                    label-width="80px"
                  >
                    <el-select v-model="phase.phaseType" placeholder="选择类型" style="width: 100%">
                      <el-option label="急性期" value="ACUTE" />
                      <el-option label="亚急性期" value="SUBACUTE" />
                      <el-option label="康复期" value="REHABILITATION" />
                      <el-option label="功能恢复期" value="FUNCTIONAL" />
                      <el-option label="重返训练" value="RETURN_TRAINING" />
                      <el-option label="重返比赛" value="RETURN_COMPETITION" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item
                    :prop="`phases.${index}.phaseName`"
                    label="阶段名称"
                    label-width="80px"
                  >
                    <el-input v-model="phase.phaseName" placeholder="输入阶段名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item
                    :prop="`phases.${index}.phaseOrder`"
                    :rules="{ required: true, message: '请输入阶段顺序', trigger: 'blur' }"
                    label="阶段顺序"
                    label-width="80px"
                  >
                    <el-input-number
                      v-model="phase.phaseOrder"
                      :min="1"
                      :max="10"
                      style="width: 100%"
                      @change="reorderPhases"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item
                    :prop="`phases.${index}.rpeUpperLimit`"
                    label="RPE上限"
                    label-width="80px"
                  >
                    <el-input-number
                      v-model="phase.rpeUpperLimit"
                      :min="1"
                      :max="20"
                      placeholder="主观疲劳感上限"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item
                    :prop="`phases.${index}.estimatedDays`"
                    label="预计天数"
                    label-width="80px"
                  >
                    <el-input-number
                      v-model="phase.estimatedDays"
                      :min="1"
                      :max="365"
                      placeholder="预计康复天数"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item
                :prop="`phases.${index}.goals`"
                label="阶段目标"
                label-width="80px"
              >
                <el-input
                  v-model="phase.goals"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入本阶段的康复目标"
                />
              </el-form-item>
              <el-form-item
                :prop="`phases.${index}.description`"
                label="训练内容"
                label-width="80px"
              >
                <el-input
                  v-model="phase.description"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入具体训练内容和注意事项"
                />
              </el-form-item>
            </div>

            <el-button
              v-if="canEdit"
              type="primary"
              plain
              :icon="Plus"
              @click="addPhase"
              style="width: 100%"
            >
              添加康复阶段
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="康复计划详情" width="800px">
      <div class="detail-content" v-if="currentPlan">
        <div class="detail-header">
          <h3>{{ currentPlan.planName || '康复计划' }}</h3>
          <div class="detail-tags">
            <el-tag :type="getStatusType(currentPlan.status)" size="small">
              {{ getStatusText(currentPlan.status) }}
            </el-tag>
            <el-tag size="small" type="info">
              治疗师: {{ currentPlan.therapistName || '-' }}
            </el-tag>
            <el-tag size="small" type="success">
              总周期: {{ getTotalDays(currentPlan.phases) }}天
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="关联伤病">
            {{ getInjuryInfo(currentPlan.injuryRecordId) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentPlan.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="计划描述" :span="2">
            {{ currentPlan.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">康复阶段</h4>
        <el-steps
          :active="currentPlan.phases?.length || 0"
          finish-status="success"
          direction="vertical"
        >
          <el-step
            v-for="(phase, index) in currentPlan.phases"
            :key="phase.id || index"
            :title="`第${phase.phaseOrder}阶段: ${getPhaseTypeText(phase.phaseType)}`"
            :description="phase.phaseName"
          >
            <template #icon>
              <span class="step-icon">{{ phase.phaseOrder }}</span>
            </template>
            <div class="step-content">
              <el-row :gutter="12">
                <el-col :span="12">
                  <p><strong>RPE上限：</strong>{{ phase.rpeUpperLimit || '-' }}</p>
                  <p><strong>预计天数：</strong>{{ phase.estimatedDays || '-' }}天</p>
                </el-col>
                <el-col :span="12">
                  <p><strong>目标：</strong>{{ phase.goals || '-' }}</p>
                </el-col>
              </el-row>
              <p v-if="phase.description" style="margin-top: 8px">
                <strong>训练内容：</strong>{{ phase.description }}
              </p>
            </div>
          </el-step>
        </el-steps>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Edit, Delete, User, Clock, List
} from '@element-plus/icons-vue'
import {
  getInjuryRecords,
  getRehabilitationPlansByInjury,
  getRehabilitationPlan,
  createRehabilitationPlan,
  updateRehabilitationPlan,
  deleteRehabilitationPlan
} from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const canEdit = computed(() => userStore.hasRole(['ADMIN', 'DOCTOR', 'THERAPIST']))

const loading = ref(false)
const submitLoading = ref(false)
const searchKeyword = ref('')
const selectedInjury = ref(null)
const plans = ref([])
const injuryList = ref([])

const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const currentPlan = ref(null)
const formRef = ref(null)

const form = reactive({
  id: null,
  injuryRecordId: null,
  planName: '',
  therapistId: null,
  therapistName: '',
  description: '',
  status: 'ACTIVE',
  phases: [
    {
      id: null,
      phaseOrder: 1,
      phaseType: 'ACUTE',
      phaseName: '',
      rpeUpperLimit: null,
      estimatedDays: 7,
      description: '',
      goals: ''
    }
  ]
})

const formRules = {
  injuryRecordId: [{ required: true, message: '请选择关联伤病', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑康复计划' : '新增康复计划')

const getStatusType = (status) => {
  const map = {
    ACTIVE: 'primary',
    COMPLETED: 'success',
    PAUSED: 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    ACTIVE: '进行中',
    COMPLETED: '已完成',
    PAUSED: '已暂停'
  }
  return map[status] || '未知'
}

const getPhaseTypeText = (type) => {
  const map = {
    ACUTE: '急性期',
    SUBACUTE: '亚急性期',
    REHABILITATION: '康复期',
    FUNCTIONAL: '功能恢复期',
    RETURN_TRAINING: '重返训练',
    RETURN_COMPETITION: '重返比赛'
  }
  return map[type] || type || '-'
}

const getTotalDays = (phases) => {
  if (!phases || phases.length === 0) return 0
  return phases.reduce((sum, p) => sum + (p.estimatedDays || 0), 0)
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

const getInjuryInfo = (id) => {
  const injury = injuryList.value.find(i => i.id === id)
  if (!injury) return '-'
  return `${injury.athleteName} - ${injury.bodyPart}${injury.injuryType}`
}

const fetchInjuryList = async () => {
  try {
    const res = await getInjuryRecords({ pageNum: 1, pageSize: 100 })
    injuryList.value = res.list || []
  } catch (e) {
    console.error('获取伤病列表失败', e)
  }
}

const fetchPlans = async () => {
  if (!selectedInjury.value) {
    plans.value = []
    return
  }
  loading.value = true
  try {
    let res = await getRehabilitationPlansByInjury(selectedInjury.value)
    let data = res || []
    
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      data = data.filter(p => 
        (p.planName && p.planName.toLowerCase().includes(keyword)) ||
        (p.therapistName && p.therapistName.toLowerCase().includes(keyword)) ||
        (p.description && p.description.toLowerCase().includes(keyword))
      )
    }
    
    plans.value = data
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchPlans()
}

const resetSearch = () => {
  searchKeyword.value = ''
  selectedInjury.value = null
  plans.value = []
}

const addPhase = () => {
  const maxOrder = Math.max(...form.phases.map(p => p.phaseOrder), 0)
  form.phases.push({
    id: null,
    phaseOrder: maxOrder + 1,
    phaseType: 'REHABILITATION',
    phaseName: '',
    rpeUpperLimit: null,
    estimatedDays: 7,
    description: '',
    goals: ''
  })
}

const removePhase = (index) => {
  form.phases.splice(index, 1)
  reorderPhases()
}

const reorderPhases = () => {
  form.phases.sort((a, b) => a.phaseOrder - b.phaseOrder)
  form.phases.forEach((p, i) => {
    p.phaseOrder = i + 1
  })
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    injuryRecordId: null,
    planName: '',
    therapistId: null,
    therapistName: '',
    description: '',
    status: 'ACTIVE',
    phases: [
      {
        id: null,
        phaseOrder: 1,
        phaseType: 'ACUTE',
        phaseName: '',
        rpeUpperLimit: null,
        estimatedDays: 7,
        description: '',
        goals: ''
      }
    ]
  })
  formRef.value?.resetFields()
}

const onInjuryChange = () => {
  const injury = injuryList.value.find(i => i.id === form.injuryRecordId)
  if (injury && !form.planName) {
    form.planName = `${injury.athleteName}${injury.bodyPart}康复计划`
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  
  if (route.query.injuryId) {
    form.injuryRecordId = Number(route.query.injuryId)
    onInjuryChange()
  }
  
  dialogVisible.value = true
}

const handleEdit = async (plan) => {
  isEdit.value = true
  resetForm()
  
  try {
    const detail = await getRehabilitationPlan(plan.id)
    Object.assign(form, {
      id: detail.id,
      injuryRecordId: detail.injuryRecordId,
      planName: detail.planName,
      therapistId: detail.therapistId,
      therapistName: detail.therapistName,
      description: detail.description,
      status: detail.status,
      phases: (detail.phases || []).map(p => ({
        id: p.id,
        phaseOrder: p.phaseOrder,
        phaseType: p.phaseType,
        phaseName: p.phaseName,
        rpeUpperLimit: p.rpeUpperLimit,
        estimatedDays: p.estimatedDays,
        description: p.description,
        goals: p.goals
      }))
    })
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取计划详情失败')
  }
}

const handleView = async (plan) => {
  try {
    currentPlan.value = await getRehabilitationPlan(plan.id)
    viewVisible.value = true
  } catch (e) {
    ElMessage.error('获取计划详情失败')
  }
}

const handleDelete = (plan) => {
  ElMessageBox.confirm(`确定要删除康复计划 "${plan.planName || '未命名'}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteRehabilitationPlan(plan.id)
    ElMessage.success('删除成功')
    fetchPlans()
  }).catch(() => {})
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const submitData = {
      injuryRecordId: form.injuryRecordId,
      planName: form.planName,
      therapistId: form.therapistId,
      therapistName: form.therapistName,
      description: form.description,
      status: form.status,
      phases: form.phases.map(p => ({
        id: p.id || undefined,
        phaseOrder: p.phaseOrder,
        phaseType: p.phaseType,
        phaseName: p.phaseName,
        rpeUpperLimit: p.rpeUpperLimit,
        estimatedDays: p.estimatedDays,
        description: p.description,
        goals: p.goals
      }))
    }
    
    if (isEdit.value) {
      await updateRehabilitationPlan(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createRehabilitationPlan(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchPlans()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await fetchInjuryList()
  
  if (route.query.injuryId) {
    selectedInjury.value = Number(route.query.injuryId)
    fetchPlans()
  }
})
</script>

<style scoped lang="scss">
.rehabilitation-page {
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

    .plans-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .plan-card {
      border-radius: 12px;
      border: 1px solid #e2e8f0;
      transition: all 0.3s ease;

      &:hover {
        border-color: $primary-light;
        @include card-hover;
      }

      .plan-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .plan-info {
          .plan-name {
            margin: 0 0 8px;
            font-size: 18px;
            font-weight: 600;
            color: #1e293b;
          }

          .plan-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;

            .meta-item {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 13px;
              color: #64748b;

              .el-icon {
                font-size: 14px;
              }
            }
          }
        }

        .plan-actions {
          display: flex;
          gap: 4px;
        }
      }

      .plan-description {
        margin-bottom: 16px;
        padding: 12px;
        background-color: #f8fafc;
        border-radius: 8px;
        color: #475569;
        font-size: 14px;
      }

      .phases-timeline {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .phase-item {
          padding: 12px;
          background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
          border-left: 4px solid $primary-color;
          border-radius: 0 8px 8px 0;

          .phase-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;

            .phase-order {
              font-weight: 600;
              color: $primary-color;
            }

            .phase-type {
              padding: 2px 8px;
              background-color: $primary-color;
              color: #fff;
              border-radius: 4px;
              font-size: 12px;
            }

            .phase-name {
              font-weight: 500;
              color: #1e293b;
            }
          }

          .phase-info {
            display: flex;
            gap: 8px;
            margin-bottom: 8px;
          }

          .phase-goals {
            font-size: 13px;
            color: #475569;
          }
        }
      }
    }
  }

  :deep(.plan-dialog) {
    .el-dialog__body {
      max-height: 600px;
      overflow-y: auto;
    }

    .phases-form-item {
      .el-form-item__label {
        align-self: flex-start;
        padding-top: 12px;
      }

      .phases-editor {
        width: 100%;
        display: flex;
        flex-direction: column;
        gap: 16px;

        .phase-editor-card {
          padding: 16px;
          background-color: #f8fafc;
          border-radius: 8px;
          border: 1px solid #e2e8f0;

          .phase-editor-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .phase-editor-title {
              font-weight: 600;
              color: $primary-color;
            }
          }
        }
      }
    }
  }

  .detail-content {
    .detail-header {
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #e2e8f0;

      h3 {
        margin: 0 0 12px;
        font-size: 20px;
        color: #1e293b;
      }

      .detail-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .section-title {
      margin: 0 0 16px;
      font-size: 16px;
      color: #1e293b;
    }

    :deep(.el-step) {
      padding-bottom: 24px;

      .step-icon {
        width: 32px;
        height: 32px;
        background: $primary-color;
        color: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
      }

      .step-content {
        padding: 12px 0 0 12px;
        font-size: 14px;
        color: #475569;
      }
    }
  }
}
</style>
