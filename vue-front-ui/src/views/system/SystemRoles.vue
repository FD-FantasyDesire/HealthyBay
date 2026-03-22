<template>
    <div class="system-roles-container">
        <div class="toolbar">
            <el-button type="primary" @click="handleAddRole">新增角色</el-button>
        </div>

        <!-- 角色表格 -->
        <el-table v-loading="loading" :data="rolesData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="roleName" label="角色名称" width="180" />
            <el-table-column prop="roleKey" label="角色标识" width="180" />
            <el-table-column prop="createTime" label="创建时间" />
            <el-table-column label="操作" width="220" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="primary" link @click="handleEditRole(scope.row)">编辑</el-button>
                    <el-button size="small" type="success" link
                        @click="handleAssignPermissions(scope.row)">分配权限</el-button>
                    <el-popconfirm title="确定要删除该角色吗？" @confirm="handleDeleteRole(scope.row.id)">
                        <template #reference>
                            <el-button size="small" type="danger" link>删除</el-button>
                        </template>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next" :total="total" @size-change="fetchRoles"
                @current-change="fetchRoles" />
        </div>

        <!-- 新增/编辑角色弹窗 -->
        <el-dialog :title="isEditRole ? '编辑角色' : '新增角色'" v-model="roleDialogVisible" width="500px">
            <el-form :model="roleForm" label-width="100px">
                <el-form-item label="角色名称">
                    <el-input v-model="roleForm.roleName" placeholder="例如：班级管理员" />
                </el-form-item>
                <el-form-item label="角色标识">
                    <el-input v-model="roleForm.roleKey" placeholder="例如：class_admin" :disabled="isEditRole" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="roleDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitRoleForm">确定</el-button>
            </template>
        </el-dialog>

        <!-- 分配权限弹窗 -->
        <el-dialog title="分配权限" v-model="permDialogVisible" width="600px">
            <div v-loading="permLoading">
                <p>当前配置角色：<strong>{{ currentAssigningRole?.roleName }}</strong></p>
                <div style="margin-top: 15px; max-height: 400px; overflow-y: auto;">
                    <el-checkbox-group v-model="checkedPermIds">
                        <el-row>
                            <el-col :span="12" v-for="perm in allPermissions" :key="perm.id"
                                style="margin-bottom: 10px;">
                                <el-checkbox :label="perm.id" :value="perm.id">{{ perm.permName }} ({{ perm.permKey
                                    }})</el-checkbox>
                            </el-col>
                        </el-row>
                    </el-checkbox-group>
                </div>
            </div>
            <template #footer>
                <el-button @click="permDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAssignPermissions" :loading="permSubmitLoading">保存配置</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/system/roles'

// ================= 角色列表 =================
const loading = ref(false)
const rolesData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const fetchRoles = async () => {
    loading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get(`${API_BASE}/page`, {
            headers: { Authorization: `Bearer ${token}` },
            params: { current: currentPage.value, size: pageSize.value }
        })
        if (res.data.code === 200) {
            rolesData.value = res.data.data.records
            total.value = res.data.data.total
        } else {
            ElMessage.error(res.data.msg || '获取角色失败')
        }
    } catch (error) {
        ElMessage.error('网络错误')
    } finally {
        loading.value = false
    }
}

// ================= 角色增删改 =================
const roleDialogVisible = ref(false)
const isEditRole = ref(false)
const roleForm = ref({ id: null, roleName: '', roleKey: '' })

const handleAddRole = () => {
    isEditRole.value = false
    roleForm.value = { id: null, roleName: '', roleKey: '' }
    roleDialogVisible.value = true
}

const handleEditRole = (row: any) => {
    isEditRole.value = true
    roleForm.value = { id: row.id, roleName: row.roleName, roleKey: row.roleKey }
    roleDialogVisible.value = true
}

const submitRoleForm = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const config = { headers: { Authorization: `Bearer ${token}` } }
        let res
        if (isEditRole.value) {
            res = await axios.put(API_BASE, roleForm.value, config)
        } else {
            res = await axios.post(API_BASE, roleForm.value, config)
        }
        if (res.data.code === 200) {
            ElMessage.success('操作成功')
            roleDialogVisible.value = false
            fetchRoles()
        } else {
            ElMessage.error(res.data.msg || '操作失败')
        }
    } catch (error) {
        ElMessage.error('网络错误')
    }
}

const handleDeleteRole = async (id: number) => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.delete(`${API_BASE}/${id}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            ElMessage.success('删除成功')
            fetchRoles()
        } else {
            ElMessage.error(res.data.msg || '删除失败')
        }
    } catch (error) {
        ElMessage.error('网络错误')
    }
}

// ================= 分配权限 =================
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSubmitLoading = ref(false)
const currentAssigningRole = ref<any>(null)
const allPermissions = ref<any[]>([])
const checkedPermIds = ref<number[]>([])

// 挂载时不仅要拉角色，也可以提前把所有权限字典拉回来缓存
const fetchAllPermissions = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get(`${API_BASE}/permissions`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            allPermissions.value = res.data.data
        }
    } catch (error) {
        console.error('获取权限字典失败', error)
    }
}

const handleAssignPermissions = async (role: any) => {
    currentAssigningRole.value = role
    permDialogVisible.value = true
    permLoading.value = true
    checkedPermIds.value = []

    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get(`${API_BASE}/${role.id}/permissions`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            checkedPermIds.value = res.data.data // 填入已绑定的权限ID数组
        }
    } catch (error) {
        ElMessage.error('获取当前角色权限失败')
    } finally {
        permLoading.value = false
    }
}

const submitAssignPermissions = async () => {
    if (!currentAssigningRole.value) return
    permSubmitLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.post(
            `${API_BASE}/${currentAssigningRole.value.id}/permissions`,
            checkedPermIds.value,
            { headers: { Authorization: `Bearer ${token}` } }
        )
        if (res.data.code === 200) {
            ElMessage.success('权限配置成功')
            permDialogVisible.value = false
        } else {
            ElMessage.error(res.data.msg || '权限配置失败')
        }
    } catch (error) {
        ElMessage.error('网络错误')
    } finally {
        permSubmitLoading.value = false
    }
}

onMounted(() => {
    fetchRoles()
    fetchAllPermissions()
})
</script>

<style scoped>
.system-roles-container {
    padding: 10px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
