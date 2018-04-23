package io.dkfarms.util

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import io.dkfarms.ui.category.*

/**
 * Project : dk-farms
 * Package name : io.dkfarms.util
 *
 * Adapter for ViewPager on Home screen
 */
class HomePagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
	
	
	override fun getItem(position: Int): Fragment {
		return when (position) {
			0 -> {
				FruitFragment()
			}
			1 -> {
				VegetableFragment()
			}
			2 -> {
				BushMeatFragment()
			}
			3 -> {
				CerealFragment()
			}
			4 -> {
				FreshWaterFragment()
			}
			5 -> {
				MeatFragment()
			}
			6 -> {
				MilkFragment()
			}
			7 -> {
				PoultryFragment()
			}
			8 -> {
				SeaFoodsFragment()
			}
			9 -> {
				SpicesFragment()
			}
			10 -> {
				TuberFragment()
			}
			else -> throw IllegalArgumentException("this fragment is not valid")
		}
	}
	
	override fun getCount(): Int {
		return PAGE_COUNT
	}
	
	companion object {
		private const val PAGE_COUNT = 11
	}
}