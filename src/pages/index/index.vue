<template>
  <!--
    首页 — 核心页面
    UI 参考「研岛」清新柔和风格
    布局:
      1. 顶部标题区(标题 + 倒计时 + 打卡天数)
      2. 搜索栏
      3. 功能横幅(横向滚动)
      4. 每日一题卡片
      5. 科目题库网格(2x2)
      6. 快捷功能入口(2列)
      7. 底部 4 宫格功能按钮
      8. 底部 TabBar

    [EXTENSION-POINT] 新增考试分类: 科目网格/每日一题/搜索范围
    自动根据 currentCategoryId 动态切换
  -->
  <view class="home-page">
    <!-- ========== 1. 顶部区域 ========== -->
    <view class="home-header">
      <!-- 状态栏占位 -->
      <view class="home-header__status" :style="{ height: statusBarHeight + 'px' }" />

      <!-- 标题行: 左侧标题 + 右侧倒计时/打卡 -->
      <view class="home-header__main">
        <view class="home-header__left">
          <text class="home-header__title">{{ categoryMeta.homeTitle }}</text>
          <text class="home-header__subtitle">{{ categoryMeta.description }}</text>
        </view>
        <view class="home-header__right">
          <view class="home-header__countdown">
            <text class="home-header__countdown-label">距考试</text>
            <text class="home-header__countdown-num">{{ countdownDays }}</text>
            <text class="home-header__countdown-label">天</text>
          </view>
          <view class="home-header__checkin" @tap="handleCheckIn">
            <text class="home-header__checkin-text">
              已打卡 {{ userStore.streakDays }} 天
            </text>
          </view>
        </view>
      </view>

      <!-- 微信胶囊占位 -->
      <view class="home-header__capsule-placeholder" />
    </view>

    <!-- ========== 2. 搜索栏 ========== -->
    <SearchBar @click="handleSearchClick" />

    <!-- ========== 3. 功能横幅 (横向滚动) ========== -->
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

    <!-- ========== 4. 每日一题卡片 ========== -->
    <DailyQuestionCard
      :daily-question="dailyQuestion"
      :answered="userStore.todayCheckedIn"
      @check-in="handleCheckIn"
      @go-answer="handleDailyQuestion"
    />

    <!-- ========== 5. 科目题库网格 (2x2) ========== -->
    <!-- [EXTENSION-POINT] subjects 来自当前分类, 新增分类自动渲染新科目 -->
    <view class="home-section">
      <view class="home-section__header">
        <text class="home-section__title">科目题库</text>
        <text class="home-section__more" @tap="handleMoreSubjects">全部 ›</text>
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
            <text class="home-subject-item__count">{{ subject.totalQuestions }}题</text>
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

    <!-- ========== 6. 快捷功能入口 (2列) ========== -->
    <view class="home-quick-entries">
      <view class="home-quick-entry" @tap="handleQuickEntry('past-exam')">
        <view class="home-quick-entry__icon" style="background: #EBF3FC;">
          <text class="home-quick-entry__emoji">📋</text>
        </view>
        <view class="home-quick-entry__info">
          <text class="home-quick-entry__title">历年真题</text>
          <text class="home-quick-entry__desc">历年考试真题汇总</text>
        </view>
      </view>
      <view class="home-quick-entry" @tap="handleQuickEntry('memory-card')">
        <view class="home-quick-entry__icon" style="background: #FFF7E6;">
          <text class="home-quick-entry__emoji">🃏</text>
        </view>
        <view class="home-quick-entry__info">
          <text class="home-quick-entry__title">记忆卡</text>
          <text class="home-quick-entry__desc">知识点快速记忆</text>
        </view>
      </view>
    </view>

    <!-- ========== 7. 底部 4 宫格功能按钮 ========== -->
    <view class="home-tool-grid">
      <view class="home-tool-item" @tap="handleToolClick('wrong-book')">
        <view class="home-tool-item__icon-wrap" style="background: #FFF1F0;">
          <text class="home-tool-item__emoji">📝</text>
        </view>
        <text class="home-tool-item__text">错题本</text>
        <view v-if="wrongCount > 0" class="home-tool-item__badge">{{ wrongCount > 99 ? '99+' : wrongCount }}</view>
      </view>
      <view class="home-tool-item" @tap="handleToolClick('random')">
        <view class="home-tool-item__icon-wrap" style="background: #EBF3FC;">
          <text class="home-tool-item__emoji">🎲</text>
        </view>
        <text class="home-tool-item__text">随机刷题</text>
      </view>
      <view class="home-tool-item" @tap="handleToolClick('question-list')">
        <view class="home-tool-item__icon-wrap" style="background: #F6FFED;">
          <text class="home-tool-item__emoji">📄</text>
        </view>
        <text class="home-tool-item__text">我的题单</text>
      </view>
      <view class="home-tool-item" @tap="handleToolClick('notes')">
        <view class="home-tool-item__icon-wrap" style="background: #FFF7E6;">
          <text class="home-tool-item__emoji">📒</text>
        </view>
        <text class="home-tool-item__text">我的笔记</text>
      </view>
    </view>

    <!-- 安全区域底部占位 -->
    <view class="home-safe-bottom" />

    <!-- ========== 8. 底部 TabBar ========== -->
    <CustomTabbar
      current-path="/pages/index/index"
      :wrong-count="wrongCount"
      @change="handleTabChange"
    />

    <!-- 分类切换弹窗(多分类时显示) -->
    <!-- [EXTENSION-POINT] 当有多个考试分类时自动显示切换入口 -->
    <CategorySwitch
      :visible="showCategorySwitch"
      :current-id="examStore.currentCategoryId"
      @close="showCategorySwitch = false"
      @select="handleCategorySwitch"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useExamStore } from '@/stores/exam'
