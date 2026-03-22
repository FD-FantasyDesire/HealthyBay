<template>
    <div class="system-logs-container">
        <div class="toolbar">
            <el-input v-model="searchKeyword" placeholder="搜索操作/方法/IP..." style="width: 250px" clearable
                @keyup.enter="fetchData" @clear="fetchData">
                <template #append>
                    <el-button @click="fetchData"><el-icon>
                            <Search />
                        </el-icon></el-button>
                </template>
            </el-input>
            <el-button type="primary" @click="fetchData" style="margin-left: 15px;"><el-icon>
                    <Refresh />
                </el-icon> 刷新</el-button>
            <div style="flex: 1"></div>
            <el-button type="warning" @click="handleBackup" :loading="backupLoading">
                <el-icon>
                    <Download />
                </el-icon> 系统数据备份
            </el-button>
        </div>

        <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;" border stripe>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="userId" label="操作人ID" width="100" align="center" />
            <el-table-column prop="operation" label="操作模块/名称" width="180" show-overflow-tooltip />
            <el-table-column prop="method" label="请求方法" min-width="250" show-overflow-tooltip />
            <el-table-column prop="requestIp" label="请求IP" width="150" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                        {{ scope.row.status === 1 ? '成功' : '失败' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" min-width="150" show-overflow-tooltip />
            <el-table-column prop="createTime" label="操作时间" width="180" align="center">
                <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/operation-log'
const BACKUP_API_URL = 'http://localhost:8080/api/system/backup'

const loading = ref(false)
const backupLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')

const formatDate = (dateString: string) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', { hour12: false })
}

const fetchData = async () => {
    loading.value = true
    try {
        const token = localStorage.getItem('token') || ''
        const res = await axios.get(`${API_BASE_URL}/page`, {
            headers: { Authorization: `Bearer ${token}` },
            params: {
                current: currentPage.value,
                size: pageSize.value,
                keyword: searchKeyword.value || undefined
            }
        })
        if (res.data.code === 200) {
            tableData.value = res.data.data.records
            total.value = res.data.data.total
        } else {
            ElMessage.error(res.data.msg || '获取日志数据失败')
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

const handleBackup = () => {
    ElMessageBox.confirm(
        '执行数据库备份将在服务器生成一份最新的 SQL 备份文件，是否继续？',
        '系统数据备份',
        {
            confirmButtonText: '立即备份',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        backupLoading.value = true
        try {
            const token = localStorage.getItem('token') || ''
            const res = await axios.post(BACKUP_API_URL, {}, {
                headers: { Authorization: `Bearer ${token}` }
            })
            if (res.data.code === 200) {
                ElMessage.success(res.data.data || '备份成功')
            } else {
                ElMessage.error(res.data.msg || '备份失败')
            }
        } catch (error) {
            console.error(error)
            ElMessage.error('请求备份接口异常')
        } finally {
            backupLoading.value = false
        }
    }).catch(() => {
        // 取消
    })
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.system-logs-container {
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