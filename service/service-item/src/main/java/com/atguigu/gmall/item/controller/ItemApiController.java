package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/item")
@RestController
@CrossOrigin
public class ItemApiController {
    @Autowired
    ItemService itemService;

    @GetMapping("getItem/{skuId}")
    public Map<String, Object> getItem(@PathVariable("skuId") Long skuId){
        //封装 item页面所需数据接口
        Map<String, Object> map = itemService.getItem(skuId);
        return map;
    }
}
