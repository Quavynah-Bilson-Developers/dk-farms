package io.dkfarms.api

/**
 * Project : dk-farms
 * Package name : io.dkfarms.api
 */
enum class UserType(val label: String) {

    /**
     * Customer data type
     */
    TYPE_CUSTOMER("customer"),
    /**
     * Supplier data type
     */
    TYPE_SUPPLIER("supplier"),
    /**
     * Admin data type
     */
    TYPE_ADMIN("admin");
}