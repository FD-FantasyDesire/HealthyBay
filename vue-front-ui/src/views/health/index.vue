<template>
    <div class="health-report-container">
        <el-card class="report-card">
            <template #header>
                <div class="card-header">
                    <span>每日健康上报</span>
                </div>
            </template>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="report-form">
                <el-form-item label="当前体温(℃)" prop="temperature">
                    <el-input-number v-model="form.temperature" :min="35" :max="42" :step="0.1" :precision="1" />
                </el-form-item>

                <el-form-item label="相关症状" prop="symptoms">
                    <el-checkbox-group v-model="form.symptoms">
                        <el-checkbox label="发热">发热</el-checkbox>
                        <el-checkbox label="咳嗽">咳嗽</el-checkbox>
                        <el-checkbox label="乏力">乏力</el-checkbox>
                        <el-checkbox label="喉咙痛">喉咙痛</el-checkbox>
                        <el-checkbox label="嗅觉/味觉减退">嗅觉/味觉减退</el-checkbox>
                        <el-checkbox label="腹泻">腹泻</el-checkbox>
                        <el-checkbox label="无症状">无症状</el-checkbox>
                    </el-checkbox-group>
                </el-form-item>

                <el-form-item label="确诊疾病" prop="diseaseId">
                    <el-select v-model="form.diseaseId" placeholder="请选择疾病（如确诊）" clearable style="width: 100%">
                        <el-option label="无" :value="0" />
                        <el-option v-for="item in diseaseOptions" :key="item.id" :label="item.diseaseName"
                            :value="item.id" />
                    </el-select>
                </el-form-item>

                <el-form-item label="接触史" prop="exposureHistory">
                    <el-input v-model="form.exposureHistory" type="textarea" :rows="3"
                        placeholder="近14天是否有中高风险地区旅居史或确诊病例接触史？如果没有，请填无。" />
                </el-form-item>

                <el-form-item label="疫苗接种状态" prop="vaccinationStatus">
                    <el-radio-group v-model="form.vaccinationStatus">
                        <el-radio :label="1">已完成接种</el-radio>
                        <el-radio :label="3">部分接种</el-radio>
                        <el-radio :label="2">未接种</el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitReport" :loading="submitLoading">提交今日上报</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card class="history-card" style="margin-top: 20px;">
            <template #header>
                <div class="card-header">
                    <span>我的上报记录</span>
                </div>
            </template>
            <el-table :data="historyData" v-loading="historyLoading" style="width: 100%" border stripe>
                <el-table-column prop="reportDate" label="上报日期" width="120" align="center">
                    <template #default="scope">{{ formatDate(scope.row.reportDate) }}</template>
                </el-table-column>
                <el-table-column prop="temperature" label="体温(℃)" width="100" align="center">
                    <template #default="scope">
                        <span :class="{ 'text-danger': scope.row.temperature >= 37.3 }">
                            {{ scope.row.temperature }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="diseaseId" label="提报疾病" width="150" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.diseaseId ? 'danger' : 'success'">
                            {{ getDiseaseName(scope.row.diseaseId) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="diseaseId" label="提报疾病" width="150" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.diseaseId ? 'danger' : 'success'">
                            {{ getDiseaseName(scope.row.diseaseId) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="symptomJson" label="症状" />
                <el-table-column prop="riskLevel" label="系统评估风险" width="120" align="center">
                    <template #default="scope">
                        <el-tag :type="getRiskTag(scope.row.riskLevel)">
                            {{ getRiskText(scope.row.riskLevel) }}
                        </el-tag>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination-container">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5, 10, 20]"
                    layout="total, prev, pager, next" :total="total" @size-change="fetchHistory"
                    @current-change="fetchHistory" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import axios from 'axios'

// 表单部分
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const diseaseOptions = ref<any[]>([])

const form = reactive({
    temperature: 36.5,
    symptoms: ['无症状'],
    diseaseId: 0,
    exposureHistory: '无',
    vaccinationStatus: 1
})

const rules = reactive<FormRules>({
    temperature: [{ required: true, message: '请填写体温', trigger: 'blur' }],
    symptoms: [{ type: 'array', required: true, message: '请选择症状', trigger: 'change' }],
    diseaseId: [{ required: true, message: '请选择疾病状态', trigger: 'change' }],
    exposureHistory: [{ required: true, message: '请填写接触史', trigger: 'blur' }],
    vaccinationStatus: [{ required: true, message: '请选择疫苗接种状态', trigger: 'change' }]
})

// 监听症状选择：如果选了无症状，清空其他；如果选了其他，清空无症状
watch(() => form.symptoms, (newVal, oldVal) => {
    if (newVal.includes('无症状') && !oldVal.includes('无症状')) {
        form.symptoms = ['无症状']
    } else if (newVal.includes('无症状') && newVal.length > 1) {
        form.symptoms = newVal.filter(s => s !== '无症状')
    }
})

// 历史记录部分
const historyLoading = ref(false)
const historyData = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 获取疾病字典
const fetchDiseases = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/system/diseases/list', {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            diseaseOptions.value = res.data.data
        }
    } catch (e) {
        console.error('获取疾病列表失败', e)
    }
}

// 获取历史记录
const fetchHistory = async () => {
    historyLoading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/health-report/my', {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                current: currentPage.value,
                size: pageSize.value
            }
        })
        if (res.data.code === 200) {
            historyData.value = res.data.data.records
            total.value = res.data.data.total
        }
    } catch (e) {
        console.error('获取历史记录失败', e)
    } finally {
        historyLoading.value = false
    }
}

// 提交上报
const submitReport = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitLoading.value = true
            try {
                const token = localStorage.getItem('token') || ''

                // 转换数据格式
                const submitData = {
                    temperature: form.temperature,
                    symptomJson: JSON.stringify(form.symptoms),
                    diseaseId: form.diseaseId === 0 ? null : form.diseaseId,
                    exposureHistory: form.exposureHistory,
                    vaccinationStatus: form.vaccinationStatus
                }

                const res = await axios.post('http://localhost:8080/api/health-report', submitData, {
                    headers: { Authorization: `Bearer ${token}` }
                })

                if (res.data.code === 200) {
                    ElMessage.success('今日健康上报成功！')
                    fetchHistory() // 刷新记录
                } else {
                    ElMessage.error(res.data.msg || '上报失败')
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

const resetForm = () => {
    if (formRef.value) {
        formRef.value.resetFields()
    }
}

// 辅助函数
const formatDate = (dateString: string) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toISOString().split('T')[0]
}

const getRiskText = (level: number) => {
    const map: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }
    return map[level] || '未知'
}

const getRiskTag = (level: number) => {
    const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
    return map[level] || 'info'
}

const getDiseaseName = (diseaseId: number | null) => {
    if (!diseaseId) return '无'
    const disease = diseaseOptions.value.find(d => d.id === diseaseId)
    return disease ? disease.diseaseName : '未知疾病'
}

onMounted(() => {
    fetchDiseases()
    fetchHistory()
})
</script>

<style scoped>
.health-report-container {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;
}

.card-header {
    font-weight: bold;
    font-size: 16px;
}

.text-danger {
    color: #f56c6c;
    font-weight: bold;
}

.pagination-container {
    margin-top: 15px;
    display: flex;
    justify-content: flex-end;
}
</style>