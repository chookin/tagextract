package cmri.tagbase.base;

import cmri.etl.common.Request;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.pipeline.MongoPipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.utils.configuration.ConfigManager;
import cmri.utils.configuration.OptionsPack;

import java.util.Collection;

/**
 * Created by zhuyin on 3/24/15.
 */
public interface CategoryCollect {
    default boolean collectCategories(OptionsPack optionsPack) {
        if (!optionsPack.process("collect-categories")) {
            return false;
        }
        collectCategoriesOfSite();
        return true;
    }

    default void collectCategoriesOfSite() {
        new SpiderAdapter(getSiteName() + "/category")
                .addRequest(getSeedCategoryRequests())
                .addPipeline(new FilePipeline(), new MongoPipeline(getSiteName()))
                .userAgent(getCategoryUserAgent())
                .run();
    }

    String getSiteName();

    Collection<Request> getSeedCategoryRequests();

    default String getCategoryUserAgent(){
        return ConfigManager.get("web.userAgent");
    }
}
