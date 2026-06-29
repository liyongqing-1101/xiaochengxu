<template>
  <view class="register-page">
    <!-- 装饰背景 -->
    <view class="register-bg">
      <view class="register-bg__circle register-bg__circle--1" />
      <view class="register-bg__circle register-bg__circle--2" />
    </view>

    <!-- 内容区 -->
    <view class="register-content">
      <!-- Logo -->
      <view class="register-logo">
        <text class="register-logo__icon">📚</text>
        <text class="register-logo__title">高校教资题库</text>
        <text class="register-logo__subtitle">注册账号，开始备考</text>
      </view>

      <!-- 表单 -->
      <view class="register-form">
        <!-- 用户名 -->
        <view class="register-form__item">
          <text class="register-form__icon">👤</text>
          <input
            v-model="username"
            class="register-form__input"
            placeholder="请输入用户名"
            placeholder-style="color: #C0C4CC"
            :maxlength="20"
          />
        </view>

        <!-- 密码 -->
        <view class="register-form__item">
          <text class="register-form__icon">🔒</text>
          <input
            v-model="password"
            class="register-form__input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码（8-18位）"
            placeholder-style="color: #C0C4CC"
            :maxlength="18"
          />
          <text class="register-form__toggle" @tap="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </text>
        </view>

        <!-- 确认密码 -->
        <view class="register-form__item">
          <text class="register-form__icon">🔒</text>
          <input
            v-model="confirmPassword"
            class="register-form__input"
            :type="showConfirm ? 'text' : 'password'"
            placeholder="请确认密码"
            placeholder-style="color: #C0C4CC"
            :maxlength="18"
          />
          <text class="register-form__toggle" @tap="showConfirm = !showConfirm">
            {{ showConfirm ? '🙈' : '👁️' }}
          </text>
        </view>

        <!-- 注册按钮 -->
        <view
          class="register-form__btn"
          :class="{ 'register-form__btn--loading': registering }"
          @tap="handleRegister"
        >
          <text>{{ registering ? '注册中...' : '注 册' }}</text>
        </view>

        <!-- 去登录 -->
        <view class="register-form__footer">
          <text class="register-form__footer-text">已有账号？</text>
          <text class="register-form__footer-link" @tap="handleGoLogin">去登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirm = ref(false)
const registering = ref(false)

function validate(): string | null {
  if (!username.value.trim()) {
    return '请输入用户名'
  }
  if (password.value.length < 8 || password.value.length > 18) {
    return '密码长度需为8-18位'
  }
  if (password.value !== confirmPassword.value) {
    return '两次密码不一致'
  }
  return null
}

async function handleRegister(): Promise<void> {
  const error = validate()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }

  registering.value = true
  try {
    await userStore.register(username.value.trim(), password.value, confirmPassword.value)
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch (e: any) {
    // 业务错误已由拦截器显示 toast，此处不再重复提示
  } finally {
    registering.value = false
  }
}

function handleGoLogin(): void {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #EBF3FC 0%, #F5F7FA 50%, #FFFFFF 100%);
  position: relative;
  overflow: hidden;
}

// 装饰背景
.register-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;

  &__circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.08;

    &--1 {
      width: 400rpx;
      height: 400rpx;
      background: #4A90D9;
      top: -100rpx;
      right: -120rpx;
    }

    &--2 {
      width: 300rpx;
      height: 300rpx;
      background: #52C41A;
      bottom: -80rpx;
      left: -100rpx;
    }
  }
}

// 内容区
.register-content {
  position: relative;
  z-index: 1;
  padding: 120rpx $spacing-xl $spacing-xxl;
}

.register-logo {
  @include flex-column-center;
  margin-bottom: 80rpx;

  &__icon {
    font-size: 72rpx;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
  }

  &__subtitle {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}

// 表单
.register-form {
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
</style>
