package cn.okzyl.studyjamscompose

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Quick use vibration
 * @param [timingPairs] first wait duration,second time duration.
 * @param [amplitude] Setting all timing amplitude,value must 0~255.
 * @param [amplitudes] Setting specific timing amplitude.
 * @param [repeat] value enum: -1 is no repeat;0 is repeat.
 * @return [Vibrator] use Vibrator cancel vibration.
 */
fun Context.vibrator(
    vararg timingPairs: Pair<Long, Long>,
    amplitude: Int? = null,
    amplitudes: IntArray? = null,
    repeat: Int = -1
): Vibrator? {
    val vibratorManager: VibratorManager?
    val vibrator: Vibrator?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibrator = vibratorManager?.defaultVibrator
    } else {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrator?.hasVibrator() != true) {
        return null
    }
    val timings = mutableListOf<Long>()
    timingPairs.forEach {
        timings.add(it.first)
        timings.add(it.second)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val realAmplitudes = IntArray(timings.size)
        for (i in 0 until timings.size / 2) {
            realAmplitudes[i * 2 + 1] =
                amplitudes?.get(i) ?: amplitude ?: VibrationEffect.DEFAULT_AMPLITUDE
        }
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                timings.toLongArray(),
                realAmplitudes,
                repeat
            )
        )
    } else {
        vibrator.vibrate(timings.toLongArray(), repeat)
    }
    return vibrator
}


inline fun <T> MutableList<T>.mapInPlace(mutator: (T)->T) {
    val iterate = this.listIterator()
    while (iterate.hasNext()) {
        val oldValue = iterate.next()
        val newValue = mutator(oldValue)
        if (newValue !== oldValue) {
            iterate.set(newValue)
        }
    }
}