package com.usian.service;

import com.usian.pojo.TbContentCategory;

import java.util.List;

public interface ContentCategoryService {
    List<TbContentCategory> selectContentCategoryByParentId(Long id);

    Integer insertContentCategory(TbContentCategory tbContentCategory);

    Integer updateContentCategory(TbContentCategory tbContentCategory);

    Integer deleteContentCategoryById(Long categoryId);
}
