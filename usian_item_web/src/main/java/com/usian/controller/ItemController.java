package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backend/item")
@Api("商品管理接口")  //修饰整个类，描述Controller的作用
public class ItemController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @RequestMapping(value = "/selectItemInfo",method = RequestMethod.POST)
    @ApiOperation(value = "查询商品基本信息",notes = "根据商品itemId查询该商品的基本信息")  //描述一个类的一个方法，或者说一个接口
    @ApiImplicitParam(name = "itemId",type = "Long",value = "商品id")  //单个参数描述
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
    @GetMapping("/selectTbItemAllByPage")
    @ApiOperation(value = "查询商品并分页",notes = "分页查询信息，默认为第一页，每页显示两条")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",type = "Integer",value = "页码",defaultValue = "1"),
            @ApiImplicitParam(name = "rows",type = "Integer",value = "每页多少条",defaultValue = "2")
    })
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
    @PostMapping("/insertTbItem")
    @ApiOperation(value = "添加商品",notes = "添加商品及描述和规格参数信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "desc",type = "String",value = "商品描述信息"),
            @ApiImplicitParam(name = "itemParams",type = "String",value = "商品规格参数信息")
    })
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
