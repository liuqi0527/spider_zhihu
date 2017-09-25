package com.example.spider.controller;

import com.example.spider.dao.MainTopicRepository;
import com.example.spider.dao.SubTopicRepository;
import com.example.spider.domain.MainTopic;
import com.example.spider.domain.SubTopic;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;
import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/25
 */

@Controller
@RequestMapping("/topic")
public class TopicController {

    @Autowired
    private MainTopicRepository mainTopicRepository;

    @Autowired
    private SubTopicRepository subTopicRepository;

    @GetMapping
    public ModelAndView listMainTopic() {
        List<MainTopic> list = mainTopicRepository.findAll();
        if (CollectionUtils.isEmpty(list)) {
            list = fetchMainTopic();
        }

        return new ModelAndView("/zhihu/mainList", "list", list);
    }

    @PostMapping
    @ResponseBody
    public ModelAndView reloadMainTopic() {
        return new ModelAndView("/zhihu/mainList", "list", fetchMainTopic());
    }

    @GetMapping("{id}")
    public ModelAndView listSubTopic(@PathVariable("id") long mainTopicId) {
        MainTopic mainTopic = mainTopicRepository.findOne(mainTopicId);
        if (mainTopic == null) {
            return new ModelAndView("/error", "message", "main topic not found");
        }

        List<SubTopic> list = subTopicRepository.findByParentId(mainTopicId);
        if (CollectionUtils.isEmpty(list)) {
            list = fetchSubTopic(mainTopicId, 20);
        }

        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("list", list);
        modelMap.put("topicName", mainTopic.getName());
        return new ModelAndView("/zhihu/subList", modelMap);
    }

    private List<SubTopic> fetchSubTopic(long mainTopicId, int pageSize) {
        List<SubTopic> list = new ArrayList<>();
        UnicodeUnescaper unicodeUnescaper = new UnicodeUnescaper();
        String requestParam = "{\"topic_id\":%s,\"offset\":%s,\"hash_id\":\"37492588249aa9b50ee49d1797e9af81\"}";
        try {
            int offset = 0;
            for (int i = 0; i < pageSize; i++) {
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setRetryHandler(new DefaultHttpRequestRetryHandler())
                        .setConnectionManager(new PoolingHttpClientConnectionManager())
                        .build();

                List<BasicNameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("method", "next"));
                params.add(new BasicNameValuePair("params", String.format(requestParam, mainTopicId, offset)));

                HttpPost req = new HttpPost("https://www.zhihu.com/node/TopicsPlazzaListV2");
                req.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
                HttpResponse resp = httpClient.execute(req);
                String sb = EntityUtils.toString(resp.getEntity());
                if (sb.length() < 25)
                    break;


                //<div class=\"blk\">\n<a target=\"_blank\" href=\"\/topic\/19550517\">\n
                // <img src=\"https:\/\/pic4.zhimg.com\/f07808da5625fef3607f8b75b770349f_xs.jpg\"
                // alt=\"\u4e92\u8054\u7f51\">
                Matcher matcher = Pattern.compile("href=\\\\\"\\\\/topic\\\\/(\\d+)\\\\\".*?alt=\\\\\"(.+?)\\\\\"").matcher(sb);
                while (matcher.find()) {
                    long childTopicId = Long.parseLong(matcher.group(1));
                    String childTopicName = unicodeUnescaper.translate(matcher.group(2));
                    list.add(new SubTopic(childTopicId, mainTopicId, childTopicName));
                    System.out.println("       " + childTopicId + "  " + childTopicName);
                }
                offset += 20;
            }
            subTopicRepository.save(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<MainTopic> fetchMainTopic() {
        try {
            URL url = new URL("https://www.zhihu.com/topics");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader bfr = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = (bfr.readLine())) != null) {
                sb.append(line);
            }
            String data = sb.toString();
            if (StringUtils.isEmpty(data)) {
                return Collections.emptyList();
            }

            List<MainTopic> list = new ArrayList<>();
            //<li class="zm-topic-cat-item" data-id="304"><a href="#美食">美食</a></li>
            Matcher matcher = Pattern.compile("data-id=\"(\\d+)\".*?href=\"#(.+?)\"").matcher(data);
            while (matcher.find()) {
                long topicId = Long.parseLong(matcher.group(1));
                String topicName = matcher.group(2);
                list.add(new MainTopic(topicId, topicName));
                System.out.println(topicId + " " + topicName);
            }

            mainTopicRepository.deleteAllInBatch();
            mainTopicRepository.save(list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
