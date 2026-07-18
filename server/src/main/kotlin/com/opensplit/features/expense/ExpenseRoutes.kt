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
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureExpenseRoutes() {
  val expenseService by inject<ExpenseService>()

  routing {
    authenticateUser {
      route("/households/{householdId}/expenses") {
        post {
          val householdId = call.parameters["householdId"]
          if (householdId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(generalError = "Household id is required"),
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
          val expense = expenseService.createExpense(householdId, user.userId, request)
          call.respond(HttpStatusCode.Created, expense)
        }
      }
    }
  }
}
