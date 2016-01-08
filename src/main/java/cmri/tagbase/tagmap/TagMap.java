package cmri.tagbase.tagmap;

import cmri.tagbase.orm.dao.CategoryDAO;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.TagMapEntity;
import cmri.tagbase.orm.repository.TagMapRepository;
import cmri.tagbase.utils.SpringHelper;
import com.google.common.collect.Sets;
import com.mongodb.QueryBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhuyin on 4/15/15.
 */
@Service
public class TagMap {
    private static final Logger LOG = Logger.getLogger(TagMap.class);

    private Map<String, Set<CategoryEntity>> collectedCategories = new ConcurrentHashMap<>();

    @Autowired
    private TagMapRepository tagMapRepository;

    public static TagMap getInstance(){
        return (TagMap) SpringHelper.getAppContext().getBean("tagMap");
    }

    /**
     * Note: one category can have more than one tag, and one tag can have more than one category.
     * @param sites which site to query. If null or empty, query all.
     * @return Collection format is {site_name: {category_key: [tag_name]}}
     */
    public Map<String, Map<CategoryIdent, Set<String>>> getCategoriesMap(Set<String> sites) {
        if (sites == null || sites.isEmpty()) {
            sites = Sets.newHashSet(tagMapRepository.findDistinctSite());
        }

        Map<String, Map<CategoryIdent, Set<String>>> myMap = new HashMap<>();
        for (String site : sites) {
            Map<CategoryIdent, Set<String>> categoryIdentTagsMap = new HashMap<>(); // {categoryIdent: [tag]}一个分类可以具有多个标签
            myMap.put(site, categoryIdentTagsMap);

            /**
             1，获取该网站的分类-标签映射配置；
             2，获取该网址已爬取到哪些分类；
             3，遍历分类-标签映射配置，检查该配置是否适用于那些爬取到的分类，如果适用，则给相应的叶子分类打标签（为啥只给叶子分类打标签，因为，在系统存储中具体的视频、音乐、商品等物品都是直接对应的叶子分类，每个物品都对应一个叶子分类名），并把叶子分类key和标签的映射加入到该网站的标签映射中。
             */
            Map<CategoryIdent, Set<String>> tagmap = generateIdentMapForTagMap(site); // 分类标识-标签的映射
            Set<CategoryEntity> categories = getCollectedCategories(site);
            addToIdentTagsMap(categories, tagmap, categoryIdentTagsMap);

            // 对于优酷，二级分类“综艺/时尚”打了“视频/综艺/时尚”标签，而一级分类“时尚”没有被打标签
            // 进一步对遗留的“时尚”打标签么? 算了，不再处理
            //Set<CategoryEntity> left = getUnmatchedCategories(categories, categoryIdentTagsMap.keySet());
            //addToIdentTagsMap(left, tagmap, categoryIdentTagsMap);
        }
        return myMap;
    }

    private Set<CategoryEntity> getUnmatchedCategories(Collection<CategoryEntity> categories, Collection<CategoryIdent> idents){
        Set<CategoryEntity> rst = new HashSet<>();
        for(CategoryEntity category: categories){
            if(idents.contains(new CategoryIdent(category))){
                continue;
            }
            rst.add(category);
        }
        return rst;
    }
    /**
     * 根据所配置的"分类-标签的映射记录"生成"分类key"和"标签映射记录"的map, 具有同样分类key的标签可以有多个
     * @param site site name
     * @return map of {ident: [TagMapEntity]}
     */
    private Map<CategoryIdent, Set<String>> generateIdentMapForTagMap(String site){
        Map<CategoryIdent, Set<String>> map = new HashMap<>();

        // query tag category mapping of this site
        List<TagMapEntity> recs = tagMapRepository.findBySite(site);
        LOG.info("find " + recs.size() + " tag map records for site "+ site);
        for(TagMapEntity rec : recs){
            addToIdentTagsMap(new CategoryIdent(rec), rec.getTag(), map);
        }
        return map;
    }

    private void addToIdentTagsMap(Set<CategoryEntity> categories, Map<CategoryIdent, Set<String>> tagmap, Map<CategoryIdent, Set<String>> categoryIdentTagsMap) {
        for (Map.Entry<CategoryIdent, Set<String>> entry : tagmap.entrySet()) {
            // 先tagmap遍历，而不是遍历categories，这是因为，若遍历categories,那么需要先尝试得到能匹配的ident，之后，再继续尝试获得最匹配的tag。相比遍历tagmap，实现起来会更复杂些。
            CategoryIdent ident = entry.getKey();
            Set<String> tagMapEntities = entry.getValue();
            for (String tag : tagMapEntities) {
                List<CategoryEntity> leaves = getMatchedCategories(ident, categories);
                if (leaves.isEmpty()) {
                    LOG.warn("cannot find category of " + ident);
                    /** 如果在所采集的分类中找不到具有该分类key的分类，那么把该分类及其所映射的标签加入到映射中。例如，在歌手名没有作为分类来采集的情况下，在打标签时，还是需要给具有指定歌手的歌曲打该歌手的标签
                    */
                     addToIdentTagsMap(ident, tag, categoryIdentTagsMap);
                } else {
                    for (CategoryEntity leaf : leaves)
                        addToIdentTagsMap(new CategoryIdent(leaf), tag, categoryIdentTagsMap);
                }
            }
        }
    }

