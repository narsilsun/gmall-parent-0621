package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SupService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/product")
@RestController
@CrossOrigin
public class SpuApiController {
    @Autowired
    private SupService supService;

    //分页查询 商品属性SPU管理
    @RequestMapping("/{pageNo}/{size}")
    public Result spuList(@PathVariable("pageNo") Long pageNo,@PathVariable("size") Long size,Long category3Id){
        IPage<SpuInfo> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(size);
        IPage<SpuInfo> supInfoIPage = supService.spuList(page,category3Id);
        return Result.ok(supInfoIPage);
    }
    @RequestMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrs = supService.baseSaleAttrList();
        return Result.ok(baseSaleAttrs);
    }
    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        supService.saveSpuInfo(spuInfo);
        return Result.ok();
    }


}
