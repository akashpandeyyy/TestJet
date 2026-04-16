package com.example.modernui.ui.components.intentpackage

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.modernui.Api.model.PidOptData

object RdHelper {
    data class DeviceInfo(val id: String, val name: String, val icon: ImageVector)

    val SUPPORTED_DEVICES = listOf(
        DeviceInfo("face", "Face Recognition", Icons.Default.Face),
        DeviceInfo("mfs110", "Mantra MFS110", Icons.Default.Fingerprint),
        DeviceInfo("morpho_l1", "Morpho L1", Icons.Default.Fingerprint),
        DeviceInfo("mantra_mis100v2", "Mantra Iris MIS100", Icons.Default.RemoveRedEye)
    )

    const val FACE_RD_PACKAGE = "in.gov.uidai.facerd"
    const val MANTRA_RD_PACKAGE = "com.mantra.mfs110.rdservice"
    const val MORPHO_RD_PACKAGE = "com.idemia.l1rdservice"
    const val MANTRA_IRIS_RD_PACKAGE = "com.mantra.mis100v2.rdservice"

    const val FACE_RD_CAPTURE_ACTION = "in.gov.uidai.rdservice.face.CAPTURE"
    const val FP_RD_CAPTURE_ACTION = "in.gov.uidai.rdservice.fp.CAPTURE"
    const val IRIS_RD_CAPTURE_ACTION = "in.gov.uidai.rdservice.iris.CAPTURE"
    
    // Intent Extra Keys
    const val KEY_FACE_REQUEST = "request"
    const val KEY_FACE_RESPONSE = "response"
    const val KEY_PID_OPTIONS = "PID_OPTIONS"
    const val KEY_PID_DATA = "PID_DATA"

//    fun getInputKey(deviceId: String): String {
//        return if (deviceId == "face") KEY_FACE_REQUEST else KEY_PID_OPTIONS
//    }
//
//    fun getOutputKey(deviceId: String): String {
//        return if (deviceId == "face") KEY_FACE_RESPONSE else KEY_PID_DATA
//    }

fun getInputKey(deviceId: String): String {
    return when (deviceId) {
        "face" -> KEY_FACE_REQUEST
        else -> KEY_PID_OPTIONS
    }
}

    fun getOutputKey(deviceId: String): String {
        return when (deviceId) {
            "face" -> KEY_FACE_RESPONSE
            else -> KEY_PID_DATA
        }
    }


    val faceType = PidOptData(fType = "", fCount = "", iCount = "")

    // Fingerprint ke liye iCount ko "0" set karo strictly
    val fingType = PidOptData(
        fType = "2",
        fCount = "1",
        iCount = "0" // "0" means no Iris
    )

    // Iris ke liye fCount ko "0" set karo
    val irisType = PidOptData(
        fType = "0",
        fCount = "0",
        iCount = "1" // "1" means one eye
    )

//    fun makePidXm(data: PidOptData): String {
//        val xml = """
//            <PidOptions env="P" ver="1.0">
//                <CustOpts>
//                    <Param name="purpose" value="auth"/>
//                    <Param name="requestAdditionalInfo" value="true"/>
//                </CustOpts>
//                <Opts fCount="${data.fCount}" fType="${data.fType}" format="0" iCount="${data.iCount}" iType="" pCount="1" pType="1" pidVer="2.0" posh="UNKNOWN" timeout="20000" wadh=""/>
//            </PidOptions>
//        """.trimIndent()
//        Log.d("RdHelper", "Generated XML:\n$xml")
//        return xml
//    }

//    fun makePidXm(data: PidOptData): String {
//        val xml = """
//        <PidOptions env="P" ver="1.0">
//            <CustOpts>
//                <Param name="purpose" value="auth"/>
//                <Param name="requestAdditionalInfo" value="true"/>
//            </CustOpts>
//            <Opts fCount="${data.fCount}" fType="${data.fType}" format="0" iCount="${data.iCount}" iType="0" pCount="1" pType="1" pidVer="2.0" posh="UNKNOWN" timeout="20000" wadh=""/>
//        </PidOptions>
//    """.trimIndent()
//        Log.d("RdHelper", "Generated XML:\n$xml")
//        return xml
//    }


//    fun makePidXm(data: PidOptData): String {
//        return "<PidOptions env=\"P\" ver=\"1.0\"><CustOpts><Param name=\"purpose\" value=\"auth\"/><Param name=\"requestAdditionalInfo\" value=\"true\"/></CustOpts><Opts fCount=\"${data.fCount}\" fType=\"${data.fType}\" format=\"0\" iCount=\"${data.iCount}\" iType=\"0\" pCount=\"1\" pType=\"1\" pidVer=\"2.0\" posh=\"UNKNOWN\" timeout=\"20000\" wadh=\"\"/></PidOptions>"
//    }

