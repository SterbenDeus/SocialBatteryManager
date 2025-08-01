package com.example.socialbatterymanager.features.people.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.R

class ContactsImporter(private val context: Context) {
    
    fun importContacts(): List<Person> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }

        val contacts = mutableListOf<Person>()
        
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use { cursor ->
            val contactIdColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            val processedContacts = mutableSetOf<String>()
            
            while (cursor.moveToNext()) {
                val contactId = cursor.getString(contactIdColumn)
                val name = cursor.getString(nameColumn)
                val phone = cursor.getString(phoneColumn)
                
                if (!processedContacts.contains(contactId) && !name.isNullOrEmpty()) {
                    processedContacts.add(contactId)
                    
                    val email = getContactEmail(contactId)
                    
                    contacts.add(
                        Person(
                            name = name,
                            email = email,
                            phone = phone,
                            notes = context.getString(R.string.imported_from_contacts)
                        )
                    )
                }
            }
        }
        
        return contacts
    }
    
    private fun getContactEmail(contactId: String): String? {
        val emailCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )
        
        emailCursor?.use { cursor ->
            val emailColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            if (cursor.moveToFirst()) {
                return cursor.getString(emailColumn)
            }
        }
        
        return null
    }
}