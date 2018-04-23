package io.dkfarms.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.support.annotation.NonNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import io.dkfarms.data.Customer
import io.dkfarms.data.Supplier

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 *
 * API instantiating class: Init firebase APIs, User local data and more
 */
class FarmBaseApi private constructor(private val context: Context) {
	//Core props
	private var prefs: SharedPreferences
	var auth: FirebaseAuth
	var firestore: FirebaseFirestore
	var storage: FirebaseStorage
	
	//User login state
	var isLoggedIn: Boolean = false
	var isNewUser: Boolean = true
	var hasPendingPurchases: Boolean = false
	var isSavingData: Boolean = false
	var isConnected: Boolean = false
	
	//Common user props
	var id: Long? = null
	var uid: String? = null
	var email: String? = null
	var username: String? = null
	var photo: String? = null
	var phone: String? = null
	var address: String? = null
	var lat: Double? = null
	var lng: Double? = null
	var type: String? = null
	
	//Get customer instance
	val customer: Customer
		get() = Customer.Builder()
				.setID(id)
				.setAddress(address)
				.setEmail(email)
				//Always null because data will be retrieved from server when required
				.setLocation(if (lat == null || lng == null) null else GeoPoint(lat!!, lng!!))
				.setPhone(phone)
				.setPhoto(photo)
				.setUID(uid)
				.setDOB(null)
				.setUsername(username)
				.build()
	
	init {
		//Init shared preferences for storing data locally (usually with text format)
		prefs = context.getSharedPreferences(BASE_API_PREFS, Context.MODE_PRIVATE)
		
		//Init firebase APIs
		auth = FirebaseAuth.getInstance()
		firestore = FirebaseFirestore.getInstance()
		storage = FirebaseStorage.getInstance()
		
		//Set initial prefs
		uid = prefs.getString(KEY_UID, null)
		id = prefs.getLong(KEY_USER_ID, -1L)
		email = prefs.getString(KEY_EMAIL, null)
		username = prefs.getString(KEY_USERNAME, null)
		photo = prefs.getString(KEY_PHOTO, null)
		phone = prefs.getString(KEY_PHONE, null)
		address = prefs.getString(KEY_ADDRESS, null)
		type = prefs.getString(KEY_TYPE, null)
		lat = prefs.getFloat(KEY_LAT, 0.0f).toDouble()
		lng = prefs.getFloat(KEY_LNG, 0.0f).toDouble()
		
		//Set login state
		isLoggedIn = !uid.isNullOrEmpty()
		
		//New user state
		isNewUser = prefs.getBoolean(KEY_NEW_USER, true)
		
		//User has pending purchases
		hasPendingPurchases = prefs.getBoolean(KEY_PENDING_PURCHASES, false)
		
		isConnected = getInternetConnection()
		
		//User has enabled data saving
		isSavingData = prefs.getBoolean(KEY_DATA_SAVING, false)
		
		//Get details when user is logged in
		if (isLoggedIn) {
			uid = prefs.getString(KEY_UID, null)
			id = prefs.getLong(KEY_USER_ID, -1L)
			email = prefs.getString(KEY_EMAIL, null)
			username = prefs.getString(KEY_USERNAME, null)
			photo = prefs.getString(KEY_PHOTO, null)
			phone = prefs.getString(KEY_PHONE, null)
			address = prefs.getString(KEY_ADDRESS, null)
			type = prefs.getString(KEY_TYPE, null)
			lat = prefs.getFloat(KEY_LAT, 0.0f).toDouble()
			lng = prefs.getFloat(KEY_LNG, 0.0f).toDouble()
		}
	}
	
	private fun getInternetConnection(): Boolean {
		val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
		val networkInfo = manager?.activeNetworkInfo
		return networkInfo != null && networkInfo.isConnectedOrConnecting
	}
	
