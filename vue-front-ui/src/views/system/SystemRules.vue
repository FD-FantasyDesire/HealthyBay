<template>
    <div class="system-rules-container">
        <div class="toolbar">
            <el-input v-model="searchQuery" placeholder="搜索规则名称..." style="width: 250px" clearable
                @keyup.enter="fetchData" @clear="fetchData">
                <template #append>
                    <el-button @click="fetchData"><el-icon>
                            <Search />
                        </el-icon></el-button>
                </template>
            </el-input>
            <el-select v-model="searchEnabled" placeholder="状态" clearable style="width: 150px; margin-left: 15px;"
                @change="fetchData">
                <el-option label="全部" :value="null" />
                <el-option label="已启用" :value="1" />
                <el-option label="已禁用" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleAdd" style="margin-left: 15px;">新增预警规则</el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="ruleName" label="规则名称" width="200" />

            <el-table-column label="作用范围" width="180">
                <template #default="scope">
                    <span v-if="!scope.row.classScope">全校所有班级</span>
                    <span v-else>指定班级 (ID: {{ scope.row.classScope }})</span>
                </template>
            </el-table-column>

            <el-table-column label="监测疾病" width="180">
                <template #default="scope">
                    <span v-if="!scope.row.diseaseId">任何异常/疾病</span>
                    <span v-else>特定疾病 (ID: {{ scope.row.diseaseId }})</span>
                </template>
            </el-table-column>

            <el-table-column label="触发条件" width="200">
                <template #default="scope">
                    {{ scope.row.timeWindowHours }}小时内达{{ scope.row.thresholdCount }}人
                </template>
            </el-table-column>

            <el-table-column prop="riskLevel" label="触发风险等级" width="120" align="center">
                <template #default="scope">
                    <el-tag :type="getRiskTagType(scope.row.riskLevel)">
                        {{ getRiskText(scope.row.riskLevel) }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column prop="enabled" label="状态" width="100" align="center">
                <template #default="scope">
                    <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0"
                        @change="(val: any) => handleStatusChange(scope.row, val as number)" />
                </template>
            </el-table-column>

            <el-table-column label="操作" width="150" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                    <el-popconfirm title="确定删除该规则吗？" @confirm="handleDelete(scope.row.id)">
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
        <el-dialog :title="isEdit ? '编辑预警规则' : '新增预警规则'" v-model="dialogVisible" width="600px" @close="resetForm">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="规则名称" prop="ruleName">
                    <el-input v-model="form.ruleName" placeholder="例如：全校流感爆发预警" />
                </el-form-item>

                <el-form-item label="作用班级" prop="classScope">
                    <el-select v-model="form.classScope" placeholder="不选则表示全校所有班级" clearable style="width: 100%">
                        <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
                    </el-select>
                </el-form-item>

                <el-form-item label="监测疾病" prop="diseaseId">
                    <el-select v-model="form.diseaseId" placeholder="不选则表示监测所有异常数据" clearable style="width: 100%">
                        <el-option v-for="item in diseaseList" :key="item.id" :label="item.diseaseName"
                            :value="item.id" />
                    </el-select>
                </el-form-item>

                <el-form-item label="时间窗口(小时)" prop="timeWindowHours">
                    <el-input-number v-model="form.timeWindowHours" :min="1" :max="720" style="width: 150px" />
                </el-form-item>

                <el-form-item label="触发阈值(人数)" prop="thresholdCount">
                    <el-input-number v-model="form.thresholdCount" :min="1" :max="1000" style="width: 150px" />
                </el-form-item>

                <el-form-item label="生成风险等级" prop="riskLevel">
                    <el-radio-group v-model="form.riskLevel">
                        <el-radio :label="1">低风险</el-radio>
                        <el-radio :label="2">中风险</el-radio>
                        <el-radio :label="3">高风险</el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="是否启用" prop="enabled">
                    <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
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

const API_BASE_URL = 'http://localhost:8080/api/warning-rule'

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const searchEnabled = ref<number | null>(null)

// 下拉字典数据
const classList = ref<any[]>([])
const diseaseList = ref<any[]>([])

// 弹窗表单数据
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
    id: null as number | null,
    ruleName: '',
    classScope: null as number | null,
    diseaseId: null as number | null,
    timeWindowHours: 24,
    thresholdCount: 3,
    riskLevel: 2,
    enabled: 1
})

const rules = reactive<FormRules>({
    ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
    timeWindowHours: [{ required: true, message: '请输入时间窗口', trigger: 'blur' }],
    thresholdCount: [{ required: true, message: '请输入触发阈值', trigger: 'blur' }],
    riskLevel: [{ required: true, message: '请选择风险等级', trigger: 'change' }]
})

// 获取基础字典数据
const fetchDictData = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const config = { headers: { Authorization: `Bearer ${token}` } }

        // 并发获取班级和疾病列表（这里简单获取所有，不考虑分页超过单页的情况）
        const [classRes, diseaseRes] = await Promise.all([
            axios.get(`http://localhost:8080/api/class/page?current=1&size=1000`, config),
            axios.get(`http://localhost:8080/api/system/diseases/page?current=1&size=1000`, config)
        ])

        if (classRes.data.code === 200) {
            classList.value = classRes.data.data.records
        }
        if (diseaseRes.data.code === 200) {
            diseaseList.value = diseaseRes.data.data.records
        }
    } catch (error) {
        console.error("加载字典数据失败", error)
    }
}

// 风险等级映射
const getRiskText = (level: number) => {
    const map: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }
    return map[level] || '未知'
}

const getRiskTagType = (level: number) => {
    const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger' }
    return map[level] || 'info'
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
                keyword: searchQuery.value || undefined,
                enabled: searchEnabled.value !== null ? searchEnabled.value : undefined
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

// 快速修改状态
const handleStatusChange = async (row: any, val: number) => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.put(API_BASE_URL, { id: row.id, enabled: val }, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            ElMessage.success('状态更新成功')
        } else {
            row.enabled = val === 1 ? 0 : 1 // revert
            ElMessage.error(res.data.msg || '状态更新失败')
        }
    } catch (error) {
        row.enabled = val === 1 ? 0 : 1 // revert
        ElMessage.error('网络错误，请稍后重试')
    }
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
    form.ruleName = ''
    form.classScope = null
    form.diseaseId = null
    form.timeWindowHours = 24
    form.thresholdCount = 3
    form.riskLevel = 2
    form.enabled = 1
}

// 弹窗关闭时重置校验状态
const resetForm = () => {
    if (formRef.value) {
        formRef.value.resetFields()
    }
}

onMounted(() => {
    fetchDictData()
    fetchData()
})
</script>

<style scoped>
.system-rules-container {
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