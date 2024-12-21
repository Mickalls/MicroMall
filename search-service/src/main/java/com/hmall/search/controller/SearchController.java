package com.hmall.search.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final RestHighLevelClient client  = new RestHighLevelClient(RestClient.builder(
            HttpHost.create("http://localhost:9200")));

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) throws IOException {
        SearchRequest request = new SearchRequest("items");

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (StrUtil.isNotBlank(query.getKey())) {
            boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getCategory())) {
            boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        if (StrUtil.isNotBlank(query.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (query.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }
        if (query.getMinPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }

        request.source().query(boolQuery);

        request.source().from((query.getPageNo()-1) * query.getPageSize()).size(query.getPageSize());

        if (StrUtil.isNotBlank(query.getSortBy())) {
            request.source().sort(query.getSortBy(), query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
        } else {
            request.source().sort("updateTime", query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
        }

        // 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        return handleResponse(response, query);
    }

    private PageDTO<ItemDTO> handleResponse(SearchResponse response, ItemPageQuery query) {
        // 0. 存储es查询结果
        List<ItemDoc> itemDocList = new ArrayList<>();

        SearchHits searchHits = response.getHits();
        // 1.获取总条数
        long total = searchHits.getTotalHits().value;

        // 2.遍历结果数组
        SearchHit[] hits = searchHits.getHits();

        for (SearchHit hit : hits) {
            // 3.得到_source，也就是原始json文档
            String source = hit.getSourceAsString();
            // 4.反序列化并打印
            ItemDoc itemDoc = JSONUtil.toBean(source, ItemDoc.class);
            itemDocList.add(itemDoc);
        }

        // 5. 转换为ItemDTO集合
        List<ItemDTO> itemDTOS = BeanUtil.copyToList(itemDocList, ItemDTO.class);

        return new PageDTO<>(total, query.getPageNo().longValue(), itemDTOS);
    }
}
