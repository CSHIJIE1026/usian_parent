package com.usian.controller;

import com.usian.feign.ContentServiceFeign;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/content")
public class ContentCategoryController {

    @Autowired
    private ContentServiceFeign contentServiceFeign;

    /**
     * 内容分类查询
     * @param id
     * @return
     */
    @RequestMapping("/selectContentCategoryByParentId")
    public Result selectContentCategoryByParentId(@RequestParam(defaultValue = "0") Long id){
        List<TbContentCategory> tbContentCategoryList = contentServiceFeign.selectContentCategoryByParentId(id);
        if (tbContentCategoryList != null && tbContentCategoryList.size() > 0){
            return Result.ok(tbContentCategoryList);
        }
        return Result.error("查无结果");
    }

    /**
     * 添加分类内容
     * @param tbContentCategory
     * @return
     */
    @RequestMapping("/insertContentCategory")
    public Result insertContentCategory(TbContentCategory tbContentCategory){
        Integer num = contentServiceFeign.insertContentCategory(tbContentCategory);
        if (num != null){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 修改分类内容
     * @param tbContentCategory
     * @return
     */
    @RequestMapping("/updateContentCategory")
    public Result updateContentCategory(TbContentCategory tbContentCategory){
        Integer num = contentServiceFeign.updateContentCategory(tbContentCategory);
        if (num != null){
            return Result.ok();
        }
        return Result.error("修改失败");
    }

    /**
     * 删除分类内容
     * @param categoryId
     * @return
     */
    @RequestMapping("/deleteContentCategoryById")
    public Result deleteContentCategoryById(Long categoryId){
        Integer num = contentServiceFeign.deleteContentCategoryById(categoryId);
        if (num == 200){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

}
