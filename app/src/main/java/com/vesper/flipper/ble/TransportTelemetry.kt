package com.vesper.flipper.ble

data class TransportTelemetry(
    val transportLabel: String = "None",
    val writeCharacteristicUuid: String? = null,
    val notifyCharacteristicUuid: String? = null,
    val lastWriteTypeLabel: String? = null,
    val lastWriteFailure: String? = null,
    val writeRouteVerified: Boolean = false,
    val lastRxSummary: String? = null,
    val lastUpdatedAtMs: Long = 0L
) {
    companion object {
        fun idle(): TransportTelemetry = TransportTelemetry()
    }
}
