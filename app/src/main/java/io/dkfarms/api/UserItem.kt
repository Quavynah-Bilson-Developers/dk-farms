package io.dkfarms.api

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 *
 * Base class for all users of the system (Customer,Supplier & Admin)
 */
abstract class UserItem {
    open var id: Long? = null
    open var username: String? = null
    open var photo: String? = null
    open var phone: String? = null
    open var uid: String? = null
    open var email: String? = null

}