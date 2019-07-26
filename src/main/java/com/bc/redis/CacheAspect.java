package com.bc.redis;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;


@Component
@Aspect
public class CacheAspect {

    public static final Logger infoLog = LoggerFactory.getLogger(CacheAspect.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @SuppressWarnings("unchecked")
    @Around("@annotation(com.bc.redis.RedisCache)")
    public Object RedisCache(final ProceedingJoinPoint jp) throws Throwable{
        Method method = getMethod(jp);

        RedisCache cache = method.getAnnotation(RedisCache.class);

        Object[] args = jp.getArgs();

        final String key = parseKey(cache.fieldKey(), method,jp.getArgs());
        infoLog.info("生成key:" + key);
        infoLog.debug("生成key:" + key);
        infoLog.warn("生成key:" + key);
        infoLog.error("生成key:" + key);

        // 得到被代理的方法上的注解
        Class modelType = method.getAnnotation(RedisCache.class).type();

        // 检查redis中是否有缓存
        String value = (String) redisTemplate.opsForHash().get(modelType.getName(), key);

        // 最终返回结果
        Object result = null;
        if (null == value) {
            // 缓存未命中
            infoLog.info("缓存未命中");

            // 调用数据库查询方法
            result = jp.proceed(args);

            // 序列化查询结果
            final String json = serialize(result);
            final String hashName = modelType.getName();
            final int expire = cache.expire();

            // 序列化结果放入缓存
            redisTemplate.opsForHash().put(modelType.getName(), key, json);
        }else{
            // 缓存命中
            infoLog.info("缓存命中");
            Class returnType = ((MethodSignature)jp.getSignature()).getReturnType();

            // 反序列化从缓存拿到的json
            result = deserialize(value, returnType, modelType);

            infoLog.info("反序列化结果:" + result);
        }
        return result;

    }

    /**
     * 删除缓存
     */
    @Around("@annotation(com.bc.redis.RedisEvict)")
    public Object RedisEvict(final ProceedingJoinPoint jp) throws Throwable {
        Method me = ((MethodSignature) jp.getSignature()).getMethod();

        Class modelType = me.getAnnotation(RedisEvict.class).type();
        infoLog.info("清空缓存:" + modelType.getName());

        // 清除对应缓存
        redisTemplate.delete(modelType.getName());

        return jp.proceed(jp.getArgs());
    }


    /**
     * 获取被拦截的方法
     * MethodSignature.getMethod()获取的是顶层接口或者父类的方法对象，而缓存的注解在实现类的方法上，所以应该使用反射
     * 获取当前对象的方法对象
     * @param pjp
     * @return
     */
    public Method getMethod(ProceedingJoinPoint pjp){


        Object[] args = pjp.getArgs();
        Class[] argTypes = new Class[pjp.getArgs().length];
        for (int i = 0; i < args.length; i++){
            argTypes[i] = args[i].getClass();
        }
        Method method = null;

        try {
            Signature signature = pjp.getSignature();
            MethodSignature methodSignature = (MethodSignature)signature;
            method = methodSignature.getMethod();
           // method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(), argTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return method;
    }

    /**
     * 获取缓存的key
     */
    private String parseKey(String key, Method method, Object[] args){
        // 获取被拦截方法参数名列表（使用Spring支持类库）
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++){
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }

    /**
     * 序列化
     */
    protected String serialize(Object target){
        return JSON.toJSONString(target);
    }

    /**
     * 反序列化
     */
    protected Object deserialize(String jsonString, Class clazz, Class modelType){
        if (clazz.isAssignableFrom(List.class)){
            return JSON.parseArray(jsonString, modelType);
        }

        return JSON.parseObject(jsonString, clazz);
    }
}
