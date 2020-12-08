package com.atguigu.gmall.config;

//类型annocation
public @interface GmallCache {
    public String skuPrefix() default "sku:";

    public String spuPrefix() default "spu:";

    public String prefix() default "GmallCache:";
}
