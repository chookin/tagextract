package cmri.tagbase.music;

import cmri.tagbase.orm.dao.KindDAO;
import cmri.tagbase.tagmap.CategoryIdent;
import cmri.tagbase.base.TagUpdate;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhuyin on 4/14/15.
 */
public class MusicTagUpdate extends TagUpdate {
    @Override
    public KindDAO getDAO() {
        return new KindDAO("music"){
            @Override
            public DBObject getUpdateQuery(CategoryIdent ident){
                QueryBuilder queryBuilder = new QueryBuilder().put("site").is(ident.getSite());
                queryBuilder.or(new BasicDBObject("category", ident.getName())
                        , new BasicDBObject("properties.keywords", ident.getName())
                        , new BasicDBObject("properties.keyWords", ident.getName())
                        , new BasicDBObject("properties.singer.name", ident.getName())
                );
                if (StringUtils.isNotEmpty(ident.getCode())) {
                    queryBuilder.put("c_code").is(ident.getCode());
                }
                return queryBuilder.get();
            }
        };
    }

    public static void main(String[] args){
        new MusicTagUpdate().setArgs(args).action();
    }
}
