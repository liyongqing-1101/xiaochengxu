<template>
  <!--
    错题本页面
    功能:
    - 按科目筛选错题
    - 错题列表(错误次数、知识点标签)
    - 重做错题
    - 移除错题
    - 清空错题

    [EXTENSION-POINT] 筛选支持多考试分类
  -->
  <view class="wrong-book-page">
    <!-- 顶部统计 + 筛选 -->
    <view class="wrong-book-header">
      <view class="wrong-book-header__stats">
        <text class="wrong-book-header__count">错题数: {{ totalCount }}</text>
        <text v-if="filterSubjectId" class="wrong-book-header__filter">
          筛选: {{ currentFilterName }}
        </text>
      </view>

      <!-- 科目筛选 -->
      <view class="wrong-book-filter">
        <scroll-view scroll-x :show-scrollbar="false">
          <view class="wrong-book-filter__track">
            <view
              class="wrong-book-filter__item"
              :class="{ 'wrong-book-filter__item--active': filterSubjectId === null }"
              @tap="setFilter(null)"
            >
              <text>全部</text>
            </view>
            <view
              v-for="stat in subjectStats"
              :key="stat.subjectId"
              class="wrong-book-filter__item"
              :class="{ 'wrong-book-filter__item--active': filterSubjectId === stat.subjectId }"
              @tap="setFilter(stat.subjectId)"
            >
              <text>{{ stat.subjectName }} ({{ stat.count }})</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 操作栏 -->
      <view v-if="filteredList.length > 0" class="wrong-book-actions">
        <view class="wrong-book-actions__btn" @tap="handleRedoAll">
          <text>重做全部</text>
        </view>
        <view class="wrong-book-actions__btn wrong-book-actions__btn--danger" @tap="handleClear">
          <text>清空</text>
        </view>
      </view>
    </view>

    <!-- 错题列表 -->
    <scroll-view
      scroll-y
      class="wrong-book-content"
      :enhanced="true"
      :show-scrollbar="false"
    >
      <view
        v-for="item in filteredList"
        :key="item.question.id"
        class="wrong-book-item"
      >
        <QuestionCard
          :question="item.question"
          :show-options="true"
          :show-footer="true"
          :show-error-info="true"
          :error-count="item.errorCount"
          :last-wrong-answer="item.lastWrongAnswer.join(', ')"
        />
        <view class="wrong-book-item__actions">
          <view class="wrong-book-item__btn wrong-book-item__btn--redo" @tap="handleRedo(item)">
            <text>重做</text>
          </view>
          <view class="wrong-book-item__btn wrong-book-item__btn--remove" @tap="handleRemove(item.question.id)">
            <text>移除</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <AppEmpty
        v-if="filteredList.length === 0"
        title="暂无错题"
        description="继续刷题，答错的题目会自动加入错题本"
      >
        <view class="wrong-book-empty-btn" @tap="handleGoPractice">
          <text>去刷题</text>
        </view>
      </AppEmpty>

      <!-- 安全区域底部 -->
      <view class="wrong-book-safe-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useWrongBookStore } from '@/stores/wrongBook'
import type { WrongQuestion } from '@/types/question'
import QuestionCard from '@components/question/QuestionCard.vue'
import AppEmpty from '@components/common/AppEmpty.vue'

const wrongBookStore = useWrongBookStore()

const filteredList = computed(() => wrongBookStore.filteredList)
const totalCount = computed(() => wrongBookStore.totalCount)
const filterSubjectId = computed(() => wrongBookStore.filterSubjectId)
const subjectStats = computed(() => wrongBookStore.subjectStats)

const currentFilterName = computed(() => {
  if (!filterSubjectId.value) return ''
  const stat = subjectStats.value.find(s => s.subjectId === filterSubjectId.value)
  return stat?.subjectName || ''
})

function setFilter(subjectId: number | null): void {
  wrongBookStore.setFilter(subjectId)
}

/** 重做单题 */
function handleRedo(item: WrongQuestion): void {
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${item.question.subjectId}&mode=practice`,
  })
}

/** 重做全部筛选错题 */
function handleRedoAll(): void {
  const subjectId = filterSubjectId.value || filteredList.value[0]?.question.subjectId
  if (!subjectId) return
  uni.navigateTo({
    url: `/subpackages/answer/pages/index?subjectId=${subjectId}&mode=practice`,
  })
}

/** 移除错题 */
function handleRemove(questionId: number): void {
  uni.showModal({
    title: '确认移除',
    content: '将从错题本中移除该题目',
    success: (res) => {
      if (res.confirm) {
        wrongBookStore.removeWrong(questionId)
        uni.showToast({ title: '已移除', icon: 'success' })
      }
    },
  })
}

/** 清空错题 */
function handleClear(): void {
  uni.showModal({
    title: '确认清空',
    content: filterSubjectId.value ? '将清空当前科目的所有错题' : '将清空所有错题',
    success: (res) => {
      if (res.confirm) {
        if (filterSubjectId.value) {
          wrongBookStore.clearBySubject(filterSubjectId.value)
        } else {
          wrongBookStore.clearAll()
        }
        uni.showToast({ title: '已清空', icon: 'success' })
      }
    },
  })
}

/** 去刷题 */
function handleGoPractice(): void {
  uni.switchTab({ url: '/pages/study/index' })
}

onMounted(() => {
  wrongBookStore.loadFromStorage()
})
</script>

<style lang="scss" scoped>
.wrong-book-page {
  min-height: 100vh;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
}

// 头部
.wrong-book-header {
  background: $color-bg-white;
  padding: $spacing-md $spacing-base;
  border-bottom: 1rpx solid $color-border-light;
}

.wrong-book-header__stats {
  @include flex-start;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;
}

.wrong-book-header__count {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.wrong-book-header__filter {
  font-size: $font-size-sm;
  color: $color-primary;
}

// 筛选
.wrong-book-filter {
  margin-bottom: $spacing-sm;

  &__track {
    @include flex-start;
    gap: $spacing-sm;
  }

  &__item {
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-xl;
    background: $color-bg-page;
    font-size: $font-size-sm;
    color: $color-text-secondary;
    white-space: nowrap;

    &--active {
      background: $color-primary-bg;
      color: $color-primary;
    }
  }
}

// 操作栏
.wrong-book-actions {
  @include flex-between;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $color-border-light;
}

.wrong-book-actions__btn {
  flex: 1;
  height: 64rpx;
  border-radius: $radius-base;
  @include flex-center;
  font-size: $font-size-sm;
  background: $color-primary-bg;
  color: $color-primary;

  &--danger {
    background: $answer-wrong;
    color: $color-danger;
    margin-left: $spacing-sm;
  }
}

// 内容
.wrong-book-content {
  flex: 1;
  overflow-y: auto;
  padding-top: $spacing-sm;
}

.wrong-book-item {
  margin-bottom: $spacing-sm;

  &__actions {
    @include flex-between;
    padding: 0 $spacing-base;
    gap: $spacing-sm;
  }

  &__btn {
    flex: 1;
    height: 64rpx;
    border-radius: $radius-base;
    @include flex-center;
    font-size: $font-size-sm;

    &--redo {
      background: $color-primary-bg;
      color: $color-primary;
    }

    &--remove {
      background: $color-bg-gray;
      color: $color-text-secondary;
    }
  }
}

.wrong-book-empty-btn {
  padding: $spacing-sm $spacing-lg;
  background: $gradient-primary;
  border-radius: $radius-xl;
  color: #fff;
  font-size: $font-size-base;
}

.wrong-book-safe-bottom {
  height: $spacing-lg;
}
</style>
