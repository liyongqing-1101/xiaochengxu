<template>
  <!-- 全屏遮罩 + 底部弹窗容器 -->
  <view v-if="visible" class="popup-mask" @tap="handleMaskClick">
    <!-- 弹窗主体（从底部滑入） -->
    <view
      class="popup-content"
      :class="{ 'popup-content--show': visible }"
      @tap.stop
    >
      <!-- 标题行 -->
      <view class="popup__header">
        <text class="popup__title">随机刷题</text>
        <view class="popup__close" @tap.stop="handleClose">
          <text class="popup__close-icon">✕</text>
        </view>
      </view>

      <!-- 副标题 -->
      <text class="popup__subtitle">选择科目</text>

      <!-- 科目标签（横向4个并排） -->
      <view class="popup__tags">
        <view
          v-for="subject in subjects"
          :key="subject.id"
          class="popup__tag"
          :class="{ 'popup__tag--active': selectedId === subject.id }"
          @tap.stop="handleSelect(subject.id)"
        >
          <text>{{ subject.name }}</text>
        </view>
      </view>

      <!-- 提示文字 -->
      <text class="popup__hint">不选则刷全部科目</text>

      <!-- 开始按钮 -->
      <view class="popup__btn" @tap.stop="handleStart">
        <text class="popup__btn-text">开始刷题</text>
      </view>

      <!-- 底部安全区 -->
      <view class="popup__safe-bottom" />
    </view>
  </view>
</template>

<script setup lang="ts">
import type { Subject } from '@/types/exam'

interface Props {
  visible: boolean
  subjects: Subject[]
  selectedId: number | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', id: number | null): void
  (e: 'start', subjectId: number | null): void
}>()

function handleMaskClick(): void {
  emit('close')
}

function handleClose(): void {
  emit('close')
}

function handleSelect(id: number): void {
  // 单选逻辑：点击已选中则取消
  const newId = props.selectedId === id ? null : id
  emit('select', newId)
}

function handleStart(): void {
  emit('start', props.selectedId)
}
</script>

<style lang="scss" scoped>
// 全屏遮罩层
.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end; // 底部对齐
}

// 弹窗主体（默认在屏幕下方外）
.popup-content {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 48rpx 32rpx 32rpx;
  box-sizing: border-box;
  transform: translateY(100%); // 默认在屏幕外
  transition: transform 0.3s ease-out;
}

// 显示时滑入
.popup-content--show {
  transform: translateY(0);
}

.popup__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.popup__title {
  font-size: 40rpx;
  font-weight: bold;
  color: #1D2129;
}

.popup__close {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: -16rpx;
}

.popup__close-icon {
  font-size: 36rpx;
  color: #86909C;
}

.popup__subtitle {
  font-size: 28rpx;
  color: #86909C;
  margin-bottom: 24rpx;
  display: block;
}

.popup__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.popup__tag {
  padding: 16rpx 32rpx;
  background: #F5F7FA;
  border-radius: 40rpx;
  transition: all 0.2s;

  text {
    font-size: 28rpx;
    color: #86909C;
  }

  &--active {
    background: #4A90D9;

    text {
      color: #fff;
      font-weight: 500;
    }
  }
}

.popup__hint {
  font-size: 24rpx;
  color: #C9CDD4;
  margin-bottom: 48rpx;
  display: block;
}

.popup__btn {
  height: 96rpx;
  background: #1D2129;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
  }
}

.popup__safe-bottom {
  height: env(safe-area-inset-bottom);
}
</style>
