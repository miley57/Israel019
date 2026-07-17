package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CatisRepository

    // --- Active Project & Role ---
    val activeProjectCode = MutableStateFlow("CATIS-P001")
    val activeRole = MutableStateFlow("CATIS Administrator") // Default role with full access

    // --- State Streams ---
    val allTransactions: StateFlow<List<MaterialTransaction>>
    val allStructuralUpdates: StateFlow<List<StructuralUpdate>>
    val allEquipmentLogs: StateFlow<List<EquipmentLog>>
    val allSecurityLogs: StateFlow<List<SecurityLog>>
    val allIncidentLogs: StateFlow<List<IncidentLog>>

    // --- Filtered State Streams based on activeProjectCode ---
    val projectTransactions: StateFlow<List<MaterialTransaction>>
    val projectStructuralUpdates: StateFlow<List<StructuralUpdate>>
    val projectEquipmentLogs: StateFlow<List<EquipmentLog>>
    val projectSecurityLogs: StateFlow<List<SecurityLog>>
    val projectIncidentLogs: StateFlow<List<IncidentLog>>

    init {
        val database = CatisDatabase.getDatabase(application)
        val dao = database.catisDao()
        repository = CatisRepository(dao)

        // Seed data in coroutine
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        // Initialize streams
        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allStructuralUpdates = repository.allStructuralUpdates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allEquipmentLogs = repository.allEquipmentLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSecurityLogs = repository.allSecurityLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allIncidentLogs = repository.allIncidentLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Dynamically filtered project data (using combine to react to activeProjectCode or list updates)
        projectTransactions = combine(activeProjectCode, allTransactions) { code, txs ->
            txs.filter { it.projectCode == code }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        projectStructuralUpdates = combine(activeProjectCode, allStructuralUpdates) { code, list ->
            list.filter { it.projectCode == code }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        projectEquipmentLogs = combine(activeProjectCode, allEquipmentLogs) { code, list ->
            list.filter { it.projectCode == code }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        projectSecurityLogs = combine(activeProjectCode, allSecurityLogs) { code, list ->
            list.filter { it.projectCode == code }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        projectIncidentLogs = combine(activeProjectCode, allIncidentLogs) { code, list ->
            list.filter { it.projectCode == code }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- Operations ---

    fun setProject(code: String) {
        activeProjectCode.value = code
    }

    fun setRole(role: String) {
        activeRole.value = role
    }

    // --- Form Submissions ---

    fun submitMaterialDelivery(
        projectCode: String,
        date: String,
        time: String,
        supplierName: String,
        vehicleNumber: String,
        materialCode: String,
        quantity: Double,
        unit: String,
        storageLocation: String,
        verificationStatus: String,
        condition: String,
        receivedBy: String,
        securityVerified: String,
        photoPath: String?,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                MaterialTransaction(
                    projectCode = projectCode,
                    transactionType = "DELIVERY",
                    date = date,
                    time = time,
                    supplierOrRecipient = supplierName,
                    vehicleNumber = vehicleNumber,
                    materialCode = materialCode,
                    quantity = quantity,
                    unit = unit,
                    location = storageLocation,
                    verificationStatus = verificationStatus,
                    condition = condition,
                    loggedBy = receivedBy,
                    securityVerified = securityVerified,
                    photoPath = photoPath,
                    remarks = remarks
                )
            )

            // Auto-log a corresponding security gate verification event
            repository.insertSecurityLog(
                SecurityLog(
                    projectCode = projectCode,
                    gateCode = "SG-001",
                    activityType = "VEHICLE_IN",
                    description = "Verified material delivery ($materialCode, $quantity $unit) via vehicle $vehicleNumber.",
                    vehicleNumber = vehicleNumber,
                    verifiedBy = "Officer Samuel"
                )
            )
        }
    }

    fun submitMaterialIssue(
        projectCode: String,
        date: String,
        time: String,
        recipient: String,
        materialCode: String,
        quantity: Double,
        unit: String,
        storageLocation: String,
        loggedBy: String,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                MaterialTransaction(
                    projectCode = projectCode,
                    transactionType = "ISSUE",
                    date = date,
                    time = time,
                    supplierOrRecipient = recipient,
                    vehicleNumber = "N/A",
                    materialCode = materialCode,
                    quantity = quantity,
                    unit = unit,
                    location = storageLocation,
                    verificationStatus = "Fully Verified",
                    condition = "Good Condition",
                    loggedBy = loggedBy,
                    securityVerified = "Yes",
                    remarks = remarks
                )
            )
        }
    }

    fun submitStructuralUpdate(
        projectCode: String,
        elementCode: String,
        elementName: String,
        status: String,
        progressPercent: Int,
        updatedBy: String,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.insertStructuralUpdate(
                StructuralUpdate(
                    projectCode = projectCode,
                    elementCode = elementCode,
                    elementName = elementName,
                    status = status,
                    progressPercent = progressPercent,
                    updatedBy = updatedBy,
                    remarks = remarks
                )
            )
        }
    }

    fun submitEquipmentLog(
        projectCode: String,
        equipmentCode: String,
        equipmentName: String,
        status: String,
        operatorName: String,
        location: String,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.insertEquipmentLog(
                EquipmentLog(
                    projectCode = projectCode,
                    equipmentCode = equipmentCode,
                    equipmentName = equipmentName,
                    status = status,
                    operatorName = operatorName,
                    location = location,
                    remarks = remarks
                )
            )

            // If status is Breakdown, automatically log an Incident!
            if (status == "Breakdown") {
                repository.insertIncidentLog(
                    IncidentLog(
                        projectCode = projectCode,
                        type = "Equipment Breakdown",
                        description = "Equipment breakdown logged for $equipmentName ($equipmentCode). Location: $location. Status set to Breakdown.",
                        severity = "Medium",
                        responsiblePersonnel = operatorName,
                        resolutionStatus = "Open"
                    )
                )
            }
        }
    }

    fun submitSecurityLog(
        projectCode: String,
        gateCode: String,
        activityType: String,
        description: String,
        vehicleNumber: String,
        verifiedBy: String
    ) {
        viewModelScope.launch {
            repository.insertSecurityLog(
                SecurityLog(
                    projectCode = projectCode,
                    gateCode = gateCode,
                    activityType = activityType,
                    description = description,
                    vehicleNumber = vehicleNumber,
                    verifiedBy = verifiedBy
                )
            )
        }
    }

    fun submitIncidentLog(
        projectCode: String,
        type: String,
        description: String,
        severity: String,
        responsiblePersonnel: String,
        resolutionStatus: String
    ) {
        viewModelScope.launch {
            repository.insertIncidentLog(
                IncidentLog(
                    projectCode = projectCode,
                    type = type,
                    description = description,
                    severity = severity,
                    responsiblePersonnel = responsiblePersonnel,
                    resolutionStatus = resolutionStatus
                )
            )
        }
    }

    // --- Session-stored Site Progress Photos ---
    private val _siteProgressPhotos = MutableStateFlow<List<SiteProgressPhoto>>(emptyList())
    val siteProgressPhotos: StateFlow<List<SiteProgressPhoto>> = _siteProgressPhotos.asStateFlow()

    fun addProgressPhoto(
        projectCode: String,
        description: String,
        progressZone: String,
        bitmap: android.graphics.Bitmap?,
        isSimulated: Boolean = false
    ) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val timestamp = dateFormat.format(java.util.Date())
        val newPhoto = SiteProgressPhoto(
            id = java.util.UUID.randomUUID().toString(),
            projectCode = projectCode,
            timestamp = timestamp,
            description = description,
            progressZone = progressZone,
            bitmap = bitmap,
            isSimulated = isSimulated
        )
        _siteProgressPhotos.value = _siteProgressPhotos.value + newPhoto
    }

    fun deleteProgressPhoto(photoId: String) {
        _siteProgressPhotos.value = _siteProgressPhotos.value.filterNot { it.id == photoId }
    }
}

data class SiteProgressPhoto(
    val id: String,
    val projectCode: String,
    val timestamp: String,
    val description: String,
    val progressZone: String,
    val bitmap: android.graphics.Bitmap?,
    val isSimulated: Boolean = false
)

