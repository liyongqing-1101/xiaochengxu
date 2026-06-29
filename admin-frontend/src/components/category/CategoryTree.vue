<template>
  <div class="category-tree-container">
    <div class="tree-header">
      <h3>分类结构</h3>
      <el-button type="primary" size="small" @click="handleAddRoot">新增大类</el-button>
    </div>

    <el-tree
      ref="treeRef"
      :data="localTree"
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
          <span class="node-icon">{{ iconForType(data.type) }}</span>
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
        <span class="menu-icon">＋</span>
        <span>添加子{{ childTypeLabel(contextMenu.data?.type) }}</span>
      </div>
      <div class="menu-item" @click="handleMenuEdit">
        <span class="menu-icon">✎</span>
        <span>编辑</span>
      </div>
      <div class="menu-item danger" @click="handleMenuDelete">
        <span class="menu-icon">✕</span>
        <span>删除</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getCategoryTree } from '@/api/modules/category'
import { useCategoryStore } from '@/stores/category'

const emit = defineEmits<{
  (e: 'node-click', node: any): void
  (e: 'add', parent: any): void
  (e: 'edit', node: any): void
  (e: 'delete', node: any): void
}>()

const categoryStore = useCategoryStore()
const treeRef = ref()
const localTree = ref<any[]>([])

// 右键菜单状态
const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  data: null as any,
})

// 直接调用 API 获取最新数据，不依赖 store 缓存
async function loadTreeData() {
  try {
    const data = await getCategoryTree()
    localTree.value = buildTreeData(data || [])
  } catch (e) {
    console.error('加载分类树失败', e)
  }
}

function buildTreeData(categories: any[]): any[] {
  return categories.map(cat => ({
    id: `cat-${cat.id}`,
    label: cat.name,
    type: 'category',
    raw: cat,
    children: (cat.examSubjects || []).map((subj: any) => ({
      id: `subj-${subj.id}`,
      label: subj.name,
      type: 'subject',
      raw: subj,
      children: (subj.examChapters || []).map((chap: any) => ({
        id: `chap-${chap.id}`,
        label: chap.name,
        type: 'chapter',
        raw: chap,
        children: (chap.examTags || []).map((tag: any) => ({
          id: `tag-${tag.id}`,
          label: tag.name,
          type: 'tag',
          raw: tag,
        })) || [],
      })) || [],
    })) || [],
  }))
}

function typeLabel(type: string) {
  const map: Record<string, string> = {
    category: '大类',
    subject: '科目',
    chapter: '章节',
    tag: '知识点',
  }
  return map[type] || ''
}

function iconForType(type: string) {
  const map: Record<string, string> = {
    category: '📁',
    subject: '📖',
    chapter: '📄',
    tag: '🏷️',
  }
  return map[type] || '📌'
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

// 单击节点：选中并查看详情
function handleNodeClick(data: any) {
  closeContextMenu()
  // 传递完整的节点数据（包含 type 信息）
  emit('node-click', { ...data.raw, _type: data.type })
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
  emit('add', { ...contextMenu.data.raw, _type: contextMenu.data.type })
  closeContextMenu()
}

function handleMenuEdit() {
  if (!contextMenu.data) return
  emit('edit', { ...contextMenu.data.raw, _type: contextMenu.data.type })
  closeContextMenu()
}

function handleMenuDelete() {
  if (!contextMenu.data) return
  const data = contextMenu.data
  ElMessageBox.confirm(`确定要删除「${data.label}」吗？删除后子分类也会一并删除。`, '确认删除', {
    type: 'warning',
  }).then(() => {
    emit('delete', { ...data.raw, _type: data.type })
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

.menu-icon {
  width: 18px;
  text-align: center;
  font-size: 14px;
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