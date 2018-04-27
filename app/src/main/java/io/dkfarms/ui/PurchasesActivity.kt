package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle
import android.text.format.DateUtils
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.api.PurchasePrefs
import io.dkfarms.util.Constants
import io.dkfarms.util.GlideApp
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView

/**
 * User's purchases UI
 */
class PurchasesActivity : Activity() {
	private val toolbar: Toolbar by bindView(R.id.toolbar)
	private val details: TextView by bindView(R.id.purchase_progress_detail)
	private val progress: ProgressBar by bindView(R.id.purchase_progress)
	private val loading: ProgressBar by bindView(R.id.progress_loading)
	private val profile: ForegroundImageView by bindView(R.id.user_profile)
	private val username: TextView by bindView(R.id.user_username)
	private val confirmation: Button by bindView(R.id.confirmation)
	private val container: ViewGroup by bindView(R.id.container)
	private val progressContainer: ViewGroup by bindView(R.id.progress_container)
	
	private lateinit var api: FarmBaseApi
	private lateinit var prefs: PurchasePrefs
	private var isLoading: Boolean = true
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_purchases)
		setActionBar(toolbar)
		
		toolbar.setNavigationOnClickListener({ dismiss() })
		
		//Init shared preferences
		api = FarmBaseApi[this]
		prefs = PurchasePrefs(this)
		
		if (api.isLoggedIn) {
			checkLoadingState()
			loadUserData()
		}
	}
	
	private fun checkLoadingState() {
		if (isLoading) {
			TransitionManager.beginDelayedTransition(container)
			loading.visibility = View.VISIBLE
			progressContainer.visibility = View.GONE
		} else {
			TransitionManager.beginDelayedTransition(container)
			loading.visibility = View.GONE
			progressContainer.visibility = View.VISIBLE
		}
	}
	
	private fun loadUserData() {
		username.text = api.username?.toLowerCase()
		GlideApp.with(this)
				.load(api.photo)
				.circleCrop()
				.placeholder(R.drawable.avatar_placeholder)
				.error(R.drawable.avatar_placeholder)
				.fallback(R.drawable.avatar_placeholder)
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				.transition(withCrossFade())
				.into(profile)
		
		isLoading = true
		api.firestore.collection("${Constants.ORDER_REF}/${api.uid}")
				.get()
				.addOnCompleteListener(this, { task ->
					if (task.isSuccessful) {
						val documents = task.result.documents
						if (documents.isNotEmpty()) {
							val snapshot = documents[0]
							if (snapshot.exists()) {
								setProgressData()
							}
						}
					}
				})
	}
	
	private fun setProgressData() {
		isLoading = false
		checkLoadingState()
		
		//Get first order item and update UI
		val threeDaysTime = prefs.today
		val timestamp = System.currentTimeMillis()
		val max = (threeDaysTime - timestamp).toInt() / Constants.MAX_DIV
		prefs.setCurrentProgress(100.minus(max))
		
		//Button props
		confirmation.isEnabled = prefs.isDue
		confirmation.setOnClickListener({
			MaterialDialog.Builder(this)
					.title("Confirm product")
					.content(getString(R.string.confirm_prompt))
					.positiveText("Confirm")
					.negativeText("Dismiss")
					.onNegative({ dialog, _ -> dialog.dismiss() })
					.onPositive({ dialog, _ ->
						dialog.dismiss()
						setResultAndFinish()
					})
					.build().show()
		})
		
		if (prefs.isDue) {
			details.visibility = View.GONE
		} else {
			details.text = DateUtils.getRelativeTimeSpanString(threeDaysTime, timestamp,
					DateUtils.SECOND_IN_MILLIS)
		}
		progress.progress = prefs.progress
		progress.secondaryProgress = 100
	}
	
	private fun dismiss() {
		setResult(DISMISSING)
		finishAfterTransition()
	}
	
	private fun setResultAndFinish() {
		setResult(CART_CLEARED)
		finishAfterTransition()
	}
	
	override fun onBackPressed() {
		dismiss()
	}
	
	companion object {
		const val CART_CLEARED = 16
		const val DISMISSING = 17
	}
	
	
}
