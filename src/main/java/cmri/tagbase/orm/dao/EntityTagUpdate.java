package cmri.tagbase.orm.dao;

import cmri.tagbase.tagmap.CategoryIdent;
import com.mongodb.DBObject;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhuyin on 6/26/15.
 */
public interface EntityTagUpdate {
    /**
     *
     * @param tagMap map of {site:{categoryIdent:[tag]}}
     */
    void updateTag(Map<String, Map<CategoryIdent, Set<String>>> tagMap);

    DBObject getUpdateQuery(CategoryIdent ident);
}
