<template>
    <div class="my-notices">
        <div v-if="loading" style="text-align: center; padding: 20px">加载中...</div>
        <div v-else-if="notices.length === 0" style="text-align: center; padding: 20px">
            <el-empty description="暂无通知"></el-empty>
        </div>
        <div v-else>
            <el-card v-for="notice in notices" :key="notice.id" style="margin-bottom: 15px;">
                <template #header>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>
                            <strong>{{ notice.title }}</strong>
                            <el-tag v-if="!isRead(notice.id)" type="danger" size="small"
                                style="margin-left: 10px;">未读</el-tag>
                        </span>
                        <span style="font-size: 12px; color: #999">{{ notice.createTime }}</span>
                    </div>
                </template>
                <div>
                    <p style="white-space: pre-wrap; font-size: 14px; color: #333; line-height: 1.6;">{{ notice.content
                    }}</p>
                </div>
                <div style="margin-top: 15px; text-align: right;" v-if="!isRead(notice.id)">
                    <el-button type="success" size="small" @click="markAsRead(notice.id)">我知道了</el-button>
                </div>
            </el-card>

            <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" :total="total"
                    @size-change="fetchData" @current-change="fetchData" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const notices = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const readNoticeIds = ref<number[]>([])

const getReadIdsFromCookie = (): number[] => {
    const name = "read_notices=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = (ca[i] || '').trim();
        if (c.indexOf(name) === 0) {
            try {
                return JSON.parse(c.substring(name.length, c.length));
            } catch (e) {
                return [];
            }
        }
    }
    return [];
}

const saveReadIdsToCookie = (ids: number[]) => {
    const d = new Date();
    d.setTime(d.getTime() + (365 * 24 * 60 * 60 * 1000)); // 1 year expiration
    const expires = "expires=" + d.toUTCString();
    document.cookie = "read_notices=" + JSON.stringify(ids) + ";" + expires + ";path=/";
}

const isRead = (id: number) => {
    return readNoticeIds.value.includes(id)
}

const markAsRead = (id: number) => {
    if (!readNoticeIds.value.includes(id)) {
        readNoticeIds.value.push(id)
        saveReadIdsToCookie(readNoticeIds.value)
        ElMessage.success('已标记为已读')
    }
}

const fetchData = async () => {
    loading.value = true
    try {
        const res = await fetch(`http://localhost:8080/api/notice/page?current=${currentPage.value}&size=${pageSize.value}&status=1`, {
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            }
        })
        const json = await res.json()
        if (json.code === 200) {
            notices.value = json.data.records
            total.value = json.data.total
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    readNoticeIds.value = getReadIdsFromCookie()
    fetchData()
})
</script>

<style scoped>
.my-notices {
    padding: 10px 0;
}
</style>