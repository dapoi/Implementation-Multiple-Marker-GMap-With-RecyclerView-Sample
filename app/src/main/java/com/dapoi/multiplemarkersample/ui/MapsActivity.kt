package com.dapoi.multiplemarkersample.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dapoi.multiplemarkersample.R
import com.dapoi.multiplemarkersample.adapter.MainAdapter
import com.dapoi.multiplemarkersample.data.HospitalResponseItem
import com.dapoi.multiplemarkersample.databinding.ActivityMapsBinding
import com.dapoi.multiplemarkersample.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var permissionArrays = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mainAdapter: MainAdapter
    private lateinit var mainViewModel: MainViewModel
    private lateinit var progressDialog: ProgressDialog
    private lateinit var snapHelper: LinearSnapHelper

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        val setPermission = Build.VERSION.SDK_INT
        if (setPermission > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                requestPermissions(permissionArrays, 101)
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu…")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("sedang menampilkan lokasi..")

        mainAdapter = MainAdapter()
        binding.rvListLocation.apply {
            adapter = mainAdapter
            layoutManager =
                LinearLayoutManager(this@MapsActivity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            snapHelper = object : LinearSnapHelper() {
                override fun findTargetSnapPosition(
                    layoutManager: RecyclerView.LayoutManager?,
                    velocityX: Int,
                    velocityY: Int
                ): Int {
                    val centerView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

                    val position = layoutManager!!.getPosition(centerView)
                    var targetPosition = -1
                    if (layoutManager.canScrollHorizontally()) {
                        targetPosition = if (velocityX < 0) {
                            position - 1
                        } else {
                            position + 1
                        }
                    }

                    if (layoutManager.canScrollVertically()) {
                        targetPosition = if (velocityY < 0) {
                            position - 1
                        } else {
                            position + 1
                        }
                    }

                    val firstItem = 0
                    val lastItem = layoutManager.itemCount - 1
                    targetPosition = Math.min(lastItem, targetPosition.coerceAtLeast(firstItem))
                    return targetPosition
                }
            }
            snapHelper.attachToRecyclerView(this)
        }

        binding.etSearch.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard(this@MapsActivity)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    mainAdapter.filter.filter(newText)
                    return false
                }
            })
        }
    }

    private fun hideKeyboard(context: Context) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationViewModel()
    }

    private fun getLocationViewModel() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.getDataHospital()
        progressDialog.show()
        mainViewModel.hospitalData.observe(this) {
            mainAdapter.setData(it)
            getMarker(it)
            progressDialog.dismiss()
        }
    }

    private fun getMarker(data: List<HospitalResponseItem?>?) {
        if (data != null) {
            for (i in data.indices) {

                // set lat long
                val latLngMarker =
                    data[i]?.lokasi?.lat?.let {
                        data[i]?.lokasi?.lon?.let { it1 -> LatLng(it, it1) }
                    }

                // get lat long marker
                latLngMarker?.let {
                    MarkerOptions().position(it).title(data[i]?.nama)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                }?.let {
                    mMap.addMarker(
                        it
                    )
                }

                // show marker
                val latLngResult =
                    data[0]?.lokasi?.lat?.let {
                        data[0]?.lokasi?.lon?.let { it1 ->
                            LatLng(
                                it,
                                it1
                            )
                        }
                    }

                // set position marker
                latLngResult?.let { CameraUpdateFactory.newLatLng(it) }?.let { mMap.moveCamera(it) }
                latLngResult?.let {
                    LatLng(
                        latLngResult.latitude,
                        it.longitude
                    )
                }?.let {
                    CameraUpdateFactory.newLatLngZoom(
                        it, 15f
                    )
                }?.let {
                    mMap.animateCamera(
                        it
                    )
                }
                mMap.uiSettings.isZoomControlsEnabled = true
                mMap.uiSettings.setAllGesturesEnabled(true)
            }

            mainAdapter.setOnItemClick(object : MainAdapter.OnItemClickListener {
                override fun onItemClick(item: HospitalResponseItem) {
                    item.lokasi?.lat?.let { item.lokasi.lon?.let { it1 -> LatLng(it, it1) } }?.let {
                        MarkerOptions().position(it)
                            .title(item.nama)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    }?.let {
                        mMap.addMarker(
                            it
                        ).let { marker ->
                            marker?.showInfoWindow()
                        }
                    }
                    item.lokasi?.lat?.let {
                        item.lokasi.lon?.let { it1 ->
                            LatLng(
                                it,
                                it1
                            )
                        }

                    }?.let {
                        CameraUpdateFactory.newLatLngZoom(
                            it, 15f
                        )
                    }?.let {
                        mMap.animateCamera(
                            it
                        )
                    }
                }
            })

            mMap.setOnMarkerClickListener { marker ->
                val markerPosition = marker.position
                mMap.addMarker(
                    MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )?.showInfoWindow()
                var markerSelected = -1
                for (i in data.indices) {
                    if (markerPosition.latitude == data[i]?.lokasi?.lat && markerPosition.longitude == data[i]?.lokasi?.lon) {
                        markerSelected = i
                    }
                }
                val cameraPosition = CameraPosition.Builder()
                    .target(markerPosition)
                    .zoom(15f)
                    .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                binding.rvListLocation.scrollToPosition(markerSelected)
                false
            }
        }
    }


    companion object {
        fun setWindowFlag(activity: AppCompatActivity, bits: Int, on: Boolean) {
            val win = activity.window
            val winParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }
}