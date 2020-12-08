package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/product")
@RestController
@CrossOrigin
public class BaseAttrApiController {

    @Autowired
    private BaseAttrService baseAttrService;

    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category3Id){
        List<BaseAttrInfo> baseAttrInfos = baseAttrService.attrInfoList(category3Id);
        return Result.ok(baseAttrInfos);
    }
    @RequestMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        baseAttrService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }
    //修改前查询
    @RequestMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
       List<BaseAttrValue> baseAttrValues = baseAttrService.getAttrValueList(attrId);
       return Result.ok(baseAttrValues);
    }

}
