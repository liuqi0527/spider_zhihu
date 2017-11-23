package com.example.spider.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.example.spider.dao.MainTopicRepository;
import com.example.spider.dao.QuestionRepository;
import com.example.spider.dao.SubTopicRepository;
import com.example.spider.domain.MainTopic;
import com.example.spider.domain.Question;
import com.example.spider.domain.SubTopic;
import com.example.spider.util.FunctionWithException;
import com.example.spider.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/27
 */

@Controller
@RequestMapping("/quest")
public class QuestController {
    private static final int PAGE_SIZE = 500;

    @Autowired
    private MainTopicRepository mainTopicRepository;

    @Autowired
    private SubTopicRepository subTopicRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private Map<Long, List<SseEmitter>> sseEmitterMap = new ConcurrentHashMap<>();

    private ExecutorService executorService;

    @PostConstruct
    private void init() {
        executorService = Executors.newCachedThreadPool();
    }

    @GetMapping("{id}/{page}")
    public ModelAndView listQuest(@PathVariable("id") long subTopicId, @PathVariable("page") int page) {
        SubTopic topic = subTopicRepository.findById(subTopicId);
        if (topic == null) {
            return new ModelAndView("/error", "message", "subTopic not found");
        }
        MainTopic mainTopic = mainTopicRepository.findOne(topic.getParentId());
        List<Question> list = questionRepository.findByParentId(subTopicId);

        int startIndex = Math.min(page * PAGE_SIZE, list.size());
        int endIndex = Math.min(startIndex + PAGE_SIZE, list.size());

        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put("list", list.subList(startIndex, endIndex));
        modelMap.put("subTopic", topic);
        modelMap.put("mainTopic", mainTopic);
        modelMap.put("page", startIndex / PAGE_SIZE);
        modelMap.put("pageSize", Math.max(list.size() / PAGE_SIZE - 1, 0));
        return new ModelAndView("zhihu/questList", modelMap);
    }

    @GetMapping("load/{id}")
    public SseEmitter loadQuest(@PathVariable("id") long subTopicId) {
        List<SseEmitter> list = sseEmitterMap.get(subTopicId);
        if (list == null) {
            list = new ArrayList<>();
            sseEmitterMap.put(subTopicId, list);
            fetchQuest(subTopicId);
        }
        SseEmitter sseEmitter = new SseEmitter();
        list.add(sseEmitter);
        return sseEmitter;
    }

