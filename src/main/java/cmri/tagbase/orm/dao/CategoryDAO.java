package cmri.tagbase.orm.dao;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.tagmap.CategoryIdent;
import cmri.utils.dao.MongoDAO;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by zhuyin on 12/15/14.
 */
public class CategoryDAO extends MongoDAO<CategoryEntity> implements EntityTagUpdate {
    private static Logger LOG = Logger.getLogger(CategoryDAO.class);

    private CategoryDAO() {
        super("category");
    }

    public static CategoryDAO getInstance() {
        return new CategoryDAO();
    }

    @Override
    protected BasicDBObject getBasicDBObject(CategoryEntity entity) {
        BasicDBObject doc = new BasicDBObject();
        for(Map.Entry<String, Object> entry: entity.toStringMap().entrySet()){
            if("collection".equals(entry.getKey())){
                continue;
            }
            doc.put(entry.getKey(), entry.getValue());
        }
        return doc;
    }

    @Override
    protected CategoryEntity parse(DBObject dbObject) {
        if(dbObject == null) return null;
        CategoryEntity category = new CategoryEntity()
                .setName((String) dbObject.get("name"))
                .setSite((String) dbObject.get("site"))
                .setCode((String) dbObject.get("code"))
                .setLevel((Integer) dbObject.get("level"))
                .setUrl((String) dbObject.get("url"))
                .setCrawlTime((Date) dbObject.get("crawlTime"))
                .setTime((Date) dbObject.get("time"))
                .addTags(dbObject.get("tag"))
                ;
        String p_name = (String) dbObject.get("p_name");
        String p_code = (String) dbObject.get("p_code");
        if(StringUtils.isNotEmpty(p_name) || StringUtils.isNotEmpty(p_code)){
            category.setParent(new CategoryEntity().setName(p_name)
                            .setCode(p_code)
                            .setSite(category.getSite())
            );
        }
        return category;
    }

    @Override
    protected String getId(CategoryEntity entity) {
        return entity.getId();
    }

    @Override
    public int save(Collection<CategoryEntity> entities) {
        // To avoid overwrite the crawled time field, which marks when the category's kinds had been collect.
        return saveIfNotExists(entities);
    }

    public List<CategoryEntity> findNotYetCrawlSince(String site, Date time, boolean tagRequire) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.put("site").is(site)
                .put("leaf").is(true)
                .put("crawlTime").lessThanEquals(time);
        if (tagRequire) {
            queryBuilder.put("tag").exists(true);
        }
        return find(queryBuilder.get());
    }

    @Override
    public void updateTag(Map<String, Map<CategoryIdent, Set<String>>> categoriesMap) {
        DBCollection dbCollection = this.getCollection();
        int index = 0;
        for (Map.Entry<String, Map<CategoryIdent, Set<String>>> siteCategories : categoriesMap.entrySet()) {
            Map<CategoryIdent, Set<String>> subCategoriesMap = siteCategories.getValue();
            for (Map.Entry<CategoryIdent, Set<String>> entry : subCategoriesMap.entrySet()) {
                CategoryIdent ident = entry.getKey();
                Set<String> tags = entry.getValue();
                LOG.info(index++ + ". Update category " + ident + "->" + tags);
                DBObject oper = new BasicDBObject("$addToSet", new BasicDBObject("tag", new BasicDBObject("$each", tags)));
                dbCollection.update(getUpdateQuery(ident), oper, false, true);
            }
        }
        LOG.info("Done of update categories tags");
    }

    @Override
    public DBObject getUpdateQuery(CategoryIdent ident){
        QueryBuilder queryBuilder = new QueryBuilder().put("site").is(ident.getSite())
                .put("name").is(ident.getName());
        if (StringUtils.isNotEmpty(ident.getCode())) {
            queryBuilder.put("code").is(ident.getCode());
        }
        return queryBuilder.get();
    }
}
