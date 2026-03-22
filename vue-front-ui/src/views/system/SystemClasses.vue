<template>
    <div class="system-classes-container">
        <div class="toolbar">
            <el-input v-model="searchQuery" placeholder="搜索班级名称/学院/专业..." style="width: 250px" clearable
                @keyup.enter="fetchData" @clear="fetchData">
                <template #append>
                    <el-button @click="fetchData"><el-icon>
                            <Search />
                        </el-icon></el-button>
                </template>
            </el-input>
            <el-button type="primary" @click="handleAdd" style="margin-left: 15px;">新增班级</el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="className" label="班级名称" width="200" />
            <el-table-column prop="college" label="所属学院" width="180" />
            <el-table-column prop="major" label="专业" width="180" />
            <el-table-column prop="grade" label="年级" width="120" align="center" />
            <el-table-column prop="createTime" label="创建时间" width="180" align="center">
                <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="280" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="success" link @click="openAssignStudent(scope.row)">分配学生</el-button>
                    <el-button size="small" type="warning" link @click="openSetManager(scope.row)">设置管理员</el-button>
                    <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                    <el-popconfirm title="确定删除该班级吗？" @confirm="handleDelete(scope.row.id)">
                        <template #reference>
                            <el-button size="small" type="danger" link>删除</el-button>
                        </template>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" />
        </div>


        <!-- 分配学生弹窗 (分页多选) -->
        <el-dialog title="分配学生" v-model="assignStudentVisible" width="700px" @close="resetAssignStudent">
            <div style="display:flex; margin-bottom: 10px;">
                <el-input v-model="assignSearchKeyword" placeholder="搜索学号/姓名" style="width:200px" clearable
                    @keyup.enter="fetchAssignableStudents" @clear="fetchAssignableStudents"></el-input>
                <el-button type="primary" style="margin-left: 10px" @click="fetchAssignableStudents">搜索</el-button>
            </div>
            <el-table :data="assignableStudents" v-loading="assignLoading" style="width: 100%"
                @selection-change="handleSelectionChange" border height="300">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="username" label="用户名" width="120" />
                <el-table-column prop="realName" label="姓名" width="120" />
                <el-table-column prop="studentNo" label="学号" />
            </el-table>
            <div style="margin-top:10px; display:flex; justify-content:flex-end;">
                <el-pagination v-model:current-page="assignCurrentPage" v-model:page-size="assignPageSize"
                    :page-sizes="[10, 50, 100]" layout="total, prev, pager, next" :total="assignTotal"
                    @size-change="fetchAssignableStudents" @current-change="fetchAssignableStudents" />
            </div>
            <template #footer>
                <el-button @click="assignStudentVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAssignStudents" :loading="submitAssignLoading">确认分配</el-button>
            </template>
        </el-dialog>

        <!-- 设置管理员弹窗 (远程搜索单选) -->
        <el-dialog title="设置班级管理员" v-model="setManagerVisible" width="500px" @close="resetSetManager">
            <el-form label-width="120px">
                <el-form-item label="选择管理员">
                    <el-select v-model="selectedManagerId" filterable remote reserve-keyword placeholder="输入姓名或账号搜索"
                        :remote-method="searchManagers" :loading="managerSearchLoading" style="width: 100%">
                        <el-option v-for="item in managerOptions" :key="item.id"
                            :label="item.realName + ' (' + item.username + ')'" :value="item.id" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="setManagerVisible = false">取消</el-button>
                <el-button type="primary" @click="submitSetManager" :loading="submitManagerLoading">确认设置</el-button>
            </template>
        </el-dialog>

        <!-- 表单弹窗 -->
        <el-dialog :title="isEdit ? '编辑班级' : '新增班级'" v-model="dialogVisible" width="500px" @close="resetForm">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="班级名称" prop="className">
                    <el-input v-model="form.className" placeholder="例如：软件工程1班" />
                </el-form-item>
                <el-form-item label="所属学院" prop="college">
                    <el-input v-model="form.college" placeholder="例如：计算机科学与工程学院" />
                </el-form-item>
                <el-form-item label="专业" prop="major">
                    <el-input v-model="form.major" placeholder="例如：软件工程" />
                </el-form-item>
                <el-form-item label="年级" prop="grade">
                    <el-input v-model="form.grade" placeholder="例如：2023级" />
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
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/class'

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')

// 弹窗表单数据
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
    id: null as number | null,
    className: '',
    college: '',
    major: '',
    grade: ''
})

const rules = reactive<FormRules>({
    className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
    college: [{ required: true, message: '请输入所属学院', trigger: 'blur' }],
    major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
    grade: [{ required: true, message: '请输入年级', trigger: 'blur' }]
})

// 日期格式化
const formatDate = (dateString: string) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', { hour12: false })
}

// 获取数据列表
const fetchData = async () => {
    loading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get(`${API_BASE_URL}/page`, {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                current: currentPage.value,
                size: pageSize.value,
                keyword: searchQuery.value || undefined
            }
        })

        if (res.data.code === 200) {
            tableData.value = res.data.data.records
            total.value = res.data.data.total
        } else {
            ElMessage.error(res.data.msg || '获取数据失败')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('网络错误，请稍后重试')
    } finally {
        loading.value = false
    }
}

