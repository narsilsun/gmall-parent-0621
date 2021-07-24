package com.atguigu.gmall.list.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.util.List;

public interface ListService {
    List<JSONObject> getBaseCategoryList();

    void cancelSale(Long skuId);

    void onSale(Long skuId);

    SearchResponseVo list(SearchParam searchParam);

    void hotScore(Long skuId);
}
