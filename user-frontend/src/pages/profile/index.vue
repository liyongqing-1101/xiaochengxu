<template>
  <!--
    我的页面 — 个人中心
    功能:
    - 微信头像昵称（点击编辑昵称）
    - 会员状态占位
    - 刷题统计数据
    - 快捷入口(错题本/收藏/记录/打卡)
    - 菜单列表(收藏/记录/设置/反馈)
    - 退出登录（二次确认）
    - 个人信息编辑弹窗（居中）
    说明: 全部使用原生 view + CSS，不依赖 vant 组件，避免小程序组件未注册导致样式错乱
  -->
  <view class="profile-page">
    <!-- 顶部背景 -->
    <view class="profile-header-bg" />

    <!-- 用户信息卡片 -->
    <view class="profile-user-card">
      <view class="profile-user-card__top" @tap="handleEditProfile">
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

    <!-- 菜单列表（原生 cell 列表） -->
    <view class="profile-section">
      <view class="menu-list">
        <view class="menu-cell" @tap="handleNavigate('/subpackages/profile/pages/favorites')">
          <text class="menu-cell__icon">⭐</text>
          <text class="menu-cell__title">我的收藏</text>
          <text class="menu-cell__arrow">›</text>
        </view>
        <view class="menu-cell" @tap="handleNavigate('/subpackages/profile/pages/history')">
          <text class="menu-cell__icon">📋</text>
          <text class="menu-cell__title">练习记录</text>
          <text class="menu-cell__arrow">›</text>
        </view>
        <view class="menu-cell" @tap="handleNavigate('/subpackages/profile/pages/settings')">
          <text class="menu-cell__icon">⚙️</text>
          <text class="menu-cell__title">设置</text>
          <text class="menu-cell__arrow">›</text>
        </view>
        <view class="menu-cell" @tap="handleFeedback">
          <text class="menu-cell__icon">💬</text>
          <text class="menu-cell__title">意见反馈</text>
          <text class="menu-cell__arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录（原生按钮） -->
    <view class="profile-logout">
      <view class="logout-btn" @tap="handleLogout">
        <text class="logout-btn__text">退出登录</text>
      </view>
    </view>

    <!-- 安全区域底部 -->
    <view class="profile-safe-bottom" />

    <!-- 底部 TabBar -->
    <CustomTabbar
      current-path="/pages/profile/index"
      :wrong-count="wrongCount"
      @change="handleTabChange"
    />

    <!-- 编辑昵称弹窗（原生居中对话框） -->
    <view v-if="editPopupVisible" class="edit-mask" @tap="closeEditPopup">
      <view class="edit-dialog" @tap.stop>
        <view class="edit-dialog__header">
          <text class="edit-dialog__title">编辑个人信息</text>
        </view>
        <view class="edit-dialog__body">
          <view class="edit-dialog__field">
            <text class="edit-dialog__label">昵称</text>
            <input
              v-model="editForm.nickname"
              class="edit-dialog__input"
              placeholder="请输入昵称"
              placeholder-class="edit-dialog__placeholder"
              maxlength="20"
            />
          </view>
          <text class="edit-dialog__tip">昵称不能为空，不能全为空格</text>
        </view>
        <view class="edit-dialog__footer">
          <view class="edit-dialog__btn edit-dialog__btn--plain" @tap="closeEditPopup">
            <text>取消</text>
          </view>
          <view
            class="edit-dialog__btn edit-dialog__btn--primary"
            :class="{ 'edit-dialog__btn--disabled': !canSave }"
            @tap="handleSaveProfile"
          >
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useWrongBookStore } from '@/stores/wrongBook'
import { useCheckIn } from '@/composables/useCheckIn'
import CustomTabbar from '@components/tabbar/CustomTabbar.vue'
import { userApi } from '@/api/modules/user'

const userStore = useUserStore()
const wrongBookStore = useWrongBookStore()
const { doCheckIn } = useCheckIn()

const wrongCount = computed(() => wrongBookStore.totalCount)
const accuracyPercent = computed(() => {
  const acc = userStore.accuracy
  return acc > 0 ? `${(acc * 100).toFixed(1)}%` : '0%'
})

// 编辑弹窗状态
const editPopupVisible = ref(false)
const editForm = reactive({
  nickname: ''
})

// 保存按钮可用：非空且去空格后非空
const canSave = computed(
  () => !!editForm.nickname && editForm.nickname.trim() !== ''
)

/**
 * 跳转到指定页面
 */
function handleNavigate(url: string): void {
  console.log('[Profile] 页面跳转:', url)
  uni.navigateTo({ url })
}

/**
 * TabBar切换
 */
function handleTabChange(path: string): void {
  console.log('[Profile] TabBar切换:', path)
  uni.switchTab({ url: path })
}

/**
 * 每日打卡
 */
async function handleCheckIn(): Promise<void> {
  console.log('[Profile] 开始每日打卡')
  await doCheckIn()
}

/**
 * 意见反馈
 */
function handleFeedback(): void {
  console.log('[Profile] 打开意见反馈')
  uni.showModal({
    title: '意见反馈',
    editable: true,
    placeholderText: '请输入您的反馈意见...',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          console.log('[Profile] 提交反馈:', res.content)
          await userApi.submitFeedback(res.content)
          uni.showToast({ title: '感谢反馈!', icon: 'success' })
        } catch {
          uni.showToast({ title: '提交失败', icon: 'none' })
        }
      }
    },
  })
}

/**
 * 打开个人信息编辑弹窗
 */
function handleEditProfile(): void {
  console.log('[Profile] 打开个人信息编辑弹窗')
  // 初始化表单值（去除首尾空格）
  const current = userStore.nickname
  editForm.nickname = current && current !== '微信用户' ? current.trim() : ''
  editPopupVisible.value = true
}

