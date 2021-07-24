package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ListController {
    @Autowired
    ListFeignClient listFeignClient;

    @RequestMapping({"list.html","search.html"})
    public String list(Model model, SearchParam searchParam, HttpServletRequest request){

        String urlParam = getUrlParam(searchParam, request);
        SearchResponseVo searchResponseVo =  listFeignClient.list(searchParam);
        List<Goods> goodsList = searchResponseVo.getGoodsList();
        if(null!=goodsList&&goodsList.size()>0){
            model.addAttribute("goodsList",goodsList);
            model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
            model.addAttribute("attrsList",searchResponseVo.getAttrsList());
            model.addAttribute("urlParam",urlParam);
        }

        if(!StringUtils.isEmpty(searchParam.getTrademark())){                                     //:华为
            model.addAttribute("trademarkParam", searchParam.getTrademark().split(":")[1]);
        }
        //属性面包屑
        if(null!=searchParam.getProps()&&searchParam.getProps().length>0){
            List<SearchAttr> searchAttrs = new ArrayList<>();
            for (String prop : searchParam.getProps()) {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrName(prop.split(":")[2]);//属性id:属性名称:属性值
                searchAttr.setAttrValue(prop.split(":")[1]);
                searchAttr.setAttrId(Long.parseLong(prop.split(":")[0]));
                searchAttrs.add(searchAttr);
            }
            model.addAttribute("propsParamList",searchAttrs);
        }
        //排序
        if(!StringUtils.isEmpty(searchParam.getOrder())){
            HashMap<String, String> orderMap = new HashMap<>();

            //页面中是orderMap.type  后台拼接：1:hotScore 2:price  前台页面传递：order=  2:desc
            orderMap.put("type", searchParam.getOrder().split(":")[0]);
            orderMap.put("sort",searchParam.getOrder().split(":")[1]);
            model.addAttribute("orderMap",orderMap);
        }

        return "list/index";
    }

    /**
     * 拼接地址
     * @param searchParam
     * @param request
     * @return
     */
    private String getUrlParam(SearchParam searchParam, HttpServletRequest request) {
        //获取uri list.html
        String requestURI = request.getRequestURI();
        //拼接地址
        StringBuffer urlParam = new StringBuffer(requestURI);
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();//平台属性Id:平台属性值名称:平台属性名
        String trademark = searchParam.getTrademark();

        //三级分类id keyword是两个进入详情的方式 二选一\
        if(!StringUtils.isEmpty(keyword)){
            urlParam.append("?keyword="+keyword);
        }

        if(null!=category3Id&&category3Id>0){
            urlParam.append("?category3Id="+category3Id);
        }

        if(null!=props&&props.length>0){
            for (String prop : props) {
                urlParam.append("&props="+prop);
            }
        }

        if(!StringUtils.isEmpty(trademark)){
            urlParam.append("&trademark="+trademark);
        }
        return urlParam.toString();
    }

    @RequestMapping("/")
    public String index(Model model){

            List<JSONObject> list =  listFeignClient.getBaseCategoryList();
            model.addAttribute("list",list);

        return "index/index";
    }
}
