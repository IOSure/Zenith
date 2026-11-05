package com.etrisad.zenith.ui.components.pausepoint

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.etrisad.zenith.R
import kotlin.random.Random

enum class PausePointTaskType(
    val displayName: String,
    val description: String,
    @StringRes val displayNameRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector
) {
    WAITING(
        "Waiting", "Wait for a set duration before continuing",
        R.string.pause_task_waiting_name, R.string.pause_task_waiting_desc,
        Icons.Outlined.AccessTime
    ),
    BREATHING(
        "Breathing", "Follow a breathing exercise",
        R.string.pause_task_breathing_name, R.string.pause_task_breathing_desc,
        Icons.Outlined.Nature
    ),
    WALK(
        "Walk", "Walk a certain number of steps",
        R.string.pause_task_walk_name, R.string.pause_task_walk_desc,
        Icons.AutoMirrored.Outlined.DirectionsWalk
    ),
    QR_SCAN(
        "QR Scan", "Scan a QR code",
        R.string.pause_task_qr_scan_name, R.string.pause_task_qr_scan_desc,
        Icons.Outlined.QrCodeScanner
    ),
    NFC_SCAN(
        "NFC Scan", "Scan a registered NFC tag",
        R.string.pause_task_nfc_scan_name, R.string.pause_task_nfc_scan_desc,
        Icons.Outlined.Nfc
    ),
    NUMBER_SLIDE(
        "Number Slide", "Slide the tiles to solve the puzzle",
        R.string.pause_task_number_slide_name, R.string.pause_task_number_slide_desc,
        Icons.Outlined.GridView
    ),
    SWITCH(
        "Switch Puzzle", "Match the switch sequence",
        R.string.pause_task_switch_name, R.string.pause_task_switch_desc,
        Icons.Outlined.ToggleOn
    ),
    COUNTING(
        "Counting", "Complete a counting exercise",
        R.string.pause_task_counting_name, R.string.pause_task_counting_desc,
        Icons.Outlined.FitnessCenter
    ),
    TYPING(
        "Typing", "Type a specific text correctly",
        R.string.pause_task_typing_name, R.string.pause_task_typing_desc,
        Icons.Outlined.Keyboard
    ),
    MATH(
        "Math", "Solve a quick math problem",
        R.string.pause_task_math_name, R.string.pause_task_math_desc,
        Icons.Outlined.Calculate
    ),
    CHOOSE_APP(
        "Choose App", "Open a suggested goal app",
        R.string.pause_task_choose_app_name, R.string.pause_task_choose_app_desc,
        Icons.Outlined.TouchApp
    )
}

sealed class PausePointTask {
    abstract val type: PausePointTaskType

    data class Waiting(
        val durationSeconds: Int = 15
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.WAITING
    }

    data class Breathing(
        val rounds: Int = 3
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.BREATHING
    }

    data class Walk(
        val steps: Int = 10
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.WALK
    }

    data class QrScan(
        val code: String = "PAUSE-${Random.nextInt(100000, 999999)}",
        val validCodes: List<String> = emptyList(),
        val acceptAny: Boolean = false
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.QR_SCAN
    }

    data class NfcScan(
        val validTagIds: List<String> = emptyList(),
        val acceptAny: Boolean = false
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.NFC_SCAN
    }

    data class NumberSlide(
        val size: Int = 3
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.NUMBER_SLIDE
    }

    data class Switch(
        val leverCount: Int = 4,
        val timeoutSeconds: Int? = null
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.SWITCH
    }

    data class Math(
        val maxOperand: Int = 20,
        val operator: MathOperator = MathOperator.ADD
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.MATH
    }

    data class Counting(
        val targetNumber: Int = Random.nextInt(5, 21),
        val label: String = ""
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.COUNTING
    }

    data class Typing(
        val sentence: String = sentencePool.random()
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.TYPING
    }

    data class ChooseApp(
        val suggestedPackage: String = "",
        val suggestedAppName: String = FALLBACK_APP_NAME
    ) : PausePointTask() {
        override val type get() = PausePointTaskType.CHOOSE_APP
    }

