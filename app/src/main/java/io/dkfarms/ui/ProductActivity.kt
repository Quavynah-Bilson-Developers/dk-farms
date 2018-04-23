package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Cart
import io.dkfarms.data.Product
import io.dkfarms.util.Constants
import io.dkfarms.util.GlideApp
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ParallaxScrimageView

/**
 * Product details activity
 */
class ProductActivity : Activity() {
	private val wrapper: FrameLayout by bindView(R.id.action_wrapper)
	private val back: ImageButton by bindView(R.id.back)
	private val add: ImageButton by bindView(R.id.add)
	private val container: ViewGroup by bindView(R.id.container)
	private val name: TextView by bindView(R.id.product_name)
	private val desc: TextView by bindView(R.id.product_desc)
	private val price: TextView by bindView(R.id.product_price)
	private val quantity: TextView by bindView(R.id.product_qty)
	private val weight: Spinner by bindView(R.id.product_weight_spinner)
	private val image: ParallaxScrimageView by bindView(R.id.product_image)
	
	private lateinit var api: FarmBaseApi
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product)
		
		//Init api
		api = FarmBaseApi[this@ProductActivity]
		
		//Get intent
		val intent = intent
		if (intent.hasExtra(EXTRA_PRODUCT)) {
			val product = intent.getParcelableExtra<Product>(EXTRA_PRODUCT)
			bindProduct(product)
		}
	}
	
	private fun bindProduct(product: Product?) {
		if (product == null) return
		
		//Load image
		GlideApp.with(applicationContext)
//				.load(R.drawable.sugarcane)
				.load(product.image)
				.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				.transition(withCrossFade())
				.into(image)
		
		//Load other props
		name.text = product.name
		desc.text = product.description
		quantity.text = String.format("Quantity left: %d", product.quantity)
		price.text = String.format("Price: GHC %s", product.price)
		
		
		//Setup spinner
		val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
		arrayAdapter.addAll(product.weights)
		weight.adapter = arrayAdapter
		weight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onNothingSelected(parent: AdapterView<*>?) {}
			
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val obj = product.weights?.get(position)
				if (TextUtils.isDigitsOnly(obj)) {
					price.text = String.format("Price: GHC %s", product.price.times(obj!!.toInt()))
				} else {
					//Olonka is 5KG
					price.text = String.format("Price: GHC %s", product.price.times(5))
				}
			}
		}
		
		//Action
		add.setOnClickListener({
			if (api.isLoggedIn) {
				addToCart(product)
			} else {
				Snackbar.make(container, "Sorry you are not logged in", Snackbar.LENGTH_LONG).show()
			}
		})
		back.setOnClickListener({ onBackPressed() })
	}
	
	private fun addToCart(product: Product) {
		showMessage("Adding to cart")
		val document = api.firestore.collection("${Constants.CART_REF}/${api.uid}").document()
		val cart = Cart(document.id, product)
		document.set(cart).addOnCompleteListener(this@ProductActivity, { task ->
			if (task.isSuccessful) {
				showMessage("Added ${product.name}")
			} else {
				showMessage(task.exception?.localizedMessage)
			}
		}).addOnFailureListener(this, { exception ->
			showMessage(exception.localizedMessage)
		})
	}
	
	private fun showMessage(s: String?) {
		if (s == null) {
			Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
		} else {
			Snackbar.make(container, s, Snackbar.LENGTH_SHORT).show()
		}
	}
	
	companion object {
		const val EXTRA_PRODUCT = "EXTRA_PRODUCT"
	}
}
