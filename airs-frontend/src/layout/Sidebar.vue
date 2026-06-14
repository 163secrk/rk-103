<template>
  <div class="sidebar-wrapper">
    <div class="logo-section">
      <div class="logo">
        <Trophy class="logo-icon" />
      </div>
      <span v-if="!collapse" class="logo-text">AIRS</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapse"
      :collapse-transition="false"
      background-color="transparent"
      text-color="#cbd5e1"
      active-text-color="#ffffff"
      router
      class="sidebar-menu"
    >
      <el-menu-item
        v-for="menu in menus"
        :key="menu.name"
        :index="menu.path"
      >
        <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
        <template #title>{{ menu.title }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Trophy, DataLine, User, Document, Warning, Trophy as TrophyIcon, Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const props = defineProps({
  collapse: Boolean
})

const route = useRoute()
const userStore = useUserStore()

const roleMenus = {
  ADMIN: ['dashboard', 'athletes', 'injury', 'rehabilitation', 'training', 'users'],
  DOCTOR: ['dashboard', 'athletes', 'injury', 'rehabilitation'],
  THERAPIST: ['dashboard', 'athletes', 'rehabilitation'],
  COACH: ['dashboard', 'athletes', 'training'],
  ATHLETE: ['dashboard', 'athletes']
}

const allMenus = [
  { name: 'dashboard', title: '仪表盘', icon: 'DataLine', path: '/dashboard' },
  { name: 'athletes', title: '运动员管理', icon: 'User', path: '/athletes' },
  { name: 'injury', title: '伤病记录', icon: 'Document', path: '/injury' },
  { name: 'rehabilitation', title: '康复计划', icon: 'Warning', path: '/rehabilitation' },
  { name: 'training', title: '训练计划', icon: 'Trophy', path: '/training' },
  { name: 'users', title: '用户管理', icon: 'Setting', path: '/users' }
]

const menus = computed(() => {
  const role = userStore.userInfo?.role
  const allowed = roleMenus[role] || []
  return allMenus.filter(m => allowed.includes(m.name))
})

const activeMenu = computed(() => route.path)

const getIcon = (name) => {
  const icons = { DataLine, User, Document, Warning, Trophy: TrophyIcon, Setting }
  return icons[name] || DataLine
}
</script>

<style scoped lang="scss">
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-section {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .logo {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, $primary-light, $accent-green-light);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .logo-icon {
      font-size: 20px;
      color: #fff;
    }
  }

  .logo-text {
    font-size: 22px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 2px;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  padding: 16px 0;

  :deep(.el-menu-item) {
    margin: 4px 12px;
    border-radius: 8px;
    height: 48px;
    line-height: 48px;
    transition: all 0.3s ease;

    &:hover {
      background-color: $sidebar-hover !important;
    }

    &.is-active {
      background: linear-gradient(135deg, $primary-color, $accent-green);
      color: #fff;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 24px;
        background-color: #fff;
        border-radius: 0 2px 2px 0;
      }
    }

    .el-icon {
      font-size: 18px;
      margin-right: 8px;
    }
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(135deg, $primary-color, $accent-green) !important;
  }
}
</style>