    companion object {
        /**
         * Sentinel name used when no goal app is available. Display code replaces it with
         * a localized string instead of showing the English default.
         */
        const val FALLBACK_APP_NAME = "a productive app"

        val sentencePool = listOf(
            "Stay focused and mindful.",
            "Small steps lead to big changes.",
            "Every moment is a fresh beginning.",
            "Progress not perfection.",
            "Be present in this moment.",
            "You are capable of amazing things.",
            "Focus on what matters most.",
            "One task at a time."
        )

        /** Resource ids of [sentencePool], in the same order, for localized display. */
        val sentencePoolRes: List<Int> = listOf(
            R.string.pause_task_sentence_1,
            R.string.pause_task_sentence_2,
            R.string.pause_task_sentence_3,
            R.string.pause_task_sentence_4,
            R.string.pause_task_sentence_5,
            R.string.pause_task_sentence_6,
            R.string.pause_task_sentence_7,
            R.string.pause_task_sentence_8
        )
    }
}

/**
 * Localized instruction shown above a pause point task. Kept out of the sealed class because
 * resolving @StringRes needs a composable context.
 */
@Composable
fun PausePointTask.localizedInstruction(): String = when (this) {
    is PausePointTask.Waiting ->
        stringResource(R.string.pause_task_instruction_waiting, durationSeconds)
    is PausePointTask.Breathing ->
        stringResource(R.string.pause_task_instruction_breathing, rounds)
    is PausePointTask.Walk ->
        stringResource(R.string.pause_task_instruction_walk, steps)
    is PausePointTask.QrScan ->
        if (validCodes.isNotEmpty()) stringResource(R.string.pause_task_instruction_qr_saved)
        else stringResource(R.string.pause_task_instruction_qr_any)
    is PausePointTask.NfcScan ->
        if (validTagIds.isNotEmpty()) stringResource(R.string.pause_task_instruction_nfc_saved)
        else stringResource(R.string.pause_task_instruction_nfc_any)
    is PausePointTask.NumberSlide ->
        stringResource(R.string.pause_task_instruction_number_slide)
    is PausePointTask.Switch ->
        stringResource(R.string.pause_task_instruction_switch)
    is PausePointTask.Math ->
        stringResource(R.string.pause_task_instruction_math)
    is PausePointTask.Counting ->
        if (label.isNotEmpty()) stringResource(
            R.string.pause_task_instruction_counting_named, targetNumber, label
        ) else stringResource(R.string.pause_task_instruction_counting, targetNumber)
    is PausePointTask.Typing ->
        stringResource(R.string.pause_task_instruction_typing)
    is PausePointTask.ChooseApp ->
        stringResource(R.string.pause_task_instruction_choose_app, localizedAppName())
}

/** Display name of the suggested goal app, localized when the task only carries the fallback. */
@Composable
fun PausePointTask.ChooseApp.localizedAppName(): String =
    if (suggestedAppName == PausePointTask.FALLBACK_APP_NAME) {
        stringResource(R.string.pause_task_choose_app_fallback)
    } else {
        suggestedAppName
    }

/**
 * Sentence to display and type for the Typing task: default pool sentences are shown in the
 * current language, custom sentences are shown exactly as the user entered them.
 */
@Composable
fun PausePointTask.Typing.localizedSentence(): String {
    val index = PausePointTask.sentencePool.indexOf(sentence)
    return if (index >= 0) stringResource(PausePointTask.sentencePoolRes[index]) else sentence
}

enum class MathOperator(val symbol: String, val displaySymbol: String) {
    ADD("+", "+"),
    SUBTRACT("-", "−"),
    MULTIPLY("*", "×"),
    DIVIDE(":", "÷");

    fun apply(a: Int, b: Int): Int = when (this) {
        ADD -> a + b
        SUBTRACT -> a - b
        MULTIPLY -> a * b
        DIVIDE -> if (b != 0) a / b else 0
    }

    companion object {
        fun fromSymbol(raw: String?): MathOperator {
            if (raw.isNullOrBlank()) return ADD
            val s = raw.trim()
            return when (s) {
                "+", "＋" -> ADD
                "-", "−", "–", "—" -> SUBTRACT
                "*", "x", "X", "×", "✕", "·" -> MULTIPLY
                "/", ":", "÷", "／" -> DIVIDE
                else -> ADD
            }
        }

        fun normalize(raw: String?): String = fromSymbol(raw).symbol
    }
}

data class PausePointVariant(
    val label: String = "",
    val text: String = "",
    val seconds: Int = 0,
    val rounds: Int = 0,
    val steps: Int = 0,
    val size: Int = 0,
    val levers: Int = 0,
    val maxOperand: Int = 0,
    val target: Int = 0,
    val mathOperator: String = "+"
) {
    val resolvedMathOperator: MathOperator get() = MathOperator.fromSymbol(mathOperator)
}

