<template>
    <div class="system-area-container">
        <div class="toolbar">
            <el-input v-model="searchQuery" placeholder="搜索区域/楼栋/房间名称或代码..." style="width: 250px" clearable
                @keyup.enter="fetchData" @clear="fetchData">
                <template #append>
                    <el-button @click="fetchData"><el-icon>
                            <Search />
                        </el-icon></el-button>
                </template>
            </el-input>
            <el-select v-model="searchType" placeholder="层级类型" clearable style="width: 150px; margin-left: 15px;"
                @change="fetchData">
                <el-option label="全部" :value="null" />
                <el-option label="区域/校区" :value="1" />
                <el-option label="楼栋" :value="2" />
                <el-option label="房间" :value="3" />
            </el-select>
            <el-button type="primary" @click="handleAdd(null)" style="margin-left: 15px;">新增顶层区域</el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" row-key="id" border
            default-expand-all :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
            <el-table-column prop="areaName" label="名称" width="250" />
            <el-table-column prop="areaCode" label="代码" width="150" />
            <el-table-column prop="areaType" label="类型" width="120" align="center">
                <template #default="scope">
                    <el-tag :type="getTypeTag(scope.row.areaType)">
                        {{ getTypeText(scope.row.areaType) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="level" label="层级深度" width="100" align="center" />
            <el-table-column label="操作" width="350" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="success" link @click="handleAdd(scope.row)"
                        v-if="scope.row.areaType < 3">新增子节点</el-button>
                    <el-button size="small" type="success" link @click="openAssignRoom(scope.row)"
                        v-if="scope.row.areaType === 3">分配人员</el-button>

                    <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                    <el-popconfirm title="确定删除该节点吗？如果存在子节点将无法删除" @confirm="handleDelete(scope.row.id)">
                        <template #reference>
                            <el-button size="small" type="danger" link>删除</el-button>
                        </template>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>


        <!-- 分配人员弹窗 (分页多选) -->
        <el-dialog title="分配人员到房间" v-model="assignRoomVisible" width="700px" @close="resetAssignRoom">
            <div style="display:flex; margin-bottom: 10px;">
                <el-input v-model="assignSearchKeyword" placeholder="搜索学号/姓名" style="width:200px" clearable
                    @keyup.enter="fetchAssignableUsers" @clear="fetchAssignableUsers"></el-input>
                <el-button type="primary" style="margin-left: 10px" @click="fetchAssignableUsers">搜索</el-button>
            </div>
            <el-table :data="assignableUsers" v-loading="assignLoading" style="width: 100%"
                @selection-change="handleSelectionChange" border height="300">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="realName" label="姓名" width="120" />
                <el-table-column prop="studentNo" label="学号/工号" />
            </el-table>
            <div style="margin-top:10px; display:flex; justify-content:flex-end;">
                <el-pagination v-model:current-page="assignCurrentPage" v-model:page-size="assignPageSize"
                    :page-sizes="[10, 50, 100]" layout="total, prev, pager, next" :total="assignTotal"
                    @size-change="fetchAssignableUsers" @current-change="fetchAssignableUsers" />
            </div>
            <template #footer>
                <el-button @click="assignRoomVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAssignRoom" :loading="submitAssignLoading">确认分配</el-button>
            </template>
        </el-dialog>



        <!-- 表单弹窗 -->
        <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="上级节点">
                    <el-input v-model="parentName" disabled placeholder="无（顶层区域）" />
                </el-form-item>
                <el-form-item label="节点类型" prop="areaType">
                    <el-radio-group v-model="form.areaType" :disabled="isEdit || form.parentId !== 0">
                        <el-radio :label="1">区域/校区</el-radio>
                        <el-radio :label="2">楼栋</el-radio>
                        <el-radio :label="3">房间</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="名称" prop="areaName">
                    <el-input v-model="form.areaName" placeholder="请输入名称，如：东校区、1号楼、101室" />
                </el-form-item>
                <el-form-item label="代码" prop="areaCode">
                    <el-input v-model="form.areaCode" placeholder="请输入代码，如：E-CAMPUS、B1、R101" />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/area'

// 表格数据
const loading = ref(false)
const tableData = ref([])
const searchQuery = ref('')
const searchType = ref<number | null>(null)

// 弹窗表单数据
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const parentName = ref('')

const form = reactive({
    id: null as number | null,
    areaName: '',
    areaType: 1,
    parentId: 0,
    level: 1,
    areaCode: ''
})

const dialogTitle = computed(() => {
    if (isEdit.value) return '编辑节点'
    return form.parentId === 0 ? '新增顶层区域' : '新增子节点'
})

const rules = reactive<FormRules>({
    areaName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    areaCode: [{ required: true, message: '请输入代码', trigger: 'blur' }],
    areaType: [{ required: true, message: '请选择节点类型', trigger: 'change' }]
})

// 获取树形数据
const fetchData = async () => {
    loading.value = true
    try {
        const token = localStorage.getItem('token') || ''

        if (searchQuery.value || searchType.value) {
            const res = await axios.get(`${API_BASE_URL}/page`, {
                headers: { Authorization: `Bearer ${token}` },
                params: {
                    current: 1,
                    size: 1000,
                    keyword: searchQuery.value || undefined,
                    areaType: searchType.value || undefined
                }
            })
            if (res.data.code === 200) {
                tableData.value = res.data.data.records
            } else {
                ElMessage.error(res.data.msg || '获取数据失败')
            }
        } else {
            const res = await axios.get(`${API_BASE_URL}/tree`, {
                headers: { Authorization: `Bearer ${token}` }
            })
            if (res.data.code === 200) {
                tableData.value = res.data.data
            } else {
                ElMessage.error(res.data.msg || '获取数据失败')
            }
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('网络错误，请稍后重试')
    } finally {
        loading.value = false
    }
}

// 格式化类型显示
const getTypeText = (type: number) => {
    const map: Record<number, string> = { 1: '区域/校区', 2: '楼栋', 3: '房间' }
    return map[type] || '未知'
}

const getTypeTag = (type: number) => {
    const map: Record<number, string> = { 1: '', 2: 'success', 3: 'warning' }
    return map[type] || 'info'
}

// 新增操作
const handleAdd = (row: any | null) => {
    isEdit.value = false
    resetFormState()

    if (row) {
        form.parentId = row.id
        form.level = row.level + 1
        form.areaType = row.areaType + 1
        parentName.value = row.areaName
    } else {
        form.parentId = 0
        form.level = 1
        form.areaType = 1
        parentName.value = '无（顶层区域）'
    }

    dialogVisible.value = true
}

// 编辑操作
const handleEdit = (row: any) => {
    isEdit.value = true
    Object.assign(form, row)
    parentName.value = row.parentId === 0 ? '无（顶层区域）' : '（上级节点）'
    dialogVisible.value = true
}

// 删除操作
const handleDelete = async (id: number) => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.delete(`${API_BASE_URL}/${id}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            ElMessage.success('删除成功')
            fetchData()
        } else {
            ElMessage.error(res.data.msg || '删除失败')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('网络错误，请稍后重试')
    }
}

// 表单提交
const submitForm = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitLoading.value = true
            try {
                const token = localStorage.getItem('token') || ''
                const config = { headers: { Authorization: `Bearer ${token}` } }

                const submitData = { ...form }

                let res
                if (isEdit.value) {
                    res = await axios.put(API_BASE_URL, submitData, config)
                } else {
                    res = await axios.post(API_BASE_URL, submitData, config)
                }

                if (res.data.code === 200) {
                    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
                    dialogVisible.value = false
                    fetchData()
                } else {
                    ElMessage.error(res.data.msg || '操作失败')
                }
            } catch (error) {
                console.error(error)
                ElMessage.error('网络错误，请稍后重试')
            } finally {
                submitLoading.value = false
            }
        }
    })
}

// 重置表单对象
const resetFormState = () => {
    form.id = null
    form.areaName = ''
    form.areaCode = ''
    form.areaType = 1
    form.parentId = 0
    form.level = 1
}


// ==== 分配人员相关 ====
const assignRoomVisible = ref(false)
const assignSearchKeyword = ref('')
const assignableUsers = ref<any[]>([])
const assignLoading = ref(false)
const assignCurrentPage = ref(1)
const assignPageSize = ref(10)
const assignTotal = ref(0)
const selectedUserIds = ref<number[]>([])
const currentRoomId = ref<number | null>(null)
const submitAssignLoading = ref(false)

const openAssignRoom = (row: any) => {
    currentRoomId.value = row.id
    assignRoomVisible.value = true
    assignCurrentPage.value = 1
    assignSearchKeyword.value = ''
    fetchAssignableUsers()
}

const fetchAssignableUsers = async () => {
    assignLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/system/users/assignable', {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                roleId: 1, // 仅查询学生
                unassignedRoom: assignSearchKeyword.value ? undefined : true, // 如果输入了搜索词，则全局搜索（包含已分配的），便于调换房间
                keyword: assignSearchKeyword.value || undefined,
                current: assignCurrentPage.value,
                size: assignPageSize.value
            }
        })
        if (res.data.code === 200) {
            assignableUsers.value = res.data.data.records
            assignTotal.value = res.data.data.total
        }
    } catch (e) {
        ElMessage.error('获取可分配人员失败')
    } finally {
        assignLoading.value = false
    }
}

const handleSelectionChange = (val: any[]) => {
    selectedUserIds.value = val.map(v => v.id)
}

const submitAssignRoom = async () => {
    if (selectedUserIds.value.length === 0) {
        ElMessage.warning('请至少选择一名人员')
        return
    }
    submitAssignLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.put(
            `http://localhost:8080/api/system/users/assign-room?roomId=${currentRoomId.value}`,
            selectedUserIds.value,
            { headers: { Authorization: `Bearer ${token}` } }
        )
        if (res.data.code === 200) {
            ElMessage.success('分配成功')
            assignRoomVisible.value = false
        } else {
            ElMessage.error(res.data.msg || '分配失败')
        }
    } catch (e) {
        ElMessage.error('分配请求异常')
    } finally {
        submitAssignLoading.value = false
    }
}

const resetAssignRoom = () => {
    selectedUserIds.value = []
    assignableUsers.value = []
}



// 弹窗关闭时重置校验状态
const resetForm = () => {
    if (formRef.value) {
        formRef.value.resetFields()
    }
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.system-area-container {
    padding: 10px;
}

.toolbar {
    display: flex;
    align-items: center;
}
</style>
