package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material_transactions")
data class MaterialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectCode: String,
    val transactionType: String, // "DELIVERY" or "ISSUE"
    val date: String,
    val time: String,
    val supplierOrRecipient: String, // Supplier Name for delivery, Recipient for issue
    val vehicleNumber: String,
    val materialCode: String, // MAT-001, MAT-002, etc.
    val quantity: Double,
    val unit: String, // Bags, m³, Tonnes, etc.
    val location: String, // Cement Store, Rebar Yard, etc.
    val verificationStatus: String, // Fully Verified, Partially Verified, etc.
    val condition: String, // Good Condition, Minor Damage, etc.
    val loggedBy: String, // Storekeeper name
    val securityVerified: String, // Yes / No
    val photoPath: String? = null,
    val remarks: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "structural_progress")
data class StructuralUpdate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectCode: String,
    val elementCode: String, // ST-001, ST-002, etc.
    val elementName: String,
    val status: String, // Not Started, In Progress, Completed, Pending Inspection
    val progressPercent: Int,
    val updatedBy: String, // Engineer
    val photoPath: String? = null,
    val remarks: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "equipment_register")
data class EquipmentLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectCode: String,
    val equipmentCode: String, // EQ-001, EQ-002
    val equipmentName: String,
    val status: String, // Active, Idle, Breakdown, Under Maintenance
    val operatorName: String,
    val location: String,
    val remarks: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_logs")
data class SecurityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectCode: String,
    val gateCode: String, // SG-001, SG-002
    val activityType: String, // VEHICLE_IN, VEHICLE_OUT, PERSONNEL_IN, PERSONNEL_OUT
    val description: String,
    val vehicleNumber: String,
    val verifiedBy: String, // Security Guard Name
    val photoPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "incident_logs")
data class IncidentLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectCode: String,
    val type: String, // Unauthorised Material Movement, Material Variance, Equipment Breakdown, Security Breach, Reporting Noncompliance
    val description: String,
    val severity: String, // Low, Medium, High, Critical
    val responsiblePersonnel: String,
    val photoPath: String? = null,
    val resolutionStatus: String, // Open, Under Investigation, Resolved
    val timestamp: Long = System.currentTimeMillis()
)
