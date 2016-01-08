package cmri.tagbase.b2c.mobile.jd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Created by zhuyin on 3/3/15.
 */
class JsonGoodsPageProcessor implements PageProcessor {
    private static final JsonGoodsPageProcessor processor =new JsonGoodsPageProcessor();

    public static Request getRequest(CategoryEntity category, String url){
        return new Request(url, processor)
                .setTarget(Request.TargetResource.Json)
                .putExtra("categoryEntity", category);
    }
    @Override
    public void process(ResultItems page) {
        String url = page.getRequest().getUrl();
        getLogger().trace("extract page data: " + url);
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);

        try {
            String strJson = (String) page.getResource();
            JsonParser parser = new JsonParser();
            JsonObject ojson = parser.parse(strJson).getAsJsonObject();
            JsonArray ajson = ojson.get("wares").getAsJsonArray();
            for (JsonElement e : ajson) {
                JsonObject jGoods = e.getAsJsonObject();
                String name = jGoods.get("wname").getAsString();
                String strId = jGoods.get("wareId").getAsString();
                String wUrl = String.format("http://m.jd.com/product/%s.html", strId);
                String desc = jGoods.get("adword").getAsString();

                KindEntity goodsEntity = new KindEntity("goods")
                        .setName(name)
                        .setUrl(wUrl)
                        .setCategory(categoryEntity)
                        .setCode(strId)
                        .set("desc", desc)
                        .set("price", getPrice(jGoods))
                        .set("scoreNum", getScoreNum(jGoods))
                        .set("scoreValue", getGood(jGoods));
                getLogger().trace(goodsEntity);
                page.addItem(goodsEntity);
            }
        } catch (IllegalStateException | JsonSyntaxException | UnsupportedOperationException ex) {
            throw new RuntimeException("url: " + url, ex);
        }
    }

    Double getPrice(JsonObject jGoods) {
        JsonElement jPrice = jGoods.get("jdPrice");
        String strPrice;
        if (jPrice.isJsonNull()) {
            return null;
        } else {
            strPrice = StringUtils.strip(jPrice.getAsString());
            if (strPrice.contains("无报价") || strPrice.isEmpty()) {
                return null;
            }
        }
        return Double.parseDouble(strPrice);
    }

    Double getGood(JsonObject jGoods) {
        String strGood = StringUtils.strip(jGoods.get("good").getAsString());
        if (strGood.contains("无评价") || strGood.isEmpty()) {
            return null;
        } else {
            strGood = strGood.substring(0, strGood.indexOf("%"));
        }
        return Double.parseDouble(strGood);
    }

    Integer getScoreNum(JsonObject jGoods) {
        String strScoreNum = jGoods.get("totalCount").getAsString().trim();
        return Integer.parseInt(strScoreNum);
    }
}
