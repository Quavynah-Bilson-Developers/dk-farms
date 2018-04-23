package io.dkfarms.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.transition.TransitionManager
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Customer
import io.dkfarms.util.Constants
import io.dkfarms.util.DatePickerFragment
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.PasswordEntry

/**
 * User authentication screen
 */
class AuthActivity : Activity(), DatePickerDialog.OnDateSetListener {
	
	
	//Widgets
	private val container: ViewGroup by bindView(R.id.container)
	private val email: TextInputEditText by bindView(R.id.email_content)
	private val phone: TextInputEditText by bindView(R.id.phone_content)
	private val username: TextInputEditText by bindView(R.id.username_content)
	private val password: PasswordEntry by bindView(R.id.password_content)
	private val dob: Button by bindView(R.id.dob_button)
	private val register: Button by bindView(R.id.register_button)
	private val login: Button by bindView(R.id.login_button)
	private val forgotPassword: Button by bindView(R.id.forgot_password)
	
	//Helpers
	private lateinit var loading: MaterialDialog
	private lateinit var prefs: FarmBaseApi
	private lateinit var auth: FirebaseAuth
	private lateinit var db: CollectionReference
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_auth)
		
		//Preferences
		prefs = FarmBaseApi[this@AuthActivity]
		
		//Init loading dialog
		loading = Constants.getDialog(this@AuthActivity)
		
		//Init firebase
		db = prefs.firestore.collection(Constants.CUSTOMER_REF) //Customer database reference
		auth = prefs.auth   //Firebase auth init
	}
	
	/**
	 * Authenticates user using the email address and password provided
	 */
	fun doLogin(v: View) {
		if (isValidFields()) {
			try {
				//Get values for the email and password
				val email = email.text.toString()
				val pwd = password.text.toString()
				
				//Show loading dialog
				loading.show()
				
				//Sign in with credentials
				auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this, { task ->
					if (task.isSuccessful) {
						//User has been signed in successfully. Now retrieve user data from database
						db.document(task.result.user.uid).get()
								.addOnCompleteListener(this@AuthActivity, { getUserTask ->
									if (getUserTask.isSuccessful) {
										//Retrieve result from query
										val result = getUserTask.result
										
										//Check for existence of the data retrieved
										if (result.exists()) {
											try {
												//Convert result obtained from query to user data model
												val customer = result.toObject(Customer::class.java)
												
												//Set user data locally and navigate to home page
												prefs.updateUserData(customer)
												navHome()
											} catch (e: Exception) {
												//Data return may have been altered on the server
												// for some reason and may not return a
												// "Customer" data model and hence may crash the
												// app
												showMessage(e.localizedMessage)
											}
											
										} else {
											//Notify user of error
											showMessage("User data for \"$email\" does not exist")
										}
										
									} else {
										showMessage(getUserTask.exception?.localizedMessage)
									}
								})
					} else {
						//Display error reason to user
						showMessage(task.exception?.localizedMessage)
					}
				})
			} catch (e: Exception) {
				//Some devices do not have an updated version of play services and this could
				// cause the application to crash when this action is invoked
				showMessage(e.localizedMessage)
			}
		} else {
			//Fields have invalid values
			showMessage("Please check your credentials and try again")
		}
	}
	
	//Navigate to the Home screen
	private fun navHome(hasExtra: Boolean = false) {
		if (loading.isShowing) loading.dismiss()
		val intent = Intent(this@AuthActivity, HomeActivity::class.java)
		//Pass data to home screen to notify that the user is new
		if (hasExtra) intent.putExtra(HomeActivity.EXTRA_NEW_USER, hasExtra)
		startActivity(intent)
		finishAfterTransition()
	}
	
	/**
	 * Creates new user using the email address and password provided
	 */
	fun createAccount(v: View) {
		when {
			username.visibility != View.VISIBLE || phone.visibility != View.VISIBLE || dob.visibility != View.VISIBLE -> {
				TransitionManager.beginDelayedTransition(container)
				username.visibility = View.VISIBLE
				phone.visibility = View.VISIBLE
				dob.visibility = View.VISIBLE
				username.requestFocus()
			}
			
			!isValidFields(true) -> {
				showMessage("Please check your credentials and try again")
				dob.setTextColor(resources.getColor(R.color.error))
			}
			
			else -> {
				//Show loading dialog
				loading.show()
				
				Toast.makeText(applicationContext, "Pick your current location", Toast.LENGTH_LONG).show()
				startPlacePicker()
			}
		}
	}
	
	fun pickDOB(v: View) {
		val fragment = DatePickerFragment()
		if (fragmentManager != null) {
			fragment.show(fragmentManager, DatePickerFragment.TAG)
		}
	}
	
	@SuppressLint("SetTextI18n")
	override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
		dob.setTextColor(resources.getColor(R.color.text_primary_dark))
		dob.text = "$dayOfMonth-${month.plus(1)}-$year"
	}
	
	private fun startPlacePicker() {
		//Shows a map for the user to pick his / her location address
		try {
			val builder = PlacePicker.IntentBuilder()
			startActivityForResult(builder.build(this@AuthActivity), PLACE_PICKER_CODE)
		} catch (e: GooglePlayServicesNotAvailableException) {
			//Google play services may not be available on the user's device. this is rare though
			showMessage(e.localizedMessage)
		} catch (e: GooglePlayServicesRepairableException) {
			//Google play services may be damaged for some reason on the user's device.
			showMessage(e.localizedMessage)
		}
	}
	
	//Displays message in the form of toast or snackbar popup
	private fun showMessage(message: String?) {
		if (loading.isShowing) loading.dismiss()
		if (message != null) {
			Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show()
		} else {
			Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
		}
	}
	
	//Validates the fields of the user's email address and password
	private fun isValidFields(isRegistration: Boolean = false): Boolean {
		return if (isRegistration) {
			(!email.text.toString().isEmpty()
					&& Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()
					&& !username.text.toString().isEmpty()
					&& !phone.text.toString().isEmpty()
					&& dob.text.toString() != getString(R.string.dob_text)
					&& !password.text.toString().isEmpty()
					&& password.length() > 6)
		} else {
			(!email.text.toString().isEmpty()
					&& Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()
					&& !password.text.toString().isEmpty()
					&& password.length() > 6)
		}
	}
	
	fun resetPassword(v: View) {
		try {
			val email = email.text.toString()
			if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				showMessage("Please enter your email address only. Perhaps it is written wrongly")
				username.requestFocus()
			} else {
				loading.show()
				auth.sendPasswordResetEmail(email).addOnCompleteListener(this@AuthActivity, { task ->
					if (task.isSuccessful) {
						loading.dismiss()
						showMessage("Email sent successfully to $email")
					} else {
						showMessage(task.exception?.localizedMessage)
					}
				})
			}
		} catch (e: Exception) {
			showMessage(e.localizedMessage)
		}
	}
	
	override fun onBackPressed() {
		//Prompt user if they want to end an authentication action
		MaterialDialog.Builder(this@AuthActivity)
				.content("Are you sure you want to quit?")
				.positiveText("Quit")
				.negativeText("Cancel")
				.onPositive({ dialog, _ ->
					dialog.dismiss()
					super.onBackPressed()
				})
				.onNegative({ dialog, _ ->
					dialog.dismiss()
				})
				.build().show()
	}
	
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == RESULT_OK) {
			when (requestCode) {
				PLACE_PICKER_CODE -> {
					val place = PlacePicker.getPlace(this, data)
					val address = place.address
					val point = GeoPoint(place.latLng.latitude, place.latLng.longitude)
					createUserAccount(point, address)
				}
			}
		}
	}
	
	private fun createUserAccount(point: GeoPoint, address: CharSequence) {
		//Get values for the email and password
		val email = email.text.toString()
		val pwd = password.text.toString()
		
		//Sign in with credentials
		auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(this, { task ->
			if (task.isSuccessful) {
				
				//Get user data from activity
				val firebaseUser = task.result.user
				createUserFromData(firebaseUser, address, point)
			} else {
				showMessage(task.exception?.localizedMessage)
			}
		})
	}
	
	private fun createUserFromData(firebaseUser: FirebaseUser, address: CharSequence? = null, location: GeoPoint? = null) {
		//Create user instance
		val customer = Customer.Builder()
				.setID(System.currentTimeMillis())
				.setEmail(email.text.toString())
				.setUsername(username.text.toString())
				.setPhone(phone.text.toString())
				.setPhoto(firebaseUser.photoUrl.toString())
				.setUID(firebaseUser.uid)
				.setAddress(address.toString())
				.setDOB(dob.text.toString())
				.setLocation(location)
				.build()
		
		//Push data to database
		db.document(firebaseUser.uid).set(customer)
				.addOnCompleteListener(this@AuthActivity, { createUserTask ->
					if (createUserTask.isSuccessful) {
						//navigate home once user has been created
						prefs.updateUserData(customer)
						navHome(true)
					} else {
						showMessage(createUserTask.exception?.localizedMessage)
					}
				})
	}
	
	companion object {
		private const val PLACE_PICKER_CODE = 5
		
	}
}
