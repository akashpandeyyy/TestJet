package com.example.modernui.Api



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
        const val MOBILE_PLANS = "recharge/mobilePlans/{mobileNumber}"
        const val TRANSACTION = "recharge/transaction"
    }

    object User {
        const val FETCH_USER_BALANCE = "common/fetchTotalPanelBalance"

    }

    object Aeps {
        const val AEPS_STATUS_APP = "aeps/aepsStatus/APP"
        const val AEPS_TRANSACTION = "aeps/transaction"


    }
    object MTB {
        const val MTB_LISTED_BANK = "common/fetchPayoutUserBank/"
        const val MTB_ADD_PAYOUT_BANK = "common/addPayoutBank"
        const val MTB_PAYOUT = "aeps/payout"

    }
    object CMS {
        const val CMS = "aeps/generateCmsLink"
    }
    object Insurance
    {
        const val Insurance="insurance/lead"
    }
//    object 2FA
//    {
//        const val 2FAToken="sdk/validate2FAToken"
//        const val 2FAToken="sdk/validate2FAToken"
//    }
object BankList
{
    const val Bank_List="user/banks"
}

}
