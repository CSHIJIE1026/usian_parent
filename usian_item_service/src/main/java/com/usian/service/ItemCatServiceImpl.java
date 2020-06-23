package com.usian.service;

import com.usian.mapper.TbItemCatMapper;
import com.usian.pojo.TbItemCat;
import com.usian.pojo.TbItemCatExample;
import com.usian.redis.RedisClient;
import com.usian.utils.CatNode;
import com.usian.utils.CatResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Value("${PORTAL_CATRESULT_KEY}")
    private String PORTAL_CATRESULT_KEY;

    @Autowired
    private RedisClient redisClient;

    /**
     * 根据分类 ID 查询子节点
     * @param id
     * @return
     */
    @Override
    public List<TbItemCat> selectItemCategoryByParentId(Long id) {
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andStatusEqualTo(1);
        criteria.andParentIdEqualTo(id);
        List<TbItemCat> list = tbItemCatMapper.selectByExample(tbItemCatExample);
        if (id != 0){
            int a = 6 / 0;
        }
        return list;
    }

    @Override
    public CatResult selectItemCategoryAll() {
        //查询缓存
        CatResult catResultRedis = (CatResult) redisClient.get(PORTAL_CATRESULT_KEY);
        if (catResultRedis != null){
            System.out.println("从数据库查询");
            return catResultRedis;
        }
        //因为一级菜单有子菜单，子菜单有子子菜单，所以要递归调用
        CatResult catResult = new CatResult();
        catResult.setData(getList(0L));

        //添加到缓存
        redisClient.set(PORTAL_CATRESULT_KEY,catResult);
        System.out.println("从redis查询");
        return catResult;
    }

    private List<?> getList(Long parentId){
        //1、查询商品类目列表
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbItemCat> tbItemCatList = tbItemCatMapper.selectByExample(tbItemCatExample);
        //2、拼接CatNode
        List catNodeList = new ArrayList();
        int count = 0;
        for (int i = 0; i < tbItemCatList.size(); i++) {
            TbItemCat tbItemCat =  tbItemCatList.get(i);
            //2.1、该类目是父节点
            if (tbItemCat.getIsParent()){
                CatNode catNode = new CatNode();
                catNode.setName(tbItemCat.getName());
                catNode.setItem(getList(tbItemCat.getId()));//递归调用
                //把父节点装到集合中：n:"",i:[]
                catNodeList.add(catNode);
                count++;
                if (count == 18){
                    break;
                }
            }else {
                //2.2、该节点不是父节点，直接报类目名称添加到catNodeList
                catNodeList.add(tbItemCat.getName());
            }
        }
        return catNodeList;
    }
}
