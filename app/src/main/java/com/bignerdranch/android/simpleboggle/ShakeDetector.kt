import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.pow
import kotlin.math.sqrt

class ShakeDetector : SensorEventListener {
    private var shakeListener: (() -> Unit)? = null
    private var lastTime: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    private val shakeThreshold = 800.0

    fun setOnShakeListener(listener: () -> Unit) {
        this.shakeListener = listener
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastTime) > 100) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = sqrt((x - lastX).toDouble().pow(2.0) + (y - lastY).toDouble().pow(2.0) + (z - lastZ).toDouble().pow(2.0)) / (currentTime - lastTime) * 10000
            if (speed > shakeThreshold) {
                shakeListener?.invoke()
            }

            lastTime = currentTime
            lastX = x
            lastY = y
            lastZ = z
        }
    }
}
