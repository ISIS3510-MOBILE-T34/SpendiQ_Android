package com.isis3510.spendiq.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.isis3510.spendiq.R
import com.isis3510.spendiq.viewmodel.AccountViewModel
import com.isis3510.spendiq.viewmodel.OffersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataFetchService : LifecycleService() {

    private val NOTIFICATION_CHANNEL_ID = "data_fetch_channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        Log.d("DataFetchService", "Service started")

        // Crear el canal de notificación (para Android O y versiones superiores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Data Fetch Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Crear la notificación para el servicio en primer plano
        val notification = createNotification()

        // Iniciar el servicio como un servicio en primer plano
        startForeground(NOTIFICATION_ID, notification)

        // Verificar la conectividad antes de ejecutar la operación de fetch
        if (isNetworkAvailable()) {
            // Realizar el fetch de datos en un hilo de fondo
            fetchData()
        } else {
            Log.d("DataFetchService", "No network connection, skipping data fetch")
            stopSelf()  // Detenemos el servicio si no hay conexión
        }
    }

    private fun createNotification(): Notification {
        Log.d("DataFetchService", "Creating notification")
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Fetching Data")
            .setContentText("Cargando Cuentas y Ofertas en segundo plano.")
            .setSmallIcon(R.drawable.notification) // Asegúrate de tener un ícono de notificación
            .build()
    }

    // Verificar la conectividad de red
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            // Aquí llamamos a las funciones para hacer el fetch de las cuentas y ofertas
            try {
                // Llama al ViewModel o Repositorio para obtener los datos
                val accountViewModel = AccountViewModel()
                val offersViewModel = OffersViewModel(application)

                accountViewModel.fetchAccountsFinal()  // Obtiene las cuentas
                offersViewModel.fetchOffersFinal()    // Obtiene las ofertas

                // Aquí puedes agregar más lógica, como guardar los datos o realizar otras tareas
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stopSelf()  // Detenemos el servicio después de completar la tarea
        }
    }
}
