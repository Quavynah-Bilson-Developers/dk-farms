package io.dkfarms.api

import android.content.Context
import android.content.SharedPreferences

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 *
 * Local storage for user purchase progress
 */
class PurchasePrefs(context: Context) {
	private var prefs: SharedPreferences = context.getSharedPreferences(PURCHASE_PREFS, Context.MODE_PRIVATE)
	var progress: Int = 1
	var today: Long = -1L
	var isDue: Boolean = false
	
	init {
		//Set progress prefs
		progress = prefs.getInt(KEY_PROGRESS, 1)
		today = prefs.getLong(KEY_TODAY, -1L)
		
		//Set due date condition
		isDue = progress == 100
		
		//Get progress when due date is true
		if (isDue) progress = prefs.getInt(KEY_PROGRESS, 1)
	}
	
	fun setCurrentProgress(current: Int = 1) {
		progress = current
		prefs.edit().putInt(KEY_PROGRESS, progress).apply()
	}
	
	fun setFinalDate(todaysDate: Long) {
		today = todaysDate
		prefs.edit().putLong(KEY_TODAY, today).apply()
	}
	
	companion object {
		private const val PURCHASE_PREFS = "PURCHASE_PREFS"
		private const val KEY_PROGRESS = "KEY_PROGRESS"
		private const val KEY_TODAY = "KEY_TODAY"
	}
}