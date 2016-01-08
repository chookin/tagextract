package cmri.tagbase.orm.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by zhuyin on 3/11/15.
 */
public abstract class TagEntity<T> extends BaseEntity<T> {
    protected String code;
    protected String site;
    protected Set<String> tags = new TreeSet<>();

    public String getId() {
        StringBuilder strb = new StringBuilder(site);
        if (code != null) {
            strb.append("-").append(code);
        }
        strb.append("-").append(name.substring(0, Math.min(9, name.length())));
        return strb.toString();
    }

    public String getCode() {
        return code;
    }

    public T setCode(String code) {
        this.code = code;
        return getThis();
    }

    public String getSite() {
        return this.site;
    }

    public T setSite(String site) {
        this.site = site;
        return getThis();
    }

    public T addTags(String tag) {
        if(StringUtils.isNotEmpty(tag))
            this.tags.add(tag);
        return getThis();
    }

    public T addTags(Collection<String> tags) {
        if(tags != null)
            this.tags.addAll(tags);
        return getThis();
    }

    public T addTags(Object obj){
        if(obj instanceof String){
            String tag = (String) obj;
            this.tags.add(tag);
        }else if(obj instanceof Collection){
            ((Collection<?>) obj).forEach(this::addTags);
        }
        return getThis();
    }

    public Set<String> getTags() {
        return this.tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagEntity)) return false;
        if (!super.equals(o)) return false;

        TagEntity tagEntity = (TagEntity) o;

        if (name != null ? !name.equals(tagEntity.name) : tagEntity.name != null) return false;
        if (code != null ? !code.equals(tagEntity.code) : tagEntity.code != null) return false;
        if (site != null ? !site.equals(tagEntity.site) : tagEntity.site != null) return false;
        if (tags != null ? !tags.equals(tagEntity.tags) : tagEntity.tags != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TagEntity{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", url='" + url + '\'' +
                ", site='" + site + '\'' +
                ", tag='" + tags + '\'' +
                ", properties=" + properties +
                '}';
    }
}
