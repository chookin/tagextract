package cmri.tagbase.video;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.video.aiqiyi.TagPageProcessor;
import cmri.tagbase.video.aiqiyi.VideoPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;

import java.util.Collection;

/**
 * Created by zhuyin on 5/13/15.
 */
public class AiqiyiCollect extends VideoCollect {
    @Override
    public String getSiteName() {
        return SiteName.Aiqiyi;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return VideoPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    public static void main(String[] args){
        new AiqiyiCollect().setArgs(args).action();
    }
}
