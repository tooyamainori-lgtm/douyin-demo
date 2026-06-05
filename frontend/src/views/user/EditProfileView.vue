<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const saving = ref(false)
const uploading = ref(false)

const form = ref({
  nickname: '',
  bio: '',
  gender: 0,
  birthday: '',
})

onMounted(async () => {
  if (!userStore.isLogin) {
    router.push('/login')
    return
  }
  // 从 API 加载完整用户信息
  try {
    const u = await userApi.getMe()
    form.value.nickname = u.nickname || ''
    form.value.bio = u.bio || ''
    form.value.gender = u.gender ?? 0
    form.value.birthday = u.birthday || ''
  } catch { /* ignore */ }
})

/** 保存资料 */
async function handleSave() {
  if (!form.value.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    const updated = await userApi.updateProfile({
      nickname: form.value.nickname.trim(),
      bio: form.value.bio.trim() || undefined,
      gender: form.value.gender || undefined,
      birthday: form.value.birthday || undefined,
    })
    // 完整更新本地 store
    if (userStore.user) {
      userStore.user.nickname = updated.nickname
      userStore.user.avatarUrl = updated.avatarUrl
      userStore.user.bio = updated.bio
      userStore.user.gender = updated.gender
      userStore.user.birthday = updated.birthday
    }
    ElMessage.success('保存成功')
    router.back()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/** 上传头像 */
async function handleAvatarChange(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  try {
    const url = await userApi.uploadAvatar(fd)
    if (userStore.user) {
      userStore.user.avatarUrl = url
    }
    ElMessage.success('头像已更新')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="edit-page">
    <div class="edit-card">
      <h2>编辑个人资料</h2>

      <!-- 头像 -->
      <div class="avatar-section">
        <div class="avatar" v-if="userStore.user">
          <img v-if="userStore.user.avatarUrl" :src="userStore.user.avatarUrl" class="avatar-img" />
          <span v-else class="avatar-text">{{ userStore.user.nickname?.charAt(0) || '?' }}</span>
        </div>
        <el-upload
          class="avatar-uploader"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
          :on-change="(f: any) => handleAvatarChange(f.raw)"
        >
          <el-button :loading="uploading" size="small" type="primary" plain>
            {{ uploading ? '上传中...' : '更换头像' }}
          </el-button>
        </el-upload>
      </div>

      <el-form label-position="top" @submit.prevent="handleSave">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" maxlength="30" placeholder="输入昵称" />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="介绍一下自己..."
          />
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生日">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave" style="width:100%">
            {{ saving ? '保存中...' : '保存' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.edit-page {
  max-width: 480px;
  margin: 40px auto;
  padding: 0 16px;
}

.edit-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
}

.edit-card h2 {
  margin: 0 0 24px;
  font-size: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fe2c55, #25f4ee);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-text {
  color: #fff;
  font-size: 28px;
  font-weight: bold;
}
</style>
