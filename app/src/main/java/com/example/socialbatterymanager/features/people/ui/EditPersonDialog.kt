package com.example.socialbatterymanager.features.people.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.Person
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditPersonDialog(
    private val fragment: Fragment,
    private val person: Person? = null,
    private val onPersonSaved: (Person) -> Unit
) {
    
    private val context: Context get() = fragment.requireContext()
    private lateinit var dialog: AlertDialog
    private lateinit var ivAvatar: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private var selectedImageUri: Uri? = null
    
    private val imagePickerLauncher: ActivityResultLauncher<Intent> = 
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    ivAvatar.setImageURI(uri)
                }
            }
        }

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_person, null)
        
        ivAvatar = view.findViewById(R.id.ivAvatar)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        etNotes = view.findViewById(R.id.etNotes)
        
        val btnChangeAvatar = view.findViewById<Button>(R.id.btnChangeAvatar)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // Pre-populate fields if editing existing person
        person?.let {
            etName.setText(it.name)
            etEmail.setText(it.email)
            etPhone.setText(it.phone)
            etNotes.setText(it.notes)
            
            // TODO: Load avatar image if available
            if (!it.avatarPath.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(it.avatarPath)
                    ivAvatar.setImageURI(uri)
                    selectedImageUri = uri
                } catch (e: Exception) {
                    // If avatar can't be loaded, keep default
                }
            }
        }

        btnChangeAvatar.setOnClickListener {
            openImagePicker()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            savePerson()
        }

        dialog = AlertDialog.Builder(context)
            .setTitle(
                if (person == null) {
                    context.getString(R.string.add_person)
                } else {
                    context.getString(R.string.edit_person)
                }
            )
            .setView(view)
            .create()
            
        dialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun savePerson() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            etName.error = context.getString(R.string.error_name_required)
            return
        }

        val email = etEmail.text.toString().trim().ifEmpty { null }
        val phone = etPhone.text.toString().trim().ifEmpty { null }
        val notes = etNotes.text.toString().trim().ifEmpty { null }
        val avatarPath = selectedImageUri?.toString()

        val personToSave = if (person == null) {
            Person(
                name = name,
                email = email,
                phone = phone,
                notes = notes,
                avatarPath = avatarPath
            )
        } else {
            person.copy(
                name = name,
                email = email,
                phone = phone,
                notes = notes,
                avatarPath = avatarPath
            )
        }

        fragment.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(context)
            if (person == null) {
                db.personDao().insertPerson(personToSave)
            } else {
                db.personDao().updatePerson(personToSave)
            }
            
            fragment.activity?.runOnUiThread {
                onPersonSaved(personToSave)
                dialog.dismiss()
            }
        }
    }
}