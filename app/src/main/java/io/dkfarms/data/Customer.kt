package io.dkfarms.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import io.dkfarms.api.UserItem

/**
 * Project : dk-farms
 * Package name : io.dkfarms.data
 *
 * Models a DKFarm customer
 */
class Customer : UserItem, Parcelable {
	
	override var id: Long? = -1L
	override var uid: String? = null
	override var email: String? = null
	override var username: String? = null
	override var photo: String? = null
	override var phone: String? = null
	var address: String? = null
	var dob: String? = null
	var location: GeoPoint? = null
	
	//Default constructor for serialization by firebase
	constructor()
	
	//Secondary constructor for creating customer data model
	constructor(id: Long? = System.currentTimeMillis(), uid: String?, dob: String?, email: String?, username: String?,
	            photo: String?, phone: String?, address: String? = null, location: GeoPoint? = null) {
		this.id = id
		this.uid = uid
		this.email = email
		this.username = username
		this.photo = photo
		this.phone = phone
		this.address = address
		this.dob = dob
		this.location = location
	}
	
	/**
	 * Builder class for creating new user instance
	 */
	class Builder {
		private var id: Long? = -1L
		private var uid: String? = null
		private var email: String? = null
		private var username: String? = null
		private var photo: String? = null
		private var phone: String? = null
		private var address: String? = null
		private var dob: String? = null
		private var location: GeoPoint? = null
		
		fun setID(id: Long?): Builder {
			this.id = id
			return this
		}
		
		fun setUID(uid: String?): Builder {
			this.uid = uid
			return this
		}
		
		fun setEmail(email: String?): Builder {
			this.email = email
			return this
		}
		
		fun setUsername(username: String?): Builder {
			this.username = username
			return this
		}
		
		fun setPhoto(photo: String?): Builder {
			this.photo = photo
			return this
		}
		
		fun setPhone(phone: String?): Builder {
			this.phone = phone
			return this
		}
		
		fun setAddress(address: String?): Builder {
			this.address = address
			return this
		}
		
		fun setDOB(dob: String?): Builder {
			this.dob = dob
			return this
		}
		
		fun setLocation(location: GeoPoint?): Builder {
			this.location = location
			return this
		}
		
		//this return the user data
		fun build(): Customer {
			return Customer(id, uid, dob, email, username, photo, phone, address, location)
		}
	}
	
	companion object CREATOR : Parcelable.Creator<Customer> {
		override fun createFromParcel(parcel: Parcel): Customer {
			return Customer(parcel)
		}
		
		override fun newArray(size: Int): Array<Customer?> {
			return arrayOfNulls(size)
		}
		
		fun toMap(existing: Customer?): Map<String, Any?> {
			return if (existing == null) emptyMap<String, Any?>() else {
				mapOf(
						Pair("id", existing.id),
						Pair("uid", existing.uid),
						Pair("username", existing.username),
						Pair("email", existing.email),
						Pair("photo", existing.photo),
						Pair("phone", existing.phone),
						Pair("address", existing.address),
						Pair("dob", existing.dob),
						Pair("location", existing.location)
				)
			}
		}
	}
	
	constructor(parcel: Parcel) : this() {
		id = parcel.readValue(Long::class.java.classLoader) as? Long
		uid = parcel.readString()
		email = parcel.readString()
		username = parcel.readString()
		photo = parcel.readString()
		phone = parcel.readString()
		address = parcel.readString()
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeValue(id)
		parcel.writeString(uid)
		parcel.writeString(email)
		parcel.writeString(username)
		parcel.writeString(photo)
		parcel.writeString(phone)
		parcel.writeString(address)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	
}
