package com.example.lifelineapp

import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifelineapp.adapters.AppointmentsAdapter
import com.example.lifelineapp.model.Appointment

import com.example.lifelineapp.utils.FullScreenUtil
import com.example.lifelineapp.utils.SpaceItemRecyclerView
import com.google.firebase.database.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import nl.joery.animatedbottombar.AnimatedBottomBar
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentsAdapter: AppointmentsAdapter
    private lateinit var calendarView: MaterialCalendarView
    private val appointmentsList = mutableListOf<Appointment>()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)

        // Set the activity to full-screen mode
        FullScreenUtil.setupFullScreenMode(this)

        // Initialize views
        calendarView = findViewById(R.id.calendarView)
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)

        // Set up RecyclerView
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentsAdapter = AppointmentsAdapter(appointmentsList)
        appointmentsRecyclerView.adapter = appointmentsAdapter

        // Convert spacing from dp to pixels
        val spacingInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            13f, // 16dp spacing
            resources.displayMetrics
        ).toInt()

        // Add item decoration to RecyclerView
        appointmentsRecyclerView.addItemDecoration(SpaceItemRecyclerView(spacingInPixels))

        // Fetch appointments from Firebase
        fetchAppointments()

        // Set up bottom bar
        val bottomBar = findViewById<AnimatedBottomBar>(R.id.navBar)
        BottomBarUtils.setupBottomBar(this, bottomBar)
    }

    private fun fetchAppointments() {
        val patientId = "patient_1"  // Use the actual patient ID as needed
        database.child("users").child("patients").child(patientId).child("appointments")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointmentsList.clear()
                    val datesToHighlight = mutableListOf<CalendarDay>()

                    for (appointmentSnapshot in snapshot.children) {
                        val time = appointmentSnapshot.child("Time").getValue(String::class.java) ?: ""
                        val dateStr = appointmentSnapshot.child("Date").getValue(String::class.java) ?: ""
                        val address = appointmentSnapshot.child("Address").getValue(String::class.java) ?: ""

                        val appointment = Appointment(time, dateStr, address)
                        appointmentsList.add(appointment)

                        // Parse the date string and add to calendar highlights
                        val date = dateFormat.parse(dateStr)
                        date?.let {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            val calendarDay = CalendarDay.from(calendar)
                            datesToHighlight.add(calendarDay)
                        }
                    }
                    appointmentsAdapter.notifyDataSetChanged()
                    calendarView.addDecorators(EventHighlighter(datesToHighlight))
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
    }
}




//class CalendarActivity : AppCompatActivity() {
//
//    private lateinit var calendarView: MaterialCalendarView
//    private lateinit var upcomingAppointmentsRecyclerView: RecyclerView
//    private lateinit var appointmentsAdapter: AppointmentsAdapter
//    private var appointmentsList: List<Appointment> = listOf()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_calendar)
//
//        // Set the activity to full-screen mode
//        FullScreenUtil.setupFullScreenMode(this)
//
//        // Initialize views
//        calendarView = findViewById(R.id.calendarView)
//        upcomingAppointmentsRecyclerView = findViewById(R.id.upcomingAppointmentsRecyclerView)
//
//        // Setup RecyclerView
//        appointmentsAdapter = AppointmentsAdapter(appointmentsList)
//        upcomingAppointmentsRecyclerView.layoutManager = LinearLayoutManager(this)
//        upcomingAppointmentsRecyclerView.adapter = appointmentsAdapter
//
//        // Load upcoming appointments
//        loadUpcomingAppointments()
//
//        // Set up the bottom bar
//        val bottomBar = findViewById<AnimatedBottomBar>(R.id.navBar)
//        BottomBarUtils.setupBottomBar(this, bottomBar)
//    }
//
//    private fun loadUpcomingAppointments() {
//        // Mock data for testing, replace with Firebase fetching logic
//        appointmentsList = listOf(
//            Appointment("23 January 2023", "5:30 PM - 6:00 PM", "Global Office Facility"),
//            Appointment("25 January 2023", "10:00 AM - 11:00 AM", "Central Clinic"),
//            Appointment("30 January 2023", "1:00 PM - 2:00 PM", "East Medical Center")
//        )
//        appointmentsAdapter.updateData(appointmentsList)
//    }
//}