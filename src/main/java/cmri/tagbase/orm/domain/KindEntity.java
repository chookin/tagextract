package cmri.tagbase.orm.domain;

import cmri.etl.common.MapItem;
import cmri.utils.web.UrlHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuyin on 3/11/15.
 * Kind is a item under a category.
 */
public class KindEntity extends TagEntity<KindEntity> implements MapItem {
    protected CategoryEntity category;
    private String collection;

    /**
     *
     * @param collection 类型，如书籍book,视频video，动漫anime，类型决定了商品存入mongo的哪个collection.
     */
    public KindEntity(String collection){
        this.collection = collection;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public KindEntity setCategory(CategoryEntity category) {
        this.category = category;
        if(category != null) {
            this.setSite(category.getSite());
            this.addTags(category.getTags());
        }
        return getThis();
    }

    public String getCollection(){
        return collection;
    }

    @Override
    protected KindEntity getThis() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KindEntity)) return false;
        if (!super.equals(o)) return false;

        KindEntity that = (KindEntity) o;

        return !(category != null ? !category.equals(that.category) : that.category != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KindEntity{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", url='" + url + '\'' +
                ", site='" + site + '\'' +
                ", tag='" + tags + '\'' +
                ", category=" + category +
                ", properties=" + properties +
                '}';
    }

    public Map<String, Object> toStringMap(){
        Map<String, Object> item = new HashMap<>();
        item.put("collection", this.collection);
        item.put("_id", this.getId());
        item.put("name", this.getName());
        item.put("site", this.getSite());
        if(StringUtils.isNotEmpty(this.getCode()))
            item.put("code", this.getCode());
        if(StringUtils.isNotEmpty(this.getUrl())) {
            item.put("url", this.getUrl());
            item.put("domain", UrlHelper.getBaseDomain(this.getUrl()));
        }
        if(!this.getTags().isEmpty())
            item.put("tag", this.getTags());
        item.put("time", this.getTime());

        item.put("category", this.getCategory().getName());
        item.put("c_code", this.getCategory().getCode());
        if(!this.getProperties().isEmpty())
            item.put("properties", this.getProperties());
        return item;
    }
}
