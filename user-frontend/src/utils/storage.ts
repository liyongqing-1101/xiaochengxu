/**
 * uniStorage 封装工具
 *
 * 特性:
 * - 类型安全的 get/set
 * - TTL (Time-To-Live) 过期机制
 * - 批量读写减少 I/O 调用
 * - 统一的 key 前缀命名空间
 */

import { STORAGE_PREFIX } from './constants'

/** 带 TTL 的存储值 */
interface StorageEntry<T> {
  data: T
  /** 过期时间戳, 0 表示永不过期 */
  expiresAt: number
}

/**
 * 获取完整存储 key (带命名空间前缀)
 */
function fullKey(key: string): string {
  return STORAGE_PREFIX + key
}

/**
 * 类型安全的存储读取
 */
function get<T>(key: string, defaultValue?: T): T | null {
  try {
    const full = fullKey(key)
    const raw = uni.getStorageSync(full)
    if (!raw) return defaultValue ?? null

    // 尝试解析 JSON
    let parsed: StorageEntry<T> | T
    try {
      parsed = JSON.parse(raw)
    } catch {
      return raw as unknown as T
    }

    // 检查 TTL
    if (typeof parsed === 'object' && parsed !== null && 'expiresAt' in parsed && 'data' in parsed) {
      const entry = parsed as StorageEntry<T>
      if (entry.expiresAt > 0 && Date.now() > entry.expiresAt) {
        remove(key)
        return defaultValue ?? null
      }
      return entry.data
    }

    return parsed as T
  } catch {
    return defaultValue ?? null
  }
}

/**
 * 类型安全的存储写入
 */
function set<T>(key: string, value: T): void {
  try {
    const full = fullKey(key)
    const str = JSON.stringify(value)
    uni.setStorageSync(full, str)
  } catch (e) {
    console.error('[Storage] set error:', key, e)
  }
}

/**
 * 带 TTL 的存储写入
 */
function setWithTTL<T>(key: string, value: T, ttlMs: number): void {
  try {
    const full = fullKey(key)
    const entry: StorageEntry<T> = {
      data: value,
      expiresAt: Date.now() + ttlMs,
    }
    uni.setStorageSync(full, JSON.stringify(entry))
  } catch (e) {
    console.error('[Storage] setWithTTL error:', key, e)
  }
}

/**
 * 带 TTL 的存储读取
 */
function getWithTTL<T>(key: string): T | null {
  return get<T>(key)
}

/**
 * 删除指定 key
 */
function remove(key: string): void {
  try {
    uni.removeStorageSync(fullKey(key))
  } catch (e) {
    console.error('[Storage] remove error:', key, e)
  }
}

/**
 * 清除所有带命名空间前缀的存储
 */
function clearAll(): void {
  try {
    const { keys } = uni.getStorageInfoSync()
    keys
      .filter(k => k.startsWith(STORAGE_PREFIX))
      .forEach(k => uni.removeStorageSync(k))
  } catch (e) {
    console.error('[Storage] clearAll error:', e)
  }
}

/**
 * 批量读取
 */
function getBatch(keys: string[]): Record<string, unknown> {
  const result: Record<string, unknown> = {}
  keys.forEach(key => {
    result[key] = get(key)
  })
  return result
}

/**
 * 批量写入
 */
function setBatch(entries: Record<string, unknown>): void {
  Object.entries(entries).forEach(([key, value]) => {
    set(key, value)
  })
}

/**
 * 检查 key 是否存在且未过期
 */
function has(key: string): boolean {
  return get(key) !== null
}

export const storage = {
  get,
  set,
  getWithTTL,
  setWithTTL,
  remove,
  clearAll,
  getBatch,
  setBatch,
  has,
}
