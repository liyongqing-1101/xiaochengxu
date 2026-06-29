<template>
  <!--
    学习页面
    功能:
    - 科目分类Tab切换
    - 章节列表 + 知识点筛选
    - 分页题目列表
    - 搜索功能
    - 按章节/知识点刷题入口

    [EXTENSION-POINT] 新增考试分类: 科目Tab根据 examStore.subjects 动态渲染
  -->
  <view class="study-page">
    <!-- 搜索栏 -->
    <view class="study-search">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索题目、知识点..."
        shape="round"
        background="#F5F7FA"
        @search="handleSearch"
      />
    </view>

    <!-- 科目Tab -->
    <view class="study-tabs">
      <scroll-view scroll-x :show-scrollbar="false">
        <view class="study-tabs__track">
          <view
            class="study-tabs__item"
            :class="{ 'study-tabs__item--active': currentSubjectId === null }"
            @tap="selectSubject(null)"
          >
            <text>全部</text>
          </view>
          <view
            v-for="subject in examStore.subjects"
            :key="subject.id"
            class="study-tabs__item"
            :class="{ 'study-tabs__item--active': currentSubjectId === subject.id }"
            @tap="selectSubject(subject.id)"
          >
            <text>{{ subject.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 内容区域 -->
    <scroll-view
      scroll-y
      class="study-content"
      @scrolltolower="handleLoadMore"
      :enhanced="true"
      :show-scrollbar="false"
    >
      <!-- 章节列表 (选择科目后显示) -->
      <view v-if="currentSubjectId && chapters.length > 0" class="study-section">
        <view class="study-section__header">
          <text class="study-section__title">章节列表</text>
        </view>
        <view
          v-for="chapter in chapters"
          :key="chapter.id"
          class="study-chapter"
          @tap="handleChapterClick(chapter)"
        >
          <view class="study-chapter__info">
            <text class="study-chapter__name">{{ chapter.name }}</text>
            <text class="study-chapter__count">{{ chapter.questionCount }}题</text>
          </view>
          <ProgressBar
            :current="chapter.completedCount || 0"
            :total="chapter.questionCount"
            :show-info="false"
            :height="6"
          />
        </view>
      </view>

      <!-- 题目列表 -->
      <view class="study-section">
        <view class="study-section__header">
          <text class="study-section__title">题目列表</text>
          <text v-if="questionTotal > 0" class="study-section__count">共 {{ questionTotal }} 题</text>
        </view>

        <view
          v-for="question in questionList"
          :key="question.id"
          class="study-question-item"
          @tap="handleQuestionClick(question)"
        >
          <QuestionCard
            :question="question"
            :show-options="false"
            :show-subject="false"
            :show-footer="true"
          />
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="study-loading">
          <text>加载中...</text>
        </view>

        <!-- 空状态 -->
        <AppEmpty
          v-if="questionList.length === 0 && !loadingMore"
          title="暂无题目"
          description="换个科目或关键词试试"
        />
      </view>

      <!-- 安全区域底部 -->
      <view class="study-safe-bottom" />
    </scroll-view>

    <!-- 底部 TabBar -->
    <CustomTabbar
      current-path="/pages/study/index"
      @change="handleTabChange"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useExamStore } from '@/stores/exam'
import { useUserStore } from '@/stores/user'
import type { Chapter, Subject } from '@/types/exam'
import type { Question } from '@/types/question'
import type { PaginatedParams } from '@/types/api'
import { CategoryId } from '@/types/enums'
import { PAGINATION } from '@/utils/constants'
import QuestionCard from '@components/question/QuestionCard.vue'
import ProgressBar from '@components/question/ProgressBar.vue'
import CustomTabbar from '@components/tabbar/CustomTabbar.vue'
import AppEmpty from '@components/common/AppEmpty.vue'

const examStore = useExamStore()

const searchKeyword = ref('')
const currentSubjectId = ref<number | null>(null)
const chapters = ref<Chapter[]>([])
const questionList = ref<Question[]>([])
const questionTotal = ref(0)
const currentPage = ref(1)
const loadingMore = ref(false)
const hasMore = ref(true)

// 加载章节
async function loadChapters(subjectId: number): Promise<void> {
  chapters.value = await examStore.fetchChapters(subjectId)
}

// 加载题目列表
async function loadQuestions(reset = false): Promise<void> {
  if (loadingMore.value) return
  if (!reset && !hasMore.value) return

  if (reset) {
    currentPage.value = 1
    questionList.value = []
    hasMore.value = true
  }

  loadingMore.value = true

  try {
    const { questionApi } = await import('@/api/modules/question')
    const params: PaginatedParams = {
      page: currentPage.value,
      pageSize: PAGINATION.DEFAULT_PAGE_SIZE,
      categoryId: examStore.currentCategoryId,
      subjectId: currentSubjectId.value ?? undefined,
      keyword: searchKeyword.value || undefined,
    }

    const result = await questionApi.getQuestionList(params)
    questionList.value = reset
      ? result.list
      : [...questionList.value, ...result.list]
    questionTotal.value = result.total
    hasMore.value = result.hasMore
    currentPage.value++
  } catch (e) {
    console.error('加载题目失败:', e)
  } finally {
    loadingMore.value = false
  }
}

// 选择科目
function selectSubject(subjectId: number | null): void {
  currentSubjectId.value = subjectId
  if (subjectId) {
    loadChapters(subjectId)
  } else {
    chapters.value = []
  }
  loadQuestions(true)
}

// 搜索
function handleSearch(): void {
  loadQuestions(true)
}

// 加载更多
function handleLoadMore(): void {
  loadQuestions()
}

// 章节点击 — 按章节刷题
function handleChapterClick(chapter: Chapter): void {
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${chapter.subjectId}&chapterId=${chapter.id}&mode=practice`,
  })
}

// 题目点击 — 进入答题
function handleQuestionClick(question: Question): void {
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${question.subjectId}&mode=practice`,
  })
}

