<template>
  <div class="dashboard">
    <div class="page-header">
      <h2 class="page-title">仪表盘</h2>
      <p class="page-subtitle">欢迎使用 AIRS 运动员伤病康复追踪系统</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card data-card">
        <div class="stat-icon healthy">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">运动员总数</div>
        </div>
      </div>
      <div class="stat-card data-card">
        <div class="stat-icon available">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.healthy }}</div>
          <div class="stat-label">健康</div>
        </div>
      </div>
      <div class="stat-card data-card">
        <div class="stat-icon rehab">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.rehab }}</div>
          <div class="stat-label">康复中</div>
        </div>
      </div>
      <div class="stat-card data-card">
        <div class="stat-icon injured">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.injured }}</div>
          <div class="stat-label">伤停</div>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <el-card class="data-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">运动员状态分布</span>
          </div>
        </template>
        <div class="empty-chart">
          <el-icon size="64" color="#cbd5e1"><DataLine /></el-icon>
          <p>图表功能开发中...</p>
        </div>
      </el-card>

      <el-card class="data-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">最近活动</span>
          </div>
        </template>
        <div class="empty-chart">
          <el-icon size="64" color="#cbd5e1"><Document /></el-icon>
          <p>暂无活动记录</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAthletes } from '@/api'
import { User, CircleCheck, Warning, DataLine, Document } from '@element-plus/icons-vue'

const stats = ref({
  total: 0,
  healthy: 0,
  injured: 0,
  rehab: 0,
  available: 0
})

onMounted(async () => {
  try {
    const res = await getAthletes({ pageNum: 1, pageSize: 1000 })
    const list = res.list || []
    stats.value.total = res.total || 0
    stats.value.healthy = list.filter(a => a.status === 'HEALTHY').length
    stats.value.injured = list.filter(a => a.status === 'INJURED').length
    stats.value.rehab = list.filter(a => a.status === 'REHAB').length
    stats.value.available = list.filter(a => a.status === 'AVAILABLE').length
  } catch {}
})
</script>

<style scoped lang="scss">
.dashboard {
  .page-header {
    margin-bottom: 24px;

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

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: #fff;
      border-radius: 12px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: $card-shadow;

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;

        &.healthy { background: linear-gradient(135deg, $status-healthy, #85ce61); }
        &.injured { background: linear-gradient(135deg, $status-injured, #f78989); }
        &.rehab { background: linear-gradient(135deg, $status-rehab, #ebb563); }
        &.available { background: linear-gradient(135deg, $status-available, #66b1ff); }
      }

      .stat-content {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #1e293b;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: #64748b;
          margin-top: 4px;
        }
      }
    }
  }

  .chart-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-weight: 600;
        color: #1e293b;
      }
    }

    .empty-chart {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      color: #94a3b8;
    }
  }
}
</style>
