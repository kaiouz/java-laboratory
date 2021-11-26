package io.github.kaiouz.douban;

public class Pageable {

    private final int page;

    private final int size;

    private final int start;

    private final int limit;

    private final int end;

    public Pageable(int page, int size) {
        this.page = page;
        this.size = size;
        this.start = (page - 1) * size;
        this.limit = size;
        this.end = this.start + this.limit - 1;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getStart() {
        return start;
    }

    public int getLimit() {
        return limit;
    }

    public int getEnd() {
        return end;
    }
}
