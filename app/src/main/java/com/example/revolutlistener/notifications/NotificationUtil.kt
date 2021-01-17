package com.example.revolutlistener.notifications

import android.content.Context
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.revolutlistener.R
import com.example.revolutlistener.domain.Amount
import kotlin.random.Random.Default.nextInt

private const val PAID = "Paid"
private const val CHANNEL_ID = "52"

val amountRegex = "[Pp]aid [€$]\\d+(.\\d{2})?".toRegex()

fun isMoneySpentNotification(sbn: StatusBarNotification): Boolean {

    // TODO analyse text generated by revolut and google pay to create regex expression
    if (sbn.packageName == "com.example.revolutlistener") {
        return false
    }
    val notificationText = sbn.notification.extras["android.text"].toString()
    return isMoneySpentNotification(notificationText)
}

// Wrapper so I can test this specifically
fun isMoneySpentNotification(notificationText: String) = amountRegex.find(notificationText) != null


fun parseMonetaryAmount(sbn: StatusBarNotification): Amount {
    return parseMonetaryAmount(sbn.notification.extras["android.text"].toString())
}
fun parseMonetaryAmount(notificationText: String): Amount {

    // Notification is in the form '/emoji paid €xx.yy <- need to check this
    // Allowed to do this as isMoneySpendNotification guarantees a match
    val matchResult = amountRegex.find(notificationText)!!
    // Escape currency symbol
    val amountString = matchResult.value.substring(5)
    // Parse resulting string as Amount object
    return Amount.parseString(amountString)
}

fun createUpdatedBudgetNotification(context: Context, spentToday: Amount, leftToSpend: Amount) {
    val text = "You've spent $spentToday in total today.\nYou have $leftToSpend left to spend today"
    postNotification(context, "Ledger.io",text)
}

private fun postNotification(context: Context, title: String, text: String) {
    var builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(text)
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(52, builder.build())
    }

}
