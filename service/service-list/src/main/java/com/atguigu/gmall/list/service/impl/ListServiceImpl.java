package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
   private ProductFeignClient productFeignClient;


    @Override
    public List<JSONObject> getBaseCategoryList() {
        return productFeignClient.getBaseCategoryList();
    }
}
