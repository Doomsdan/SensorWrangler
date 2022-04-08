package me.danielschaefer.sensorwrangler.data

// TODO: Maybe use Instant instead of Long
// TODO: Allow other value instead of Double. Some measurements are not numeric
data class DataPoint(var timestamp: Long, val value: Double)
