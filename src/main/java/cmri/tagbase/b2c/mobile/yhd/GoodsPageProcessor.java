package cmri.tagbase.b2c.mobile.yhd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhuyin on 3/3/15.
 */
public class GoodsPageProcessor implements PageProcessor {
    static String siteUrl = "http://m.yhd.com";
    private static final GoodsPageProcessor processor = new GoodsPageProcessor();

    public static Request getRequest(CategoryEntity category){
        if (StringUtils.isBlank(category.getUrl())) {
            return null;
        }
        return new Request(category.getUrl(), processor)
                .putExtra("categoryEntity", category);
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);

        List<String> urls = generatePageUrls(doc);
        getLogger().trace(urls.size() + " urls for " + categoryEntity);

        List<Request> requests = urls.stream().map(url -> GoodsPageProcessor2.getRequest(categoryEntity, url)).collect(Collectors.toList());
        page.addTargetRequest(requests);
    }

    /**
     * var defaultPageUrl = '/search/c33625/p1-s1?virtualflag=1';
     * // http://m.yhd.com/search/c33625/p2-s1?virtualflag=1&req.ajaxFlag=1
     *
     * @return
     * @throws java.io.IOException
     */
    String getPageUrlOrigin(Document doc) {
        String htmls = doc.toString();
        String keyStart = "defaultPageUrl =";
        int start = htmls.indexOf(keyStart);
        start += keyStart.length();
        String defaultPageUrl = htmls.substring(start, htmls.indexOf(";", start)).replace("'", "").trim();
        if (defaultPageUrl.indexOf("?") != 1) {
            return siteUrl + defaultPageUrl + "&req.ajaxFlag=1";
        } else {
            return siteUrl + defaultPageUrl + "?req.ajaxFlag=1";
        }
    }

    int getTotalPage(Document doc) {
        // var totalPage = '12';
        String totalPage = StringHelper.parseRegex(doc.toString(), "totalPage\\D+([\\d]+)\\D+", 1);
        if(totalPage == null) {
            throw new RuntimeException("Cannot parse 'totalPage' of doc : " + doc.baseUri());
        }
        return Integer.parseInt(totalPage);
    }

    List<String> generatePageUrls(Document doc) {
        String format = getPageUrlOrigin(doc);
        List<String> urls = new ArrayList<>();
        int totalPage = getTotalPage(doc);
        for (int i = 1; i <= totalPage; ++i) {
            String url = format.replaceFirst("/p[0-9]*-", "/p" + i + "-");
            urls.add(url);
        }
        return urls;
    }
}
