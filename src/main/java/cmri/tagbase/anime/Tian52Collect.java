package cmri.tagbase.anime;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.anime.tian52.AnimePageProcessor;
import cmri.tagbase.anime.tian52.TagPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;

import java.util.Collection;

/**
 * Created by zhuyin on 6/8/15.
 */
public class Tian52Collect extends AnimeCollect {
    @Override
    public String getSiteName() {
        return SiteName.Tian52;
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
        new Tian52Collect().setArgs(args).action();
    }
}
