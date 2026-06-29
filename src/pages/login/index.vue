<template>
  <!--
    登录页面
    微信一键授权登录 → 后端 JWT 接口 → 存入 Pinia + 本地存储 → 跳转首页
  -->
  <view class="login-page">
    <!-- 顶部装饰 -->
    <view class="login-bg">
      <view class="login-bg__circle login-bg__circle--1" />
      <view class="login-bg__circle login-bg__circle--2" />
    </view>

    <!-- Logo + 名称 -->
    <view class="login-header">
      <view class="login-header__logo">
        <text class="login-header__logo-text">📚</text>
      </view>
      <text class="login-header__name">高校教资题库</text>
      <text class="login-header__slogan">轻松备考, 高效刷题</text>
    </view>

    <!-- 微信一键登录按钮 -->
    <view class="login-body">
      <view class="login-btn" :class="{ 'login-btn--loading': loading }" @tap="handleLogin">
        <image class="login-btn__icon" src="/static/images/icons/wechat.png" mode="aspectFit" />
        <text class="login-btn__text">{{ loading ? '登录中...' : '微信一键登录' }}</text>
      </view>

      <!-- 隐私协议 -->
      <view class="login-agreement">
        <text class="login-agreement__text">
          登录即表示同意
          <text class="login-agreement__link" @tap="handlePrivacy">《隐私政策》</text>
          和
          <text class="login-agreement__link" @tap="handleAgreement">《用户协议》</text>
        </text>
      </view>
    </view>

    <!-- 游客模式 -->
    <view class="login-guest">
      <text class="login-guest__text" @tap="handleGuest">暂不登录, 先看看</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)

async function handleLogin(): Promise<void> {
  if (loading.value) return
  loading.value = true

  try {
    await userStore.login()
    uni.reLaunch({ url: '/pages/index/index' })
  } catch {
    // 错误已在 store 中处理
  } finally {
    loading.value = false
  }
}

/** 游客模式 — 直接进入首页 */
function handleGuest(): void {
  uni.reLaunch({ url: '/pages/index/index' })
}

function handlePrivacy(): void {
  uni.showToast({ title: '隐私政策', icon: 'none' })
}

function handleAgreement(): void {
  uni.showToast({ title: '用户协议', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  @include flex-column-center;
  background: $color-bg-white;
  position: relative;
  overflow: hidden;
}

// 背景装饰
.login-bg {
  @include absolute-fill;
  pointer-events: none;

  &__circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.06;

    &--1 {
      width: 600rpx;
      height: 600rpx;
      background: $color-primary;
      top: -200rpx;
      right: -200rpx;
    }

    &--2 {
      width: 400rpx;
      height: 400rpx;
      background: $color-primary-light;
      bottom: -100rpx;
      left: -100rpx;
    }
  }
}

// Logo 区
.login-header {
  @include flex-column-center;
  gap: $spacing-sm;
  position: relative;
  z-index: 1;
  margin-bottom: $spacing-xxl;
}

.login-header__logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-xl;
  @include gradient-bg(#4A90D9, #7AB3E8);
  @include flex-center;
  box-shadow: 0 8rpx 32rpx rgba(74, 144, 217, 0.3);
}

.login-header__logo-text {
  font-size: 80rpx;
}

.login-header__name {
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.login-header__slogan {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

// 登录按钮
.login-body {
  @include flex-column-center;
  gap: $spacing-lg;
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 0 $spacing-xl;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  border-radius: $radius-xl;
  background: #07C160;
  @include flex-center;
  gap: $spacing-sm;
  box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.3);
  transition: opacity 0.2s;

  &--loading {
    opacity: 0.7;
  }
}

.login-btn__icon {
  width: 44rpx;
  height: 44rpx;
}

.login-btn__text {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: #fff;
}

// 隐私协议
.login-agreement {
  @include flex-center;
}

.login-agreement__text {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  text-align: center;
  line-height: $line-height-loose;
}

.login-agreement__link {
  color: $color-primary;
}

// 游客模式
.login-guest {
  position: absolute;
  bottom: calc($spacing-xxl + env(safe-area-inset-bottom));
  z-index: 1;
}

.login-guest__text {
  font-size: $font-size-sm;
  color: $color-text-placeholder;
}
</style>