    private void addToIdentTagsMap(CategoryIdent ident, String tag, Map<CategoryIdent, Set<String>> categoryKeyTags){
        Set<String> originTags = categoryKeyTags.get(ident); // 一个分类可以具有多个标签
        if (originTags == null) {
            originTags = new HashSet<>();
            categoryKeyTags.put(ident, originTags);
        }
        originTags.add(tag);
    }

    /**
     * Load collected categories of site, if cached, return; else load from db.
     * @return collected categories of {@code site}.
     */
    private Set<CategoryEntity> getCollectedCategories(String site){
        Set<CategoryEntity> categories = collectedCategories.get(site);
        if(categories != null){
            return categories;
        }

        // if not find, then query from db.
        categories = new HashSet<>();
        CategoryDAO dao = CategoryDAO.getInstance();
        try{
            List<CategoryEntity> list = dao.find(new QueryBuilder().put("site").is(site).get());
            for(CategoryEntity category: list) {
                categories.add(category);
            }
            updateCategoryParent(categories);
            collectedCategories.put(site, categories);
            return categories;
        }finally {
            dao.close();
        }
    }

    private void updateCategoryParent(Collection<CategoryEntity> categories){
        Map<String, CategoryEntity> map = new HashMap<>();
        for(CategoryEntity category: categories){
            String key = category.getSite()+"_"+category.getName()+"_"+category.getCode();
            map.put(key, category);
        }
        for(CategoryEntity category: categories){
            if(category.getParent() == null){
                continue;
            }
            String parentKey = category.getSite()+"_"+category.getParent().getName()+"_"+category.getParent().getCode();
            CategoryEntity parent = map.get(parentKey);
            category.setParent(parent);
        }
    }
    /**
     * 查找具有指定分类标识的分类。
     * <ol>分类判断分4种情况, 按照先后次序依次判断，如果当前判断能找到匹配的，则直接返回找到的分类；否则，继续下一个匹配：
         <li>严格匹配, 提取那些完全匹配的</li>
         <li>如果不存在完全匹配的，那么查找是否存在“parent”不需要一致的, 即根据分类名和分类id来判断，优先忽略“parent”的原因是“parent”是自动解析得到的</li>
         <li>有些“分类-标签”映射，存在分类名相同的情况，但是却没有配置分类的id，此时，根据自动解析得到的“parent”和分类名做判断</li>
         <li>如果以上情况都提取不到，那么只根据分类名做判断</li>
     * </ol>
     * @param ident 要查找的分类标识
     * @param categories 从该分类集合中查找出分类
     * @return 查找到的分类集合
     */
    private List<CategoryEntity> getMatchedCategories(CategoryIdent ident, Set<CategoryEntity> categories){
        List<CategoryEntity> leaves = new ArrayList<>();
        for(CategoryEntity category: categories){
            CategoryIdent my = new CategoryIdent(category);
            if(my.equals(ident)){
                leaves.addAll(getFamily(category));
            }
        }
        if(leaves.size() > 0){
            return leaves;
        }
        for(CategoryEntity category: categories){
            CategoryIdent my = new CategoryIdent(category);
            if(my.equalsIgnoreParentName(ident)){
                leaves.addAll(getFamily(category));
            }
        }
        if(leaves.size() > 0){
            return leaves;
        }
        for(CategoryEntity category: categories){
            CategoryIdent my = new CategoryIdent(category);
            if(my.equalsIgnoreCode(ident)){
                leaves.addAll(getFamily(category));
            }
        }
        if(leaves.size() > 0){
            return leaves;
        }
        for(CategoryEntity category: categories){
            CategoryIdent my = new CategoryIdent(category);
            if(my.equalsIgnoreCodeParentName(ident)){
                leaves.addAll(getFamily(category));
            }
        }
        return leaves;
    }

    /**
     * @return collection include itself and all of its descendant.
     */
    private Collection<CategoryEntity> getFamily(CategoryEntity category){
        if(category.getChildren().isEmpty()){
            return Collections.singletonList(category);
        }
        Collection<CategoryEntity> rst = new ArrayList<>();
        rst.add(category);
        for(CategoryEntity entity: category.getChildren()){
            rst.addAll(getFamily(entity));
        }
        return rst;
    }
}
