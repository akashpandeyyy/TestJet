package com.example.modernui.ui.screens.dmt

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernui.Api.UserRepo
import com.example.modernui.core.datastore.SessionManager
import com.example.modernui.core.location.UserLocationProvider
import com.example.modernui.ui.screens.aeps.RdCaptureState
import com.example.modernui.ui.screens.aeps.VerificationStep
import com.example.modernui.ui.screens.cashdeposite.FingerprintDevice
import com.example.modernui.ui.screens.cashdeposite.fingerprintDevicesMock
import com.example.modernui.ui.screens.dmt.jiomodel.BeneDetail
import com.example.modernui.ui.screens.dmt.jiomodel.ValidateUserRequest
import com.example.modernui.ui.components.intentpackage.RdHelper
import com.example.modernui.ui.screens.dmt.jiomodel.CreateTxnRequest
import com.example.modernui.ui.screens.dmt.jiomodel.KycDataRequest
import com.example.modernui.ui.screens.dmt.jiomodel.ReValidateTnxRequest
import com.example.modernui.ui.screens.dmt.jiomodel.ValidateTnxRequest
import kotlin.collections.emptyList
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DmtViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager,
    private val locationProvider: UserLocationProvider,

    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _balance = MutableStateFlow("₹0.00")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _navigationEvent = MutableSharedFlow<DmtScreen>()

    private val _beneficiaries = MutableStateFlow<List<BeneDetail>>(emptyList())
    val beneficiaries: StateFlow<List<BeneDetail>> = _beneficiaries.asStateFlow()

    private val _rdCaptureState = MutableStateFlow<RdCaptureState>(RdCaptureState.Idle)
    val rdCaptureState: StateFlow<RdCaptureState> = _rdCaptureState.asStateFlow()

    private val _scanState = MutableStateFlow(VerificationStep.IDLE)
    val scanState: StateFlow<VerificationStep> = _scanState.asStateFlow()

    val biometricData = MutableStateFlow("")
    val aadhaarNumber = MutableStateFlow("")
    val senderNumber = MutableStateFlow("")
    val isotp = MutableStateFlow("")
    val trfAmt = MutableStateFlow("")
    val trfamtreqid = MutableStateFlow("")
    val selectedDevice = MutableStateFlow<FingerprintDevice>(fingerprintDevicesMock.first())


    init {
        viewModelScope.launch {
            sessionManager.userSessionFlow.collectLatest { session ->
                if (session.token != null) {
                    refreshBalance()
                }
            }
        }
    }

    fun refreshBalance() {
        viewModelScope.launch {
            try {
                val response = userRepo.fetchUserBalance()
                if (response.status == 1) {
                    _balance.value = "₹${response.data?.total ?: 0.0}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to fetch balance"
            }
        }
    }

    fun ontrfamtChange(value: String) {
        if (value.all { it.isDigit() }) {
            trfAmt.value = value
        }
    }

    fun jiocheckMobile(mobile: String, onUserFound: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            senderNumber.value=mobile
            try {
                val location = locationProvider.getCurrentLocation()
                val request = ValidateUserRequest(
                    mobile = mobile,
                    latitude = location?.first,
                    longitude = location?.second,
                )

                val response = userRepo.jiodmtvalidatecustmoer(request)

                if (response.status == 22 && response.data != null) {

                    _beneficiaries.value = response.data.beneDetails
                    onUserFound(true)
                    val navigationEvent = _navigationEvent
                } else {
                    onUserFound(false) // New user hai
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to check mobile"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun airtelcheckMobile(mobile: String, onUserFound: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
        Toast.makeText(context,"babu rukk jao na ", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendOtp(mobile: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {

                delay(1000)
                onResult(true)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send OTP"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleRdServiceResult(responseData: String) {
        val errCodeMatch = Regex("errCode\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
        val errCode = errCodeMatch?.groupValues?.get(1)

        if (errCode == "0") {
            biometricData.value = responseData
            Log.d("PID_XML", biometricData.value)
            ekyc()
            _scanState.value = VerificationStep.SUCCESS

        } else {
            val errInfoMatch = Regex("errInfo\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(responseData)
            val errInfo = errInfoMatch?.groupValues?.get(1) ?: "Verification Failed"
            _errorMessage.value = "$errInfo (Code: $errCode)"
            _scanState.value = VerificationStep.ERROR
        }
    }

    fun ekyc(){
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("Adhar data",aadhaarNumber.value)
            Log.d("sender number",senderNumber.value)
            Log.d("biometric",biometricData.value)
            try {
                val location = locationProvider.getCurrentLocation()
                val request = KycDataRequest(
                    aadhaar =aadhaarNumber.value,
                    biometric = biometricData.value,
                    mobile = senderNumber.value,
                    latitude = location?.first,
                    longitude = location?.second,
                )

                val response = userRepo.jiodmtcustmoerkyc(request)

                if (response.status == 1 ) {
                    isotp.value= 1.toString()
                    Log.d("jiodmtcustmoerkyc",response.toString())

                } else {

                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to check mobile"
            } finally {
                _isLoading.value = false
            }
        }

    }

    fun initiateBiometricScan() {
        _scanState.value = VerificationStep.SCANNING
        viewModelScope.launch {
            try {
                val currentDevice = selectedDevice.value.id
                val deviceId = when (currentDevice) {
                    "face_scan" -> "face"
                    "mantra_mfs110" -> "mfs110"
                    "mantra_iris" -> "mantra_mis100v2"
                    "morpho_l1" -> "morpho_l1"
                    else -> currentDevice
                }

                val pidOptData = when (deviceId) {
                    "face" -> RdHelper.faceType
                    "mfs110", "morpho_l1" -> RdHelper.fingType
                    "mantra_mis100v2" -> RdHelper.irisType
                    else -> RdHelper.fingType
                }

                val pidOptions = RdHelper.makePidXm(deviceId, pidOptData)
                val action = RdHelper.getAction(deviceId)
                val packageName = RdHelper.getPackage(deviceId)
                val inputKey = RdHelper.getInputKey(deviceId)

                _rdCaptureState.value = RdCaptureState.Capture(
                    action = action,
                    packageName = packageName,
                    inputKey = inputKey,
                    pidOptions = pidOptions
                )
            } catch (e: Exception) {
                _errorMessage.value = "Capture initialization failed: ${e.message}"
            }
        }
    }

    fun resetRdCaptureState() {
        _rdCaptureState.value = RdCaptureState.Idle
        _scanState.value = VerificationStep.IDLE
    }

    fun onDeviceSelected(device: FingerprintDevice) {
        selectedDevice.value = device
    }

    fun verifyOtp(otp: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(1500)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

suspend fun resendOtp() {
    try {
        val request = ReValidateTnxRequest(
            requestId = trfamtreqid.value
        )
        val response = userRepo.resendtnx(request)
        if (response.status == 1) {
            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
    }
}

    fun verifytnxotp(otpp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {


                val request= ValidateTnxRequest(
                    requestId = trfamtreqid.value,
                   otp = otpp
                )
                 val response= userRepo.validatetnx(request)
                if (response.status==1 ){
                onResult(true, "DMT transaction of successful")
                } else{

                    onResult(false, response.message ?: "Transfer failed")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Transfer failed")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun tnxvaliotp(amt: String,beniid : String, ifsc: String, acount : String, name: String, onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val location = locationProvider.getCurrentLocation()
                val request= CreateTxnRequest(
                    amount = amt,
                    beneId = beniid,
                    mobile =senderNumber.value ,
                    account = acount,
                    ifsc = ifsc,
                    name = name,
                    latitude = location?.first,
                    longitude = location?.second

                )
                val response= userRepo.createtnx(request)
                if (response.status==1 && response.data!=null){
                    trfamtreqid.value=response.data.requestId
                    onResult(true)

                }else if(response.status==0){
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show();

                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show();
                onResult(false)

            } finally {
                _isLoading.value = false
            }
        }

    }


}
