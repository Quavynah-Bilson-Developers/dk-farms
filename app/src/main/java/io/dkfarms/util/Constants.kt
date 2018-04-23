package io.dkfarms.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import io.dkfarms.R
import io.peanutsdk.widget.PeanutLoadingIndicator

/**
 * Project : dk-farms
 * Package name : io.dkfarms.util
 *
 * Utility class for storing constants
 */
object Constants {
	//Categories
	const val BUSH_MEAT = "Bush Meat"
	const val CEREALS = "Cereal"
	const val FRESH_WATER_FISH = "Fresh Water Fish"
	const val FRUITS = "Fruit"
	const val MEAT = "Meat"
	const val MILK = "Milk"
	const val POULTRY = "Poultry"
	const val SEA_FOODS = "Sea Foods"
	const val SPICES = "Spices"
	const val TUBER_SUCKER_ROOT_OIL = "Tuber,Sucker,Root & Oil"
	const val VEGETABLES = "Vegetables"
	
	/**
	 * Product type
	 */
	enum class Type(val type: String) {
		BUSH_MEAT("bush_meat"),
		CEREALS("cereal"),
		FRESH_WATER_FISH("fresh_water_fish"),
		FRUITS("fruit"),
		MEAT("meat"),
		MILK("milk"),
		POULTRY("poultry"),
		SEA_FOODS("sea_food"),
		SPICES("spices"),
		TUBER_SUCKER_ROOT_OIL("tuber_root"),
		VEGETABLES("vegetable")
	}
	
	//Database root reference for version 1
	private const val root = "dk-farms/v1"
	
	//Customer database ref
	const val CUSTOMER_REF = "$root/customers"
	//Supplier database ref
	const val SUPPLIER_REF = "$root/suppliers"
	//Product database ref
	const val PRODUCT_REF = "$root/products"
	//Feedback database ref
	const val FEEDBACK_REF = "$root/feedback"
	//Daily tips database ref
	const val TIPS_REF = "$root/tips"
	//Shopping Cart database ref
	const val CART_REF = "dk-farms/carts"
	//Orders database ref
	const val ORDER_REF = "dk-farms/orders"
	//Purchases database ref
	const val PURCHASE_REF = "$root/purchases"
	
	//User profile image storage ref
	const val STORAGE_USER = "$root/users"
	
	//Loading dialog
	fun getDialog(context: Context): MaterialDialog {
		val v: View = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null, false)
		val indicator = v.findViewById<PeanutLoadingIndicator>(R.id.avi)
		
		return MaterialDialog.Builder(context)
				.theme(Theme.DARK)
				.customView(v, false)
				.showListener({ _ ->
					startAnim(indicator)
				})
				.dismissListener({ dialog ->
					stopAnim(indicator)
					dialog.dismiss()
				})
				.canceledOnTouchOutside(false)
				.build()
	}
	
	//Start loading indicator animation
	private fun startAnim(avi: PeanutLoadingIndicator) = avi.show()
	
	//Stop loading indicator animation
	private fun stopAnim(avi: PeanutLoadingIndicator) = avi.hide()
}