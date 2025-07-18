package com.example.socialbatterymanager.people

import android.content.Context
import android.provider.ContactsContract
import com.example.socialbatterymanager.model.Person

class ContactsImporter(private val context: Context) {
    
    fun importContacts(): List<Person> {
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
                            notes = "Imported from contacts"
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