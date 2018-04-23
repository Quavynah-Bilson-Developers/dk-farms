package io.dkfarms.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Project : dk-farms
 * Package name : io.dkfarms.data
 *
 *
 * Models a ordered product item by a customer
 */
class Order : Parcelable {
	var key: String? = null
	var timestamp: Date? = null
	var carts: List<Cart>? = null
	var uid: String? = null
	var method: String? = null
	
	
	constructor()
	
	constructor(key: String, carts: List<Cart>, uid: String, method: String) {
		this.key = key
		this.timestamp = Date(System.currentTimeMillis())
		this.carts = carts
		this.uid = uid
		this.method = method
	}
	
	constructor(parcel: Parcel) : this() {
		key = parcel.readString()
		carts = parcel.createTypedArrayList(Cart)
		uid = parcel.readString()
		method = parcel.readString()
		val tmp = parcel.readLong()
		timestamp = if (tmp > -1L) Date(tmp) else null
	}
	
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(key)
		parcel.writeTypedList(carts)
		parcel.writeString(uid)
		parcel.writeString(method)
		parcel.writeLong(if (timestamp == null) -1L else timestamp!!.time)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	override fun toString(): String {
		return "Order(key=$key, timestamp=$timestamp, carts=$carts, uid=$uid, method=$method)"
	}
	
	companion object CREATOR : Parcelable.Creator<Order> {
		override fun createFromParcel(parcel: Parcel): Order {
			return Order(parcel)
		}
		
		override fun newArray(size: Int): Array<Order?> {
			return arrayOfNulls(size)
		}
	}
	
	
}