    private void fetchQuest(long subTopicId) {
        executorService.submit(() -> {
            List<Long> idList = fetchQuestList(subTopicId);
            List<Question> list = new ArrayList<>();
            try {
                CloseableHttpClient client = HttpClients.createDefault();
                for (long id : idList) {
                    System.out.println("load quest " + id);

                    CloseableHttpResponse response = client.execute(new HttpGet("https://www.zhihu.com/question/" + id));
                    String data = EntityUtils.toString(response.getEntity());
                    try {
                        Document document = Jsoup.parse(data);
                        Question question = new Question(id, subTopicId);

                        //title
                        question.setTitle(document.select("h1.QuestionHeader-title").first().text());

                        //content
                        Elements contentEle = document.select("div.QuestionRichText");
                        question.setDescription(contentEle.first().getElementsByTag("span").text());

                        //author
                        String dataJson = document.select("div[data-zop-question]").attr("data-zop-question");
                        JsonNode jsonNode = JsonUtil.objectMapper.readTree(dataJson);
                        question.setAuthor(jsonNode.get("authorName").asText());

                        //关注者   被浏览
                        Elements focusEle = document.select("div.NumberBoard");
                        int index = 0;
                        for (Element element : focusEle.select("div.NumberBoard-value")) {
                            if (index++ == 0) {
                                question.setFocusCount(Integer.parseInt(element.text()));
                            } else {
                                question.setViewCount(Integer.parseInt(element.text()));
                            }
                        }

                        //2 条评论
                        Elements commentEle = document.select("div.QuestionHeader-Comment");
                        question.setCommentCount(parserNumber(commentEle.first().getElementsByTag("button").first().text(), "条评论"));

                        //95 个回答
                        Elements answerEle = document.select("h4.List-headerText");
                        question.setAnswerCount(parserNumber(answerEle.first().child(0).text(), "个回答"));

                        //tag
                        Elements tasElement = document.select("div.QuestionHeader-topics");
                        for (Element element : tasElement.first().select("div.Popover")) {
                            question.addTag(element.child(0).text());
                        }

                        list.add(question);
                        System.out.println("load quest result -> " + JsonUtil.toJson(question));

                        sendSseEvent(subTopicId, sseEmitter -> {
                            sseEmitter.send(JsonUtil.toJson(question));
                            return false;
                        });
                        List<SseEmitter> sseEmitters = sseEmitterMap.computeIfAbsent(subTopicId, key -> new ArrayList<>());
                        sseEmitters.forEach(sseEmitter -> {
                            try {
                                sseEmitter.send(JsonUtil.toJson(question));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(data);
                    }
                }

                sendSseEvent(subTopicId, sseEmitter -> true);
//            questionRepository.deleteAllById(idList);
                questionRepository.save(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendSseEvent(long subTopicId, FunctionWithException<SseEmitter, Boolean> function) {
        List<SseEmitter> list = sseEmitterMap.get(subTopicId);
        if (list == null) {
            return;
        }

        Iterator<SseEmitter> iterator = list.iterator();
        while (iterator.hasNext()) {
            SseEmitter sseEmitter = iterator.next();
            boolean remove;
            try {
                remove = function.apply(sseEmitter);
            } catch (Exception e) {
                e.printStackTrace();
                remove = true;
            }
            if (remove) {
                try {
                    sseEmitter.send(SseEmitter.event().name("shutdown").data("shutdown"));
                    sseEmitter.complete();
                    iterator.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (list.isEmpty()) {
            sseEmitterMap.remove(subTopicId);
        }
    }

    private int parserNumber(String str, String sub) {
        try {
            return StringUtils.contains(str, sub) ? Integer.parseInt(StringUtils.trim(StringUtils.substringBefore(str, sub))) : 0;
        } catch (Exception e) {
            System.out.println(String.format("sub int from=%s, cut=%s", str, sub));
            e.printStackTrace();
            return 0;
        }
    }

    private List<Long> fetchQuestList(long subTopicId) {
        int questCount = 30;
        List<Long> list = new ArrayList<>();
        String dataScore = null;
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            while (true) {
                HttpUriRequest request;
                String regExp = "data-score=\"([\\\\.\\d]+?)\"[\\s\\S]*?href=\"/question/(\\d+?)/answer";
                if (dataScore == null) {
                    request = new HttpGet("https://www.zhihu.com/topic/" + subTopicId + "/hot");
                } else {
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("start", "0"));
                    params.add(new BasicNameValuePair("offset", dataScore));
                    HttpPost post = new HttpPost("https://www.zhihu.com/topic/" + subTopicId + "/hot");
                    post.setEntity(new UrlEncodedFormEntity(params));
                    request = post;
                    regExp = "data-score=\\\\\"([.\\d]+?)\\\\\"[\\S\\s]*?href=\\\\\"\\\\/question\\\\/(\\d+?)\\\\/answer";
                }

                //...itemtype="http://schema.org/Question" data-score="3935.74679152" data-type="Answer"....
                //<a class="question_link" href="/question/62137377" target="_blank" data-id="17079883" data-za-element-name="Title">
                //哪个瞬间让你突然觉得逛知乎真有用？
                // </a>
                CloseableHttpResponse response = client.execute(request);
                String responseData = EntityUtils.toString(response.getEntity());
                Matcher matcher = Pattern.compile(regExp).matcher(responseData);
                boolean find = false;
                while (matcher.find()) {
                    dataScore = matcher.group(1);
                    list.add(Long.parseLong(matcher.group(2)));
                    find = true;
                }

                if (list.size() >= questCount || !find) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        String html = StringUtils.join(Files.readAllLines(Paths.get("test.html")));
        System.out.println(html);

        UnicodeUnescaper unicodeUnescaper = new UnicodeUnescaper();
        System.out.println(unicodeUnescaper.translate(html));
    }

}
