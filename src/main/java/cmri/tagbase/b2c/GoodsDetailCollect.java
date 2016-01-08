package cmri.tagbase.b2c;

import cmri.etl.common.Request;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.pipeline.MongoPipeline;
import cmri.etl.spider.Spider;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.dao.CategoryDAO;
import cmri.tagbase.orm.dao.KindDAO;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.OptionsPack;
import cmri.utils.lang.JsonHelper;
import cmri.utils.lang.MapAdapter;
import cmri.utils.lang.PinyinUtils;
import com.mongodb.QueryBuilder;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyin on 3/24/15.
 */
interface GoodsDetailCollect {
    default boolean collectGoodsDetail(OptionsPack optionsPack) {
        if (!optionsPack.process("collect-goodsDetail")) {
            return false;
        }
        return collectGoodsDetailOfCategories(optionsPack) || collectGoodsDetailOfSite(optionsPack);
    }

    default boolean collectGoodsDetailOfSite(OptionsPack optionsPack) {
        QueryBuilder queryBuilder = new QueryBuilder()
                .put("site").is(getSiteName())
                .put("leaf").is(true);
        boolean isAll = optionsPack.exists("--all");
        if (!isAll) {
            queryBuilder.put("tag").notEquals(null);
        }
        List<CategoryEntity> categories = CategoryDAO.getInstance().find(queryBuilder.get());
        getLogger().trace("get " + categories.size() + " categories of " + getSiteName());
        collectGoodsDetailOfCategories(categories);
        return true;
    }

    /**
     * <ul>
     * <li>--category={\"site\":\"jd\",\"code\":\"1320-1585-9434\"}</li>
     * </ul>
     */
    default boolean collectGoodsDetailOfCategories(OptionsPack optionsPack) {
        String option = "--category";
        if (optionsPack.get(option) == null) {
            return false;
        }
        String paras = optionsPack.get(option);
        Map<String, Object> kv = JsonHelper.parseStringObjectMap(paras);
        List<CategoryEntity> categories = CategoryDAO.getInstance().find(kv);
        collectGoodsDetailOfCategories(categories);
        return true;
    }

    default long collectGoodsDetailOfCategories(Collection<CategoryEntity> entities) {
        long count = 0;
        KindDAO dao = new KindDAO("goods");
        try {
            for (CategoryEntity category : entities) {
                getLogger().info("process category " + category);
                QueryBuilder queryBuilder = new QueryBuilder()
                        .put("site").is(category.getSite())
                        .put("category").is(category.getName())
                        .put("c_code").is(category.getCode())
                        .put("properties.品牌").is(null);
                List<KindEntity> goods = dao.find(queryBuilder.get());
                String name = "goodsDetail" + "/" + category.getSite() + "/" + PinyinUtils.getPinYin(category.getName(), "-");
                if(category.getCode() != null){
                    name = name + "_" + category.getCode();
                }
                Spider spider = new SpiderAdapter(name,  new MapAdapter<>("scheduler", "cmri.etl.scheduler.RedisPriorityScheduler").get())
                        .addPipeline(new FilePipeline(), new MongoPipeline(category))
                        ;
                for (KindEntity good : goods) {
                    spider.addRequest(getRequest(good));
                }
                spider.run();
            }
            return count;
        } finally {
            dao.close();
        }
    }

    String getSiteName();

    Logger getLogger();

    Request getRequest(KindEntity good);
}
