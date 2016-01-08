package cmri.tagbase.site.chinaz;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.SiteEntity;
import cmri.utils.web.UrlHelper;
import org.apache.commons.lang3.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuyin on 3/4/15.
 */
public class TopSitePageProcessor implements PageProcessor {
    private static final TopSitePageProcessor processor = new TopSitePageProcessor();

    public static Request getRequest(CategoryEntity category){
        return new Request(category.getUrl(), processor)
                .putExtra("categoryEntity", category);
    }
    @Override
    public void process(ResultItems page) {
        getLogger().trace("extract top site of: " + page.getRequest().getUrl());
        Document doc = (Document) page.getResource();
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);

        List<SiteEntity> sites = new ArrayList<>();
        Elements elements = doc.select("body > div.main > div.clearfix.cols2.section-list > div.col-2  div.p-body > div > ul > li");
        for (Element element : elements) {
            String name = element.select("div.info > h3 > a").text().trim();
            if (name.isEmpty()) {
                continue;
            }
            String desc = element.select("div.info > div.desc").text().trim();
            SiteEntity siteEntity = new SiteEntity()
                    .setName(name)
                    .setUrl(getUrl(element))
                    .set("desc", desc)
                    .addCategory(categoryEntity.getName(), getNo(element))
                    .addTags(categoryEntity.getTags())
                    .addRanking("alexa", getAlexa(element))
                    .addRanking("baiduWeight", getBaiduWeight(element))
                    .addRanking("pr", getPR(element))
                    .addRanking("reverseLink", getReverseLinkNum(element));
            getLogger().trace(siteEntity);
            sites.add(siteEntity);
        }

        String nextUrl = getNextPageUrl(doc);
        if (nextUrl != null) {
            Request request = new Request(nextUrl, processor)
                    .putExtra("categoryEntity", categoryEntity);
            page.addTargetRequest(request);
        }
        sites.forEach(page::addItem);
    }

    String getNextPageUrl(Document doc) {
        Element element = doc.select("#divNextValue > div > a").last();
        if (element == null) {
            getLogger().warn("Failed to get next page element of " + doc.baseUri());
            return null;
        }
        String nextUrl = element.absUrl("href");
        if (nextUrl.equals(doc.baseUri())) {
            return null;
        } else {
            return nextUrl;
        }
    }

    String getUrl(Element element) {
        String siteUrl = element.select("div.info > h3 > span").text().trim();
        return UrlHelper.eraseProtocolAndStart3W(siteUrl);
    }

    int getNo(Element element) {
        try {
            String str = element.select("div.rank > div").text().trim();
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage() + " and base url is " + element.baseUri());
        }
    }

    Integer getAlexa(Element element) {
        try {
            String str = element.select("div.info > div.score > span:nth-child(1)").text();
            str = str.substring(str.indexOf("：") + 1).trim();
            if (str.contains("-")) {
                return null;
            }
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage() + " and base url is " + element.baseUri());
        }
    }

    Integer getReverseLinkNum(Element element) {
        try {
            String str = element.select("div.info > div.score > span:nth-child(4)").text();
            str = str.substring(str.indexOf("：") + 1).trim();
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage() + " and base url is " + element.baseUri());
        }
    }

    Integer getBaiduWeight(Element element) {
        String imgUrl = element.select("div.info > div.score > span:nth-child(2) > img").attr("src");
        for (int i = 0; i <= 10; ++i) {
            if (imgUrl.contains(Integer.toString(i))) {
                return i;
            }
        }
        return null;
    }

    Integer getPR(Element element) {
        String imgUrl = element.select("div.info > div.score > span:nth-child(3) > img").attr("src");
        for (int i = 0; i <= 10; ++i) {
            if (imgUrl.contains(Integer.toString(i))) {
                return i;
            }
        }
        return null;
    }
}
