<template>
  <div class="category-tree-container">
    <div class="tree-header">
      <h3>分类结构</h3>
      <el-button type="primary" size="small" @click="handleAddRoot">新增大类</el-button>
    </div>

    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="{ children: 'children', label: 'label' }"
      node-key="id"
      default-expand-all
      highlight-current
      :expand-on-click-node="false"
      @node-click="handleNodeClick"
      @node-contextmenu="handleContextMenu"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <el-icon v-if="data.type === 'category'" class="node-icon category-icon"><Folder /></el-icon>
          <el-icon v-else-if="data.type === 'subject'" class="node-icon subject-icon"><Document /></el-icon>
          <el-icon v-else-if="data.type === 'chapter'" class="node-icon chapter-icon"><Collection /></el-icon>
          <el-icon v-else class="node-icon tag-icon"><PriceTag /></el-icon>
          <span class="node-label">{{ data.label }}</span>
          <span class="node-type-tag">{{ typeLabel(data.type) }}</span>
        </span>
      </template>
    </el-tree>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
    >
      <div class="menu-item" @click="handleMenuAdd">
        <el-icon><Plus /></el-icon>
        <span>添加子{{ childTypeLabel(contextMenu.data?.type) }}</span>
      </div>
      <div class="menu-item" @click="handleMenuEdit">
        <el-icon><Edit /></el-icon>
        <span>编辑</span>
      </div>
      <div class="menu-item danger" @click="handleMenuDelete">
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import {
  Folder, Document, Collection, PriceTag,
  Plus, Edit, Delete,
} from '@element-plus/icons-vue'

const emit = defineEmits<{
  (e: 'node-click', node: any): void
  (e: 'add', parent: any): void
  (e: 'edit', node: any): void
  (e: 'delete', node: any): void
}>()

const categoryStore = useCategoryStore()
const treeRef = ref()

// 右键菜单状态
const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  data: null as any,
})

function typeLabel(type: string) {
  const map: Record<string, string> = {
    category: '大类',
    subject: '科目',
    chapter: '章节',
    tag: '知识点',
  }
  return map[type] || ''
}

function childTypeLabel(type: string) {
  const map: Record<string, string> = {
    category: '科目',
    subject: '章节',
    chapter: '知识点',
    tag: '',
  }
  return map[type] || ''
}

// 树形数据
const treeData = computed(() => {
  return categoryStore.tree.map(cat => ({
    id: `cat-${cat.id}`,
    label: cat.name,
    type: 'category',
    raw: cat,
    children: (cat as any).examSubjects?.map((subj: any) => ({
      id: `subj-${subj.id}`,
      label: subj.name,
      type: 'subject',
      raw: subj,
      children: (subj as any).examChapters?.map((chap: any) => ({
        id: `chap-${chap.id}`,
        label: chap.name,
        type: 'chapter',
        raw: chap,
        children: (chap as any).examTags?.map((tag: any) => ({
          id: `tag-${tag.id}`,
          label: tag.name,
          type: 'tag',
          raw: tag,
        })) || [],
      })) || [],
    })) || [],
  }))
})

// 单击节点：选中并查看详情
function handleNodeClick(data: any) {
  closeContextMenu()
  emit('node-click', data.raw)
}

// 右键菜单
function handleContextMenu(event: MouseEvent, data: any) {
  event.preventDefault()
  contextMenu.visible = true
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
  contextMenu.data = data
}

function closeContextMenu() {
  contextMenu.visible = false
  contextMenu.data = null
}

function handleMenuAdd() {
  if (!contextMenu.data) return
  const type = contextMenu.data.type
  if (type === 'tag') {
    ElMessage.warning('知识点下不能再添加子分类')
    closeContextMenu()
    return
  }
  emit('add', contextMenu.data.raw)
  closeContextMenu()
}

function handleMenuEdit() {
  if (!contextMenu.data) return
  emit('edit', contextMenu.data.raw)
  closeContextMenu()
}

function handleMenuDelete() {
  if (!contextMenu.data) return
  const data = contextMenu.data
  ElMessageBox.confirm(`确定要删除「${data.label}」吗？删除后子分类也会一并删除。`, '确认删除', {
    type: 'warning',
  }).then(() => {
    emit('delete', data.raw)
  }).catch(() => {})
  closeContextMenu()
}

function handleAddRoot() {
  emit('add', null)  // null 表示新增顶级分类
}

// 点击其他区域关闭右键菜单
function onDocumentClick() {
  closeContextMenu()
}

onMounted(() => {
  categoryStore.fetchCategoryTree()
  document.addEventListener('click', onDocumentClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>

<style scoped>
.category-tree-container {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 12px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.tree-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  padding-right: 8px;
}

.node-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.category-icon { color: #409eff; }
.subject-icon { color: #67c23a; }
.chapter-icon { color: #e6a23c; }
.tag-icon { color: #909399; }

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-type-tag {
  font-size: 11px;
  color: #909399;
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 150px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background 0.2s;
}

.menu-item:hover {
  background: #f5f7fa;
}

.menu-item.danger {
  color: #f56c6c;
}

.menu-item.danger:hover {
  background: #fef0f0;
}
</style>