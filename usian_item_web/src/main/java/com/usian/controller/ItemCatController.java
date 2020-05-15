package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItemCat;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/itemCategory")
public class ItemCatController {

    @Autowired
    private ItemServiceFeign itemServiceFeign;

    @RequestMapping("/selectItemCategoryByParentId")
    public Result selectItemCategoryByParentIdv(@RequestParam(defaultValue = "0") Long id){
        List<TbItemCat> tbItemCatList = itemServiceFeign.selectItemCategoryByParentIdv(id);
        if (tbItemCatList != null && tbItemCatList.size() > 0){
            return Result.ok(tbItemCatList);
        }
        return Result.error("查无结果");
    }

}
