package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//调用目标(目标配置文件中命名)
//service-Product-productApiController
@FeignClient(value = "service-product")
@Component
public interface ProductFeignClient {
                    //完整
    @RequestMapping("/api/product/getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);

    @RequestMapping("/api/product/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSpuSaleAttr/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttr(@PathVariable("spuId") Long spuId);

    @RequestMapping("api/product/getCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getCategoryViewByCategory3Id(@PathVariable("category3Id")  Long category3Id);

    @RequestMapping("api/product/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId,@PathVariable("spuId") Long spuId);

    @RequestMapping("api/product/getSkuValueIdsMap/{spuId}")
    Map<String, Long> getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    @RequestMapping("api/product/getBaseCategoryList")
    List<JSONObject> getBaseCategoryList();

    @RequestMapping("api/product/getTrademarkById/{tmId}")
    BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId);

    @RequestMapping("api/product/getSearchAttrList/{skuId}")
    List<SearchAttr> getSearchAttrList(@PathVariable("skuId") Long skuId);
}
