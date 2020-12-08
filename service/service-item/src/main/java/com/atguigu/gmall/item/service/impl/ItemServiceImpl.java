package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ItemServiceImpl implements ItemService {

    //由product调数据库
    @Autowired
    ProductFeignClient productFeignClient;
    //线程池
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;


    @Override
    public Map<String, Object> getItem(Long skuId) {
        long start = System.currentTimeMillis();
//        Map<String, Object> map = getStringObjectMapSingle(skuId);
        Map<String, Object> map = getStringObjectMapTread(skuId);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        return map;
    }

    /**
     * 多线程 + 线程池
     * @param skuId
     * @return
     */
    private Map<String, Object> getStringObjectMapTread(Long skuId) {

        Map<String, Object> map = new HashMap<>();
        //获取skuInfo
        CompletableFuture<SkuInfo> CompletableFutureSkuInfo = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                map.put("skuInfo", skuInfo);
                return skuInfo;
            }
    //加入线程池
        },threadPoolExecutor);
                    //无返回结果 直接放入map
        CompletableFuture<Void> CompletableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal bigDecimal = productFeignClient.getPrice(skuId);
                map.put("price", bigDecimal);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> CompletableFutureSpuSaleAttrList = CompletableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
                map.put("spuSaleAttrList", spuSaleAttrList);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> CompletableFutureCategoryView = CompletableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //分级 封装视图
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());
                map.put("categoryView", baseCategoryView);
            }
        },threadPoolExecutor);
        CompletableFuture<Void> CompletableFutureValuesSkuJson = CompletableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //按照格式返回给前端 同一spu下的所有组合
                Map<String, Long> jsonMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());

                String json = JSON.toJSONString(jsonMap);
                map.put("valuesSkuJson", json);
            }
        },threadPoolExecutor);
        CompletableFuture.allOf(CompletableFutureSkuInfo,CompletableFuturePrice, CompletableFutureSpuSaleAttrList,
                CompletableFutureCategoryView,CompletableFutureValuesSkuJson
        ).join();

        return map;
    }

    /**
     * 单线程
     * @param skuId
     * @return
     */
    private Map<String, Object> getStringObjectMapSingle(Long skuId) {
        Map<String, Object> map = new HashMap<>();
        //详情
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        //价格
        BigDecimal bigDecimal = productFeignClient.getPrice(skuId);
        //销售属性
//        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttr(skuInfo.getSpuId());
        //销售属性 标记当前
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        //分级 封装视图
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryViewByCategory3Id(skuInfo.getCategory3Id());

        //按照格式返回给前端 同一spu下的所有组合
        Map<String, Long> jsonMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());

        String json = JSON.toJSONString(jsonMap);
        map.put("valuesSkuJson", json);

        map.put("categoryView", baseCategoryView);
        map.put("price", bigDecimal);
        map.put("skuInfo", skuInfo);
        map.put("spuSaleAttrList", spuSaleAttrList);
        return map;
    }
}
