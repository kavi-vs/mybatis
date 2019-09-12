package tech.kavi.vs.mybatis

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.rx.java.RxHelper
import rx.Observable
import rx.Single

/**
 * 基于Observable异步获取方法结果
 * eg: observableAsync{ userDao.listUser() }.subscribe()
 * */
inline fun <T : Any> observableAsync(crossinline operation: () -> T): Observable<T> {
    val resultHandler = RxHelper.observableFuture<T>()
    handlerAsync({operation.invoke()}, resultHandler.toHandler())
    return resultHandler
}

/**
 * 基于Handler<AsyncResult>异步获取方法结果
 * eg: handlerAsync({ userDao.listUser() }, Handler{})
 * */
inline fun <T : Any> handlerAsync(crossinline operation: () -> T,  resultHandler: Handler<AsyncResult<T>>) {
    LocalThreadPoolHolder.executorService.execute {
        Thread.currentThread().name = LocalThreadPoolHolder.LOCAL_THREAD_POOL_NAME
        try {
            resultHandler.handle(Future.succeededFuture(operation.invoke()))
        } catch (e: Exception) {
            resultHandler.handle(Future.failedFuture(e))
            LocalThreadPoolHolder.logger.error(e)
        }
    }
}

/**
 * 基于Single异步获取方法结果
 * */
inline fun <T : Any> singleAsync(crossinline operation: () -> T): Single<T> {
    return observableAsync{ operation.invoke() }.toSingle()
}
