<template>
  <!--
    答题页面
    功能:
    - 顶部: 倒计时 + 进度 + 收藏按钮
    - 题干富文本展示
    - 根据 question.type 动态切换: 单选/多选/判断组件
    - 底部: 上一题/提交/下一题
    - 提交后展示答案解析面板
    - 错题自动存入错题本
    - 答题卡弹窗

    [EXTENSION-POINT] 新增题型: 添加新的题型组件并在 questionComponentMap 中注册
  -->
  <view class="answer-page">
    <!-- ===== 自定义导航栏 ===== -->
    <AppNavbar
      :title="subjectName || '答题'"
      :show-back="true"
      @back="handleBack"
    />

    <!-- ===== 顶部信息栏 ===== -->
    <view class="answer-topbar">
      <!-- 倒计时 -->
      <QuestionTimer :display="timerDisplay" :remaining="timerRemaining" />

      <!-- 进度 -->
      <view class="answer-topbar__progress">
        <text class="answer-topbar__progress-text">{{ currentNumber }} / {{ totalCount }}</text>
      </view>

      <!-- 收藏按钮 -->
      <view class="answer-topbar__collect" @tap="handleCollect">
        <text class="answer-topbar__collect-icon" :class="{ 'answer-topbar__collect-icon--active': currentQuestion?.collected }">
          {{ currentQuestion?.collected ? '⭐' : '☆' }}
        </text>
      </view>

      <!-- 答题卡按钮 -->
      <view class="answer-topbar__sheet-btn" @tap="showSheet = true">
        <text class="answer-topbar__sheet-text">答题卡</text>
      </view>
    </view>

    <!-- ===== 题目内容区 ===== -->
    <scroll-view
      scroll-y
      class="answer-content"
      :scroll-into-view="scrollIntoView"
      :enhanced="true"
      :show-scrollbar="false"
    >
      <view v-if="currentQuestion" id="question-area">
        <!-- 题干 -->
        <QuestionStem
          :stem="currentQuestion.stem"
          :question-type="currentQuestion.type"
          :difficulty="currentQuestion.difficulty"
        />

        <!-- 动态题型组件 -->
        <!-- [EXTENSION-POINT] 新增题型在此添加映射 -->
        <SingleChoice
          v-if="currentQuestion.type === 1"
          :options="currentQuestion.options"
          :selected-options="selectedOptions"
          :correct-answer="submittedCorrectAnswer"
          :submitted="isCurrentSubmitted"
          @select="handleSelectOption"
        />

        <MultiChoice
          v-else-if="currentQuestion.type === 2"
          :options="currentQuestion.options"
          :selected-options="selectedOptions"
          :correct-answer="submittedCorrectAnswer"
          :submitted="isCurrentSubmitted"
          @select="handleSelectOption"
        />

        <TrueFalse
          v-else-if="currentQuestion.type === 3"
          :options="currentQuestion.options"
          :selected-options="selectedOptions"
          :correct-answer="submittedCorrectAnswer"
          :submitted="isCurrentSubmitted"
          @select="handleSelectOption"
        />

        <!-- 答案解析面板(提交后显示) -->
        <ExplanationPanel
          :visible="showExplanation && submitResult !== null"
          :is-correct="submitResult?.isCorrect ?? false"
          :correct-answer="submitResult?.correctAnswer ?? []"
          :explanation="submitResult?.explanation ?? ''"
          :tags="currentQuestion.tags"
        />
      </view>

      <!-- 无题目 -->
      <AppEmpty v-if="!currentQuestion && !loading" title="暂无题目" />

      <!-- 加载中 -->
      <view v-if="loading" class="answer-loading">
        <text class="answer-loading__text">加载题目中...</text>
      </view>
    </scroll-view>

    <!-- ===== 底部操作栏 ===== -->
    <view class="answer-actions">
      <!-- 未提交: 显示上一题 + 提交 + 下一题 -->
      <template v-if="!isCurrentSubmitted">
        <view
          class="answer-actions__btn answer-actions__btn--outline"
          :class="{ 'answer-actions__btn--disabled': isFirstQuestion }"
          @tap="handlePrev"
        >
          <text>上一题</text>
        </view>

        <view
          class="answer-actions__btn answer-actions__btn--primary"
          :class="{ 'answer-actions__btn--disabled': selectedOptions.length === 0 }"
          @tap="handleSubmit"
        >
          <text>{{ submitting ? '提交中...' : '提交答案' }}</text>
        </view>

        <view
          class="answer-actions__btn answer-actions__btn--outline"
          :class="{ 'answer-actions__btn--disabled': isLastQuestion }"
          @tap="handleNext"
        >
          <text>下一题</text>
        </view>
      </template>

      <!-- 已提交: 显示上一题 + 下一题 -->
      <template v-else>
        <view
          class="answer-actions__btn answer-actions__btn--outline"
          :class="{ 'answer-actions__btn--disabled': isFirstQuestion }"
          @tap="handlePrev"
        >
          <text>上一题</text>
        </view>

        <view
          class="answer-actions__btn answer-actions__btn--primary"
          @tap="handleNext"
        >
          <text>{{ isLastQuestion ? '完成答题' : '下一题' }}</text>
        </view>
      </template>
    </view>

    <!-- ===== 答题卡弹窗 ===== -->
    <AnswerSheet
      :visible="showSheet"
      :status-map="answerStatusMap"
      :question-order="questionOrder"
      @close="showSheet = false"
      @jump="handleSheetJump"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useQuestionStore } from '@/stores/question'
