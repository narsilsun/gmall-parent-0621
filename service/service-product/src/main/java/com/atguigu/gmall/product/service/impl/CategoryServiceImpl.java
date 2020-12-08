package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }


    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id", category1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(wrapper);
        return baseCategory2s;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> wrapper = new QueryWrapper<>();
        wrapper.eq("category2_id", category2Id);
        List<BaseCategory3> baseCategory3s = baseCategory3Mapper.selectList(wrapper);
        return baseCategory3s;
    }

    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        QueryWrapper<BaseCategoryView> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", category3Id);
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectOne(wrapper);
        return baseCategoryView;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
//        category1.categoryId}
        //包含 一级 二级 三级
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        //返回值
        List<JSONObject> category1list = new ArrayList<>();

        //分类方法                            list
        Map<Long, List<BaseCategoryView>> categroy1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //对map遍历 entrySet().for                        一级分类map
        for (Map.Entry<Long, List<BaseCategoryView>> categroy1Object : categroy1Map.entrySet()) {
            //获取一级分类名字              index
            String category1Name = categroy1Object.getValue().get(0).getCategory1Name();
            Long categroy1Id = categroy1Object.getKey();

            JSONObject categroy1Json = new JSONObject();
            categroy1Json.put("categoryName",category1Name);
            categroy1Json.put("categoryId",categroy1Id);
            //二级分类
            List<JSONObject> category2list = new ArrayList<>();
            List<BaseCategoryView> category2Views = categroy1Object.getValue();
            Map<Long, List<BaseCategoryView>> categroy2Map = category2Views.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> categroy2Object : categroy2Map.entrySet()) {
                String category2Name = categroy2Object.getValue().get(0).getCategory2Name();
                Long categroy2Id = categroy2Object.getKey();

                JSONObject categroy2Json = new JSONObject();
                categroy2Json.put("categoryName",category2Name);
                categroy2Json.put("categoryId",categroy2Id);
                category2list.add(categroy2Json);

                List<BaseCategoryView> category3Views = categroy2Object.getValue();
                //分类
                List<JSONObject> category3list = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> categroy3Map = category3Views.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> categroy3Object : categroy3Map.entrySet()) {
                    Long categroy3Id = categroy3Object.getKey();
                    String category3Name = categroy3Object.getValue().get(0).getCategory3Name();
                    JSONObject categroy3Json = new JSONObject();
                    categroy3Json.put("categoryId",categroy3Id);
                    categroy3Json.put("categoryName",category3Name);
                    category3list.add(categroy3Json);
                }
                //三级作为child加入二级的jsonObject
                categroy2Json.put("categoryChild", category3list);
                //同时 放入list 以便加入一级
                category2list.add(categroy2Json);
            }


            categroy1Json.put("categoryChild",category2list);
            category1list.add(categroy1Json);
        }
        return category1list;
    }

}
