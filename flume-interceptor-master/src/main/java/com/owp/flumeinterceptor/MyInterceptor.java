package com.owp.flumeinterceptor;

import com.google.gson.Gson;
import org.apache.commons.codec.Charsets;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class MyInterceptor implements Interceptor {
    //打印日志，便于测试方法的执行顺序
    private static final Logger logger = LoggerFactory.getLogger(MyInterceptor.class);

    //自定义拦截器参数，用来接收自定义拦截器flume配置参数
    private static String param = "";

    @Override
    public void initialize() {
        logger.info("----------自定义拦截器initialize方法执行");
    }

    @Override
    public Event intercept(Event event) {
        logger.info("----------intercept(Event event)方法执行");
        if (event == null) return null;
        // 通过获取event数据，转化成字符串
        String line = new String(event.getBody(), Charsets.UTF_8);
        logger.info("Kafka数据，header：【{}】，数据：【{}】", event.getHeaders(), line);
        Gson gson = new Gson();
        //解析kafka读取到的数据
        LinkedHashMap<String, Object> lineInfo = gson.fromJson(line, LinkedHashMap.class);
        //处理后的新数据
        Map<String, String> cdJsonMap = new LinkedHashMap();
        //TODO 添加处理逻辑
        String kafkaData = gson.toJson(cdJsonMap);
        //重新设置event中的body（当然这里也可以设置even其他属性，例如header）
        event.setBody(kafkaData.getBytes(Charsets.UTF_8));
        return event;
    }
    @Override
    public List<Event> intercept(List<Event> list) {
        logger.info("----------intercept(List<Event> events)方法执行");
        if (list == null) return null;
        List<Event> events = new ArrayList<Event>();
        for (Event event : list) {
            Event intercept = intercept(event);
            events.add(intercept);
        }
        return events;
    }
    @Override
    public void close() {
        logger.info("----------自定义拦截器close方法执行");
    }

    /**
     * 通过该静态内部类来创建自定义对象供flume使用，实现Interceptor.Builder接口，并实现其抽象方法
     */
    public static class Builder implements Interceptor.Builder {
        /**
         * 该方法主要用来返回创建的自定义类拦截器对象
         *
         * @return
         */
        @Override
        public Interceptor build() {
            logger.info("----------build方法执行");
            return new MyInterceptor();

        }

        /**
         * 用来接收flume配置自定义拦截器参数
         *
         * @param context 通过该对象可以获取flume配置自定义拦截器的参数
         */
        @Override
        public void configure(Context context) {
            logger.info("----------configure方法执行");
            /*
            通过调用context对象的getString方法来获取flume配置自定义拦截器的参数，方法参数要和自定义拦截器配置中的参数保持一致+
             */
            param = context.getString("param");
        }
    }
}