import { useWrongBookStore } from '@/stores/wrongBook'
import { useCheckIn } from '@/composables/useCheckIn'
import type { Subject } from '@/types/exam'
import type { DailyQuestion } from '@/types/question'
import SearchBar from '@components/search/SearchBar.vue'
import DailyQuestionCard from '@components/question/DailyQuestionCard.vue'
import ProgressBar from '@components/question/ProgressBar.vue'
import CustomTabbar from '@components/tabbar/CustomTabbar.vue'
import CategorySwitch from '@components/category/CategorySwitch.vue'

const userStore = useUserStore()
const examStore = useExamStore()
const wrongBookStore = useWrongBookStore()
const { doCheckIn } = useCheckIn()

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = systemInfo.statusBarHeight || 20

// 分类元信息
const categoryMeta = computed(() => examStore.currentCategoryMeta)

// 倒计时天数
const countdownDays = ref(0)

// 每日一题
const dailyQuestion = ref<DailyQuestion | null>(null)

// 科目列表(最多显示4个)
const displaySubjects = computed<Subject[]>(() => {
  return examStore.subjects.slice(0, 4)
})

// 错题总数
const wrongCount = computed(() => wrongBookStore.totalCount)

// 分类切换弹窗
const showCategorySwitch = ref(false)

// 功能横幅数据
const banners = [
  { emoji: '🔥', text: '每日打卡赢积分', bg: 'linear-gradient(135deg, #FFF1F0, #FFE7E5)' },
  { emoji: '📚', text: '海量题库随时刷', bg: 'linear-gradient(135deg, #EBF3FC, #D6E8FA)' },
  { emoji: '🎯', text: '精准考点解析', bg: 'linear-gradient(135deg, #F6FFED, #E6F9D8)' },
  { emoji: '💡', text: '智能错题复习', bg: 'linear-gradient(135deg, #FFF7E6, #FFECD0)' },
]

// 科目图标映射
const subjectIcons: Record<number, string> = {
  1: '📖', 2: '📝', 3: '🎓', 4: '📚',
  5: '📋', 6: '📌', 7: '🔖', 8: '📑',
}

// 科目主题色
const subjectColors: Record<number, string> = {
  1: '#4A90D9', 2: '#52C41A', 3: '#FAAD14', 4: '#FF4D4F',
  5: '#722ED1', 6: '#13C2C2', 7: '#EB2F96', 8: '#FA8C16',
}

function subjectIcon(id: number): string {
  return subjectIcons[id] || '📚'
}

function subjectColor(id: number): string {
  return subjectColors[id] || '#4A90D9'
}

// ═══════════════════════════════════════
// 数据加载
// ═══════════════════════════════════════
async function loadPageData(): Promise<void> {
  try {
    // 并行加载分类和用户统计
    await Promise.all([
      examStore.fetchCategories(),
      userStore.fetchStats(),
    ])

    // 加载每日一题
    loadDailyQuestion()

    // 计算倒计时(假设考试日期为固定日期, 实际应从后端获取)
    // [EXTENSION-POINT] 不同考试分类有不同考试日期
    calculateCountdown()
  } catch (e) {
    console.error('首页数据加载失败:', e)
  }
}

