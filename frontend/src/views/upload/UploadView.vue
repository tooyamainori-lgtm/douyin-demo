<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { videoApi } from '@/api/video'
import { tagApi, type TagInfo } from '@/api/tag'
import { ElMessage } from 'element-plus'

const router = useRouter()

const uploading = ref(false)
const videoFile = ref<File | null>(null)
const coverFile = ref<File | null>(null)
const allTags = ref<TagInfo[]>([])
const selectedTags = ref<string[]>([])

/** 封面预览 URL */
const coverPreviewUrl = computed(() =>
  coverFile.value ? URL.createObjectURL(coverFile.value) : undefined,
)

const form = ref({
  title: '',
  description: '',
})

/** 加载预设标签列表 */
onMounted(async () => {
  try {
    allTags.value = await tagApi.list()
  } catch { /* 忽略 */ }
})

/** 切换标签选中 */
function toggleTag(name: string) {
  const idx = selectedTags.value.indexOf(name)
  if (idx >= 0) {
    selectedTags.value.splice(idx, 1)
  } else if (selectedTags.value.length < 3) {
    selectedTags.value.push(name)
  } else {
    ElMessage.warning('最多选择 3 个标签')
  }
}

function handleFileChange(file: File) {
  videoFile.value = file
}

async function handleUpload() {
  if (!videoFile.value) {
    ElMessage.warning('请选择视频文件')
    return
  }
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入视频标题')
    return
  }
  const fd = new FormData()
  fd.append('video', videoFile.value)
  fd.append('title', form.value.title.trim())
  fd.append('description', form.value.description.trim())
  fd.append('tags', selectedTags.value.join(','))
  if (coverFile.value) fd.append('cover', coverFile.value)

  uploading.value = true
  try {
    await videoApi.upload(fd)
    ElMessage.success('上传成功！')
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="upload-page">
    <div class="upload-card">
      <h2>上传作品</h2>

      <el-form label-position="top" @submit.prevent="handleUpload">
        <el-form-item label="选择视频">
          <el-upload
            class="video-uploader"
            :auto-upload="false"
            :limit="1"
            accept=".mp4"
            :on-change="(f: any) => handleFileChange(f.raw)"
          >
            <el-button type="primary" size="large">
              {{ videoFile ? videoFile.name : '点击选择 MP4 文件' }}
            </el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="视频封面（选填）">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept="image/*"
            :on-change="(f: any) => coverFile = f.raw"
          >
            <el-button size="small">
              {{ coverFile ? coverFile.name : '选择封面图片' }}
            </el-button>
          </el-upload>
          <img
            v-if="coverFile"
            :src="coverPreviewUrl"
            class="cover-preview"
          />
        </el-form-item>

        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="1-100字" maxlength="100" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="说说你的视频..."
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="标签（最多3个）">
          <div class="tag-selector">
            <span
              v-for="tag in allTags"
              :key="tag.name"
              :class="['tag-chip', { selected: selectedTags.includes(tag.name) }]"
              @click="toggleTag(tag.name)"
            >
              <span class="tag-chip-icon">{{ tag.icon }}</span>
              <span class="tag-chip-name">{{ tag.name }}</span>
            </span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="uploading"
            @click="handleUpload"
            style="width: 100%"
          >
            {{ uploading ? '上传中...' : '发布' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.upload-page {
  max-width: 520px;
  margin: 40px auto;
  padding: 0 16px;
}

.upload-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
}

.upload-card h2 {
  margin: 0 0 24px;
  font-size: 20px;
}

.video-uploader :deep(.el-upload) {
  display: block;
}

.cover-preview {
  width: 120px;
  height: 68px;
  object-fit: cover;
  border-radius: 4px;
  margin-top: 8px;
  border: 1px solid #e4e7ed;
}

/* Tag chips */
.tag-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: var(--radius-lg);
  background: var(--color-bg-alt);
  border: 2px solid transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
}

.tag-chip:hover {
  border-color: var(--color-border);
  color: var(--color-text);
  transform: translateY(-1px);
}

.tag-chip.selected {
  background: var(--color-primary-soft);
  border-color: var(--color-primary);
  color: var(--color-primary);
  font-weight: 600;
}

.tag-chip-icon { font-size: 14px; }
.tag-chip-name { line-height: 1; }
</style>
