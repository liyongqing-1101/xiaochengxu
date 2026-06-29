<template>
  <!--
    登录页面
    支持: 账号密码登录 + 微信一键登录
  -->
  <view class="login-page">
    <!-- 装饰背景 -->
    <view class="login-bg">
      <view class="login-bg__circle login-bg__circle--1" />
      <view class="login-bg__circle login-bg__circle--2" />
      <view class="login-bg__circle login-bg__circle--3" />
    </view>

    <!-- 内容区 -->
    <view class="login-content">
      <!-- Logo 区域 -->
      <view class="login-logo">
        <text class="login-logo__icon">📚</text>
        <text class="login-logo__title">高校教资题库</text>
        <text class="login-logo__slogan">高效备考，轻松拿证</text>
      </view>

      <!-- 登录模式切换 -->
      <view class="login-tabs">
        <view
          class="login-tabs__item"
          :class="{ 'login-tabs__item--active': loginMode === 'account' }"
          @tap="loginMode = 'account'"
        >
          <text>账号登录</text>
        </view>
        <view
          class="login-tabs__item"
          :class="{ 'login-tabs__item--active': loginMode === 'wechat' }"
          @tap="loginMode = 'wechat'"
        >
          <text>微信登录</text>
        </view>
      </view>

      <!-- ===== 账号密码登录表单 ===== -->
      <view v-if="loginMode === 'account'" class="login-form">
        <view class="login-form__item">
          <text class="login-form__icon">👤</text>
          <input
            v-model="username"
            class="login-form__input"
            placeholder="请输入用户名"
            placeholder-style="color: #C0C4CC"
          />
        </view>
        <view class="login-form__item">
          <text class="login-form__icon">🔒</text>
          <input
            v-model="password"
            class="login-form__input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            placeholder-style="color: #C0C4CC"
          />
          <text class="login-form__toggle" @tap="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </text>
        </view>

        <view
          class="login-form__btn"
          :class="{ 'login-form__btn--loading': loggingIn }"
          @tap="handleAccountLogin"
        >
          <text>{{ loggingIn ? '登录中...' : '登 录' }}</text>
        </view>

        <view class="login-form__footer">
          <text class="login-form__footer-text">没有账号？</text>
          <text class="login-form__footer-link" @tap="handleGoRegister">去注册</text>
        </view>
      </view>

      <!-- ===== 微信登录 ===== -->
      <view v-else class="login-wechat">
        <view class="login-wechat__desc">
          <text>授权微信账号快速登录</text>
        </view>
        <view
          class="login-wechat__btn"
          :class="{ 'login-wechat__btn--loading': loggingIn }"
          @tap="handleWechatLogin"
        >
          <text class="login-wechat__btn-icon">💬</text>
          <text>{{ loggingIn ? '登录中...' : '微信一键登录' }}</text>
        </view>
        <view class="login-form__footer" style="margin-top: 40rpx;">
          <text class="login-form__footer-text">没有账号？</text>
          <text class="login-form__footer-link" @tap="handleGoRegister">去注册</text>
        </view>
      </view>

      <!-- 协议 -->
      <view class="login-agreement">
        <text class="login-agreement__text">
          登录即表示同意
          <text class="login-agreement__link" @tap="handlePrivacy">《用户协议》</text>
          和
          <text class="login-agreement__link" @tap="handleAgreement">《隐私政策》</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loginMode = ref<'account' | 'wechat'>('account')
const username = ref('')
const password = ref('')
const showPassword = ref(false)
const loggingIn = ref(false)

