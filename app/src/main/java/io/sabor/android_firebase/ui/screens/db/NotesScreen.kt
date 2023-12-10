
package io.sabor.android_firebase.ui.screens.db

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sabor.android_firebase.model.Note
import io.sabor.android_firebase.utils.CloudStorageManager
import io.sabor.android_firebase.utils.FirestoreManager
import io.sabor.android_firebase.utils.StoreUserEmail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotesScreen(firestore: FirestoreManager, storage: CloudStorageManager) {
    var showAddNoteDialog by remember { mutableStateOf(false) }


    val notes by firestore.getNotesFlow().collectAsState(emptyList())

    var costo: String by remember { mutableStateOf("") }
    var splash: Boolean by remember { mutableStateOf(true) }



    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            if(splash){FloatingActionButton(
                onClick = {
                    showAddNoteDialog = true
                },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
                if (showAddNoteDialog) {
                    AddNoteDialog(
                        onNoteAdded = { note ->
                            scope.launch {
                                firestore.addNote(note)
                                //costo = firestore.updateNote(note).toString()
                            }
                            showAddNoteDialog = false
                        },

                        onDialogDismissed = { showAddNoteDialog = false }, storage = storage, firestore = firestore
                    )
                }}

        }
    ) {
        if(!notes.isNullOrEmpty()) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp)
            ) {
                notes.forEach {
                    item {
                        NoteItem(note = it, firestore = firestore)
                    }
                }
                splash = false
            }
        } else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No se encontraron \nRecetas",
                    fontSize = 18.sp, fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, firestore: FirestoreManager) {
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var image by remember { mutableStateOf("") }
    //image = note.id.toString()

    val onDeleteNoteConfirmed: () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            firestore.deleteNote(note.id ?: "")
        }
    }

    if (showDeleteNoteDialog) {
        DeleteNoteDialog(
            onConfirmDelete = {
                onDeleteNoteConfirmed()
                showDeleteNoteDialog = false
            },
            onDismiss = {
                showDeleteNoteDialog = false
            }
        )
    }



    val context = LocalContext.current
    // scope
    val scope = rememberCoroutineScope()
    // datastore Email
    val dataStore = StoreUserEmail(context)
    // get saved email
    val savedEmail = dataStore.getEmail.collectAsState(initial = "")

    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        image = firestore.updatelike(note).toString()

        firestore.updateNote(note)
        scope.launch {
            dataStore.saveEmail(image)
        }
        //firestore.updateNote1(note)
    }


    Card(
        modifier = Modifier
            .padding(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 0.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(text = note.title.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = note.preparedness,
                fontWeight = FontWeight.Thin,
                fontSize = 13.sp,
                lineHeight = 15.sp)

            IconButton(
                onClick = { showDeleteNoteDialog = true },
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
            }
        }
    }

  //  AddNote(note.id.toString(), firestore =  firestore)

}


@Composable
fun AddNoteDialog(onNoteAdded: (Note) -> Unit, onDialogDismissed: () -> Unit, storage: CloudStorageManager, firestore: FirestoreManager) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var preparedness by remember { mutableStateOf("") }
    var ingredient by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var junk by remember { mutableStateOf("") }
    var ju by remember { mutableStateOf(0) }
    var moda by remember { mutableStateOf(0) }
    var uri by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf(0.0) }

    val context = LocalContext.current
    // scope
    val scope = rememberCoroutineScope()
    // datastore Email
    val dataStore = StoreUserEmail(context)
    // get saved email
    val savedEmail = dataStore.getEmail.collectAsState(initial = "")

    var gallery by remember { mutableStateOf<List<String>>(listOf()) }

    userId = savedEmail.value!!

    fun promedio(): Int {
        ju = content.toInt()*junk.toInt()
        return ju
    }

    fun price(s:Double):String{
        return s.toString()
    }


    LaunchedEffect(Unit) {
        gallery = storage.getUserImages()
        if(gallery.size==1){
            image = gallery[0]
        }

    }





    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Agregar Receta") },
        confirmButton = {
            Button(
                onClick = {
                    moda = promedio()

                    val newNote = Note(
                        title = title,
                        content = content,
                        preparedness = preparedness,
                        ingredient = ingredient,
                        url = image,
                        notes = junk,
                        moda = moda,
                        uri = uri

                        )
                    onNoteAdded(newNote)
                    title = ""
                    content = ""
                    preparedness = ""
                    ingredient = ""
                    image = ""
                    junk = ""
                    moda = 0
                    uri = ""



                }
            ) {
                Text(text = "Agregar")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDialogDismissed()
                }
            ) {
                Text(text = "Cancelar")
            }
        },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    label = { Text(text = "Título") }
                )
                TextField(
                    value = uri,
                    onValueChange = { uri = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    label = { Text(text = "Categoria") }
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    value = preparedness,
                    onValueChange = { preparedness = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    maxLines = 4,
                    label = { Text(text = "Preparacion") }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    value = ingredient,
                    onValueChange = { ingredient = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    maxLines = 4,
                    label = { Text(text = "Ingredientes") }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    value = content,
                    onValueChange = { content = it},
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    maxLines = 4,
                    label = { Text(text = "Precio") }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    value = junk,
                    onValueChange = { junk = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    maxLines = 4,
                    label = { Text(text = "Stock") }
                )
            }
        }
    )
}

@Composable
fun DeleteNoteDialog(onConfirmDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Receta") },
        text = { Text("¿Estás seguro que deseas eliminar la receta?") },
        confirmButton = {
            Button(
                onClick = onConfirmDelete
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

