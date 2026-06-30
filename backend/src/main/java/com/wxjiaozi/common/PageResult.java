package com.wxjiaozi.common;

import java.util.List;

/**
 * 分页结果封装（MyBatis 原生版）
 */
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private boolean hasMore;

    public PageResult() {
    }

    /**
     * 构造分页结果
     * @param list 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param hasMore 是否有更多
     */
    public PageResult(List<T> list, long total, int page, int pageSize, boolean hasMore) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasMore = hasMore;
    }

    /**
     * 构造分页结果（自动计算hasMore）
     * @param list 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> list, long total, int page, int pageSize) {
        this(list, total, page, pageSize, (long) page * pageSize < total);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
