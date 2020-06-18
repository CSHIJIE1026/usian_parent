package com.usian.service;

import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbOrderItemMapper;
import com.usian.mapper.TbOrderMapper;
import com.usian.mapper.TbOrderShippingMapper;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService{

    @Value("${ORDER_ID_KEY}")
    public String ORDER_ID_KEY;

    @Value("${ORDER_ID_BEGIN}")
    public Long ORDER_ID_BEGIN;

    @Value("${ORDER_ITEM_ID_KEY}")
    public String ORDER_ITEM_ID_KEY;

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Autowired
    private TbOrderShippingMapper tbOrderShippingMapper;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private TbItemMapper tbItemMapper;


    @Override
    public String insertOrder(OrderInfo orderInfo) {
        //1、解析orderInfo
        TbOrder tbOrder = orderInfo.getTbOrder();
        TbOrderShipping tbOrderShipping = orderInfo.getTbOrderShipping();
        List<TbOrderItem> tbOrderItemList = JsonUtils.jsonToList(orderInfo.getTbOrderItem(), TbOrderItem.class);

        //2、保存订单信息
        if (!redisClient.exists(ORDER_ID_KEY)){
            redisClient.set(ORDER_ID_KEY,ORDER_ID_BEGIN);
        }
        Long orderId = redisClient.incr(ORDER_ID_KEY, 1L);
        tbOrder.setOrderId(orderId.toString());
        tbOrder.setCreateTime(new Date());
        tbOrder.setUpdateTime(new Date());

        //1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
        tbOrder.setStatus(1);
        tbOrderMapper.insertSelective(tbOrder);

        //3、保存明细信息
        if (!redisClient.exists(ORDER_ITEM_ID_KEY)){
            redisClient.set(ORDER_ITEM_ID_KEY,0);
        }
        for (int i = 0; i < tbOrderItemList.size(); i++) {
            Long orderItemId = redisClient.incr(ORDER_ITEM_ID_KEY, 1L);
            TbOrderItem tbOrderItem = tbOrderItemList.get(i);
            tbOrderItem.setId(orderItemId.toString());
            tbOrderItem.setOrderId(orderId.toString());
            tbOrderItemMapper.insertSelective(tbOrderItem);
        }

        //4、保存物流信息
        tbOrderShipping.setOrderId(orderId.toString());
        tbOrderShipping.setCreated(new Date());
        tbOrderShipping.setUpdated(new Date());
        tbOrderShippingMapper.insertSelective(tbOrderShipping);


        //发布消息到mq，完成扣减库存
        amqpTemplate.convertAndSend("order_exchage","order.add",orderId);

        //5、返回订单id
        return orderId.toString();
    }

    /**
     * 查询超时订单
     * @return
     */
    @Override
    public List<TbOrder> selectOverTimeTborder() {
        //扫描条件：状态是未付款 并且 创建时间 <= 当前时间 – 2天  并且付款方式为在线支付的订单
        return tbOrderMapper.selectOverTimeTborder();
    }

    /**
     * 关闭超时订单
     * @param tbOrder
     */
    @Override
    public void updateOverTimeTbOrder(TbOrder tbOrder) {
        //修改订单的状态为关闭状态、结束时间、关闭时间、修改时间为当前时间
        tbOrder.setStatus(6);
        tbOrder.setCloseTime(new Date());
        tbOrder.setUpdateTime(new Date());
        tbOrder.setEndTime(new Date());
        tbOrderMapper.updateByPrimaryKeySelective(tbOrder);
    }

    /**
     * 把商品数量加回去
     * @param orderId
     */
    @Override
    public void updateTbItemByOrderId(String orderId) {
        //1、通过orderId查询LisT<TbOrderItem>
        TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = tbOrderItemExample.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<TbOrderItem> tbOrderItemList = tbOrderItemMapper.selectByExample(tbOrderItemExample);

        for (int i = 0; i < tbOrderItemList.size(); i++) {
            TbOrderItem tbOrderItem = tbOrderItemList.get(i);
            //2、查询出List<TbOrderItem>的对象，根据这个对象的itemId查询商品
            TbItem tbItem = tbItemMapper.selectByPrimaryKey(Long.valueOf(tbOrderItem.getItemId()));
            //3、跟据查询出来的商品修改数量
            tbItem.setNum(tbItem.getNum()+tbOrderItem.getNum());
            tbItemMapper.updateByPrimaryKeySelective(tbItem);
        }

    }
}
