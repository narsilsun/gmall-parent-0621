package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

///baseTrademark/getTrademarkList
@RequestMapping("/admin/product")
@RestController
@CrossOrigin
public class TrademarkApiController {

    @Autowired
    private TrademarkService trademarkService;
    //品牌
    @RequestMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> trademarkList = trademarkService.getTrademarkList();
        return Result.ok(trademarkList);
    }

}
