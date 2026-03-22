<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">健康湾</div>
      <el-menu :default-active="activeMenu" class="el-menu-vertical" background-color="#304156" text-color="#bfcbd9"
        active-text-color="#409EFF" @select="handleMenuSelect">
        <template v-if="hasPermission('dashboard:view')">
          <el-menu-item index="dashboard">
            <el-icon>
              <DataLine />
            </el-icon>
            <span>控制台</span>
          </el-menu-item>
        </template>

        <!-- 学生菜单 -->
        <template v-if="hasPermission('health:report')">
          <el-menu-item index="health-report">
            <el-icon>
              <Edit />
            </el-icon>
            <span>健康上报</span>
          </el-menu-item>
        </template>
        <template v-if="hasPermission('notice:view')">
          <el-menu-item index="my-notices">
            <el-icon>
              <Message />
            </el-icon>
            <span>通知查询</span>
          </el-menu-item>
        </template>

        <!-- 班级管理员菜单 -->
        <template v-if="hasPermission('class:dashboard:view')">
          <el-menu-item index="class-dashboard">
            <el-icon>
              <Odometer />
            </el-icon>
            <span>班级看板</span>
          </el-menu-item>
        </template>
        <template v-if="hasPermission('warning:view')">
          <el-menu-item index="warnings">
            <el-icon>
              <Warning />
            </el-icon>
            <span>预警处理</span>
          </el-menu-item>
        </template>
        <template v-if="hasPermission('notice:publish')">
          <el-menu-item index="send-notice">
            <el-icon>
              <Position />
            </el-icon>
            <span>发布通知</span>
          </el-menu-item>
        </template>

        <!-- 系统管理员菜单 -->
        <template
          v-if="hasPermission('system:setting') || hasPermission('user:manage') || hasPermission('role:manage') || hasPermission('class:manage') || hasPermission('disease:manage') || hasPermission('rule:manage')">
          <el-sub-menu index="system">
            <template #title>
              <el-icon>
                <Setting />
              </el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="system-users" v-if="hasPermission('user:manage')">用户管理</el-menu-item>
            <el-menu-item index="system-roles" v-if="hasPermission('role:manage')">角色权限管理</el-menu-item>
            <el-menu-item index="system-classes" v-if="hasPermission('class:manage')">班级管理</el-menu-item>
            <el-menu-item index="system-area" v-if="hasPermission('system:setting')">楼栋房间管理</el-menu-item>
            <el-menu-item index="system-diseases" v-if="hasPermission('disease:manage')">传染病字典</el-menu-item>
            <el-menu-item index="system-rules" v-if="hasPermission('rule:manage')">预警规则配置</el-menu-item>
            <el-menu-item index="system-logs" v-if="hasPermission('log:manage')">审计日志管理</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentMenuTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <span class="username-text">当前用户: {{ currentUsername }} [{{ userRoles.join(', ') }}]</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <!-- 动态组件区域，用于占位和展示虚拟数据 -->
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>{{ currentMenuTitle }}</span>
            </div>
          </template>

          <div v-if="activeMenu === 'dashboard'">
            <Dashboard />
          </div>

          <div v-else-if="activeMenu === 'health-report'">
            <HealthReport />
          </div>

          <div v-else-if="activeMenu === 'my-notices'">
            <MyNotices />
          </div>

          <div v-else-if="activeMenu === 'class-dashboard'">
            <ClassDashboard />
          </div>

          <div v-else-if="activeMenu === 'warnings'">
            <WarningsRecord />
          </div>

          <div v-else-if="activeMenu === 'send-notice'">
            <SystemNotices />
          </div>

          <!-- 系统管理: 传染病字典 -->
          <div v-else-if="activeMenu === 'system-diseases'">
            <SystemDiseases />
          </div>

          <!-- 系统管理: 预警规则配置 -->
          <div v-else-if="activeMenu === 'system-rules'">
            <SystemRules />
          </div>

          <!-- 系统管理: 用户管理 -->
          <div v-else-if="activeMenu === 'system-users'">
            <SystemUsers />
          </div>

          <!-- 系统管理: 角色权限管理 -->
          <div v-else-if="activeMenu === 'system-roles'">
            <SystemRoles />
          </div>

          <!-- 系统管理: 班级管理 -->
          <div v-else-if="activeMenu === 'system-classes'">
            <SystemClasses />
          </div>

          <!-- 系统管理: 楼栋房间管理 -->
          <div v-else-if="activeMenu === 'system-area'">
            <SystemArea />
          </div>

          <!-- 系统管理: 审计日志管理 -->
          <div v-else-if="activeMenu === 'system-logs'">
            <SystemLogs />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  DataLine,
  Edit,
  Message,
  Odometer,
  Warning,
  Position,
  Setting,
  ArrowDown
} from '@element-plus/icons-vue'

