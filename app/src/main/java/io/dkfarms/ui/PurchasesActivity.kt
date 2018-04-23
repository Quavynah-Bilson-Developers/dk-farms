package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle

import io.dkfarms.R

/**
 * User's purchases UI
 */
class PurchasesActivity : Activity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_purchases)
	}
	
	private fun dismiss() {
		setResult(CART_CLEARED)
		finishAfterTransition()
	}
	
	companion object {
		const val CART_CLEARED = 16
	}
}
