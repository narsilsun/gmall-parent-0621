package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.lang.ref.WeakReference;
import java.nio.channels.WritePendingException;
import java.util.List;

@Service
public class BaseAttrServiceImpl implements BaseAttrService {
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> attrInfoList(Long category3Id) {
//        QueryWrapper<BaseAttrInfo> wrapper = new QueryWrapper<>();
//        wrapper.eq("category_id",category3Id);
//        wrapper.eq("category_level", 3);
//        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectList(wrapper);
//        //遍历塞入attr_value
//        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
//            //value.attr_id = info.id
//            Long attrId = baseAttrInfo.getId();
//
//            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("attr_id", attrId);
//            //通过valueMapper 和wrapper 得到list
//            List<BaseAttrValue> baseAttrValuesList = baseAttrValueMapper.selectList(queryWrapper);
//            //放入info
//            baseAttrInfo.setAttrValueList(baseAttrValuesList);
//        }
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectAttrInfoList(3,category3Id);
        return baseAttrInfos;
    }



    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //获取info主键 属性id
        Long attrId = baseAttrInfo.getId();
        //更新
        if(null!=attrId||attrId>0){
            //更新传入的baseAttrInfo
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //先删除再添加
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", attrId);
            baseAttrValueMapper.delete(wrapper);
        }else{
            baseAttrInfoMapper.insert(baseAttrInfo);
            //防止id=null
            attrId = baseAttrInfo.getId();
        }
//        获取并通过属性id更新value
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue attrValue : attrValueList) {
            attrValue.setAttrId(attrId);
            baseAttrValueMapper.insert(attrValue);
        }
    }







    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(wrapper);
        return baseAttrValues;
    }


}
