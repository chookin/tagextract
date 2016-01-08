package cmri.tagbase.video;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.video.heshijie.CategoryPageProcessor;
import cmri.tagbase.video.heshijie.VideoPageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 6/5/15.
 */
public class HeshijieCollect extends VideoCollect {
    @Override
    public String getSiteName() {
        return SiteName.Heshijie;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return VideoPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    public static void main(String[] args){
        new HeshijieCollect().setArgs(args).action();
    }
}
