package com.usian.feign;

import com.usian.fallback.ItemServiceFallback;
import com.usian.pojo.*;
import com.usian.utils.CatResult;
import com.usian.utils.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "usian-item-service",fallbackFactory = ItemServiceFallback.class)
public interface ItemServiceFeign {

    /**
     *查询所有商品
     * @param itemId
     * @return
     */
    @RequestMapping("/service/item/selectItemInfo")
    TbItem selectItemInfo(@RequestParam Long itemId);

    /**
     * 分页查询商品列表
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/service/item/selectTbItemAllByPage")
    PageResult selectTbItemAllByPage(@RequestParam("page") Integer page, @RequestParam("rows") Integer rows);

    /**
     * 根据ID查询商品类目
     * @param id
     * @return
     */
    @RequestMapping("/service/itemCat/selectItemCategoryByParentId")
    List<TbItemCat> selectItemCategoryByParentIdv(@RequestParam Long id);

    /**
     * 查询商品规格模板
     * @param itemCatId
     * @return
     */
    @RequestMapping("/service/itemParam/selectItemParamByItemCatId/{itemCatId}")
    TbItemParam selectItemParamByItemCatId(@PathVariable Long itemCatId);

    /**
     * 添加商品
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/service/item/insertTbItem")
    Integer insertTbItem(TbItem tbItem,@RequestParam String desc,@RequestParam String itemParams);

    /**
     * 删除商品
     * @param itemId
     * @return
     */
    @RequestMapping("/service/item/deleteItemById")
    Integer deleteItemById(@RequestParam Long itemId);

    /**
     * 分页查询所有商品规格模板
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/service/itemParam/selectItemParamAll")
    PageResult selectItemParamAll(@RequestParam Integer page, @RequestParam Integer rows);

    /**
     * 删除商品规格模板
     * @param id
     * @return
     */
    @RequestMapping("/service/itemParam/deleteItemParamById")
    Integer deleteItemParamById(@RequestParam Long id);

    @RequestMapping("/service/itemParam/insertItemParam")
    Integer insertItemParam(@RequestParam Long itemCatId, @RequestParam String paramData);

    @RequestMapping("/service/itemCat/selectItemCategoryAll")
    CatResult selectItemCategoryAll();

    @RequestMapping("/service/item/preUpdateItem")
    Map<String, Object> preUpdateItem(@RequestParam Long itemId);

    @RequestMapping("/service/item/updateTbItem")
    Integer updateTbItem(TbItem tbItem,@RequestParam String desc,@RequestParam String itemParams);

    @RequestMapping("/service/item/selectItemDescByItemId")
    TbItemDesc selectItemDescByItemId(@RequestParam Long itemId);

    @RequestMapping("/service/itemParam/selectTbItemParamItemByItemId")
    TbItemParamItem selectTbItemParamItemByItemId(@RequestParam Long itemId);
}