// Tab 切换
function handleTabChange(path: string): void {
  uni.switchTab({ url: path })
}

onMounted(() => {
  loadQuestions(true)
})

onShow(() => {
  const userStore = useUserStore()
  userStore.restoreSession()
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' })
  }
})
</script>

<style lang="scss" scoped>
.study-page {
  min-height: 100vh;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
}

// 搜索
.study-search {
  background: $color-bg-page;
}

// 科目Tab
.study-tabs {
  background: $color-bg-white;
  padding: $spacing-sm 0;
  margin-bottom: $spacing-sm;

  &__track {
    @include flex-start;
    padding: 0 $spacing-base;
    gap: $spacing-sm;
  }

  &__item {
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-xl;
    background: $color-bg-page;
    font-size: $font-size-sm;
    color: $color-text-secondary;
    white-space: nowrap;
    transition: all 0.2s;

    &--active {
      background: $color-primary;
      color: #fff;
    }
  }
}

// 内容区
.study-content {
  flex: 1;
  overflow-y: auto;
}

// 分区
.study-section {
  margin-bottom: $spacing-sm;

  &__header {
    @include flex-between;
    padding: $spacing-sm $spacing-base;
  }

  &__title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__count {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

// 章节
.study-chapter {
  margin: 0 $spacing-base $spacing-sm;
  padding: $spacing-md;
  background: $color-bg-white;
  border-radius: $radius-base;
  box-shadow: $shadow-sm;

  &__info {
    @include flex-between;
    margin-bottom: $spacing-sm;
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

// 题目列表项
.study-question-item {
  // QuestionCard 自带 margin
}

// 加载
.study-loading {
  @include flex-center;
  padding: $spacing-md;
  font-size: $font-size-sm;
  color: $color-text-placeholder;
}

// 安全区域
.study-safe-bottom {
  height: calc($tabbar-height + env(safe-area-inset-bottom) + $spacing-md);
}
</style>
