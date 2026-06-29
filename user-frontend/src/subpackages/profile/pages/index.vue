<template>
  <!--
    我的页面 — 个人中心
    功能:
    - 微信头像昵称
    - 会员状态占位
    - 刷题数据统计
    - 菜单列表(收藏/记录/设置/反馈)
  -->
  <view class="profile-page">
    <!-- 顶部背景 -->
    <view class="profile-header-bg" />

    <!-- 用户信息卡片 -->
    <view class="profile-user-card">
      <view class="profile-user-card__top">
        <image class="profile-user-card__avatar" :src="userStore.avatar" mode="aspectFill" />
        <view class="profile-user-card__info">
          <text class="profile-user-card__name">{{ userStore.nickname }}</text>
          <view class="profile-user-card__membership">
            <text class="profile-user-card__vip-text">{{ userStore.isVip ? 'VIP会员' : '免费用户' }}</text>
          </view>
        </view>
        <view class="profile-user-card__arrow">
          <text>›</text>
        </view>
      </view>

      <!-- 统计数据 -->
      <view class="profile-stats">
        <view class="profile-stat-item">
          <text class="profile-stat-item__num">{{ userStore.totalQuestions }}</text>
          <text class="profile-stat-item__label">总做题</text>
        </view>
        <view class="profile-stat-item">
          <text class="profile-stat-item__num profile-stat-item__num--primary">
            {{ accuracyPercent }}
          </text>
          <text class="profile-stat-item__label">正确率</text>
        </view>
        <view class="profile-stat-item">
          <text class="profile-stat-item__num">{{ userStore.streakDays }}</text>
          <text class="profile-stat-item__label">打卡天数</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="profile-section">
      <view class="profile-menu-grid">
        <view class="profile-menu-item" @tap="handleNavigate('/subpackages/wrong-book/pages/index')">
          <text class="profile-menu-item__emoji">📝</text>
          <text class="profile-menu-item__text">错题本</text>
          <text class="profile-menu-item__count" v-if="wrongCount > 0">{{ wrongCount }}题</text>
        </view>
        <view class="profile-menu-item" @tap="handleNavigate('/subpackages/profile/pages/favorites')">
          <text class="profile-menu-item__emoji">⭐</text>
          <text class="profile-menu-item__text">我的收藏</text>
        </view>
        <view class="profile-menu-item" @tap="handleNavigate('/subpackages/profile/pages/history')">
          <text class="profile-menu-item__emoji">📋</text>
          <text class="profile-menu-item__text">练习记录</text>
        </view>
        <view class="profile-menu-item" @tap="handleCheckIn">
          <text class="profile-menu-item__emoji">🔥</text>
          <text class="profile-menu-item__text">每日打卡</text>
        </view>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="profile-section">
      <van-cell-group inset>
        <van-cell title="我的收藏" icon="star-o" is-link @click="handleNavigate('/subpackages/profile/pages/favorites')" />
        <van-cell title="练习记录" icon="clock-o" is-link @click="handleNavigate('/subpackages/profile/pages/history')" />
        <van-cell title="设置" icon="setting-o" is-link @click="handleNavigate('/subpackages/profile/pages/settings')" />
        <van-cell title="意见反馈" icon="chat-o" is-link @click="handleFeedback" />
      </van-cell-group>
    </view>

    <!-- 退出登录 -->
    <view class="profile-logout">
      <van-button type="default" block round @click="handleLogout">退出登录</van-button>
    </view>

    <!-- 安全区域底部 -->
    <view class="profile-safe-bottom" />

    <!-- 底部 TabBar -->
    <CustomTabbar
      current-path="/subpackages/profile/pages/index"
      :wrong-count="wrongCount"
      @change="handleTabChange"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useWrongBookStore } from '@/stores/wrongBook'
import { useCheckIn } from '@/composables/useCheckIn'
import CustomTabbar from '@components/tabbar/CustomTabbar.vue'

const userStore = useUserStore()
const wrongBookStore = useWrongBookStore()
const { doCheckIn } = useCheckIn()

const wrongCount = computed(() => wrongBookStore.totalCount)
const accuracyPercent = computed(() => {
  const acc = userStore.accuracy
  return acc > 0 ? `${(acc * 100).toFixed(1)}%` : '0%'
})

function handleNavigate(url: string): void {
  uni.navigateTo({ url })
}

function handleTabChange(path: string): void {
  uni.switchTab({ url: path })
}

async function handleCheckIn(): Promise<void> {
  await doCheckIn()
}

function handleFeedback(): void {
  uni.showModal({
    title: '意见反馈',
    editable: true,
    placeholderText: '请输入您的反馈意见...',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          const { userApi } = await import('@/api/modules/user')
          await userApi.submitFeedback(res.content)
          uni.showToast({ title: '感谢反馈!', icon: 'success' })
        } catch {
          uni.showToast({ title: '提交失败', icon: 'none' })
        }
      }
    },
  })
}

function handleLogout(): void {
  uni.showModal({
    title: '确认退出',
    content: '退出登录后需要重新授权',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.reLaunch({ url: '/pages/login/index' })
      }
    },
  })
}

onMounted(() => {
  userStore.fetchStats()
  wrongBookStore.loadFromStorage()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $color-bg-page;
}

// 顶部背景
.profile-header-bg {
  height: 200rpx;
  background: linear-gradient(180deg, #4A90D9, #7AB3E8);
}

// 用户信息卡片
.profile-user-card {
  margin: -80rpx $spacing-base 0;
  background: $color-bg-white;
  border-radius: $radius-md;
  padding: $spacing-md;
  box-shadow: $shadow-base;
}

.profile-user-card__top {
  @include flex-start;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.profile-user-card__avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 4rpx solid $color-primary-bg;
}

.profile-user-card__info {
  flex: 1;
}

.profile-user-card__name {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  display: block;
}

.profile-user-card__membership {
  margin-top: 4rpx;
}

.profile-user-card__vip-text {
  font-size: $font-size-xs;
  color: $color-warning;
  background: #FFF7E6;
  padding: 4rpx 12rpx;
  border-radius: $radius-sm;
}

.profile-user-card__arrow {
  font-size: $font-size-xxl;
  color: $color-text-placeholder;
}

// 统计
.profile-stats {
  @include flex-between;
  padding: $spacing-md 0;
  border-top: 1rpx solid $color-border-light;
}

.profile-stat-item {
  flex: 1;
  @include flex-column-center;
  gap: 4rpx;
}

.profile-stat-item__num {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;

  &--primary {
    color: $color-primary;
  }
}

.profile-stat-item__label {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

// 快捷入口
.profile-section {
  margin: $spacing-sm $spacing-base;
}

.profile-menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-sm;
}

.profile-menu-item {
  @include card($spacing-md, $radius-base);
  @include flex-column-center;
  gap: $spacing-xs;
  position: relative;
}

.profile-menu-item__emoji {
  font-size: 40rpx;
}

.profile-menu-item__text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.profile-menu-item__count {
  font-size: $font-size-xs;
  color: $color-danger;
}

// 退出登录
.profile-logout {
  margin: $spacing-lg $spacing-base;
}

// 安全区域
.profile-safe-bottom {
  height: calc($tabbar-height + env(safe-area-inset-bottom) + $spacing-md);
}
</style>
