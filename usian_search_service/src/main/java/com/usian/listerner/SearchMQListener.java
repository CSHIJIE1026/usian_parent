package com.usian.listerner;

import com.usian.service.SearchItemService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchMQListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private SearchItemService searchItemService;

    /**
     * 监听者接收消息三要素：
     *  1、queue
     *  2、exchange
     *  3、routing key
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search_queue",durable = "true"),
            exchange = @Exchange(value = "item_exchage",type = ExchangeTypes.TOPIC),
            key = {"item.*"}
    ))

    public void listen(String itemId) throws IOException {
        System.out.println("接收消息 "+itemId);
        int result = searchItemService.insertDocument(itemId);
        if (result > 0){
            throw new RuntimeException("同步失败");
        }
    }


}
