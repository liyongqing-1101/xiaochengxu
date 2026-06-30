<template>
  <!--
    首页 — 高校教资题库
    基准界面: 顶部 → 功能栏 → 快捷横幅 → 每日一题 → 科目四宫格 → 快捷操作 → TabBar
    已移除: CategorySwitch 弹窗、RandomExamPopup 弹窗
  -->
  <view class="home-page">
    <!-- ========== 1. 顶部区域 ========== -->
    <view class="home-header">
      <view class="home-header__status" :style="{ height: statusBarHeight + 'px' }" />
      <view class="home-header__main">
        <view class="home-header__left">
          <text class="home-header__title">高校教资题库</text>
          <text class="home-header__subtitle">高校教师资格证考试</text>
        </view>
        <view class="home-header__right">
          <view class="home-header__countdown">
            <text class="home-header__countdown-label">距考试</text>
            <text class="home-header__countdown-num">{{ countdownDays }}</text>
            <text class="home-header__countdown-label">天</text>
          </view>
          <view class="home-header__checkin" @tap="handleCheckIn">
            <text class="home-header__checkin-text">已打卡 {{ userStore.streakDays }} 天</text>
          </view>
        </view>
      </view>
      <view class="home-header__capsule-placeholder" />
    </view>

    <!-- ========== 2. 考试类型切换栏 ========== -->
    <view class="home-category-bar">
      <view class="home-category-bar__left">
        <text class="home-category-bar__icon">📂</text>
        <text class="home-category-bar__text">{{ examStore.currentCategoryMeta.name }}</text>
      </view>
      <text class="home-category-bar__arrow">切换 ›</text>
    </view>

    <!-- ========== 3. 快捷功能区（横幅滚动） ========== -->
    <view class="home-banner">
      <scroll-view scroll-x class="home-banner__scroll" :show-scrollbar="false">
        <view class="home-banner__track">
          <view
            v-for="(banner, i) in banners"
            :key="i"
            class="home-banner__item"
            :style="{ background: banner.bg }"
            @tap="handleBannerClick(banner)"
          >
            <text class="home-banner__emoji">{{ banner.emoji }}</text>
            <text class="home-banner__text">{{ banner.text }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- ========== 每日一题卡片 ========== -->
    <DailyQuestionCard
      :daily-question="dailyQuestion"
      :answered="userStore.todayCheckedIn"
      @check-in="handleCheckIn"
      @go-answer="handleDailyQuestion"
    />

    <!-- ========== 4. 科目四宫格 ========== -->
    <view class="home-section">
      <view class="home-section__header">
        <text class="home-section__title">科目题库</text>
      </view>
      <view class="home-subject-grid">
        <view
          v-for="subject in displaySubjects"
          :key="subject.id"
          class="home-subject-item"
          @tap="handleSubjectClick(subject)"
        >
          <view class="home-subject-item__icon" :style="{ background: subjectColor(subject.id) }">
            <text class="home-subject-item__emoji">{{ subjectIcon(subject.id) }}</text>
          </view>
          <view class="home-subject-item__info">
            <text class="home-subject-item__name">{{ subject.name }}</text>
            <text class="home-subject-item__progress">
              {{ subject.completedQuestions || 0 }} / {{ subject.totalQuestions }}
            </text>
          </view>
          <ProgressBar
            :current="subject.completedQuestions || 0"
            :total="subject.totalQuestions"
            :show-info="false"
            :height="6"
            :color="subjectColor(subject.id)"
          />
        </view>
      </view>
    </view>

    <!-- ========== 5. 快捷操作栏 ========== -->
    <view class="home-tool-grid">
      <view class="home-tool-item" @tap="handleToolClick('wrong-book')">
        <view class="home-tool-item__icon-wrap" style="background: #FFF1F0;">
          <text class="home-tool-item__emoji">📝</text>
        </view>
        <text class="home-tool-item__text">错题本</text>
        <view v-if="wrongCount > 0" class="home-tool-item__badge">
          {{ wrongCount > 99 ? '99+' : wrongCount }}
        </view>
      </view>
      <view class="home-tool-item" @tap="handleToolClick('random')">
        <view class="home-tool-item__icon-wrap" style="background: #EBF3FC;">
          <text class="home-tool-item__emoji">🎲</text>
        </view>
        <text class="home-tool-item__text">随机刷题</text>
      </view>
      <view class="home-tool-item" @tap="handleToolClick('daily')">
        <view class="home-tool-item__icon-wrap" style="background: #F6FFED;">
          <text class="home-tool-item__emoji">📅</text>
        </view>
        <text class="home-tool-item__text">每日一题</text>
      </view>
      <view class="home-tool-item" @tap="handleToolClick('quick')">
        <view class="home-tool-item__icon-wrap" style="background: #FFF7E6;">
          <text class="home-tool-item__emoji">⚡</text>
        </view>
        <text class="home-tool-item__text">快速练习</text>
      </view>
    </view>

    <!-- 安全区域底部 -->
    <view class="home-safe-bottom" />

    <!-- ========== 随机刷题弹窗 ========== -->
    <RandomExamPopup
      :visible="showRandomPopup"
      :subjects="displaySubjects"
      :selected-id="randomSelectedSubjectId"
      @close="handleRandomPopupClose"
      @select="handleRandomSubjectSelect"
      @start="handleRandomStart"
    />

    <!-- ========== 6. 底部 TabBar ========== -->
    <CustomTabbar
      current-path="/pages/index/index"
      :wrong-count="wrongCount"
      @change="handleTabChange"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useExamStore } from '@/stores/exam'