async function loadDailyQuestion(): Promise<void> {
  try {
    const { questionApi } = await import('@/api/modules/question')
    dailyQuestion.value = await questionApi.getDailyQuestion(examStore.currentCategoryId)
  } catch {
    // 静默失败, 无每日一题也正常显示
  }
}

function calculateCountdown(): void {
  // [EXTENSION-POINT] 不同考试分类配置不同考试日期
  const examDates: Record<number, string> = {
    1: '2026-10-31', // 教资
    // 2: '2026-09-20', // 计算机二级
    // 3: '2026-11-14', // 软考
  }
  const examDate = examDates[examStore.currentCategoryId] || '2026-12-31'
  const target = new Date(examDate)
  const now = new Date()
  const diff = Math.max(0, Math.ceil((target.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)))
  countdownDays.value = diff
}

// ═══════════════════════════════════════
// 事件处理
// ═══════════════════════════════════════

/** 搜索 */
function handleSearchClick(): void {
  uni.navigateTo({ url: '/pages/study/index?focusSearch=1' })
}

/** 打卡 */
async function handleCheckIn(): Promise<void> {
  await doCheckIn()
}

/** 每日一题 */
function handleDailyQuestion(): void {
  if (!dailyQuestion.value?.question) return
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${dailyQuestion.value.question.subjectId}&mode=daily`,
  })
}

/** 横幅点击 */
function handleBannerClick(banner: { text: string }): void {
  uni.showToast({ title: banner.text, icon: 'none' })
}

/** 科目点击 — 进入答题 */
function handleSubjectClick(subject: Subject): void {
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${subject.id}&mode=practice`,
  })
}

/** 更多科目 — 跳转学习页 */
function handleMoreSubjects(): void {
  uni.switchTab({ url: '/pages/study/index' })
}

/** 快捷入口 */
function handleQuickEntry(type: string): void {
  // [EXTENSION-POINT] 快捷入口可扩展对应功能页面
  if (type === 'past-exam') {
    uni.navigateTo({
      url: `/pages/study/index?filter=past-exam`,
    })
  } else if (type === 'memory-card') {
    uni.showToast({ title: '记忆卡功能即将上线', icon: 'none' })
  }
}

/** 底部 4 宫格 */
function handleToolClick(type: string): void {
  switch (type) {
    case 'wrong-book':
      uni.navigateTo({ url: '/subpackages/wrong-book/pages/index' })
      break
    case 'random':
      // 随机刷题 — 进入答题页, mode=random
      uni.navigateTo({
        url: `/subpackages/answer/pages/index?subjectId=0&mode=random`,
      })
      break
    case 'question-list':
      uni.showToast({ title: '题单功能即将上线', icon: 'none' })
      break
    case 'notes':
      uni.showToast({ title: '笔记功能即将上线', icon: 'none' })
      break
  }
}

/** Tab 切换 */
function handleTabChange(path: string): void {
  uni.switchTab({ url: path })
}

/** 分类切换 */
function handleCategorySwitch(id: number): void {
  examStore.switchCategory(id)
  // 重新加载首页数据
  loadPageData()
}

// ═══════════════════════════════════════
// 生命周期
// ═══════════════════════════════════════
onMounted(() => {
  loadPageData()
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
// 3. 功能横幅
// ═══════════════════════════════════════
.home-banner {
  margin: 0 $spacing-base $spacing-md;

  &__scroll {
    width: 100%;
  }

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

  &__emoji {
    font-size: $font-size-md;
  }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-regular;
    white-space: nowrap;
  }
}

// ═══════════════════════════════════════
// 5. 科目题库网格
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

  &__more {
    font-size: $font-size-sm;
    color: $color-primary;
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

  &__emoji {
    font-size: 36rpx;
  }

  &__info {
    @include flex-between;
  }

  &__name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__count {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

// ═══════════════════════════════════════
// 6. 快捷功能入口
// ═══════════════════════════════════════
.home-quick-entries {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm;
  margin: 0 $spacing-base $spacing-md;
}

.home-quick-entry {
  @include card;
  @include flex-start;
  gap: $spacing-md;

  &__icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-base;
    @include flex-center;
    flex-shrink: 0;
  }

  &__emoji {
    font-size: 40rpx;
  }

  &__info {
    @include flex-column;
    gap: 4rpx;
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__desc {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    @include text-ellipsis(1);
  }
}

// ═══════════════════════════════════════
// 7. 底部 4 宫格
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

  &__emoji {
    font-size: 40rpx;
  }

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
// 安全区域
// ═══════════════════════════════════════
.home-safe-bottom {
  height: calc($tabbar-height + env(safe-area-inset-bottom) + $spacing-md);
}
</style>
