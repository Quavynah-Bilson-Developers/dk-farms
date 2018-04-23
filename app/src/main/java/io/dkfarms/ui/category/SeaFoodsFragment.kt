package io.dkfarms.ui.category


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.dkfarms.R
import io.dkfarms.api.ProductsDataManager
import io.dkfarms.data.Product
import io.dkfarms.util.CategoryGridAdapter
import io.dkfarms.util.Constants
import io.peanutsdk.recyclerview.GridItemDividerDecoration
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView


class SeaFoodsFragment : Fragment() {
	
	private val header: TextView by bindView(R.id.category_header)
	private val grid: RecyclerView by bindView(R.id.category_list)
	
	private lateinit var dataManager: ProductsDataManager
	private lateinit var adapter: CategoryGridAdapter
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.category_page, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		
		val host = activity!!
		
		header.text = Constants.SEA_FOODS
		
		val type: String = Constants.Type.SEA_FOODS.type
		adapter = CategoryGridAdapter(type, layoutInflater, host)
		grid.layoutManager = LinearLayoutManager(context)
		grid.setHasFixedSize(true)
		grid.adapter = adapter
		grid.itemAnimator = SlideInItemAnimator()
		grid.addItemDecoration(GridItemDividerDecoration(host, R.dimen.divider_height, R.color.divider))
		
		dataManager = object : ProductsDataManager(host) {
			override fun onDataLoaded(data: MutableList<Product>) {
				adapter.addNewProducts(data)
			}
		}
		
		dataManager.loadProductByType(type)
	}
	
	override fun onDestroy() {
		dataManager.cancelLoading()
		super.onDestroy()
	}
	
	
}
