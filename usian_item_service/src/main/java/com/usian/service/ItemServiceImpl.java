package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbItemCatMapper;
import com.usian.mapper.TbItemDescMapper;
import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.pojo.*;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public TbItem selectItemInfo(Long itemId) {
        return tbItemMapper.selectByPrimaryKey(itemId);
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
        pageResult.setTotalPage(tbItemPageInfo.getTotal());
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

        return num1+num2+num3;
    }
}
