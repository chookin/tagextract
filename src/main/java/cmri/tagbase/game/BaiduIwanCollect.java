package cmri.tagbase.game;

import cmri.etl.common.Request;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.SiteName;
import cmri.tagbase.game.baidu.GamePageProcessor;
import cmri.tagbase.game.baidu.TagPageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 6/9/15.
 */
public class BaiduIwanCollect extends GameCollect {
    @Override
    public String getSiteName() {
        return SiteName.BaiduMobileGame;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return GamePageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    public static void main(String[] args){
        new BaiduIwanCollect().setArgs(args).action();
    }
}
