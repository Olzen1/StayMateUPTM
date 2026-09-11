package com.staymate.uptm.utils // Or com.staymate.uptm if you didn't create a utils folder

object UptmConstants {

    // 1. Authentication
    const val EMAIL_DOMAIN = "@student.uptm.edu.my"

    // 2. Semesters (Assuming 1 to 8 for standard degree/diploma progression)
    val SEMESTERS = (1..25).map { "Semester $it" }

    // 3. Courses & Programmes
    val COURSES = listOf(
        // FOUNDATION
        "Foundation in Arts (FA001)",
        "Foundation in Commerce (FC001)",
        "Foundation in IT (FT001)",

        // DIPLOMA
        "Diploma of Accountancy (AA103)",
        "Diploma in Computer Science (CC101)",
        "Diploma in Corporate Communication (BK101)",

        // BACHELOR
        "Bachelor of Accountancy (Honours) (AA201)",
        "Bachelor of Arts (Hons) in Applied English Language Studies (BE201)",
        "Bachelor of Arts in 3D Animation and Digital Media (Honours) (CM201)",
        "Bachelor of Business Administration (Honours) Human Resource Management (AB201)",
        "Bachelor of Early Childhood Education (Honours) (BE202)",
        "Bachelor in Computer Science (Honours) (CC203)",
        "Bachelor of Business Administration (Honours) (AB202)",
        "Bachelor of Communication (Hons) in Corporate Communication (BK201)",
        "Bachelor of Information Technology (Honours) in Computer Application Development (CT204)",
        "Bachelor in Information Technology (Honours) (Data Analytics) (CT207)",
        "Bachelor of Education (Honours) in Teaching English As A Second Language (TESL) (BE203)",
        "Bachelor of Information Technology (Honours) in Cyber Security (CT206)",
        "Bachelor in Business Administration (Honours) (Digital Marketing) (AB203)",
        "Bachelor in Artificial Intelligence (Honours) (CC204)",

        // PROFESSIONAL
        "ACCA Foundation in Accountancy (AFIA)",
        "Association of Chartered Certified Accountants (ACCA) Professional Qualification",

        // POST GRADUATE
        "Postgraduate Diploma in Education",
        "Master of Business Administration",
        "MBA (Corporate Administration and Governance) (in collaboration with MAICSA)",
        "Doctor of Philosophy in Information Technology",
        "Master of Science in Information Systems",
        "Master of Accountancy (in collaboration with CIMA)",
        "Doctor of Philosophy in Business Administration",
        "Doctor of Philosophy in Education",
        "Master of Education (TESL)"
    )
}