data class PausePointConfig(
    val waitingVariants: List<PausePointVariant> = PausePointDefaults.waitingVariants,
    val breathingVariants: List<PausePointVariant> = PausePointDefaults.breathingVariants,
    val walkVariants: List<PausePointVariant> = PausePointDefaults.walkVariants,
    val numberSlideVariants: List<PausePointVariant> = PausePointDefaults.numberSlideVariants,
    val switchVariants: List<PausePointVariant> = PausePointDefaults.switchVariants,
    val mathVariants: List<PausePointVariant> = PausePointDefaults.mathVariants,
    val countingVariants: List<PausePointVariant> = PausePointDefaults.countingVariants,
    val typingVariants: List<PausePointVariant> = PausePointDefaults.typingVariants
) {
    fun variantsFor(type: PausePointTaskType): List<PausePointVariant> = when (type) {
        PausePointTaskType.WAITING -> waitingVariants
        PausePointTaskType.BREATHING -> breathingVariants
        PausePointTaskType.WALK -> walkVariants
        PausePointTaskType.NUMBER_SLIDE -> numberSlideVariants
        PausePointTaskType.SWITCH -> switchVariants
        PausePointTaskType.MATH -> mathVariants
        PausePointTaskType.COUNTING -> countingVariants
        PausePointTaskType.TYPING -> typingVariants
        PausePointTaskType.QR_SCAN -> emptyList()
        PausePointTaskType.NFC_SCAN -> emptyList()
        PausePointTaskType.CHOOSE_APP -> emptyList()
    }
}

object PausePointDefaults {
    val waitingVariants = listOf(PausePointVariant(seconds = 15))
    val breathingVariants = listOf(PausePointVariant(rounds = 3))
    val walkVariants = listOf(PausePointVariant(steps = 10))
    val numberSlideVariants = listOf(PausePointVariant(size = 3))
    val switchVariants = listOf(PausePointVariant(levers = 4))
    val mathVariants = listOf(
        PausePointVariant(maxOperand = 20, mathOperator = "+"),
        PausePointVariant(maxOperand = 20, mathOperator = "-"),
        PausePointVariant(maxOperand = 12, mathOperator = "*"),
        PausePointVariant(maxOperand = 12, mathOperator = ":")
    )
    val countingVariants = listOf(PausePointVariant(target = 15))
    val typingVariants = PausePointTask.sentencePool.map { PausePointVariant(text = it) }

    fun variantsFor(type: PausePointTaskType): List<PausePointVariant> = when (type) {
        PausePointTaskType.WAITING -> waitingVariants
        PausePointTaskType.BREATHING -> breathingVariants
        PausePointTaskType.WALK -> walkVariants
        PausePointTaskType.NUMBER_SLIDE -> numberSlideVariants
        PausePointTaskType.SWITCH -> switchVariants
        PausePointTaskType.MATH -> mathVariants
        PausePointTaskType.COUNTING -> countingVariants
        PausePointTaskType.TYPING -> typingVariants
        PausePointTaskType.QR_SCAN -> emptyList()
        PausePointTaskType.NFC_SCAN -> emptyList()
        PausePointTaskType.CHOOSE_APP -> emptyList()
    }
}

object PausePointEngine {

    fun normalizeNfcId(raw: String): String =
        raw.trim().uppercase().replace(":", "").replace(" ", "").replace("-", "")

    fun hasUsableQrCodes(qrCodes: List<String>): Boolean =
        qrCodes.any { it.trim().isNotEmpty() }

    fun hasUsableNfcTags(nfcTagIds: List<String>): Boolean =
        nfcTagIds.any { normalizeNfcId(it).isNotEmpty() }

    private fun pickVariant(variants: List<PausePointVariant>, fallback: List<PausePointVariant>): PausePointVariant {
        val pool = variants.ifEmpty { fallback }
        return if (pool.isNotEmpty()) pool.random() else PausePointVariant()
    }

    fun isTypeAvailable(
        type: PausePointTaskType,
        qrCodes: List<String> = emptyList(),
        nfcTagIds: List<String> = emptyList(),
        goalPackageNames: Set<String> = emptySet(),
        cameraGranted: Boolean = true,
        nfcAvailable: Boolean = true
    ): Boolean = when (type) {
        PausePointTaskType.QR_SCAN -> hasUsableQrCodes(qrCodes) && cameraGranted
        PausePointTaskType.NFC_SCAN -> hasUsableNfcTags(nfcTagIds) && nfcAvailable
        PausePointTaskType.CHOOSE_APP -> goalPackageNames.isNotEmpty()
        else -> true
    }

