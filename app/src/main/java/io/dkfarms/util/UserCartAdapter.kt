package io.dkfarms.util

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.dkfarms.BuildConfig
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.data.Cart
import io.dkfarms.data.Order
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView
import java.text.NumberFormat

/**
 * Project : dk-farms
 * Package name : io.dkfarms.util
 *
 * User's cart adapter
 */
class UserCartAdapter(private val context: Activity,
                      private val inflater: LayoutInflater,
                      private val api: FarmBaseApi) :
		RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	private val carts: MutableList<Cart> = ArrayList(0)
	private val nf: NumberFormat = NumberFormat.getInstance()
	private var loading: MaterialDialog = Constants.getDialog(context)
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			TYPE_CART_DATA -> {
				createCartDataHolder(parent)
			}
			
			TYPE_CART_CONTENT -> {
				createCartContentHolder(parent)
			}
			
			else -> throw IllegalArgumentException("Viewholder cannot be created")
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (position) {
			0 -> TYPE_CART_CONTENT
			else -> TYPE_CART_DATA
		}
	}
	
	private fun createCartContentHolder(parent: ViewGroup): ContentViewHolder {
		return ContentViewHolder(inflater.inflate(R.layout.cart_data_content, parent, false))
	}
	
	private fun createCartDataHolder(parent: ViewGroup): UserCartHolder {
		return UserCartHolder(inflater.inflate(R.layout.item_cart, parent, false))
	}
	
	override fun getItemCount(): Int {
		var count = 1
		if (carts.isNotEmpty()) count += carts.size
		return count
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (getItemViewType(position)) {
			TYPE_CART_DATA -> {
				bindCartData(holder as UserCartHolder)
			}
			
			TYPE_CART_CONTENT -> {
				bindContent(holder as ContentViewHolder)
			}
		}
	}
	
	private var hasPassedMinimum: Boolean = false
	private var dcost: Double = 12.00
	private var subPrices = 0.00
	private var paymentMethod: CharSequence = context.resources.getStringArray(R.array.payment_options)[0]
	private fun bindCartData(holder: UserCartHolder) {
		//get position of the adapter
		val position = holder.adapterPosition
		
		//Check the view type
		if (getItemViewType(position) == TYPE_CART_DATA) {
			//Get cart item
			val cart = carts[position - 1]
			
			//Get product item
			val product = cart.product
			
			//Set constant variable to keep count of items being added
			var count = 1
			
			//Set fields
			holder.name.text = product?.name
			holder.price.text = String.format("GHC %s/each", nf.format(product?.price))
			holder.count.text = String.format("%s", nf.format(count))
			GlideApp.with(context)
					.load(product?.image)
					.placeholder(R.drawable.avatar_placeholder)
					.error(R.drawable.avatar_placeholder)
					.circleCrop()
					.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
					.transition(withCrossFade())
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.into(holder.image)
			
			//Add items
			holder.add.setOnClickListener({
				count++
				holder.count.text = String.format("%s", nf.format(count))
			})
			
			//Less items
			holder.less.setOnClickListener({
				if (count == 1) return@setOnClickListener
				count--
				holder.count.text = String.format("%s", nf.format(count))
			})
			
			//Remove item from cart
			holder.close.setOnClickListener({ v ->
				val pos = position - 1
				val snackbar = Snackbar.make(v, "Remove ${product?.name}?", Snackbar.LENGTH_LONG)
				snackbar.setAction("Yes", {
					carts.remove(carts[pos])
					notifyItemRemoved(pos)
					removeFromDB(cart.key, v)
				})
				snackbar.show()
			})
		}
	}
	
	private fun bindContent(holder: ContentViewHolder) {
		val stringArray = context.resources.getStringArray(R.array.payment_options)
		val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, stringArray)
		holder.spinner.adapter = adapter
		holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onNothingSelected(parent: AdapterView<*>?) {}
			
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				when (position) {
					0 -> paymentMethod = stringArray[0]
					1 -> paymentMethod = stringArray[1]
				}
			}
		}
		
		//Init props
		holder.deliveryCost.text = String.format("GHC %s", nf.format(dcost))
		holder.subtotal.text = getSubTotal(holder)
		holder.location.text = api.address
		holder.checkout.setOnClickListener({ v ->
			if (hasPassedMinimum) {
				if (api.isConnected) {
					if (api.isLoggedIn) {
						loading.show()
						addToOrders()
					} else {
						showAlert("Login required", "Sorry you are not logged in")
					}
				} else {
					showAlert("Warning", "Cannot connect to the internet")
				}
			} else {
				showAlert("Minimum purchase alert", "You can only place order for items worth GHC 50.00 or more")
			}
		})
	}
	
	private fun showAlert(title: CharSequence, message: CharSequence) {
		MaterialDialog.Builder(context)
				.title(title)
				.content(message)
				.positiveText("OK")
				.onPositive({ dialog, _ -> dialog.dismiss() })
				.build().show()
	}
	
	private fun getSubTotal(holder: ContentViewHolder): CharSequence {
		for (cart in carts) {
			val price = cart.product?.price
			if (price != null) subPrices += price
		}
		
		//Set total cost
		val total = subPrices + dcost
		if (total >= 50.00) hasPassedMinimum = true
		holder.total.text = String.format("GHC %s", nf.format(total))
		
		return String.format("GHC %s", nf.format(subPrices))
	}
	
	private fun addToOrders() {
		val document = api.firestore.collection("${Constants.ORDER_REF}/${api.uid}").document()
		
		//List of orders
		val order = Order(
				document.id,
				carts,
				api.uid!!,
				paymentMethod.toString()
		)
		
		if (BuildConfig.DEBUG) {
			Log.v("UserCartAdapter", order.toString())
		}
		
		//Document
		document.set(order).addOnCompleteListener(context, { task ->
			if (task.isSuccessful) {
				api.setHasPurchases(true)
				updateContentInCart()
			} else {
				loading.dismiss()
				showStateDialog(false)
			}
		}).addOnFailureListener(context, { _ -> showStateDialog(false) })
	}
	
	private fun updateContentInCart() {
		api.firestore.collection("${Constants.CART_REF}/${api.uid}").get()
				.addOnCompleteListener(context, { task ->
					if (task.isSuccessful) {
						for (document in task.result.documents) {
							if (document.exists()) {
								document.reference.delete().addOnCompleteListener(context, { _ -> })
							}
						}
						
						showStateDialog(true)
					} else {
						showStateDialog(false)
					}
				}).addOnFailureListener(context, { _ -> showStateDialog(false) })
	}
	
	private fun showStateDialog(state: Boolean) {
		val v = inflater.inflate(R.layout.purchase_status_view, null, false)
		val status = v.findViewById<TextView>(R.id.purchases_status_text)
		val img = v.findViewById<ForegroundImageView>(R.id.purchases_status_img)
		
		val builder = MaterialDialog.Builder(context)
				.cancelable(false)
				.canceledOnTouchOutside(false)
				.theme(Theme.LIGHT)
				.customView(v, true)
		builder.positiveText("Dismiss")
		if (state) {
			status.text = context.getString(R.string.successful_purchase)
			img.setImageResource(R.drawable.ic_success)
			builder.onPositive({ dialog, _ ->
				dialog.dismiss()
				context.finishAfterTransition()
			})
		} else {
			status.text = context.getString(R.string.unsuccessful_purchase)
			img.setImageResource(R.drawable.ic_failure)
			builder.onPositive({ dialog, _ -> dialog.dismiss() })
		}
		
		//Show dialog
		builder.build().show()
	}
	
	private fun removeFromDB(key: String?, v: View) {
		if (key.isNullOrEmpty()) {
			Snackbar.make(v, "Cannot remove item from database", Snackbar.LENGTH_LONG).show()
		} else {
			api.firestore.document("${Constants.CART_REF}/${api.uid}/$key")
					.delete()
					.addOnCompleteListener(context, { task ->
						if (task.isSuccessful) {
							if (BuildConfig.DEBUG) Log.d("UserCartAdapter",
									"$key removed successfully")
						} else {
							Toast.makeText(context.applicationContext, task.exception?.localizedMessage,
									Toast.LENGTH_LONG).show()
						}
					})
					.addOnFailureListener(context, { exception ->
						Toast.makeText(context.applicationContext, exception.localizedMessage, Toast.LENGTH_LONG)
								.show()
					})
		}
	}
	
	/**
	 * Add new content to shopping cart from database
	 */
	fun add(data: MutableList<Cart>) {
		if (data.isEmpty()) return
		var add = true
		val size = carts.size
		for (item in data) {
			for (i in 0 until size) {
				if (item.key == carts[i].key) add = false
			}
			
			//Add items that are not already in the list
			if (add) {
				carts.add(item)
				notifyItemRangeChanged(0, data.size)
			}
		}
	}
	
	/**
	 * Get content of existing cart
	 */
	fun getCartItems(): MutableList<Cart> = carts
	
	/**
	 * User cart viewholder
	 */
	class UserCartHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		//Fields
		val image: ForegroundImageView by bindView(R.id.product_image)
		val name: TextView by bindView(R.id.product_name)
		val price: TextView by bindView(R.id.product_price)
		val count: TextView by bindView(R.id.count)
		val less: ImageButton by bindView(R.id.less)
		val add: ImageButton by bindView(R.id.add)
		val close: ImageButton by bindView(R.id.close_item_cart)
	}
	
	/**
	 * Cart content viewholder
	 */
	class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val subtotal: TextView by bindView(R.id.subtotal)
		val total: TextView by bindView(R.id.total)
		val deliveryCost: TextView by bindView(R.id.dcost)
		val location: TextView by bindView(R.id.location)
		val checkout: Button by bindView(R.id.checkout)
		val spinner: Spinner by bindView(R.id.payment_method_spinner)
	}
	
	companion object {
		private const val TYPE_CART_DATA = R.layout.item_cart
		private const val TYPE_CART_CONTENT = R.layout.cart_data_content
	}
}