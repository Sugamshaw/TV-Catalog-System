package Models

import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class FirebaseCustomerSettings {
    private val firestore = FirebaseFirestore.getInstance()

    fun layouttype(callback: (Customer_settings?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            firestore.collection(USER_NODE)
                .document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        callback(null)
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        val user = document.toObject<User>()
                        user?.let {
                            firestore.collection(CUSTOMER_SETTINGS)
                                .document(it.storename.toString())
                                .addSnapshotListener { it2, error2 ->
                                    if (error2 != null) {
                                        callback(null)
                                        return@addSnapshotListener
                                    }

                                    if (it2 != null && it2.exists()) {
                                        val customerSettings = it2.toObject<Customer_settings>()
                                        callback(customerSettings) // This will trigger the callback whenever the layoutoption changes
                                    } else {
                                        callback(null)
                                    }
                                }
                        }
                    } else {
                        callback(null)
                    }
                }
        } ?: callback(null)
    }

}
