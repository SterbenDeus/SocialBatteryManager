package com.example.socialbatterymanager.features.people.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.features.people.data.ContactsImporter
import com.example.socialbatterymanager.features.people.data.PeopleRepository
import com.example.socialbatterymanager.features.people.data.PersonWithStats
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class PeopleFragment : Fragment() {
    
    private lateinit var rvPeople: RecyclerView
    private lateinit var adapter: PersonAdapter
    private lateinit var btnAddPerson: Button
    private lateinit var btnImportContacts: MaterialButton
    private lateinit var btnSortEnergyDrain: MaterialButton
    private lateinit var btnSortInteractions: MaterialButton
    private lateinit var btnSortName: MaterialButton
    private lateinit var tvTotalPeople: TextView
    private lateinit var tvAvgDrain: TextView
    private lateinit var tvThisWeek: TextView
    private lateinit var tvContactsCount: TextView
    
    private lateinit var viewModel: PeopleViewModel
    
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
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        return view
    }

    private fun initViews(view: View) {
        rvPeople = view.findViewById(R.id.rvPeople)
        btnAddPerson = view.findViewById(R.id.btnAddPerson)
        btnImportContacts = view.findViewById(R.id.btnImportContacts)
        btnSortEnergyDrain = view.findViewById(R.id.btnSortEnergyDrain)
        btnSortInteractions = view.findViewById(R.id.btnSortInteractions)
        btnSortName = view.findViewById(R.id.btnSortName)
        tvTotalPeople = view.findViewById(R.id.tvTotalPeople)
        tvAvgDrain = view.findViewById(R.id.tvAvgDrain)
        tvThisWeek = view.findViewById(R.id.tvThisWeek)
        tvContactsCount = view.findViewById(R.id.tvContactsCount)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = PeopleRepository(database)
        val factory = PeopleViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PeopleViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = PersonAdapter(
            onItemClick = { personWithStats -> showPersonDetails(personWithStats) },
            onMoreClick = { personWithStats -> showPersonOptions(personWithStats) }
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

        btnSortEnergyDrain.setOnClickListener {
            setSortOption(SortOption.ENERGY_DRAIN)
        }

        btnSortInteractions.setOnClickListener {
            setSortOption(SortOption.INTERACTIONS)
        }

        btnSortName.setOnClickListener {
            setSortOption(SortOption.NAME)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.people.collect { people ->
                        adapter.submitList(people)
                        updateContactsCount(people.size)
                    }
                }
                launch {
                    viewModel.weeklyStats.collect { stats ->
                        stats?.let {
                            tvTotalPeople.text = it.totalPeople.toString()
                            tvAvgDrain.text = getString(R.string.hours_format, it.avgDrain)
                            tvThisWeek.text = it.thisWeekInteractions.toString()
                        }
                    }
                }
                launch {
                    viewModel.sortOption.collect { sortOption ->
                        updateSortButtons(sortOption)
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun setSortOption(option: SortOption) {
        viewModel.setSortOption(option)
    }

    private fun updateSortButtons(currentSort: SortOption) {
        // Reset all buttons
        resetSortButton(btnSortEnergyDrain)
        resetSortButton(btnSortInteractions)
        resetSortButton(btnSortName)

        // Highlight selected button
        when (currentSort) {
            SortOption.ENERGY_DRAIN -> highlightSortButton(btnSortEnergyDrain)
            SortOption.INTERACTIONS -> highlightSortButton(btnSortInteractions)
            SortOption.NAME -> highlightSortButton(btnSortName)
        }
    }

    private fun resetSortButton(button: MaterialButton) {
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.jscc_charcoal))
        button.strokeWidth = 0
    }

    private fun highlightSortButton(button: MaterialButton) {
        button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jscc_sage_green)
    }

    private fun updateContactsCount(count: Int) {
        tvContactsCount.text = resources.getQuantityString(R.plurals.contacts_count, count, count)
    }

    private fun showAddPersonDialog() {
        EditPersonDialog(
            fragment = this,
            person = null,
            onPersonSaved = { person ->
                Toast.makeText(requireContext(), getString(R.string.person_added_success), Toast.LENGTH_SHORT).show()
            }
        ).show()
    }

    private fun showPersonDetails(personWithStats: PersonWithStats) {
        // Show person details - for now, just edit
        EditPersonDialog(
            fragment = this,
            person = personWithStats.person,
            onPersonSaved = { updatedPerson ->
                Toast.makeText(requireContext(), getString(R.string.person_updated_success), Toast.LENGTH_SHORT).show()
            }
        ).show()
    }

    private fun showPersonOptions(personWithStats: PersonWithStats) {
        // Show options menu (edit, delete, etc.)
        val options = arrayOf(
            getString(R.string.edit),
            getString(R.string.delete),
            getString(R.string.view_stats)
        )

        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.options_for, personWithStats.person.name))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPersonDetails(personWithStats)
                    1 -> deletePerson(personWithStats.person)
                    2 -> showPersonStats(personWithStats.person)
                }
            }
            .show()
    }

    private fun deletePerson(person: Person) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_person_title)
            .setMessage(getString(R.string.delete_person_message, person.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePerson(person)
                Toast.makeText(requireContext(), getString(R.string.person_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
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
                        getString(
                            R.string.imported_contacts,
                            newContacts.size,
                            contacts.size - newContacts.size
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.refreshData()
                }
            } catch (e: SecurityException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.contacts_permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_importing_contacts, e.message),
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
                Toast.makeText(requireContext(), getString(R.string.contacts_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }
}