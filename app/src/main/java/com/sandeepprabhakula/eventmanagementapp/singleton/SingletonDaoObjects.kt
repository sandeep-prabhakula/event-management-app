package com.sandeepprabhakula.eventmanagementapp.singleton

import com.sandeepprabhakula.eventmanagementapp.daos.EventsDao
import com.sandeepprabhakula.eventmanagementapp.daos.UserDao

object SingletonDaoObjects {
    val userDao = UserDao()
    val eventsDao = EventsDao()
}
