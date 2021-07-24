package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

//web项目
@Controller
public class ItemController {
    @Autowired
    ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId")Long skuId, Model model, HttpServletRequest request){
        String userId = request.getHeader("userId");
        //封装成map 减少调用次数
        Map<String, Object> map = new HashMap<>();
        //内部调用由springboot封装 无需返回成result格式
        map = itemFeignClient.getItem(skuId);
        model.addAllAttributes(map);
        return "item/index";
    }

    @RequestMapping("test")
    public String test(Model model){
        model.addAttribute("test", "test1");
        return "test";
    }
}
