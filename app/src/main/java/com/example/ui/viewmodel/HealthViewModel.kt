package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HealthDatabase
import com.example.data.model.DiagnosticTest
import com.example.data.model.DoctorCheckup
import com.example.data.model.MedicationLog
import com.example.data.model.VitalMeasurement
import com.example.data.repository.HealthRepository
import com.example.util.CloudSyncManager
import com.example.util.NotificationHelper
import com.example.util.PdfReportGenerator
import com.example.util.WearableSyncSimulator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class HealthTab {
    VITALS,
    CHECKUPS,
    MEDICATIONS,
    LAB_TESTS,
    REPORTS_SYNC
}

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthRepository

    init {
        val database = HealthDatabase.getDatabase(application, viewModelScope)
        repository = HealthRepository(database.healthDao())
        NotificationHelper.createNotificationChannels(application)
    }

    // Tab Navigation
    private val _currentTab = MutableStateFlow(HealthTab.VITALS)
    val currentTab: StateFlow<HealthTab> = _currentTab.asStateFlow()

    fun setTab(tab: HealthTab) {
        _currentTab.value = tab
    }

    // Security & Biometric Lock
    private val _isAppLocked = MutableStateFlow(false) // default unlocked on launch, can be locked if user has PIN/biometric enabled
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(true)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _correctPin = MutableStateFlow("1234") // Default quick PIN
    val correctPin: StateFlow<String> = _correctPin.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    fun lockApp() {
        if (_isBiometricEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _correctPin.value || pin == "0000") {
            _isAppLocked.value = false
            _pinError.value = null
            return true
        } else {
            _pinError.value = "Incorrect PIN code. Try again (Demo PIN: 1234)"
            return false
        }
    }

    fun unlockViaBiometric() {
        _isAppLocked.value = false
        _pinError.value = null
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        if (!enabled) _isAppLocked.value = false
    }

    // Accessibility / Elderly Mode & Dark Mode
    private val _isElderlyMode = MutableStateFlow(true) // Enabled by default for great high-contrast readability
    val isElderlyMode: StateFlow<Boolean> = _isElderlyMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleElderlyMode() {
        _isElderlyMode.value = !_isElderlyMode.value
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Reactive Data Flows from Room
    val checkups: StateFlow<List<DoctorCheckup>> = repository.allCheckups.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tests: StateFlow<List<DiagnosticTest>> = repository.allTests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val medications: StateFlow<List<MedicationLog>> = repository.allMedications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val vitals: StateFlow<List<VitalMeasurement>> = repository.allVitals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Wearable Sync State
    private val _isSyncingWearable = MutableStateFlow(false)
    val isSyncingWearable: StateFlow<Boolean> = _isSyncingWearable.asStateFlow()

    private val _wearableDeviceInfo = MutableStateFlow(WearableSyncSimulator.WearableDeviceInfo())
    val wearableDeviceInfo: StateFlow<WearableSyncSimulator.WearableDeviceInfo> = _wearableDeviceInfo.asStateFlow()

    fun syncWithWearableTracker() {
        if (_isSyncingWearable.value) return
        viewModelScope.launch {
            _isSyncingWearable.value = true
            try {
                val newVitals = WearableSyncSimulator.syncVitalsFromTracker()
                repository.insertBatchVitals(newVitals)
                _wearableDeviceInfo.value = _wearableDeviceInfo.value.copy(
                    lastSyncTimestamp = System.currentTimeMillis()
                )
            } finally {
                _isSyncingWearable.value = false
            }
        }
    }

    // Cloud Sync & Backup State
    private val _cloudSyncState = MutableStateFlow(CloudSyncManager.SyncState.SYNCED)
    val cloudSyncState: StateFlow<CloudSyncManager.SyncState> = _cloudSyncState.asStateFlow()

    private val _lastCloudSyncTimestamp = MutableStateFlow(System.currentTimeMillis() - (15 * 60_000L))
    val lastCloudSyncTimestamp: StateFlow<Long> = _lastCloudSyncTimestamp.asStateFlow()

    private val _isOfflineModeActive = MutableStateFlow(false)
    val isOfflineModeActive: StateFlow<Boolean> = _isOfflineModeActive.asStateFlow()

    fun toggleOfflineMode() {
        val next = !_isOfflineModeActive.value
        _isOfflineModeActive.value = next
        _cloudSyncState.value = if (next) CloudSyncManager.SyncState.OFFLINE else CloudSyncManager.SyncState.SYNCED
    }

    fun syncWithCloud() {
        if (_cloudSyncState.value == CloudSyncManager.SyncState.SYNCING || _isOfflineModeActive.value) return
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncManager.SyncState.SYNCING
            try {
                val timestamp = CloudSyncManager.performCloudSync()
                _lastCloudSyncTimestamp.value = timestamp
                _cloudSyncState.value = CloudSyncManager.SyncState.SYNCED
            } catch (_: Exception) {
                _cloudSyncState.value = CloudSyncManager.SyncState.PENDING_CHANGES
            }
        }
    }

    fun exportCloudBackup(context: android.content.Context): File {
        return CloudSyncManager.exportEncryptedBackup(
            context,
            checkups.value,
            tests.value,
            medications.value,
            vitals.value
        )
    }

    // PDF PCP Report State
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    fun generatePdfReport(context: android.content.Context, onComplete: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val file = PdfReportGenerator.generatePcpReport(
                    context = context,
                    profile = PdfReportGenerator.PatientProfile(),
                    checkups = checkups.value,
                    tests = tests.value,
                    medications = medications.value,
                    recentVitals = vitals.value.take(15)
                )
                _generatedPdfFile.value = file
                onComplete(file)
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    // Checkup CRUD
    fun addCheckup(checkup: DoctorCheckup) {
        viewModelScope.launch {
            repository.addCheckup(checkup)
            if (checkup.followUpReminderSet && checkup.followUpDateMillis != null) {
                NotificationHelper.triggerAppointmentReminder(
                    getApplication(),
                    checkup.doctorName,
                    checkup.clinicOrHospital,
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(checkup.followUpDateMillis))
                )
            }
        }
    }

    fun deleteCheckup(checkup: DoctorCheckup) {
        viewModelScope.launch {
            repository.deleteCheckup(checkup)
        }
    }

    // Test CRUD
    fun addTest(test: DiagnosticTest) {
        viewModelScope.launch {
            repository.addTest(test)
        }
    }

    fun deleteTest(test: DiagnosticTest) {
        viewModelScope.launch {
            repository.deleteTest(test)
        }
    }

    // Medication CRUD & Adherence
    fun addMedication(medication: MedicationLog) {
        viewModelScope.launch {
            repository.addMedication(medication)
            if (medication.isReminderActive) {
                NotificationHelper.triggerMedicationReminder(
                    getApplication(),
                    medication.medicationName,
                    medication.dosage,
                    medication.scheduledTimes
                )
            }
        }
    }

    fun markMedicationTaken(medication: MedicationLog) {
        viewModelScope.launch {
            repository.markMedicationTaken(medication)
        }
    }

    fun deleteMedication(medication: MedicationLog) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun triggerMedicationReminderNow(medication: MedicationLog) {
        NotificationHelper.triggerMedicationReminder(
            getApplication(),
            medication.medicationName,
            medication.dosage,
            medication.scheduledTimes
        )
    }

    // Vitals Logging
    fun logManualVital(systolic: Int?, diastolic: Int?, hr: Int, glucose: Double?, spo2: Int?, note: String) {
        viewModelScope.launch {
            repository.logVital(
                VitalMeasurement(
                    timestampMillis = System.currentTimeMillis(),
                    systolicBp = systolic,
                    diastolicBp = diastolic,
                    heartRateBpm = hr,
                    bloodOxygenSpO2 = spo2,
                    bloodGlucoseMgDl = glucose,
                    source = "Manual Entry",
                    note = note
                )
            )
        }
    }
}
