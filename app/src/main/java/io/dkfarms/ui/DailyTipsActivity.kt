package io.dkfarms.ui

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toolbar
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.dkfarms.R
import io.dkfarms.api.FarmBaseApi
import io.dkfarms.api.TipsDataManager
import io.dkfarms.data.DailyTips
import io.dkfarms.util.GlideApp
import io.peanutsdk.recyclerview.SlideInItemAnimator
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.FourThreeImageView

/**
 * Daily tips from administrator
 */
class DailyTipsActivity : Activity() {
	private val toolbar: Toolbar by bindView(R.id.toolbar)
	private val container: ViewGroup by bindView(R.id.container)
	private val grid: RecyclerView by bindView(R.id.tips_grid)
	private val empty: TextView by bindView(R.id.tips_empty)
	
	private lateinit var api: FarmBaseApi
	private lateinit var dataManager: TipsDataManager
	private lateinit var adapter: DailyTipsAdapter
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_daily_tips)
		setActionBar(toolbar)
		
		//init prefs
		api = FarmBaseApi[this@DailyTipsActivity]
		
		//init recyclerview
		adapter = DailyTipsAdapter()
		val lm = LinearLayoutManager(this@DailyTipsActivity, LinearLayoutManager.VERTICAL, false)
		grid.layoutManager = lm
		grid.setHasFixedSize(true)
		grid.adapter = adapter
		grid.itemAnimator = SlideInItemAnimator()
		grid.addItemDecoration(DividerItemDecoration(this, lm.orientation))
		
		//Init data manager
		dataManager = object : TipsDataManager(this) {
			override fun onDataLoaded(data: MutableList<DailyTips>) {
				//tips loaded
				adapter.addDailyTips(data)
				checkEmptyState()
			}
		}
		
		//Load daily tips
		dataManager.loadDailyTips()
		checkEmptyState()
		
		//Init toolbar props
		toolbar.setNavigationOnClickListener({ onBackPressed() })
	}
	
	private fun checkEmptyState() {
		if (adapter.itemCount == 0) {
			TransitionManager.beginDelayedTransition(container)
			grid.visibility = View.GONE
			empty.visibility = View.VISIBLE
		} else {
			TransitionManager.beginDelayedTransition(container)
			grid.visibility = View.VISIBLE
			empty.visibility = View.GONE
		}
	}
	
	override fun onDestroy() {
		dataManager.cancelLoading()
		super.onDestroy()
	}
	
	/**
	 * Adapter for daily tips
	 */
	internal inner class DailyTipsAdapter : RecyclerView.Adapter<DailyTipsAdapter.DailyTipsHolder>() {
		private val tips: MutableList<DailyTips> = ArrayList(0)
		
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyTipsHolder {
			return DailyTipsHolder(layoutInflater.inflate(R.layout.item_daily_tip, parent, false))
		}
		
		override fun getItemCount(): Int {
			return tips.size
		}
		
		override fun onBindViewHolder(holder: DailyTipsHolder, position: Int) {
			val dailyTips = tips[position]
			
			//Add props
			holder.title.text = dailyTips.title
			holder.content.text = dailyTips.content
			if (dailyTips.timestamp != null) {
				holder.timestamp.text = DateUtils.getRelativeTimeSpanString(dailyTips.timestamp!!.time,
						System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
			}
			GlideApp.with(holder.image.context)
					.asBitmap()
					.load(dailyTips.image)
					.placeholder(R.color.content_placeholder)
					.error(R.color.content_placeholder)
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.transition(withCrossFade())
					.into(holder.image)
			
			holder.close.setOnClickListener({ _ ->
				val pos = position
				tips.removeAt(pos)
				notifyItemRemoved(pos)
				
				val snackbar = Snackbar.make(container, "Deleted ${dailyTips.title?.trim()}", Snackbar.LENGTH_LONG)
				snackbar.setAction("Undo", { _ ->
					snackbar.dismiss()
					tips.add(pos, dailyTips)
					notifyItemInserted(pos)
				}).setActionTextColor(resources.getColor(android.R.color.holo_blue_light))
				snackbar.show()
			})
			
		}
		
		/**
		 * Add new daily tips here
		 */
		fun addDailyTips(newTips: MutableList<DailyTips>) {
			if (newTips.isEmpty()) return
			var add = true
			val size = tips.size
			
			for (item in newTips) {
				for (i in 0 until size) {
					if (item == tips[i]) add = false
				}
				
				if (add) {
					tips.add(item)
					notifyItemRangeChanged(0, newTips.size)
				}
				
			}
		}
		
		inner class DailyTipsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
			val title: TextView by bindView(R.id.tip_title)
			val content: TextView by bindView(R.id.tip_content)
			val timestamp: TextView by bindView(R.id.tip_timestamp)
			val image: FourThreeImageView by bindView(R.id.tip_image)
			val close: ImageButton by bindView(R.id.close_tip)
		}
	}
}
