package io.dkfarms.api

import android.app.Activity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.dkfarms.data.DailyTips
import io.dkfarms.util.Constants
import io.peanutsdk.recyclerview.BaseDataManager

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 *
 * Data manager for loading daily tips from the database
 */
abstract class TipsDataManager(private val context: Activity) : BaseDataManager<MutableList<DailyTips>>(context) {
	
	private val inflight: MutableList<Query> = ArrayList(0)
	private val db: CollectionReference = FirebaseFirestore.getInstance().collection(Constants.TIPS_REF)
	
	override fun cancelLoading() {
		if (inflight.isNotEmpty()) inflight.clear()
	}
	
	/**
	 * Loads all products irrespective of the type
	 */
	fun loadDailyTips() {
		val tips: MutableList<DailyTips> = ArrayList(0)
		loadStarted()
		inflight.add(db)
		
		//Load data from database
		db.addSnapshotListener(context, { querySnapshot, exception ->
			if (exception != null) {
				setResult(tips)
				return@addSnapshotListener
			}
			
			if (querySnapshot != null) {
				//Loop through all documents and convert them to product items
				for (doc in querySnapshot.documents) {
					if (doc.exists()) {
						val product = doc.toObject(DailyTips::class.java)
						tips.add(product)
					}
				}
				
				//Set callback
				setResult(tips)
			}
		})
	}
	
	private fun setResult(tips: MutableList<DailyTips>) {
		loadFinished()
		onDataLoaded(tips)
		inflight.remove(db)
	}
	
	
}