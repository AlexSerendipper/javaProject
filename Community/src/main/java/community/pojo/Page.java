package community.pojo;

/**
 * 封装分页相关的信息.
 */
public class Page {
    // 当前页码, 默认为1
    private int pageNum = 1;

    // 每页显示的条数
    private int pageSize = 10;

    // 数据总数！(用于计算总页数)
    private int total;

    public Page(int pageNum, int pageSize,int total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    public int getPageNum() {
        return pageNum;
    }

    // 只能设置大于1的当前页码，如果设置当前页码小于1，则不会执行，故默认为1
    public void setPageNum(int pageNum) {
        if (pageNum >= 1) {
            this.pageNum = pageNum;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {

        if (pageSize >= 1 && pageSize <= 100) {
            this.pageSize = pageSize;
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int nums) {
        if (nums >= 0) {
            this.total = nums;
        }
    }


    /**
     * 获取当前页的起始行（即当前页第一条数据的索引，第一页第一条数据索引为0）
     * @return
     */
    public int getOffset() {
        // current * limit - limit
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getPages() {
        // rows / limit [+1]
        if (total % pageSize == 0) {
            return total / pageSize;
        } else {
            return total / pageSize + 1;
        }
    }

    /**
     * 获取起始页码
     *
     * @return
     */
    public int getFrom() {
        int from = pageNum - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     *
     * @return
     */
    public int getTo() {
        int to = pageNum + 2;
        int pages = getPages();
        return to > pages ? pages : to;
    }

}
