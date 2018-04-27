package io.dkfarms.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Customer
import io.dkfarms.util.Constants
import io.dkfarms.util.GlideApp
import io.dkfarms.util.HomePagerAdapter
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView
import io.peanutsdk.widget.InkPageIndicator
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.*

/**
 * Home screen of the application
 */
class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
	private val drawer: DrawerLayout by bindView(R.id.drawer_layout)
	private val navView: NavigationView by bindView(R.id.nav_view)
	private val toolbar: Toolbar by bindView(R.id.toolbar)
	private val pager: ViewPager by bindView(R.id.pager)
	private val indicator: InkPageIndicator by bindView(R.id.indicator)
	
	private lateinit var manager: FragmentManager
	private lateinit var headerView: View
	//Core props
	private lateinit var api: FarmBaseApi
	private lateinit var pagerAdapter: HomePagerAdapter
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_home)
		setSupportActionBar(toolbar)
		
		//Fragment manager
		manager = supportFragmentManager
		
		//Prefs
		api = FarmBaseApi[this@HomeActivity]
		
		//Init fragments
		initProps()
		
		val toggle = ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		drawer.addDrawerListener(toggle)
		toggle.syncState()
		
		navView.setNavigationItemSelectedListener(this)
		headerView = navView.getHeaderView(0)
		setupHeader()
	}
	
	private fun initProps() {
		//init viewpager & indicator
		pagerAdapter = HomePagerAdapter(supportFragmentManager)
		pager.adapter = pagerAdapter
		pager.pageMargin = resources.getDimensionPixelSize(R.dimen.spacing_normal)
		indicator.setViewPager(pager)
	}
	
	private fun setupHeader() {
		val profile = headerView.findViewById<ImageView>(R.id.user_profile)
		val username = headerView.findViewById<TextView>(R.id.user_username)
		val email = headerView.findViewById<TextView>(R.id.user_email)
		
		if (api.isLoggedIn) {
			val customer = api.customer
			GlideApp.with(applicationContext)
					.asBitmap()
					.load(customer.photo)
					.placeholder(R.drawable.avatar_placeholder)
					.error(R.drawable.avatar_placeholder)
					.fallback(R.drawable.avatar_placeholder)
					.circleCrop()
					.transition(withCrossFade())
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.into(profile)
			
			username.text = customer.username
			email.text = customer.email
		}
	}
	
	override fun onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START)
		} else {
			super.onBackPressed()
		}
	}
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.home, menu)
		return true
	}
	
	override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
		val purchasesItem = menu?.findItem(R.id.action_purchases)
		purchasesItem?.isEnabled = api.hasPendingPurchases
		return true
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		when (item.itemId) {
			R.id.action_search -> {
				startActivity(Intent(this@HomeActivity, SearchActivity::class.java))
				return true
			}
			R.id.action_logout -> {
				if (api.isLoggedIn) {
					api.logout()
					startActivity(Intent(this@HomeActivity, AuthActivity::class.java))
					finish()
				}
				return true
			}
			R.id.action_purchases -> {
				startActivityForResult(Intent(this@HomeActivity, PurchasesActivity::class.java), PURCHASES_VIEW_CODE)
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}
	
	override fun onEnterAnimationComplete() {
		super.onEnterAnimationComplete()
		if (api.isNewUser) {
			setupPrompts()
		}
	}
	
	private fun setupPrompts() {
		MaterialTapTargetPrompt.Builder(this)
				.setPrimaryText(R.string.search_prompt_title)
				.setSecondaryText(R.string.search_prompt_description)
				.setAnimationInterpolator(FastOutSlowInInterpolator())
				.setAutoDismiss(false)
				.setAutoFinish(false)
				.setIcon(R.drawable.ic_search_24dp)
				.setTarget(R.id.action_search)
				.setPromptStateChangeListener { prompt, state ->
					when (state) {
						MaterialTapTargetPrompt.STATE_FOCAL_PRESSED,
						MaterialTapTargetPrompt.STATE_DISMISSED,
						MaterialTapTargetPrompt.STATE_FINISHED -> {
							prompt.finish()
							api.setIsNewUser(false)
						}
						
					}
				}
				.show()
	}
	
	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		// Handle navigation view item clicks here.
		when (item.itemId) {
			R.id.nav_orders -> {
				//Orders fragment
				startActivity(Intent(this@HomeActivity, OrderActivity::class.java))
			}
			R.id.nav_account -> {
				//Account fragment
				startActivity(Intent(this@HomeActivity, AccountActivity::class.java))
			}
			R.id.nav_location -> {
				startActivity(Intent(this@HomeActivity, LocationActivity::class.java))
			}
			R.id.nav_feedback -> {
				//Feedback dialog
				val customView = layoutInflater.inflate(R.layout.feedback_popup, null, false)
				
				//get fields
				val userProfile = customView.findViewById<ForegroundImageView>(R.id.user_profile)
				val editText = customView.findViewById<EditText>(R.id.user_feedback)
				
				//Load profile image
				if (api.isLoggedIn) {
					GlideApp.with(applicationContext)
							.load(api.photo)
							.placeholder(R.drawable.avatar_placeholder)
							.error(R.drawable.avatar_placeholder)
							.fallback(R.drawable.avatar_placeholder)
							.circleCrop()
							.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.into(userProfile)
					
					//Build dialog
					MaterialDialog.Builder(this@HomeActivity)
							.customView(customView, true)
							.positiveText("Send")
							.negativeText("Return")
							.onPositive({ dialog, _ ->
								val message = editText.text.toString()
								Toast.makeText(applicationContext,
										"Your feedback has been sent successfully",
										Toast.LENGTH_SHORT).show()
								sendFeedback(message)
								dialog.dismiss()
							})
							.onNegative({ dialog, _ -> dialog.dismiss() })
							.build().show()
				} else {
					Snackbar.make(drawer, "Sorry ! you are not logged in for some reason", Snackbar.LENGTH_LONG)
							.show()
				}
				
				
			}
			R.id.nav_tips -> {
				//Daily tips activity
				startActivity(Intent(this@HomeActivity, DailyTipsActivity::class.java))
			}
		}
		
		drawer.closeDrawer(GravityCompat.START)
		return true
	}
	
	/**
	 * Send feedback to the database
	 */
	private fun sendFeedback(message: String) {
		val hashMap = hashMapOf(
				Pair<String, Any?>("timestamp", Date(System.currentTimeMillis())),
				Pair<String, Any?>("message", message),
				Pair<String, Any?>("customer", Customer.toMap(api.customer))
		)
		api.firestore.collection(Constants.FEEDBACK_REF).document()
				.set(hashMap)
				.addOnCompleteListener(this@HomeActivity, { task ->
					if (!task.isSuccessful) {
						Toast.makeText(applicationContext, task.exception?.localizedMessage,
								Toast.LENGTH_LONG).show()
					}
				}).addOnFailureListener(this@HomeActivity, { exception ->
					Toast.makeText(applicationContext, exception.localizedMessage, Toast
							.LENGTH_LONG).show()
				})
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (requestCode) {
			PURCHASES_VIEW_CODE -> {
				if (resultCode == PurchasesActivity.CART_CLEARED) {
					invalidateOptionsMenu()
					clearOrder()
				}
			}
		}
	}
	
	private fun clearOrder() {
		api.firestore.collection("${Constants.ORDER_REF}/${api.uid}")
				.get().addOnCompleteListener(this, { task ->
					if (task.isSuccessful) {
						for (document in task.result.documents) {
							document.reference.delete().addOnCompleteListener(this, { _ -> })
						}
						
						Snackbar.make(drawer, "Thank you for shopping with us", Snackbar.LENGTH_LONG)
								.show()
					}
				})
	}
	
	companion object {
		const val EXTRA_NEW_USER = "EXTRA_NEW_USER"
		const val PURCHASES_VIEW_CODE = 8
	}
}
