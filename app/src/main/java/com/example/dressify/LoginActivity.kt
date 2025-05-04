package com.example.dressify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.dressify.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 101
    private val db = FirebaseFirestore.getInstance()  // Firebase Firestore instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Sign out the current user (Firebase and Google)
        signOut()

        // Initialize Google Sign-In options
        Log.d("LoginActivity", "Initializing Google Sign-In...")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // From google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle Google Sign-In button click
        binding.btnGoogleSignIn.setOnClickListener {
            Log.d("LoginActivity", "Google Sign-In button clicked")
            signInWithGoogle()
        }
    }

    private fun signOut() {
        // Sign out of Firebase
        FirebaseAuth.getInstance().signOut()

        // Sign out of Google
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            .addOnCompleteListener(this) {
                Log.d("LoginActivity", "Signed out successfully from Google.")
            }
    }

    private fun signInWithGoogle() {
        // Log intent creation
        Log.d("LoginActivity", "Starting Google Sign-In Intent...")
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN) // Start the sign-in intent for account selection
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            Log.d("LoginActivity", "onActivityResult called")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isSuccessful) {
                val account: GoogleSignInAccount? = task.result
                Log.d("LoginActivity", "Google Sign-In successful: ${account?.email}")
                firebaseAuthWithGoogle(account)
            } else {
                Log.e("LoginActivity", "Google Sign-In Failed: ${task.exception}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            Log.d("LoginActivity", "Attempting Firebase Authentication with Google account: ${account.email}")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign-in success, now check if the user is in Firestore
                        Log.d("LoginActivity", "Firebase Authentication successful")
                        checkIfUserExists(account)
                    } else {
                        Log.e("LoginActivity", "Firebase Authentication failed: ${task.exception}")
                    }
                }
        } else {
            Log.e("LoginActivity", "Google account is null during authentication")
        }
    }

    private fun checkIfUserExists(account: GoogleSignInAccount) {
        val email = account.email ?: return
        val name = account.displayName ?: "No Name"

        val usersCollection = db.collection("Dressify_users")

        usersCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // User doesn't exist — create new document
                    createUserDocument(email, name)
                } else {
                    // User exists, get their document ID
                    val documentId = querySnapshot.documents.first().id
                    Log.d("LoginActivity", "User already exists with ID: $documentId")

                    // Pass documentId to MainActivity
                    navigateToMainActivity(documentId)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Error checking user existence: ${exception.message}")
            }
    }


    private fun createUserDocument(email: String, name: String) {
        val usersCollection = db.collection("Dressify_users")

        // Generate a unique ID based on the current time
        val uniqueId = System.currentTimeMillis().toString()

        val nameDetails = hashMapOf(
            "id" to uniqueId,
            "name" to name,
            "age" to "",
            "gender" to "",
            "emoji" to "",
            "skinColour" to "",
            "skinType" to "",
            "height" to "",
        )

        val newUser = hashMapOf(
            "email" to email,
            "names" to listOf(nameDetails)
        )

        usersCollection.add(newUser)
            .addOnSuccessListener { documentReference ->
                Log.d("LoginActivity", "New user document created with ID: ${documentReference.id}")

                // Pass new documentId to MainActivity
                navigateToMainActivity(documentReference.id)
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Error creating user document: ${exception.message}")
            }
    }





    private fun navigateToMainActivity(documentId: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("documentId", documentId)
        startActivity(intent)
        finish()
    }


}
