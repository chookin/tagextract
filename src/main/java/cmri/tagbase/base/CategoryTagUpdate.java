package cmri.tagbase.base;

import cmri.tagbase.tagmap.TagMap;
import cmri.tagbase.orm.dao.CategoryDAO;
import com.mongodb.QueryBuilder;

import java.util.Set;

/**
 * Created by zhuyin on 3/25/15.
 */
public interface CategoryTagUpdate {
    default boolean updateCategory(Set<String> sites) {
        CategoryDAO categoryDAO = CategoryDAO.getInstance();
        try {
            categoryDAO.dropField("tag", new QueryBuilder().put("site").in(sites).get());
            categoryDAO.updateTag(getTagMap().getCategoriesMap(sites));
        } finally {
            categoryDAO.close();
        }
        return true;
    }
    TagMap getTagMap();
}
