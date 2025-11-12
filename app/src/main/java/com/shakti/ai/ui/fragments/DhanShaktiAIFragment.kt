package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.shakti.ai.R
import com.shakti.ai.viewmodel.DhanShaktiViewModel
import com.shakti.ai.models.LoanAssessment
import com.shakti.ai.models.RiskProfile
import kotlinx.coroutines.launch

class DhanShaktiAIFragment : Fragment() {

    private val viewModel: DhanShaktiViewModel by activityViewModels()

    // UI Elements
    private lateinit var financialLiteracyScore: TextView
    private lateinit var financialProgress: ProgressBar
    private lateinit var btnContinueLearning: Button
    private lateinit var btnApplyPmjdy: Button
    private lateinit var btnApplyStandup: Button
    private lateinit var btnApplyMudra: Button
    private lateinit var btnApplyMahilaSamman: Button
    private lateinit var btnStartBankSetup: Button
    private lateinit var btnLoanAssessment: Button
    private lateinit var btnInvestmentPlanner: Button
    private lateinit var btnBudgetAnalysis: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dhan_shakti_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()

        // Load government schemes on start
        viewModel.loadAllSchemes()
    }

    private fun initializeViews(view: View) {
        financialLiteracyScore = view.findViewById(R.id.financial_literacy_score)
        financialProgress = view.findViewById(R.id.financial_progress)
        btnContinueLearning = view.findViewById(R.id.btn_continue_learning)
        btnApplyPmjdy = view.findViewById(R.id.btn_apply_pmjdy)
        btnApplyStandup = view.findViewById(R.id.btn_apply_standup)
        btnApplyMudra = view.findViewById(R.id.btn_apply_mudra)
        btnApplyMahilaSamman = view.findViewById(R.id.btn_apply_mahila_samman)
        btnStartBankSetup = view.findViewById(R.id.btn_start_bank_setup)

        // Additional buttons - create them programmatically as they don't exist in layout yet
        btnLoanAssessment = createButton("💰 Check Loan Eligibility")
        btnInvestmentPlanner = createButton("📈 Plan Investment")
        btnBudgetAnalysis = createButton("📊 Budget Analysis")
    }

    private fun createButton(text: String): Button {
        return Button(requireContext()).apply {
            this.text = text
        }
    }

    private fun setupClickListeners() {
        btnContinueLearning.setOnClickListener {
            showLearningModule()
        }

        btnApplyPmjdy.setOnClickListener {
            showSchemeDetails("Pradhan Mantri Jan Dhan Yojana")
        }

        btnApplyStandup.setOnClickListener {
            showSchemeDetails("Stand Up India Scheme")
        }

        btnApplyMudra.setOnClickListener {
            showSchemeDetails("MUDRA Loan")
        }

        btnApplyMahilaSamman.setOnClickListener {
            showSchemeDetails("Mahila Samman Savings")
        }

        btnStartBankSetup.setOnClickListener {
            startBankAccountSetup()
        }

        btnLoanAssessment.setOnClickListener {
            showLoanAssessmentDialog()
        }

        btnInvestmentPlanner.setOnClickListener {
            showInvestmentPlannerDialog()
        }

        btnBudgetAnalysis.setOnClickListener {
            analyzeBudget()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                btnLoanAssessment.isEnabled = !isLoading
                btnInvestmentPlanner.isEnabled = !isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.governmentSchemes.collect { schemes ->
                if (schemes.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        " Loaded ${schemes.size} government schemes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loanAssessment.collect { assessment ->
                assessment?.let {
                    showLoanAssessmentResult(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.investmentPlan.collect { plan ->
                plan?.let {
                    showInvestmentPlanResult(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun showLearningModule() {
        val modules = arrayOf(
            "Financial Literacy Basics",
            "Understanding Bank Accounts",
            "How to Save Money",
            "Understanding Loans & Interest",
            "Investment Fundamentals",
            "Government Schemes for Women",
            "Tax Benefits for Women",
            "Digital Banking Basics"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(" Financial Learning Modules")
            .setItems(modules) { _, which ->
                val module = modules[which]
                // Increase literacy score
                val currentScore = financialProgress.progress
                val newScore = (currentScore + 10).coerceAtMost(100)
                financialProgress.progress = newScore
                financialLiteracyScore.text = "$newScore%"

                Toast.makeText(context, " Completed: $module\nScore +10%", Toast.LENGTH_LONG).show()

                // Show module content
                showModuleContent(module)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showModuleContent(moduleName: String) {
        val content = when {
            moduleName.contains("Bank Accounts") -> """
                Types of Bank Accounts:
                
                1. Savings Account
                   - Earn interest on deposits
                   - Easy withdrawal
                   - Minimum balance required
                
                2. Current Account
                   - For business transactions
                   - No interest
                   - Unlimited transactions
                
                3. Fixed Deposit
                   - Higher interest rates
                   - Lock-in period
                   - Safe investment
                
                Tip: Start with a Jan Dhan Yojana account - Zero balance required!
            """.trimIndent()

            moduleName.contains("Save Money") -> """
                Smart Saving Tips:
                
                1. 50-30-20 Rule
                   - 50% Needs
                   - 30% Wants
                   - 20% Savings
                
                2. Set up Auto-transfer
                   - Automatic savings on salary day
                
                3. Emergency Fund
                   - Save 6 months expenses
                
                4. Cut Unnecessary Expenses
                   - Track your spending
                   - Avoid impulse buying
                
                Start small: Even ₹100/month adds up to ₹1,200/year!
            """.trimIndent()

            else -> """
                This module covers: $moduleName
                
                Content is being prepared by AI...
                Please check back later for detailed lessons!
            """.trimIndent()
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(moduleName)
            .setMessage(content)
            .setPositiveButton("Next Module") { _, _ ->
                showLearningModule()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSchemeDetails(schemeName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val schemes = viewModel.governmentSchemes.value
            val scheme = schemes.find { it.name.contains(schemeName, ignoreCase = true) }

            if (scheme != null) {
                val message = """
                    ${scheme.name}
                    
                    ${scheme.description}
                    
                    Eligibility:
                    ${scheme.eligibility}
                    
                    Loan Amount:
                    ${scheme.loanAmount}
                    
                    Interest Rate:
                    ${scheme.interestRate}
                    
                    Website:
                    ${scheme.website}
                """.trimIndent()

                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Scheme Details")
                    .setMessage(message)
                    .setPositiveButton("Apply Now") { _, _ ->
                        Toast.makeText(
                            context,
                            "Opening application portal...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNeutralButton("Eligibility Check") { _, _ ->
                        showLoanAssessmentDialog()
                    }
                    .setNegativeButton("Close", null)
                    .show()
            } else {
                Toast.makeText(context, "Loading scheme details...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startBankAccountSetup() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(" Open Bank Account Safely")
            .setMessage(
                """
                This 5-step wizard will help you:
                
                Choose the right bank account
                Complete video KYC from home
                Use a safe address (office/friend's place)
                Set up separate mobile number
                Configure e-statements only (no paper)
                Get debit card to safe address
                
                Time needed: 10 minutes
                
                This ensures complete privacy and safety.
            """.trimIndent()
            )
            .setPositiveButton("Start Setup") { _, _ ->
                showBankSelectionWizard()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun showBankSelectionWizard() {
        val banks = arrayOf(
            "Jan Dhan Yojana (Recommended for beginners)",
            "State Bank of India - Women's Savings Account",
            "ICICI Bank - Women's Account",
            "HDFC Bank - Women Power Account",
            "Axis Bank - Women's Privilege Account"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Step 1: Choose Your Bank")
            .setItems(banks) { _, which ->
                val selectedBank = banks[which]
                Toast.makeText(
                    context,
                    "Selected: $selectedBank\n\n Proceeding to video KYC...",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun showLoanAssessmentDialog() {
        val view = layoutInflater.inflate(
            android.R.layout.simple_list_item_1,
            null
        ) as android.widget.LinearLayout
        view.orientation = android.widget.LinearLayout.VERTICAL
        view.setPadding(50, 20, 50, 20)

        val incomeInput = EditText(requireContext()).apply {
            hint = "Monthly Income (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val ageInput = EditText(requireContext()).apply {
            hint = "Age"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val businessInput = EditText(requireContext()).apply {
            hint = "Business Type (e.g., Tailoring, Shop, etc.)"
        }
        val loansInput = EditText(requireContext()).apply {
            hint = "Existing Loans (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val creditInput = EditText(requireContext()).apply {
            hint = "Credit Score (if known, or 0)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        view.addView(incomeInput)
        view.addView(ageInput)
        view.addView(businessInput)
        view.addView(loansInput)
        view.addView(creditInput)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(" Loan Eligibility Assessment")
            .setMessage("Enter your details for personalized loan recommendations:")
            .setView(view)
            .setPositiveButton("Check Eligibility") { _, _ ->
                val income = incomeInput.text.toString().toLongOrNull() ?: 0L
                val age = ageInput.text.toString().toIntOrNull() ?: 25
                val business = businessInput.text.toString().ifBlank { "Small Business" }
                val loans = loansInput.text.toString().toLongOrNull() ?: 0L
                val credit = creditInput.text.toString().toIntOrNull() ?: 650

                viewModel.assessLoanEligibility(income, age, business, loans, credit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoanAssessmentResult(assessment: LoanAssessment) {
        val eligibilityIcon = if (assessment.eligible) "" else ""
        val message = """
            $eligibilityIcon ${assessment.eligibilityStatus}
            
            Eligibility Score: ${assessment.score.toInt()}/100
            
            Maximum Loan: ₹${assessment.maxLoanAmount / 100000} lakhs
            
            Recommended: ₹${assessment.recommendedLoanAmount / 100000} lakhs
            
            Interest Rate: ${assessment.interestRate}% per annum
            
            Repayment Period: ${assessment.repaymentPeriod} months
            
            AI Advice:
            ${assessment.aiAdvice}
            
            Applicable Schemes: ${assessment.applicableSchemes.size}
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Loan Eligibility Report")
            .setMessage(message)
            .setPositiveButton("View Schemes") { _, _ ->
                if (assessment.applicableSchemes.isNotEmpty()) {
                    showApplicableSchemes(assessment.applicableSchemes)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showApplicableSchemes(schemes: List<com.shakti.ai.models.GovernmentScheme>) {
        val schemesText = schemes.joinToString("\n\n") {
            "${it.name}\n${it.description}\n${it.loanAmount}\n${it.interestRate}"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Recommended Schemes")
            .setMessage(schemesText)
            .setPositiveButton("Apply") { _, _ ->
                Toast.makeText(context, "Opening application portal...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showInvestmentPlannerDialog() {
        val view = layoutInflater.inflate(
            android.R.layout.simple_list_item_1,
            null
        ) as android.widget.LinearLayout
        view.orientation = android.widget.LinearLayout.VERTICAL
        view.setPadding(50, 20, 50, 20)

        val targetInput = EditText(requireContext()).apply {
            hint = "Target Amount (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val timeframeInput = EditText(requireContext()).apply {
            hint = "Time Period (months)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val monthlyInput = EditText(requireContext()).apply {
            hint = "Monthly Investment (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        view.addView(targetInput)
        view.addView(timeframeInput)
        view.addView(monthlyInput)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(" Investment Planner")
            .setMessage("Let's plan your investment strategy:")
            .setView(view)
            .setPositiveButton("Create Plan") { _, _ ->
                val target = targetInput.text.toString().toLongOrNull() ?: 100000L
                val timeframe = timeframeInput.text.toString().toIntOrNull() ?: 12
                val monthly = monthlyInput.text.toString().toLongOrNull() ?: 5000L

                viewModel.createInvestmentPlan(target, timeframe, RiskProfile.MEDIUM, monthly)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showInvestmentPlanResult(plan: com.shakti.ai.models.InvestmentPlan) {
        val allocationText = plan.assetAllocation.entries.joinToString("\n") {
            "${it.key}: ${String.format("%.1f", it.value * 100)}%"
        }

        val message = """
            Target Amount: ₹${plan.targetAmount}
            Timeframe: ${plan.timeframeMonths} months
            Monthly Investment: ₹${plan.monthlyInvestment}
            
            Asset Allocation:
            $allocationText
            
            Expected Return: ${String.format("%.1f", plan.expectedAnnualReturn * 100)}% per year
            Projected Final Amount: ₹${plan.projectedFinalAmount}
            
            AI Advice:
            ${plan.aiAdvice}
            
            Recommendations:
            ${plan.specificRecommendations.joinToString("\n") { " $it" }}
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Your Investment Plan")
            .setMessage(message)
            .setPositiveButton("Start Investing") { _, _ ->
                Toast.makeText(
                    context,
                    "Great! Opening investment platforms...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun analyzeBudget() {
        Toast.makeText(context, " Opening budget analysis...", Toast.LENGTH_SHORT).show()

        val budgetTips = """
            Smart Budget Tips:
            
            1. Track all expenses for 1 month
            2. Categorize: Needs vs Wants
            3. Set spending limits per category
            4. Review weekly
            5. Adjust as needed
            
            Popular apps for budget tracking:
            • Walnut
            • ET Money
            • Money View
            • Excel/Google Sheets (free!)
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Budget Analysis")
            .setMessage(budgetTips)
            .setPositiveButton("Got it!", null)
            .show()
    }
}