// 分页处理
const handleSizeChange = (val: number) => {
    pageSize.value = val
    fetchData()
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData()
}

// 新增操作
const handleAdd = () => {
    isEdit.value = false
    resetFormState()
    dialogVisible.value = true
}

// 编辑操作
const handleEdit = (row: any) => {
    isEdit.value = true
    Object.assign(form, row)
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
            // 如果当前页只有一条数据且不是第一页，则删除后跳到上一页
            if (tableData.value.length === 1 && currentPage.value > 1) {
                currentPage.value -= 1
            }
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

                let res
                if (isEdit.value) {
                    res = await axios.put(API_BASE_URL, form, config)
                } else {
                    res = await axios.post(API_BASE_URL, form, config)
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
    form.className = ''
    form.college = ''
    form.major = ''
    form.grade = ''
}


// ==== 分配学生相关 ====
const assignStudentVisible = ref(false)
const assignSearchKeyword = ref('')
const assignableStudents = ref<any[]>([])
const assignLoading = ref(false)
const assignCurrentPage = ref(1)
const assignPageSize = ref(10)
const assignTotal = ref(0)
const selectedStudentIds = ref<number[]>([])
const currentClassId = ref<number | null>(null)
const submitAssignLoading = ref(false)

const openAssignStudent = (row: any) => {
    currentClassId.value = row.id
    assignStudentVisible.value = true
    assignCurrentPage.value = 1
    assignSearchKeyword.value = ''
    fetchAssignableStudents()
}

const fetchAssignableStudents = async () => {
    assignLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/system/users/assignable', {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                roleId: 1, // 假设1为学生角色，可调整或不传
                unassignedClass: assignSearchKeyword.value ? undefined : true, // 默认看未分配的，如果有搜索词则全局搜索
                keyword: assignSearchKeyword.value || undefined,
                current: assignCurrentPage.value,
                size: assignPageSize.value
            }
        })
        if (res.data.code === 200) {
            assignableStudents.value = res.data.data.records
            assignTotal.value = res.data.data.total
        }
    } catch (e) {
        ElMessage.error('获取可分配学生失败')
    } finally {
        assignLoading.value = false
    }
}

const handleSelectionChange = (val: any[]) => {
    selectedStudentIds.value = val.map(v => v.id)
}

const submitAssignStudents = async () => {
    if (selectedStudentIds.value.length === 0) {
        ElMessage.warning('请至少选择一名学生')
        return
    }
    submitAssignLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.put(
            `http://localhost:8080/api/system/users/assign-class?classId=${currentClassId.value}`,
            selectedStudentIds.value,
            { headers: { Authorization: `Bearer ${token}` } }
        )
        if (res.data.code === 200) {
            ElMessage.success('分配成功')
            assignStudentVisible.value = false
        } else {
            ElMessage.error(res.data.msg || '分配失败')
        }
    } catch (e) {
        ElMessage.error('分配请求异常')
    } finally {
        submitAssignLoading.value = false
    }
}

const resetAssignStudent = () => {
    selectedStudentIds.value = []
    assignableStudents.value = []
}

// ==== 设置管理员相关 ====
const setManagerVisible = ref(false)
const managerOptions = ref<any[]>([])
const selectedManagerId = ref<number | null>(null)
const managerSearchLoading = ref(false)
const submitManagerLoading = ref(false)

const openSetManager = (row: any) => {
    currentClassId.value = row.id
    setManagerVisible.value = true
    selectedManagerId.value = null
    searchManagers('') // load initials
}

const searchManagers = async (query: string) => {
    managerSearchLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        // 可以传入特定 roleId, 比如 3 是班级管理员角色，如果没有可以不传
        const res = await axios.get('http://localhost:8080/api/system/users/assignable', {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                keyword: query || undefined,
                current: 1,
                size: 20 // 最多拉取前20个匹配
            }
        })
        if (res.data.code === 200) {
            managerOptions.value = res.data.data.records
        }
    } catch (e) {
        // ...
    } finally {
        managerSearchLoading.value = false
    }
}

const submitSetManager = async () => {
    if (!selectedManagerId.value) {
        ElMessage.warning('请选择一位管理员')
        return
    }
    submitManagerLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.put(
            `http://localhost:8080/api/manager-class/class/${currentClassId.value}/manager/${selectedManagerId.value}`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
        )
        if (res.data.code === 200) {
            ElMessage.success('设置成功')
            setManagerVisible.value = false
        } else {
            ElMessage.error(res.data.msg || '设置失败')
        }
    } catch (e) {
        ElMessage.error('请求异常')
    } finally {
        submitManagerLoading.value = false
    }
}

const resetSetManager = () => {
    managerOptions.value = []
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
.system-classes-container {
    padding: 10px;
}

.toolbar {
    display: flex;
    align-items: center;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>