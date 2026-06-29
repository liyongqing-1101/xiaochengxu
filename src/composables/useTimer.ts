/**
 * 倒计时 composable
 */
import { ref, onUnmounted } from 'vue'
import { formatDuration } from '@/utils/format'

export function useTimer(totalSeconds: number, onEnd?: () => void) {
  const remaining = ref(totalSeconds)
  const isRunning = ref(false)
  const display = ref(formatDuration(totalSeconds))
  let timer: ReturnType<typeof setInterval> | null = null

  function start(): void {
    if (isRunning.value) return
    isRunning.value = true

    timer = setInterval(() => {
      remaining.value--
      display.value = formatDuration(remaining.value)

      if (remaining.value <= 0) {
        stop()
        onEnd?.()
      }
    }, 1000)
  }

  function stop(): void {
    isRunning.value = false
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  function reset(newTotal?: number): void {
    stop()
    if (newTotal !== undefined) {
      totalSeconds = newTotal
    }
    remaining.value = totalSeconds
    display.value = formatDuration(totalSeconds)
  }

  function pause(): void {
    stop()
  }

  function resume(): void {
    if (remaining.value > 0) {
      start()
    }
  }

  onUnmounted(() => {
    stop()
  })

  return {
    remaining,
    isRunning,
    display,
    start,
    stop,
    reset,
    pause,
    resume,
  }
}
