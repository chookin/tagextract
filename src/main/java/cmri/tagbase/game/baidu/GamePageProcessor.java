package cmri.tagbase.game.baidu;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by zhuyin on 6/9/15.
 */
public class GamePageProcessor implements PageProcessor {
    private static final GamePageProcessor processor = new GamePageProcessor();

    public static Request getRequest(CategoryEntity category){
        return getRequest(category, 1);
    }

    private static Request getRequest(CategoryEntity category, int pageIndex){
        String url = "http://iwan.baidu.com/MobilegameAjax/selectGame?type="+category.getCode() + "&page="+pageIndex;
        return new Request(url, processor)
                .putExtra("categoryEntity", category)
                .putExtra("pageIdx", pageIndex)
                .setTarget(Request.TargetResource.Json)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        String strJson = (String) page.getResource();
        JsonObject ojson = new JsonParser().parse(strJson).getAsJsonObject();
        JsonObject dataJson = ojson.get("data").getAsJsonObject();
        JsonArray listJson = dataJson.get("list").getAsJsonArray();

        for (JsonElement e : listJson) {
            JsonObject jGame = e.getAsJsonObject();

            String name = jGame.get("title").getAsString();
            name = StringEscapeUtils.unescapeJava(name);
            String strId = jGame.get("gameid").getAsString();

            KindEntity game = new KindEntity("game")
                    .setName(name)
                    .setCategory(category)
                    .setCode(strId);
            if(jGame.has("trueurl")){
                // "trueurl" : "http:\/\/iwan.baidu.com\/mobilegame\/detailMobilegame?gameid=918",
                String url = jGame.get("trueurl").getAsString().replace("\\", "");
                game.setUrl(url);
            }else if(jGame.has("url")){
                String url = jGame.get("url").getAsString().replace("\\", "");
                game.setUrl(url);
            }else{
                getLogger().warn("fail to get url for "+game);
                continue;
            }
            if(jGame.has("intro")){
                String desc = jGame.get("intro").getAsString();
                game.set("desc", desc);
            }
            if(jGame.has("score")){
                int scoreValue = jGame.get("score").getAsInt() * 10;
                game.set("scoreValue", scoreValue);
            }
            if(jGame.has("download_times")){
                int downloadTimes = jGame.get("download_times").getAsInt();
                game.set("downloadTimes",downloadTimes);
            }
            getLogger().trace(game);
            page.addItem(game);
        }

        int pageIdx = page.getRequest().getExtra("pageIdx", Integer.class);
        if(pageIdx == 1) {
            int totalPage = dataJson.get("totalpage").getAsInt();
            for(int idx = 2; idx<totalPage; ++idx){
                page.addTargetRequest(getRequest(category, idx));
            }
        }
    }
}
