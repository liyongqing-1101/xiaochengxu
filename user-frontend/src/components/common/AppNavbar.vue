<template>
  <!-- 自定义导航栏 -->
  <view class="app-navbar" :style="navbarStyle">
    <!-- 状态栏占位 -->
    <view class="app-navbar__status-bar" :style="{ height: statusBarHeight + 'px' }" />

    <!-- 导航栏主体 -->
    <view class="app-navbar__body" :style="{ height: navHeight + 'px' }">
      <!-- 返回按钮 -->
      <view v-if="showBack" class="app-navbar__back" @tap="handleBack">
        <text class="app-navbar__back-icon">{{ backIcon }}</text>
      </view>

      <!-- 标题 -->
      <view class="app-navbar__title" :class="{ 'has-back': showBack }">
        <text class="app-navbar__title-text">{{ title }}</text>
      </view>

      <!-- 右侧操作区(微信胶囊占位) -->
      <view class="app-navbar__capsule" />
    </view>
  </view>
  <!-- 占位元素 -->
  <view :style="{ height: (statusBarHeight + navHeight) + 'px' }" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  title?: string
  showBack?: boolean
  backIcon?: string
  backgroundColor?: string
}>(), {
  title: '',
  showBack: false,
  backIcon: '←',
  backgroundColor: '#FFFFFF',
})

const emit = defineEmits<{
  back: []
}>()

// 获取系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = systemInfo.statusBarHeight || 20
const navHeight = 44

const navbarStyle = computed(() => ({
  background: props.backgroundColor,
}))

function handleBack(): void {
  emit('back')
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.app-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background: $color-bg-white;

  &__body {
    @include flex-between;
    padding: 0 $spacing-base;
    position: relative;
  }

  &__back {
    width: 80rpx;
    height: 100%;
    @include flex-center;
    flex-shrink: 0;

    &-icon {
      font-size: $font-size-xl;
      color: $color-text-primary;
    }
  }

  &__title {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    max-width: 50%;

    &.has-back {
      left: calc(50% + 40rpx);
    }

    &-text {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      @include text-ellipsis(1);
    }
  }

  &__capsule {
    width: 87px; // 微信胶囊宽度
    height: 32px;
    flex-shrink: 0;
  }
}
</style>
