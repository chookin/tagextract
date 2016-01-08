package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.music.qq.TagPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.music.qq.MusicPageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 4/2/15.
 */
public class QQMusicCollect extends MusicCollect {
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
        return SiteName.QQMusic;
    }

    public static void main(String[] args){
        new QQMusicCollect().setArgs(args).action();
    }
}