<template>
  <!--
    登录页面 — 账号密码登录 + 微信登录

    【关键】绕过 Pinia store 直接调用 authApi，原因同注册页：
    微信小程序环境中 useUserStore() 可能返回 undefined。
    登录成功后手动写入 storage，tabBar 页 onShow 会 restoreSession 恢复状态。
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
            :disabled="loggingIn"
          />
        </view>
        <view class="login-form__item">
          <text class="login-form__icon">🔒</text>
          <input
            v-model="password"
            class="login-form__input"
            :type="showPwd ? 'text' : 'password'"
            placeholder="请输入密码"
            placeholder-style="color: #C0C4CC"
            :disabled="loggingIn"
          />
          <text class="login-form__toggle" @tap="showPwd = !showPwd">
            {{ showPwd ? '🙈' : '👁️' }}
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
/**
 * 登录页面 — 逻辑层
 *
 * ========== 架构决策 ==========
 * 绕过 Pinia store，直接调用 authApi + 手动写 storage。
 * 原因：微信小程序 UniApp 环境中 useUserStore() 可能返回 undefined。
 * 登录成功后 tabBar 页面 onShow 会调用 restoreSession() 从 storage 恢复状态。
 *
 * ========== 调试日志 ==========
 * [Login] === 开始登录 ===
 * [Login] 请求参数: { username, password: "***" }
 * [Login] 后端响应: { token: "eyJ...", userInfo: {...} }
 * [Login] ✅ 登录成功，跳转首页
 */
import { ref } from 'vue'
import { authApi } from '@/api/modules/auth'
import { storage } from '@/utils/storage'
import { StorageKey } from '@/utils/constants'
import type { LoginResult } from '@/types/user'

// ═══════════════════════════════════════
// 表单
// ═══════════════════════════════════════
const loginMode = ref<'account' | 'wechat'>('account')
const username = ref<string>('')
const password = ref<string>('')
const showPwd = ref<boolean>(false)
const loggingIn = ref<boolean>(false)

// ═══════════════════════════════════════
// 账号密码登录
// ═══════════════════════════════════════
async function handleAccountLogin(): Promise<void> {
  // 防重复点击
  if (loggingIn.value) {
    console.log('[Login] 重复点击已拦截')
    return
  }

  // 前端校验
  if (!username.value.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  console.log('[Login] === 开始登录 ===')
  console.log('[Login] 请求参数:', {
    username: username.value.trim(),
    password: '***',
  })

  loggingIn.value = true

  try {
    // 直接调用 authApi，不经过 Pinia store
    // 拦截器已修复：响应 { code:0, data:{token,userInfo} } 自动解包为 { token, userInfo }
    const result: LoginResult = await authApi.loginByUsername(
      username.value.trim(),
      password.value,
    )

    console.log('[Login] 后端响应:', {
      token: result.token ? `${result.token.substring(0, 20)}...` : 'MISSING',
      userInfo: result.userInfo,
    })

    // 手动写入 storage（替代 store 的持久化）
    storage.set(StorageKey.TOKEN, result.token)
    storage.set(StorageKey.USER_INFO, result.userInfo)

    console.log('[Login] ✅ 登录成功，跳转首页')

    // 跳转首页（index 页 onShow 会 restoreSession 恢复 store 状态）
    uni.reLaunch({ url: '/pages/index/index' })
  } catch (e: unknown) {
    // 后端业务错误（用户名/密码错误等）已由拦截器 handleBusinessError 弹 toast
    // 这里只兜底网络异常
    const err = e as Record<string, unknown>
    console.error('[Login] ❌ 登录失败:', err)

    if (err?.errMsg && typeof err.errMsg === 'string') {
      if (err.errMsg.includes('request:fail') || err.errMsg.includes('timeout')) {
        uni.showToast({ title: '网络异常，请检查网络连接', icon: 'none' })
      }
    } else if (err?.message && typeof err.message === 'string') {
      uni.showToast({ title: err.message, icon: 'none' })
    }
  } finally {
    loggingIn.value = false
    console.log('[Login] === 登录流程结束 ===')
  }
}

// ═══════════════════════════════════════
// 微信登录（开发环境暂不可用）
// ═══════════════════════════════════════
async function handleWechatLogin(): Promise<void> {
  uni.showToast({ title: '微信登录需在真机测试', icon: 'none' })
}

// ═══════════════════════════════════════
// 辅助
// ═══════════════════════════════════════
function handleGoRegister(): void {
  uni.navigateTo({ url: '/pages/register/index' })
}

function handlePrivacy(): void {
  uni.showModal({
    title: '隐私政策',
    content: '我们将严格遵守相关法律法规，保护您的个人信息安全。',
    showCancel: false,
    confirmText: '我知道了',
  })
}

function handleAgreement(): void {
  uni.showModal({
    title: '用户协议',
    content: '使用本小程序即表示同意用户协议。',
    showCancel: false,
    confirmText: '我知道了',
  })
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
