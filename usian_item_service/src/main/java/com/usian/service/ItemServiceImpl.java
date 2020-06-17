package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.*;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private TbItemDescMapper tbItemDescMapper;

    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Value("${ITEM_INFO}")
    private String ITEM_INFO;

    @Value("${BASE}")
    private String BASE;

    @Value("${DESC}")
    private String DESC;

    @Value("${PARAM}")
    private String PARAM;

    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;

    @Value("${SETNX_BASC_LOCK_KEY}")
    private String SETNX_BASC_LOCK_KEY;

    @Value("${SETNX_DESC_LOCK_KEY}")
    private String SETNX_DESC_LOCK_KEY;

    /**
     * 查询商品信息
     * @param itemId
     * @return
     */
    @Override
    public TbItem selectItemInfo(Long itemId) {
        //查询缓存
        TbItem tbItem = (TbItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + BASE);
        if (tbItem != null){
            return tbItem;
        }
        if (redisClient.setnx(SETNX_BASC_LOCK_KEY+":"+itemId,itemId,30L)){
            tbItem = tbItemMapper.selectByPrimaryKey(itemId);
            if (tbItem != null){
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,tbItem);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE,ITEM_INFO_EXPIRE);
            }else {
                /********************解决缓存穿透************************/
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,null);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE,30L);
            }
            redisClient.del(SETNX_BASC_LOCK_KEY+":"+itemId);
            return tbItem;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemInfo(itemId);
        }
    }

    /**
     * 查询所有商品，并分页
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        TbItemExample tbItemExample = new TbItemExample();
        tbItemExample.setOrderByClause("updated DESC");
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo((byte)1);
        List<TbItem> tbItemList = tbItemMapper.selectByExample(tbItemExample);
        for (int i = 0; i < tbItemList.size(); i++) {
            TbItem tbItem =  tbItemList.get(i);
            tbItem.setPrice(tbItem.getPrice()/100);
        }
        PageInfo<TbItem> tbItemPageInfo = new PageInfo<>(tbItemList);
        PageResult pageResult = new PageResult();
        pageResult.setPageIndex(tbItemPageInfo.getPageNum());
        pageResult.setResult(tbItemPageInfo.getList());
        pageResult.setTotalPage(Long.valueOf(tbItemPageInfo.getPages()));
        return pageResult;
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer insertTbItem(TbItem tbItem, String desc, String itemParams) {
        //1、保存商品信息
        long itemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)1);
        tbItem.setCreated(date);
        tbItem.setUpdated(date);
        tbItem.setPrice(tbItem.getPrice()*100);
        int tbItemNum = tbItemMapper.insertSelective(tbItem);

        //2、保存商品描述信息
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(itemId);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        tbItemDesc.setItemDesc(desc);
        int tbItemDescNum = tbItemDescMapper.insertSelective(tbItemDesc);

        //3、保存商品规格信息
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setItemId(itemId);
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(date);
        int tbItemParamItemNum = tbItemParamItemMapper.insertSelective(tbItemParamItem);

        amqpTemplate.convertAndSend("item_exchage","item.add",itemId);

        return tbItemNum+tbItemDescNum+tbItemParamItemNum;
    }

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @Override
    public Integer deleteItemById(Long itemId) {
        int i = tbItemMapper.deleteByPrimaryKey(itemId);
        redisClient.del(ITEM_INFO + ":" + itemId + ":" + BASE);
        redisClient.del(ITEM_INFO + ":" + itemId + ":" + DESC);
        redisClient.del(ITEM_INFO + ":" + itemId + ":" + PARAM);
        return i;
    }

    /**
     * 商品回显
     * @param itemId
     * @return
     */
    @Override
    public Map<String, Object> preUpdateItem(Long itemId) {
        Map<String, Object> map = new HashMap<>();

        //根据商品ID查询商品
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        map.put("item",tbItem);

        //根据商品ID查询商品描述
        TbItemDesc tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc",tbItemDesc.getItemDesc());

        //根据商品ID查询商品类目
        TbItemCat tbItemCat = tbItemCatMapper.selectByPrimaryKey(tbItem.getCid());
        map.put("itemCat",tbItemCat.getName());

        //根据商品ID查询商品规格参数
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(itemId);
        List<TbItemParamItem> tbItemParamItems = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        if (tbItemParamItems.size() > 0 && tbItemParamItems != null){
            map.put("itemParamItem",tbItemParamItems.get(0).getParamData());
        }
        return map;
    }

    /**
     * 商品修改
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer updateTbItem(TbItem tbItem, String desc, String itemParams) {

        tbItem.setStatus((byte) 1);
        tbItem.setUpdated(new Date());
        tbItem.setCreated(new Date());
        int num1 = tbItemMapper.updateByPrimaryKeySelective(tbItem);


        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setCreated(new Date());
        tbItemDesc.setUpdated(new Date());
        tbItemDesc.setItemDesc(desc);
        int num2 = tbItemDescMapper.updateByPrimaryKeySelective(tbItemDesc);


        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setCreated(new Date());
        tbItemParamItem.setParamData(itemParams);
        tbItemParamItem.setUpdated(new Date());

        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(tbItem.getId());

        int num3 = tbItemParamItemMapper.updateByExampleSelective(tbItemParamItem, tbItemParamItemExample);

        redisClient.del(ITEM_INFO + ":" + tbItem.getId() + ":" + BASE);
        redisClient.del(ITEM_INFO + ":" + tbItem.getId() + ":" + DESC);
        redisClient.del(ITEM_INFO + ":" + tbItem.getId() + ":" + PARAM);

        return num1+num2+num3;
    }

    /**
     * 根据商品 ID 查询商品介绍
     * @param itemId
     * @return
     */
    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId) {
        //1、先查询redis,如果有直接返回
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(ITEM_INFO + ":" + itemId + ":" + DESC);
        if (tbItemDesc != null){
            return tbItemDesc;
        }
        /*****************解决缓存击穿***************/
        if (redisClient.setnx(SETNX_DESC_LOCK_KEY+":"+itemId,itemId,30L)){
            //2、再查询mysql,并把查询结果缓存到redis,并设置失效时间
            TbItemDescExample tbItemDescExample = new TbItemDescExample();
            TbItemDescExample.Criteria criteria = tbItemDescExample.createCriteria();
            criteria.andItemIdEqualTo(itemId);
            List<TbItemDesc> tbItemDescList = tbItemDescMapper.selectByExampleWithBLOBs(tbItemDescExample);
            if (tbItemDescList != null && tbItemDescList.size() > 0){
                tbItemDesc = tbItemDescList.get(0);
                //把数据保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,tbItemDesc);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,ITEM_INFO_EXPIRE);
            }else {
                /********************解决缓存穿透************************/
                //把空对象保存到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC,null);
                //设置缓存的有效期
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,30L);
            }
            redisClient.del(SETNX_DESC_LOCK_KEY+":"+itemId);
            return tbItemDesc;
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemDescByItemId(itemId);
        }
    }

    @Override
    public Integer updateTbItemByOrderId(String orderId) {
        TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = tbOrderItemExample.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<TbOrderItem> tbOrderItemList = tbOrderItemMapper.selectByExample(tbOrderItemExample);
        int result = 0;
        for (int i = 0; i < tbOrderItemList.size(); i++) {
            TbOrderItem tbOrderItem = tbOrderItemList.get(i);
            TbItem tbItem = tbItemMapper.selectByPrimaryKey(Long.valueOf(tbOrderItem.getItemId()));
            tbItem.setNum(tbItem.getNum()-tbOrderItem.getNum());
            result += tbItemMapper.updateByPrimaryKeySelective(tbItem);
        }
        return result;
    }
}
