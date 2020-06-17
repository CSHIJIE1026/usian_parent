package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.OrderServiceFeign;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderShipping;
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

    @Autowired
    private OrderServiceFeign orderServiceFeign;

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

    @RequestMapping("/insertOrder")
    public Result insertOrder(String orderItem, TbOrder tbOrder, TbOrderShipping tbOrderShipping){

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTbOrderItem(orderItem);
        orderInfo.setTbOrder(tbOrder);
        orderInfo.setTbOrderShipping(tbOrderShipping);

        String orderId = orderServiceFeign.insertOrder(orderInfo);

        if (orderId != null){
            return Result.ok(orderId);
        }
        return Result.error("提交订单错误");
    }

}
