package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsElasticsearchRepository;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import jdk.nashorn.internal.runtime.regexp.joni.Option;
import org.apache.lucene.search.join.ScoreMode;
import org.aspectj.weaver.ast.Var;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
   private ProductFeignClient productFeignClient;
    @Autowired
    GoodsElasticsearchRepository goodsElasticsearchRepository;
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<JSONObject> getBaseCategoryList() {
        return productFeignClient.getBaseCategoryList();
    }

    @Override
    public void cancelSale(Long skuId) {
        //自定义REpository 继承 ElasticsearchRepository (类似于baseMapper)
        goodsElasticsearchRepository.deleteById(skuId);

    }

    @Override
    public void onSale(Long skuId) {
        //填满goods 用goodsElasticsearchRepository
        Goods goods = new Goods();

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 属性数据
        List<SearchAttr> searchAttrs = productFeignClient.getSearchAttrList(skuId);

        // 商标数据
        BaseTrademark baseTrademark = productFeignClient.getTrademarkById(skuInfo.getTmId());

        goods.setTitle(skuInfo.getSkuName());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setCreateTime(new Date());
        goods.setCategory3Id(skuInfo.getCategory3Id());

        goods.setTmId(baseTrademark.getId());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setTmName(baseTrademark.getTmName());

        goods.setHotScore(0L);

        goods.setAttrs(searchAttrs);
        goods.setId(skuId);

        goodsElasticsearchRepository.save(goods);
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        //未解析返回结果
        SearchResponse searchResponse = null;
        //搜索请求语句
        SearchRequest searchRequest = getSearchRequest(searchParam);


        try {
                                         //searchRequest  RequestOptions
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchResponseVo searchResponseVo = getSearchResponseVo(searchResponse);
        return searchResponseVo;
    }

    /**
     * 热度值
     * @param skuId
     */
    @Override
    public void hotScore(Long skuId) {
        Integer hotScore = (Integer)redisTemplate.opsForValue().get("hotScore"+skuId);
        if(null!=hotScore){
            hotScore++;
            redisTemplate.opsForValue().increment("hotScore"+skuId, 1);
            //积攒到10的倍数 将热度值更新到es中
            if(hotScore%10==0){
                Goods goods = goodsElasticsearchRepository.findById(skuId).get();
                goods.setHotScore(Long.parseLong(hotScore+""));//redis中的值时int 所以先转成String再用parseLOng方法
                goodsElasticsearchRepository.save(goods);
            }
        }else{
            redisTemplate.opsForValue().set("hotScore"+skuId, 1);
        }




    }


    /**
     * 请求语句
     * @param searchParam
     * @return
     */
    private SearchRequest getSearchRequest(SearchParam searchParam) {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");

//前端传回 通过param获取
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();//平台属性Id:平台属性值名称:平台属性名
        String trademark = searchParam.getTrademark();
        String order = searchParam.getOrder();// 排序规则
        // 后台拼接：1:hotScore 2:price  前台页面传递：order=2:desc


//dsl语句的封装
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    //属性与三级分类是并集 用bool {must(match) filter(term)}
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //                                                                              queryBuilder实现类
        if(null!=props&&props.length>0){
            for (String prop : props) {
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValueName = split[1];
                String attrName = split[2];

                BoolQueryBuilder boolQueryBuilderNested = new BoolQueryBuilder();

                TermQueryBuilder termQueryBuilderAttrId = new TermQueryBuilder("attrs.attrId",attrId);
                MatchQueryBuilder matchQueryBuilderAttrValueName = new MatchQueryBuilder("attrs.attrValue",attrValueName);
                boolQueryBuilderNested.must(matchQueryBuilderAttrValueName);
                boolQueryBuilderNested.filter(termQueryBuilderAttrId);

                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQueryBuilderNested, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }

        }
//三级分类查询
        if(null!=category3Id&&category3Id>0){
            TermQueryBuilder termQueryBuilder =  new TermQueryBuilder("category3Id",category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);
//商标分组
//商标分组

//        TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(, null);
                                                                    //        "aggs":{
                                                                    //            "tmIdAgg":{
                                                                    //                "terms": {
                                                                    //                    "field": "tmId"
                                                                                        //方法名自定义    字段名
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                                            //二级分组
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"))
                ;
        searchSourceBuilder.aggregation(termsAggregationBuilder);


    //属性聚合                                                                        paht即field
//        NestedAggregationBuilder nested = AggregationBuilders.nested("attrsAgg", "attrs")
//                //nested自带分组
//                .subAggregation(
//                         AggregationBuilders.terms("attrIdAgg").field("attr.attrId")
//                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
//                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
//                );
//        searchSourceBuilder.aggregation(nested);
//        System.out.println(searchSourceBuilder.toString());
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs").subAggregation(
                AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))
        );
        searchSourceBuilder.aggregation(nestedAggregationBuilder);
        System.out.println(searchSourceBuilder.toString());

