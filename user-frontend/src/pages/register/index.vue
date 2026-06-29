<template>
  <!--
    注册页面 — 用户名密码注册
    校验流程: 前端格式校验 → 直接调用 authApi → 成功跳转

    【关键】绕过 Pinia store 直接调用 authApi.register()
    原因: 微信小程序环境中 useUserStore() 在 script setup 顶层和函数内部
    均可能返回 undefined（Pinia 模块加载时序问题），导致 register 方法不可用。
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
            placeholder="4-20位字母/数字/下划线，字母开头"
            placeholder-style="color: #C0C4CC"
            :maxlength="20"
            :disabled="submitting"
          />
        </view>

        <!-- 密码 -->
        <view class="register-form__item">
          <text class="register-form__icon">🔒</text>
          <input
            v-model="password"
            class="register-form__input"
            :type="showPwd ? 'text' : 'password'"
            placeholder="8-18位，需同时包含字母和数字"
            placeholder-style="color: #C0C4CC"
            :maxlength="18"
            :disabled="submitting"
          />
          <text class="register-form__toggle" @tap="showPwd = !showPwd">
            {{ showPwd ? '🙈' : '👁️' }}
          </text>
        </view>

        <!-- 确认密码 -->
        <view class="register-form__item">
          <text class="register-form__icon">🔒</text>
          <input
            v-model="confirmPwd"
            class="register-form__input"
            :type="showConfirm ? 'text' : 'password'"
            placeholder="请再次输入密码"
            placeholder-style="color: #C0C4CC"
            :maxlength="18"
            :disabled="submitting"
          />
          <text class="register-form__toggle" @tap="showConfirm = !showConfirm">
            {{ showConfirm ? '🙈' : '👁️' }}
          </text>
        </view>

        <!-- 用户协议（小程序审核必查） -->
        <view class="register-form__agreement">
          <view class="register-form__checkbox" @tap="agreed = !agreed">
            <text :class="agreed ? 'register-form__checkbox--on' : 'register-form__checkbox--off'">
              {{ agreed ? '☑' : '☐' }}
            </text>
          </view>
          <text class="register-form__agreement-text">
            已阅读并同意
            <text class="register-form__link" @tap.stop="showAgreement('privacy')">《隐私政策》</text>
            和
            <text class="register-form__link" @tap.stop="showAgreement('terms')">《用户协议》</text>
          </text>
        </view>

        <!-- 注册按钮 -->
        <view
          class="register-form__btn"
          :class="{ 'register-form__btn--loading': submitting }"
          @tap="handleRegister"
        >
          <text>{{ submitting ? '注册中...' : '注 册' }}</text>
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
 * ========== 架构决策 ==========
 * 绕过 Pinia store，直接调用 authApi.register()。
 * 原因：微信小程序 UniApp 环境中，<script setup> 顶层和事件处理函数内部
 * 调用 useUserStore() 均可能返回 undefined（Pinia 模块加载时序问题）。
 * authApi 是纯 JS 模块导出，无运行时依赖，100% 可靠。
 *
 * ========== 校验分层 ==========
 * 第1层：前端格式校验（正则）→ uni.showToast
 * 第2层：后端业务校验（用户名重复等）→ 拦截器 handleBusinessError 自动弹 toast
 * 第3层：网络异常兜底 → catch 块打印日志 + 弹 toast
 *
 * ========== 调试指南 ==========
 * 微信开发者工具 → Console 面板：
 *   [Register] === 开始注册 ===
 *   [Register] 表单数据: { username, passwordLength, confirmMatch, agreed }
 *   [Register] 校验结果: null | "错误信息"
 *   [Register] 请求参数: { username, password: "***", confirmPassword: "***" }
 *   [Register] 后端响应: { statusCode, data }
 *   [Register] 注册成功 / 注册失败: ...
 * 微信开发者工具 → Network 面板：
 *   应能看到 POST /api/auth/register 请求
 */
import { ref } from 'vue'
import { authApi } from '@/api/modules/auth'

// ═══════════════════════════════════════
// 表单字段
// ═══════════════════════════════════════
const username = ref<string>('')
const password = ref<string>('')
const confirmPwd = ref<string>('')
const showPwd = ref<boolean>(false)
const showConfirm = ref<boolean>(false)
const agreed = ref<boolean>(false)
const submitting = ref<boolean>(false)

// ═══════════════════════════════════════
// 校验正则（常量）
// ═══════════════════════════════════════
/** 用户名：字母开头，4-20位字母/数字/下划线 */
const RE_USERNAME = /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/

/** 密码：8-18位，必须同时包含字母和数字 */
const RE_PASSWORD = /^(?=.*[a-zA-Z])(?=.*\d).{8,18}$/

// ═══════════════════════════════════════
// 前端校验
// ═══════════════════════════════════════

