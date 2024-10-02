package com.isis3510.spendiq.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val notification = sbn.notification
            val extras = notification.extras

            // Log the raw notification details
            Log.d("NotificationListener", "Notification received from: ${sbn.packageName}")

            // Extract the title and the text from the notification
            val title = extras.getString("android.title", "No title")
            val text = extras.getCharSequence("android.text", "No text").toString()

            // Log the raw notification title and text
            Log.d("NotificationListener", "Notification Title: $title")
            Log.d("NotificationListener", "Notification Text: $text")

            // Adjusted Regex to capture the amount and company
            val regex = Regex("Nequi: Pagaste \\$([\\d,.]+) en ([\\w\\s]+)", RegexOption.IGNORE_CASE)
            val matchResult = regex.find(text)

            matchResult?.let {
                val amount = matchResult.groupValues[1]
                val company = matchResult.groupValues[2]

                // Log the extracted amount and company for debugging
                Log.d("NotificationListener", "Transaction Amount: $amount, Company: $company")
            } ?: run {
                // Log if no match is found
                Log.d("NotificationListener", "No match found in notification text")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationListener", "Notification removed: ${sbn?.packageName}")
    }
}
