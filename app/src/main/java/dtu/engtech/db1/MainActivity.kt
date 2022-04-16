package dtu.engtech.estimoteproximity103

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.proximity_sdk.api.*
import dtu.engtech.db1.ui.theme.AttachmentKeys
import dtu.engtech.db1.ui.theme.BeaconInfo
import dtu.engtech.db1.ui.theme.DB1Theme
import dtu.engtech.db1.ui.theme.ZoneEventViewModel
import dtu.engtech.estimoteproximity103.CloudCredentials.APP_ID
import dtu.engtech.estimoteproximity103.CloudCredentials.APP_TOKEN

object CloudCredentials {
        const val APP_ID = "gruppe3-7ae"
        const val APP_TOKEN = "0b90af4659575dbf64ba00b89ea6159b"
    }

object DeviceList {
    val deviceNames = mapOf("e24c134e34c78d991e60e33b8e06b528" to "510",
        "9581c32e6b1bf8d6687a2dc8f026d020" to "512",
        "5d36743f41b52abced6ac7ebb825d71f" to "511")
}

private const val TAG = "PROXIMITY"
private const val SCANTAG = "SCANNING"

class MainActivity : ComponentActivity() {

    private lateinit var proximityObserver: ProximityObserver
    private var proximityObservationHandler: ProximityObserver.Handler? = null

    private val cloudCredentials = EstimoteCloudCredentials(
        APP_ID,
        APP_TOKEN
    )

    private val zoneEventViewModel by viewModels<ZoneEventViewModel>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DB1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    BeaconListView(zoneEventViewModel.zoneInfo)
                }
            }
        }

        // Requirements check
        RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
            this,
            onRequirementsFulfilled = { startProximityObservation() },
            onRequirementsMissing = displayToastAboutMissingRequirements,
            onError = displayToastAboutError
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        proximityObservationHandler?.stop()
    }


    private fun startProximityObservation() {
        proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
            .onError(displayToastAboutError)
            .withTelemetryReportingDisabled()
            .withAnalyticsReportingDisabled()
            .withEstimoteSecureMonitoringDisabled()
            .withBalancedPowerMode()
            .build()

        val proximityZones = ArrayList<ProximityZone>()
        proximityZones.add(zoneBuild("Medicin1"))
        proximityZones.add(zoneBuild("Medicin2"))
        proximityZones.add(zoneBuild("Medicin3"))

        proximityObservationHandler = proximityObserver.startObserving(proximityZones)
    }

    private fun zoneBuild(tag: String): ProximityZone {
        return ProximityZoneBuilder()
            .forTag(tag)
            .inNearRange()
            .onEnter {
                Log.d(TAG, "Enter: ${it.tag}")
            }
            .onExit {
                Log.d(TAG, "Exit: ${it.tag}")
            }
            .onContextChange {
                Log.d(TAG, "Change: $it")
                zoneEventViewModel.updateZoneContexts(it)
            }
            .build()
    }

    // Lambda functions for displaying errors when checking requirements
    private val displayToastAboutMissingRequirements: (List<Requirement>) -> Unit = {
        Toast.makeText(
            this,
            "Unable to start proximity observation. Requirements not fulfilled: ${it.size}",
            Toast.LENGTH_SHORT
        ).show()
    }
    private val displayToastAboutError: (Throwable) -> Unit = {
        Toast.makeText(
            this,
            "Error while trying to start proximity observation: ${it.message}",
            Toast.LENGTH_SHORT
        ).show()
        Log.d("ERROR", it.message?: "null message")
    }

}

@Composable
fun BeaconListView(zoneInfo: List<BeaconInfo>) {
    LazyColumn {
        items(zoneInfo) { beaconInfo ->
            BeaconCard(beaconInfo)
        }
    }
}

@Composable
fun BeaconCard(beaconInfo: BeaconInfo) {
    Column {
        Text(text = DeviceList.deviceNames[beaconInfo.deviceID.uppercase()] ?: "No device name")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = beaconInfo.tag)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = beaconInfo.attachments[AttachmentKeys.DESCRIPTION.key] ?: "No description")
        Spacer(modifier = Modifier.height(16.dp))
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DB1Theme {

    }
}