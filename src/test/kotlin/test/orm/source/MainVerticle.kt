package test.orm.source

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import rx.Single
import rx.schedulers.Schedulers
import tech.kavi.vs.mybatis.MybatisDataSourceBean
import tech.kavi.vs.mybatis.singleAsync
import tech.kavi.vs.web.HandlerRequestAnnotationBeanName
import tech.kavi.vs.web.LauncherVerticle
import test.orm.source.dao.BookDao
import test.orm.source.dao.UserDao
import test.orm.source.entity.Book
import test.orm.source.entity.User
import test.orm.source.mapper.UserMapper
import test.orm.source.mapper2.BookMapper
import java.util.*

/**
 * 主体加载器
 * */
@Import(BeanConfig::class, MybatisDataSourceBean::class)
@ComponentScan
class MainVerticle : LauncherVerticle() {

    @Autowired lateinit var userDao: UserDao

    @Autowired lateinit var bookDao: BookDao

    @Autowired lateinit var bookMapper: BookMapper

    @Throws(Exception::class)
    override fun start() {
        super.start()
        try {
       /*     bookDao.add(Book(1, name=Date().toString()))
            userDao.listUser().forEach {
                println(it)
            }*/
            // 异步获取数据封装

            // rx 异步方式

            println("--------------------------")
            val startTime3 = System.currentTimeMillis()
            singleAsync{
                println("3> ${Thread.currentThread().name}")
                userDao.listUser()
            }.subscribe{
                println("3 > ${System.currentTimeMillis() - startTime3}")
            }
      /*      single<Handler<AsyncResult<String>>>({

            })*/

         /*   bookMapper.listBook().forEach {
                println(it)
            }*/
         /*   mapper.listUser().forEach {
                println(it)
            }*/
        }catch (e: Exception){
            e.printStackTrace()
        }

    }
    fun singles(body: ((String) -> String)?) {

    }

     fun test():String {
         println("run test")
        return "1"
    }



    /**
     * Verticle 入口
     * */
    companion object {
        @JvmStatic
        fun main(args:Array<String>) {
            launcher(MainVerticle::class.java)
        }
    }
}
