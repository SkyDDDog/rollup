package com.lyd.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lyd.controller.VO.PrePostVO;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.stylesheets.LinkStyle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RequestUtil {

    public static List<String> getRecIdsByRequestUrl(Long userId) throws Exception {
        // 定义请求路径
        String prefix = "http://";
        String ip = "27.158.103.146";
        String port = "65000";
        String path = "/api/v1/recommend/";
        String requestUrl = prefix+ip+":"+port+path;
        if (userId!=null) {
            requestUrl += userId;
        }

        // 创建连接并设置参数
        URL url = new URL(requestUrl);
        HttpURLConnection httpCoon = (HttpURLConnection) url.openConnection();
        httpCoon.setRequestMethod("GET");
        httpCoon.setRequestProperty("Charset","UTF-8");
        httpCoon.setConnectTimeout(100);

//         发起http请求 (getInputStream触发http请求)
        if (httpCoon.getResponseCode() != 200) {
            throw new Exception("调用请求异常,状态码:"+httpCoon.getResponseCode());
        }

        // 获取输入流和读数据
        BufferedReader br = new BufferedReader(new InputStreamReader(httpCoon.getInputStream()));
        String resultData = br.readLine();
        JSONObject jsonObject = JSON.parseObject(resultData);
        Integer code = (Integer) jsonObject.get("statue");
        String msg = (String) jsonObject.get("message");
        JSONArray data = jsonObject.getJSONArray("data");
        log.info("py接口状态码:"+code.toString());
        log.info("msg:"+msg);
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String id = data.get(i).toString();
//            log.info(id);
            res.add(id);
        }
        // 关闭连接
        httpCoon.disconnect();
        return res;
    }


}
