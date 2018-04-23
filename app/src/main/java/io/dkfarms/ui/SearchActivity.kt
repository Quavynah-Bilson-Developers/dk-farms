package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle
import io.dkfarms.R
import io.dkfarms.api.ProductsDataManager
import io.dkfarms.data.Product

/**
 * Query for products and categories
 */
class SearchActivity : Activity() {
	
	private lateinit var dataManager: ProductsDataManager
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_search)
		
		//Init data manager
		dataManager = object : ProductsDataManager(this@SearchActivity) {
			override fun onDataLoaded(data: MutableList<Product>) {
				//todo: add data to adapter
			}
		}
		dataManager.loadAllProducts()
	}
	
	override fun onDestroy() {
		dataManager.cancelLoading()
		super.onDestroy()
	}
}
