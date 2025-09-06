package com.example.socialbatterymanager.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.databinding.FragmentOnboardingPageBinding

class OnboardingPageFragment : Fragment() {
    
    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!
    
    private var pagePosition: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagePosition = arguments?.getInt(ARG_POSITION) ?: 0
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupContent()
    }
    
    private fun setupContent() {
        when (pagePosition) {
            0 -> {
                binding.imageOnboarding.setImageResource(R.drawable.ic_onboarding_welcome)
                binding.titleOnboarding.text = getString(R.string.onboarding_title_1)
                binding.descriptionOnboarding.text = getString(R.string.onboarding_description_1)
            }
            1 -> {
                binding.imageOnboarding.setImageResource(R.drawable.ic_onboarding_battery)
                binding.titleOnboarding.text = getString(R.string.onboarding_title_2)
                binding.descriptionOnboarding.text = getString(R.string.onboarding_description_2)
            }
            2 -> {
                binding.imageOnboarding.setImageResource(R.drawable.ic_onboarding_analytics)
                binding.titleOnboarding.text = getString(R.string.onboarding_title_3)
                binding.descriptionOnboarding.text = getString(R.string.onboarding_description_3)
            }
        }
        
        // Accessibility support
        binding.imageOnboarding.contentDescription = binding.titleOnboarding.text
        binding.titleOnboarding.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        binding.descriptionOnboarding.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val ARG_POSITION = "position"
        
        fun newInstance(position: Int): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }
}
