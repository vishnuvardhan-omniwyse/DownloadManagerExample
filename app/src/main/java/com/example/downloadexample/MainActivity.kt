package com.example.downloadexample

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_STORAGE_CODE = 1000
    }

    private lateinit var downloadID: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        editText.setText(getString(R.string.sample_url))

        downloadButton.setOnClickListener {
            //Checking OS Version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Check Permission
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted , requesting it 
                    val permissions: Array<String> =
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //Show Pop Up runtime
                    requestPermissions(permissions, PERMISSION_STORAGE_CODE)
                } else {
                    startDownloading()
                }
            } else {
                startDownloading()
            }
        }
    }

    private fun startDownloading() {
        //getting url from user
        val url = editText.text.toString()
        //getting filename
        val filename: String = url.substring(url.lastIndexOf('/') + 1)
        //download request

        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
            .setTitle(filename)
            .setDescription("Downloading")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

        val manager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadID = manager.enqueue(request)
        download_status.text = getString(R.string.download_started)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownloading()
                } else {
                    //permission Denied
                    Toast.makeText(this, getString(R.string.error_permission), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val completedId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (completedId == downloadID) {
                Toast.makeText(context, getString(R.string.download_completed), Toast.LENGTH_LONG)
                    .show()
                download_status.text = getString(R.string.download_completed)
            }
        }
    }
}