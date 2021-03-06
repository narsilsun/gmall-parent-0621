package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.CategoryService;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.product.service.SupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/product")
public class ProductApiController {
    //复用之前的skuService
    @Autowired
    SkuService skuService;

    @Autowired
    SupService supService;
    @Autowired
    CategoryService categoryService;

    @RequestMapping("getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo =  skuService.getSkuInfo(skuId);
        return skuInfo;
    }

    @RequestMapping("getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId){
        BigDecimal bigDecimal = skuService.getPrice(skuId);
//        BigDecimal bigDecimal = new BigDecimal("0");
        return bigDecimal;
    }
    @RequestMapping("getSpuSaleAttr/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttr(@PathVariable("skuId") Long spuId){
        SkuInfo skuInfo =  skuService.getSkuInfo(spuId);
        List<SpuSaleAttr> spuSaleAttrs = supService.spuSaleAttrList(spuId);
        return spuSaleAttrs;
    }
    @RequestMapping("getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId,@PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> spuSaleAttrList =  supService.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrList;
    }
    /**
     * 获取分类 封装到视图
     * @param category3Id
     * @return
     */
    @RequestMapping("getCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getCategoryViewByCategory3Id(@PathVariable("category3Id")  Long category3Id){
        BaseCategoryView baseCategoryView = categoryService.getCategoryViewByCategory3Id(category3Id);

        return baseCategoryView;
    }

    /**
     * 获取spu下的所有销售属性
     * @param spuId
     * @return
     */
    @RequestMapping("/getSkuValueIdsMap/{spuId}")
    Map<String, Long> getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
       Map<String,Long>  map = supService.getSkuValueIdsMap(spuId);
       return map;
    }

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){
            List<JSONObject> list = categoryService.getBaseCategoryList();
            return list;
        }
    }


