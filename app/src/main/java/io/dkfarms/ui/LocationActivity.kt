package io.dkfarms.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Customer
import io.dkfarms.util.Constants
import io.peanutsdk.util.bindView
import java.lang.Exception

/**
 * Project : dk-farms
 * Package name : io.dkfarms.ui
 *
 * User location activity
 */
class LocationActivity : Activity(), OnMapReadyCallback {
	
	private val toolbar: Toolbar by bindView(R.id.toolbar)
	private val location: TextView by bindView(R.id.user_location)
	private val update: Button by bindView(R.id.update_location)
	
	private lateinit var api: FarmBaseApi
	private var map: GoogleMap? = null
	private lateinit var customer: Customer
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.fragment_location)
		setActionBar(toolbar)
		toolbar.setNavigationOnClickListener({ onBackPressed() })
		
		api = FarmBaseApi[this@LocationActivity]
		
		//Init customer
		customer = api.customer
		
		//Init fragment
		val mapFragment = fragmentManager.findFragmentById(R.id.map) as? MapFragment
		
		//Fetch user data
		if (api.isLoggedIn && api.isConnected) {
			location.text = customer.address
			api.firestore.collection(Constants.CUSTOMER_REF)
					.document(customer.uid!!)
					.get()
					.addOnCompleteListener(this@LocationActivity, { task ->
						if (task.isSuccessful) {
							val snapshot = task.result
							if (snapshot.exists()) {
								customer = snapshot.toObject(Customer::class.java)
								mapFragment?.getMapAsync(this)
							}
						}
					})
		} else if (api.isLoggedIn) {
			location.text = api.address
		} else {
			location.text = getString(R.string.no_location)
		}
		
		//Update click action
		update.isEnabled = api.isLoggedIn
		update.setOnClickListener({
			try {
				val intent = PlacePicker.IntentBuilder().build(this@LocationActivity)
				startActivityForResult(intent, PLACE_CODE)
			} catch (e: GooglePlayServicesNotAvailableException) {
				showErrorMessage(e)
			} catch (e: GooglePlayServicesRepairableException) {
				showErrorMessage(e)
			}
		})
	}
	
	override fun onMapReady(p0: GoogleMap?) {
		map = p0
		
		if (map != null) {
			//If user has location address
			if (customer.location != null) {
				val position = LatLng(customer.location!!.latitude, customer.location!!.longitude)
				map!!.addMarker(MarkerOptions()
						.title(customer.username)
						.snippet(customer.address)
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
						.position(position))
				map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
			}
		}
	}
	
	
	private fun showErrorMessage(e: Exception) {
		Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == RESULT_OK) {
			if (requestCode == PLACE_CODE) {
				if (data != null) {
					val place = PlacePicker.getPlace(this, data)
					location.text = place.address
					
					customer.address = place.address.toString()
					val latLng = place.latLng
					customer.location = GeoPoint(latLng.latitude, latLng.longitude)
					api.updateUserData(customer)
				}
			}
		}
	}
	
	companion object {
		private const val PLACE_CODE = 33
	}
}