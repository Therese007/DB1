package dtu.engtech.db1.ui.theme

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.estimote.proximity_sdk.api.ProximityZoneContext

private const val TAG = "PROXIMITY"

class ZoneEventViewModel: ViewModel() {
    var zoneContexts by mutableStateOf(setOf<ProximityZoneContext>())

    fun updateZoneContexts(zoneContexts: Set<ProximityZoneContext>) {
        this.zoneContexts = zoneContexts
        Log.d(dtu.engtech.db1.ui.theme.TAG, "ZONE CONTEXTS: ${zoneContexts.toString()}")
    }

    val TAG: String get() {
        if (zoneContexts.count() > 0)
            return zoneContexts.first().tag
        else
            return "No zone"
    }

    val zoneInfo: List<BeaconInfo> get() {
        return zoneContexts.map { it -> BeaconInfo(it.deviceId, it.tag, it.attachments) }.toList()
    }

}

