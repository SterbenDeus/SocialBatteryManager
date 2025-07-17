package com.example.socialbatterymanager.people

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.AppDatabase
import com.example.socialbatterymanager.model.Person
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

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
            // TODO: Load avatar image
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
            .setTitle(if (person == null) "Add Person" else "Edit Person")
            .setView(view)
            .create()
            
        dialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Note: This would need to be handled by the fragment using ActivityResultLauncher
        // For now, just show a toast
        Toast.makeText(context, "Image picker not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun savePerson() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }

        val email = etEmail.text.toString().trim().ifEmpty { null }
        val phone = etPhone.text.toString().trim().ifEmpty { null }
        val notes = etNotes.text.toString().trim().ifEmpty { null }

        val personToSave = if (person == null) {
            Person(
                name = name,
                email = email,
                phone = phone,
                notes = notes
            )
        } else {
            person.copy(
                name = name,
                email = email,
                phone = phone,
                notes = notes
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