    // 1. Face
    private fun getFaceXml(): String {
        return """<PidOptions env="P" ver="1.0"><CustOpts><Param name="purpose" value="auth"/><Param name="requestAdditionalInfo" value="true"/></CustOpts><Opts fCount="" fType="" format="0" iCount="" iType="" pCount="1" pType="1" pidVer="2.0" posh="UNKNOWN" timeout="20000" wadh=""/></PidOptions>""".trimIndent()
    }

    // 2. Fingerprint ke liye XML (As per Mantra/Morpho Spec jo aapne diya)
    private fun getFingerXml(data: PidOptData, env: String = "P"): String {
        return """<PidOptions ver="1.0"> <Opts fCount="${data.fCount}" fType="${data.fType}" iCount="${data.iCount}" pCount="0" format="0" pidVer="2.0" timeout="20000" posh="UNKNOWN" env="$env" wadh=""/> <CustOpts><Param name="mantrakey" value="" /></CustOpts> </PidOptions>""".trimIndent()
    }

    // 3. Main function to call above functions
    fun makePidXm(deviceId: String, data: PidOptData): String {
        return if (deviceId == "face") {
            getFaceXml()
        } else {
            getFingerXml(data)
        }
    }






//    fun getAction(selectedDevice: String): String {
//        return when (selectedDevice) {
//            "mfs110", "morpho_l1" -> FP_RD_CAPTURE_ACTION
//            "mantra_mis100v2" -> "com.mantra.mfs110.rdservice"
//            "face" -> FACE_RD_CAPTURE_ACTION
//            else -> FACE_RD_CAPTURE_ACTION
//        }
//    }
//
//    fun getPackage(selectedDevice: String): String {
//        return when (selectedDevice) {
//            "mfs110" -> MANTRA_RD_PACKAGE
//            "mantra_mis100v2" -> "com.mantra.mfs110.rdservice"
//            "morpho_l1" -> MORPHO_RD_PACKAGE
//            "face" -> FACE_RD_PACKAGE
//            else -> FACE_RD_PACKAGE
//        }
//    }



    fun getAction(selectedDevice: String): String {
        return when (selectedDevice) {
            "mfs110", "morpho_l1" -> FP_RD_CAPTURE_ACTION
            "mantra_mis100v2" -> IRIS_RD_CAPTURE_ACTION
            "face" -> FACE_RD_CAPTURE_ACTION
            else -> FACE_RD_CAPTURE_ACTION
        }
    }

    fun getPackage(selectedDevice: String): String {
        return when (selectedDevice) {
            "mfs110" -> MANTRA_RD_PACKAGE
            "mantra_mis100v2" -> MANTRA_IRIS_RD_PACKAGE // Fix: Correct Iris package
            "morpho_l1" -> MORPHO_RD_PACKAGE
            "face" -> FACE_RD_PACKAGE
            else -> FACE_RD_PACKAGE
        }
    }
    data class PidOptData(
        val fType: String = "0",
        val fCount: String = "0",
        val iCount: String = "0", // Default 0
        val pCount: String = "1",
        val pType: String = "1"
    )
}
