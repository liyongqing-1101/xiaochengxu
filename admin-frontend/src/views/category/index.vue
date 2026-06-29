<template>
  <div class="category-page">
    <!-- 左侧：分类树 -->
    <div class="left-panel">
      <CategoryTree
        ref="treeRef"
        @node-click="handleNodeClick"
        @add="handleAdd"
        @edit="handleEdit"
        @delete="handleDelete"
      />
    </div>

    <!-- 右侧：详情 / 编辑表单 -->
    <div class="right-panel">
      <div v-if="!selectedNode" class="empty-state">
        <span class="empty-icon">📂</span>
        <p>请从左侧选择一个分类节点查看详情</p>
      </div>

      <div v-else class="detail-panel">
        <div class="panel-header">
          <h3>{{ isNew ? '新增' + typeLabel : '编辑' + typeLabel }}</h3>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          label-position="top"
          @submit.prevent
        >
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入名称" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item v-if="selectedNode.type === 'category'" label="图标">
            <el-input v-model="form.icon" placeholder="图标标识（可选）" maxlength="100" />
          </el-form-item>

          <el-form-item v-if="selectedNode.type === 'category'" label="描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="分类描述（可选）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
          </el-form-item>

          <el-form-item label="状态">
            <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>

          <!-- 只读信息 -->
          <el-divider />
          <el-descriptions v-if="!isNew" :column="1" size="small" border>
            <el-descriptions-item label="ID">{{ selectedNode.raw?.id }}</el-descriptions-item>
            <el-descriptions-item v-if="parentInfo" label="所属上级">{{ parentInfo }}</el-descriptions-item>
            <el-descriptions-item v-if="questionCount !== null" label="题目数量">{{ questionCount }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedNode.raw?.createTime" label="创建时间">{{ selectedNode.raw.createTime }}</el-descriptions-item>
            <el-descriptions-item v-if="selectedNode.raw?.updateTime" label="更新时间">{{ selectedNode.raw.updateTime }}</el-descriptions-item>
          </el-descriptions>

          <div class="form-actions">
            <el-button v-if="isNew" type="primary" :loading="saving" @click="handleSave">创建</el-button>
            <el-button v-else type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
            <el-button v-if="isNew" @click="handleCancelAdd">取消</el-button>
            <el-button v-else @click="handleReset">重置</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import CategoryTree from '@/components/category/CategoryTree.vue'
import { useCategoryStore } from '@/stores/category'
import {
  createCategory, updateCategory, deleteCategory,
  createSubject, updateSubject, deleteSubject,
  createChapter, updateChapter, deleteChapter,
  createTag, updateTag, deleteTag,
} from '@/api/modules/category'

const categoryStore = useCategoryStore()
const formRef = ref<FormInstance>()
const treeRef = ref()

// 选中的树节点
const selectedNode = ref<any>(null)
// 是否新增模式
const isNew = ref(false)
// 新增时的父节点
const parentNode = ref<any>(null)
const saving = ref(false)

// 表单数据
const form = reactive({
  name: '',
  icon: '',
  description: '',
  sortOrder: 0,
  status: 1,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

const typeLabel = computed(() => {
  if (isNew.value && parentNode.value) {
    const map: Record<string, string> = {
      category: '科目',
      subject: '章节',
      chapter: '知识点',
    }
    return map[parentNode.value.type] || ''
  }
  const map: Record<string, string> = {
    category: '考试大类',
    subject: '科目',
    chapter: '章节',
    tag: '知识点',
  }
  return selectedNode.value?.type ? map[selectedNode.value.type] : ''
})

const questionCount = computed(() => {
  const raw = selectedNode.value?.raw
  if (!raw) return null
  return raw.totalQuestions ?? raw.questionCount ?? null
})

const parentInfo = computed(() => {
  if (!selectedNode.value?.raw) return null
  const raw = selectedNode.value.raw
  // 根据类型查找上级名称
  if (selectedNode.value.type === 'subject') {
    const cat = categoryStore.tree.find(c => c.id === raw.categoryId)
    return cat ? `${cat.name}（大类）` : null
  }
  if (selectedNode.value.type === 'chapter') {
    for (const cat of categoryStore.tree) {
      const subj = (cat as any).examSubjects?.find((s: any) => s.id === raw.subjectId)
      if (subj) return `${subj.name}（科目）`
    }
  }
  if (selectedNode.value.type === 'tag') {
    for (const cat of categoryStore.tree) {
      for (const subj of (cat as any).examSubjects || []) {
        const chap = subj.examChapters?.find((c: any) => c.id === raw.chapterId)
        if (chap) return `${chap.name}（章节）`
      }
    }
  }
  return null
})

// 单击树节点
function handleNodeClick(node: any) {
  isNew.value = false
  parentNode.value = null
  selectedNode.value = { type: getNodeType(node), raw: node }
  fillForm(node)
}

function getNodeType(node: any): string {
  // 优先使用 CategoryTree 传递的 _type 字段
  if (node._type) return node._type
  // fallback: 根据存在的字段推断
  if (node.examSubjects !== undefined) return 'category'
  if (node.examChapters !== undefined) return 'subject'
  if (node.examTags !== undefined) return 'chapter'
  return 'tag'
}

function fillForm(node: any) {
  form.name = node.name || ''
  form.icon = node.icon || ''
  form.description = node.description || ''
  form.sortOrder = node.sortOrder ?? 0
  form.status = node.status ?? 1
}

// 右键菜单 - 添加
function handleAdd(parent: any) {
  isNew.value = true
  parentNode.value = parent ? { type: getNodeType(parent), raw: parent } : { type: 'root', raw: null }
  selectedNode.value = { type: parent ? childType(getNodeType(parent)) : 'category', raw: null }
  form.name = ''
  form.icon = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
}

function childType(parentType: string): string {
  const map: Record<string, string> = { category: 'subject', subject: 'chapter', chapter: 'tag' }
  return map[parentType] || 'category'
}

// 右键菜单 - 编辑
function handleEdit(node: any) {
  isNew.value = false
  parentNode.value = null
  selectedNode.value = { type: getNodeType(node), raw: node }
  fillForm(node)
}

// 保存
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请检查表单填写')
    return
  }

  saving.value = true
  try {
    if (isNew.value) {
      await doCreate()
    } else {
      await doUpdate()
    }
    ElMessage.success(isNew.value ? '创建成功' : '保存成功')
    // 刷新树
    categoryStore.tree = []
    await categoryStore.fetchCategoryTree(true)
    if (treeRef.value) {
      treeRef.value.loadTreeData()
    }
    // 重置选中
    if (isNew.value) {
      handleCancelAdd()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function doCreate() {
  const type = selectedNode.value.type
  const parent = parentNode.value?.raw
  const data: any = {
    name: form.name,
    sortOrder: form.sortOrder,
    status: form.status,
  }

  if (type === 'subject') {
    data.categoryId = parent?.id
    data.icon = form.icon
    await createSubject(data)
  } else if (type === 'chapter') {
    data.subjectId = parent?.id
    await createChapter(data)
  } else if (type === 'tag') {
    data.chapterId = parent?.id
    await createTag(data)
  } else if (type === 'category') {
    data.icon = form.icon
    data.description = form.description
    await createCategory(data)
  }
}

async function doUpdate() {
  const type = selectedNode.value.type
  const id = selectedNode.value.raw.id
  const data: any = {
    name: form.name,
    sortOrder: form.sortOrder,
    status: form.status,
  }

  if (type === 'category') {
    data.icon = form.icon
    data.description = form.description
    await updateCategory(id, data)
  } else if (type === 'subject') {
    data.icon = form.icon
    await updateSubject(id, data)
  } else if (type === 'chapter') {
    await updateChapter(id, data)
  } else if (type === 'tag') {
    await updateTag(id, data)
  }
}

async function handleDelete(node: any) {
  // 此函数由 CategoryTree 的 delete 事件触发，但删除确认已在 CategoryTree 中完成
  // 这里实际执行删除
  const type = getNodeType(node)
  const id = node.id

  try {
    if (type === 'category') {
      await deleteCategory(id)
    } else if (type === 'subject') {
      await deleteSubject(id)
    } else if (type === 'chapter') {
      await deleteChapter(id)
    } else if (type === 'tag') {
      await deleteTag(id)
    }
    ElMessage.success('删除成功')
    categoryStore.tree = []
    await categoryStore.fetchCategoryTree(true)
    if (treeRef.value) {
      treeRef.value.loadTreeData()
    }
    // 如果删除的是当前选中的节点，清空选中
    if (selectedNode.value?.raw?.id === id) {
      selectedNode.value = null
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

function handleCancelAdd() {
  isNew.value = false
  parentNode.value = null
  selectedNode.value = null
}

function handleReset() {
  if (selectedNode.value?.raw) {
    fillForm(selectedNode.value.raw)
  }
}
</script>

<style scoped>
.category-page {
  display: flex;
  height: 100%;
  gap: 0;
}

.left-panel {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #ebeef5;
  padding: 16px;
  overflow-y: auto;
  background: #fff;
}

.right-panel {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #fafafa;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.4;
}

.empty-state p {
  font-size: 15px;
}

.detail-panel {
  max-width: 560px;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.panel-header {
  margin-bottom: 20px;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.form-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>