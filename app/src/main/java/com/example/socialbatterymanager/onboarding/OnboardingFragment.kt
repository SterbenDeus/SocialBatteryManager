package com.example.socialbatterymanager.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.databinding.FragmentOnboardingBinding
import com.example.socialbatterymanager.preferences.PreferencesManager
import com.example.socialbatterymanager.shared.utils.BiometricAuthHelper
import com.example.socialbatterymanager.shared.utils.ErrorHandler
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class OnboardingFragment : Fragment() {
    
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var biometricAuthHelper: BiometricAuthHelper
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        biometricAuthHelper = BiometricAuthHelper(this)
        
        setupViewPager()
        setupClickListeners()
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationButtons(position)
            }
        })
    }
    
    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            try {
                val currentItem = binding.viewPager.currentItem
                if (currentItem < 2) {
                    binding.viewPager.currentItem = currentItem + 1
                } else {
                    // Complete onboarding
                    completeOnboarding()
                }
            } catch (e: Exception) {
                ErrorHandler.handleException(binding.root, e)
            }
        }
        
        binding.btnSkip.setOnClickListener {
            try {
                completeOnboarding()
            } catch (e: Exception) {
                ErrorHandler.handleException(binding.root, e)
            }
        }
    }
    
    private fun completeOnboarding() {
        // Check if biometric authentication is available and offer to set it up
        if (biometricAuthHelper.shouldShowBiometricPrompt()) {
            showBiometricSetupDialog()
        } else {
            finalizeOnboarding()
        }
    }
    
    private fun showBiometricSetupDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.biometric_title))
        builder.setMessage("Would you like to enable biometric authentication for secure access to your social battery data?")
        builder.setPositiveButton("Yes") { _, _ ->
            setupBiometricAuthentication()
        }
        builder.setNegativeButton("Skip") { _, _ ->
            finalizeOnboarding()
        }
        builder.setCancelable(false)
        builder.show()
    }
    
    private fun setupBiometricAuthentication() {
        biometricAuthHelper.showBiometricPrompt(
            onSuccess = {
                lifecycleScope.launch {
                    preferencesManager.setBiometricEnabled(true)
                    finalizeOnboarding()
                }
            },
            onError = { errorMessage ->
                ErrorHandler.showErrorSnackbar(
                    binding.root,
                    errorMessage,
                    getString(R.string.retry)
                ) {
                    setupBiometricAuthentication()
                }
            },
            onFailed = {
                ErrorHandler.showErrorSnackbar(
                    binding.root,
                    getString(R.string.biometric_error),
                    getString(R.string.retry)
                ) {
                    setupBiometricAuthentication()
                }
            }
        )
    }
    
    private fun finalizeOnboarding() {
        lifecycleScope.launch {
            preferencesManager.setOnboardingCompleted(true)
            findNavController().navigate(R.id.action_onboarding_to_home)
        }
    }
    
    private fun updateNavigationButtons(position: Int) {
        when (position) {
            0 -> {
                binding.btnNext.text = getString(R.string.next)
                binding.btnSkip.visibility = View.VISIBLE
            }
            1 -> {
                binding.btnNext.text = getString(R.string.next)
                binding.btnSkip.visibility = View.VISIBLE
            }
            2 -> {
                binding.btnNext.text = getString(R.string.get_started)
                binding.btnSkip.visibility = View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class OnboardingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return OnboardingPageFragment.newInstance(position)
    }
}