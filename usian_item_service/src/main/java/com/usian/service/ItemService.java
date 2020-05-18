package com.usian.service;

import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;

public interface ItemService {

    public TbItem selectItemInfo(Long itemId);

    PageResult selectTbItemAllByPage(Integer page, Integer rows);

    Integer insertTbItem(TbItem tbItem, String desc, String itemParams);

    Integer deleteItemById(Long itemId);
}
