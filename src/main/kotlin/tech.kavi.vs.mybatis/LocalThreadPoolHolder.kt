package tech.kavi.vs.mybatis

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newCachedThreadPool

class LocalThreadPoolHolder{
    companion object{
        // 线程池日志
        var logger: Logger =  LoggerFactory.getLogger(LocalThreadPoolHolder::class.java)
        var LOCAL_THREAD_POOL_NAME : String = "LOCAL-THREAD-POOL"
        const val LOCAL_THREAD_POOL_BEAN : String = "LocalThreadPool-Bean"
        var executorService : ExecutorService = newCachedThreadPool()
    }
}