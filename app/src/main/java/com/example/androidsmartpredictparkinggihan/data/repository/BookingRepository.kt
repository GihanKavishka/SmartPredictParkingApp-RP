package com.example.androidsmartpredictparkinggihan.data.repository

import android.content.Context
import com.example.androidsmartpredictparkinggihan.data.local.BookingDao
import com.example.androidsmartpredictparkinggihan.data.local.ParkingSpaceDao
import com.example.androidsmartpredictparkinggihan.data.local.UserDao
import com.example.androidsmartpredictparkinggihan.data.model.BookingEntity
import com.example.androidsmartpredictparkinggihan.data.model.ParkingListingResult
import com.example.androidsmartpredictparkinggihan.data.model.ParkingSearchCriteria
import com.example.androidsmartpredictparkinggihan.data.model.PaymentStatus
import com.example.androidsmartpredictparkinggihan.data.model.UserEntity
import com.example.androidsmartpredictparkinggihan.notifications.BookingNotificationHelper

class BookingRepository(
    private val bookingDao: BookingDao,
    private val parkingSpaceDao: ParkingSpaceDao,
    private val userDao: UserDao,
    private val appContext: Context,
) {
    suspend fun createPaidBooking(
        driver: UserEntity,
        listing: ParkingListingResult,
        criteria: ParkingSearchCriteria,
        bookedHours: Int,
        paymentMethod: String,
    ): Result<BookingEntity> {
        if (bookedHours <= 0) {
            return Result.failure(IllegalArgumentException("Book at least 1 hour"))
        }
        val space = parkingSpaceDao.findById(listing.space.parkingId)
            ?: return Result.failure(IllegalArgumentException("Parking space not found"))
        val total = space.basePriceLkr * bookedHours
        val booking = BookingEntity(
            driverUserId = driver.id,
            driverName = driver.fullName,
            driverEmail = driver.email,
            parkingId = space.parkingId,
            spaceTitle = space.title,
            location = space.location,
            ownerUserId = space.ownerUserId,
            requestedHour = criteria.requestedHour,
            dayOfWeek = criteria.dayOfWeek,
            bookedHours = bookedHours,
            hourlyRateLkr = space.basePriceLkr,
            totalPaidLkr = total,
            paymentMethod = paymentMethod,
            paymentStatus = PaymentStatus.PAID,
            ownerNotificationRead = false,
        )
        val id = bookingDao.insert(booking)
        val saved = booking.copy(id = id)
        notifyOwner(saved)
        return Result.success(saved)
    }

    private suspend fun notifyOwner(booking: BookingEntity) {
        val ownerId = booking.ownerUserId ?: return
        val owner = userDao.findById(ownerId) ?: return
        BookingNotificationHelper.showOwnerBookingNotification(
            context = appContext,
            ownerUserId = owner.id,
            bookingId = booking.id,
            title = "New parking booking",
            message = "${booking.driverName} booked ${booking.parkingId} (${booking.spaceTitle}) · LKR ${booking.totalPaidLkr.format(0)} paid",
        )
    }

    suspend fun getAllBookings(): List<BookingEntity> = bookingDao.getAll()

    suspend fun getDriverBookings(driverId: Long): List<BookingEntity> = bookingDao.getByDriver(driverId)

    suspend fun getOwnerBookings(ownerId: Long): List<BookingEntity> = bookingDao.getByOwner(ownerId)

    suspend fun countUnreadOwnerNotifications(ownerId: Long): Int =
        bookingDao.countUnreadForOwner(ownerId)

    suspend fun markOwnerNotificationsRead(ownerId: Long) {
        bookingDao.markAllReadForOwner(ownerId)
    }

    suspend fun totalBookings(): Int = bookingDao.count()

    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
}