	/**
	 * Sets logged in user details
	 */
	fun updateUserData(@NonNull user: UserItem) {
		//Update login state
		isLoggedIn = true
		id = user.id
		email = user.email
		uid = user.uid
		username = user.username
		photo = user.photo
		phone = user.phone
		if (user is Customer) {
			//Set type to customer
			type = UserType.TYPE_CUSTOMER.label
			address = user.address
			lat = user.location?.latitude
			lng = user.location?.longitude
		} else if (user is Supplier) {
			type = UserType.TYPE_SUPPLIER.label
			address = user.address
		}
		
		//Store locally
		val editor = prefs.edit()
		editor.putLong(KEY_USER_ID, id!!)
		editor.putString(KEY_UID, uid)
		editor.putString(KEY_TYPE, type)
		editor.putString(KEY_EMAIL, email)
		editor.putString(KEY_USERNAME, username)
		editor.putString(KEY_PHOTO, photo)
		editor.putString(KEY_PHONE, phone)
		editor.putString(KEY_ADDRESS, address)
		if (user is Customer && lat != null && lng != null) {
			editor.putFloat(KEY_LAT, lat!!.toFloat())
			editor.putFloat(KEY_LNG, lng!!.toFloat())
		}
		editor.apply()
	}
	
	/**
	 * Sets user's logout state
	 */
	fun logout() {
		//Sign out of firebase
		if (auth.currentUser != null) auth.signOut()
		//Update login state
		isLoggedIn = false
		isNewUser = true
		email = null
		uid = null
		username = null
		photo = null
		phone = null
		address = null
		type = null
		lat = null
		lng = null
		id = -1L
		
		//Store locally
		val editor = prefs.edit()
		editor.putLong(KEY_USER_ID, id!!)
		editor.putString(KEY_UID, uid)
		editor.putString(KEY_TYPE, type)
		editor.putString(KEY_EMAIL, email)
		editor.putString(KEY_USERNAME, username)
		editor.putString(KEY_PHOTO, photo)
		editor.putString(KEY_ADDRESS, address)
		editor.putString(KEY_PHONE, phone)
		editor.putFloat(KEY_LAT, 0.0f)
		editor.putFloat(KEY_LNG, 0.0f)
		editor.apply()
	}
	
	/**
	 * Set new user state.
	 */
	fun setIsNewUser(state: Boolean) {
		isNewUser = state
		prefs.edit().putBoolean(KEY_NEW_USER, state).apply()
	}
	
	/**
	 * Set user's purchases state. Shows whether or not a user has any purchases incoming
	 */
	fun setHasPurchases(state: Boolean) {
		hasPendingPurchases = state
		prefs.edit().putBoolean(KEY_PENDING_PURCHASES, state).apply()
	}
	
	fun setDataSaver(state: Boolean) {
		isSavingData = state
		prefs.edit().putBoolean(KEY_DATA_SAVING, state).apply()
	}
	
	companion object {
		//Constants
		private const val BASE_API_PREFS = "BASE_API_PREFS"
		private const val KEY_USER_ID = "KEY_USER_ID"
		private const val KEY_UID = "KEY_UID"
		private const val KEY_EMAIL = "KEY_EMAIL"
		private const val KEY_USERNAME = "KEY_USERNAME"
		private const val KEY_PHOTO = "KEY_PHOTO"
		private const val KEY_PHONE = "KEY_PHONE"
		private const val KEY_ADDRESS = "KEY_ADDRESS"
		private const val KEY_TYPE = "KEY_TYPE"
		private const val KEY_NEW_USER = "KEY_NEW_USER"
		private const val KEY_PENDING_PURCHASES = "KEY_PENDING_PURCHASES"
		private const val KEY_DATA_SAVING = "KEY_DATA_SAVING"
		private const val KEY_LNG = "KEY_LNG"
		private const val KEY_LAT = "KEY_LAT"
		
		
		@SuppressLint("StaticFieldLeak")
		@Volatile
		private var singleton: FarmBaseApi? = null
		
		operator fun get(context: Context): FarmBaseApi {
			if (singleton == null) {
				synchronized(FarmBaseApi::class.java) {
					singleton = FarmBaseApi(context)
				}
			}
			return singleton!!
		}
	}
}