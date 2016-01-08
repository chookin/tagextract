package cmri.tagbase.b2c.mobile.jd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhuyin on 3/3/15.
 */
public class GoodsPageProcessor implements PageProcessor {
    private static final String siteUrl = "http://m.jd.com";

    private static final GoodsPageProcessor processor = new GoodsPageProcessor();

    public static Request getRequest(CategoryEntity category){
        if (StringUtils.isBlank(category.getUrl())) {
            return null;
        }
        return new Request(category.getUrl(), processor)
                .putExtra("categoryEntity", category)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);
        Document doc = (Document) page.getResource();

        List<String> urls = generatePageUrls(doc);
        if(urls.isEmpty()){
            return;
        }
        getLogger().trace(urls.size() + " urls for " + categoryEntity);

        List<Request> requests = urls.stream()
                .map(url -> JsonGoodsPageProcessor.getRequest(categoryEntity, url))
                .collect(Collectors.toList());
        page.addTargetRequest(requests);
    }

    /**
     * get the json data's url format.
     * <p/>
     * function getUrl(page){
     * var url='';
     * if(keyword){
     * url = '/ware/search.json?cid=0&keyword=&sort=1&page=' + page + '&expressionKey=&expandName=&minprice=&maxprice=&stock=&resourceType=search&resourceValue=&sid=b5a5ca41b8a547defa01ca2dd5b8871c';
     * }else{
     * url='/products/1315-1342-12003-0-0-0-0-0-0-0-1-1-' + page + '.html?cid=12003&stock=&resourceType=search&resourceValue=&sid=b5a5ca41b8a547defa01ca2dd5b8871c&_format_=json';
     * }
     * return url;
     * }
     *
     * @return
     */
    String getPageUrlFormat(Document doc) {
        String htmls = doc.toString();
        String keyStart = "function getUrl(page){";
        int start = htmls.indexOf(keyStart);
        keyStart = "else{";
        start = htmls.indexOf(keyStart, start);
        start += keyStart.length();
        String urlFormat = StringUtils.strip(htmls.substring(start, htmls.indexOf("}", start)));
        int index1 = urlFormat.indexOf('\'');
        int index2 = urlFormat.indexOf('\'', index1 + 1);
        int index3 = urlFormat.indexOf('\'', index2 + 1);
        int index4 = urlFormat.indexOf('\'', index3 + 1);
        // mem: escaping formatting characters in java String.format
        // just double up the %
        return String.format("%s%s%%d%s", siteUrl, urlFormat.substring(index1 + 1, index2), urlFormat.substring(index3 + 1, index4));
    }

    /**
     * Get the total pages by keyword "totalPage="
     *
     * @return
     */
    Integer getTotalPage(Document doc) {
        String htmls = doc.toString();
        String keyStart = "totalPage="; // totalPage= 200,//总页数
        int start = htmls.indexOf(keyStart);
        if(start == -1){
            getLogger().error("Cannot parse totalPage of " + doc.baseUri());
            return null;
        }
        start += keyStart.length();
        return Integer.parseInt(htmls.substring(start, htmls.indexOf(",", start)).trim());
    }

    /**
     * Get the json data's urls.
     *
     * @return
     * @throws IOException
     */
    List<String> generatePageUrls(Document doc) {
        String format = getPageUrlFormat(doc);
        List<String> urls = new ArrayList<>();
        Integer totalPage = getTotalPage(doc);
        if(totalPage == null){
            return urls;
        }
        for (int i = 1; i <= totalPage; ++i) {
            String url = String.format(format, i);
            urls.add(url);
        }
        return urls;
    }
}
