package cmri.tagbase.video;

import cmri.etl.common.Request;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.SiteName;
import cmri.tagbase.video.youku.CategoryPageProcessor;
import cmri.tagbase.video.youku.VideoPageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 4/8/15.
 */
public class YoukuCollect extends VideoCollect {
    @Override
    public String getSiteName() {
        return SiteName.Youku;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return VideoPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    public static void main(String[] args) {
        new YoukuCollect().setArgs(args).action();
    }
}
