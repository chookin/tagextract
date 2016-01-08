package cmri.tagbase.video;

import cmri.tagbase.base.CategoryCollect;
import cmri.tagbase.base.KindCollect;
import cmri.utils.lang.BaseOper;
import org.apache.log4j.Logger;

/**
 * Created by zhuyin on 4/8/15.
 */
abstract class VideoCollect extends BaseOper implements CategoryCollect, KindCollect {
    private static final Logger LOG = Logger.getLogger(VideoCollect.class);
    @Override
    public boolean action() {
        return collectCategories(getOptionsPack())
                || collectKinds(getOptionsPack());
    }
}
