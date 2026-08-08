package com.example.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الاختبارات التفاعلية", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (uiState.quizState) {
                QuizState.SETUP -> QuizSetupView(viewModel = viewModel, uiState = uiState)
                QuizState.ACTIVE -> QuizActiveView(viewModel = viewModel, uiState = uiState)
                QuizState.COMPLETED -> QuizCompletedView(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

@Composable
private fun QuizSetupView(
    viewModel: QuizViewModel,
    uiState: QuizUiState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "اختر نوع الاختبار ومستواه",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        // Quiz Type Cards
        Text(text = "نوع الاختبار:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuizTypeOptionCard(
                title = "اختيار من متعدد",
                icon = Icons.Default.Quiz,
                isSelected = uiState.quizType == QuizType.MCQ,
                onClick = { viewModel.setQuizType(QuizType.MCQ) },
                modifier = Modifier.weight(1f),
                testTag = "quiz_type_mcq"
            )
            QuizTypeOptionCard(
                title = "كتابة الكلمة",
                icon = Icons.Default.Edit,
                isSelected = uiState.quizType == QuizType.SPELLING,
                onClick = { viewModel.setQuizType(QuizType.SPELLING) },
                modifier = Modifier.weight(1f),
                testTag = "quiz_type_spelling"
            )
            QuizTypeOptionCard(
                title = "استماع واختيار",
                icon = Icons.Default.VolumeUp,
                isSelected = uiState.quizType == QuizType.LISTENING,
                onClick = { viewModel.setQuizType(QuizType.LISTENING) },
                modifier = Modifier.weight(1f),
                testTag = "quiz_type_listening"
            )
        }

        // Level Selector
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "المستوى التعليمي:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val levels = listOf("ALL" to "الكل", "A1-A2" to "مبتدئ", "B1-B2" to "متوسط", "C1-C2" to "متقدم")
            levels.forEach { (code, label) ->
                FilterChip(
                    selected = uiState.selectedLevel == code,
                    onClick = { viewModel.setSelectedLevel(code) },
                    label = { Text(label, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Question Count Selector
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "عدد الأسئلة:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(5, 10, 15).forEach { count ->
                FilterChip(
                    selected = uiState.questionCount == count,
                    onClick = { viewModel.setQuestionCount(count) },
                    label = { Text("$count أسئلة", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.startQuiz() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_quiz_submit_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ابدأ الاختبار الآن", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun QuizTypeOptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuizActiveView(
    viewModel: QuizViewModel,
    uiState: QuizUiState
) {
    val question = uiState.questions.getOrNull(uiState.currentQuestionIndex) ?: return

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "السؤال ${uiState.currentQuestionIndex + 1} من ${uiState.questions.size}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "النتيجة: ${uiState.score}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        LinearProgressIndicator(
            progress = { (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        // Question Display Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState.quizType) {
                    QuizType.MCQ -> {
                        Text(
                            text = "ما هو معنى الكلمة التالية؟",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = question.word.englishWord,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(onClick = { viewModel.playCurrentAudio() }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    QuizType.SPELLING -> {
                        Text(
                            text = "اكتب الكلمة بالإنجليزية للترجمة التالية:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = question.word.arabicTranslation,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "توضيح: ${question.word.exampleTranslation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    QuizType.LISTENING -> {
                        Text(
                            text = "استمع للكلمة واختر الترجمة الصحيحة:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalIconButton(
                            onClick = { viewModel.playCurrentAudio() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio", modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "اضغط للاستماع مجدداً 🔊", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Answers Input or Options List
        if (uiState.quizType == QuizType.SPELLING) {
            OutlinedTextField(
                value = uiState.textAnswerInput,
                onValueChange = { viewModel.onTextInputChanged(it) },
                label = { Text("اكتب الكلمة هنا بالإنجليزية") },
                enabled = !uiState.isAnswerSubmitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spelling_text_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEach { option ->
                    val isSelected = uiState.selectedAnswer == option
                    val isCorrectChoice = option == question.correctAnswer
                    val buttonColor = when {
                        !uiState.isAnswerSubmitted -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        isCorrectChoice -> Color(0xFFC8E6C9) // Green light
                        isSelected && !isCorrectChoice -> Color(0xFFFFCDD2) // Red light
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = buttonColor),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isAnswerSubmitted) {
                                viewModel.onOptionSelected(option)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (uiState.isAnswerSubmitted) {
                                if (isCorrectChoice) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Answer Feedback Message
        if (uiState.isAnswerSubmitted) {
            val isCorrect = if (uiState.quizType == QuizType.SPELLING) {
                uiState.textAnswerInput.trim().equals(question.correctAnswer, ignoreCase = true)
            } else {
                uiState.selectedAnswer == question.correctAnswer
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCorrect) "إجابة صحيحة! ممتاز 👏" else "إجابة خاطئة. الكلمة الصحيحة هي: ${question.word.englishWord} (${question.correctAnswer})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button: Submit or Next Question
        if (!uiState.isAnswerSubmitted) {
            Button(
                onClick = { viewModel.submitAnswer() },
                enabled = if (uiState.quizType == QuizType.SPELLING) uiState.textAnswerInput.isNotBlank() else uiState.selectedAnswer != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_answer_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("تأكيد الإجابة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        } else {
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_question_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (uiState.currentQuestionIndex + 1 < uiState.questions.size) "السؤال التالي ➔" else "عرض النتيجة النهائية 🏆",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun QuizCompletedView(
    viewModel: QuizViewModel,
    uiState: QuizUiState
) {
    val total = uiState.questions.size
    val percentage = if (total > 0) (uiState.score.toFloat() / total * 100).toInt() else 0

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "اكتمل الاختبار بنجاح!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الدرجة النهائية: %$percentage",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "أجبت بشكل صحيح على ${uiState.score} من أصل $total سؤالاً",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "تفاصيل الإجابات:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.userAnswers) { res ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = res.question.word.englishWord,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "الترجمة: ${res.question.word.arabicTranslation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "إجابتك: ${res.userAnswer.ifBlank { "لم تُجب" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (res.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }

                        Icon(
                            imageVector = if (res.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (res.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.resetQuizToSetup() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("إعداد اختبار جديد")
            }

            Button(
                onClick = { viewModel.startQuiz() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("إعادة الاختبار")
            }
        }
    }
}