    fun availableAlternatives(
        currentType: PausePointTaskType,
        enabledTypes: Set<PausePointTaskType>,
        qrCodes: List<String> = emptyList(),
        nfcTagIds: List<String> = emptyList(),
        goalPackageNames: Set<String> = emptySet(),
        cameraGranted: Boolean = true,
        nfcAvailable: Boolean = true
    ): List<PausePointTaskType> = enabledTypes
        .filter { it != currentType }
        .filter {
            isTypeAvailable(
                type = it,
                qrCodes = qrCodes,
                nfcTagIds = nfcTagIds,
                goalPackageNames = goalPackageNames,
                cameraGranted = cameraGranted,
                nfcAvailable = nfcAvailable
            )
        }

    fun generateTask(
        enabledTypes: Set<PausePointTaskType> = PausePointTaskType.entries.toSet(),
        goalPackageNames: Set<String> = emptySet(),
        goalAppNames: Map<String, String> = emptyMap(),
        qrCodes: List<String> = emptyList(),
        nfcTagIds: List<String> = emptyList(),
        config: PausePointConfig = PausePointConfig(),
        cameraGranted: Boolean = true,
        nfcAvailable: Boolean = true
    ): PausePointTask {
        val filteredTypes = enabledTypes
            .filter { type ->
                when (type) {
                    PausePointTaskType.QR_SCAN -> hasUsableQrCodes(qrCodes) && cameraGranted
                    PausePointTaskType.NFC_SCAN -> hasUsableNfcTags(nfcTagIds) && nfcAvailable
                    PausePointTaskType.CHOOSE_APP -> goalPackageNames.isNotEmpty()
                    else -> true
                }
            }
            .toList()
        if (filteredTypes.isEmpty()) return PausePointTask.Waiting()

        val selectedType = filteredTypes.random()

        return when (selectedType) {
            PausePointTaskType.WAITING -> PausePointTask.Waiting(
                durationSeconds = pickVariant(config.waitingVariants, PausePointDefaults.waitingVariants).seconds.coerceAtLeast(1)
            )
            PausePointTaskType.BREATHING -> PausePointTask.Breathing(
                rounds = pickVariant(config.breathingVariants, PausePointDefaults.breathingVariants).rounds.coerceAtLeast(1)
            )
            PausePointTaskType.WALK -> PausePointTask.Walk(
                steps = pickVariant(config.walkVariants, PausePointDefaults.walkVariants).steps.coerceAtLeast(1)
            )
            PausePointTaskType.QR_SCAN -> PausePointTask.QrScan(
                code = if (qrCodes.isNotEmpty()) qrCodes.random() else "PAUSE-${Random.nextInt(100000, 999999)}",
                validCodes = qrCodes
            )
            PausePointTaskType.NFC_SCAN -> PausePointTask.NfcScan(
                validTagIds = nfcTagIds
            )
            PausePointTaskType.NUMBER_SLIDE -> PausePointTask.NumberSlide(
                size = pickVariant(config.numberSlideVariants, PausePointDefaults.numberSlideVariants).size.coerceAtLeast(3)
            )
            PausePointTaskType.SWITCH -> {
                val v = pickVariant(config.switchVariants, PausePointDefaults.switchVariants)
                PausePointTask.Switch(
                    leverCount = v.levers.coerceAtLeast(2),
                    timeoutSeconds = v.seconds.takeIf { it > 0 }
                )
            }
            PausePointTaskType.MATH -> {
                val v = pickVariant(config.mathVariants, PausePointDefaults.mathVariants)
                PausePointTask.Math(
                    maxOperand = v.maxOperand.coerceAtLeast(1),
                    operator = MathOperator.fromSymbol(v.mathOperator)
                )
            }
            PausePointTaskType.COUNTING -> {
                val v = pickVariant(config.countingVariants, PausePointDefaults.countingVariants)
                PausePointTask.Counting(
                    targetNumber = v.target.coerceAtLeast(1),
                    label = v.label
                )
            }
            PausePointTaskType.TYPING -> {
                val text = pickVariant(config.typingVariants, PausePointDefaults.typingVariants).text
                PausePointTask.Typing(
                    sentence = text.ifBlank { PausePointTask.sentencePool.random() }
                )
            }
            PausePointTaskType.CHOOSE_APP -> {
                if (goalPackageNames.isNotEmpty()) {
                    val randomGoal = goalPackageNames.random()
                    PausePointTask.ChooseApp(
                        suggestedPackage = randomGoal,
                        suggestedAppName = goalAppNames[randomGoal] ?: PausePointTask.FALLBACK_APP_NAME
                    )
                } else {
                    PausePointTask.Waiting()
                }
            }
        }
    }
}
