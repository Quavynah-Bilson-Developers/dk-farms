package io.dkfarms.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Project : dk-farms
 * Package name : io.dkfarms.data
 *
 * Models a Product item
 */
class Product : Parcelable {
	//Fields: Has implicit getters and setters
	var key: String? = null
	var name: String? = null
	var image: String? = null
	var description: String? = null
	var type: String? = null
	var price: Double = 0.00
	var quantity: Int = 0
	var weights: List<String>? = null
	var timestamp: Date? = null
	
	//Default constructor
	constructor()
	
	constructor(key: String?, name: String?, image: String?, description: String?, type: String?,
	            price: Double, quantity: Int, weights: List<String>?) {
		this.key = key
		this.name = name
		this.image = image
		this.description = description
		this.type = type
		this.price = price
		this.quantity = quantity
		this.weights = weights
		this.timestamp = Date(System.currentTimeMillis())
	}
	
	constructor(parcel: Parcel) : this() {
		key = parcel.readString()
		name = parcel.readString()
		image = parcel.readString()
		description = parcel.readString()
		type = parcel.readString()
		price = parcel.readDouble()
		weights = parcel.createStringArrayList()
		quantity = parcel.readInt()
		val tmp = parcel.readLong()
		timestamp = if (tmp > -1L) Date(tmp) else null
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(key)
		parcel.writeString(name)
		parcel.writeString(image)
		parcel.writeString(description)
		parcel.writeString(type)
		parcel.writeDouble(price)
		parcel.writeStringList(weights)
		parcel.writeInt(quantity)
		parcel.writeLong(if (timestamp == null) -1L else timestamp!!.time)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<Product> {
		override fun createFromParcel(parcel: Parcel): Product {
			return Product(parcel)
		}
		
		override fun newArray(size: Int): Array<Product?> {
			return arrayOfNulls(size)
		}
	}
	
}
