package com.seven.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/wxTimingPush")
@Controller
public class Pusher {

    @Value("${appId}")
    private String appId;//公众号appId

    @Value("${secret}")
    private String secret;//公众号secret

    @Value("${toUser}")
    private String toUser;//推送的用户,微信id

    @Value("${templateId}")
    private String templateId;//模板id

    @Value("${caiHongPiKey}")
    private String caiHongPiKey;//彩虹屁Key

    @Value("${ingDate}")
    private String ingDate;//恋爱日期

    @Value("${meetDate}")
    private String meetDate;//见面日期

    @Value("${regionCoding}")
    private String regionCoding;//行政区编码

    @Value("${platKey}")
    private String platKey;//百度地图Key

    @RequestMapping(value = "/text", method = RequestMethod.GET)
    @ResponseBody
    Object turnoffList() {
        try {
            this.push();
            return "成功";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // 定时 早7点推送  0秒 0分 7时
    @Scheduled(cron = "0 0 7 * * ?")
    public void goodMorning() {
        this.push();
    }


    public void push() {
        //1，配置
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(appId);
        wxStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);
        // 推送消息
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(toUser)//微信用户id
                .templateId(templateId)//模板id
                .build();
        // 配置你的信息
        Weather weather = this.getWeather();
        templateMessage.addData(new WxMpTemplateData("riqi", weather.getDate() + "  " + weather.getWeek(), "#00BFFF"));
        templateMessage.addData(new WxMpTemplateData("tianqi", weather.getText_now(), "#00FFFF"));
        templateMessage.addData(new WxMpTemplateData("low", weather.getLow() + "", "#173177"));
        templateMessage.addData(new WxMpTemplateData("temp", weather.getTemp() + "", "#EE212D"));
        templateMessage.addData(new WxMpTemplateData("high", weather.getHigh() + "", "#FF6347"));
        templateMessage.addData(new WxMpTemplateData("caihongpi", this.getCaiHongPi(), "#FF69B4"));
        templateMessage.addData(new WxMpTemplateData("lianai", JiNianRiUtils.getLianAi(ingDate) + "", "#FF1493"));
        templateMessage.addData(new WxMpTemplateData("shengri", JiNianRiUtils.getBirthday_Jo(meetDate) + "", "#FFA500"));

        String beizhu = "❤";
        if (JiNianRiUtils.getLianAi(ingDate) % 365 == 0) {
            beizhu = "今天是恋爱" + (JiNianRiUtils.getLianAi(ingDate) / 365) + "周年纪念日！";
        }
        if(JiNianRiUtils.getBirthday_Jo(meetDate)  == 0){
            beizhu = "小李在飞奔过来的路上";
        }
        templateMessage.addData(new WxMpTemplateData("beizhu",beizhu,"#FF0000"));

        try {
            System.out.println(templateMessage.toJson());
            System.out.println(wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage));
        } catch (Exception e) {
            System.out.println("推送失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取撩妹话语
     * @return
     */
    public String getCaiHongPi() {

        String key = caiHongPiKey;
        String httpUrl = "http://api.tianapi.com/caihongpi/index?key="+key;
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray newslist = jsonObject.getJSONArray("newslist");
        String content = newslist.getJSONObject(0).getString("content");
        return content;
    }

    /**
     * 获取地图信息
     * @return
     */
    public  Weather getWeather(){
        RestTemplate restTemplate = new RestTemplate();
        Map<String,String> map = new HashMap<>();
        map.put("district_id",regionCoding);
        map.put("data_type","all");
        map.put("ak",platKey);
        String res = restTemplate.getForObject(
                "https://api.map.baidu.com/weather/v1/?district_id={district_id}&data_type={data_type}&ak={ak}",
                String.class,
                map);
        JSONObject json = JSONObject.parseObject(res);
        System.out.println(json);
        JSONArray forecasts = json.getJSONObject("result").getJSONArray("forecasts");
        List<Weather> weathers = forecasts.toJavaList(Weather.class);
        JSONObject now = json.getJSONObject("result").getJSONObject("now");
        Weather weather = weathers.get(0);
        weather.setText_now(now.getString("text"));
        weather.setTemp(now.getString("temp"));
        return weather;
    }
}
