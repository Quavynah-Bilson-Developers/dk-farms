package io.dkfarms.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Project : dk-farms
 * Package name : io.dkfarms.data
 *
 *
 * Models a customer's shopping cart item
 */
class Cart : Parcelable {
	var key: String? = null
	var product: Product? = null
	var timestamp: Date? = null
	var isViewable: Boolean = true
	
	constructor(parcel: Parcel) : this() {
		key = parcel.readString()
		product = parcel.readParcelable(Product::class.java.classLoader)
		isViewable = parcel.readByte() != 0.toByte()
		val tmp = parcel.readLong()
		timestamp = if (tmp > -1L) Date(tmp) else null
	}
	
	constructor()
	
	constructor(key: String?, product: Product?) {
		this.key = key
		this.product = product
		this.isViewable = true
		this.timestamp = Date(System.currentTimeMillis())
	}
	
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(key)
		parcel.writeParcelable(product, flags)
		parcel.writeByte(if (isViewable) 1 else 0)
		parcel.writeLong(if (timestamp == null) -1L else timestamp!!.time)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<Cart> {
		override fun createFromParcel(parcel: Parcel): Cart {
			return Cart(parcel)
		}
		
		override fun newArray(size: Int): Array<Cart?> {
			return arrayOfNulls(size)
		}
	}
}
