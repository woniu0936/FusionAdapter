package com.fusion.adapter.diagnostics

import android.util.Log
import com.fusion.adapter.log.FusionLogger

/**
 * [DiagnosticsPrinter]
 * Shared logic for printing Fusion Diagnostics reports.
 */
object DiagnosticsPrinter {

    fun print(tag: String, diag: FusionDiagnostics) {
        val sb = StringBuilder()
        sb.append("\n====================================================================================================\n")
        sb.append("                                      FUSION ADAPTER DIAGNOSTICS                                     \n")
        sb.append("====================================================================================================\n")
        sb.append(" Debug Mode: ${diag.isDebug}\n")
        sb.append(" Total Items: ${diag.totalItems}\n")
        sb.append(" Registered Delegates: ${diag.registeredDelegatesCount}\n")
        sb.append("----------------------------------------------------------------------------------------------------\n")
        sb.append(String.format("%-10s | %-30s | %-20s | %-6s | %-6s | %-10s\n", "ViewType", "Key", "Delegate", "Create", "Bind", "Avg Create"))
        sb.append("----------------------------------------------------------------------------------------------------\n")

        diag.delegates.forEach { d ->
            val key = if (d.viewTypeKey.length > 30) d.viewTypeKey.take(27) + "..." else d.viewTypeKey
            val delegate = if (d.delegateClass.length > 20) d.delegateClass.take(17) + "..." else d.delegateClass

            sb.append(String.format(
                "%-10d | %-30s | %-20s | %-6d | %-6d | %-8.3f ms\n",
                d.viewType, key, delegate, d.createCount, d.bindCount, d.avgCreateTimeMs
            ))
        }
        sb.append("====================================================================================================\n")
        FusionLogger.print(Log.INFO, tag, sb.toString())
    }
}