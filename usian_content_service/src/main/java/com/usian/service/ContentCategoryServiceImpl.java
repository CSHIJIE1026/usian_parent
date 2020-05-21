package com.usian.service;

import com.usian.mapper.TbContentCategoryMapper;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;

    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbContentCategory> tbContentCategoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        return tbContentCategoryList;
    }

    @Override
    public Integer insertContentCategory(TbContentCategory tbContentCategory) {
        //1、添加内容分类，补齐数据
        tbContentCategory.setCreated(new Date());
        tbContentCategory.setUpdated(new Date());
        tbContentCategory.setIsParent(false);
        tbContentCategory.setStatus(1);
        tbContentCategory.setSortOrder(1);
        //2、插入数据
        int num = tbContentCategoryMapper.insertSelective(tbContentCategory);
        //3、根据当前主键，查询当前节点的父节点
        TbContentCategory contentCategory = tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());
        //4、判断当前父节点是否是子节点
        if (!contentCategory.getIsParent()){ //如果不是子节点
            contentCategory.setIsParent(true);  //改为true
            contentCategory.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
        }
        return num;
    }

    @Override
    public Integer updateContentCategory(TbContentCategory tbContentCategory) {
        tbContentCategory.setUpdated(new Date());
        int num = tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
        return num;
    }

    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        //查询当前节点
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        //父节点 不允许删除
        if (tbContentCategory.getIsParent() == true){
            return 0;
        }
        //不是父节点
        tbContentCategoryMapper.deleteByPrimaryKey(categoryId);
        //查询当前节点的兄弟节点
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(categoryId);
        List<TbContentCategory> tbContentCategoryList = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        //删除之后如果父节点没有孩子，则修改isParent为false
        if (tbContentCategoryList.size() == 0){
            TbContentCategory parentTbContentCategory = new TbContentCategory();
            parentTbContentCategory.setUpdated(new Date());
            parentTbContentCategory.setIsParent(false);
            parentTbContentCategory.setParentId(tbContentCategory.getParentId());
            tbContentCategoryMapper.updateByPrimaryKeySelective(parentTbContentCategory);
        }
        return 200;
    }
}
