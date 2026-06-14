<template>
  <div class="placeholder">
    <el-card class="data-card" shadow="never">
      <div class="placeholder-content">
        <el-icon size="80" color="#cbd5e1"><Construction /></el-icon>
        <h3 class="title">{{ $route.meta.title }}</h3>
        <p class="subtitle">功能开发中，敬请期待...</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Construction } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.placeholder {
  height: 100%;
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;

  .placeholder-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 40px;
    gap: 16px;

    .title {
      font-size: 20px;
      font-weight: 600;
      color: #475569;
      margin: 0;
    }

    .subtitle {
      font-size: 14px;
      color: #94a3b8;
      margin: 0;
    }
  }
}
</style>
