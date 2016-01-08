package cmri.tagbase.music;

import cmri.tagbase.base.KindCollect;
import cmri.utils.lang.BaseOper;
import cmri.tagbase.base.CategoryCollect;

/**
 * Created by zhuyin on 3/31/15.
 */
abstract class MusicCollect extends BaseOper implements CategoryCollect, KindCollect {
    @Override
    public boolean action() {
        return collectCategories(getOptionsPack())
                || collectKinds(getOptionsPack());
    }
}
