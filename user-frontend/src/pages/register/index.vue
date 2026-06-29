<template>
  <!--
    注册页面 — 用户名密码注册
    校验流程: 前端格式校验 → 后端业务校验 → 成功跳转
  -->
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
            placeholder="请输入用户名（4-20位字母/数字/下划线）"
            placeholder-style="color: #C0C4CC"
            :maxlength="20"
            :disabled="registering"
          />
        </view>

        <!-- 密码 -->
        <view class="register-form__item">
          <text class="register-form__icon">🔒</text>
          <input
            v-model="password"
            class="register-form__input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码（8-18位，含字母+数字）"
            placeholder-style="color: #C0C4CC"
            :maxlength="18"
            :disabled="registering"
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
            :disabled="registering"
          />
          <text class="register-form__toggle" @tap="showConfirm = !showConfirm">
            {{ showConfirm ? '🙈' : '👁️' }}
          </text>
        </view>

        <!-- 用户协议 -->
        <view class="register-form__agreement">
          <view class="register-form__checkbox" @tap="agreed = !agreed">
            <text :class="agreed ? 'register-form__checkbox--checked' : 'register-form__checkbox--unchecked'">
              {{ agreed ? '☑' : '☐' }}
            </text>
          </view>
          <text class="register-form__agreement-text">
            已阅读并同意
            <text class="register-form__agreement-link" @tap.stop="handleShowAgreement('privacy')">《隐私政策》</text>
            和
            <text class="register-form__agreement-link" @tap.stop="handleShowAgreement('terms')">《用户协议》</text>
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
/**
 * 注册页面 — 逻辑层
 *
 * 校验分层:
 *   第1层: 前端格式校验（用户名/密码格式、两次密码一致、协议勾选）
 *   第2层: 后端业务校验（用户名重复等）→ 拦截器 handleBusinessError 自动弹 toast
 *   第3层: 网络超时兜底 → catch 块统一提示
 */
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

// ═══════════════════════════════════════
// 表单数据
// ═══════════════════════════════════════

/** 用户名 */
const username = ref<string>('')

/** 密码 */
const password = ref<string>('')

/** 确认密码 */
const confirmPassword = ref<string>('')

/** 是否显示密码明文 */
const showPassword = ref<boolean>(false)

/** 是否显示确认密码明文 */
const showConfirm = ref<boolean>(false)

/** 是否已勾选用户协议 */
const agreed = ref<boolean>(false)

/** 注册进行中标志（防重复点击 + 输入框禁用） */
const registering = ref<boolean>(false)

// ═══════════════════════════════════════
// 校验常量
// ═══════════════════════════════════════

/** 用户名正则：4-20位，字母开头，可包含字母/数字/下划线 */
const USERNAME_REGEX: RegExp = /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/

/** 密码最小长度 */
const PASSWORD_MIN: number = 8

/** 密码最大长度 */
const PASSWORD_MAX: number = 18

/** 密码强度正则：必须同时包含字母和数字 */
const PASSWORD_STRONG_REGEX: RegExp = /^(?=.*[a-zA-Z])(?=.*\d).+$/

// ═══════════════════════════════════════
// 校验函数
// ═══════════════════════════════════════

/** 校验结果类型：null = 通过，string = 错误提示 */
type ValidationResult = string | null

/**
 * 前端表单校验
 * 第1层校验，在发起网络请求前执行
 */
function validate(): ValidationResult {
  const name: string = username.value.trim()

  // 用户名为空
  if (!name) {
    return '请输入用户名'
  }

  // 用户名长度
  if (name.length < 4) {
    return '用户名至少4位'
  }

  // 用户名格式：字母开头 + 字母/数字/下划线
  if (!USERNAME_REGEX.test(name)) {
    return '用户名需为4-20位字母/数字/下划线，字母开头'
  }

  // 密码长度
  if (password.value.length < PASSWORD_MIN || password.value.length > PASSWORD_MAX) {
    return `密码长度需为${PASSWORD_MIN}-${PASSWORD_MAX}位`
  }

  // 密码强度：必须同时包含字母和数字
  if (!PASSWORD_STRONG_REGEX.test(password.value)) {
    return '密码需同时包含字母和数字'
  }

  // 两次密码一致性
  if (password.value !== confirmPassword.value) {
    return '两次密码不一致'
  }

  // 用户协议勾选（小程序审核必查项）
  if (!agreed.value) {
    return '请先阅读并同意用户协议和隐私政策'
  }

  return null
}

