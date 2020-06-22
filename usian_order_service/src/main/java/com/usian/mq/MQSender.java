package com.usian.mq;

import com.usian.mapper.LocalMessageMapper;
import com.usian.pojo.LocalMessage;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQSender implements ReturnCallback,ConfirmCallback {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private LocalMessageMapper localMessageMapper;

    public void sendMsg(LocalMessage localMessage) {
        RabbitTemplate rabbitTemplate = (RabbitTemplate) this.amqpTemplate;
        rabbitTemplate.setConfirmCallback(this); //确认回调
        rabbitTemplate.setReturnCallback(this);  //失败回退

        //用于确认之后更改本地消息状态或删除本地消息--本地消息id
        CorrelationData correlationData = new CorrelationData(localMessage.getTxNo());
        rabbitTemplate.convertAndSend("order_exchage","order.add", JsonUtils.objectToJson(localMessage),correlationData);
    }

    /**
     * 消息发送失败时调用
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("return--message:" + new String(message.getBody())
                + ",exchange:" + exchange + ",routingKey:" + routingKey);
    }

    /**
     * 下游服务消息确认成功返回后调用
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        if (ack){
            // 消息发送成功,更新本地消息为已成功发送状态或者直接删除该本地消息记录
            LocalMessage localMessage = localMessageMapper.selectByPrimaryKey(id);
            localMessage.setState(1);
            localMessageMapper.updateByPrimaryKeySelective(localMessage);
        }
    }
}
