package com.opensplit.features.auth

import com.opensplit.database.Users
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class AuthRepositoryImpl(private val database: Database) : AuthRepository {
  override fun findUserByEmail(email: String): AuthUser? =
      transaction(database) {
        Users.selectAll().where { Users.email eq email }.limit(1).firstOrNull()?.toAuthUser()
      }

  override fun findUserById(userId: String): AuthUser? =
      transaction(database) {
        Users.selectAll().where { Users.id eq userId }.limit(1).firstOrNull()?.toAuthUser()
      }

  override fun createUser(user: AuthUser) {
    transaction(database) {
      Users.insert {
        it[Users.id] = user.id
        it[Users.name] = user.name
        it[Users.email] = user.email
        it[Users.passwordHash] = user.passwordHash
      }
    }
  }

  private fun ResultRow.toAuthUser(): AuthUser =
      AuthUser(
          id = get(Users.id),
          name = get(Users.name),
          email = get(Users.email),
          passwordHash = get(Users.passwordHash),
      )
}
