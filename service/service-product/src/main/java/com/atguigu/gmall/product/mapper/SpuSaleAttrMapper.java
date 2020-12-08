package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
                                                    //多个参数需要@Param
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId") Long spuId);
                                        //单个参数不需要@Param
    List<Map> selectSaleAttrValuesBySpu( @Param("spuId") Long spuId);
}
