package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.music.migu.MusicPageProcessor;
import cmri.tagbase.music.migu.TagPageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 4/3/15.
 */
public class MiguMusicCollect extends MusicCollect {

    @Override
    public String getSiteName() {
        return SiteName.MiguMusic;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return MusicPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    public static void main(String[] args){
        new MiguMusicCollect().setArgs(args).action();
    }
}
