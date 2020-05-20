package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItemParam;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/itemParam")
public class ItemParamController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    /**
     * 根据商品分类ID查询规格参数模板
     * @param itemCatId
     * @return
     */
    @RequestMapping("/selectItemParamByItemCatId/{itemCatId}")
    public Result selectItemParamByItemCatId(@PathVariable Long itemCatId){
        TbItemParam tbItemParam = itemServiceFeign.selectItemParamByItemCatId(itemCatId);
        if (tbItemParam != null){
            return Result.ok(tbItemParam);
        }
        return Result.error("查无结果");
    }

    /**
     * 分页查询所有商品规格模板
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/selectItemParamAll")
    public Result selectItemParamAll(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "3") Integer rows){
        PageResult pageResult = itemServiceFeign.selectItemParamAll(page,rows);
        if (pageResult.getResult() != null && pageResult.getResult().size() > 0){
            return Result.ok(pageResult);
        }
        return Result.error("查无结果");
    }

    /**
     * 删除商品规格模板
     * @param id
     * @return
     */
    @RequestMapping("/deleteItemParamById")
    public Result deleteItemParamById(Long id){
        Integer i = itemServiceFeign.deleteItemParamById(id);
        if (i == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

}
