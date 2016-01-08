package cmri.tagbase.anime;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.anime.dm456.AnimePageProcessor;
import cmri.tagbase.anime.dm456.TagPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;

import java.util.Collection;

/**
 * Created by zhuyin on 6/8/15.
 */
public class Dm456Collect extends AnimeCollect {
    @Override
    public String getSiteName() {
        return SiteName.dm456;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return AnimePageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    public static void main(String[] args){
        new Dm456Collect().setArgs(args).action();
    }
}
