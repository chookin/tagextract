package cmri.tagbase.orm.dao;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.UrlHelper;
import cmri.tagbase.tagmap.CategoryIdent;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.dao.MongoDAO;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhuyin on 3/15/15.
 */
public class KindDAO extends MongoDAO<KindEntity> implements EntityTagUpdate{
    private static final Logger LOG = Logger.getLogger(KindDAO.class);
    public KindDAO(String collectionName) {
        super(collectionName);
    }

    @Override
    protected String getId(KindEntity entity) {
        return entity.getId();
    }

    @Override
    protected BasicDBObject getBasicDBObject(KindEntity entity) {
        BasicDBObject doc = new BasicDBObject();
        doc.put("_id", entity.getId());
        doc.put("name", entity.getName());
        doc.put("site", entity.getSite());
        if(StringUtils.isNotEmpty(entity.getCode()))
            doc.put("code", entity.getCode());
        if(StringUtils.isNotEmpty(entity.getUrl())) {
            doc.put("url", entity.getUrl());
            doc.put("domain", UrlHelper.getBaseDomain(entity.getUrl()));
        }
        if(!entity.getTags().isEmpty())
            doc.put("tag", entity.getTags());
        doc.put("time", entity.getTime());

        doc.put("category", entity.getCategory().getName());
        doc.put("c_code", entity.getCategory().getCode());
        if(!entity.getProperties().isEmpty())
            doc.put("properties", entity.getProperties());
        return doc;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected KindEntity parse(DBObject dbObject) {
        if(dbObject == null) return null;
        String site = (String) dbObject.get("site");
        return new KindEntity(getCollectionName())
                .setName((String) dbObject.get("name"))
                .setCode((String) dbObject.get("code"))
                .setUrl((String) dbObject.get("url"))
                .setTime((Date) dbObject.get("time"))
                .setCategory(new CategoryEntity()
                        .setName((String) dbObject.get("category"))
                        .setCode((String) dbObject.get("c_code"))
                        .addTags((Collection<String>) dbObject.get("tag"))
                        .setSite(site))
                .set((Map<String, Object>) dbObject.get("properties"))
                ;
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
                LOG.info(index++ + ". Update kinds " + ident + "->" + tags);
                DBObject oper = new BasicDBObject("$addToSet", new BasicDBObject("tag", new BasicDBObject("$each", tags)));
                dbCollection.update(getUpdateQuery(ident), oper, false, true);
            }
        }
        LOG.info("Done of update kinds tags");
    }

    @Override
    public DBObject getUpdateQuery(CategoryIdent ident){
        QueryBuilder queryBuilder = new QueryBuilder().put("site").is(ident.getSite());
        queryBuilder.or(new BasicDBObject("category", ident.getName())
                , new BasicDBObject("properties.keywords", ident.getName())
                , new BasicDBObject("properties.keyWords", ident.getName())
        );
        if (StringUtils.isNotEmpty(ident.getCode())) {
            queryBuilder.put("c_code").is(ident.getCode());
        }
        return queryBuilder.get();
    }
}
