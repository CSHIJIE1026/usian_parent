package com.usian.feign;

import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "usian-content-service")
public interface ContentServiceFeign {

    /**
     * 内容分类查询
     * @param id
     * @return
     */
    @RequestMapping("/service/contentCategory/selectContentCategoryByParentId")
    List<TbContentCategory> selectContentCategoryByParentId(@RequestParam Long id);

    /**
     * 添加分类内容
     * @param tbContentCategory
     * @return
     */
    @RequestMapping("/service/contentCategory/insertContentCategory")
    Integer insertContentCategory(TbContentCategory tbContentCategory);

    /**
     * 修改分类内容
     * @param tbContentCategory
     * @return
     */
    @RequestMapping("/service/contentCategory/updateContentCategory")
    Integer updateContentCategory(TbContentCategory tbContentCategory);

    /**
     * 删除分类内容
     * @param categoryId
     * @return
     */
    @RequestMapping("/service/contentCategory/deleteContentCategoryById")
    Integer deleteContentCategoryById(@RequestParam Long categoryId);

    /**
     * 分页内容管理
     * @param page
     * @param rows
     * @param categoryId
     * @return
     */
    @RequestMapping("/service/content/selectTbContentAllByCategoryId")
    PageResult selectTbContentAllByCategoryId(@RequestParam Integer page,@RequestParam Integer rows,@RequestParam Long categoryId);

    /**
     * 内容管理添加
     * @param tbContent
     * @return
     */
    @RequestMapping("/service/content/insertTbContent")
    Integer insertTbContent(TbContent tbContent);

    /**
     * 内容管理删除
     * @param ids
     * @return
     */
    @RequestMapping("/service/content/deleteContentByIds")
    Integer deleteContentByIds(@RequestParam Long ids);
}
