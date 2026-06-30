<template>
  <!-- 自定义底部 TabBar -->
  <view class="custom-tabbar" :style="{ paddingBottom: safeAreaBottom }">
    <view
      v-for="tab in tabs"
      :key="tab.path"
      class="custom-tabbar__item"
      :class="{ 'custom-tabbar__item--active': tab.path === currentPath }"
      @tap="handleTabClick(tab)"
    >
      <text class="custom-tabbar__icon">{{ tab.path === currentPath ? tab.activeIcon : tab.icon }}</text>
      <text class="custom-tabbar__text">{{ tab.text }}</text>

      <!-- 错题数角标 -->
      <view v-if="tab.badge && tab.badge > 0" class="custom-tabbar__badge">
        <text class="custom-tabbar__badge-text">{{ tab.badge > 99 ? '99+' : tab.badge }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface TabItem {
  path: string
  text: string
  icon: string
  activeIcon: string
  badge?: number
}

const props = withDefaults(defineProps<{
  currentPath: string
  wrongCount?: number
}>(), {
  wrongCount: 0,
})

const emit = defineEmits<{
  change: [path: string]
}>()

const safeAreaBottom = 'env(safe-area-inset-bottom)'

const tabs = computed<TabItem[]>(() => [
  {
    path: '/pages/index/index',
    text: '首页',
    icon: '🏠',
    activeIcon: '🏠',
  },
  {
    path: '/pages/study/index',
    text: '学习',
    icon: '📖',
    activeIcon: '📖',
  },
  {
    path: '/pages/teaching-aid/index',
    text: '教辅',
    icon: '📚',
    activeIcon: '📚',
  },
  {
    path: '/pages/community/index',
    text: '社区',
    icon: '💬',
    activeIcon: '💬',
  },
  {
    path: '/pages/profile/index',
    text: '我的',
    icon: '👤',
    activeIcon: '👤',
    badge: props.wrongCount,
  },
])

function handleTabClick(tab: TabItem): void {
  if (tab.path === props.currentPath) return

  emit('change', tab.path)

  uni.switchTab({
    url: tab.path,
  })
}
</script>

<style lang="scss" scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: $color-bg-white;
  border-top: 1rpx solid $color-border-light;
  z-index: $z-index-sticky;
  height: calc($tabbar-height + env(safe-area-inset-bottom));

  &__item {
    flex: 1;
    @include flex-column-center;
    padding: $spacing-xs 0;
    position: relative;
  }

  &__icon {
    font-size: 44rpx;
    line-height: 1;
    margin-bottom: 4rpx;
  }

  &__text {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    transition: color 0.2s;

    .custom-tabbar__item--active & {
      color: $color-primary;
      font-weight: $font-weight-medium;
    }
  }

  &__badge {
    position: absolute;
    top: 4rpx;
    right: 50%;
    transform: translateX(36rpx);
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background: $color-danger;
    border-radius: 16rpx;
    @include flex-center;
  }

  &__badge-text {
    font-size: 18rpx;
    color: #fff;
    line-height: 1;
  }
}
</style>
