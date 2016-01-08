package cmri.tagbase.base;

import cmri.tagbase.tagmap.TagMap;
import cmri.utils.lang.BaseOper;
import cmri.utils.lang.JsonHelper;

import java.util.Set;

/**
 * Created by zhuyin on 3/25/15.
 */
public abstract class TagUpdate extends BaseOper implements CategoryTagUpdate, KindTagUpdate {
    private static TagMap tagMap = TagMap.getInstance();

    @Override
    public TagMap getTagMap() {
        return tagMap;
    }

    @Override
    public boolean action() {
        Set<String> sites = parseSiteOption();
        if(sites == null){
            getLogger().info("please set sites to update");
            return false;
        }
        return updateCategory(sites) && updateKind(sites);
    }

    Set<String> parseSiteOption() {
        String option = "--sites";
        if (this.getOptionsPack().get(option) == null) {
            return null;
        }
        String paras = getOptionsPack().get(option);
        return JsonHelper.parseStringSet(paras);
    }
}
