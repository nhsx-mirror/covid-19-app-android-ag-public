package uk.nhs.nhsx.covid19.android.app.analytics

import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.analytics.legacy.AnalyticsMetricsJsonStorage
import uk.nhs.nhsx.covid19.android.app.analytics.legacy.AnalyticsMetricsStorage
import uk.nhs.nhsx.covid19.android.app.remote.data.Metrics
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnalyticsMetricsStorageTest {

    private val moshi = Moshi.Builder().build()

    private val analyticsMetricsJsonStorage =
        mockk<AnalyticsMetricsJsonStorage>(relaxed = true)

    private val testSubject =
        AnalyticsMetricsStorage(
            analyticsMetricsJsonStorage,
            moshi
        )

    @Test
    fun `verify empty storage`() {
        every { analyticsMetricsJsonStorage.value } returns null

        val parsedMetrics = testSubject.metrics

        assertNull(parsedMetrics)
    }

    @Test
    fun `verify corrupted storage`() {
        every { analyticsMetricsJsonStorage.value } returns "dsfdsfsdfdsfdsf"

        val parsedMetrics = testSubject.metrics

        assertNull(parsedMetrics)
    }

    @Test
    fun `verify storing null`() {

        testSubject.metrics = null

        verify { analyticsMetricsJsonStorage.value = null }
    }

    @Test
    fun `verify serialization`() {
        every { analyticsMetricsJsonStorage.value } returns metricsJson

        val parsedMetrics = testSubject.metrics

        assertEquals(metrics, parsedMetrics)
    }

    @Test
    fun `verify deserialization`() {

        testSubject.metrics = metrics

        verify { analyticsMetricsJsonStorage.value = metricsJson }
    }

    private val metrics = Metrics(
        canceledCheckIn = 1,
        checkedIn = 2,
        completedOnboarding = 3,
        completedQuestionnaireAndStartedIsolation = 4,
        completedQuestionnaireButDidNotStartIsolation = 5,
        cumulativeDownloadBytes = 6,
        cumulativeUploadBytes = 7,
        encounterDetectionPausedBackgroundTick = 8,
        hasHadRiskyContactBackgroundTick = 9,
        hasSelfDiagnosedPositiveBackgroundTick = 10,
        isIsolatingBackgroundTick = 11,
        receivedNegativeTestResult = 12,
        receivedPositiveTestResult = 13,
        receivedVoidTestResult = 14,
        receivedNegativeTestResultEnteredManually = 15,
        receivedNegativeTestResultViaPolling = 16,
        receivedPositiveTestResultEnteredManually = 17,
        receivedPositiveTestResultViaPolling = 18,
        receivedVoidTestResultEnteredManually = 19,
        receivedVoidTestResultViaPolling = 20,
        receivedPositiveLFDTestResultViaPolling = 21,
        receivedNegativeLFDTestResultViaPolling = 22,
        receivedVoidLFDTestResultViaPolling = 23,
        receivedPositiveLFDTestResultEnteredManually = 24,
        receivedNegativeLFDTestResultEnteredManually = 25,
        receivedVoidLFDTestResultEnteredManually = 26,
        runningNormallyBackgroundTick = 27,
        totalBackgroundTasks = 28,
        hasSelfDiagnosedBackgroundTick = 29,
        hasTestedPositiveBackgroundTick = 30,
        hasTestedLFDPositiveBackgroundTick = 31,
        isIsolatingForSelfDiagnosedBackgroundTick = 32,
        isIsolatingForTestedPositiveBackgroundTick = 33,
        isIsolatingForTestedLFDPositiveBackgroundTick = 34,
        isIsolatingForHadRiskyContactBackgroundTick = 35,
        receivedRiskyContactNotification = 36,
        startedIsolation = 37,
        receivedActiveIpcToken = 38,
        haveActiveIpcTokenBackgroundTick = 39,
        selectedIsolationPaymentsButton = 40,
        launchedIsolationPaymentsApplication = 41,
        launchedTestOrdering = 42,
        totalExposureWindowsNotConsideredRisky = 43,
        totalExposureWindowsConsideredRisky = 44,
        acknowledgedStartOfIsolationDueToRiskyContact = 45,
        hasRiskyContactNotificationsEnabledBackgroundTick = 46,
        totalRiskyContactReminderNotifications = 47,
        receivedUnconfirmedPositiveTestResult = 48,
        isIsolatingForUnconfirmedTestBackgroundTick = 49,
        declaredNegativeResultFromDCT = 50,
        didHaveSymptomsBeforeReceivedTestResult = 51,
        didRememberOnsetSymptomsDateBeforeReceivedTestResult = 52,
        didAskForSymptomsOnPositiveTestEntry = 53,
        receivedPositiveSelfRapidTestResultEnteredManually = 54,
        hasTestedSelfRapidPositiveBackgroundTick = 55,
        isIsolatingForTestedSelfRapidPositiveBackgroundTick = 56,
        receivedRiskyVenueM1Warning = 57,
        receivedRiskyVenueM2Warning = 58,
        hasReceivedRiskyVenueM2WarningBackgroundTick = 59,
        totalAlarmManagerBackgroundTasks = 60,
        missingPacketsLast7Days = 61
    )

    private val metricsJson =
        """{"canceledCheckIn":1,"checkedIn":2,"completedOnboarding":3,"completedQuestionnaireAndStartedIsolation":4,"completedQuestionnaireButDidNotStartIsolation":5,"cumulativeDownloadBytes":6,"cumulativeUploadBytes":7,"encounterDetectionPausedBackgroundTick":8,"hasHadRiskyContactBackgroundTick":9,"hasSelfDiagnosedPositiveBackgroundTick":10,"isIsolatingBackgroundTick":11,"receivedNegativeTestResult":12,"receivedPositiveTestResult":13,"receivedVoidTestResult":14,"receivedVoidTestResultEnteredManually":19,"receivedPositiveTestResultEnteredManually":17,"receivedNegativeTestResultEnteredManually":15,"receivedVoidTestResultViaPolling":20,"receivedPositiveTestResultViaPolling":18,"receivedNegativeTestResultViaPolling":16,"receivedVoidLFDTestResultEnteredManually":26,"receivedPositiveLFDTestResultEnteredManually":24,"receivedNegativeLFDTestResultEnteredManually":25,"receivedVoidLFDTestResultViaPolling":23,"receivedPositiveLFDTestResultViaPolling":21,"receivedNegativeLFDTestResultViaPolling":22,"receivedUnconfirmedPositiveTestResult":48,"receivedPositiveSelfRapidTestResultEnteredManually":54,"runningNormallyBackgroundTick":27,"totalBackgroundTasks":28,"hasSelfDiagnosedBackgroundTick":29,"hasTestedPositiveBackgroundTick":30,"hasTestedLFDPositiveBackgroundTick":31,"hasTestedSelfRapidPositiveBackgroundTick":55,"isIsolatingForSelfDiagnosedBackgroundTick":32,"isIsolatingForTestedPositiveBackgroundTick":33,"isIsolatingForTestedLFDPositiveBackgroundTick":34,"isIsolatingForTestedSelfRapidPositiveBackgroundTick":56,"isIsolatingForUnconfirmedTestBackgroundTick":49,"isIsolatingForHadRiskyContactBackgroundTick":35,"receivedRiskyContactNotification":36,"startedIsolation":37,"receivedActiveIpcToken":38,"haveActiveIpcTokenBackgroundTick":39,"selectedIsolationPaymentsButton":40,"launchedIsolationPaymentsApplication":41,"launchedTestOrdering":42,"totalExposureWindowsNotConsideredRisky":43,"totalExposureWindowsConsideredRisky":44,"acknowledgedStartOfIsolationDueToRiskyContact":45,"hasRiskyContactNotificationsEnabledBackgroundTick":46,"totalRiskyContactReminderNotifications":47,"declaredNegativeResultFromDCT":50,"didHaveSymptomsBeforeReceivedTestResult":51,"didRememberOnsetSymptomsDateBeforeReceivedTestResult":52,"didAskForSymptomsOnPositiveTestEntry":53,"receivedRiskyVenueM1Warning":57,"receivedRiskyVenueM2Warning":58,"hasReceivedRiskyVenueM2WarningBackgroundTick":59,"totalAlarmManagerBackgroundTasks":60,"missingPacketsLast7Days":61}""".trimIndent()
}
