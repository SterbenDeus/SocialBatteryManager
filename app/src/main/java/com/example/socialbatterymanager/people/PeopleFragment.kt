package com.example.socialbatterymanager.people

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.AppDatabase
import com.example.socialbatterymanager.model.Person
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class PeopleFragment : Fragment() {
    
    private lateinit var rvPeople: RecyclerView
    private lateinit var adapter: PersonAdapter
    private lateinit var btnAddPerson: Button
    private lateinit var btnImportContacts: Button
    
    companion object {
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_people, container, false)
        
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadPeople()
        
        return view
    }

    private fun initViews(view: View) {
        rvPeople = view.findViewById(R.id.rvPeople)
        btnAddPerson = view.findViewById(R.id.btnAddPerson)
        btnImportContacts = view.findViewById(R.id.btnImportContacts)
    }

    private fun setupRecyclerView() {
        adapter = PersonAdapter(
            onItemClick = { person -> showPersonDetails(person) },
            onMoreClick = { person -> showPersonOptions(person) }
        )
        rvPeople.layoutManager = LinearLayoutManager(requireContext())
        rvPeople.adapter = adapter
    }

    private fun setupClickListeners() {
        btnAddPerson.setOnClickListener {
            showAddPersonDialog()
        }

        btnImportContacts.setOnClickListener {
            importContacts()
        }
    }

    private fun loadPeople() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.personDao().getAllPeople().collect { people ->
                adapter.submitList(people)
            }
        }
    }

    private fun showAddPersonDialog() {
        EditPersonDialog(
            fragment = this,
            person = null,
            onPersonSaved = { person ->
                Toast.makeText(requireContext(), "Person added successfully", Toast.LENGTH_SHORT).show()
            }
        ).show()
    }

    private fun showPersonDetails(person: Person) {
        // Show person details - for now, just edit
        EditPersonDialog(
            fragment = this,
            person = person,
            onPersonSaved = { updatedPerson ->
                Toast.makeText(requireContext(), "Person updated successfully", Toast.LENGTH_SHORT).show()
            }
        ).show()
    }

    private fun showPersonOptions(person: Person) {
        // Show options menu (edit, delete, etc.)
        val options = arrayOf("Edit", "Delete", "View Stats")
        
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Options for ${person.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPersonDetails(person)
                    1 -> deletePerson(person)
                    2 -> showPersonStats(person)
                }
            }
            .show()
    }

    private fun deletePerson(person: Person) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Person")
            .setMessage("Are you sure you want to delete ${person.name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.personDao().deletePerson(person)
                    Toast.makeText(requireContext(), "Person deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPersonStats(person: Person) {
        PersonStatsDialog(this, person).show()
    }

    private fun importContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_REQUEST_CODE
            )
        } else {
            performContactsImport()
        }
    }

    private fun performContactsImport() {
        lifecycleScope.launch {
            try {
                val importer = ContactsImporter(requireContext())
                val contacts = importer.importContacts()
                
                val db = AppDatabase.getDatabase(requireContext())
                val personDao = db.personDao()
                
                // Get existing people to avoid duplicates
                val existingPeople = personDao.getAllPeople().first()
                val existingNames = existingPeople.map { it.name.lowercase() }.toSet()
                
                val newContacts = contacts.filter { contact ->
                    !existingNames.contains(contact.name.lowercase())
                }
                
                newContacts.forEach { contact ->
                    personDao.insertPerson(contact)
                }
                
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Imported ${newContacts.size} contacts (${contacts.size - newContacts.size} duplicates skipped)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error importing contacts: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performContactsImport()
            } else {
                Toast.makeText(requireContext(), "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}