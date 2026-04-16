package com.example.modernui.ui.components.intentpackage

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.ui.graphics.vector.ImageVector

// ─────────────────────────────────────────────
// PID OPT DATA — single source of truth
// (was duplicated inside RdHelper before)
// ─────────────────────────────────────────────

data class PidOptData(
    val fType:  String = "0",
    val fCount: String = "0",
    val iCount: String = "0",
    val pCount: String = "1",
    val pType:  String = "1"
)

// ─────────────────────────────────────────────
// RD SERVICE HELPER
// ─────────────────────────────────────────────

object RdHelper {

    private const val TAG = "RdHelper"

    // ── Device info ───────────────────────────

    data class DeviceInfo(
        val id:   String,
        val name: String,
        val icon: ImageVector
    )

    val SUPPORTED_DEVICES = listOf(
        DeviceInfo("face",            "Face Recognition",    Icons.Default.Face),
        DeviceInfo("mfs110",          "Mantra MFS110",       Icons.Default.Fingerprint),
        DeviceInfo("morpho_l1",       "Morpho L1",           Icons.Default.Fingerprint),
        DeviceInfo("mantra_mis100v2", "Mantra Iris MIS100",  Icons.Default.RemoveRedEye)
    )

    // ── Package names ─────────────────────────

    const val FACE_RD_PACKAGE         = "in.gov.uidai.facerd"
    const val MANTRA_RD_PACKAGE       = "com.mantra.mfs110.rdservice"
    const val MORPHO_RD_PACKAGE       = "com.idemia.l1rdservice"
    const val MANTRA_IRIS_RD_PACKAGE  = "com.mantra.mis100v2.rdservice"

    // ── Intent actions ────────────────────────

    const val FACE_RD_CAPTURE_ACTION  = "in.gov.uidai.rdservice.face.CAPTURE"
    const val FP_RD_CAPTURE_ACTION    = "in.gov.uidai.rdservice.fp.CAPTURE"
    const val IRIS_RD_CAPTURE_ACTION  = "in.gov.uidai.rdservice.iris.CAPTURE"

    // ── Intent extra keys ─────────────────────

    const val KEY_FACE_REQUEST  = "request"
    const val KEY_FACE_RESPONSE = "response"
    const val KEY_PID_OPTIONS   = "PID_OPTIONS"
    const val KEY_PID_DATA      = "PID_DATA"

    // ── Preset PidOptData per modality ────────

    /** Face — no finger/iris counts */
    val faceType = PidOptData(fType = "", fCount = "", iCount = "")

    /** Fingerprint — single finger, no iris */
    val fingType = PidOptData(fType = "2", fCount = "1", iCount = "0")

    /** Iris — single eye, no finger */
    val irisType = PidOptData(fType = "0", fCount = "0", iCount = "1")

    // ── Route helpers ─────────────────────────

    fun getAction(deviceId: String): String = when (deviceId) {
        "mfs110", "morpho_l1" -> FP_RD_CAPTURE_ACTION
        "mantra_mis100v2"     -> IRIS_RD_CAPTURE_ACTION
        "face"                -> FACE_RD_CAPTURE_ACTION
        else                  -> FACE_RD_CAPTURE_ACTION
    }

    fun getPackage(deviceId: String): String = when (deviceId) {
        "mfs110"          -> MANTRA_RD_PACKAGE
        "morpho_l1"       -> MORPHO_RD_PACKAGE
        "mantra_mis100v2" -> MANTRA_IRIS_RD_PACKAGE
        "face"            -> FACE_RD_PACKAGE
        else              -> FACE_RD_PACKAGE
    }

    fun getInputKey(deviceId: String): String =
        if (deviceId == "face") KEY_FACE_REQUEST else KEY_PID_OPTIONS

    fun getOutputKey(deviceId: String): String =
        if (deviceId == "face") KEY_FACE_RESPONSE else KEY_PID_DATA

    fun getPidType(deviceId: String): PidOptData = when (deviceId) {
        "mfs110", "morpho_l1" -> fingType
        "mantra_mis100v2"     -> irisType
        else                  -> faceType
    }

    // ══════════════════════════════════════════
    // PID XML GENERATION — FIXED
    //
    // Three separate formats per UIDAI spec:
    //   1. Face RD  → CustOpts with pType/pCount
    //   2. Finger RD → Mantra/Morpho compatible format
    //   3. Iris RD  → iCount=1, fCount=0
    // ══════════════════════════════════════════

    /**
     * Face XML — per UIDAI Face RD Service spec.
     * Uses pType="1" pCount="1" to request photo capture.
     * fCount/iCount intentionally empty (not applicable).
     */
    private fun buildFaceXml(): String {
        val xml = """<PidOptions env="P" ver="1.0"><CustOpts><Param name="purpose" value="auth"/><Param name="requestAdditionalInfo" value="true"/></CustOpts><Opts fCount="" fType="" format="0" iCount="" iType="" pCount="1" pType="1" pidVer="2.0" posh="UNKNOWN" timeout="20000" wadh=""/></PidOptions>"""
        Log.d(TAG, "Face XML: $xml")
        return xml
    }

    /**
     * Fingerprint XML — compatible with Mantra MFS110 & Morpho L1.
     *
     * Key fixes vs the old version:
     *  - env moved inside <Opts> (not on root) — matches Mantra spec
     *  - iType="0" explicit (required by Morpho)
     *  - mantrakey param included (required by Mantra RD)
     *  - No extra whitespace inside tags (some parsers are strict)
     */
    private fun buildFingerXml(data: PidOptData): String {
        val xml = """<PidOptions ver="1.0"><Opts fCount="${data.fCount}" fType="${data.fType}" iCount="${data.iCount}" iType="0" pCount="0" format="0" pidVer="2.0" timeout="20000" posh="UNKNOWN" env="P" wadh=""/><CustOpts><Param name="mantrakey" value=""/></CustOpts></PidOptions>"""
        Log.d(TAG, "Finger XML: $xml")
        return xml
    }

    /**
     * Iris XML — Mantra MIS100V2 spec.
     * iCount="1" = one eye, fCount="0" = no finger.
     */
    private fun buildIrisXml(data: PidOptData): String {
        val xml = """<PidOptions env="P" ver="1.0"><CustOpts><Param name="purpose" value="auth"/></CustOpts><Opts fCount="0" fType="0" iCount="${data.iCount}" iType="0" pCount="0" format="0" pidVer="2.0" timeout="20000" posh="UNKNOWN" wadh=""/></PidOptions>"""
        Log.d(TAG, "Iris XML: $xml")
        return xml
    }

    /**
     * Main dispatcher — picks the right XML builder per device.
     */
    fun makePidXm(deviceId: String, data: PidOptData): String {
        return when (deviceId) {
            "face"            -> buildFaceXml()
            "mantra_mis100v2" -> buildIrisXml(data)
            else              -> buildFingerXml(data) // mfs110, morpho_l1
        }
    }

    /**
     * Convenience: build PID XML directly from deviceId (auto-picks PidOptData).
     */
    fun buildPidXml(deviceId: String): String =
        makePidXm(deviceId, getPidType(deviceId))
}
