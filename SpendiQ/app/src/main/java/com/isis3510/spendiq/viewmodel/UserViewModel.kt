import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _userData = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val userData: StateFlow<Map<String, Any?>> = _userData

    init {
        loadUserDataFromFirebase()
    }

    private fun loadUserDataFromFirebase() {
        // Obtener el usuario actual
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            viewModelScope.launch {
                val firestore = FirebaseFirestore.getInstance()
                val userDocument = firestore.collection("users").document(userId)

                userDocument.get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Extraer solo los campos necesarios
                            _userData.value = mapOf(
                                "fullName" to document.getString("fullName"),
                                "email" to document.getString("email"),
                                "phoneNumber" to document.getString("phoneNumber"),
                                "birthDate" to document.getString("birthDate")
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Manejar el error
                        println("Error al obtener datos del usuario: $exception")
                    }
            }
        } else {
            // Manejar el caso en que no haya un usuario autenticado
            println("No hay un usuario autenticado.")
        }
    }
}
