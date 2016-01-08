package cmri.tagbase.orm.domain;

import cmri.etl.common.MapItem;

import java.util.*;

/**
 * Created by zhuyin on 1/16/15.
 */
public class SiteEntity extends BaseEntity<SiteEntity> implements MapItem {
    /**
     * {category_name: order}
     */
    private Map<String, Integer> categories = new TreeMap<>();
    private Set<String> tags = new TreeSet<>();
    private Map<String, Integer> ranking = new TreeMap<>();

    public static Map<String, Integer> parseCategories(Collection<Map<String, Object>> categories) {
        Map<String, Integer> myCategories = new TreeMap<>();
        for (Map map : categories) {
            myCategories.put((String) map.get("name"), (Integer) map.get("no"));
        }
        return myCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteEntity that = (SiteEntity) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!categories.equals(that.categories)) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + categories.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SiteEntity{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", categories='" + categories + '\'' +
                ", ranking=" + ranking +
                '}';
    }

    @Override
    protected SiteEntity getThis() {
        return this;
    }

    public Map<String, Integer> getCategories() {
        return this.categories;
    }

    public List<Map<String, Object>> getCategoriesAsList() {
        List<Map<String, Object>> myCategories = new ArrayList<>();
        for (Map.Entry entry : this.categories.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("no", entry.getValue());
            myCategories.add(item);
        }
        return myCategories;
    }

    public SiteEntity addCategory(String categoryName, Integer no) {
        this.categories.put(categoryName, no);
        return this;
    }

    public SiteEntity addCategories(Map<String, Integer> categories) {
        if(categories != null) {
            this.categories.putAll(categories);
        }
        return this;
    }

    public SiteEntity addCategories(List<Map<String, Object>> categories) {
        return this.addCategories(parseCategories(categories));
    }

    public SiteEntity addTags(String tag) {
        if (tag == null || tag.isEmpty()) {
            return this;
        }
        this.tags.add(tag);
        return this;
    }

    public SiteEntity addTags(Collection<String> tags) {
        if(tags != null)
            this.tags.addAll(tags);
        return this;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public Map<String, Integer> getRanking() {
        return this.ranking;
    }

    public SiteEntity addRanking(String name, Integer value) {
        this.ranking.put(name, value);
        return this;
    }

    public SiteEntity addRanking(Map<String, Integer> ranking) {
        if (ranking != null) {
            this.ranking.putAll(ranking);
        }
        return this;
    }

    @Override
    public Map<String, Object> toStringMap() {
        Map<String, Object> item = new HashMap<>();
        item.put("collection", "site");
        item.put("_id", getId());
        item.put("name", name);
        item.put("url", url);
        item.put("ranking", ranking);
        item.put("category", this.getCategoriesAsList());
        if(!tags.isEmpty())
            item.put("tag", tags);
        item.put("time", time);
        if(!properties.isEmpty())
            item.put("properties", properties);
        return item;
    }

    @Override
    public String getId() {
        return name + '-' + url.substring(0, url.indexOf('.'));
    }
}
