package uk.nhs.nhsx.covid19.android.app.testordering.linktestresult

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.DidRememberOnsetSymptomsDateBeforeReceivedTestResult
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEventProcessor
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate.CannotRememberDate
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate.ExplicitDate
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate.NotStated
import uk.nhs.nhsx.covid19.android.app.testordering.ReceivedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.SymptomsDate
import uk.nhs.nhsx.covid19.android.app.testordering.UnacknowledgedTestResultsProvider
import uk.nhs.nhsx.covid19.android.app.util.SingleLiveEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

class LinkTestResultOnsetDateViewModel @Inject constructor(
    private val unacknowledgedTestResultsProvider: UnacknowledgedTestResultsProvider,
    private val analyticsEventProcessor: AnalyticsEventProcessor
) : ViewModel() {

    companion object {
        private const val MAX_DAYS_FOR_ONSET_DATE = 5L
    }

    @VisibleForTesting
    internal val viewState = MutableLiveData<ViewState>()
    fun viewState(): LiveData<ViewState> = viewState

    private val continueEvent = SingleLiveEvent<Unit>()
    fun continueEvent(): LiveData<Unit> = continueEvent

    private val datePickerContainerClickedLiveData = SingleLiveEvent<Long>()
    fun datePickerContainerClicked() = datePickerContainerClickedLiveData

    private lateinit var testResult: ReceivedTestResult

    fun onCreate(testResult: ReceivedTestResult) {
        this.testResult = testResult

        val lastPossibleOnsetDate = testResult.testEndDate.atZone(ZoneId.systemDefault()).toLocalDate()
        val firstPossibleOnsetDate = lastPossibleOnsetDate.minusDays(MAX_DAYS_FOR_ONSET_DATE)

        if (viewState.value == null) {
            viewState.postValue(
                ViewState(
                    onsetDate = NotStated,
                    showOnsetDateError = false,
                    symptomsOnsetWindowDays = firstPossibleOnsetDate..lastPossibleOnsetDate
                )
            )
        }
    }

    fun onDatePickerContainerClicked() {
        datePickerContainerClickedLiveData.postValue(testResult.testEndDate.toEpochMilli())
    }

    fun onDateSelected(dateInMillis: Long) {
        val instant: Instant = Instant.ofEpochMilli(dateInMillis)
        val localDate = instant.atZone(ZoneOffset.UTC).toLocalDate()
        val currentState = viewState.value ?: return
        val newState =
            currentState.copy(onsetDate = ExplicitDate(localDate), showOnsetDateError = false)
        viewState.postValue(newState)
    }

    fun cannotRememberDateChecked() {
        val currentState = viewState.value ?: return
        val newState = currentState.copy(onsetDate = CannotRememberDate, showOnsetDateError = false)
        viewState.postValue(newState)
    }

    fun cannotRememberDateUnchecked() {
        val currentState = viewState.value ?: return
        val newState = currentState.copy(onsetDate = NotStated, showOnsetDateError = false)
        viewState.postValue(newState)
    }

    fun onButtonContinueClicked() {
        val currentState = viewState.value ?: return
        viewModelScope.launch {
            when (val onsetDate = currentState.onsetDate) {
                is NotStated -> {
                    val newState = currentState.copy(showOnsetDateError = true)
                    viewState.postValue(newState)
                }
                is ExplicitDate, is CannotRememberDate -> {
                    if (onsetDate is ExplicitDate) {
                        analyticsEventProcessor.track(DidRememberOnsetSymptomsDateBeforeReceivedTestResult)
                    }
                    unacknowledgedTestResultsProvider.setSymptomsOnsetDate(testResult, onsetDate.toSymptomsDate())
                    continueEvent.postValue(Unit)
                }
            }
        }
    }

    fun isOnsetDateValid(
        dateInMillis: Long,
        symptomsOnsetWindowDays: ClosedRange<LocalDate>
    ): Boolean {
        val date = Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date in symptomsOnsetWindowDays
    }

    private fun SelectedDate.toSymptomsDate(): SymptomsDate =
        when (this) {
            NotStated, CannotRememberDate -> SymptomsDate(explicitDate = null)
            is ExplicitDate -> SymptomsDate(explicitDate = date)
        }

    data class ViewState(
        val onsetDate: SelectedDate,
        val showOnsetDateError: Boolean,
        val symptomsOnsetWindowDays: ClosedRange<LocalDate>
    )
}
