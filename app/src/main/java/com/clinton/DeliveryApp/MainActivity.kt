package com.clinton.DeliveryApp

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    var textViewAddress: TextView? = null
    var textViewUser: TextView? = null
    var apiUrl =
        "http://192.168.1.110/food-delivery-application/fooddeliveryapp/public/api/users/delivery?email="
    var sharedPreferences: SharedPreferences? = null
    var order_id: String? = null
    var longitude: String? = null
    var latitude: String? = null
    var cardViewDeliveryDetails: CardView? = null
    var textViewTitle: TextView? = null
    @SuppressLint("SetTextI18n")
    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewAddress = findViewById(R.id.new_delivery)
        textViewUser = findViewById(R.id.userDetails)
        sharedPreferences = getSharedPreferences("DeliveryBoyApp", MODE_PRIVATE)
        cardViewDeliveryDetails = findViewById(R.id.deliveryDetails)
        textViewTitle = findViewById(R.id.textDeliveryTitle)
        textViewTitle.setText("No pending delivery")
        cardViewDeliveryDetails.setVisibility(View.GONE)
        if (sharedPreferences.getString("login", "false").equals("false")) {
            val intent = Intent(getApplicationContext(), Login::class.java)
            startActivity(intent)
            finish()
        }
        checkPermissions()
        val gpsUtils = GPSUtils(this)
        gpsUtils.statusCheck()
        textViewUser.setText(
            sharedPreferences.getString("name", "") + "\n" +
                    sharedPreferences.getString("email", "")
        )
        fetchData()
        val buttonLocation: Button = findViewById(R.id.startDelivery)
        val buttonSuccess: Button = findViewById(R.id.btn_success)
        val buttonFailed: Button = findViewById(R.id.btn_failed)
        buttonSuccess.setOnClickListener(object : OnClickListener() {
            @Override
            fun onClick(view: View?) {
                markOrderStatus(
                    "http://192.168.1.110/food-delivery-application/fooddeliveryapp/public/api/users/delivery/success?order_id=$order_id",
                    "success"
                )
            }
        })
        buttonFailed.setOnClickListener(object : OnClickListener() {
            @Override
            fun onClick(view: View?) {
                markOrderStatus(
                    "http://192.168.1.110/food-delivery-application/fooddeliveryapp/public/api/users/delivery/failed?order_id=$order_id",
                    "failed"
                )
            }
        })
        buttonLocation.setOnClickListener(object : OnClickListener() {
            @Override
            fun onClick(view: View?) {
                val intent = Intent(getApplicationContext(), ViewLocation::class.java)
                intent.putExtra("lat", latitude)
                intent.putExtra("long", longitude)
                startActivity(intent)
            }
        })
    }

    fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }

    fun markOrderStatus(url: String?, status: String) {
        val queue: RequestQueue = Volley.newRequestQueue(getApplicationContext())
        val stringRequest = StringRequest(Request.Method.GET, url,
            object : Listener<String?>() {
                @Override
                fun onResponse(response: String) {
                    if (response.equals("success")) {
                        Toast.makeText(
                            this@MainActivity, "Marked: Delivery "
                                    + status, Toast.LENGTH_SHORT
                        ).show()
                        cardViewDeliveryDetails.setVisibility(View.GONE)
                        textViewTitle.setText("No pending delivery")
                    } else Toast.makeText(
                        this@MainActivity, "Operation Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, object : ErrorListener() {
                @Override
                fun onErrorResponse(error: VolleyError) {
                    error.printStackTrace()
                }
            })
        queue.add(stringRequest)
    }

    fun parseJSON(data: String?) {
        try {
            val jsonArray = JSONArray(data)
            if (jsonArray.length() > 0) {
                cardViewDeliveryDetails.setVisibility(View.VISIBLE)
                textViewTitle.setText("New Delivery Details Found")
            }
            for (i in 0 until jsonArray.length()) {
                val stu: JSONObject = jsonArray.getJSONObject(i)
                val address: String = stu.getString("destination_address")
                order_id = stu.getString("id")
                latitude = stu.getString("destination_lat")
                longitude = stu.getString("destination_lon")
                textViewAddress.setText("Address: $address")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun fetchData() {
        val queue: RequestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET,
            apiUrl + sharedPreferences.getString("email", ""),
            object : Listener<String?>() {
                @Override
                fun onResponse(response: String?) {
                    parseJSON(response)
                }
            }, object : ErrorListener() {
                @Override
                fun onErrorResponse(error: VolleyError) {
                    error.printStackTrace()
                }
            })
        queue.add(stringRequest)
    }
}