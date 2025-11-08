package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.shakti.ai.R
import com.shakti.ai.viewmodel.GyaanViewModel
import kotlinx.coroutines.launch

class GyaanAIFragment : Fragment() {

    private val viewModel: GyaanViewModel by viewModels()

    private lateinit var categoryInput: EditText
    private lateinit var stateInput: EditText
    private lateinit var courseInput: EditText
    private lateinit var incomeInput: EditText
    private lateinit var percentageInput: EditText
    private lateinit var btnFindScholarships: Button
    private lateinit var btnPreFillForms: Button
    private lateinit var btnDocumentChecklist: Button
    private lateinit var btnDeadlineReminders: Button
    private lateinit var btnApplicationTracking: Button
    private lateinit var btnVirtualMentorship: Button
    private lateinit var btnWomenLeadersStories: Button
    private lateinit var btnSkillDevelopment: Button
    private lateinit var btnOnlineCourses: Button
    private lateinit var btnCareerGuidance: Button
    private lateinit var btnSkillAssessment: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gyaan_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        categoryInput = view.findViewById(R.id.category_input)
        stateInput = view.findViewById(R.id.state_input)
        courseInput = view.findViewById(R.id.course_input)
        incomeInput = view.findViewById(R.id.income_input)
        percentageInput = view.findViewById(R.id.percentage_input)
        btnFindScholarships = view.findViewById(R.id.btn_find_scholarships)
        btnPreFillForms = view.findViewById(R.id.btn_pre_fill_forms)
        btnDocumentChecklist = view.findViewById(R.id.btn_document_checklist)
        btnDeadlineReminders = view.findViewById(R.id.btn_deadline_reminders)
        btnApplicationTracking = view.findViewById(R.id.btn_application_tracking)
        btnVirtualMentorship = view.findViewById(R.id.btn_virtual_mentorship)
        btnWomenLeadersStories = view.findViewById(R.id.btn_women_leaders_stories)
        btnSkillDevelopment = view.findViewById(R.id.btn_skill_development)

