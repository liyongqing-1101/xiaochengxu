package com.wxjiaozi.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private boolean hasMore;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, int page, int pageSize, boolean hasMore) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasMore = hasMore;
    }

    public static <T> PageResult<T> of(IPage<T> mybatisPage) {
        long page = mybatisPage.getCurrent();
        long pageSize = mybatisPage.getSize();
        long total = mybatisPage.getTotal();
        boolean hasMore = page * pageSize < total;
        return new PageResult<>(
                mybatisPage.getRecords(),
                total,
                (int) page,
                (int) pageSize,
                hasMore
        );
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
