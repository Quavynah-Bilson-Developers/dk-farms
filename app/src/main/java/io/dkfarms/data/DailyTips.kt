package io.dkfarms.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Project : dk-farms
 * Package name : io.dkfarms.data
 *
 * Daily tip from admin data model
 */
class DailyTips : Parcelable {
	var key: String? = null
	var title: String? = null
	var image: String? = null
	var content: String? = null
	var timestamp: Date? = null
	
	constructor()
	
	constructor(key: String, title: String, image: String, content: String) {
		this.key = key
		this.title = title
		this.image = image
		this.content = content
		this.timestamp = Date(System.currentTimeMillis())
	}
	
	constructor(parcel: Parcel) : this() {
		key = parcel.readString()
		title = parcel.readString()
		image = parcel.readString()
		content = parcel.readString()
		val tmp = parcel.readLong()
		timestamp = if (tmp > -1L) Date(tmp) else null
	}
	
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(key)
		parcel.writeString(title)
		parcel.writeString(image)
		parcel.writeString(content)
		parcel.writeLong(if (timestamp == null) -1L else timestamp!!.time)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<DailyTips> {
		override fun createFromParcel(parcel: Parcel): DailyTips {
			return DailyTips(parcel)
		}
		
		override fun newArray(size: Int): Array<DailyTips?> {
			return arrayOfNulls(size)
		}
	}
}
