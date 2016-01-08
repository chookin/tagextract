package cmri.tagbase.b2c.mobile.tb;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.io.FileHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhuyin on 3/3/15.
 */
public class GoodsPageProcessor implements PageProcessor {
    private static final GoodsPageProcessor processor = new GoodsPageProcessor();

    public static Request getRequest(CategoryEntity category){
        if (StringUtils.isBlank(category.getUrl())) {
            return null;
        }
        String url = category.getUrl();

        // may be failed for some categories, such as "移动硬盘"
        // http://s.m.taobao.com/product/search.htm?spm=0.0.0.0&sst=1&q=%E7%A7%BB%E5%8A%A8%E7%A1%AC%E7%9B%98&top_search=1&iwRet=true&useless_q=1&catmap=50038509
        if(url.contains("product")) {
            url = String.format("http://s.m.taobao.com/search.htm?q=%s", category.getName());
        }
        return new Request(url, processor)
                .putExtra("categoryEntity", category)
                .setDownloader(TbGoodsDownloader.getInstance())
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);
        List<String> urls;
        try {
            urls = generatePageUrls(page.getRequest());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLogger().trace(urls.size() + " urls for " + categoryEntity);

        List<Request> requests = urls.stream()
                .map(url -> JsonGoodsPageProcessor.getRequest(categoryEntity, url))
                .collect(Collectors.toList());
        page.addTargetRequest(requests);
    }

    String getPageUrlOrigin(Request request) throws IOException {
        return FileHelper.readString(TbGoodsDownloader.getUrlFileName(request));
    }

    int getTotalPage(Request request) throws IOException {
        return Integer.parseInt(FileHelper.readString(TbGoodsDownloader.getTotalPageFileName(request)).trim());
    }

    List<String> generatePageUrls(Request request) throws IOException {
        try {
            String format = getPageUrlOrigin(request);
            List<String> urls = new ArrayList<>();
            int totalPage = getTotalPage(request);
            for (int i = 1; i <= totalPage; ++i) {
                String url = format.replaceFirst("page=[0-9]*", "page=" + i);
                urls.add(url);
            }
            return urls;
        }catch (IOException e){
            TbGoodsDownloader.enableScroll();
            throw e;
        }
    }
}
