package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.product.service.SupService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//http://localhost:8080/admin/product/list/1/10
@RequestMapping("/admin/product")
@RestController
@CrossOrigin
public class SkuApiController {
    @Autowired
    private SkuService skuService;
    @Autowired
    private SupService supService;

    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){

        skuService.cancelSale(skuId);

        return Result.ok();
    }

    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){

        skuService.onSale(skuId);

        return Result.ok();
    }

    //分页显示
    @RequestMapping("/list/{pageNo}/{pageSize}")
    public Result skuList(@PathVariable("pageNo") Long pageNo,@PathVariable("pageSize")Long pageSize){
        IPage<SkuInfo> skuInfoPage = new Page<>();
        skuInfoPage.setSize(pageSize);
        skuInfoPage.setCurrent(pageNo);
        IPage<SkuInfo> skuInfoList = skuService.getSkuList(skuInfoPage);
        return Result.ok(skuInfoList);
    }

    //添加sku-获取图片spuImageList/1
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId")Long spuId){
        List<SpuImage> spuImageList = supService.spuImageList(spuId);
        return Result.ok(spuImageList);
    }
    //获取销售属性 spuSaleAttrList/{spuId}
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId")Long spuId){
        List<SpuSaleAttr> spuSaleAttrs = supService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrs);
    }
    //添加sku saveSkuInfo
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

}
