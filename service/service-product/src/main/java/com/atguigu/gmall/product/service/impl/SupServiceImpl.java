package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupServiceImpl implements SupService {
    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public IPage<SpuInfo> spuList(IPage<SpuInfo> page, Long category3Id) {

        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", category3Id);
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(page, wrapper);
        return spuInfoIPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrs;
    }



    @Override          //spuInfo中的图片集合sumImageList 和销售属性集合spuSaleAttrList
    public void saveSpuInfo(SpuInfo spuInfo) {
        //id是数据库生成的
        spuInfoMapper.insert(spuInfo);
        //获取主键id
        Long spuId = spuInfo.getId();

        //得到图片集合 判断后遍历
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(null!=spuImageList){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuId);
                spuImageMapper.insert(spuImage);
            }
        }
        //销售属性集合(版本 颜色.. 其中有sale_attr_value属性值)
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(null!=spuSaleAttrList){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuId);
                spuSaleAttrMapper.insert(spuSaleAttr);

                //id来自于字典Base_Sale_Attr
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if(null!=spuSaleAttrValueList){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //需要通过两个id确定
                        spuSaleAttrValue.setSpuId(spuId);
                        spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttrValue.getBaseSaleAttrId());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(wrapper);
        return spuImageList;
    }

    /**
     * 单纯查询所有spu属性(过时)
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        QueryWrapper<SpuSaleAttr> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(wrapper);

        if(null!=spuSaleAttrs){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
                QueryWrapper<SpuSaleAttrValue> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("spu_id",spuId);
                queryWrapper.eq("base_sale_attr_id",spuSaleAttr.getBaseSaleAttrId());
                List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapper);
                spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
            }
        }
        return spuSaleAttrs;
    }

    /**
     * 通过spu_sale_attr & spu_sale_attr_value & sku_sale_attr_value
     * 三表直接标记处checkId=1 即当前的sku
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs =spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrs;
    }

    @Override
    public Map<String, Long> getSkuValueIdsMap(Long spuId) {
        Map<String, Long> jsonMap = null;
        try {
            //通过SpuId  查询  spu_sale_attr_value表  找到 同一spuId下  存在的sku对应的sale_attr_value
            //resultmap 返回多个结果 自动封装成list
            List<Map> saleMapInList = spuSaleAttrMapper.selectSaleAttrValuesBySpu(spuId);
//   查询后     saleMapInList是 value_ids  sku_id
            jsonMap = new HashMap<>();
            for (Map map : saleMapInList) {
                String value_ids = (String) map.get("value_Ids");
                Long sku_id = (Long) map.get("sku_id");
                jsonMap.put(value_ids,sku_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonMap;
    }
}
