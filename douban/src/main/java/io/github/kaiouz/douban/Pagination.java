package io.github.kaiouz.douban;

import java.util.List;

public class Pagination<T> {

    private final int page;

    private final int size;

    private final int totalPage;

    private final int total;

    private final List<T> data;


    public Pagination(int page, int size, int total, List<T> data) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPage = (total + size - 1) / size;
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotal() {
        return total;
    }

    public List<T> getData() {
        return data;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public boolean hasPrePage() {
        return this.page > 1;
    }

    public Pageable prePage() {
        return new Pageable(this.page - 1, this.size);
    }

    public boolean hasNextPage() {
        return this.page < this.totalPage;
    }

    public Pageable nextPage() {
        return new Pageable(this.page + 1, this.size);
    }
}