import { useExamStore } from '@/stores/exam'
import { useTimer } from '@/composables/useTimer'
import type { SubmitResult } from '@/types/question'
import { CategoryId } from '@/types/enums'
import { TIMER } from '@/utils/constants'
import AppNavbar from '@components/common/AppNavbar.vue'
import AppEmpty from '@components/common/AppEmpty.vue'
import QuestionStem from '../components/QuestionStem.vue'
import QuestionTimer from '../components/QuestionTimer.vue'
import SingleChoice from '../components/SingleChoice.vue'
import MultiChoice from '../components/MultiChoice.vue'
import TrueFalse from '../components/TrueFalse.vue'
import AnswerSheet from '../components/AnswerSheet.vue'
import ExplanationPanel from '../components/ExplanationPanel.vue'

const questionStore = useQuestionStore()
const examStore = useExamStore()

const loading = ref(false)
const showSheet = ref(false)
const scrollIntoView = ref('')
const submitResult = ref<SubmitResult | null>(null)
const submittedCorrectAnswer = ref<string[]>([])
const subjectName = ref('')

// 随机模式 30 分钟，其他模式 45 分钟
const RANDOM_EXAM_SECONDS = 30 * 60

// 计时器
const { display: timerDisplay, remaining: timerRemaining, start: startTimer, stop: stopTimer, reset: resetTimer } =
  useTimer(TIMER.COUNTDOWN_SECONDS, () => {
    uni.showToast({ title: '答题时间到', icon: 'none' })
    handleEndSession()
  })

// 当前模式
const currentMode = ref('practice')

// 响应式计算
const currentQuestion = computed(() => questionStore.currentQuestion)
const currentNumber = computed(() => questionStore.currentNumber)
const totalCount = computed(() => questionStore.totalCount)
const isFirstQuestion = computed(() => questionStore.isFirstQuestion)
const isLastQuestion = computed(() => questionStore.isLastQuestion)
const selectedOptions = computed(() => questionStore.selectedOptions)
const isCurrentSubmitted = computed(() => questionStore.isCurrentSubmitted)
const showExplanation = computed(() => questionStore.showExplanation)
const answerStatusMap = computed(() => questionStore.answerStatusMap)
const submitting = computed(() => questionStore.submitting)

// 题目顺序(用于答题卡)
const questionOrder = computed(() =>
  questionStore.session?.questions.map(q => q.id) ?? [],
)