import { useWrongBookStore } from '@/stores/wrongBook'
import { useCheckIn } from '@/composables/useCheckIn'
import type { Subject } from '@/types/exam'
import type { DailyQuestion } from '@/types/question'
import DailyQuestionCard from '@components/question/DailyQuestionCard.vue'
import ProgressBar from '@components/question/ProgressBar.vue'
import CustomTabbar from '@components/tabbar/CustomTabbar.vue'
import RandomExamPopup from '@components/exam/RandomExamPopup.vue'

const userStore = useUserStore()
const examStore = useExamStore()
const wrongBookStore = useWrongBookStore()
const { doCheckIn } = useCheckIn()

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight: number = systemInfo.statusBarHeight || 20

const countdownDays = ref<number>(0)
const dailyQuestion = ref<DailyQuestion | null>(null)

// ═══════════════════════════════════════
// 随机刷题弹窗状态
// ═══════════════════════════════════════
const showRandomPopup = ref(false)
const randomSelectedSubjectId = ref<number | null>(null)

// ═══════════════════════════════════════
// 4 个固定科目
// ═══════════════════════════════════════
const FIXED_SUBJECTS = [
  { id: 1, name: '高等教育学', icon: '📖', color: '#4A90D9' },
  { id: 2, name: '高等教育法规和政策', icon: '⚖️', color: '#52C41A' },
  { id: 3, name: '教师伦理学', icon: '🎓', color: '#FAAD14' },
  { id: 4, name: '大学心理学', icon: '🧠', color: '#FF4D4F' },
]

const displaySubjects = computed<Subject[]>(() => {
  return FIXED_SUBJECTS.map(fixed => {
    const serverSubject = examStore.subjects.find(s => s.name === fixed.name)
    return {
      id: serverSubject?.id || fixed.id,
      categoryId: examStore.currentCategoryId,
      name: fixed.name,
      icon: fixed.icon,
      totalQuestions: serverSubject?.totalQuestions || 0,
      completedQuestions: (serverSubject as any)?.completedQuestions || 0,
      sortOrder: 0,
      status: 1,
    } as Subject
  })
})

const wrongCount = computed<number>(() => wrongBookStore.totalCount)

const banners = [
  { emoji: '🔥', text: '每日打卡赢积分', bg: 'linear-gradient(135deg, #FFF1F0, #FFE7E5)' },
  { emoji: '📚', text: '海量题库随时刷', bg: 'linear-gradient(135deg, #EBF3FC, #D6E8FA)' },
  { emoji: '🎯', text: '精准考点解析', bg: 'linear-gradient(135deg, #F6FFED, #E6F9D8)' },
  { emoji: '💡', text: '智能错题复习', bg: 'linear-gradient(135deg, #FFF7E6, #FFECD0)' },
]

