package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentExample;
import com.usian.redis.RedisClient;
import com.usian.utils.AdNode;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper tbContentMapper;

    @Value("${PORTAL_AD_KEY}")
    private String PORTAL_AD_KEY;

    @Autowired
    private RedisClient redisClient;


    @Override
    public PageResult selectTbContentAllByCategoryId(Integer page, Integer rows, Long categoryId) {
        PageHelper.startPage(page,rows);
        TbContentExample tbContentExample = new TbContentExample();
        tbContentExample.setOrderByClause("updated DESC");
        TbContentExample.Criteria criteria = tbContentExample.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<TbContent> tbContentList = tbContentMapper.selectByExampleWithBLOBs(tbContentExample);
        PageInfo<TbContent> pageInfo = new PageInfo<TbContent>(tbContentList);
        PageResult pageResult = new PageResult();
        pageResult.setResult(pageInfo.getList());
        pageResult.setPageIndex(pageInfo.getPageNum());
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));
        return pageResult;
    }

    @Override
    public Integer insertTbContent(TbContent tbContent) {
        tbContent.setCreated(new Date());
        tbContent.setUpdated(new Date());
        int num = tbContentMapper.insertSelective(tbContent);
        //缓存同步
        redisClient.hdel(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        return num;
    }

    @Override
    public Integer deleteContentByIds(Long ids) {
        int num = tbContentMapper.deleteByPrimaryKey(ids);
        //缓存同步
        redisClient.hdel(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        return num;
    }

    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;
    @Value("${AD_HEIGHT}")
    private Integer AD_HEIGHT;
    @Value("${AD_WIDTH}")
    private Integer AD_WIDTH;
    @Value("${AD_HEIGHTB}")
    private Integer AD_HEIGHTB;
    @Value("${AD_WIDTHB}")
    private Integer AD_WIDTHB;

    @Override
    public List<AdNode> selectFrontendContentByAD() {
        //查询缓存
        List<AdNode> adNodeListRedis = (List<AdNode>) redisClient.hget(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        if (adNodeListRedis != null){
            return adNodeListRedis;
        }

        TbContentExample tbContentExample = new TbContentExample();
        TbContentExample.Criteria criteria = tbContentExample.createCriteria();
        criteria.andCategoryIdEqualTo(AD_CATEGORY_ID);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(tbContentExample);
        List<AdNode> adNodeList = new ArrayList<AdNode>();
        for (int i = 0; i < tbContentList.size(); i++) {
            TbContent tbContent =  tbContentList.get(i);
            AdNode adNode = new AdNode();
            adNode.setHeight(AD_HEIGHT);
            adNode.setHeightB(AD_HEIGHTB);
            adNode.setWidth(AD_WIDTH);
            adNode.setWidthB(AD_WIDTHB);
            adNode.setSrc(tbContent.getPic());
            adNode.setHref(tbContent.getUrl());
            adNode.setSrcB(tbContent.getPic2());
            adNodeList.add(adNode);
    }
        //添加到缓存
        redisClient.hset(PORTAL_AD_KEY,AD_CATEGORY_ID.toString(),adNodeList);
        return adNodeList;
    }
}
