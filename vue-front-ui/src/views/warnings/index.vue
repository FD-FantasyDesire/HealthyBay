<template>
    <div class="warnings-container">
        <div class="toolbar">
            <el-select v-model="searchStatus" placeholder="处理状态" clearable style="width: 150px; margin-right: 15px;"
                @change="fetchData">
                <el-option label="全部" :value="null" />
                <el-option label="未处理" :value="0" />
                <el-option label="已处理" :value="1" />
            </el-select>
            <el-button type="primary" @click="fetchData"><el-icon>
                    <Refresh />
                </el-icon> 刷新</el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="预警ID" width="80" align="center" />

            <el-table-column label="产生时间" width="180" align="center">
                <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                </template>
            </el-table-column>

            <el-table-column label="涉及班级" width="180">
                <template #default="scope">
                    {{ getClassName(scope.row.classId) }}
                </template>
            </el-table-column>

            <el-table-column label="疑似传染病" width="150">
                <template #default="scope">
                    <span v-if="!scope.row.diseaseId">未知异常</span>
                    <span v-else>{{ getDiseaseName(scope.row.diseaseId) }}</span>
                </template>
            </el-table-column>

            <el-table-column prop="triggerCount" label="触发人数" width="100" align="center">
                <template #default="scope">
                    <el-tag type="danger" effect="dark">{{ scope.row.triggerCount }} 人</el-tag>
                </template>
            </el-table-column>

            <el-table-column prop="warningLevel" label="预警等级" width="100" align="center">
                <template #default="scope">
                    <el-tag :type="getRiskTagType(scope.row.warningLevel)">
                        {{ getRiskText(scope.row.warningLevel) }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column prop="status" label="处理状态" width="100" align="center">
                <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                        {{ scope.row.status === 1 ? '已处理' : '待处理' }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column label="操作" width="180" align="center" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="info" @click="handleViewDetails(scope.row)">溯源</el-button>
                    <el-button v-if="scope.row.status === 0" size="small" type="primary"
                        @click="handleProcess(scope.row)">标记处理</el-button>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
                @current-change="handleCurrentChange" />
        </div>

        <!-- 预警溯源详情弹窗 -->
        <el-dialog title="预警事件追溯" v-model="detailsVisible" width="800px">
            <el-table :data="detailsData" v-loading="detailsLoading" border stripe>
                <el-table-column prop="studentNo" label="学号" width="120" />
                <el-table-column prop="realName" label="姓名" width="120" />
                <el-table-column prop="temperature" label="体温(℃)" width="100" align="center">
                    <template #default="scope">
                        <span :class="{ 'text-danger': scope.row.temperature >= 37.3 }">
                            {{ scope.row.temperature }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="symptoms" label="症状" />
                <el-table-column prop="riskLevel" label="单次风险" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="getRiskTagType(scope.row.riskLevel)">
                            {{ getRiskText(scope.row.riskLevel) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="上报时间" width="160">
                    <template #default="scope">
                        {{ formatDate(scope.row.reportTime) }}
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/warning-record'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchStatus = ref<number | null>(0) // 默认查未处理

const detailsVisible = ref(false)
const detailsLoading = ref(false)
const detailsData = ref([])

// 字典缓存
const classDict = ref<Record<number, string>>({})
const diseaseDict = ref<Record<number, string>>({})

// 日期格式化
const formatDate = (dateString: string) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', { hour12: false })
}

// 字典映射
const getClassName = (id: number) => classDict.value[id] || `班级ID:${id}`
const getDiseaseName = (id: number) => diseaseDict.value[id] || `疾病ID:${id}`

const getRiskText = (level: number) => {
    const map: Record<number, string> = { 1: '低风险', 2: '中风险', 3: '高风险' }
    return map[level] || '未知'
}

const getRiskTagType = (level: number) => {
    const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger' }
    return map[level] || 'info'
}

// 加载基础数据字典
const loadDictionaries = async () => {
    try {
        const token = localStorage.getItem('token') || ''
        const config = { headers: { Authorization: `Bearer ${token}` } }

        // 获取管理的班级（系统管理员返回所有班级）
        const [classRes, diseaseRes] = await Promise.all([
            axios.get(`http://localhost:8080/api/class/managed`, config),
            axios.get(`http://localhost:8080/api/system/diseases/list`, config)
        ])

        if (classRes.data.code === 200) {
            classRes.data.data.forEach((c: any) => {
                classDict.value[c.id] = c.className
            })
        }
        if (diseaseRes.data.code === 200) {
            diseaseRes.data.data.forEach((d: any) => {
                diseaseDict.value[d.id] = d.diseaseName
            })
        }
    } catch (error) {
        console.error("加载字典数据失败", error)
    }
}

// 获取数据列表
const fetchData = async () => {
    loading.value = true
    try {
        const token = localStorage.getItem('token') || ''

        // 实际业务中，对于班级管理员需要传 managerClassIds
        // 如果是系统管理员(如包含admin角色)，则不需要传，查询全部
        const rolesStr = localStorage.getItem('roles')
        let isAdmin = false
        if (rolesStr) {
            try {
                const roles = JSON.parse(rolesStr)
                if (roles.includes('admin') || roles.includes('system_admin')) {
                    isAdmin = true
                }
            } catch (e) { }
        }

        const payload: any = {
            current: currentPage.value,
            size: pageSize.value,
            status: searchStatus.value !== null ? searchStatus.value : undefined
        }

        // 如果不是管理员，需要从后端或本地获取该用户管理的 classIds 列表
        // （当前暂简化为由后端在拦截器处理或全部展示用于演示）

        const res = await axios.post(`${API_BASE_URL}/page`, payload, {
            headers: { Authorization: `Bearer ${token}` }
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

const handleSizeChange = (val: number) => {
    pageSize.value = val
    fetchData()
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchData()
}

// 查看详情(溯源)
const handleViewDetails = async (row: any) => {
    detailsVisible.value = true
    detailsLoading.value = true
    detailsData.value = []

    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get(`${API_BASE_URL}/${row.id}/details`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        if (res.data.code === 200) {
            detailsData.value = res.data.data
        } else {
            ElMessage.error(res.data.msg || '获取溯源详情失败')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('网络错误，请稍后重试')
    } finally {
        detailsLoading.value = false
    }
}

// 标记处理
const handleProcess = (row: any) => {
    ElMessageBox.confirm(
        '确定要将该预警记录标记为已处理吗？这通常意味着您已完成线下核实和干预。',
        '处理确认',
        {
            confirmButtonText: '确定已处理',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        try {
            const token = localStorage.getItem('token') || ''
            const res = await axios.put(API_BASE_URL, { id: row.id, status: 1 }, {
                headers: { Authorization: `Bearer ${token}` }
            })
            if (res.data.code === 200) {
                ElMessage.success('已标记处理')
                fetchData()
            } else {
                ElMessage.error(res.data.msg || '操作失败')
            }
        } catch (error) {
            console.error(error)
            ElMessage.error('网络错误')
        }
    }).catch(() => {
        // 取消
    })
}

onMounted(async () => {
    await loadDictionaries()
    fetchData()
})
</script>

<style scoped>
.warnings-container {
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

.text-muted {
    color: #909399;
    font-size: 13px;
}
</style>