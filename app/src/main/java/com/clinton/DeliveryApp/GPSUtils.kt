package com.clinton.DeliveryApp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager

class GPSUtils(activity: Activity) {
    var activity: Activity

    init {
        this.activity = activity
    }

    fun statusCheck() {
        val manager: LocationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = Builder(activity)
        builder.setMessage("Enable the GPS to have seamless experience")
            .setCancelable(false)
            .setPositiveButton("Enable GPS", object : OnClickListener() {
                fun onClick(dialog: DialogInterface?, id: Int) {
                    activity.startActivity(
                        Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
            })
            .setNegativeButton("No Thanks", object : OnClickListener() {
                fun onClick(dialog: DialogInterface, id: Int) {
                    dialog.cancel()
                }
            })
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}