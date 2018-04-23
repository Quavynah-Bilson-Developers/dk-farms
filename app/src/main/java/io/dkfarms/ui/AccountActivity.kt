package io.dkfarms.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ShareCompat
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Customer
import io.dkfarms.util.Constants
import io.dkfarms.util.GlideApp
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView

/**
 * User account activity
 */
class AccountActivity : Activity() {
	private val container: ViewGroup by bindView(R.id.container)
	private val email: TextView by bindView(R.id.user_email)
	private val profile: ForegroundImageView by bindView(R.id.user_profile)
	private val username: TextView by bindView(R.id.user_username)
	private val switch: Switch by bindView(R.id.data_switch)
	private val cacheReset: RelativeLayout by bindView(R.id.cache_reset_container)
	private val share: LinearLayout by bindView(R.id.action_share)
	private val toolbar: Toolbar by bindView(R.id.toolbar)
	
	private lateinit var api: FarmBaseApi
	private var imageUri: Uri? = null
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.fragment_account)
		setActionBar(toolbar)
		toolbar.setNavigationOnClickListener({ onBackPressed() })
		
		api = FarmBaseApi[this@AccountActivity]
		
		//Alter data saving state
		switch.isChecked = api.isSavingData
		switch.setOnCheckedChangeListener({ _, isChecked -> api.setDataSaver(isChecked) })
		
		//Load user data
		if (api.isLoggedIn) {
			var customer: Customer
			if (api.isConnected) {
				//Set placeholders while we make the fetch
				username.text = api.username
				email.text = api.email
				
				//Fetch user data from database
				api.firestore.collection(Constants.CUSTOMER_REF).document(api.uid!!)
						.get()
						.addOnCompleteListener(this@AccountActivity, { task ->
							if (task.isSuccessful) {
								val snapshot = task.result
								if (snapshot.exists()) {
									customer = snapshot.toObject(Customer::class.java)
									loadCustomer(customer)
								}
							} else {
								customer = api.customer
								loadCustomer(customer)
							}
						})
						.addOnFailureListener(this@AccountActivity, { _ ->
							customer = api.customer
							loadCustomer(customer)
						})
			} else {
				customer = api.customer
				loadCustomer(customer)
			}
		}
		
		cacheReset.setOnClickListener({
			Snackbar.make(container, "Cache cleared successfully", Snackbar.LENGTH_LONG).show()
		})
		
		share.setOnClickListener({
			ShareCompat.IntentBuilder.from(this@AccountActivity)
					.setChooserTitle("Share ${getString(R.string.farm_app_name)} with...")
					.setSubject(getString(R.string.share_content))
					.setType("text/*")
					.setText(getString(R.string.share_content))
					.startChooser()
		})
		
	}
	
	private fun loadCustomer(customer: Customer) {
		username.text = customer.username
		email.text = customer.email
		GlideApp.with(profile.context)
				.load(customer.photo)
				.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
				.circleCrop()
				.transition(DrawableTransitionOptions.withCrossFade())
				.placeholder(R.drawable.avatar_placeholder)
				.error(R.drawable.avatar_placeholder)
				.into(profile)
		
		profile.setOnClickListener({
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = "image/*"
			startActivityForResult(Intent.createChooser(intent, "Select profile image"),
					GALLERY_CODE)
		})
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == GALLERY_CODE) {
				if (data != null) {
					imageUri = data.data
					
					GlideApp.with(profile.context)
							.load(data.data)
							.circleCrop()
							.into(profile)
					
					if (api.isLoggedIn) {
						//upload image to storage
						uploadImage()
					}
				}
			}
		}
	}
	
	private fun uploadImage() {
		try {
			api.storage.getReference(Constants.STORAGE_USER).child("${api.uid}.jpg")
					.putFile(imageUri!!)
					.addOnSuccessListener(this@AccountActivity, { taskSnapshot ->
						imageUri = taskSnapshot.downloadUrl
						updateUser()
					})
		} catch (e: Exception) {
		}
	}
	
	private fun updateUser() {
		val hashMap = hashMapOf(
				Pair<String, Any?>("photo", imageUri.toString()),
				Pair<String, Any?>("username", username.text.toString())
		)
		api.firestore.collection(Constants.CUSTOMER_REF)
				.document(api.uid!!)
				.update(hashMap).addOnCompleteListener(this@AccountActivity, { task ->
					if (!task.isSuccessful) {
						Toast.makeText(applicationContext, "Unable to update user", Toast.LENGTH_LONG)
								.show()
					}
				}).addOnFailureListener(this@AccountActivity, { exception ->
					Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG)
							.show()
				})
	}
	
	companion object {
		private const val GALLERY_CODE = 21
	}
}