// ═══════════════════════════════════════
// 事件处理
// ═══════════════════════════════════════

/**
 * 注册按钮点击
 *
 * 关键: useUserStore() 必须在函数内部调用，
 * 避免 UniApp <script setup> 顶层 Pinia 未挂载导致返回 undefined
 */
async function handleRegister(): Promise<void> {
  // ── 防重复点击 ──
  if (registering.value) return

  // ── 在事件处理函数内部获取 store 实例（避免顶层 Pinia 时序问题） ──
  const userStore = useUserStore()

  // ── 调试日志：确认 store 实例和 register 方法存在 ──
  console.log('[Register] userStore 实例:', userStore)
  console.log('[Register] register 方法:', typeof userStore?.register)

  // 兜底：store 仍然为 undefined 时的明确提示
  if (!userStore) {
    console.error('[Register] useUserStore() 返回 undefined，Pinia 未就绪')
    uni.showToast({ title: '系统初始化中，请稍后重试', icon: 'none' })
    return
  }

  // ── 第1层：前端格式校验 ──
  const error: ValidationResult = validate()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }

  // ── 发起注册请求 ──
  const trimmedName: string = username.value.trim()

  console.log('[Register] 请求参数:', {
    username: trimmedName,
    password: '***',
    confirmPassword: '***',
  })

  registering.value = true

  try {
    // 第2层：后端业务校验（用户名重复等）由拦截器 handleBusinessError 弹 toast
    await userStore.register(trimmedName, password.value, confirmPassword.value)

    console.log('[Register] 注册成功')

    uni.showToast({ title: '注册成功', icon: 'success' })

    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        // 有上一页（从登录页跳转过来）→ 返回登录页
        uni.navigateBack()
      } else {
        // 无历史页面栈 → 直接跳转登录页
        uni.redirectTo({ url: '/pages/login/index' })
      }
    }, 1000)
  } catch (e: unknown) {
    // 第3层：网络超时/服务不可用等兜底错误
    // 注意：后端业务错误（code≠0）已由拦截器 handleBusinessError 自动弹 toast
    // 这里只处理网络层异常（超时、断网等）
    const err = e as { errMsg?: string; message?: string }
    console.error('[Register] 注册失败:', err)

    // 判断是否为业务错误（拦截器已处理过）还是网络异常
    if (err?.errMsg && err.errMsg.includes('request:fail')) {
      // 网络层异常 → 兜底提示
      uni.showToast({ title: '网络异常，请检查网络连接', icon: 'none' })
    } else if (err?.message) {
      // 有 message 字段 → 可能是未经过拦截器的异常
      uni.showToast({ title: err.message, icon: 'none' })
    }
    // 其他情况：拦截器已弹 toast，不再重复提示
  } finally {
    registering.value = false
  }
}

/** 跳转登录页 */
function handleGoLogin(): void {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.redirectTo({ url: '/pages/login/index' })
  }
}

/** 查看协议内容 */
function handleShowAgreement(type: 'privacy' | 'terms'): void {
  const titles: Record<string, string> = {
    privacy: '隐私政策',
    terms: '用户协议',
  }
  uni.showModal({
    title: titles[type],
    content: `${titles[type]}内容暂未上线，敬请期待。\n\n我们将严格遵守相关法律法规，保护您的个人信息安全。`,
    showCancel: false,
    confirmText: '我知道了',
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #EBF3FC 0%, #F5F7FA 50%, #FFFFFF 100%);
  position: relative;
  overflow: hidden;
}

// ═══════════════════════════════════════
// 装饰背景
// ═══════════════════════════════════════
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

// ═══════════════════════════════════════
// 内容区
// ═══════════════════════════════════════
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

// ═══════════════════════════════════════
// 表单
// ═══════════════════════════════════════
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

  // ── 用户协议勾选 ──
  &__agreement {
    @include flex-start;
    padding: $spacing-sm 0;
    margin-bottom: $spacing-sm;
    gap: $spacing-xs;
  }

  &__checkbox {
    flex-shrink: 0;
    padding: $spacing-xs;

    &--checked {
      font-size: $font-size-lg;
      color: $color-primary;
    }

    &--unchecked {
      font-size: $font-size-lg;
      color: $color-text-placeholder;
    }
  }

  &__agreement-text {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    line-height: 1.6;
  }

  &__agreement-link {
    color: $color-primary;
  }

  // ── 注册按钮 ──
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

  // ── 底部链接 ──
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
