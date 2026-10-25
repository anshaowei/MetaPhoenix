package net.csibio.metaphoenix.client.domain.query;

import lombok.Data;
import lombok.experimental.Accessors;
import net.csibio.aird.constant.SymbolConst;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PageQuery implements Serializable {

    private static final long serialVersionUID = -8745138167696978267L;

    public static final int DEFAULT_PAGE_SIZE = 40;
    public static final String DEFAULT_SORT_COLUMN = "createDate";

    protected long current = 1;
    protected int pageSize = DEFAULT_PAGE_SIZE;
    protected long start = 0;
    //Sort.Direction.DESC
    protected Sort.Direction orderBy = null;
    protected String sortColumn = null;
    protected long total = 0;
    protected String sorter;
    protected long totalPage = 0;
    //是否使用estimateCount, 默认为false,即使用正常的count方法
    protected Boolean estimateCount = false;

    protected PageQuery() {
    }

    public PageQuery(int current, int pageSize) {
        this.current = current;
        this.pageSize = pageSize;
    }

    public PageQuery(int current, int pageSize, Sort.Direction direction, String sortColumn) {
        this.current = current;
        this.pageSize = pageSize;
        this.sortColumn = sortColumn;
        this.orderBy = direction;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(final int current) {
        this.current = current;

        if (current < 1) {
            this.current = 1;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getFirst() {
        return (getCurrent() > 0 && getPageSize() > 0) ? ((getCurrent() - 1) * getPageSize()) : 0;
        /*
         * maysql=Integer((pageNo - 1) * pageSize + 0); oralce=Integer((pageNo -
         * 1) * pageSize + 1);
         */
    }

    public Long getRowStart() {
        return getFirst();
    }

    public Long getLast() {
        return (getFirst() + getPageSize() - 1);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getStart() {
        this.start = (this.current - 1) * this.pageSize;
        return start;
    }

    public long getTotalPage() {
        if (this.pageSize > 0 && this.total > 0) {
            return (this.total % this.pageSize == 0 ? (this.total / this.pageSize) : (this.total / this.pageSize + 1));
        } else {
            return 0;
        }
    }

    public Sort.Direction getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Sort.Direction orderBy) {
        this.orderBy = orderBy;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public void setSorter(String sorter) {
        String[] sorters = sorter.split("/");
        if (sorters.length != 2) {
            return;
        }
        if (sorters[1].equals("ascend")) {
            this.setOrderBy(Sort.Direction.ASC);
        } else {
            this.setOrderBy(Sort.Direction.DESC);
        }

        if (!sorters[0].contains(SymbolConst.COMMA)) {
            this.sortColumn = sorters[0];
        } else {
            this.sortColumn = sorters[0].replace(SymbolConst.COMMA, SymbolConst.DOT);
        }
    }
}
