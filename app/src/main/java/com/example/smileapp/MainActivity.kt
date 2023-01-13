package com.example.smileapp

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.smileapp.databinding.ActivityMainBinding
import com.smileid.smileidui.CaptureType
import com.smileid.smileidui.IntentHelper
import com.smileid.smileidui.SIDCaptureManager
import com.smileid.smileidui.SIDIDCaptureConfig
import com.smileid.smileidui.SIDSelfieCaptureConfig
import com.smileidentity.libsmileid.core.IdType
import com.smileidentity.libsmileid.core.SIDConfig
import com.smileidentity.libsmileid.core.SIDNetworkRequest
import com.smileidentity.libsmileid.core.SelfieCaptureConfig
import com.smileidentity.libsmileid.model.SIDMetadata
import com.smileidentity.libsmileid.model.SIDNetData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * This is the Main class
 * @author Michael
 * @
 */

class MainActivity : AppCompatActivity() {

    var uniqueTag = System.currentTimeMillis()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initSIDActivity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openBtn.setOnClickListener {
            CoroutineScope(Job() + Dispatchers.Main.immediate).launch {
                requestPermission()
            }
        }

    }

    private fun initSIDActivity() {
        uniqueTag = System.currentTimeMillis()
        Log.d("tag::", uniqueTag.toString())
        val metadata = SIDMetadata()

        val userIdInfo = metadata.getSidUserIdInfo();
        /*userIdInfo.setCountry(<Country Code>); //String Alpha-2 country code
        userIdInfo.setFirstName(<First Name>); // String
        userIdInfo.setLastName(<Last Name>); // String
        userIdInfo.setIdNumber(<ID Number>); // String
        userIdInfo.setIdType(<ID Type>); // String*/

        //Useful if there is anything which is not catered
        //for by the strongly typed methods above so you can add
        //for example dob as a key and the date as the value
        // userIdInfo.additionalValue(<Key>, <Value>);//String for both key and value

        val partnerParams = metadata.getPartnerParams();
        partnerParams.jobId = uniqueTag.toString();// unique identifier string per job
        partnerParams.setUserId(
            UUID.randomUUID().toString()
        );// unique identifier for the user information being processed
        //partnerParams.additionalValue(<key>,<value>)//string key and value
        // for anything etra which you may need associated with the job

        val data = SIDNetData(this, SIDNetData.Environment.TEST);

        val config = SIDSelfieCaptureConfig.Builder()
            /*.setCameraFace(SIDSelfieCaptureConfig.CameraFace.FRONT)
            .setCaptureTip("dkjjkdfjk")
            .setCaptureTitle("TITLE")
            .setFlashScreen(false)
            .setCaptureFullScreen(false)
            .setManualCapture(true)*/
            //.setIdType(IdType.Idcard) //Environment information
            //.setGeoInformation(<GeoInfos>)//optional GeoInfos with geo information
            //.setCaptureSide(SIDIDCaptureConfig.CaptureSide.FrontAndBack)//the same object with partner params and user id info
            .build()// string must be a unique string

        val sidCaptureManager = SIDCaptureManager.Builder(
            this,
            CaptureType.SELFIE,
            102
        )
        sidCaptureManager.setSidSelfieConfig(config)
        sidCaptureManager.setTag(uniqueTag.toString())

        sidCaptureManager.build().start()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(baseContext, "Success", Toast.LENGTH_SHORT).show()
                Toast.makeText(baseContext, "${data?.getStringExtra(IntentHelper.SMILE_REQUEST_RESULT_TAG)}", Toast.LENGTH_LONG).show()
                val metadata = SIDMetadata()

                val userIdInfo = metadata.sidUserIdInfo;
                userIdInfo.countryCode = "NG";
                userIdInfo.country ="Nigeria"
                /*userIdInfo.setFirstName(<First Name>); // String
                userIdInfo.setLastName(<Last Name>); // String*/
                userIdInfo.idNumber = "70274430222"; // String
                userIdInfo.idType = "NIN"; // String

                //Useful if there is anything which is not catered
                //for by the strongly typed methods above so you can add
                //for example dob as a key and the date as the value
                // userIdInfo.additionalValue(<Key>, <Value>);//String for both key and value

                val partnerParams = metadata.partnerParams;
                partnerParams.jobId = data?.getStringExtra(IntentHelper.SMILE_REQUEST_RESULT_TAG).toString();// unique identifier string per job
                partnerParams.userId = "1859";// unique identifier for the user information being processed
                //partnerParams.additionalValue(<key>,<value>)//string key and value
                // for anything extra which you may need associated with the job

                val envData = SIDNetData(this, SIDNetData.Environment.TEST);


                val config = SIDConfig.Builder(this)
                    .setSmileIdNetData(envData) //Environment information
                    .setSIDMetadata(metadata)//the same object with partner params and user id info
                    .setMode(SIDConfig.Mode.ENROLL)//
                    .setJobType(1)
                    .build(data?.getStringExtra(IntentHelper.SMILE_REQUEST_RESULT_TAG))
                val sIDNetworkRequest = SIDNetworkRequest(this)
                sIDNetworkRequest.setOnUpdateListener {
                    Toast.makeText(this, "Progress $it%", Toast.LENGTH_SHORT).show()
                }
                sIDNetworkRequest.setOnUpdateJobStatusListener {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
                sIDNetworkRequest.setOnAuthenticatedListener {
                    AlertDialog.Builder(this)
                        .setMessage(
                            it.statusResponse.rawJsonString
                        )
                        .setPositiveButton("ok") { dlg, p1 ->
                            dlg.dismiss()
                        }.show()

                    Toast.makeText(this, "ENROLLED!!!!", Toast.LENGTH_LONG).show()
                }
                sIDNetworkRequest.setOnDocVerificationListener {
                    AlertDialog.Builder(this)
                        .setMessage(
                            it.resultText
                        )
                        .setPositiveButton("ok") { dlg, p1 ->
                            dlg.dismiss()
                        }.show()

                    Toast.makeText(this, "ENROLLED!!!!", Toast.LENGTH_LONG).show()
                }
                sIDNetworkRequest.setOnIDValidationListener {
                    AlertDialog.Builder(this)
                        .setMessage(
                            it.resultText
                        )
                        .setPositiveButton("ok") { dlg, p1 ->
                            dlg.dismiss()
                        }.show()
                }
                sIDNetworkRequest.setOnCompleteListener {
                    AlertDialog.Builder(this)
                        .setMessage(
                            "COMPLETE!!!!"
                        )
                        .setPositiveButton("ok") { dlg, p1 ->
                            dlg.dismiss()
                        }.show()


                    Toast.makeText(this, "ENROLLED!!!!", Toast.LENGTH_LONG).show()
                }
                sIDNetworkRequest.setOnEnrolledListener {
                    AlertDialog.Builder(this)
                        .setMessage(
                            "ENROLLED!!!!"
                        )
                        .setPositiveButton("ok") { dlg, p1 ->
                            dlg.dismiss()
                        }.show()

                    Toast.makeText(this, "ENROLLED!!!!", Toast.LENGTH_LONG).show()
                }
                sIDNetworkRequest.set0nErrorListener { it ->
                    AlertDialog.Builder(this)
                        .setMessage(
                            it.stackTraceToString()
                        )
                        .setPositiveButton("ok") { dlg, p1 ->
                            dlg.dismiss()
                        }.show()

                }
                sIDNetworkRequest.submit(config)//the object from above
            } else {
                Toast.makeText(baseContext, "${data?.getStringExtra(IntentHelper.SMILE_REQUEST_RESULT_TAG)}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
            ) -> {
                initSIDActivity()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }
}