// 导入独立子组件
import SystemDiseases from './system/SystemDiseases.vue'
import SystemUsers from './system/SystemUsers.vue'
import SystemRoles from './system/SystemRoles.vue'
import SystemArea from './system/SystemArea.vue'
import SystemClasses from './system/SystemClasses.vue'
import SystemRules from './system/SystemRules.vue'
import SystemLogs from './system/OperationLog.vue'
import WarningsRecord from './warnings/index.vue'
import SystemNotices from './notice/SystemNotices.vue'
import MyNotices from './notice/MyNotices.vue'
import HealthReport from './health/index.vue'
import ClassDashboard from './class-dashboard/index.vue'
import Dashboard from './dashboard/index.vue'

const router = useRouter()

const currentUserId = ref('')
const currentUsername = ref('')
const userRoles = ref<string[]>([])
const userPermissions = ref<string[]>([])
const activeMenu = ref('dashboard')

// 菜单标题映射表
const menuTitleMap: Record<string, string> = {
  'dashboard': '控制台',
  'health-report': '健康上报',
  'my-notices': '通知查询',
  'class-dashboard': '班级看板',
  'warnings': '预警处理',
  'send-notice': '发布通知',
  'system-users': '用户管理',
  'system-roles': '角色权限管理',
  'system-classes': '班级管理',
  'system-area': '楼栋房间管理',
  'system-diseases': '传染病字典',
  'system-rules': '预警规则配置'
}

const currentMenuTitle = computed(() => {
  return menuTitleMap[activeMenu.value] || '未知模块'
})

onMounted(() => {
  // 从本地存储获取用户信息和角色权限
  const userId = localStorage.getItem('userId')
  const username = localStorage.getItem('username')

  if (userId) {
    currentUserId.value = userId
    currentUsername.value = username || '未知用户'

    // 尝试从 localStorage 读取角色的配置信息
    const rolesStr = localStorage.getItem('roles')
    if (rolesStr) {
      try {
        const parsed = JSON.parse(rolesStr)
        if (Array.isArray(parsed) && parsed.length > 0) {
          userRoles.value = parsed
        } else {
          userRoles.value = ['student']
        }
      } catch (e) {
        userRoles.value = ['student'] // 解析错误默认最低权限
      }
    } else {
      userRoles.value = ['student'] // 没有则默认最低权限学生
    }

    // 从 localStorage 读取权限配置信息
    const permsStr = localStorage.getItem('permissions')
    if (permsStr) {
      try {
        const parsed = JSON.parse(permsStr)
        if (Array.isArray(parsed)) {
          userPermissions.value = parsed
        }
      } catch (e) {
        userPermissions.value = []
      }
    }
  } else {
    // 默认给个角色方便演示，实际应跳回登录
    userRoles.value = []
    userPermissions.value = []
  }
})

const hasRole = (role: string) => {
  // admin或system_admin都视为超级管理员，拥有所有权限
  const isAdmin = userRoles.value.includes('admin') || userRoles.value.includes('system_admin');

  // 如果请求判断的本身就是admin权限，则直接返回isAdmin结果
  if (role === 'admin') {
    return isAdmin;
  }

  // 否则，如果是具体的菜单权限（如student），那么要么自身包含，要么是admin
  return userRoles.value.includes(role) || isAdmin;
}

const hasPermission = (permission: string) => {
  // admin或system_admin都视为超级管理员，拥有所有权限
  const isAdmin = userRoles.value.includes('admin') || userRoles.value.includes('system_admin');
  if (isAdmin) return true;

  return userPermissions.value.includes(permission);
}

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('roles')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.aside {
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  background-color: #2b3649;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
}

.username-text {
  font-size: 14px;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}

.content-card {
  min-height: 400px;
  border-radius: 8px;
}

.dummy-content {
  padding: 20px 0;
}
</style>
