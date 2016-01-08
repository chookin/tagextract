package cmri.tagbase.orm.domain;

import cmri.etl.common.MapItem;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by zhuyin on 12/10/14.
 */
public class CategoryEntity extends TagEntity<CategoryEntity> implements MapItem {
    private int level;
    private Date crawlTime;
    private CategoryEntity parent;
    private Set<CategoryEntity> children;

    public CategoryEntity() {
        this.crawlTime = new java.util.Date(0);
        this.level = 1;
    }

    @Override
    protected CategoryEntity getThis() {
        return this;
    }

    public CategoryEntity getParent() {
        return parent;
    }

    public CategoryEntity setParent(CategoryEntity parent) {
        this.parent = parent;
        if (this.parent != null) {
            this.parent.addChild(this);
            this.setLevel(this.parent.level + 1);
            this.setSite(parent.site);
        }
        return this;
    }

    public int getLevel() {
        return this.level;
    }

    public CategoryEntity setLevel(Integer level) {
        if(level != null) {
            this.level = level;
        }
        return this;
    }

    public Date getCrawlTime() {
        return this.crawlTime;
    }

    public CategoryEntity setCrawlTime(Date crawlTime) {
        this.crawlTime = crawlTime;
        return this;
    }

    public synchronized List<CategoryEntity> getChildren() {
        if (this.children == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(children);
    }

    public synchronized void addChild(CategoryEntity child) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryEntity)) return false;
        if (!super.equals(o)) return false;

        CategoryEntity that = (CategoryEntity) o;

        if (level != that.level) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + level;
        return result;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", level='" + level + '\'' +
                ", url='" + url + '\'' +
                ", site='" + site + '\'' +
                ", tag='" + tags + '\'' +
                (parent == null ? "" : ", parent='" + parent.name + '\'') +
                '}';
    }

    public Map<String, Object> toStringMap(){
        Map<String, Object> item = new HashMap<>();
        item.put("collection", "category");
        item.put("_id", this.getId());
        item.put("name", this.getName());
        item.put("site", this.getSite());
        if(StringUtils.isNotEmpty(this.getCode()))
            item.put("code", this.getCode());
        if(!this.getTags().isEmpty())
            item.put("tag", this.getTags());
        item.put("level", this.getLevel());
        if(StringUtils.isNotEmpty(this.getUrl()))
            item.put("url", this.getUrl());
        if (this.getParent() != null) {
            item.put("p_name", this.getParent().getName());
            if(StringUtils.isNotEmpty(this.getParent().getCode()))
                item.put("p_code", this.getParent().getCode());
        }
        item.put("leaf", this.getChildren().isEmpty());
        item.put("crawlTime", this.getCrawlTime());
        item.put("time", this.getTime());
        return item;
    }
}
