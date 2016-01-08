package cmri.tagbase.base;

import cmri.tagbase.orm.dao.KindDAO;
import cmri.tagbase.tagmap.TagMap;
import com.mongodb.QueryBuilder;

import java.util.Set;

/**
 * Created by zhuyin on 3/25/15.
 */
public interface KindTagUpdate {
    default boolean updateKind(Set<String> sites) {
        KindDAO dao = getDAO();
        try {
            dao.dropField("tag", new QueryBuilder().put("site").in(sites).get());
            dao.updateTag(this.getTagMap().getCategoriesMap(sites));
        } finally {
            dao.close();
        }
        return true;
    }
    TagMap getTagMap();
    KindDAO getDAO();
}
