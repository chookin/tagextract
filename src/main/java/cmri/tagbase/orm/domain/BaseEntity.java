package cmri.tagbase.orm.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhuyin on 3/11/15.
 */
public abstract class BaseEntity<T> implements Serializable {
    protected String url;
    protected String name;
    protected Map<String, Object> properties = new TreeMap<>();

    /**
     * acquisition time
     */
    protected Date time;

    public BaseEntity() {
        this.time = new Date();
    }

    protected abstract T getThis();

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return getThis();
    }

    public String getUrl() {
        return url;
    }

    public T setUrl(String url) {
        this.url = url;
        return getThis();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public T emptyProperties() {
        this.properties.clear();
        return getThis();
    }

    public T set(Map<String, Object> properties) {
        if(properties !=null) {
            this.properties.putAll(properties);
        }
        return getThis();
    }

    public T set(String name, Object value) {
        if(value == null){
            return getThis();
        }
        if(value instanceof String && ((String) value).isEmpty()){
            return getThis();
        }
        this.properties.put(name, value);
        return getThis();
    }

    public Object get(String propertyName) {
        return properties.get(propertyName);
    }

    public T setTime(Date time) {
        this.time = time;
        return getThis();
    }

    public Date getTime() {
        return this.time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;

        BaseEntity entity = (BaseEntity) o;

        if (name != null ? !name.equals(entity.name) : entity.name != null) return false;
        if (url != null ? !url.equals(entity.url) : entity.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
