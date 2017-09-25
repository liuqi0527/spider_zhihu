package com.example.spider.util;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/8/8
 */

public interface FunctionWithException<P, R> {

    R apply(P p) throws Exception;

}