function subjectIcon(id: number): string {
  return FIXED_SUBJECTS.find(s => s.id === id)?.icon || '📚'
}
function subjectColor(id: number): string {
  return FIXED_SUBJECTS.find(s => s.id === id)?.color || '#4A90D9'
}

// ═══════════════════════════════════════
// 数据加载
// ═══════════════════════════════════════
async function loadPageData(): Promise<void> {
  try {
    await Promise.all([
      examStore.fetchCategories(),
      userStore.fetchStats(),
    ])
    loadDailyQuestion()
    calculateCountdown()
  } catch (e) {
    console.error('首页数据加载失败:', e)
  }
}

async function loadDailyQuestion(): Promise<void> {
  try {
    const { questionApi } = await import('@/api/modules/question')
    dailyQuestion.value = await questionApi.getDailyQuestion(examStore.currentCategoryId)
  } catch { /* 静默失败 */ }
}

function calculateCountdown(): void {
  const examDates: Record<number, string> = { 1: '2026-10-31' }
  const examDate = examDates[examStore.currentCategoryId] || '2026-12-31'
  const target = new Date(examDate)
  const now = new Date()
  const diff = Math.max(0, Math.ceil((target.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)))
  countdownDays.value = diff
}

// ═══════════════════════════════════════
// 事件处理
// ═══════════════════════════════════════
async function handleCheckIn(): Promise<void> {
  await doCheckIn()
}

function handleDailyQuestion(): void {
  if (!dailyQuestion.value?.question) return
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${dailyQuestion.value.question.subjectId}&mode=daily`,
  })
}

function handleBannerClick(banner: { text: string }): void {
  uni.showToast({ title: banner.text, icon: 'none' })
}

/** 科目点击 — 从已刷题的下一题开始顺序刷题 */
function handleSubjectClick(subject: Subject): void {
  const startFrom = (subject.completedQuestions || 0) + 1
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${subject.id}&mode=sequential&startFrom=${startFrom}`,
  })
}

function handleToolClick(type: string): void {
  switch (type) {
    case 'wrong-book':
      uni.navigateTo({ url: '/subpackages/wrong-book/pages/index' })
      break
    case 'random':
      // 弹出随机刷题弹窗，必须选择科目
      if (displaySubjects.value.length === 0) {
        uni.showToast({ title: '暂无可用科目', icon: 'none' })
        return
      }
      randomSelectedSubjectId.value = null
      showRandomPopup.value = true
      break
    case 'daily':
      handleDailyQuestion()
      break
    case 'quick':
      uni.showToast({ title: '快速练习即将上线', icon: 'none' })
      break
  }
}

function handleTabChange(path: string): void {
  uni.switchTab({ url: path })
}

/** 随机刷题弹窗 — 科目选择（单选互斥） */
function handleRandomSubjectSelect(id: number | null): void {
  randomSelectedSubjectId.value = id
}

/** 随机刷题弹窗 — 关闭 */
function handleRandomPopupClose(): void {
  showRandomPopup.value = false
}

/** 随机刷题弹窗 — 开始刷题 */
function handleRandomStart(subjectId: number | null): void {
  showRandomPopup.value = false

  if (subjectId) {
    // 选中科目：跳转该科目刷题
    uni.navigateTo({
      url: `/subpackages/answer/pages/index?subjectId=${subjectId}&mode=random`,
    })
  } else {
    // 未选中科目：跳转全科目随机刷题
    uni.navigateTo({
      url: '/subpackages/answer/pages/index?mode=random',
    })
  }
}

onMounted(() => {
  loadPageData()
})