// 从页面参数获取配置
onMounted(async () => {
  const { subjectId, mode, chapterId, knowledgePointId, startFrom } = getPageParams()
  currentMode.value = (mode as string) || 'practice'

  loading.value = true
  try {
    // 构建请求参数
    const params: Record<string, any> = {
      categoryId: examStore.currentCategoryId as CategoryId,
      subjectId: Number(subjectId) || 0,
      chapterId: chapterId ? Number(chapterId) : undefined,
      knowledgePointId: knowledgePointId ? Number(knowledgePointId) : undefined,
      mode: currentMode.value,
    }

    // 随机模式：固定题型数量
    if (currentMode.value === 'random') {
      params.singleCount = 40
      params.multiCount = 20
      params.trueFalseCount = 20
    }

    // 顺序刷题模式：从指定位置开始
    if (currentMode.value === 'sequential' && startFrom) {
      params.startFrom = Number(startFrom)
    }

    await questionStore.startSession(params as any)

    const subject = examStore.subjects.find(s => s.id === Number(subjectId))
    subjectName.value = subject?.name || '答题'

    // 随机模式使用 30 分钟倒计时，其他模式 45 分钟
    const timerSeconds = currentMode.value === 'random' ? RANDOM_EXAM_SECONDS : TIMER.COUNTDOWN_SECONDS
    resetTimer(timerSeconds)
    startTimer()
  } catch (e) {
    console.error('加载答题会话失败:', e)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  stopTimer()
  // 保存答题进度
  questionStore.saveProgressToLocal()
})

// ═══════════════════════════════════════
// 事件处理
// ═══════════════════════════════════════

/** 获取页面参数 */
function getPageParams(): Record<string, string> {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  return currentPage?.$page?.options || currentPage?.options || {}
}

/** 选择选项 */
function handleSelectOption(optionId: string): void {
  questionStore.selectOption(optionId)
}

/** 提交答案 */
async function handleSubmit(): Promise<void> {
  const result = await questionStore.submitAnswer()
  if (result) {
    submitResult.value = result
    submittedCorrectAnswer.value = result.correctAnswer
    // 滚动到解析区
    scrollIntoView.value = 'question-area'
    setTimeout(() => { scrollIntoView.value = '' }, 100)
  }
}

/** 上一题 */
function handlePrev(): void {
  submitResult.value = null
  submittedCorrectAnswer.value = []
  questionStore.prevQuestion()
  scrollIntoView.value = 'question-area'
  setTimeout(() => { scrollIntoView.value = '' }, 100)
}

/** 下一题 / 完成 */
function handleNext(): void {
  if (isLastQuestion.value) {
    handleEndSession()
    return
  }
  submitResult.value = null
  submittedCorrectAnswer.value = []
  questionStore.nextQuestion()
  scrollIntoView.value = 'question-area'
  setTimeout(() => { scrollIntoView.value = '' }, 100)
}

/** 答题卡跳转 */
function handleSheetJump(questionId: number): void {
  const idx = questionStore.session?.questions.findIndex(q => q.id === questionId)
  if (idx !== undefined && idx >= 0) {
    submitResult.value = null
    submittedCorrectAnswer.value = []
    questionStore.jumpTo(idx)
    scrollIntoView.value = 'question-area'
    setTimeout(() => { scrollIntoView.value = '' }, 100)
  }
}

/** 收藏 */
async function handleCollect(): Promise<void> {
  await questionStore.toggleCollect()
}

/** 结束答题 */
async function handleEndSession(): Promise<void> {
  await questionStore.endSession()
  stopTimer()

  const { correct, total } = questionStore.progress

  if (currentMode.value === 'random') {
    // 随机试卷：总分 100 分
    // 单选 40 道 (各1分) + 多选 20 道 (各2分) + 判断 20 道 (各1分) = 100 分
    const sessionQuestions = questionStore.session?.questions || []
    let score = 0
    sessionQuestions.forEach(q => {
      const answer = questionStore.session?.answers[q.id]
      if (answer?.isCorrect) {
        score += q.type === 2 ? 2 : 1
      }
    })

    uni.showModal({
      title: '试卷提交完成',
      content: `得分: ${score} / 100 分\n正确率: ${total > 0 ? Math.round((correct / total) * 100) : 0}% (${correct}/${total})`,
      confirmText: '返回首页',
      cancelText: '查看错题',
      success: (res) => {
        if (res.confirm) {
          uni.switchTab({ url: '/pages/index/index' })
        } else {
          uni.navigateTo({ url: '/subpackages/wrong-book/pages/index' })
        }
        questionStore.reset()
      },
    })
  } else {
    const accuracy = total > 0 ? Math.round((correct / total) * 100) : 0
    uni.showModal({
      title: '答题完成',
      content: `正确率: ${accuracy}% (${correct}/${total})`,
      confirmText: '返回首页',
      cancelText: '查看错题',
      success: (res) => {
        if (res.confirm) {
          uni.switchTab({ url: '/pages/index/index' })
        } else {
          uni.navigateTo({ url: '/subpackages/wrong-book/pages/index' })
        }
        questionStore.reset()
      },
    })
  }
}

/** 返回 */
function handleBack(): void {
  if (questionStore.progress.answered > 0) {
    uni.showModal({
      title: '确认退出',
      content: '退出后答题进度将丢失，确定退出吗？',
      success: (res) => {
        if (res.confirm) {
          questionStore.reset()
          uni.navigateBack()
        }
      },
    })
  } else {
    questionStore.reset()
    uni.navigateBack()
  }
}
</script>

<style lang="scss" scoped>
.answer-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
}

// 顶部信息栏
.answer-topbar {
  @include flex-between;
  padding: $spacing-sm $spacing-base;
  background: $color-bg-white;
  border-bottom: 1rpx solid $color-border-light;
}

.answer-topbar__progress {
  &-text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }
}

.answer-topbar__collect {
  padding: $spacing-xs;

  &-icon {
    font-size: $font-size-xl;
    color: $color-text-placeholder;

    &--active {
      color: #FAAD14;
    }
  }
}

.answer-topbar__sheet-btn {
  padding: $spacing-xs $spacing-md;
  background: $color-primary-bg;
  border-radius: $radius-xl;
}

.answer-topbar__sheet-text {
  font-size: $font-size-xs;
  color: $color-primary;
}

// 题目内容区
.answer-content {
  flex: 1;
  overflow-y: auto;
}

.answer-loading {
  @include flex-center;
  padding: $spacing-xxl;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}

// 底部操作栏
.answer-actions {
  @include flex-between;
  padding: $spacing-sm $spacing-base;
  padding-bottom: calc($spacing-sm + env(safe-area-inset-bottom));
  background: $color-bg-white;
  border-top: 1rpx solid $color-border-light;
  gap: $spacing-sm;
}

.answer-actions__btn {
  flex: 1;
  height: 80rpx;
  border-radius: $radius-xl;
  @include flex-center;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  transition: all 0.2s;

  &--primary {
    background: $gradient-primary;
    color: #fff;
    box-shadow: 0 4rpx 12rpx rgba(74, 144, 217, 0.3);
  }

  &--outline {
    background: $color-bg-white;
    color: $color-primary;
    border: 2rpx solid $color-primary;
  }

  &--disabled {
    opacity: 0.4;
    pointer-events: none;
  }
}
</style>
