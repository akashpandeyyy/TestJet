package com.example.modernui.Api



object ApiEndpoints {

    object Login {
        const val USER_LOGIN = "login/userLogin"
        const val  USER_ValidateSession = "login/validateSession"
        const val VALIDATE_OTP = "login/validateOtp"


    }

    object Recharge {
        const val MOBILE_PLANS = "recharge/mobilePlans/{mobileNumber}"
        const val TRANSACTION = "recharge/transaction"
        const val DTH_PLAN = "recharge/dthPlans"
        const val DTH_TRANSACTION = "recharge/dthPlans"

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
object BankList
{
    const val Bank_List="user/banks"
}

    object DMT
    {
       //validate customer
        const val JIO_VALIDATE_CUSTOMER="dmt/validateUser"

        // send eky data
        const val SEND_KYC_DATA="dmt/ekyc"

        // send otp
        const val SEND_OTP="dmt/sendOtp"

        // verify otp
        const val VERIFY_OTP="dmt/verifyOtp"



    }


}