onShow(() => {
  userStore.restoreSession()
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: $color-bg-page;
}

// ═══════════════════════════════════════
// 1. 顶部区域
// ═══════════════════════════════════════
.home-header {
  position: relative;
  background: linear-gradient(180deg, #EBF3FC 0%, $color-bg-page 100%);
  padding: 0 $spacing-base $spacing-md;

  &__main {
    @include flex-between;
    padding-top: $spacing-sm;
  }

  &__left {
    @include flex-column;
  }

  &__title {
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    line-height: $line-height-tight;
  }

  &__subtitle {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 4rpx;
  }

  &__right {
    @include flex-start;
    gap: $spacing-md;
  }

  &__countdown {
    @include flex-column-center;
    background: rgba(255, 255, 255, 0.9);
    border-radius: $radius-base;
    padding: $spacing-xs $spacing-md;
    box-shadow: $shadow-sm;
  }

  &__countdown-label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }

  &__countdown-num {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-primary;
    line-height: $line-height-tight;
  }

  &__checkin {
    background: $gradient-primary;
    border-radius: $radius-xl;
    padding: $spacing-xs $spacing-md;
    box-shadow: 0 4rpx 12rpx rgba(74, 144, 217, 0.3);
  }

  &__checkin-text {
    font-size: $font-size-xs;
    color: #fff;
    font-weight: $font-weight-medium;
  }

  &__capsule-placeholder {
    position: absolute;
    top: calc(v-bind('statusBarHeight + "px"') + 6px);
    right: 16px;
    width: 87px;
    height: 32px;
  }
}

// ═══════════════════════════════════════
// 2. 考试类型切换栏
// ═══════════════════════════════════════
.home-category-bar {
  @include flex-between;
  margin: 0 $spacing-base $spacing-md;
  padding: $spacing-md $spacing-base;
  background: $color-bg-white;
  border-radius: $radius-base;
  box-shadow: $shadow-sm;

  &__left {
    @include flex-start;
    gap: $spacing-sm;
  }

  &__icon {
    font-size: $font-size-lg;
  }

  &__text {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__arrow {
    font-size: $font-size-sm;
    color: $color-primary;
  }
}

// ═══════════════════════════════════════
// 3. 快捷功能横幅
// ═══════════════════════════════════════
.home-banner {
  margin: 0 $spacing-base $spacing-md;

  &__scroll { width: 100%; }

  &__track {
    @include flex-start;
    gap: $spacing-sm;
    padding: $spacing-xs 0;
  }

  &__item {
    @include flex-center;
    gap: $spacing-xs;
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-xl;
    flex-shrink: 0;
    box-shadow: $shadow-sm;
  }

  &__emoji { font-size: $font-size-md; }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-regular;
    white-space: nowrap;
  }
}

// ═══════════════════════════════════════
// 4. 科目题库四宫格
// ═══════════════════════════════════════
.home-section {
  margin: 0 $spacing-base $spacing-md;

  &__header {
    @include flex-between;
    margin-bottom: $spacing-sm;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }
}

.home-subject-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm;
}

.home-subject-item {
  @include card;
  @include flex-column;
  gap: $spacing-sm;

  &__icon {
    width: 72rpx;
    height: 72rpx;
    border-radius: $radius-base;
    @include flex-center;
  }

  &__emoji { font-size: 36rpx; }

  &__info { @include flex-between; }

  &__name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    @include text-ellipsis(1);
    flex: 1;
    margin-right: $spacing-sm;
  }

  &__progress {
    font-size: $font-size-xs;
    color: $color-primary;
    font-weight: $font-weight-medium;
    white-space: nowrap;
  }
}

// ═══════════════════════════════════════
// 5. 快捷操作栏
// ═══════════════════════════════════════
.home-tool-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-sm;
  margin: 0 $spacing-base;
  padding: $spacing-md;
  background: $color-bg-white;
  border-radius: $radius-base;
  box-shadow: $shadow-sm;
}

.home-tool-item {
  @include flex-column-center;
  gap: $spacing-xs;
  position: relative;

  &__icon-wrap {
    width: 88rpx;
    height: 88rpx;
    border-radius: $radius-md;
    @include flex-center;
  }

  &__emoji { font-size: 40rpx; }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-regular;
  }

  &__badge {
    position: absolute;
    top: -4rpx;
    right: 16rpx;
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background: $color-danger;
    border-radius: 16rpx;
    font-size: 18rpx;
    color: #fff;
    @include flex-center;
  }
}

// ═══════════════════════════════════════
// 安全区域底部
// ═══════════════════════════════════════
.home-safe-bottom {
  height: calc($tabbar-height + env(safe-area-inset-bottom) + $spacing-md);
}
</style>
