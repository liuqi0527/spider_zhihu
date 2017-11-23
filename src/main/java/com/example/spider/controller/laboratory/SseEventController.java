package com.example.spider.controller.laboratory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.example.spider.domain.Question;
import com.example.spider.util.JsonUtil;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/8/8
 */

@Controller
@RequestMapping("/stream")
public class SseEventController {

    @RequestMapping("/start")
    private ModelAndView begin() {
        Map<String, Object> map = new HashMap<>();
        map.put("subTopicId", 10);
        map.put("list", Collections.emptyList());
        return new ModelAndView("/laboratory/sseEvent", map);
    }

    @RequestMapping("/emitter/{count}")
    private ResponseBodyEmitter testEmitter(@PathVariable("count") String count) {
        System.out.println("begin " + count);
        int max = NumberUtils.isCreatable(count) ? Integer.parseInt(count) : 5;

        SseEmitter emitter = new SseEmitter(1000000L);

        new Thread(() -> {
            try {
                for (int i = 0; i < max; i++) {
                    Thread.sleep(1000);

                    Question question = new Question(i, 10010);
                    question.setTitle("title_" + i);
                    question.setDescription("des_" + i);
                    question.setAuthor("author_" + i);
                    question.addTag("tag_" + i);
                    question.setCommentCount(RandomUtils.nextInt());
                    question.setAnswerCount(RandomUtils.nextInt());
                    question.setViewCount(RandomUtils.nextInt());
                    question.setFocusCount(RandomUtils.nextInt());
                    emitter.send(JsonUtil.toJson(question));
                }
                emitter.send(SseEmitter.event().name("shutdown").data("shutdown"));
                emitter.complete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return emitter;
    }
}
