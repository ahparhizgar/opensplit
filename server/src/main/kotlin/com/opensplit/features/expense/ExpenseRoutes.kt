package com.opensplit.features.expense

import com.opensplit.dto.auth.ErrorResponse
import com.opensplit.dto.expense.CreateExpenseRequest
import com.opensplit.plugins.authenticateUser
import com.opensplit.plugins.user
import com.opensplit.validation.expense.ExpenseValidation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureExpenseRoutes() {
  val expenseService by inject<ExpenseService>()

  routing {
    authenticateUser {
      route("/groups/{groupId}/expenses") {
        post {
          val groupId = call.parameters["groupId"]
          if (groupId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Group id is required"),
            )
            return@post
          }

          val request = call.receive<CreateExpenseRequest>()
          val validation = ExpenseValidation.validateExpense(request.title, request.amount)
          if (!validation.isValid) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Invalid expense data", errors = validation.errors),
            )
            return@post
          }

          val user = call.user()
          val expense =
              try {
                expenseService.createExpense(
                    groupId = groupId,
                    request = request,
                    creator = user.userId,
                )
              } catch (_: NotAMemberException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(generalError = "You are not a member of this group"),
                )
                return@post
              }
          call.respond(HttpStatusCode.Created, expense)
        }

        get {
          val groupId = call.parameters["groupId"]
          if (groupId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Group id is required"),
            )
            return@get
          }
          val expenses = expenseService.getExpenses(groupId)
          call.respond(expenses)
        }

        delete("/{expenseId}") {
          val groupId = call.parameters["groupId"] ?: return@delete
          val expenseId = call.parameters["expenseId"] ?: return@delete
          val user = call.user()
          try {
            expenseService.deleteExpense(user, groupId, expenseId)
            call.respond(HttpStatusCode.NoContent)
          } catch (_: NotAMemberException) {
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(generalError = "You are not a member of this group"),
            )
          }
        }

        put("/{expenseId}") {
          val groupId = call.parameters["groupId"]
          if (groupId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Group id is required"),
            )
            return@put
          }

          val expenseId = call.parameters["expenseId"]
          if (expenseId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Expense id is required"),
            )
            return@put
          }

          val request = call.receive<CreateExpenseRequest>()
          val validation = ExpenseValidation.validateExpense(request.title, request.amount)
          if (!validation.isValid) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Invalid expense data", errors = validation.errors),
            )
            return@put
          }

          val user = call.user()
          try {
            val expense = expenseService.updateExpense(user, groupId, expenseId, request)
            call.respond(HttpStatusCode.OK, expense)
          } catch (_: NotAMemberException) {
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(generalError = "You are not a member of this group"),
            )
          } catch (_: ExpenseNotFoundException) {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(generalError = "Expense not found"),
            )
          }
        }
      }
    }
  }
}
