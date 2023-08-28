package community.boot;

import com.alibaba.fastjson.JSONObject;
import community.dao.elasticsearch.DiscussPostRepository;
import community.mapper.DiscussPostMapper;
import community.pojo.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 @author Alex
 @create 2023-04-18-10:59
 */
@SpringBootTest
public class ElasticSearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    // 增加一条数据
    @Test
    public void test1() {
        discussPostRepository.save(discussPostMapper.getDiscussPostById(115));
    }

    // 增加多条数据
    @Test
    public void test2() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0));
    }

    // 更新数据
    @Test
    public void test3() {
        DiscussPost post = discussPostMapper.getDiscussPostById(231);
        post.setContent("踩 踩 踩");
        discussPostRepository.save(post);
    }

    // 删除数据
    @Test
    public void test4() {
        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    // 查询1
    @Test
    public void test5() {
        Iterable<DiscussPost> all = discussPostRepository.findAll();
        for (DiscussPost post : all) {
            System.out.println(post);
        }
    }

    /**
     * 搜索功能1
     */
    @Test
    public void test6() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();

        // 得到命中的数据
        Page<DiscussPost> page = discussPostRepository.search(searchQuery);
        System.out.println(page.getTotalPages());
        System.out.println(page.getTotalElements());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    /**
     * 搜索功能！
     */
    @Test
    public void test7() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();


        // 得到命中数据类 SearchHits， 内含命中的总数 totalHits，命中的具体信息列表 searchHits
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        // 得到命中的具体信息列表 searchHits，列表的每一个元素都是一个SearchHit对象
        // 其中SearchHit对象的 content属性，即为查询到的对象（discussPost）,故通过设置其title/content，实现返回高亮文本数据
        // SearchHit对象的 highlightFields属性，存储map数据（key为搜索的字段名,这里是title/content, value为添加高亮标签后的title/content的内容）
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();

        // 设置接收返回数据集合
        List<DiscussPost> discussPosts = new ArrayList<>();
        // 遍历返回的内容进行处理
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            // 得到高亮显示的内容（可能在content中，也可能在title中）
            Map<String, List<String>> highLightFields = searchHit.getHighlightFields();

            // 将高亮的内容填充到content中
            searchHit.getContent().setTitle(highLightFields.get("title") == null ? searchHit.getContent().getTitle() : highLightFields.get("title").get(0));
            searchHit.getContent().setContent(highLightFields.get("content") == null ? searchHit.getContent().getContent() : highLightFields.get("content").get(0));

            // 把discusspost实体放到集合中
            discussPosts.add(searchHit.getContent());
        }
        System.out.println("命中的总数为："+discussPosts.size());
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

    }
}
