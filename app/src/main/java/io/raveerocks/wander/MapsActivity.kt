package io.raveerocks.wander

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.raveerocks.wander.databinding.ActivityMapsBinding
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    companion object {
        private const val REQUEST_LOCATION = 1
        private val TAG = MapsActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var mapStyle: MapStyleOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)
        mapStyle = MapStyleOptions.loadRawResourceStyle(
            this,
            R.raw.map_style
        )
        setContentView(binding.root)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle()
        markHomeOnMap()
        map.setOnMapLongClickListener(this::onMapLongClick)
        map.setOnPoiClickListener(this::onMapPoiClick)
        enableMyLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setMapStyle() {
        try {
            if (!map.setMapStyle(mapStyle)) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun markHomeOnMap() {
        val home = LatLng(19.257040543416238, 72.86639511613816)
        map.apply {
            addMarker(MarkerOptions().position(home).title("Home"))
            moveCamera(CameraUpdateFactory.newLatLngZoom(home, 18f))
            addMarker(MarkerOptions().position(home))
            addGroundOverlay(
                GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                    .position(home, 100f)
            )
        }

    }

    private fun onMapLongClick(latLng: LatLng) {
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(
                    String.format(
                        Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude
                    )
                )
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    private fun onMapPoiClick(poi: PointOfInterest) {
        map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )?.showInfoWindow()
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }
    }

}