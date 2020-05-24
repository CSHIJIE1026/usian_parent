package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/backend/item")
public class ItemController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @RequestMapping("/selectItemInfo")
    public Result selectItemInfo(Long itemId){
        TbItem tbItem = itemServiceFeign.selectItemInfo(itemId);
        if (tbItem != null){
            return Result.ok(tbItem);
        }
        return Result.error("查无结果");
    }

    /**
     * 查询商品，并分页
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectTbItemAllByPage")
    public Result selectTbItemAllByPage(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "2") Integer rows){
        PageResult pageResult = itemServiceFeign.selectTbItemAllByPage(page,rows);
        if (pageResult.getResult() != null && pageResult.getResult().size() >0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/insertTbItem")
    public Result insertTbItem(TbItem tbItem,String desc,String itemParams){
        Integer result = itemServiceFeign.insertTbItem(tbItem,desc,itemParams);
        if (result == 3){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/deleteItemById")
    public Result deleteItemById(Long itemId){
        Integer i = itemServiceFeign.deleteItemById(itemId);
        if (i == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

    /**
     * 商品回显
     * @param itemId
     * @return
     */
    @RequestMapping("/preUpdateItem")
    public Result preUpdateItem(Long itemId){
        Map<String,Object> map = itemServiceFeign.preUpdateItem(itemId);
        if (map.size() > 0){
            return Result.ok(map);
        }
        return Result.error("查无结果");
    }

    /**
     * 商品修改
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/updateTbItem")
    public Result updateTbItem(TbItem tbItem,String desc,String itemParams){
        Integer num = itemServiceFeign.updateTbItem(tbItem,desc,itemParams);
        if (num == 3){
            return Result.ok();
        }
        return Result.error("修改失败");
    }

}
