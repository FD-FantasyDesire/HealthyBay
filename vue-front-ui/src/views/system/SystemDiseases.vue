<template>
    <div class="system-diseases-container">
        <div class="toolbar">
            <el-input v-model="searchQuery" placeholder="搜索传染病名称..." style="width: 250px" clearable
                @keyup.enter="fetchData" @clear="fetchData">
                <template #append>
                    <el-button @click="fetchData"><el-icon>
                            <Search />
                        </el-icon></el-button>
                </template>
            </el-input>
            <el-button type="primary" @click="handleAdd" style="margin-left: 15px;">新增传染病字典</el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="diseaseName" label="疾病名称" width="150" />
            <el-table-column prop="diseaseCode" label="代码" width="120" />
            <el-table-column prop="severityLevel" label="严重程度" width="100" align="center">
                <template #default="scope">
                    <el-tag :type="getSeverityTagType(scope.row.severityLevel)">
                        {{ getSeverityText(scope.row.severityLevel) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="incubationPeriod" label="潜伏期" width="120" />
            <el-table-column prop="symptoms" label="常见症状" show-overflow-tooltip />
            <el-table-column prop="preventionMeasures" label="预防措施" show-overflow-tooltip />
            <el-table-column label="操作" width="150" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                    <el-popconfirm title="确定删除该记录吗？" @confirm="handleDelete(scope.row.id)">
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

        <!-- 表单弹窗 -->
        <el-dialog :title="isEdit ? '编辑传染病' : '新增传染病'" v-model="dialogVisible" width="600px" @close="resetForm">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="疾病名称" prop="diseaseName">
                    <el-input v-model="form.diseaseName" placeholder="例如：流行性感冒" />
                </el-form-item>
                <el-form-item label="代码" prop="diseaseCode">
                    <el-input v-model="form.diseaseCode" placeholder="例如：INF-001" />
                </el-form-item>
                <el-form-item label="严重程度" prop="severityLevel">
                    <el-radio-group v-model="form.severityLevel">
                        <el-radio :label="1">轻微</el-radio>
                        <el-radio :label="2">中度</el-radio>
                        <el-radio :label="3">严重</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="潜伏期" prop="incubationPeriod">
                    <el-input v-model="form.incubationPeriod" placeholder="例如：1-4天" />
                </el-form-item>
                <el-form-item label="常见症状" prop="symptoms">
                    <el-input type="textarea" v-model="form.symptoms" :rows="3" placeholder="描述该疾病的常见症状..." />
                </el-form-item>
                <el-form-item label="预防措施" prop="preventionMeasures">
                    <el-input type="textarea" v-model="form.preventionMeasures" :rows="3" placeholder="描述预防该疾病的措施..." />
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

const API_BASE_URL = 'http://localhost:8080/api/system/diseases'

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
    diseaseName: '',
    diseaseCode: '',
    severityLevel: 1,
    incubationPeriod: '',
    symptoms: '',
    preventionMeasures: ''
})

const rules = reactive<FormRules>({
    diseaseName: [{ required: true, message: '请输入疾病名称', trigger: 'blur' }],
    diseaseCode: [{ required: true, message: '请输入疾病代码', trigger: 'blur' }],
    severityLevel: [{ required: true, message: '请选择严重程度', trigger: 'change' }]
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
                diseaseName: searchQuery.value || undefined
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

// 格式化严重程度显示
const getSeverityText = (level: number) => {
    const map: Record<number, string> = { 1: '轻微', 2: '中度', 3: '严重' }
    return map[level] || '未知'
}

const getSeverityTagType = (level: number) => {
    const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger' }
    return map[level] || 'info'
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

    await formRef.value.validate(async (valid, fields) => {
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
    form.diseaseName = ''
    form.diseaseCode = ''
    form.severityLevel = 1
    form.incubationPeriod = ''
    form.symptoms = ''
    form.preventionMeasures = ''
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
.system-diseases-container {
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
