package tech.kavi.vs.mybatis

import com.alibaba.druid.pool.DruidDataSource
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import tech.kavi.vs.core.VertxBeansBase.Companion.value
import java.io.IOException
import java.util.*
import javax.sql.DataSource
import org.mybatis.spring.mapper.MapperScannerConfigurer
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import tech.kavi.vs.mybatis.LocalThreadPoolHolder.Companion.LOCAL_THREAD_POOL_BEAN
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService


/**
 * 根据配置参数加载多个mybatis数据源
 * */
open class MybatisDataSourceBean : BeanFactoryPostProcessor {

    open val CONFIG_FILE = "config.mybatis.json"

    /**
     * 捕获（beanDenifition加载完成之后，bean实例化之前执行）
     * */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val factory: DefaultListableBeanFactory = beanFactory as DefaultListableBeanFactory
        // 加载mybatis配置参数
        this.loadMybatisConfig(factory)
        // 加载自定义本地线程池
        this.loadLocalThreadPool(factory)
    }

    /**
     * 加载本地自定义线程池
     * */
    private fun loadLocalThreadPool(beanFactory: DefaultListableBeanFactory) {
        try {
            // 加载自定义线程池
            if (beanFactory.containsBean(LOCAL_THREAD_POOL_BEAN)) {
                LocalThreadPoolHolder.executorService = beanFactory.getBean(LOCAL_THREAD_POOL_BEAN) as ExecutorService
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 加载mybatis参数
     * @param beanFactory
     * */
    private fun loadMybatisConfig(beanFactory: DefaultListableBeanFactory){
        val configJson = loadLocalConfig(CONFIG_FILE)
        try {
            configJson.mergeIn((beanFactory.getBean("config") as JsonObject), true)
            // 循环加载配置项
        } catch (e: Exception) {
            logger.error(e)
        }
        configJson.value<JsonArray>("mybatis")?.forEach{ this.registerDataSource(it, beanFactory) }
    }

    /**
     * 根据Mybatis配置项注册数据源
     * @param value 配置参数项
     * @param beanFactory
     * */
    private fun registerDataSource(value: Any, beanFactory: DefaultListableBeanFactory){
        try {
            val mybatisConfig = (value as JsonObject).mapTo(MybatisConfig::class.java)
            val name = mybatisConfig.name ?: throw NullPointerException("MybatisConfig name is null")
            // 构建数据源
            val dataSource = this.createDataSource(name, mybatisConfig.dataSource, beanFactory)
            // 构建SessionFactoryBean
            if (mybatisConfig.hasSqlSessionFactoryParam()) {
                this.createSqlSessionFactoryBean(name, dataSource, mybatisConfig, beanFactory)
            }
            // 是否开启事务
            if (mybatisConfig.isTransaction) {
                this.createTransactionManager(name, dataSource, beanFactory)
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    /**
     * 创建SqlSessionFactoryBean
     * */
    private fun createSqlSessionFactoryBean(name: String,
                                            dataSource: DataSource,
                                            mybatisConfig: MybatisConfig,
                                            beanFactory: DefaultListableBeanFactory): SqlSessionFactoryBean {
        val sessionFactory = SqlSessionFactoryBean()
            sessionFactory.setDataSource(dataSource)
        try {
            // 对应我们的实体类所在的包，这个时候会自动取对应包中不包括包名的简单类名作为包括包名的别名。
            // 多个package之间可以用逗号或者分号等来进行分隔（value的值一定要是包的全）
            // 设置这个以后在Mapper XML配置文件中在parameterType 的值就不用写成全路径名了
            if (mybatisConfig.typeAliasesPackage.isNotEmpty()) {
                sessionFactory.setTypeAliasesPackage(mybatisConfig.typeAliasesPackage.joinToString(","))
            }
            //表示我们的Mapper文件存放的位置，当我们的Mapper文件跟对应的Mapper接口处于同一位置的时候可以不用指定该属性的值
            if (mybatisConfig.mapperLocations.isNotEmpty()){
                val resolver = PathMatchingResourcePatternResolver()
                val mappers = mybatisConfig.mapperLocations.fold(listOf<Resource>()){ acc, s ->
                   acc + resolver.getResources(s)
                }.toTypedArray()
                sessionFactory.setMapperLocations(*mappers)
            }
            if (mybatisConfig.basePackage.isNotEmpty()) {
                this.basicMapperScannerConfigurer(name, mybatisConfig.basePackage.joinToString(","), beanFactory)
            }
        } catch (e: IOException) {
            logger.error(e)
        }
        beanFactory.registerSingleton(name + SESSION_FACTOR, sessionFactory)
        return sessionFactory
    }

    /**
     * 映射扫描
     * */
    private fun basicMapperScannerConfigurer(name:String, basePackage: String, beanFactory: DefaultListableBeanFactory): MapperScannerConfigurer {
        val mapperScannerConfigurer = MapperScannerConfigurer()
        // 属性多路径配置仅需通过英文逗号”,”分隔开不同路径即可。
        mapperScannerConfigurer.setBasePackage(basePackage)
        mapperScannerConfigurer.setSqlSessionFactoryBeanName(name + SESSION_FACTOR)
        mapperScannerConfigurer.postProcessBeanDefinitionRegistry(beanFactory)
        return mapperScannerConfigurer
    }

    /**
     * 是否使用事务
     * */
    private fun createTransactionManager(name:String, dataSource: DataSource,beanFactory: DefaultListableBeanFactory){
        val transactionManager = DataSourceTransactionManager()
        transactionManager.dataSource = dataSource
        beanFactory.registerSingleton(name + TRANSACTION, transactionManager)
    }

    /**
     * 创建数据源
     * */
    private fun createDataSource(name:String, config: Map<String, String>, beanFactory: DefaultListableBeanFactory): DataSource {
        val dataSource = DruidDataSource()
        val properties = Properties()
        config.iterator().forEachRemaining {
            properties[when (it.key.contains("druid.")) {
                true -> it.key
                else -> "druid." + it.key
            }] = it.value }
        dataSource.configFromPropety(properties)
        beanFactory.registerSingleton(name + DATA_SOURCE, dataSource)
        return dataSource
    }

    companion object{
        const val DATA_SOURCE = "-dataSource"
        const val SESSION_FACTOR = "-sessionFactor"
        const val TRANSACTION = "-transaction"

        private val logger = LoggerFactory.getLogger(MybatisDataSourceBean::class.java)

        /**
         * 加载配置mybatis config文件
         * */
        fun loadLocalConfig(configFile: String): JsonObject {
            return try {
                val res = ClassPathResource(configFile)
                val stringBuffer = StringBuilder()
                val bufferedReader = BufferedReader(InputStreamReader(res.inputStream, StandardCharsets.UTF_8))
                var nextLine: String? = bufferedReader.readLine()
                while (nextLine != null) {
                    stringBuffer.append(nextLine)
                    nextLine = bufferedReader.readLine()
                }
                bufferedReader.close()
                JsonObject(stringBuffer.toString())
            } catch (e: Exception) {
                logger.error(e)
                JsonObject()
            }
        }
    }
}