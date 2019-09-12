package tech.kavi.vs.mybatis

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.MapSerializer

@JsonIgnoreProperties(ignoreUnknown = true)
data class MybatisConfig(
        var name: String? = null,                         // 数据源名称标识
        var mapperLocations: List<String> = listOf(),     // XML影射文件所在包
        var basePackage: List<String> = listOf(),         // 数据处理接口包 Mapper接口/Dao接口
        var typeAliasesPackage: List<String> = listOf(),  // 实体类所在包
        var isTransaction: Boolean = false,               // 是否使用事务
        /*序列化为map类型*/
        @JsonSerialize(using= MapSerializer::class)
        var dataSource: Map<String, String> = mapOf()
) {
        /**
         * 判读是否需要SqlSessionFactoryBean
         * */
        fun hasSqlSessionFactoryParam(): Boolean{
                return this.mapperLocations.isNotEmpty() || this.basePackage.isNotEmpty() || this.typeAliasesPackage.isNotEmpty()
        }
}