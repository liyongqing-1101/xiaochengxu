/**
 * 分页加载 composable
 */
import { ref } from 'vue'
import { PAGINATION } from '@/utils/constants'

export function usePagination<T>(
  fetchFn: (page: number, pageSize: number) => Promise<{ list: T[]; total: number; hasMore: boolean }>,
) {
  const list = ref<T[]>([])
  const page = ref(1)
  const pageSize = ref(PAGINATION.DEFAULT_PAGE_SIZE)
  const total = ref(0)
  const loading = ref(false)
  const hasMore = ref(true)
  const error = ref(false)

  async function loadMore(): Promise<void> {
    if (loading.value || !hasMore.value) return

    loading.value = true
    error.value = false

    try {
      const result = await fetchFn(page.value, pageSize.value)
      list.value = page.value === 1 ? result.list : [...list.value, ...result.list]
      total.value = result.total
      hasMore.value = result.hasMore
      page.value++
    } catch {
      error.value = true
    } finally {
      loading.value = false
    }
  }

  async function refresh(): Promise<void> {
    page.value = 1
    hasMore.value = true
    list.value = []
    await loadMore()
  }

  function reset(): void {
    page.value = 1
    hasMore.value = true
    list.value = []
    total.value = 0
    error.value = false
    loading.value = false
  }

  return {
    list,
    page,
    total,
    loading,
    hasMore,
    error,
    loadMore,
    refresh,
    reset,
  }
}
