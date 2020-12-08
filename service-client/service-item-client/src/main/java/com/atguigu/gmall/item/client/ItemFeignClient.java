package com.atguigu.gmall.item.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@FeignClient(value = "service-item")
@Component
public interface ItemFeignClient {
                    //完整路径
    @RequestMapping("api/item/getItem/{skuId}")
    Map<String, Object> getItem(@PathVariable("skuId") Long skuId);
}
