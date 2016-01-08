package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.music.baidu.MusicPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.music.baidu.TagPageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 3/31/15.
 */
public class BaiduMusicCollect extends MusicCollect {

    @Override
    public Request getRequest(CategoryEntity category) {
        return MusicPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    @Override
    public String getSiteName() {
        return SiteName.BaiduMusic;
    }

    public static void main(String[] args){
        new BaiduMusicCollect().setArgs(args).action();
    }
}
