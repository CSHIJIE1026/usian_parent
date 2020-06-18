package com.usian.quartz;

import com.usian.pojo.TbOrder;
import com.usian.redis.RedisClient;
import com.usian.service.OrderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class OrderQuartz implements Job {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisClient redisClient;

    /**
     * 关闭超时订单
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (redisClient.setnx("SETNX_LOCK_ORDER_KEY",ip,30L)){
            System.out.println("关闭超时订单");
            //1、查询超时订单
            List<TbOrder> orderList = orderService.selectOverTimeTborder();

            //2、关闭超时订单
            for (int i = 0; i < orderList.size(); i++) {
                TbOrder tbOrder = orderList.get(i);
                orderService.updateOverTimeTbOrder(tbOrder);

                //3、把超时订单中的商品库存数量加回去
                orderService.updateTbItemByOrderId(tbOrder.getOrderId());
            }
            redisClient.del("SETNX_LOCK_ORDER_KEY");
        }else {
            System.out.println("============机器："+ip+" 占用分布式锁，任务正在执行=======================");
        }
    }
}
