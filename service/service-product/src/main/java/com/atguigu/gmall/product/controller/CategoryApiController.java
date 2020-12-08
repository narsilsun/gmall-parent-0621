package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;


import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("admin/product")
@RestController
@CrossOrigin

public class CategoryApiController {
    @Autowired
    private CategoryService categoryService;


    @GetMapping("getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> baseCategory1List = categoryService.getCategory1();
        return Result.ok(baseCategory1List);
    }
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id){
        List<BaseCategory2> baseCategory2List = categoryService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id){
        List<BaseCategory3> baseCategory3List = categoryService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

}
