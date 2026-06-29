/**
 * 每日打卡逻辑
 */
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

export function useCheckIn() {
  const userStore = useUserStore()
  const checking = ref(false)

  async function doCheckIn(): Promise<boolean> {
    if (userStore.todayCheckedIn) {
      uni.showToast({ title: '今日已打卡', icon: 'none' })
      return false
    }

    checking.value = true
    const success = await userStore.checkIn()
    checking.value = false
    return success
  }

  return {
    todayCheckedIn: userStore.todayCheckedIn,
    streakDays: userStore.streakDays,
    checking,
    doCheckIn,
  }
}
