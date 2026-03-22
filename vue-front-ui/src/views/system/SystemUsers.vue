<template>
    <div class="system-users-container">
        <div class="toolbar">
            <el-input v-model="searchQuery" placeholder="搜索用户名、姓名或学号..." style="width: 250px" clearable
                @keyup.enter="fetchData" @clear="fetchData">
                <template #append>
                    <el-button @click="fetchData"><el-icon>
                            <Search />
                        </el-icon></el-button>
                </template>
            </el-input>
            <el-button type="primary" @click="handleAdd" style="margin-left: 15px;">新增用户</el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="真实姓名" width="120" />
            <el-table-column prop="studentNo" label="学号/工号" width="150" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                    <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" active-text="正常"
                        inactive-text="禁用" inline-prompt
                        @change="(val) => handleStatusChange(scope.row.id, val as number)" />
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="220" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="success" link @click="handleAssignRoles(scope.row)">分配角色</el-button>
                    <el-popconfirm title="确定要删除该用户吗？" @confirm="handleDelete(scope.row.id)">
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

        <!-- 用户表单弹窗 -->
        <el-dialog :title="isEdit ? '编辑用户' : '新增用户'" v-model="dialogVisible" width="600px" @close="resetForm">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="form.username" placeholder="登录账号（必填）" :disabled="isEdit" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="form.password" type="password" placeholder="不填则默认为123456" show-password />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="form.realName" placeholder="用户的真实姓名" />
                </el-form-item>
                <el-form-item label="学号/工号" prop="studentNo">
                    <el-input v-model="form.studentNo" placeholder="学生学号或教师工号" />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="form.gender">
                        <el-radio :label="1">男</el-radio>
                        <el-radio :label="2">女</el-radio>
                        <el-radio :label="0">未知</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone" placeholder="11位手机号" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="form.email" placeholder="电子邮箱" />
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

const API_BASE_URL = 'http://localhost:8080/api/system/users'

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
    username: '',
    password: '',
    realName: '',
    studentNo: '',
    gender: 0,
    phone: '',
    email: '',
    status: 1
})

const rules = reactive<FormRules>({
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
})

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

// 状态切换
const handleStatusChange = async (id: number, status: number) => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.put(`${API_BASE_URL}/${id}/status/${status}`, {}, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            ElMessage.success('状态更新成功')
        } else {
            ElMessage.error(res.data.msg || '状态更新失败')
            fetchData() // 回滚视图
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('网络错误，请稍后重试')
        fetchData() // 回滚视图
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
    form.password = '' // 编辑时不显示密码
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
    form.username = ''
    form.password = ''
    form.realName = ''
    form.studentNo = ''
    form.gender = 0
    form.phone = ''
    form.email = ''
    form.status = 1
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
.system-users-container {
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
