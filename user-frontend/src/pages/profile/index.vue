<template>
  <!--
    我的页面 — 个人中心
    功能:
    - 微信头像昵称
    - 会员状态占位
    - 刷题统计数据
    - 菜单列表(收藏/记录/设置/反馈)
    - 个人信息编辑
    - 退出登录
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
      current-path="/pages/profile/index"
      :wrong-count="wrongCount"
      @change="handleTabChange"
    />

    <!-- 编辑昵称弹窗 -->
    <van-popup
      v-model:show="editPopupVisible"
      position="bottom"
      :round="true"
      close-on-click-overlay
    >
      <view class="edit-popup">
        <view class="edit-popup__header">
          <text class="edit-popup__title">编辑个人信息</text>
          <text class="edit-popup__cancel" @tap="editPopupVisible = false">取消</text>
        </view>
        <view class="edit-popup__body">
          <view class="edit-popup__field">
            <text class="edit-popup__label">昵称</text>
            <input
              v-model="editForm.nickname"
              class="edit-popup__input"
              placeholder="请输入昵称"
              maxlength="20"
            />
          </view>
        </view>
        <view class="edit-popup__footer">
          <van-button
            type="primary"
            block
            round
            :disabled="!editForm.nickname || editForm.nickname.trim() === ''"
            @click="handleSaveProfile"
          >
            保存
          </van-button>
        </view>
      </view>
    </van-popup>
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

/**
 * 编辑个人信息
 */
function handleEditProfile(): void {
  console.log('[Profile] 打开个人信息编辑弹窗')
  // 初始化表单值
  editForm.nickname = userStore.nickname !== '微信用户' ? userStore.nickname : ''
  editPopupVisible.value = true
}

/**
 * 保存个人信息
 */
async function handleSaveProfile(): Promise<void> {
  const nickname = editForm.nickname.trim()
  if (!nickname) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
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
 * 退出登录
 */
function handleLogout(): void {
  console.log('[Profile] 点击退出登录')
  uni.showModal({
    title: '确认退出',
    content: '退出登录后需要重新授权',
    success: async (res) => {
      if (res.confirm) {
        console.log('[Profile] 用户确认退出登录')
        try {
          // 调用store的logout方法（会调用后端接口并清除本地状态）
          await userStore.logout()
          uni.showToast({ title: '已退出登录', icon: 'success' })
          // 跳转到首页
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/index/index' })
          }, 500)
        } catch (err) {
          console.error('[Profile] 退出登录异常:', err)
          // 即使失败也强制跳转首页
          uni.reLaunch({ url: '/pages/index/index' })
        }
      } else {
        console.log('[Profile] 用户取消退出登录')
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

// 退出登录
.profile-logout {
  margin: $spacing-lg $spacing-base;
}

// 安全区域
.profile-safe-bottom {
  height: calc($tabbar-height + env(safe-area-inset-bottom) + $spacing-md);
}

// 编辑弹窗
.edit-popup {
  padding: $spacing-md;
  min-height: 300rpx;
}

.edit-popup__header {
  @include flex-between;
  margin-bottom: $spacing-lg;
}

.edit-popup__title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.edit-popup__cancel {
  font-size: $font-size-base;
  color: $color-text-secondary;
}

.edit-popup__body {
  margin-bottom: $spacing-lg;
}

.edit-popup__field {
  @include flex-start;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $color-border-light;
}

.edit-popup__label {
  font-size: $font-size-base;
  color: $color-text-primary;
  width: 80rpx;
}

.edit-popup__input {
  flex: 1;
  font-size: $font-size-base;
  color: $color-text-primary;
}

.edit-popup__footer {
  padding-top: $spacing-md;
}
</style>
