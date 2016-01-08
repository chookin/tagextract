package cmri.tagbase.game;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.game.heyouxi.GamePageProcessor;
import cmri.tagbase.game.heyouxi.TagPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;

import java.util.Collection;

/**
 * Created by zhuyin on 6/10/15.
 */
public class HeyouxiCollect extends GameCollect {
    @Override
    public String getSiteName() {
        return SiteName.Heyouxi;
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
        new HeyouxiCollect().setArgs(args).action();
    }
}
