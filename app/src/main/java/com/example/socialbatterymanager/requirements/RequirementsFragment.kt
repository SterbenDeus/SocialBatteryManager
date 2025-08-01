package com.example.socialbatterymanager.requirements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.requirements.RequirementsWebInterface
import com.example.requirements.ComplexityLevel
import kotlinx.coroutines.launch
import java.io.File


/**
 * Fragment for requirements upload and code generation
 * This integrates the requirements processor into the main app
 */
class RequirementsFragment : Fragment() {
    
    private lateinit var requirementsInput: EditText
    private lateinit var generateButton: Button
    private lateinit var assessButton: Button
    private lateinit var resultText: TextView
    private lateinit var complexityText: TextView
    
    private val webInterface = RequirementsWebInterface()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_requirements, container, false)
        
        initializeViews(view)
        setupClickListeners()
        
        return view
    }
    
    private fun initializeViews(view: View) {
        requirementsInput = view.findViewById(R.id.requirementsInput)
        generateButton = view.findViewById(R.id.generateButton)
        assessButton = view.findViewById(R.id.assessButton)
        resultText = view.findViewById(R.id.resultText)
        complexityText = view.findViewById(R.id.complexityText)
        
        // Load a sample requirement
        loadSampleRequirement()
    }
    
    private fun setupClickListeners() {
        generateButton.setOnClickListener {
            generateCode()
        }
        
        assessButton.setOnClickListener {
            assessComplexity()
        }
    }
    
    private fun loadSampleRequirement() {
        val sampleRequirement = """
            {
                "projectName": "SocialBatteryManager",
                "description": "Enhanced social battery management with code generation",
                "features": [
                    {
                        "name": "BatteryTracking",
                        "description": "Track social battery levels throughout the day",
                        "type": "SERVICE"
                    },
                    {
                        "name": "SocialAnalytics",
                        "description": "Analytics dashboard for social interactions",
                        "type": "COMPONENT"
                    },
                    {
                        "name": "RequirementsProcessor",
                        "description": "Process and generate code from requirements",
                        "type": "SERVICE"
                    },
                    {
                        "name": "CodeViewer",
                        "description": "View and export generated code",
                        "type": "COMPONENT"
                    }
                ]
            }
        """.trimIndent()
        
        requirementsInput.setText(sampleRequirement)
    }
    
    private fun generateCode() {
        val requirements = requirementsInput.text.toString()
        
        if (requirements.isBlank()) {
            Toast.makeText(context, "Please enter requirements", Toast.LENGTH_SHORT).show()
            return
        }
        
        generateButton.isEnabled = false
        generateButton.text = "Generating..."
        
        lifecycleScope.launch {
            try {
                val result = webInterface.processWebRequirements(
                    requirements, 
                    "requirements.json"
                )
                
                if (result.success) {
                    displayResults(result)
                } else {
                    resultText.text = "Error: ${result.message}"
                }
                
            } catch (e: Exception) {
                resultText.text = "Error: ${e.message}"
            } finally {
                generateButton.isEnabled = true
                generateButton.text = "Generate Code"
            }
        }
    }
    
    private fun assessComplexity() {
        val requirements = requirementsInput.text.toString()
        
        if (requirements.isBlank()) {
            Toast.makeText(context, "Please enter requirements", Toast.LENGTH_SHORT).show()
            return
        }
        
        val assessment = webInterface.assessComplexity(requirements)
        
        val complexityColor = when (assessment.level) {
            ComplexityLevel.SIMPLE -> "#4CAF50"
            ComplexityLevel.MODERATE -> "#FF9800"
            ComplexityLevel.COMPLEX -> "#F44336"
            ComplexityLevel.VERY_COMPLEX -> "#9C27B0"
        }
        
        complexityText.text = """
            Complexity: ${assessment.level}
            Estimated Files: ${assessment.estimatedFiles}
            Estimated Lines: ${assessment.estimatedLines}
            Features: ${assessment.features}
            
            Recommendations:
            ${assessment.recommendations.joinToString("\n• ", "• ")}
        """.trimIndent()
    }
    
    private fun displayResults(result: com.example.requirements.WebProcessingResult) {
        val output = StringBuilder()
        
        output.append("✓ Project: ${result.projectName}\n")
        output.append("✓ Summary: ${result.summary}\n")
        output.append("✓ Files Generated: ${result.files.size}\n\n")
        
        output.append("Generated Files:\n")
        result.files.forEach { file ->
            output.append("• ${file.path} (${file.type})\n")
        }
        
        // Show first few files content
        output.append("\nSample Generated Code:\n")
        result.files.take(2).forEach { file ->
            output.append("\n--- ${file.path} ---\n")
            output.append(file.content.take(300))
            if (file.content.length > 300) {
                output.append("...\n")
            }
            output.append("\n")
        }
        
        resultText.text = output.toString()
        
        // Show success message
        Toast.makeText(context, "Code generated successfully!", Toast.LENGTH_LONG).show()
    }
    
    companion object {
        fun newInstance(): RequirementsFragment {
            return RequirementsFragment()
        }
    }
}