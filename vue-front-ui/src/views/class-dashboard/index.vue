<template>
    <div class="class-dashboard-container">
        <el-card class="dashboard-card">
            <template #header>
                <div class="card-header">
                    <span>班级健康看板</span>
                    <div class="header-controls">
                        <el-select v-model="selectedClassId" placeholder="请选择管理班级" @change="fetchSummary"
                            style="width: 200px; margin-right: 15px;">
                            <el-option v-for="cls in managedClasses" :key="cls.id" :label="cls.className"
                                :value="cls.id" />
                        </el-select>
                        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD"
                            @change="fetchSummary" :clearable="false" style="width: 150px;" />
                    </div>
                </div>
            </template>

            <div v-if="!selectedClassId" class="empty-tip">
                <el-empty description="请先在上方选择要查看的班级" />
            </div>

            <div v-else>
                <!-- 统计卡片 -->
                <el-row :gutter="20" class="stat-row">
                    <el-col :span="6">
                        <el-card shadow="hover" class="stat-card">
                            <div class="stat-title">总人数</div>
                            <div class="stat-value">{{ totalStudents }}</div>
                        </el-card>
                    </el-col>
                    <el-col :span="6">
                        <el-card shadow="hover" class="stat-card">
                            <div class="stat-title">已填报</div>
                            <div class="stat-value text-success">{{ reportedCount }}</div>
                        </el-card>
                    </el-col>
                    <el-col :span="6">
                        <el-card shadow="hover" class="stat-card">
                            <div class="stat-title">未填报</div>
                            <div class="stat-value text-warning">{{ unReportedCount }}</div>
                        </el-card>
                    </el-col>
                    <el-col :span="6">
                        <el-card shadow="hover" class="stat-card">
                            <div class="stat-title">异常警告</div>
                            <div class="stat-value text-danger">{{ abnormalCount }}</div>
                        </el-card>
                    </el-col>
                </el-row>

                <!-- 详情表格 -->
                <el-table :data="summaryData" v-loading="loading" style="width: 100%" border stripe height="500">
                    <el-table-column prop="studentNo" label="学号" width="120" align="center" />
                    <el-table-column prop="realName" label="姓名" width="120" align="center" />
                    <el-table-column prop="hasReported" label="打卡状态" width="100" align="center">
                        <template #default="scope">
                            <el-tag :type="scope.row.hasReported ? 'success' : 'info'">
                                {{ scope.row.hasReported ? '已填报' : '未填报' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="temperature" label="体温(℃)" width="100" align="center">
                        <template #default="scope">
                            <span v-if="!scope.row.hasReported">-</span>
                            <span v-else :class="{ 'text-danger': scope.row.temperature >= 37.3 }">
                                {{ scope.row.temperature }}
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="diseaseId" label="传染病类型" width="150" align="center">
                        <template #default="scope">
                            <span v-if="!scope.row.hasReported">-</span>
                            <el-tag v-else :type="scope.row.diseaseId ? 'danger' : 'success'">
                                {{ getDiseaseName(scope.row.diseaseId) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="symptomJson" label="症状" min-width="150">
                        <template #default="scope">
                            <span v-if="!scope.row.hasReported">-</span>
                            <span v-else>{{ scope.row.symptomJson }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="riskLevel" label="风险评估" width="100" align="center" fixed="right">
                        <template #default="scope">
                            <span v-if="!scope.row.hasReported">-</span>
                            <el-tag v-else :type="getRiskTag(scope.row.riskLevel)">
                                {{ getRiskText(scope.row.riskLevel) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const selectedClassId = ref<number | null>(null)
const selectedDate = ref<string>(new Date().toISOString().split('T')[0] || '')
const managedClasses = ref<any[]>([])
const summaryData = ref<any[]>([])
const loading = ref(false)
const diseaseOptions = ref<any[]>([])

const totalStudents = computed(() => summaryData.value.length)
const reportedCount = computed(() => summaryData.value.filter(item => item.hasReported).length)
const unReportedCount = computed(() => totalStudents.value - reportedCount.value)
const abnormalCount = computed(() => summaryData.value.filter(item => item.hasReported && item.riskLevel > 1).length)

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
        console.error(e)
    }
}

const getDiseaseName = (diseaseId: number | null) => {
    if (!diseaseId) return '无'
    const disease = diseaseOptions.value.find(d => d.id === diseaseId)
    return disease ? disease.diseaseName : '未知疾病'
}

const getRiskText = (level: number) => {
    const map: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }
    return map[level] || '未知'
}

const getRiskTag = (level: number) => {
    const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
    return map[level] || 'info'
}

const fetchManagedClasses = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/class/managed', {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            managedClasses.value = res.data.data
            if (managedClasses.value.length > 0) {
                selectedClassId.value = managedClasses.value[0].id
                fetchSummary()
            }
        } else {
            ElMessage.error(res.data.msg || '获取管理的班级失败')
        }
    } catch (e) {
        console.error(e)
        ElMessage.error('网络错误，无法获取班级列表')
    }
}

const fetchSummary = async () => {
    if (!selectedClassId.value || !selectedDate.value) return

    loading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/health-report/class-summary', {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                classId: selectedClassId.value,
                date: selectedDate.value
            }
        })
        if (res.data.code === 200) {
            summaryData.value = res.data.data
        } else {
            ElMessage.error(res.data.msg || '获取汇总数据失败')
        }
    } catch (e) {
        console.error(e)
        ElMessage.error('网络错误')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchDiseases()
    fetchManagedClasses()
})
</script>

<style scoped>
.class-dashboard-container {
    padding: 10px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    font-size: 16px;
}

.header-controls {
    display: flex;
    align-items: center;
}

.stat-row {
    margin-bottom: 20px;
}

.stat-card {
    text-align: center;
}

.stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 10px;
}

.stat-value {
    font-size: 24px;
    font-weight: bold;
}

.text-success {
    color: #67C23A;
}

.text-warning {
    color: #E6A23C;
}

.text-danger {
    color: #F56C6C;
}
</style>