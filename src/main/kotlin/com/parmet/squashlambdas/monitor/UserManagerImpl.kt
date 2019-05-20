package com.parmet.squashlambdas.monitor

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.QueryRequest

internal class UserManagerImpl(
    private val dynamoDb: AmazonDynamoDB,
    private val tableName: String
) : UserManager {
    override fun allUsers(): List<User> =
        dynamoDb.query(QueryRequest().withTableName(tableName))
            .items
            .map {
                User(
                    name = it.getValue("name").s,
                    email = it.getValue("email").s,
                    phoneNumber = it.getValue("phoneNumber").s
                )
            }
}
