package io.dkfarms.api

import android.app.Activity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.dkfarms.data.Cart
import io.dkfarms.util.Constants
import io.peanutsdk.recyclerview.BaseDataManager

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 *
 * Data manager for loading a user's shopping cart content from the database
 */
abstract class CartDataManager(private val context: Activity) : BaseDataManager<MutableList<Cart>>(context) {
	
	private val inflight: MutableList<Query> = ArrayList(0)
	private val dREf: DocumentReference = FirebaseFirestore.getInstance().document(Constants.CART_REF)
	
	override fun cancelLoading() {
		if (inflight.isNotEmpty()) inflight.clear()
	}
	
	/**
	 * Loads all cart items for the current user
	 */
	fun loadCart(uid: String) {
		val carts: MutableList<Cart> = ArrayList(0)
		loadStarted()
		val db = dREf.collection(uid).whereEqualTo("isViewable", true)
		inflight.add(db)
		
		//Load data from database
		db.addSnapshotListener(context, { querySnapshot, exception ->
			if (exception != null) {
				setResult(carts, db)
				return@addSnapshotListener
			}
			
			if (querySnapshot != null) {
				//Loop through all documents and convert them to product items
				for (doc in querySnapshot.documents) {
					if (doc.exists()) {
						val product = doc.toObject(Cart::class.java)
						carts.add(product)
					}
				}
				
				//Set callback
				setResult(carts, db)
			}
		})
	}
	
	private fun setResult(carts: MutableList<Cart>, db: Query) {
		loadFinished()
		onDataLoaded(carts)
		inflight.remove(db)
	}
	
	
}