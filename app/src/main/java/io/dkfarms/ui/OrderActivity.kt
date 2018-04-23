package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import io.dkfarms.R
import io.dkfarms.api.CartDataManager
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Cart
import io.dkfarms.data.Product
import io.dkfarms.util.Constants
import io.dkfarms.util.UserCartAdapter
import io.peanutsdk.recyclerview.GridItemDividerDecoration
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import java.util.*

/**
 * Project : dk-farms
 * Package name : io.dkfarms.ui
 *
 * User's purchase order fragment
 */
class OrderActivity : Activity() {
	private val toolbar: Toolbar by bindView(R.id.toolbar)
	private val container: ViewGroup by bindView(R.id.container)
	private val emptyView: ViewGroup by bindView(R.id.empty_cart)
	private val grid: RecyclerView by bindView(R.id.grid_orders)
	
	private lateinit var api: FarmBaseApi
	private lateinit var dataManager: CartDataManager
	private lateinit var adapter: UserCartAdapter
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.fragment_order)
		setActionBar(toolbar)
		toolbar.setNavigationOnClickListener({ onBackPressed() })
		
		api = FarmBaseApi[this@OrderActivity]
		
		//init recyclerview
		adapter = UserCartAdapter(this, layoutInflater, api)
		val lm = LinearLayoutManager(this@OrderActivity)
		grid.adapter = adapter
		grid.layoutManager = lm
		grid.setHasFixedSize(true)
		grid.itemAnimator = SlideInItemAnimator()
		grid.addItemDecoration(GridItemDividerDecoration(this@OrderActivity, R.dimen.divider_height, R.color.divider))
		
		//Init data manager
		dataManager = object : CartDataManager(this) {
			override fun onDataLoaded(data: MutableList<Cart>) {
				adapter.add(data)
				checkEmptyState()
			}
		}
		
		//Load user data
		if (api.isLoggedIn) {
			dataManager.loadCart(api.uid!!)
			checkEmptyState()
		}
		
//		getDummyData()
	}
	
	/*private fun getDummyData() {
		Log.v("OrderActivity", "getDummyData() called")
		val list: MutableList<Cart> = ArrayList(0)
		val random = Random(6)
		for (i in 0 until 23) {
			val cart = Cart(
					random.nextInt().toString(),
					Product(
							"$i",
							"Name ${random.nextInt()}",
							"",
							"Demo description for $i",
							Constants.Type.SPICES.type,
							10.00.times(i.plus(1)),
							3.times(i),
							listOf("20", "30", "40", "50")
					)
			)
			
			if (cart.isViewable) {
				list.add(i, cart)
			}
		}
		adapter.add(list)
		checkEmptyState()
		
		Log.v("OrderActivity", adapter.getCartItems().toString())
	}*/
	
	private fun checkEmptyState() {
		if (adapter.getCartItems().isEmpty()) {
			TransitionManager.beginDelayedTransition(container)
			emptyView.visibility = View.VISIBLE
			grid.visibility = View.GONE
		} else {
			TransitionManager.beginDelayedTransition(container)
			emptyView.visibility = View.GONE
			grid.visibility = View.VISIBLE
		}
	}
	
	override fun onDestroy() {
		dataManager.cancelLoading()
		super.onDestroy()
	}
	
}