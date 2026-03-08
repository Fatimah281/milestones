package DTO;

import java.util.List;

public class PagedResult {

    private final long totalCount;
    private final int offset;
    private final int limit;
    private final List<EmployeeDto> data;

    public PagedResult(long totalCount, int offset, int limit, List<EmployeeDto> data) {
        this.totalCount = totalCount;
        this.offset = offset;
        this.limit = limit;
        this.data = data != null ? data : List.of();
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public List<EmployeeDto> getData() {
        return data;
    }
}
