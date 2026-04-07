package com.example.modernui.Api


/**
 * Centralized API configuration.
 * Host and endpoints in one place - build full URLs by attaching path to base.
 */
object ApiEndpoints {

    object Admin {
        const val FETCH_INCODE_BY_SERVICE = "admin/fetchIncodeByService"
    }

    object Login {
        const val USER_LOGIN = "login/userLogin"
        const val  USER_ValidateSession = "login/validateSession"
        const val VALIDATE_OTP = "login/validateOtp"


    }

    object Recharge {
        const val MOBILE_PLANS = "recharge/mobilePlans"
        const val TRANSACTION = "recharge/transaction"
    }

    object User {
        const val FETCH_USER_BALANCE = "user/fetchUserBalance"
        const val BANKS = "user/banks"
    }

    object Aeps {
        const val AEPS_STATUS_APP = "aeps/aepsStatus/APP"
        const val TRANSACTION = "aeps/transaction"


    }
    object MTB {
        const val MTB = "common/fetchPayoutUserBank/NA"


    }
}
