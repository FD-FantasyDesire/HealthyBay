<template>
    <div class="dashboard-container">
        <h3>欢迎来到控制台</h3>
        <p>系统运行状态：正常</p>

        <el-row :gutter="20" class="stat-row">
            <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-title">全校总人数</div>
                    <div class="stat-value">{{ summaryData.totalUsers }}</div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-title">今日上报数</div>
                    <div class="stat-value text-success">{{ summaryData.todayReports }}</div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-title">待处理预警</div>
                    <div class="stat-value text-danger">{{ summaryData.activeWarnings }}</div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-title">历史发布通知</div>
                    <div class="stat-value text-primary">{{ summaryData.totalNotices }}</div>
                </el-card>
            </el-col>
        </el-row>

        <div v-if="summaryData.totalUsers > 0" style="margin-top: 20px;">
            <p>今日全校健康打卡率：{{ reportRate }}%</p>
            <el-progress :percentage="reportRate" :status="reportRate >= 90 ? 'success' : 'warning'"></el-progress>
        </div>

        <el-card shadow="hover" style="margin-top: 20px;">
            <template #header>
                <div class="card-header">
                    <span>最近7天健康打卡趋势</span>
                </div>
            </template>
            <div ref="chartRef" style="height: 350px; width: 100%;"></div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const summaryData = ref({
    totalUsers: 0,
    todayReports: 0,
    activeWarnings: 0,
    totalNotices: 0
})

const reportRate = computed(() => {
    if (summaryData.value.totalUsers === 0) return 0
    return Number(((summaryData.value.todayReports / summaryData.value.totalUsers) * 100).toFixed(1))
})

const fetchSummary = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/dashboard/summary', {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            summaryData.value = res.data.data
        } else {
            ElMessage.error(res.data.msg || '获取概览数据失败')
        }
    } catch (e) {
        console.error(e)
    }
}

const fetchTrend = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get('http://localhost:8080/api/dashboard/trend', {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            initChart(res.data.data)
        }
    } catch (e) {
        console.error(e)
    }
}

const initChart = (data: any) => {
    if (!chartRef.value) return
    if (!chartInstance) {
        chartInstance = echarts.init(chartRef.value)
    }

    const dates = data.dates.reverse()
    const totalReports = data.totalReports.reverse()
    const abnormalReports = data.abnormalReports.reverse()

    const option = {
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['总打卡数', '异常数']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: dates
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                name: '总打卡数',
                type: 'line',
                data: totalReports,
                itemStyle: { color: '#409EFF' },
                areaStyle: { opacity: 0.1 }
            },
            {
                name: '异常数',
                type: 'line',
                data: abnormalReports,
                itemStyle: { color: '#F56C6C' }
            }
        ]
    }
    chartInstance.setOption(option)
}

const handleResize = () => {
    chartInstance?.resize()
}

onMounted(() => {
    fetchSummary()
    fetchTrend()
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    chartInstance?.dispose()
})
</script>

<style scoped>
.dashboard-container {
    padding: 20px 0;
}

.stat-row {
    margin-top: 20px;
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
    font-size: 28px;
    font-weight: bold;
}

.text-success {
    color: #67C23A;
}

.text-danger {
    color: #F56C6C;
}

.text-primary {
    color: #409EFF;
}
</style>
