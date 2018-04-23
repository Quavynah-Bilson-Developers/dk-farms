package io.dkfarms.util

import android.app.Activity
import android.content.Intent
import android.support.annotation.NonNull
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.dkfarms.R
import io.dkfarms.data.Product
import io.dkfarms.ui.ProductActivity
import io.peanutsdk.recyclerview.Divided
import io.peanutsdk.util.bindView

/**
 * Project : dk-farms
 * Package name : io.dkfarms.util
 *
 * Adapter for binding products data by category. By default, it loads only bush meat
 */
class CategoryGridAdapter(@NonNull private var type: String,
                          private val inflater: LayoutInflater,
                          private val context: Activity) :
		RecyclerView.Adapter<CategoryGridAdapter.CategoryViewHolder>() {
	
	private val products: MutableList<Product> = ArrayList(0)
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
		return CategoryViewHolder(inflater.inflate(R.layout.item_sub_category, parent, false))
	}
	
	override fun getItemCount(): Int {
		return products.size
	}
	
	override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
		val product = products[position]
		
		holder.name.text = product.name
		holder.quantity.text = String.format("%d units", product.quantity)
		
		holder.itemView.setOnClickListener({
			val intent = Intent(context, ProductActivity::class.java)
			intent.putExtra(ProductActivity.EXTRA_PRODUCT, product)
			context.startActivity(intent)
		})
	}
	
	/**
	 * Entry point for adding new products
	 */
	fun addNewProducts(newProducts: MutableList<Product>) {
		if (newProducts.isEmpty()) return
		for (item in newProducts) {
			//Check for similar items: Do not add new item if it is already in the old list
			//Check for types: Add only when the product type matches the category
			if (item.type == type) {
				products.add(item)
				notifyItemRangeChanged(0, newProducts.size)
			}
		}
	}
	
	
	class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Divided {
		val name: TextView by bindView(R.id.sub_name)
		val quantity: TextView by bindView(R.id.sub_count)
	}
}

