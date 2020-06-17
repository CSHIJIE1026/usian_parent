package com.usian.pojo;

public class OrderInfo {

    private TbOrder tbOrder;
    private String tbOrderItem;
    private TbOrderShipping tbOrderShipping;

    public TbOrder getTbOrder() {
        return tbOrder;
    }

    public void setTbOrder(TbOrder tbOrder) {
        this.tbOrder = tbOrder;
    }

    public String getTbOrderItem() {
        return tbOrderItem;
    }

    public void setTbOrderItem(String tbOrderItem) {
        this.tbOrderItem = tbOrderItem;
    }

    public TbOrderShipping getTbOrderShipping() {
        return tbOrderShipping;
    }

    public void setTbOrderShipping(TbOrderShipping tbOrderShipping) {
        this.tbOrderShipping = tbOrderShipping;
    }
}