/**
 * 关闭编辑弹窗
 */
function closeEditPopup(): void {
  editPopupVisible.value = false
}

/**
 * 保存个人信息（非空 + 去空格校验）
 */
async function handleSaveProfile(): Promise<void> {
  // 不可用时忽略（防重复点击）
  if (!canSave.value) return

  // 非空 + 去空格校验
  const nickname = editForm.nickname.trim()
  if (!nickname) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
    return
  }
  // 回填去除首尾空格后的值
  editForm.nickname = nickname

  // 昵称未变化则直接关闭
  const currentNick = userStore.userInfo?.nickname || ''
  if (nickname === currentNick) {
    editPopupVisible.value = false
    return
  }

  console.log('[Profile] 保存用户昵称:', nickname)
  try {
    await userApi.updateNickname(nickname)
    // 更新本地store
    await userStore.fetchUserInfo()
    editPopupVisible.value = false
    uni.showToast({ title: '修改成功', icon: 'success' })
  } catch (err) {
    console.error('[Profile] 修改昵称失败:', err)
    uni.showToast({ title: '修改失败，请重试', icon: 'none' })
  }
}

/**
 * 退出登录（二次确认）
 */
function handleLogout(): void {
  console.log('[Profile] 点击退出登录，弹出二次确认')
  uni.showModal({
    title: '退出登录',
    content: '确认退出当前账号？退出后需要重新登录',
    confirmText: '确认退出',
    cancelText: '再想想',
    confirmColor: '#ee0a24',
    success: async (res) => {
      if (!res.confirm) {
        console.log('[Profile] 用户取消退出登录')
        return
      }
      console.log('[Profile] 用户确认退出登录')
      uni.showLoading({ title: '退出中...', mask: true })
      try {
        // 调用store的logout方法（会调用后端接口将token加黑名单，并清除本地状态）
        await userStore.logout()
        uni.hideLoading()
        uni.showToast({ title: '已退出登录', icon: 'success' })
        // 跳转到首页
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 500)
      } catch (err) {
        console.error('[Profile] 退出登录异常:', err)
        uni.hideLoading()
        // store.logout 内部已 try/finally 保证清除本地状态，此处兜底直接跳首页
        uni.reLaunch({ url: '/pages/index/index' })
      }
    },
  })
}

/**
 * 页面加载时获取数据
 */
onMounted(() => {
  console.log('[Profile] onMounted - 加载用户统计数据')
  userStore.fetchStats()
  wrongBookStore.loadFromStorage()
})

/**
 * 每次显示页面时刷新用户信息和统计数据
 */
onShow(() => {
  console.log('[Profile] onShow - 恢复会话并刷新用户信息')
  userStore.restoreSession()

  // 检查登录状态，未登录跳转到登录页
  if (!userStore.isLoggedIn) {
    console.log('[Profile] 用户未登录，跳转到登录页')
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  // 已登录则刷新用户信息和统计数据
  console.log('[Profile] 刷新用户信息和统计数据')
  userStore.fetchUserInfo()
  userStore.fetchStats()
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

// 菜单列表（原生 cell）
.menu-list {
  background: $color-bg-white;
  border-radius: $radius-md;
  overflow: hidden;
}

.menu-cell {
  display: flex;
  align-items: center;
  padding: $spacing-md $spacing-base;
  border-bottom: 1rpx solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.menu-cell__icon {
  font-size: 36rpx;
  margin-right: $spacing-sm;
}

.menu-cell__title {
  flex: 1;
  font-size: $font-size-base;
  color: $color-text-primary;
}

.menu-cell__arrow {
  font-size: $font-size-lg;
  color: $color-text-placeholder;
}

// 退出登录（原生按钮）
.profile-logout {
  margin: $spacing-lg $spacing-base;
}

.logout-btn {
  height: 88rpx;
  background: $color-bg-white;
  border-radius: $radius-md;
  border: 1rpx solid $color-border-light;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn__text {
  font-size: $font-size-base;
  color: $color-danger;
}

// 安全区域
.profile-safe-bottom {
  height: calc($tabbar-height + env(safe-area-inset-bottom) + $spacing-md);
}

// 编辑昵称弹窗（原生居中对话框）
.edit-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-dialog {
  width: 580rpx;
  background: $color-bg-white;
  border-radius: $radius-md;
  padding: $spacing-lg $spacing-md $spacing-md;
  box-sizing: border-box;
}

.edit-dialog__header {
  text-align: center;
  margin-bottom: $spacing-md;
}

.edit-dialog__title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.edit-dialog__body {
  margin-bottom: $spacing-lg;
}

.edit-dialog__field {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-sm 0;
}

.edit-dialog__label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.edit-dialog__input {
  width: 100%;
  height: 80rpx;
  font-size: $font-size-base;
  color: $color-text-primary;
  background: $color-bg-page;
  border-radius: $radius-sm;
  padding: 0 $spacing-sm;
  box-sizing: border-box;
}

.edit-dialog__placeholder {
  color: $color-text-placeholder;
}

.edit-dialog__tip {
  display: block;
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  margin-top: $spacing-xs;
}

.edit-dialog__footer {
  display: flex;
  gap: $spacing-sm;
}

.edit-dialog__btn {
  flex: 1;
  height: 80rpx;
  border-radius: $radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-base;
}

.edit-dialog__btn--plain {
  background: $color-bg-page;
  color: $color-text-secondary;
}

.edit-dialog__btn--primary {
  background: $color-primary;
  color: #fff;
}

.edit-dialog__btn--disabled {
  background: #c9cdd4;
  color: #86909c;
}
</style>
