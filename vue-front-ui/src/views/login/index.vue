<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const isLoginMode = ref(true)

// 登录参数
const username = ref('')
const password = ref('')

// 注册参数
const regUsername = ref('')
const regPassword = ref('')
const regStudentNo = ref('')
const regRealName = ref('')
const regPhone = ref('')

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 切换模式
const toggleMode = () => {
    isLoginMode.value = !isLoginMode.value
    errorMessage.value = ''
    successMessage.value = ''
}

// 登录逻辑
const handleLogin = async () => {
    if (!username.value || !password.value) {
        errorMessage.value = '请输入用户名和密码'
        return
    }

    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
        const res = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: username.value,
                password: password.value
            })
        })

        const data = await res.json()

        if (data.code === 200) {
            localStorage.setItem('token', data.data.token)
            localStorage.setItem('username', data.data.username)
            localStorage.setItem('userId', data.data.userId)

            // 安全提取并净化roles数组，剔除null或空字符串，确保它是一个有效的字符串数组
            let rawRoles = data.data.roles || []
            if (!Array.isArray(rawRoles)) {
                rawRoles = []
            }
            const cleanRoles = rawRoles.filter((r: any) => r && typeof r === 'string' && r.trim() !== '')

            // 如果后端没有返回任何有效角色，降级为学生权限
            if (cleanRoles.length === 0) {
                cleanRoles.push('student')
            }
            localStorage.setItem('roles', JSON.stringify(cleanRoles))

            // 安全提取 permissions
            let rawPerms = data.data.permissions || []
            if (!Array.isArray(rawPerms)) {
                rawPerms = []
            }
            const cleanPerms = rawPerms.filter((p: any) => p && typeof p === 'string' && p.trim() !== '')
            localStorage.setItem('permissions', JSON.stringify(cleanPerms))

            alert('登录成功！')
            router.push('/')
        } else {
            errorMessage.value = data.msg || '登录失败'
        }
    } catch (error) {
        errorMessage.value = '网络错误，请稍后重试'
        console.error(error)
    } finally {
        loading.value = false
    }
}

// 注册逻辑
const handleRegister = async () => {
    if (!regUsername.value || !regPassword.value || !regStudentNo.value) {
        errorMessage.value = '用户名、密码和学号不能为空'
        return
    }

    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
        const res = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: regUsername.value,
                password: regPassword.value,
                studentNo: regStudentNo.value,
                realName: regRealName.value,
                phone: regPhone.value
            })
        })

        const data = await res.json()

        if (data.code === 200) {
            successMessage.value = '注册成功！请登录'
            username.value = regUsername.value
            password.value = regPassword.value
            setTimeout(() => {
                isLoginMode.value = true
            }, 1500)
        } else {
            errorMessage.value = data.msg || '注册失败'
        }
    } catch (error) {
        errorMessage.value = '网络错误，请稍后重试'
        console.error(error)
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <div class="login-container">
        <div class="login-box" :class="{ 'register-box': !isLoginMode }">
            <h2>HealthyBay {{ isLoginMode ? '登录' : '注册' }}</h2>

            <!-- 登录表单 -->
            <div v-if="isLoginMode" class="form-wrapper">
                <div class="form-item">
                    <label for="username">用户名</label>
                    <input id="username" v-model="username" type="text" placeholder="请输入用户名"
                        @keyup.enter="handleLogin" />
                </div>
                <div class="form-item">
                    <label for="password">密码</label>
                    <input id="password" v-model="password" type="password" placeholder="请输入密码"
                        @keyup.enter="handleLogin" />
                </div>

                <div v-if="errorMessage" class="error-msg">{{ errorMessage }}</div>
                <div v-if="successMessage" class="success-msg">{{ successMessage }}</div>

                <button :disabled="loading" class="primary-btn" @click="handleLogin">
                    {{ loading ? '登录中...' : '登 录' }}
                </button>
            </div>

            <!-- 注册表单 -->
            <div v-else class="form-wrapper">
                <div class="form-item">
                    <label for="regUsername">用户名 <span class="required">*</span></label>
                    <input id="regUsername" v-model="regUsername" type="text" placeholder="设置用户名" />
                </div>
                <div class="form-item">
                    <label for="regPassword">密码 <span class="required">*</span></label>
                    <input id="regPassword" v-model="regPassword" type="password" placeholder="设置密码" />
                </div>
                <div class="form-item">
                    <label for="regStudentNo">学号/工号 <span class="required">*</span></label>
                    <input id="regStudentNo" v-model="regStudentNo" type="text" placeholder="输入学号或工号" />
                </div>
                <div class="form-item">
                    <label for="regRealName">真实姓名</label>
                    <input id="regRealName" v-model="regRealName" type="text" placeholder="输入真实姓名" />
                </div>
                <div class="form-item">
                    <label for="regPhone">手机号</label>
                    <input id="regPhone" v-model="regPhone" type="text" placeholder="输入手机号" />
                </div>

                <div v-if="errorMessage" class="error-msg">{{ errorMessage }}</div>
                <div v-if="successMessage" class="success-msg">{{ successMessage }}</div>

                <button :disabled="loading" class="primary-btn register-btn" @click="handleRegister">
                    {{ loading ? '提交中...' : '注 册' }}
                </button>
            </div>

            <!-- 切换模式按钮 -->
            <div class="toggle-mode">
                <span @click="toggleMode">
                    {{ isLoginMode ? '没有账号？点击注册' : '已有账号？返回登录' }}
                </span>
            </div>
        </div>
    </div>
</template>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f5f7fa;
    padding: 20px;
}

.login-box {
    width: 380px;
    padding: 40px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
}

.register-box {
    width: 420px;
}

h2 {
    text-align: center;
    margin-bottom: 25px;
    color: #333;
}

.form-item {
    margin-bottom: 18px;
}

label {
    display: block;
    margin-bottom: 6px;
    color: #606266;
    font-size: 14px;
}

.required {
    color: #f56c6c;
}

input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    font-size: 14px;
    box-sizing: border-box;
    outline: none;
    transition: border-color 0.2s;
}

input:focus {
    border-color: #409eff;
}

.error-msg {
    color: #f56c6c;
    font-size: 13px;
    margin-bottom: 15px;
    text-align: center;
}

.success-msg {
    color: #67c23a;
    font-size: 13px;
    margin-bottom: 15px;
    text-align: center;
}

.primary-btn {
    width: 100%;
    padding: 12px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.primary-btn:hover {
    background-color: #66b1ff;
}

.primary-btn:disabled {
    background-color: #a0cfff;
    cursor: not-allowed;
}

.register-btn {
    background-color: #67c23a;
}

.register-btn:hover {
    background-color: #85ce61;
}

.toggle-mode {
    text-align: center;
    margin-top: 20px;
    font-size: 14px;
    color: #909399;
}

.toggle-mode span {
    cursor: pointer;
    transition: color 0.2s;
}

.toggle-mode span:hover {
    color: #409eff;
}
</style>
