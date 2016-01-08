package cmri.tagbase.video.youku;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.StringHelper;
import com.google.common.collect.Sets;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuyin on 4/8/15.
 */
class CategoryPageProcessor2 implements PageProcessor {
    private static final CategoryPageProcessor2 processor = new CategoryPageProcessor2();

    public static Request getRequest(CategoryEntity category){
        return new Request(category.getUrl(), processor)
                .putExtra("category", category)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity parent = page.getRequest().getExtra("category", CategoryEntity.class);
        Document doc = (Document) page.getResource();
        List<CategoryEntity> categories = processItems(doc, parent);
        categories.forEach(page::addItem);
    }

    private List<CategoryEntity> processItems(Element element, CategoryEntity parent){
        List<CategoryEntity> categories = new ArrayList<>();

        Elements items = element.select("div.yk-filter-panel > div.item");
        Set<String> validLabels = Sets.newHashSet("地区", "类型", "时间", "出品", "热门");
        // http://www.youku.com/v_olist/c_87.html 教育
        // http://www.youku.com/v_olist/c_84.html 纪录片
        for(Element item: items){
            Element eLabel = item.select("label").first();
            if(eLabel == null){
                continue;
            }
            String label = eLabel.text();
            if(!validLabels.contains(label)){
                continue;
            }
            List<CategoryEntity> subCategories = processItem(item, parent);
            categories.addAll(subCategories);
        }
        return categories;
    }

    private List<CategoryEntity> processItem(Element element, CategoryEntity parent){
        List<CategoryEntity> categories = new ArrayList<>();
        Elements elements = element.select("ul > li > a");
        for(Element item : elements){
            // <a href="http://www.youku.com/v_olist/c_97_s_1_d_1_g_古装.html">古装</a>
            String name = item.text();
            if(name.equals("全部")){
                continue;
            }
            String url = item.absUrl("href");
            String code = StringHelper.parseRegex(url, "list/([\\w\\W]+).html", 1);
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setCode(code)
                    .setUrl(url)
                    .setParent(parent);
            getLogger().trace(category);
            categories.add(category);
        }
        return categories;
    }

}