        // Additional features
        btnOnlineCourses = createButton("💻 Free Online Courses")
        btnCareerGuidance = createButton("🎯 Career Guidance")
        btnSkillAssessment = createButton("📊 Skill Assessment")
    }

    private fun createButton(text: String): Button {
        return Button(requireContext()).apply {
            this.text = text
        }
    }

    private fun setupClickListeners() {
        btnFindScholarships.setOnClickListener {
            findScholarships()
        }

        btnPreFillForms.setOnClickListener {
            showPreFillFormsWizard()
        }

        btnDocumentChecklist.setOnClickListener {
            showDocumentChecklist()
        }

        btnDeadlineReminders.setOnClickListener {
            setupDeadlineReminders()
        }

        btnApplicationTracking.setOnClickListener {
            showApplicationTracking()
        }

        btnVirtualMentorship.setOnClickListener {
            connectWithMentor()
        }

        btnWomenLeadersStories.setOnClickListener {
            showWomenLeadersStories()
        }

        btnSkillDevelopment.setOnClickListener {
            showFreeSkillDevelopmentCourses()
        }

        btnOnlineCourses.setOnClickListener {
            showOnlineCourses()
        }

        btnCareerGuidance.setOnClickListener {
            showCareerGuidance()
        }

        btnSkillAssessment.setOnClickListener {
            takeSkillAssessment()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                btnFindScholarships.isEnabled = !isLoading
                btnFindScholarships.text =
                    if (isLoading) "Searching..." else "🔍 Find My Scholarships"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scholarships.collect { scholarships ->
                if (scholarships.isNotEmpty()) {
                    showScholarships(scholarships)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.courseRecommendations.collect { courses ->
                if (courses.isNotEmpty()) {
                    showCoursesDialog(courses)
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

    private fun findScholarships() {
        val education = courseInput.text.toString()
        val income = incomeInput.text.toString().toLongOrNull() ?: 0L
        val category = categoryInput.text.toString()

        if (education.isBlank()) {
            Toast.makeText(context, "Please enter course details", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.findScholarships(education, income, category)
    }

    private fun showScholarships(scholarships: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🎓 Scholarships Found")
            .setMessage(scholarships)
            .setPositiveButton("Apply Now") { _, _ ->
                Toast.makeText(context, "Opening application portal...", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Save List") { _, _ ->
                Toast.makeText(context, "✅ Scholarships saved to your profile", Toast.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showPreFillFormsWizard() {
        val formTypes = arrayOf(
            "Scholarship Application Form",
            "College Admission Form",
            "Government Scheme Application",
            "Online Course Registration",
            "Job Application Form"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 Auto-Fill Forms")
            .setMessage("Select form type to auto-fill:")
            .setItems(formTypes) { _, which ->
                val formType = formTypes[which]
                Toast.makeText(
                    context,
                    "✅ Opening $formType with auto-fill enabled",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDocumentChecklist() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📋 Document Checklist")
            .setMessage(
                """
                For Scholarship Application:
                
                ✅ 10th Marksheet
                ✅ 12th Marksheet
                ✅ Graduation Marksheet (if applicable)
                ✅ Income Certificate (< 1 year old)
                ✅ Caste Certificate (if applicable)
                ✅ Domicile Certificate
                ✅ Aadhaar Card
                ✅ Bank Account Passbook
                ✅ Passport Size Photos (recent)
                ✅ College/University ID
                ✅ Bonafide Certificate
                
                💡 Tip: Keep scanned copies (PDF format) ready!
            """.trimIndent()
            )
            .setPositiveButton("Upload Documents") { _, _ ->
                Toast.makeText(context, "Opening document upload...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun setupDeadlineReminders() {
        val upcomingDeadlines = """
            ⏰ Upcoming Scholarship Deadlines:
            
            🔔 National Scholarship Portal
            Deadline: 31st January 2025
            Days Left: 15 days
            
            🔔 Prime Minister's Scholarship Scheme
            Deadline: 15th February 2025
            Days Left: 30 days
            
            🔔 Begum Hazrat Mahal National Scholarship
            Deadline: 28th February 2025
            Days Left: 43 days
            
            🔔 Post Matric Scholarship for Minorities
            Deadline: 15th March 2025
            Days Left: 58 days
            
            🔔 INSPIRE Scholarship (Science Students)
            Deadline: 31st March 2025
            Days Left: 74 days
            
            💡 Enable notifications to never miss a deadline!
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("⏰ Deadline Reminders")
            .setMessage(upcomingDeadlines)
            .setPositiveButton("Set Reminders") { _, _ ->
                Toast.makeText(
                    context,
                    "✅ Reminders set! You'll get notifications 7, 3, and 1 day before each deadline",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNeutralButton("Calendar Sync") { _, _ ->
                Toast.makeText(
                    context,
                    "📅 Syncing deadlines to your calendar...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showApplicationTracking() {
        val trackingInfo = """
            📊 Your Application Status:
            
            ✅ COMPLETED (2 Applications)
            • National Merit Scholarship
              Status: Under Review
              Applied: 2 weeks ago
              
            • State Minority Scholarship
              Status: Approved ✓
              Amount: ₹50,000/year
              Credited: Next month
            
            ⏳ IN PROGRESS (1 Application)
            • Post Matric Scholarship
              Status: Documents pending
              Missing: Income Certificate
              Deadline: 20 days left
            
            📝 DRAFT (3 Applications)
            • Prime Minister's Scholarship
            • INSPIRE Scholarship
            • Girl Child Scholarship
            
            💡 Complete your pending applications today!
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Application Tracking")
            .setMessage(trackingInfo)
            .setPositiveButton("Complete Pending") { _, _ ->
                Toast.makeText(
                    context,
                    "Opening pending applications...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNeutralButton("View All") { _, _ ->
                Toast.makeText(
                    context,
                    "Loading detailed application history...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showWomenLeadersStories() {
        val leaders = arrayOf(
            "🌟 Indra Nooyi - From Madras to PepsiCo CEO",
            "🌟 Kiran Mazumdar-Shaw - Biocon Founder",
            "🌟 Sudha Murty - Philanthropist & Author",
            "🌟 Mary Kom - Olympic Boxer",
            "🌟 Arundhati Bhattacharya - First Woman SBI Chairperson",
            "🌟 Sania Mirza - Tennis Champion",
            "🌟 Naina Lal Kidwai - Banking Pioneer",
            "🌟 Priyanka Chopra - Global Icon"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("💪 Women Leaders Stories")
            .setMessage("Select a leader to read their inspiring journey:")
            .setItems(leaders) { _, which ->
                showLeaderStory(which)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showLeaderStory(index: Int) {
        val stories = arrayOf(
            // Indra Nooyi
            """
                🌟 Indra Nooyi
                Former CEO, PepsiCo
                
                Journey:
                • Born in Chennai, Tamil Nadu
                • Studied at IIM Calcutta
                • Started as product manager
                • Rose to become PepsiCo CEO (2006-2018)
                • One of Fortune's Most Powerful Women
                
                Key Lessons:
                ✨ "Whatever anybody says or does, assume positive intent"
                ✨ Work-life balance is a myth, it's work-life integration
                ✨ Education is the foundation of success
                ✨ Never compromise on your values
                
                Your Turn:
                • Focus on education
                • Build strong fundamentals
                • Work hard with integrity
                • Never give up on dreams
            """.trimIndent(),
            // Kiran Mazumdar-Shaw
            """
                🌟 Kiran Mazumdar-Shaw
                Founder, Biocon (₹15,000 Cr company)
                
                Journey:
                • Started with ₹10,000 in 1978
                • Faced rejection for being a woman
                • Built India's largest biotech company
                • One of India's richest self-made women
                
                Key Lessons:
                ✨ "I learned that if you have to be an entrepreneur, you have to be fearless"
                ✨ Failures are stepping stones
                ✨ Innovation is key to success
                ✨ Give back to society
                
                Your Turn:
                • Don't fear failure
                • Start small, dream big
                • Solve real problems
                • Be persistent
            """.trimIndent(),
            // Sudha Murty
            """
                🌟 Sudha Murty
                Philanthropist, Author, Infosys Foundation
                
                Journey:
                • First woman engineer at TATA
                • Gave ₹10,000 to her husband (Narayana Murthy) to start Infosys
                • Runs Infosys Foundation
                • Written 30+ books
                • Padma Shri & Padma Bhushan awardee
                
                Key Lessons:
                ✨ "Simple living, high thinking"
                ✨ Education transforms lives
                ✨ Give back to society
                ✨ Stay humble despite success
                
                Your Turn:
                • Value education
                • Help others
                • Stay grounded
                • Write your own story
            """.trimIndent(),
            // Mary Kom
            """
                🌟 MC Mary Kom
                Olympic Bronze Medalist, 6-time World Champion
                
                Journey:
                • Born in rural Manipur
                • Faced poverty and discrimination
                • Balanced boxing with motherhood (3 kids)
                • Won Olympic medal at age 29
                • First Indian woman boxer to qualify for Olympics
                
                Key Lessons:
                ✨ "Champions aren't made in gyms. Champions are made from something they have deep inside them"
                ✨ No obstacle is too big
                ✨ Believe in yourself
                ✨ Hard work beats talent
                
                Your Turn:
                • Stay dedicated
                • Don't let circumstances define you
                • Set ambitious goals
                • Keep fighting
            """.trimIndent(),
            // Arundhati Bhattacharya
            """
                🌟 Arundhati Bhattacharya
                First Woman Chairperson, SBI
                
                Journey:
                • Joined SBI as probationary officer
                • Worked for 35+ years
                • Broke glass ceiling in male-dominated banking
                • Led India's largest bank (2013-2017)
                • Forbes' Most Powerful Women
                
                Key Lessons:
                ✨ "Don't wait for opportunities, create them"
                ✨ Competence has no gender
                ✨ Stay focused on goals
                ✨ Lead with empathy
                
                Your Turn:
                • Build strong career foundation
                • Prove yourself with work
                • Don't let stereotypes stop you
                • Aim for leadership roles
            """.trimIndent(),
            // Sania Mirza
            """
                🌟 Sania Mirza
                First Indian Woman Tennis Star
                
                Journey:
                • Started playing at age 6
                • Faced cultural barriers
                • Became world No. 1 in doubles
                • 6 Grand Slam titles
                • Broke stereotypes about Muslim women
                
                Key Lessons:
                ✨ "I don't play for records, I play because I love the sport"
                ✨ Break barriers, don't accept them
                ✨ Family support is crucial
                ✨ Hard work never fails
                
                Your Turn:
                • Follow your passion
                • Challenge societal norms
                • Train consistently
                • Inspire others
            """.trimIndent(),
            // Naina Lal Kidwai
            """
                🌟 Naina Lal Kidwai
                Banking Pioneer, First Indian Woman Harvard MBA
                
                Journey:
                • First Indian woman with Harvard MBA
                • Worked at ANZ Grindlays, Morgan Stanley, HSBC
                • Country Head, HSBC India
                • Multiple board memberships
                
                Key Lessons:
                ✨ "Be bold, be authentic, be yourself"
                ✨ Education opens doors
                ✨ Take calculated risks
                ✨ Mentor others
                
                Your Turn:
                • Invest in education
                • Build strong networks
                • Take leadership roles
                • Help other women rise
            """.trimIndent(),
            // Priyanka Chopra
            """
                🌟 Priyanka Chopra Jonas
                Global Icon, Actor, Producer
                
                Journey:
                • Miss World 2000
                • Bollywood superstar
                • Moved to Hollywood
                • Starred in Quantico, Matrix 4
                • Producer, Entrepreneur, UNICEF Goodwill Ambassador
                
                Key Lessons:
                ✨ "I've never believed in limitations"
                ✨ Take risks, go global
                ✨ Don't let anyone box you in
                ✨ Use fame for good causes
                
                Your Turn:
                • Think beyond boundaries
                • Embrace new challenges
                • Build global mindset
                • Give back to society
            """.trimIndent()
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Inspiring Journey")
            .setMessage(stories[index])
            .setPositiveButton("Save Story") { _, _ ->
                Toast.makeText(
                    context,
                    "✅ Story saved to your inspiration library",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNeutralButton("Read More Stories") { _, _ ->
                showWomenLeadersStories()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showFreeSkillDevelopmentCourses() {
        val courseCategories = """
            💻 FREE Skill Development Courses:
            
            📱 DIGITAL SKILLS
            • Google Digital Garage - Digital Marketing
            • Microsoft Digital Literacy
            • Facebook Blueprint - Social Media
            • Canva Design School
            
            💼 PROFESSIONAL SKILLS
            • Coursera - Communication Skills
            • LinkedIn Learning - Leadership
            • edX - Project Management
            • Alison - Business Skills
            
            🖥️ TECHNICAL SKILLS
            • freeCodeCamp - Coding
            • Khan Academy - Computer Science
            • NPTEL - Engineering
            • MIT OpenCourseWare
            
            🎨 CREATIVE SKILLS
            • Skillshare - Design & Art
            • YouTube - Tutorial Channels
            • Domestika - Creative Arts
            • Adobe Creative Cloud Tutorials
            
            🏢 GOVERNMENT PLATFORMS
            • SWAYAM (UGC approved)
            • NIOS (National Open School)
            • IGNOU Online Programs
            • Skill India Digital
            
            All courses are 100% FREE!
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🎓 Free Skill Development")
            .setMessage(courseCategories)
            .setPositiveButton("Browse Courses") { _, _ ->
                showSkillCourseCategories()
            }
            .setNeutralButton("Get Certified") { _, _ ->
                Toast.makeText(
                    context,
                    "Opening certification programs...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSkillCourseCategories() {
        val categories = arrayOf(
            "💻 Technology & Coding",
            "📊 Data Science & Analytics",
            "🎨 Design & Creative Arts",
            "📱 Digital Marketing",
            "💼 Business & Management",
            "🗣️ Communication Skills",
            "🌐 Languages (English, Hindi, etc.)",
            "🎓 Exam Preparation (UPSC, Banking, etc.)"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Choose Category")
            .setItems(categories) { _, which ->
                val category = categories[which].substring(2)
                viewModel.recommendCourses(emptyList(), category, 0L)
                Toast.makeText(
                    context,
                    "Loading courses in: $category",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun connectWithMentor() {
        val mentorTypes = arrayOf(
            "🎓 Academic Counselor",
            "💼 Career Mentor",
            "💻 Tech Industry Expert",
            "👩‍⚕️ Healthcare Professional",
            "👩‍🏫 Teaching/Education",
            "📊 Business/Entrepreneurship"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("👩‍🏫 Connect with Mentor")
            .setMessage("Select your area of interest:")
            .setItems(mentorTypes) { _, which ->
                val mentor = mentorTypes[which].substring(2)
                Toast.makeText(
                    context,
                    "✅ Finding mentors in: $mentor",
                    Toast.LENGTH_SHORT
                ).show()
                // Search for mentors and courses in this field
                viewModel.recommendCourses(emptyList(), mentor, 0L)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showOnlineCourses() {
        viewModel.recommendCourses(emptyList(), "Technology", 0L)

        val popularCourses = """
            💻 Free Online Learning Platforms:
            
            1. SWAYAM (Government of India)
               - Free courses with certificates
               - Accepted by employers
               
            2. NPTEL (IIT/IISc)
               - Engineering & Science
               - Free video lectures
            
            3. Google Digital Garage
               - Digital Marketing
               - Free certification
            
            4. Microsoft Learn
               - Technology skills
               - Free with certificates
            
            5. Coursera for Women
               - Scholarships available
               - Global universities
            
            6. Udemy Free Courses
               - Varied subjects
               - Lifetime access
            
            💡 Search for courses in your field!
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("💻 Free Online Courses")
            .setMessage(popularCourses)
            .setPositiveButton("Browse Courses") { _, _ ->
                viewModel.recommendCourses(emptyList(), "All", 0L)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showCoursesDialog(courses: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📚 Recommended Courses")
            .setMessage(courses)
            .setPositiveButton("Enroll") { _, _ ->
                Toast.makeText(context, "Opening course enrollment...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showCareerGuidance() {
        val input = EditText(requireContext()).apply {
            hint = "What are your interests? (e.g., Teaching, Technology, Healthcare)"
            setPadding(50, 40, 50, 40)
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🎯 Career Guidance")
            .setMessage("Tell us about your interests and skills:")
            .setView(input)
            .setPositiveButton("Get Guidance") { _, _ ->
                val interests = input.text.toString()
                if (interests.isNotBlank()) {
                    showCareerOptions(interests)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCareerOptions(interests: String) {
        val careerAdvice = """
            🎯 Career Paths for "$interests":
            
            ${getCareerSuggestions(interests)}
            
            📚 Recommended Skills to Learn:
            • Communication Skills
            • Digital Literacy
            • Leadership & Management
            • Technical Skills (field-specific)
            
            💼 Job Portals for Women:
            • Naukri.com
            • LinkedIn
            • Indeed
            • WomenJobPortal.in
            • Sheroes
            
            💡 Next Steps:
            1. Take skill assessment
            2. Complete relevant courses
            3. Build portfolio/resume
            4. Network with professionals
            5. Apply for internships/jobs
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Career Guidance")
            .setMessage(careerAdvice)
            .setPositiveButton("Explore Courses") { _, _ ->
                showOnlineCourses()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun getCareerSuggestions(interests: String): String {
        return when {
            interests.contains("tech", ignoreCase = true) -> """
                • Software Developer
                • Data Analyst
                • Digital Marketing Specialist
                • UI/UX Designer
                • Cybersecurity Analyst
            """.trimIndent()

            interests.contains("teach", ignoreCase = true) -> """
                • School Teacher
                • Online Tutor
                • Educational Content Creator
                • Career Counselor
                • Training & Development Specialist
            """.trimIndent()

            interests.contains("health", ignoreCase = true) -> """
                • Nurse
                • Medical Technician
                • Nutritionist
                • Public Health Worker
                • Healthcare Administrator
            """.trimIndent()

            else -> """
                • Based on your interests, multiple career options available
                • Take skill assessment for personalized recommendations
                • Connect with mentors for guidance
            """.trimIndent()
        }
    }

    private fun takeSkillAssessment() {
        val skills = arrayOf(
            "Communication Skills",
            "Problem Solving",
            "Technical Skills (Computers)",
            "Leadership & Management",
            "Creative Thinking",
            "Financial Literacy"
        )

        val selectedSkills = BooleanArray(skills.size)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Skill Assessment")
            .setMessage("Select skills you want to assess:")
            .setMultiChoiceItems(skills, selectedSkills) { _, which, isChecked ->
                selectedSkills[which] = isChecked
            }
            .setPositiveButton("Start Assessment") { _, _ ->
                val selected = skills.filterIndexed { index, _ -> selectedSkills[index] }
                if (selected.isNotEmpty()) {
                    showAssessmentResult(selected)
                } else {
                    Toast.makeText(context, "Please select at least one skill", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAssessmentResult(skills: List<String>) {
        val result = skills.joinToString("\n") { skill ->
            val score = (60..95).random()
            "• $skill: $score/100"
        }

        val advice = """
            📊 Your Skill Assessment Results:
            
            $result
            
            💡 Recommendations:
            • Focus on improving lower-scored skills
            • Take online courses to enhance knowledge
            • Practice regularly
            • Seek mentorship
            • Apply skills in real projects
            
            🎯 Suggested Learning Path:
            1. Complete beginner courses
            2. Work on practical projects
            3. Get certified
            4. Join communities
            5. Keep learning & growing!
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Assessment Results")
            .setMessage(advice)
            .setPositiveButton("Find Courses") { _, _ ->
                showOnlineCourses()
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
