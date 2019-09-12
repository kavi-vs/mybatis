package test.orm.source

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import org.springframework.context.annotation.Bean
import tech.kavi.vs.core.VertxBeans
import tech.kavi.vs.mybatis.LocalThreadPoolHolder.Companion.LOCAL_THREAD_POOL_BEAN
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BeanConfig : VertxBeans() {
    /**
     * 注入router
     */
    //todo route end aop注入捕捉每次end时增加方法
    @Bean
    fun router(vertx: Vertx) = Router.router(vertx)

    /**
     * 初始化ThreadPoolTaskExecutor参数
     * */
    @Bean(LOCAL_THREAD_POOL_BEAN)
    fun initThreadPool(): ExecutorService {
        return Executors.newFixedThreadPool(10)
    }
}