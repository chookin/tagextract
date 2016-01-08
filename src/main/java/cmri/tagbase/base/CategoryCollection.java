package cmri.tagbase.base;

import cmri.etl.common.Request;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.pipeline.MongoPipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.etl.spider.SpiderListener;

import java.util.Collection;
import java.util.Map;

/**
 * 爬取分类信息
 *
 * Created by zhuyin on 8/24/15.
 */
public interface CategoryCollection {
    default void collectCategories(Map<String, String> paras, SpiderListener... listeners) {
        collectCategoriesOfSite(paras, listeners);
    }

    default void collectCategoriesOfSite(Map<String, String> paras, SpiderListener... listeners) {
        new SpiderAdapter(getSiteName() + "/category", paras)
                .addRequest(getSeedCategoryRequests())
                .addPipeline(new FilePipeline(), new MongoPipeline(getSiteName()))
                .addListener(listeners)
                .run();
    }

    String getSiteName();

    Collection<Request> getSeedCategoryRequests();
}
