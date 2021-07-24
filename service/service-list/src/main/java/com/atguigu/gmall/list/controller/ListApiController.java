package com.atguigu.gmall.list.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/list")
@RestController
public class ListApiController {

    @Autowired
    private ListService listService;
    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){
        List<JSONObject> list = listService.getBaseCategoryList();
        return list;
    }
    @RequestMapping("createGoodsIndex")
    public Result createGoodsIndex(){
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }
    @RequestMapping("cancelSale/{skuId}")
   public void cancelSale(@PathVariable("skuId") Long skuId){
        listService.cancelSale(skuId);
        System.out.println("下架");
    }

    @RequestMapping("onSale/{skuId}")
    public void onSale(@PathVariable("skuId") Long skuId){
        listService.onSale(skuId);
        System.out.println("上架");
    }

    /**
     * 搜索
     * @param searchParam
     * @return
     */
    @RequestMapping("list")
    SearchResponseVo list(@RequestBody SearchParam searchParam){
        SearchResponseVo searchResponseVo =  listService.list(searchParam);
        return searchResponseVo;
    }

    @RequestMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId){
        listService.hotScore(skuId);
    }
}
