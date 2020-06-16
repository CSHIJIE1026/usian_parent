package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/frontend/cart")
public class OrderController {

    @Autowired
    private CartServiceFeign cartServiceFeign;

    @RequestMapping("/goSettlement")
    public Result goSettlement(String[] ids, String userId){
        //获取购物车
        Map<String, TbItem> cart = cartServiceFeign.selectCartByUserId(userId);
        //从购物车中获取商品
        List<TbItem> list = new ArrayList<TbItem>();
        for (String id : ids) {
            list.add(cart.get(id));
        }
        if (list.size() > 0){
            return Result.ok(list);
        }
        return Result.error("查无结果");
    }
}
