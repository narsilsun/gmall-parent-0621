package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    IPage<SkuInfo> getSkuList(IPage<SkuInfo> skuInfoPage);

    void saveSkuInfo(SkuInfo skuInfo);

    BigDecimal getPrice(Long skuId);

    SkuInfo getSkuInfo(Long skuId);
}
