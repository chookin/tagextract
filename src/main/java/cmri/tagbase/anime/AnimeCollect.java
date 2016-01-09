package cmri.tagbase.anime;

import cmri.tagbase.base.KindCollect;
import cmri.utils.lang.BaseOper;
import cmri.tagbase.base.CategoryCollect;

/**
 * Created by zhuyin on 6/8/15.
 */
abstract class AnimeCollect extends BaseOper implements CategoryCollect, KindCollect {
    @Override
    public boolean action() {
        return collectCategories(getOptions())
                || collectKinds(getOptions());
    }
}

