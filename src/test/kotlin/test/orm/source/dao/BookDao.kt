package test.orm.source.dao

import test.orm.source.entity.Book


interface BookDao{
    /**
     * 添加用户
     * @param user 用户实体类
     * */
    fun add(book: Book): Int
}