/** 账号密码登录 */
async function handleAccountLogin(): Promise<void> {
  if (!username.value.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  loggingIn.value = true
  try {
    await userStore.loginByPassword(username.value.trim(), password.value)
    uni.reLaunch({ url: '/pages/index/index' })
  } catch {
    // 业务错误已由拦截器显示 toast，此处不再重复提示
  } finally {
    loggingIn.value = false
  }
}

/** 微信登录 */
async function handleWechatLogin(): Promise<void> {
  loggingIn.value = true
  try {
    await userStore.login()
    uni.reLaunch({ url: '/pages/index/index' })
  } catch {
    // 错误已在 store 中处理
  } finally {
    loggingIn.value = false
  }
}

/** 跳转注册页 */
function handleGoRegister(): void {
  uni.navigateTo({ url: '/pages/register/index' })
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
  background: linear-gradient(180deg, #EBF3FC 0%, #F5F7FA 50%, #FFFFFF 100%);
  position: relative;
  overflow: hidden;
}

// 装饰背景
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;

  &__circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.06;

    &--1 {
      width: 500rpx;
      height: 500rpx;
      background: #4A90D9;
      top: -150rpx;
      right: -180rpx;
    }

    &--2 {
      width: 300rpx;
      height: 300rpx;
      background: #52C41A;
      bottom: 200rpx;
      left: -120rpx;
    }

    &--3 {
      width: 200rpx;
      height: 200rpx;
      background: #FAAD14;
      top: 400rpx;
      right: -60rpx;
    }
  }
}

// 内容区
.login-content {
  position: relative;
  z-index: 1;
  padding: 160rpx $spacing-xl $spacing-xxl;
}

// Logo
.login-logo {
  @include flex-column-center;
  margin-bottom: 60rpx;

  &__icon {
    font-size: 80rpx;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: 40rpx;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
  }

  &__slogan {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}

// 切换 Tab
.login-tabs {
  @include flex-center;
  background: rgba(255, 255, 255, 0.7);
  border-radius: $radius-xl;
  padding: 6rpx;
  margin-bottom: 40rpx;
}

.login-tabs__item {
  flex: 1;
  @include flex-center;
  height: 72rpx;
  border-radius: $radius-xl;
  transition: all 0.3s;

  text {
    font-size: $font-size-base;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }

  &--active {
    background: #fff;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);

    text {
      color: $color-primary;
      font-weight: $font-weight-semibold;
    }
  }
}

// 表单
.login-form {
  &__item {
    @include flex-start;
    background: #fff;
    border-radius: $radius-base;
    padding: $spacing-md $spacing-base;
    margin-bottom: $spacing-md;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    gap: $spacing-sm;
  }

  &__icon {
    font-size: $font-size-lg;
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-primary;
  }

  &__toggle {
    font-size: $font-size-lg;
    padding: $spacing-xs;
    flex-shrink: 0;
  }

  &__btn {
    @include flex-center;
    height: 96rpx;
    background: $gradient-primary;
    border-radius: $radius-xl;
    margin-top: $spacing-xl;
    box-shadow: 0 4rpx 16rpx rgba(74, 144, 217, 0.35);

    text {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: #fff;
    }

    &--loading {
      opacity: 0.7;
    }
  }

  &__footer {
    @include flex-center;
    margin-top: $spacing-xl;
    gap: $spacing-xs;

    &-text {
      font-size: $font-size-sm;
      color: $color-text-placeholder;
    }

    &-link {
      font-size: $font-size-sm;
      color: $color-primary;
      font-weight: $font-weight-medium;
    }
  }
}

// 微信登录
.login-wechat {
  &__desc {
    text-align: center;
    margin-bottom: $spacing-xl;

    text {
      font-size: $font-size-sm;
      color: $color-text-placeholder;
    }
  }

  &__btn {
    @include flex-center;
    height: 96rpx;
    background: #07C160;
    border-radius: $radius-xl;
    gap: $spacing-sm;
    box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.3);

    text {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: #fff;
    }

    &--loading {
      opacity: 0.7;
    }
  }

  &__btn-icon {
    font-size: $font-size-xl;
  }
}

// 协议
.login-agreement {
  @include flex-center;
  margin-top: 60rpx;

  &__text {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  &__link {
    color: $color-primary;
  }
}
</style>
