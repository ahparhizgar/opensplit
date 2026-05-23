package com.opensplit.validation.household

import com.opensplit.validation.auth.ValidationResult

object HouseholdValidation {

    fun validateCreateHousehold(name: String): ValidationResult {
        val errors = linkedMapOf<String, String>()
        when {
            name.isBlank() -> errors["name"] = "Household name is required"
            name.length > 255 -> errors["name"] = "Household name is too long"
        }
        return ValidationResult(errors)
    }

    fun validateJoinHousehold(inviteCode: String): ValidationResult {
        val errors = linkedMapOf<String, String>()
        if (inviteCode.isBlank()) errors["inviteCode"] = "Invite code is required"
        return ValidationResult(errors)
    }
}
