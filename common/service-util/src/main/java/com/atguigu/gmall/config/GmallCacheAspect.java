package com.atguigu.gmall.config;

import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Aspect//切面
public class GmallCacheAspect {
    @Autowired
    RedisTemplate redisTemplate;

    // 环绕通知 中 切入点表达式中 定义一个注解 未来切入其他方法时 直接使用注解
    @Around("@annotation(com.atguigu.gmall.config.GmallCache)")
                                //连接点(通过反射)
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        Object result = null;
        //拼接①
        String cacheKey = "";
        //1 获取KEY
            //通过反射得到所有方法信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            //方法名和返回值类型
            String name = signature.getMethod().getName();
            Class returnType = signature.getReturnType();
            //拼接⑤
            cacheKey= name;

            //获得注解信息
            GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);

            //参数
            Object[] args = point.getArgs();
            for (Object arg : args) {
                //拼接⑤
                cacheKey=cacheKey+":"+arg;
            }
        //2查询缓存
        result = redisTemplate.opsForValue().get(cacheKey);
        //3 没有缓存->DB
        if(null==result){

        }

        try {//4执行分布式锁
            String key = UUID.randomUUID().toString();
            //                                "sku:" + skuId + ":lock"
            Boolean OK = redisTemplate.opsForValue().setIfAbsent(cacheKey+ ":lock", key, 2, TimeUnit.SECONDS);
            //有锁
            if(OK){
                // 执行被代理方法
                result = point.proceed();
                //数据库为空
                if(null!=result){
                    redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.SECONDS);
                    //数据库有结果
                }else {
                    //同步缓存
                    redisTemplate.opsForValue().set(cacheKey,result);
                    //释放锁
                    String openKey = (String)redisTemplate.opsForValue().get(cacheKey+":lock");
                    if(key.equals(openKey)){
                        redisTemplate.delete(cacheKey +":lock");
                    }else{ //没有锁
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return redisTemplate.opsForValue().get(cacheKey);//1远程调用 2过段时间自动重新查询
                    }

                }
            }


        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return result;
    }
}
