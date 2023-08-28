package community.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来代替pageInfo插件，实现与返回的pageInfo完全相同的数据
 *
 @author Alex
 @create 2023-04-13-15:38
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
// @Slf4j
public class PageInfo {
    private Integer pageNum;
    private Integer total;
    private boolean hasPreviousPage;
    private boolean hasNextPage;
    private Integer prePage;
    private Integer nextPage;
    private Integer pages;
    private List navigatepageNums;

    public PageInfo(Page page,int navigatePages) {
        pageNum = page.getPageNum();
        total = page.getTotal();
        hasPreviousPage = page.getPageNum()!=1?true:false;
        hasNextPage = page.getPageNum()!=page.getPages()?true:false;
        prePage = page.getPageNum() - 1;
        nextPage = page.getPageNum() + 1;
        pages = page.getPages();
        navigatepageNums = getPageNums(page,navigatePages);
    }

    // 这个暂时就只支持奇数的分页吧！
    private List getPageNums(Page page,int navigatePages){
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = page.getFrom(); i <= page.getTo(); i++) {
            list.add(i);
        }
        return list;
    }
}
