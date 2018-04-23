package io.dkfarms.api

import android.app.Activity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.dkfarms.data.Product
import io.dkfarms.util.Constants
import io.peanutsdk.recyclerview.BaseDataManager

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 *
 * Data manager for loading products from the database
 */
abstract class ProductsDataManager(private val context: Activity) : BaseDataManager<MutableList<Product>>(context) {
	
	private val inflight: MutableList<Query> = ArrayList(0)
	private val db: CollectionReference = FirebaseFirestore.getInstance().collection(Constants.PRODUCT_REF)
	
	override fun cancelLoading() {
		if (inflight.isNotEmpty()) inflight.clear()
	}
	
	/**
	 * Loads all products irrespective of the type
	 */
	fun loadAllProducts() {
		val products: MutableList<Product> = ArrayList(0)
		loadStarted()
		inflight.add(db)
		
		//Load data from database
		db.addSnapshotListener(context, { querySnapshot, exception ->
			if (exception != null) {
				setResult(products)
				return@addSnapshotListener
			}
			
			if (querySnapshot != null) {
				//Loop through all documents and convert them to product items
				for (doc in querySnapshot.documents) {
					if (doc.exists()) {
						val product = doc.toObject(Product::class.java)
						products.add(product)
					}
				}
				
				//Set callback
				setResult(products)
			}
		})
	}
	
	/**
	 * Gets snapshots for products of a particular kind
	 */
	fun loadProductByType(type: String) {
		val products: MutableList<Product> = ArrayList(0)
		loadStarted()
		inflight.add(db)
		
		//Load data from database
		db.addSnapshotListener(context, { querySnapshot, exception ->
			if (exception != null) {
				setResult(products)
				return@addSnapshotListener
			}
			
			if (querySnapshot != null) {
				//Loop through all documents and convert them to product items
				for (doc in querySnapshot.documents) {
					if (doc.exists()) {
						val product = doc.toObject(Product::class.java)
						if (product.type == type) {
							products.add(product)
						}
					}
				}
				
				//Set callback
				setResult(products)
			}
		})
	}
	
	private fun setResult(products: MutableList<Product>) {
		loadFinished()
		onDataLoaded(products)
		inflight.remove(db)
	}
	
	
}