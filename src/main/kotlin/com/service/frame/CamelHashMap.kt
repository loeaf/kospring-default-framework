package com.service.frame

import org.springframework.jdbc.support.JdbcUtils

class CamelHashMap : HashMap<String, Any>() {
    override fun put(key: String, value: Any): Any? {
        return super.put(JdbcUtils.convertUnderscoreNameToPropertyName(key), value)
    }
}