package zed.rainxch.core.presentation.utils

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Multiplatform replacement for `"%.1f".format(value)`, which is JVM-only.
 */
fun Double.toOneDecimalString(): String {
    val scaled = (this * 10).roundToLong()
    val sign = if (scaled < 0) "-" else ""
    val absScaled = abs(scaled)
    return "$sign${absScaled / 10}.${absScaled % 10}"
}

fun formatFileSize(bytes: Long): String =
    when {
        bytes >= 1_073_741_824 -> "${(bytes / 1_073_741_824.0).toOneDecimalString()} GB"
        bytes >= 1_048_576 -> "${(bytes / 1_048_576.0).toOneDecimalString()} MB"
        bytes >= 1_024 -> "${(bytes / 1_024.0).toOneDecimalString()} KB"
        else -> "$bytes B"
    }
