package uk.nhs.nhsx.covid19.android.app.testordering.linktestresult

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEvent.DidRememberOnsetSymptomsDateBeforeReceivedTestResult
import uk.nhs.nhsx.covid19.android.app.analytics.AnalyticsEventProcessor
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate.CannotRememberDate
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate.ExplicitDate
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.SelectedDate.NotStated
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.LAB_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.POSITIVE
import uk.nhs.nhsx.covid19.android.app.testordering.ReceivedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.SymptomsDate
import uk.nhs.nhsx.covid19.android.app.testordering.UnacknowledgedTestResultsProvider
import uk.nhs.nhsx.covid19.android.app.testordering.linktestresult.LinkTestResultOnsetDateViewModel.ViewState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkTestResultOnsetDateViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val unacknowledgedTestResultsProvider = mockk<UnacknowledgedTestResultsProvider>(relaxed = true)
    private val analyticsEventProcessor = mockk<AnalyticsEventProcessor>(relaxed = true)

    private val testEndDate = Instant.parse("2020-07-27T01:00:00.00Z")
    private val testResult = ReceivedTestResult(
        diagnosisKeySubmissionToken = "submissionToken",
        testEndDate = testEndDate,
        testResult = POSITIVE,
        testKitType = LAB_RESULT,
        diagnosisKeySubmissionSupported = false,
        requiresConfirmatoryTest = false
    )
    private val symptomsOnsetWindowDays = LocalDate.parse("2020-07-22")..LocalDate.parse("2020-07-27")

    private val testSubject = LinkTestResultOnsetDateViewModel(unacknowledgedTestResultsProvider, analyticsEventProcessor)

    private val viewStateObserver = mockk<Observer<ViewState>>(relaxed = true)
    private val datePickerContainerClickedObserver = mockk<Observer<Long>>(relaxed = true)
    private val continueEventObserver = mockk<Observer<Unit>>(relaxed = true)

    @Before
    fun setUp() {
        testSubject.viewState().observeForever(viewStateObserver)
        testSubject.datePickerContainerClicked().observeForever(datePickerContainerClickedObserver)
        testSubject.continueEvent().observeForever(continueEventObserver)
        testSubject.onCreate(testResult)
    }

    @Test
    fun `create and calculate onsetWindowDays`() = runBlocking {
        val expectedViewState = ViewState(
            NotStated,
            false,
            symptomsOnsetWindowDays
        )

        verify { viewStateObserver.onChanged(expectedViewState) }
    }

    @Test
    fun `select date`() = runBlocking {
        val initialState = testSubject.viewState.value

        val selectedDate = testEndDate.minus(1, ChronoUnit.DAYS)

        testSubject.onDateSelected(selectedDate.toEpochMilli())

        val expectedState = initialState?.copy(
            onsetDate = ExplicitDate(
                LocalDateTime.ofInstant(selectedDate, ZoneId.systemDefault()).toLocalDate()
            ),
            showOnsetDateError = false
        )

        verify { viewStateObserver.onChanged(expectedState) }
    }

    @Test
    fun `select cannot remember date`() = runBlocking {
        val initialState = testSubject.viewState.value

        testSubject.cannotRememberDateChecked()

        val expectedState = initialState?.copy(onsetDate = CannotRememberDate, showOnsetDateError = false)

        verify { viewStateObserver.onChanged(expectedState) }
    }

    @Test
    fun `unselect cannot remember date`() = runBlocking {
        val initialState = testSubject.viewState.value

        testSubject.cannotRememberDateUnchecked()

        val expectedState = initialState?.copy(onsetDate = NotStated, showOnsetDateError = false)

        verify { viewStateObserver.onChanged(expectedState) }
    }

    @Test
    fun `user clicks continue without stating a date`() = runBlocking {
        val initialState = testSubject.viewState.value

        testSubject.onButtonContinueClicked()

        val expectedState = initialState?.copy(showOnsetDateError = true)

        verify { viewStateObserver.onChanged(expectedState) }
        coVerify(exactly = 0) { analyticsEventProcessor.track(DidRememberOnsetSymptomsDateBeforeReceivedTestResult) }
        verify(exactly = 0) { continueEventObserver.onChanged(any()) }
    }

    @Test
    fun `user clicks continue with setting an explicit date`() = runBlocking {
        val initialState = testSubject.viewState.value

        val onset = testResult.testEndDate.minus(1, ChronoUnit.DAYS)
        val onsetDate = LocalDateTime.ofInstant(
            onset,
            ZoneId.systemDefault()
        ).toLocalDate()

        testSubject.onDateSelected(onset.toEpochMilli())

        testSubject.onButtonContinueClicked()

        verify { viewStateObserver.onChanged(initialState?.copy(ExplicitDate(onsetDate))) }

        coVerify { analyticsEventProcessor.track(DidRememberOnsetSymptomsDateBeforeReceivedTestResult) }
        verify {
            unacknowledgedTestResultsProvider.setSymptomsOnsetDate(
                testResult,
                SymptomsDate(
                    explicitDate = onsetDate
                )
            )
        }
        verify { continueEventObserver.onChanged(Unit) }
    }

    @Test
    fun `user clicks continue with cannot remember date`() = runBlocking {
        val initialState = testSubject.viewState.value

        testSubject.cannotRememberDateChecked()

        testSubject.onButtonContinueClicked()

        verify { viewStateObserver.onChanged(initialState?.copy(CannotRememberDate)) }

        coVerify(exactly = 0) { analyticsEventProcessor.track(DidRememberOnsetSymptomsDateBeforeReceivedTestResult) }
        verify {
            unacknowledgedTestResultsProvider.setSymptomsOnsetDate(
                testResult,
                SymptomsDate(
                    explicitDate = null
                )
            )
        }
        verify { continueEventObserver.onChanged(Unit) }
    }

    @Test
    fun `on click of date picker container emit test end date to parameterize data picker`() = runBlocking {
        testSubject.onDatePickerContainerClicked()

        verify { datePickerContainerClickedObserver.onChanged(testEndDate.toEpochMilli()) }
    }

    @Test
    fun `onset date after test end day is invalid`() {
        assertFalse(
            testSubject.isOnsetDateValid(
                testEndDate.plus(1, ChronoUnit.DAYS).toEpochMilli(),
                symptomsOnsetWindowDays
            )
        )
    }

    @Test
    fun `onset date test end day is valid`() {
        assertTrue(
            testSubject.isOnsetDateValid(
                testEndDate.toEpochMilli(),
                symptomsOnsetWindowDays
            )
        )
    }

    @Test
    fun `onset date in past is valid`() {
        assertTrue(
            testSubject.isOnsetDateValid(
                testEndDate.minus(5, ChronoUnit.DAYS).toEpochMilli(),
                symptomsOnsetWindowDays
            )
        )
    }

    @Test
    fun `onset date in past is invalid`() {
        assertFalse(
            testSubject.isOnsetDateValid(
                testEndDate.minus(6, ChronoUnit.DAYS).toEpochMilli(),
                symptomsOnsetWindowDays
            )
        )
    }
}
