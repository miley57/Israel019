package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatisDao {

    // --- Material Transactions ---
    @Query("SELECT * FROM material_transactions WHERE projectCode = :projectCode ORDER BY timestamp DESC")
    fun getMaterialTransactions(projectCode: String): Flow<List<MaterialTransaction>>

    @Query("SELECT * FROM material_transactions ORDER BY timestamp DESC")
    fun getAllMaterialTransactions(): Flow<List<MaterialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialTransaction(transaction: MaterialTransaction)


    // --- Structural Progress ---
    @Query("SELECT * FROM structural_progress WHERE projectCode = :projectCode ORDER BY timestamp DESC")
    fun getStructuralUpdates(projectCode: String): Flow<List<StructuralUpdate>>

    @Query("SELECT * FROM structural_progress ORDER BY timestamp DESC")
    fun getAllStructuralUpdates(): Flow<List<StructuralUpdate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStructuralUpdate(update: StructuralUpdate)


    // --- Equipment Log ---
    @Query("SELECT * FROM equipment_register WHERE projectCode = :projectCode ORDER BY timestamp DESC")
    fun getEquipmentLogs(projectCode: String): Flow<List<EquipmentLog>>

    @Query("SELECT * FROM equipment_register ORDER BY timestamp DESC")
    fun getAllEquipmentLogs(): Flow<List<EquipmentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentLog(log: EquipmentLog)


    // --- Security Logs ---
    @Query("SELECT * FROM security_logs WHERE projectCode = :projectCode ORDER BY timestamp DESC")
    fun getSecurityLogs(projectCode: String): Flow<List<SecurityLog>>

    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC")
    fun getAllSecurityLogs(): Flow<List<SecurityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLog)


    // --- Incident Logs ---
    @Query("SELECT * FROM incident_logs WHERE projectCode = :projectCode ORDER BY timestamp DESC")
    fun getIncidentLogs(projectCode: String): Flow<List<IncidentLog>>

    @Query("SELECT * FROM incident_logs ORDER BY timestamp DESC")
    fun getAllIncidentLogs(): Flow<List<IncidentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidentLog(log: IncidentLog)
}
