package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CatisRepository(private val catisDao: CatisDao) {

    // --- Flows ---
    val allTransactions: Flow<List<MaterialTransaction>> = catisDao.getAllMaterialTransactions()
    val allStructuralUpdates: Flow<List<StructuralUpdate>> = catisDao.getAllStructuralUpdates()
    val allEquipmentLogs: Flow<List<EquipmentLog>> = catisDao.getAllEquipmentLogs()
    val allSecurityLogs: Flow<List<SecurityLog>> = catisDao.getAllSecurityLogs()
    val allIncidentLogs: Flow<List<IncidentLog>> = catisDao.getAllIncidentLogs()

    fun getTransactionsForProject(projectCode: String): Flow<List<MaterialTransaction>> =
        catisDao.getMaterialTransactions(projectCode)

    fun getStructuralUpdatesForProject(projectCode: String): Flow<List<StructuralUpdate>> =
        catisDao.getStructuralUpdates(projectCode)

    fun getEquipmentLogsForProject(projectCode: String): Flow<List<EquipmentLog>> =
        catisDao.getEquipmentLogs(projectCode)

    fun getSecurityLogsForProject(projectCode: String): Flow<List<SecurityLog>> =
        catisDao.getSecurityLogs(projectCode)

    fun getIncidentLogsForProject(projectCode: String): Flow<List<IncidentLog>> =
        catisDao.getIncidentLogs(projectCode)


    // --- Inserts ---
    suspend fun insertTransaction(transaction: MaterialTransaction) =
        catisDao.insertMaterialTransaction(transaction)

    suspend fun insertStructuralUpdate(update: StructuralUpdate) =
        catisDao.insertStructuralUpdate(update)

    suspend fun insertEquipmentLog(log: EquipmentLog) =
        catisDao.insertEquipmentLog(log)

    suspend fun insertSecurityLog(log: SecurityLog) =
        catisDao.insertSecurityLog(log)

    suspend fun insertIncidentLog(log: IncidentLog) =
        catisDao.insertIncidentLog(log)


    // --- Seeding helper ---
    suspend fun seedDatabaseIfEmpty() {
        // If no transactions exist, seed the DB with realistic CATIS records
        val txs = allTransactions.first()
        if (txs.isEmpty()) {
            // Seed Project 1 (CATIS-P001_LMT_WAREHOUSE)
            catisDao.insertMaterialTransaction(
                MaterialTransaction(
                    projectCode = "CATIS-P001",
                    transactionType = "DELIVERY",
                    date = "2026-07-15",
                    time = "09:45 AM",
                    supplierOrRecipient = "Dangote Cement",
                    vehicleNumber = "GGE-245XA",
                    materialCode = "MAT-001 Cement",
                    quantity = 300.0,
                    unit = "Bags",
                    location = "Cement Store",
                    verificationStatus = "Fully Verified",
                    condition = "Good Condition",
                    loggedBy = "Ibrahim Musa",
                    securityVerified = "Yes",
                    remarks = "Official batch delivery, stock level increased."
                )
            )

            catisDao.insertMaterialTransaction(
                MaterialTransaction(
                    projectCode = "CATIS-P001",
                    transactionType = "DELIVERY",
                    date = "2026-07-16",
                    time = "11:30 AM",
                    supplierOrRecipient = "XYZ Aggregates",
                    vehicleNumber = "KJA-832XB",
                    materialCode = "MAT-002 Granite",
                    quantity = 45.0,
                    unit = "Tonnes",
                    location = "Aggregate Zone",
                    verificationStatus = "Fully Verified",
                    condition = "Good Condition",
                    loggedBy = "Ibrahim Musa",
                    securityVerified = "Yes",
                    remarks = "High quality sharp aggregates."
                )
            )

            catisDao.insertMaterialTransaction(
                MaterialTransaction(
                    projectCode = "CATIS-P001",
                    transactionType = "ISSUE",
                    date = "2026-07-16",
                    time = "02:00 PM",
                    supplierOrRecipient = "Section-A Foundations",
                    vehicleNumber = "N/A",
                    materialCode = "MAT-001 Cement",
                    quantity = 50.0,
                    unit = "Bags",
                    location = "Cement Store",
                    verificationStatus = "Fully Verified",
                    condition = "Good Condition",
                    loggedBy = "Ibrahim Musa",
                    securityVerified = "Yes",
                    remarks = "Issued 50 bags for ST-001 foundation casting."
                )
            )

            // Seed Structural Progress
            catisDao.insertStructuralUpdate(
                StructuralUpdate(
                    projectCode = "CATIS-P001",
                    elementCode = "ST-001",
                    elementName = "Foundation Section",
                    status = "In Progress",
                    progressPercent = 45,
                    updatedBy = "Engr. David Okoye",
                    remarks = "Concrete pouring in progress, reinforcement completed."
                )
            )

            catisDao.insertStructuralUpdate(
                StructuralUpdate(
                    projectCode = "CATIS-P001",
                    elementCode = "ST-002",
                    elementName = "Ground Floor Columns",
                    status = "Not Started",
                    progressPercent = 0,
                    updatedBy = "Engr. David Okoye",
                    remarks = "Awaiting foundation completion."
                )
            )

            // Seed Equipment Register
            catisDao.insertEquipmentLog(
                EquipmentLog(
                    projectCode = "CATIS-P001",
                    equipmentCode = "EQ-001",
                    equipmentName = "Caterpillar Excavator",
                    status = "Active",
                    operatorName = "Kabiru Sani",
                    location = "Zone 2 Foundation Excavation",
                    remarks = "Performing excavation tasks without issues."
                )
            )

            catisDao.insertEquipmentLog(
                EquipmentLog(
                    projectCode = "CATIS-P001",
                    equipmentCode = "EQ-002",
                    equipmentName = "Concrete Mixer Truck",
                    status = "Idle",
                    operatorName = "Amos Johnson",
                    location = "Batching Yard",
                    remarks = "Idle, awaiting next pouring schedule."
                )
            )

            // Seed Security Log
            catisDao.insertSecurityLog(
                SecurityLog(
                    projectCode = "CATIS-P001",
                    gateCode = "SG-001",
                    activityType = "VEHICLE_IN",
                    description = "Dangote Cement Delivery (GGE-245XA)",
                    vehicleNumber = "GGE-245XA",
                    verifiedBy = "Officer Samuel"
                )
            )

            catisDao.insertSecurityLog(
                SecurityLog(
                    projectCode = "CATIS-P001",
                    gateCode = "SG-001",
                    activityType = "VEHICLE_OUT",
                    description = "Dangote Truck Departed (GGE-245XA)",
                    vehicleNumber = "GGE-245XA",
                    verifiedBy = "Officer Samuel"
                )
            )

            // Seed Incident Log
            catisDao.insertIncidentLog(
                IncidentLog(
                    projectCode = "CATIS-P001",
                    type = "Material Variance",
                    description = "Shortage of 2 bags of Cement on delivery GGE-245XA. Claim filed with Lafarge.",
                    severity = "Medium",
                    responsiblePersonnel = "Ibrahim Musa",
                    resolutionStatus = "Under Investigation"
                )
            )

            catisDao.insertIncidentLog(
                IncidentLog(
                    projectCode = "CATIS-P001",
                    type = "Security Breach",
                    description = "Unidentified vehicle attempted entry at gate SG-002. Logged and turned away.",
                    severity = "High",
                    responsiblePersonnel = "Officer Samuel",
                    resolutionStatus = "Resolved"
                )
            )
        }
    }
}
