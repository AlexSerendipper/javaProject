package community.service;

import community.dao.elasticsearch.DiscussPostRepository;
import community.pojo.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 @author Alex
 @create 2023-04-20-10:17
 */
@Service
public class ElasticSearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    // 增
    public void saveDiscusspost(DiscussPost post){
        discussPostRepository.save(post);
    }

    // 删
    public void deleteDiscusspost(int id){
        discussPostRepository.deleteById(id);
    }

    // 搜索1：返回命中目标总数
    public int searchDiscusspostCount(String keyword){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))       // 设置关键词，以及搜索的字段
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))                   // 搜索结果按照type,score.createTime降序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withHighlightFields(                                                             // 设置高亮显示条件，为匹配到的数据前后加标签
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();


        Page<DiscussPost> page = discussPostRepository.search(searchQuery);    // 得到命中的分页page对象
        return (int)page.getTotalElements();
    }



    // 搜索2：返回高亮的discusspost数据
    public List<DiscussPost> searchDiscusspost(String keyword, int pageNum, int pageSize){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))       // 设置关键词，以及搜索的字段
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))                   // 搜索结果按照type,score.createTime降序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(pageNum, pageSize))                                   // 设置分页搜索条件，设置当前页 以及 每页显示多少条数据
                .withHighlightFields(                                                             // 设置高亮显示条件，为匹配到的数据前后加标签
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();


        // 得到命中数据类 SearchHits， 内含命中的总数 totalHits，命中的具体信息列表 searchHits
        // 命中的具体信息列表 searchHits 中的每一个元素都是一个 SearchHit 对象
        // SearchHit 对象的 content属性，即为查询到的对象（discussPost）
        // SearchHit对象的 highlightFields 属性，存储map数据（key为搜索的字段名,这里是title/content, value为添加高亮标签后的title/content的内容）
        // 故 通过设置 SearchHit.content.title/content，实现返回高亮文本数据
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        if(searchHits.isEmpty()){
            return null;
        }
        // 设置接收返回数据集合
        List<DiscussPost> discussPosts = new ArrayList<>();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            Map<String, List<String>> highLightFields = searchHit.getHighlightFields();
            // 用高亮数据替换原有数据
            searchHit.getContent().setTitle(highLightFields.get("title") == null ? searchHit.getContent().getTitle() : highLightFields.get("title").get(0));
            searchHit.getContent().setContent(highLightFields.get("content") == null ? searchHit.getContent().getContent() : highLightFields.get("content").get(0));
            // 把discusspost对象放到集合中
            discussPosts.add(searchHit.getContent());
        }
        return discussPosts;
//        System.out.println("命中的总数为：" + discussPosts.size());
//        for (DiscussPost discussPost : discussPosts) {
//            System.out.println(discussPost);  // 输出高亮显示数据的discusspost对象
//        }

    }
}
