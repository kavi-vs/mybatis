package test.orm.source.dao

import test.orm.source.entity.User

interface UserDao{
    fun add(user: User): Int
    fun listUser(): List<User>
}