// 页面size
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);

        // 关键字
        if(!StringUtils.isEmpty(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
// 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
        highlightBuilder.field("title");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
//排序
        if(!StringUtils.isEmpty(order)){
            // 排序规则
            // 后台拼接：1:hotScore 2:price  前台页面传递：order=2:desc

//            private String order = ""; // 1：综合排序/热点  2：价格

            String key = order.split(":")[0];//前台页面传递：order=2:desc
            String sort = order.split(":")[1];
            String sortName = "hotScore";
            // 后台拼接：1:hotScore 2:price  前台页面传递：order=2:desc
            if(key.equals("2")){
                sortName = "price";
            }                                   //根据sort来确定是正序还是倒序
            searchSourceBuilder.sort(sortName, sort.equals("asc")? SortOrder.ASC: SortOrder.DESC);

        }


        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }

    /**
     * 解析返回结果
     * @param searchResponse
     * @return
     */
    private SearchResponseVo getSearchResponseVo(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        /*hit{
            "total":3
            "max_score":1.0,    搜索结果概览
            hits:[
            {
            "_index":goods,
            "_type":"info",     搜索结果
            }
            ]
        }
        */
        //搜索结果概览
        SearchHits hits = searchResponse.getHits();
        //搜索结果
        SearchHit[] hitsResult = hits.getHits();

        if(hitsResult!=null&&hitsResult.length>0){
            List<Goods> goodsList = new ArrayList<>();
            for (SearchHit documentFields : hitsResult) {
                String jsonSource = documentFields.getSourceAsString();
                Goods goods = JSONObject.parseObject(jsonSource, Goods.class);
                //高亮 与source平级 都在hits中
                Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                if(null!=highlightFields) {
                    HighlightField highlightTitle = highlightFields.get("title");
                    if(null!=highlightTitle){
                        Text text = highlightTitle.getFragments()[0];//高亮片段
                        String title = text.toString();
                        goods.setTitle(title);//替换有高亮的标题
                    }

                }
                //依据封装结果 返回list
                goodsList.add(goods);
            }
            searchResponseVo.setGoodsList(goodsList);//商品集合



            //搜索结果概览 和hits平级
            //接口转换成实现类 从而获取bucket
            ParsedLongTerms tmIdAgg = (ParsedLongTerms)searchResponse.getAggregations().get("tmIdAgg");

//            List<SearchResponseTmVo> searchResponseTmVos = getSearchResponseTmVos(tmIdAgg);
            //解析商标 - 流式编程                                          tmIdBUckets集合          集合放到map中   lamda接口的实现
            List<SearchResponseTmVo> searchResponseTmVos =tmIdAgg.getBuckets().stream().map(tmAggBucket->{
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                //id
                Long idKey = tmAggBucket.getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(idKey);
                // name
                ParsedStringTerms tmNameAgg =  (ParsedStringTerms)tmAggBucket.getAggregations().get("tmNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = tmNameAgg.getBuckets();
                String nameKey = nameAggBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmName(nameKey);
                // url

                //url
                ParsedStringTerms tmLogoUrlAgg =  (ParsedStringTerms)tmAggBucket.getAggregations().get("tmLogoUrlAgg");
                List<? extends Terms.Bucket> tmLogoUrlAggBuckets = tmLogoUrlAgg.getBuckets();

                String urlKey = nameAggBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(urlKey);
                return searchResponseTmVo;
            }).collect(Collectors.toList());
            searchResponseVo.setTrademarkList(searchResponseTmVos);


//            //解析属性
//            ParsedNested attrsAgg = (ParsedNested)searchResponse.getAggregations().get("attrsAgg");
//            //根据结构需多查一层id
//            ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().get("attrIdAgg");
//            List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(attrIdBucket->{
//                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
//                //id
//                long attrIdKey = attrIdBucket.getKeyAsNumber().longValue();
//                searchResponseAttrVo.setAttrId(attrIdKey);
//
//                //name
//                ParsedStringTerms attrNameAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrNameAgg");
//                String attrNameKey = attrNameAgg.getBuckets().get(0).getKeyAsString();
//                searchResponseAttrVo.setAttrName(attrNameKey);
//
//                //attrValue
//                ParsedStringTerms attrValueAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrValueAgg");
//                List<String> attrValueList =  attrValueAgg.getBuckets().stream().map(attrValueBucket->{
//                    String attrValueKey = ((Terms.Bucket) attrValueBucket).getKeyAsString();
//                    return attrValueKey;
//                }).collect(Collectors.toList());
//                searchResponseAttrVo.setAttrValueList(attrValueList);
//
//                return searchResponseAttrVo;
//            }).collect(Collectors.toList());
//
//
//
//            searchResponseVo.setAttrsList(searchResponseAttrVos);// 属性集合


//            ParsedNested attrsAgg = (ParsedNested)searchResponse.getAggregations().get("attrsAgg");
//            ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrsAgg.getAggregations().get("attrIdAgg");
//            List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(attrIdBucket->{
//                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
//                    long attrIdKey = attrIdBucket.getKeyAsNumber().longValue();
//                    //一对一
//                ParsedStringTerms attrNameAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrNameAgg");
//                String nameKey = attrNameAgg.getBuckets().get(0).getKeyAsString();
//
//                ParsedStringTerms attrValueAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrValueAgg");
//                List<String> attrValueList=attrValueAgg.getBuckets().stream().map(attrValueBucket->{
//                    String attrValueKey = attrValueBucket.getKeyAsString();
//                    return attrValueKey;
//                }).collect(Collectors.toList());
//
//                return searchResponseVo;
//            }).collect(Collectors.toList());
//            searchResponseVo.setAttrsList(searchResponseAttrVos);// 属性集合
//        }
            ParsedNested attrsAgg = (ParsedNested)searchResponse.getAggregations().get("attrsAgg");
            ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrsAgg.getAggregations().get("attrIdAgg");
            List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(attrIdBucket->{
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                // id
                long attrIdKey = attrIdBucket.getKeyAsNumber().longValue();
                // name
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrNameAgg");
                String attrNameKey = attrNameAgg.getBuckets().get(0).getKeyAsString();
                // ValueList
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)attrIdBucket.getAggregations().get("attrValueAgg");
                List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket->{
                    String attrValueKey = attrValueBucket.getKeyAsString();
                    return attrValueKey;
                }).collect(Collectors.toList());

                searchResponseAttrVo.setAttrId(attrIdKey);
                searchResponseAttrVo.setAttrName(attrNameKey);
                searchResponseAttrVo.setAttrValueList(attrValueList);
                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setAttrsList(searchResponseAttrVos);// 属性集合

        }
        return searchResponseVo;
    }
//解析商标聚合
    private List<SearchResponseTmVo> getSearchResponseTmVos(ParsedLongTerms tmIdAgg) {
        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            //最后要返回成vo
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取bucket中的id 封装到vo
            Long idKey = bucket.getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(idKey);

            //继续从bucket中获取name url  注意使用给实现类
            ParsedStringTerms tmNameAgg =  (ParsedStringTerms)bucket.getAggregations().get("tmNameAgg");
            List<? extends Terms.Bucket> nameAggBuckets = tmNameAgg.getBuckets();
            //一对一 所有的name相同 无需遍历
//                for (Terms.Bucket nameAggBucket : nameAggBuckets) {
//
//                    String nameKey = nameAggBucket.getKeyAsString();
//                    searchResponseTmVo.setTmName(nameKey);
//                }
            String nameKey = nameAggBuckets.get(0).getKeyAsString();
            searchResponseTmVo.setTmName(nameKey);

            //url
            ParsedStringTerms tmLogoUrlAgg =  (ParsedStringTerms)bucket.getAggregations().get("tmLogoUrlAgg");
            List<? extends Terms.Bucket> tmLogoUrlAggBuckets = tmLogoUrlAgg.getBuckets();

            String urlKey = nameAggBuckets.get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(urlKey);


            searchResponseTmVos.add(searchResponseTmVo);
        }
        return searchResponseTmVos;
    }


}
