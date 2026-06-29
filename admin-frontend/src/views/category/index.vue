<template>
  <div class="category-page">
    <el-card>
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="tree-header flex-between">
            <span class="tree-title">分类结构</span>
          </div>
          <el-tree
            :data="treeData"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ data }">
              <span class="tree-node">
                <span>{{ data.name }}</span>
                <el-tag v-if="data.totalQuestions !== undefined" size="small" class="node-tag">
                  {{ data.totalQuestions || data.questionCount || 0 }}题
                </el-tag>
              </span>
            </template>
          </el-tree>
          <div class="tree-actions">
            <el-button size="small" @click="handleAdd('category')">
              <el-icon><Plus /></el-icon>大类
            </el-button>
            <el-button size="small" @click="handleAdd('subject')" :disabled="!canAddSubject">
              <el-icon><Plus /></el-icon>科目
            </el-button>
            <el-button size="small" @click="handleAdd('chapter')" :disabled="!canAddChapter">
              <el-icon><Plus /></el-icon>章节
            </el-button>
            <el-button size="small" @click="handleAdd('tag')" :disabled="!canAddTag">
              <el-icon><Plus /></el-icon>知识点
            </el-button>
          </div>
        </el-col>
        <el-col :span="16">
          <div v-if="currentNode" class="node-detail">
            <h4>{{ typeLabel }}: {{ currentNode.name }}</h4>
            <el-descriptions :column="1" border size="small" style="margin-top:12px">
              <el-descriptions-item label="ID">{{ currentNode.id }}</el-descriptions-item>
              <el-descriptions-item label="名称">{{ currentNode.name }}</el-descriptions-item>
              <el-descriptions-item v-if="currentNode.icon" label="图标">{{ currentNode.icon }}</el-descriptions-item>
              <el-descriptions-item v-if="currentNode.description" label="描述">{{ currentNode.description }}</el-descriptions-item>
              <el-descriptions-item v-if="currentNode.totalQuestions !== undefined" label="题目数">{{ currentNode.totalQuestions }}</el-descriptions-item>
              <el-descriptions-item v-if="currentNode.questionCount !== undefined" label="题目数">{{ currentNode.questionCount }}</el-descriptions-item>
              <el-descriptions-item label="排序">{{ currentNode.sortOrder }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top:12px">
              <el-button size="small" @click="handleEdit">编辑</el-button>
              <el-popconfirm title="确认删除此节点及子节点?" @confirm="handleDelete">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
          <el-empty v-else description="请选择左侧节点查看详情" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      :model-value="editVisible"
      :title="editTitle"
      width="480px"
      @close="editVisible = false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item v-if="editType === 'category'" label="图标">
          <el-input v-model="editForm.icon" placeholder="如: certificate, book" />
        </el-form-item>
        <el-form-item v-if="editType === 'category'" label="描述">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi } from '@/api/modules/category'
import type { ExamCategory, Subject, Chapter, Tag } from '@/types/category'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// Build tree data with children arrays
function buildTree(categories: ExamCategory[]): any[] {
  return categories.map(cat => ({
    ...cat,
    children: (cat.subjects || []).map(sub => ({
      ...sub,
      children: (sub.chapters || []).map(ch => ({
        ...ch,
        children: (ch.tags || []).map(tag => ({ ...tag, children: undefined })),
      })),
    })),
  }))
}

const treeData = ref<any[]>([])
const currentNode = ref<any>(null)
const editVisible = ref(false)
const editType = ref('')
const editFormRef = ref<FormInstance>()

const editForm = reactive({
  id: undefined as number | undefined,
  name: '',
  icon: '',
  description: '',
  sortOrder: 0,
})

const editRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

const typeLabel = computed(() => {
  const m: Record<string, string> = { category: '大类', subject: '科目', chapter: '章节', tag: '知识点' }
  return m[editType.value] || ''
})

const editTitle = computed(() => {
  const action = editForm.id ? '编辑' : '新增'
  return action + typeLabel.value
})

// Determine node type and parent from tree structure
function getNodeInfo(node: any) {
  if (node.children && node.children.length > 0 && node.children[0].chapters !== undefined) {
    return { type: 'category', parentId: undefined }
  }
  if (node.children && node.children.length > 0 && node.children[0].tags !== undefined) {
    return { type: 'subject', parentId: (node as Subject).categoryId }
  }
  if (node.children && node.children.length > 0 && node.children[0].children === undefined) {
    return { type: 'chapter', parentId: (node as Chapter).subjectId }
  }
  return { type: 'tag', parentId: (node as Tag).chapterId }
}

const canAddSubject = computed(() => currentNode.value && editType.value === 'category')
const canAddChapter = computed(() => currentNode.value && editType.value === 'subject')
const canAddTag = computed(() => currentNode.value && editType.value === 'chapter')

function handleNodeClick(node: any) {
  currentNode.value = node
  const info = getNodeInfo(node)
  editType.value = info.type
}

function handleAdd(type: string) {
  editType.value = type
  editForm.id = undefined
  editForm.name = ''
  editForm.icon = ''
  editForm.description = ''
  editForm.sortOrder = 0
  editVisible.value = true
}

function handleEdit() {
  if (!currentNode.value) return
  editForm.id = currentNode.value.id
  editForm.name = currentNode.value.name
  editForm.icon = currentNode.value.icon || ''
  editForm.description = currentNode.value.description || ''
  editForm.sortOrder = currentNode.value.sortOrder || 0
  editVisible.value = true
}

async function handleSaveEdit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return

  let parentId: number | undefined
  if (!editForm.id && currentNode.value) {
    parentId = editType.value === 'subject' ? currentNode.value.id
      : editType.value === 'chapter' ? currentNode.value.id
      : editType.value === 'tag' ? currentNode.value.id
      : undefined
  }

  try {
    await categoryApi.save({
      id: editForm.id,
      parentId,
      type: editType.value,
      name: editForm.name,
      icon: editForm.icon,
      description: editForm.description,
      sortOrder: editForm.sortOrder,
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    await loadTree()
  } catch { /* error handled by interceptor */ }
}

async function handleDelete() {
  if (!currentNode.value) return
  try {
    await categoryApi.deleteNode(editType.value, currentNode.value.id)
    ElMessage.success('删除成功')
    currentNode.value = null
    await loadTree()
  } catch { /* */ }
}

async function loadTree() {
  try {
    const categories = await categoryApi.getTree()
    treeData.value = buildTree(categories)
  } catch { /* */ }
}

onMounted(() => loadTree())
</script>

<style scoped lang="scss">
.category-page {
  .tree-header {
    margin-bottom: 12px;
    .tree-title { font-weight: bold; font-size: 15px; }
  }
  .tree-node {
    display: flex;
    align-items: center;
    gap: 8px;
    .node-tag { font-size: 11px; }
  }
  .tree-actions {
    margin-top: 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
  .node-detail {
    h4 { font-size: 16px; margin-bottom: 4px; }
  }
}
</style>