/**
 * 返回 null = 通过，返回 string = 错误提示
 */
function validateForm(): string | null {
  const name = username.value.trim()

  if (!name) {
    return '请输入用户名'
  }
  if (!RE_USERNAME.test(name)) {
    return '用户名需4-20位字母/数字/下划线，字母开头'
  }
  if (!RE_PASSWORD.test(password.value)) {
    return '密码需8-18位，且同时包含字母和数字'
  }
  if (password.value !== confirmPwd.value) {
    return '两次输入的密码不一致'
  }
  if (!agreed.value) {
    return '请先阅读并同意用户协议和隐私政策'
  }
  return null
}

// ═══════════════════════════════════════
// 注册处理
// ═══════════════════════════════════════

async function handleRegister(): Promise<void> {
  // ── 防重复点击 ──
  if (submitting.value) {
    console.log('[Register] 重复点击已拦截')
    return
  }

  console.log('[Register] === 开始注册 ===')
  console.log('[Register] 表单数据:', {
    username: username.value.trim(),
    passwordLength: password.value.length,
    confirmMatch: password.value === confirmPwd.value,
    agreed: agreed.value,
  })

  // ── 第1层：前端格式校验 ──
  const errMsg = validateForm()
  console.log('[Register] 校验结果:', errMsg)
  if (errMsg) {
    uni.showToast({ title: errMsg, icon: 'none', duration: 2000 })
    return
  }

  // ── 发起请求（直接调用 authApi，不经过 Pinia store） ──
  submitting.value = true
  const payload = {
    username: username.value.trim(),
    password: password.value,
    confirmPassword: confirmPwd.value,
  }
  console.log('[Register] 请求参数:', {
    username: payload.username,
    password: '***',
    confirmPassword: '***',
  })

  try {
    // 直接调用 authApi.register()，无需 Pinia store
    // 内部流程: authApi.register → post('/auth/register', payload) → uni.request
    await authApi.register(payload.username, payload.password, payload.confirmPassword)

    console.log('[Register] ✅ 注册成功')

    uni.showToast({ title: '注册成功，请登录', icon: 'success', duration: 1500 })

    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      const pages = getCurrentPages()
      console.log('[Register] 页面栈深度:', pages.length)
      if (pages.length > 1) {
        uni.navigateBack()   // 返回登录页
      } else {
        uni.redirectTo({ url: '/pages/login/index' })
      }
    }, 1500)
  } catch (e: unknown) {
    // ── 第3层：网络/未知异常 ──
    // 注意：后端业务错误（如"用户名已存在"）已由拦截器 handleBusinessError
    // 自动弹 toast，此处只兜底网络异常等拦截器未覆盖的场景
    console.error('[Register] ❌ 注册失败:', e)

    const err = e as Record<string, unknown>
    // 判断是否已被拦截器处理过（拦截器调用 originalFail 时传入 { errMsg } ）
    const rawMsg = err?.errMsg as string | undefined
    if (rawMsg) {
      if (rawMsg.includes('request:fail') || rawMsg.includes('timeout')) {
        uni.showToast({ title: '网络异常，请检查网络连接', icon: 'none' })
      }
      // 其他 errMsg 可能是拦截器传入的业务错误消息，拦截器已弹 toast，不重复
    } else if (err?.message && typeof err.message === 'string') {
      // 未经过拦截器的异常，直接显示 message
      uni.showToast({ title: err.message, icon: 'none' })
    }
    // 兜底：以上都不匹配则静默（拦截器已处理）
  } finally {
    submitting.value = false
    console.log('[Register] === 注册流程结束 ===')
  }
}

// ═══════════════════════════════════════
// 辅助函数
// ═══════════════════════════════════════

/** 返回登录页 */
function handleGoLogin(): void {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.redirectTo({ url: '/pages/login/index' })
  }
}

/** 显示协议弹窗 */
function showAgreement(type: 'privacy' | 'terms'): void {
  const titleMap: Record<string, string> = {
    privacy: '隐私政策',
    terms: '用户协议',
  }
  uni.showModal({
    title: titleMap[type],
    content: `${titleMap[type]}内容将尽快上线。\n\n我们严格遵守相关法律法规，保护您的个人信息安全。`,
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

  // 用户协议
  &__agreement {
    @include flex-start;
    padding: $spacing-sm 0;
    margin-bottom: $spacing-sm;
    gap: $spacing-xs;
  }

  &__checkbox {
    flex-shrink: 0;
    padding: $spacing-xs;

    &--on {
      font-size: $font-size-lg;
      color: $color-primary;
    }

    &--off {
      font-size: $font-size-lg;
      color: $color-text-placeholder;
    }
  }

  &__agreement-text {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    line-height: 1.6;
  }

  &__link {
    color: $color-primary;
  }

  // 注册按钮
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

  // 底部链接
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
