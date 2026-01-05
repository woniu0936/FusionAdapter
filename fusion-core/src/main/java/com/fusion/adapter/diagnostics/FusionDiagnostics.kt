package com.fusion.adapter.diagnostics

/**
 * [FusionDiagnostics]
 * Snapshot of the FusionAdapter's current state and performance metrics.
 */
data class FusionDiagnostics(
    val timestamp: Long = System.currentTimeMillis(),
    val isDebug: Boolean,
    val totalItems: Int,
    val registeredDelegatesCount: Int,
    val delegates: List<DelegateDiagnostic>
)

/**
 * [DelegateDiagnostic]
 * Detailed metrics for a specific registered Delegate.
 */
data class DelegateDiagnostic(
    val viewType: Int,
    val viewTypeKey: String,
    val delegateClass: String,
    val createCount: Long,
    val bindCount: Long,
    val avgCreateTimeMs: Double,
    val totalCreateTimeMs: Double
)
