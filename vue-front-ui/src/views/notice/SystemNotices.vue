<template>
    <div class="system-notices">
        <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAdd">发布新通知</el-button>
            <div style="flex: 1"></div>
            <el-input v-model="searchKeyword" placeholder="搜索通知标题" clearable style="width: 250px; margin-right: 10px" />
            <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
        </div>

        <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="publishScope" label="发布范围" width="120">
                <template #default="{ row }">
                    <el-tag v-if="row.publishScope === 1" type="success">全校</el-tag>
                    <el-tag v-else type="warning">指定班级: {{ getClassName(row.classId) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                    <el-tag v-if="row.status === 1" type="success">已发布</el-tag>
                    <el-tag v-else type="info">草稿</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                    <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
                    <el-popconfirm title="确定要删除此通知吗？" @confirm="handleDelete(row.id)">
                        <template #reference>
                            <el-button size="small" type="danger">删除</el-button>
                        </template>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next" :total="total" @size-change="fetchData"
                @current-change="fetchData" />
        </div>

        <!-- 表单弹窗 -->
        <el-dialog :title="dialogType === 'add' ? '发布通知' : '编辑通知'" v-model="dialogVisible" width="600px">
            <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="formData.title" placeholder="请输入通知标题" />
                </el-form-item>
                <el-form-item label="内容" prop="content">
                    <el-input type="textarea" v-model="formData.content" rows="5" placeholder="请输入通知内容" />
                </el-form-item>
                <el-form-item label="发布范围" prop="publishScope">
                    <el-radio-group v-model="formData.publishScope">
                        <el-radio :label="1" v-if="isAdmin">全校</el-radio>
                        <el-radio :label="2">指定班级</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="目标班级" prop="classId" v-if="formData.publishScope === 2">
                    <el-select v-model="formData.classId" placeholder="请选择班级" style="width: 100%" clearable>
                        <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio :label="1">立即发布</el-radio>
                        <el-radio :label="0">保存为草稿</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const classList = ref<any[]>([])
const isAdmin = ref(false)

const getClassName = (id: number) => {
    if (!id) return ''
    const cls = classList.value.find(c => c.id === id)
    return cls ? cls.className : id
}

const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)
const formRef = ref()

const formData = ref({
    id: undefined,
    title: '',
    content: '',
    publishScope: 1,
    classId: null,
    status: 1,
    publisherId: localStorage.getItem('userId') || 1
})

const rules = {
    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
    publishScope: [{ required: true, message: '请选择发布范围', trigger: 'change' }]
}

const getAuthHeaders = () => {
    return {
        'Authorization': 'Bearer ' + localStorage.getItem('token'),
        'Content-Type': 'application/json'
    }
}

const fetchData = async () => {
    loading.value = true
    try {
        const res = await fetch(`http://localhost:8080/api/notice/page?current=${currentPage.value}&size=${pageSize.value}&keyword=${searchKeyword.value}`, {
            headers: getAuthHeaders()
        })
        const json = await res.json()
        if (json.code === 200) {
            tableData.value = json.data.records
            total.value = json.data.total
        } else {
            ElMessage.error(json.message || '获取数据失败')
        }
    } catch (error) {
        ElMessage.error('网络请求失败')
    } finally {
        loading.value = false
    }
}

const fetchClassList = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/class/managed', {
            headers: getAuthHeaders()
        })
        const json = await res.json()
        if (json.code === 200) {
            classList.value = json.data
        }
    } catch (e) {
        console.error('获取班级列表失败', e)
    }
}

const handleAdd = () => {
    dialogType.value = 'add'
    formData.value = {
        id: undefined,
        title: '',
        content: '',
        publishScope: isAdmin.value ? 1 : 2,
        classId: null,
        status: 1,
        publisherId: localStorage.getItem('userId') || 1
    }
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    dialogType.value = 'edit'
    formData.value = { ...row }
    dialogVisible.value = true
}

const handleDelete = async (id: number) => {
    try {
        const res = await fetch(`http://localhost:8080/api/notice/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        })
        const json = await res.json()
        if (json.code === 200) {
            ElMessage.success('删除成功')
            fetchData()
        } else {
            ElMessage.error(json.message || '删除失败')
        }
    } catch (error) {
        ElMessage.error('网络请求失败')
    }
}

const handleSubmit = () => {
    formRef.value.validate(async (valid: boolean) => {
        if (!valid) return
        submitLoading.value = true
        const isAdd = dialogType.value === 'add'
        const url = 'http://localhost:8080/api/notice'
        const method = isAdd ? 'POST' : 'PUT'

        try {
            const res = await fetch(url, {
                method,
                headers: getAuthHeaders(),
                body: JSON.stringify(formData.value)
            })
            const json = await res.json()
            if (json.code === 200) {
                ElMessage.success(isAdd ? '发布成功' : '修改成功')
                dialogVisible.value = false
                fetchData()
            } else {
                ElMessage.error(json.message || '操作失败')
            }
        } catch (error) {
            ElMessage.error('网络请求失败')
        } finally {
            submitLoading.value = false
        }
    })
}

onMounted(() => {
    const rolesStr = localStorage.getItem('roles')
    if (rolesStr) {
        try {
            const roles = JSON.parse(rolesStr)
            isAdmin.value = roles.includes('admin') || roles.includes('system_admin')
        } catch (e) { }
    }
    fetchClassList()
    fetchData()
})
</script>

<style scoped>
.toolbar {
    display: flex;
    align-items: center;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
