package com.example.spider.jsoup;

import java.io.File;
import java.nio.charset.Charset;

import com.example.spider.util.JsonUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/8/2
 */

public class JsoupTest {

    public static void main(String[] args) {
        try {

            File file = new File(JsoupTest.class.getResource("/test.html").toURI());
            String html = FileUtils.readFileToString(file, Charset.defaultCharset());
            Document document = Jsoup.parse(html);
            System.out.println(document.title());
            System.out.println("=====================================================================================================");

            //title
            Elements titleEle = document.select("h1.QuestionHeader-title");
            for (Element e : titleEle) {
                System.out.println(e.html());
            }

            //author
            System.out.println("=====================================================================================================");
            String dataJson = document.select("div[data-zop-question]").attr("data-zop-question");
            System.out.println(JsonUtil.readStringField(dataJson, "authorName"));

            //content
            System.out.println("=====================================================================================================");
            Elements contentEle = document.select("div.QuestionRichText");
            Elements span = contentEle.get(0).getElementsByTag("span");
            System.out.println(span.text());

            //关注者   被浏览
            System.out.println("=====================================================================================================");
            Elements focusEle = document.select("div.NumberBoard");
            for (Element element : focusEle.select("div.NumberBoard-value")) {
                System.out.println(element.text());
            }

            //2 条评论
            System.out.println("=====================================================================================================");
            Elements commentEle = document.select("div.QuestionHeader-Comment");
            System.out.println(parserNumber(commentEle.get(0).getElementsByTag("button").get(0).text(), "条评论"));

            //95 个回答
            System.out.println("=====================================================================================================");
            Elements anwserEle = document.select("h4.List-headerText");
            System.out.println(parserNumber(anwserEle.get(0).child(0).text(), "个回答"));

            //tag
            System.out.println("=====================================================================================================");
            Elements tasElement = document.select("div.QuestionHeader-topics");
            for (Element element : tasElement.get(0).select("div.Popover")) {
                System.out.println(element.child(0).text());
            }


//            Elements element = document.select("div[data-za-module]");
//            for (Element element1 : element) {
//                System.out.println("=====================================================================================================");
//                System.out.println(element1.html());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parserNumber(String str, String sub) {
        return Integer.parseInt(StringUtils.trim(StringUtils.substringBefore(str, sub)));
    }
}
