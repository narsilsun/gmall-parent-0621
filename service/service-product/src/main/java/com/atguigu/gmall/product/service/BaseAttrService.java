package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;

import java.util.List;

public interface BaseAttrService {
    List<BaseAttrInfo> attrInfoList(Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);


    List<BaseAttrValue> getAttrValueList(Long attrId);
}
