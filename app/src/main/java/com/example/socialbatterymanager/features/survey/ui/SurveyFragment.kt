package com.example.socialbatterymanager.features.survey.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.socialbatterymanager.R

class SurveyFragment : Fragment() {

    private lateinit var energyGroup: RadioGroup
    private lateinit var socialGroup: RadioGroup
    private lateinit var stressGroup: RadioGroup
    private lateinit var submitButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_survey, container, false)

        energyGroup = view.findViewById(R.id.energyGroup)
        socialGroup = view.findViewById(R.id.socialGroup)
        stressGroup = view.findViewById(R.id.stressGroup)
        submitButton = view.findViewById(R.id.submitSurveyButton)

        submitButton.setOnClickListener {
            val energySelected = energyGroup.checkedRadioButtonId
            val socialSelected = socialGroup.checkedRadioButtonId
            val stressSelected = stressGroup.checkedRadioButtonId

            if (energySelected == -1 || socialSelected == -1 || stressSelected == -1) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.survey_incomplete),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.survey_submitted),
                    Toast.LENGTH_SHORT,
                ).show()
                findNavController().navigateUp()
            }
        }

        return view
    }
}
