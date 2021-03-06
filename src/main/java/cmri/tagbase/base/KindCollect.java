package cmri.tagbase.base;

import cmri.etl.common.IdItem;
import cmri.etl.common.Request;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.pipeline.MongoPipeline;
import cmri.etl.pipeline.Pipeline;
import cmri.etl.pipeline.RedisPipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.dao.CategoryDAO;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.ConfigManager;
import cmri.utils.configuration.OptionsPack;
import cmri.utils.lang.JsonHelper;
import cmri.utils.lang.PinyinUtils;
import cmri.utils.lang.SerializationHelper;
import cmri.utils.lang.TimeHelper;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by zhuyin on 3/24/15.
 */
public interface KindCollect{
    default boolean collectKinds(OptionsPack optionsPack) {
        if (!optionsPack.process("collect-kinds")) {
            return false;
        }
        return collectKindsOfCategories(optionsPack) || collectKindsOfSite(optionsPack);
    }

    /**
     * Extract and save goods of some sites. Usage examples:
     * <ul>
     *      <li>--since=1970-01-02/00:00:0</li>
     *      <li>--all</li>
     * </ul>
     */
    default boolean collectKindsOfSite(OptionsPack optionsPack) {
        Date since = new Date(0);
        String paraSince = optionsPack.get("--since");
        if (paraSince != null) {
            since = TimeHelper.parseDate(paraSince, "yyyy-MM-dd/HH:mm:ss"); // It's strange of cannot use 'T' as separator of date and time.
        }
        boolean isAll = optionsPack.get("--all") != null;
        Collection<CategoryEntity> categoryEntities = CategoryDAO.getInstance().findNotYetCrawlSince(getSiteName(), since, !isAll);
        getLogger().info("get " + categoryEntities.size() + " categories of site " + getSiteName());
        collectKindsOfCategories(categoryEntities);
        return true;
    }

    /**
     * Extract and save kinds of a category. Usage examples:
     * <ul>
     * <li>--category={\"site\":\"jd\",\"code\":\"1320-1585-9434\"}</li>
     * <li>--category={\"url\":\"http://www.amazon.cn/b/ref=dri_3_43246071?fst=as%3Aoff&rh=n%3A2127215051%2Cn%3A%212127216051%2Cn%3A2141094051%2Cn%3A43246071&bbn=2141094051&ie=UTF8&qid=1419495999&rnid=2141094051&node=43246071\"}</li>
     * </ul>
     */
    default boolean collectKindsOfCategories(OptionsPack optionsPack) {
        String option = "--category";
        String para = optionsPack.get(option);
        if(para == null){
            return false;
        }
        Map<String, Object> kv = JsonHelper.parseStringObjectMap(para);
        List<CategoryEntity> categoryEntities = CategoryDAO.getInstance().find(kv);
        collectKindsOfCategories(categoryEntities);
        return true;
    }

    default long collectKindsOfCategories(Collection<CategoryEntity> categories) {
        long count = 0;
        CategoryDAO dao = CategoryDAO.getInstance();
        try {
            for (CategoryEntity category : categories) {
                String name = category.getSite() + "/" + PinyinUtils.getPinYin(category.getName(), "-");
                if(category.getCode() != null){
                    name = name + "_" + category.getCode();
                }
                new SpiderAdapter(name)
                        .addRequest(getRequest(category))
                        .addPipeline(new FilePipeline(), getKindPipeline(category))
                        .userAgent(getKindUserAgent())
                        .run();
                category.setCrawlTime(new Date());
                dao.update(category);
            }
            return count;
        } finally {
            dao.close();
        }
    }

    String getSiteName();

    Logger getLogger();

    Request getRequest(CategoryEntity category);

    default Pipeline getKindPipeline(CategoryEntity category){
        return new RedisKindPipeline().addPipeline(new MongoPipeline(category));
    }

    default String getKindUserAgent(){
        return ConfigManager.get("web.userAgent");
    }

    /**
     * 解决同一个物品属于多个分类的情境。
     以视频为例说明，一个视频可能同时属于00年代、言情、古装等多种分类，因此，最终存储的视频记录应该包含所有的这些分类信息。为此，使用RedisKindPipeline，以提高分类信息集成的快速性和可靠性。在存储采集到的视频记录之前，先查看redis里面是否已有该记录缓存，如果有，则合并记录，并将合并后的记录先后存储于redis、mongo，若没有，则直接先后存储到redis和mongo中。
     */
    class RedisKindPipeline extends RedisPipeline<KindEntity>{
        public RedisKindPipeline() {
            super(KindEntity.class);
        }

        /**
         * 整合刚采集的物品信息和已缓存的物品信息
         * @param newOne 刚采集到的物品信息
         */
        @Override
        protected void merge(KindEntity newOne,  Jedis jedis){
            String val = jedis.get(newOne.getId());
            if (val == null)  return;
            IdItem saved = SerializationHelper.deserialize(val);
            if(!(saved instanceof KindEntity)){
                return;
            }

            KindEntity savedKind = (KindEntity) saved;
            Set<String> keywords = (Set<String>) savedKind.get("keywords");
            try {
                if (keywords == null) {
                    keywords = new HashSet<>();
                    keywords.add(savedKind.getCategory().getName());
                }
                keywords.add(newOne.getCategory().getName());
                newOne.set("keywords", keywords);
            } catch (Exception e) {
                getLogger().error("Fail to merge " + newOne + " and " + saved, e);
            }
        }
    }
}
