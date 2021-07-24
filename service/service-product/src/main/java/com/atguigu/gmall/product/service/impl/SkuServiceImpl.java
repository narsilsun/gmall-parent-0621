package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.config.GmallCache;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuServiceMapper;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired                  //skuInfo
    private SkuServiceMapper skuServiceMapper;
    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    ListFeignClient listFeignClient;
    @Override
    public IPage<SkuInfo> getSkuList(IPage<SkuInfo> skuInfoPage) {

        IPage<SkuInfo> skuInfoIPage = skuServiceMapper.selectPage(skuInfoPage, null);
        return skuInfoIPage;
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //保存skuInfo之后 获取主键
       skuServiceMapper.insert(skuInfo);
        Long skuId = skuInfo.getId();
        //目标:attr_info 达成 通过中间表(sku_attr_value)
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(null!=skuAttrValueList){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                System.out.println("skuAttrValue(1):"+skuAttrValue);
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        //目标:sale_attr 达成:通过中间表sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(null!=skuSaleAttrValueList){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                System.out.println("skuSaleAttrValue(2):"+skuSaleAttrValue);
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(null!=skuImageList){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
                skuImageMapper.insert(skuImage);
            }
        }


    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfo skuInfo = skuServiceMapper.selectById(skuId);
        BigDecimal price = skuInfo.getPrice();
        return price;
    }

    @GmallCache
    @Override
    public SkuInfo getSkuInfo(Long skuId) {

        SkuInfo skuInfo = getSkuInfoByIdFromDB(skuId);

        return skuInfo;
    }

    /**
     * 下架
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(0);
        skuInfo.setId(skuId);
        skuServiceMapper.updateById(skuInfo);
        //远程调用listFeign
        listFeignClient.cancelSale(skuId);

    }

    /**
     * 上架
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuId);
        skuServiceMapper.updateById(skuInfo);

        listFeignClient.onSale(skuId);
    }

    private SkuInfo getSkuInfoBak(Long skuId) {
        SkuInfo skuInfo = null;
        //访问nosql
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);
        //A 如果不存在访问db
        if(null==skuInfo) {
            //申请分布式锁 并将uuid作为value
            String key = UUID.randomUUID().toString();
            //是否拿到锁
            Boolean OK = redisTemplate.opsForValue().setIfAbsent("sku:" + skuId + ":lock", key, 2, TimeUnit.SECONDS);
            //B 是否拿到锁
            if (OK) {
                //拿到锁 访问db
                skuInfo = getSkuInfoByIdFromDB(skuId);
                //C 数据库中是否有数据
                if (null != skuInfo) {
//                    ①同步缓存
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX, skuInfo);
//                    ②释放锁 通过与uuid的比较 确定是否是同一把锁
                    String openKey = (String) redisTemplate.opsForValue().get("sku:" + skuId + ":lock");
                    if (key.equals(openKey)) {
                        redisTemplate.delete("sku:" + skuId + ":lock");
                    }
                    //C
                } else {
                    //数据库中没值=>同步空缓存 设定时间 稍后再看
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX, skuInfo, 5, TimeUnit.SECONDS);
                }
            } else {//B 未拿到锁 稍等片刻后自旋(同一方法)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfoByIdFromDB(skuId);
            }
        }
        return skuInfo;
    }

    private SkuInfo getSkuInfoByIdFromDB(Long skuId) {
        SkuInfo skuInfo = skuServiceMapper.selectById(skuId);
        QueryWrapper<SkuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(wrapper);
        //实体类中为了业务存在的
        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }
}
