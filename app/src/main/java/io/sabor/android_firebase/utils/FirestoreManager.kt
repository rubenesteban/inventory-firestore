package io.sabor.android_firebase.utils

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.sabor.android_firebase.model.Note
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    private val auth = AuthManager(context)
    var userId = auth.getCurrentUser()?.uid

    suspend fun addNote(note: Note): String? {
        note.userId = userId.toString()
        firestore.collection("notes").add(note).await()
        Log.d("MascotaFeliz", "Crear cuenta con ${note.id} ")
        return note.id
    }

    fun updateNote1(noteId: String) {
            firestore.collection("notes").document(noteId).collection("link")
                .document(noteId)

    }



    suspend fun updateNote(note: Note): String? {
        val noteRef = note.userId?.let { firestore.collection("likes")
            .document(note.userId!!) }
        noteRef?.set(note)?.await()
        return note.id
    }


    suspend fun updatelike(note: Note): String? {

        return note.userId
    }

    suspend fun updateNote1(note: Note) {
        val noteRef = note.id?.let { firestore.collection("notes").document(it) }
        noteRef?.set(note)?.await()
    }


    suspend fun deleteNote(noteId: String) {
        val noteRef = firestore.collection("notes").document(noteId)
        noteRef.delete().await()
    }

    fun getNotesFlow(): Flow<List<Note>> = callbackFlow {
        val notesRef = firestore.collection("notes")
            .whereEqualTo("userId", userId).orderBy("title")

        val subscription = notesRef.addSnapshotListener { snapshot, _ ->
            snapshot?.let { querySnapshot ->
                val notes = mutableListOf<Note>()
                for (document in querySnapshot.documents) {
                    val note = document.toObject(Note::class.java)
                    note?.id = document.id
                    note?.let { notes.add(it) }
                }
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }



    fun datosbd(userID:String): Flow<List<String>> = callbackFlow{
        val db = Firebase.firestore
            .collection("likes")
            .document(userID)
            .collection("live")


        db.get().addOnSuccessListener { result ->
            val notes = mutableListOf<String>()
            for (document in result) {

                val note = document.toObject(Note::class.java)

                note.let {notes.add(it.toString())}
                //  reduceQuantityChenking1(notes )

                Log.d("MascotaFeliz", "Loqueando con en dataosbd ")

            }


        }
            .addOnFailureListener { exception ->
                Log.d("MascotaFeliz", "Error getting documents: ", exception)
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }



    }
}