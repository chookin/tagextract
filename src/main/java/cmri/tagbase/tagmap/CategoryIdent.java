package cmri.tagbase.tagmap;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.TagMapEntity;

/** generate category key for each mapping record
 * 从分类-标签映射配置生成分类的key
 * 分类-标签映射的配置中，会存在分类名相同的情况，但是呢，由于分类的id比较复杂，没有登记到配置信息中，这种情况如何处理呢？
 * 例如，
 视频/电影/英国	英国
 视频/动漫/英国	英国
 CategoryEntity{name='英国', code='4/1107-------------11-1-1-iqiyi--', level='2', url='http://list.iqiyi.com/www/4/1107-------------11-1-1-iqiyi--.html', site='aiqiyi', tag='[]', parent='动漫'}

 * 另外，还存在这样的情况
 节点名称	腾讯音乐
 音乐/风格流派/轻音乐	轻音乐
 音乐/歌手/轻音乐	轻音乐
 * 暂时先不处理了吧。在给实际的分类打标签时，再具体对待
 */
public class CategoryIdent{
    private String site;
    private String name;
    private String code;
    private String parentName;

    public CategoryIdent(CategoryEntity category){
        this.site = category.getSite();
        this.name = category.getName();
        this.code = category.getCode();
        if(category.getParent() != null){
            this.parentName = category.getParent().getName();
        }
    }

    public CategoryIdent(TagMapEntity entity){
        this.site = entity.getSite();
        this.name = entity.getCategory();
        this.code = entity.getCategoryId();
        this.parentName = parseParentName(entity);
    }

    private String parseParentName(TagMapEntity entity){
        String tag = entity.getTag();
        // not used, for example, youku, 视频/动漫/益智 <-> 教育
        // if(!tag.endsWith(entity.getCategory())) return null;
        int lastSlash = tag.lastIndexOf("/");
        if(lastSlash <= 0){
            return null;
        }
        // 查找倒数第二个“/”的位置
        int penultSlash = tag.lastIndexOf("/", lastSlash - 1);
        if(penultSlash == -1){
            return null;
        }
        return tag.substring(penultSlash+1, lastSlash);
    }

    public String getSite() {
        return site;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getParentName() {
        return parentName;
    }

    @Override
    public String toString() {
        return "CategoryIdent{" +
                "site='" + site + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentName='" + parentName + '\'' +
                '}';
    }

    /**
     * @return true only if site, name and code are all equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryIdent)) return false;

        CategoryIdent that = (CategoryIdent) o;

        if (!site.equals(that.site)) return false;
        if (!name.equals(that.name)) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return !(parentName != null ? !parentName.equals(that.parentName) : that.parentName != null);
    }

    public boolean equalsIgnoreParentName(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryIdent)) return false;

        CategoryIdent that = (CategoryIdent) o;

        if (!site.equals(that.site)) return false;
        if (!name.equals(that.name)) return false;
        return !(code != null ? !code.equals(that.code) : that.code != null);
    }
    public boolean equalsIgnoreCode(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryIdent)) return false;

        CategoryIdent that = (CategoryIdent) o;

        if (!site.equals(that.site)) return false;
        if (!name.equals(that.name)) return false;
        return !(parentName != null ? !parentName.equals(that.parentName) : that.parentName != null);
    }
    public boolean equalsIgnoreCodeParentName(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryIdent)) return false;

        CategoryIdent that = (CategoryIdent) o;

        return site.equals(that.site) && name.equals(that.name);
    }


    @Override
    public int hashCode() {
        int result = site.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (parentName != null ? parentName.hashCode() : 0);
        return result;
    }
}