package com.baebae.receipt.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baebae.receipt.R
import com.baebae.receipt.appContainer
import com.baebae.receipt.domain.LabelBuilder
import com.baebae.receipt.domain.Quiz
import com.baebae.receipt.ui.card.CardExportBar
import com.baebae.receipt.ui.card.CardFormat
import com.baebae.receipt.ui.card.CardTemplate
import com.baebae.receipt.ui.card.LabelCardView
import com.baebae.receipt.ui.card.ScaledCard
import com.baebae.receipt.ui.card.exportFileName
import com.baebae.receipt.ui.card.recordInto
import com.baebae.receipt.ui.card.rememberCardLayer
import com.baebae.receipt.util.WeekMath

private enum class Stage { INTRO, PRIVACY, QUIZ, RESULT }

@Composable
fun OnboardingScreen(onConnectRealData: () -> Unit, onSkip: () -> Unit) {
    var stage by rememberSaveable { mutableStateOf(Stage.INTRO) }
    var answers by rememberSaveable { mutableStateOf(IntArray(0)) }

    BackHandler(enabled = stage != Stage.INTRO) {
        when (stage) {
            Stage.PRIVACY -> stage = Stage.INTRO
            Stage.QUIZ -> if (answers.isEmpty()) stage = Stage.PRIVACY else answers = answers.copyOf(answers.size - 1)
            Stage.RESULT -> {
                answers = answers.copyOf(answers.size - 1)
                stage = Stage.QUIZ
            }
            Stage.INTRO -> Unit
        }
    }

    when (stage) {
        Stage.INTRO -> IntroPage(
            title = stringResource(R.string.onboarding_intro_title),
            body = stringResource(R.string.onboarding_intro_body),
            action = stringResource(R.string.onboarding_next),
            onAction = { stage = Stage.PRIVACY },
        )
        Stage.PRIVACY -> IntroPage(
            title = stringResource(R.string.onboarding_privacy_title),
            body = stringResource(R.string.onboarding_privacy_body),
            action = stringResource(R.string.onboarding_try_quiz),
            onAction = { stage = Stage.QUIZ },
        )
        Stage.QUIZ -> QuizPage(
            index = answers.size,
            onAnswer = { choice ->
                answers += choice
                if (answers.size == Quiz.QUESTION_COUNT) stage = Stage.RESULT
            },
        )
        Stage.RESULT -> QuizResultPage(
            answers = answers,
            onConnectRealData = onConnectRealData,
            onSkip = onSkip,
        )
    }
}

@Composable
private fun IntroPage(title: String, body: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(16.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1.4f))
        Button(onClick = onAction, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
            Text(action)
        }
    }
}

private val questionRes = intArrayOf(
    R.string.quiz_q1, R.string.quiz_q2, R.string.quiz_q3, R.string.quiz_q4, R.string.quiz_q5,
)
private val optionRes = intArrayOf(
    R.array.quiz_q1_options, R.array.quiz_q2_options, R.array.quiz_q3_options, R.array.quiz_q4_options, R.array.quiz_q5_options,
)

@Composable
private fun QuizPage(index: Int, onAnswer: (Int) -> Unit) {
    val options = stringArrayResource(optionRes[index])
    check(options.size == Quiz.optionCounts[index]) { "quiz option count mismatch at $index" }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.quiz_progress, index + 1, Quiz.QUESTION_COUNT),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = stringResource(questionRes[index]), style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(Modifier.height(4.dp))
        options.forEachIndexed { i, option ->
            OutlinedButton(
                onClick = { onAnswer(i) },
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(option, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
        }
    }
}

@Composable
private fun QuizResultPage(answers: IntArray, onConnectRealData: () -> Unit, onSkip: () -> Unit) {
    val container = appContainer()
    val productName = container.prefs.nickname.ifEmpty { stringResource(R.string.default_nickname) }
    val categoryLabels = stringArrayResource(R.array.quiz_app_labels)
    val card = remember(answers.toList(), productName) {
        val stats = Quiz.toStats(answers.toList(), WeekMath.currentWeekStart()) { categoryLabels[it.ordinal] }
        // 같은 응답이면 같은 문구가 나오도록 응답으로 시드를 만든다.
        val seed = answers.fold(17L) { acc, a -> acc * 31 + a }
        val phrases = container.phrases.preview(stats, seed)
        LabelBuilder.build(stats, productName, phrases, topAppFallback = container.phrases.topAppFallback())
    }
    val layer = rememberCardLayer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.quiz_result_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
        ScaledCard(
            format = CardFormat.STORY,
            modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp),
            contentModifier = Modifier.recordInto(layer),
        ) {
            LabelCardView(card, CardTemplate.CLASSIC, CardFormat.STORY, showWatermark = true)
        }
        CardExportBar(layer = layer, fileName = exportFileName(card, CardFormat.STORY) + "_quiz")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(text = stringResource(R.string.quiz_result_real_data_hint), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onConnectRealData, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.quiz_result_real_data))
        }
        TextButton(onClick = onSkip) { Text(stringResource(R.string.quiz_result_later)) }
    }
}
