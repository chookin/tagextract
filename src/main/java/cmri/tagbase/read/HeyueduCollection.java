package cmri.tagbase.read;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.CasperJsDownloader;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by zhuyin on 3/24/15.
 */
public class HeyueduCollection extends CKCollection {

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return BookPageProcessor.getRequest(category);
    }

    @Override
    public String getSiteName() {
        return SiteName.Heyuedu;
    }

    static class CategoryPageProcessor implements PageProcessor {
        private static final CategoryPageProcessor processor = new CategoryPageProcessor();

        public static Collection<Request> getSeedRequests(){
            return Collections.singletonList(new Request("http://www.cmread.com/u/booklist", processor)
            );
        }

        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select("#bookstoreblock > div > h2 > p:nth-child(3) a");
            for (Element element : elements) {
                String name = element.text();
                if(name.contains("全部")){
                    continue;
                }
                String code = element.attr("id");
                String url = "http://www.cmread.com/u/booklist?nodeId="+code;
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setCode(code)
                        .setUrl(url)
                        .setSite(SiteName.Heyuedu);
                getLogger().trace(category);
                page.addItem(category);
            }
        }
    }

    static class BookPageProcessor implements PageProcessor {
        private static final BookPageProcessor processor = new BookPageProcessor();

        public static Request getRequest(CategoryEntity category){
            return new Request(category.getUrl(), processor)
                    .putExtra("categoryEntity", category)
                    .setDownloader(CasperJsDownloader.getInstance());
        }

        @Override
        public void process(ResultItems page) {
            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);

            Document doc = (Document) page.getResource();
            Elements elements = doc.select(".book_tu ul");
            for (Element element : elements) {
                KindEntity book = new KindEntity("book").setCategory(category);

                Element item = element.select("a img").first();
                if(item != null){
                    book.setName(item.attr("title"));
                }

                item = element.select("a").first();
                if(item != null){
                    String url = item.absUrl("href");
                    String code = StringHelper.parseRegex(url, "bid=(\\d+)", 1);
                    url = String.format("http://www.cmread.com/u/bookDetail?bid=%s", code);
                    book.setCode(code)
                            .setUrl(url);
                }

                String txt = element.text();
                String author = StringHelper.parseRegex(txt, "作者：\\s*(\\S+)\\s*", 1);
                if(author != null)
                    book.set("author", author);

                String readCount = StringHelper.parseRegex(txt, "点击数：\\s*(\\d+)\\s*", 1);
                if(readCount != null)
                    book.set("readCount",readCount);

                String scoreNum = StringHelper.parseRegex(txt, "鲜花数：\\s*(\\d+)\\s*", 1);
                if(readCount != null)
                    book.set("scoreNum",scoreNum);

                element = doc.select("li.book_jj").first();
                if (element != null) {
                    book.set("bookIntro", element.attr("title")); // 图书简介
                }
                getLogger().trace(book);
                page.addItem(book);
            }
            addNewRequest(page);
        }
        void addNewRequest(ResultItems page) {
            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);

            Integer curPage = page.getRequest().getExtra("page", Integer.class);
            if (null != curPage) {// if not the first page
                return;
            }
            Integer pageTotalNum = getPageTotalNum((Document) page.getResource());
            if(pageTotalNum == null){
                return;
            }
            for(int index = 2; index <= pageTotalNum; ++ index) {
                String url = String.format("http://www.cmread.com/u/booklist?nodeId=%s&page=%d", category.getCode(), index);
                page.addTargetRequest(new Request(url, processor)
                        .putExtra("categoryEntity", category)
                        .putExtra("page", index));
            }
        }

        Integer getPageTotalNum(Document doc){
            Elements elements = doc.select(".scott2 span");
            String txt = elements.get(elements.size() -2).text();
            return Integer.valueOf(txt);
        }
    }
}
