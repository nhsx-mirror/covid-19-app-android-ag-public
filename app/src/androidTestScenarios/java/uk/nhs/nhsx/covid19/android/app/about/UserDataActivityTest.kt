package uk.nhs.nhsx.covid19.android.app.about

import androidx.test.platform.app.InstrumentationRegistry
import com.jeroenmols.featureflag.framework.FeatureFlag.DAILY_CONTACT_TESTING
import com.jeroenmols.featureflag.framework.FeatureFlagTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.qrcode.Venue
import uk.nhs.nhsx.covid19.android.app.qrcode.VenueVisit
import uk.nhs.nhsx.covid19.android.app.qrcode.riskyvenues.LastVisitedBookTestTypeVenueDate
import uk.nhs.nhsx.covid19.android.app.remote.data.DurationDays
import uk.nhs.nhsx.covid19.android.app.remote.data.RiskyVenueConfigurationDurationDays
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.LAB_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.RAPID_RESULT
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestKitType.RAPID_SELF_REPORTED
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.NEGATIVE
import uk.nhs.nhsx.covid19.android.app.remote.data.VirologyTestResult.POSITIVE
import uk.nhs.nhsx.covid19.android.app.report.config.Orientation.LANDSCAPE
import uk.nhs.nhsx.covid19.android.app.report.config.Orientation.PORTRAIT
import uk.nhs.nhsx.covid19.android.app.report.notReported
import uk.nhs.nhsx.covid19.android.app.state.State.Default
import uk.nhs.nhsx.covid19.android.app.state.State.Isolation
import uk.nhs.nhsx.covid19.android.app.state.State.Isolation.ContactCase
import uk.nhs.nhsx.covid19.android.app.state.State.Isolation.IndexCase
import uk.nhs.nhsx.covid19.android.app.testhelpers.base.EspressoTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.retry.RetryFlakyTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.DataAndPrivacyRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.LocalAuthorityRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.MoreAboutAppRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.PermissionRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.PostCodeRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.StatusRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.UserDataRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.WelcomeRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.setScreenOrientation
import uk.nhs.nhsx.covid19.android.app.testordering.ReceivedTestResult
import uk.nhs.nhsx.covid19.android.app.testordering.TestResultStorageOperation.Confirm
import uk.nhs.nhsx.covid19.android.app.testordering.TestResultStorageOperation.Overwrite
import uk.nhs.nhsx.covid19.android.app.util.uiFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class UserDataActivityTest : EspressoTest() {
    private val moreAboutAppRobot = MoreAboutAppRobot()
    private val userDataRobot = UserDataRobot()
    private val welcomeRobot = WelcomeRobot()
    private val dataAndPrivacyRobot = DataAndPrivacyRobot()
    private val postCodeRobot = PostCodeRobot()
    private val localAuthorityRobot = LocalAuthorityRobot()
    private val statusRobot = StatusRobot()
    private val permissionRobot = PermissionRobot()

    private val visit1 = VenueVisit(
        venue = Venue("1", "Venue1"),
        from = Instant.parse("2020-07-25T10:00:00Z"),
        to = Instant.parse("2020-07-25T12:00:00Z")
    )
    private val visit2 = VenueVisit(
        venue = Venue("2", "Venue2"),
        from = Instant.parse("2020-07-25T14:00:00Z"),
        to = Instant.parse("2020-07-25T16:00:00Z")
    )
    private val visits = listOf(visit1, visit2)
    private val latestRiskyVenueVisitDate = LocalDate.parse("2020-07-25")

    @Before
    fun setUp() = runBlocking {
        testAppContext.getVisitedVenuesStorage().setVisits(visits)
        testAppContext.getLastVisitedBookTestTypeVenueDateProvider().lastVisitedVenue = LastVisitedBookTestTypeVenueDate(
            latestRiskyVenueVisitDate,
            RiskyVenueConfigurationDurationDays(optionToBookATest = 10)
        )
    }

    @After
    fun tearDown() {
        FeatureFlagTestHelper.clearFeatureFlags()
    }

    @Test
    fun myDataScreenShows() = notReported {
        startTestActivity<MoreAboutAppActivity>()

        moreAboutAppRobot.checkActivityIsDisplayed()
    }

    @Test
    fun clickOnSetDataOpensMyDataScreen() = notReported {
        startTestActivity<MoreAboutAppActivity>()

        moreAboutAppRobot.checkActivityIsDisplayed()

        moreAboutAppRobot.clickSeeData()

        userDataRobot.checkActivityIsDisplayed()
    }

    @RetryFlakyTest
    @Test
    fun clickOnDeleteUserData_opensWelcomeScreenAndShowsPermissionScreenWithoutDialog() =
        notReported {
            testAppContext.setPostCode(null)

            startTestActivity<UserDataActivity>()

            userDataRobot.checkActivityIsDisplayed()

            userDataRobot.userClicksOnDeleteAllDataButton()

            userDataRobot.userClicksDeleteDataOnDialog()

            waitFor { welcomeRobot.isActivityDisplayed() }

            welcomeRobot.checkActivityIsDisplayed()

            welcomeRobot.clickConfirmOnboarding()

            welcomeRobot.checkAgeConfirmationDialogIsDisplayed()

            welcomeRobot.clickConfirmAgePositive()

            dataAndPrivacyRobot.checkActivityIsDisplayed()

            dataAndPrivacyRobot.clickConfirmOnboarding()

            postCodeRobot.checkActivityIsDisplayed()

            postCodeRobot.enterPostCode("N12")

            postCodeRobot.clickContinue()

            waitFor { localAuthorityRobot.checkActivityIsDisplayed() }

            localAuthorityRobot.clickConfirm()

            waitFor { permissionRobot.checkActivityIsDisplayed() }

            permissionRobot.clickEnablePermissions()

            statusRobot.checkActivityIsDisplayed()
        }

    @Test
    fun deleteSingleVenueVisit() = notReported {
        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        userDataRobot.checkVisitIsDisplayedInRow(visit2, 0)
        userDataRobot.checkVisitIsDisplayedInRow(visit1, 1)

        waitFor { userDataRobot.editVenueVisitsIsDisplayed() }

        userDataRobot.userClicksEditVenueVisits()

        userDataRobot.checkDeleteIconForFirstVenueVisitIsDisplayed()

        userDataRobot.clickDeleteVenueVisitOnFirstPosition()

        waitFor { userDataRobot.confirmDialogIsDisplayed() }

        setScreenOrientation(LANDSCAPE)

        waitFor { userDataRobot.confirmDialogIsDisplayed() }

        setScreenOrientation(PORTRAIT)

        waitFor { userDataRobot.confirmDialogIsDisplayed() }

        userDataRobot.userClicksConfirmOnDialog()

        waitFor { userDataRobot.userClicksEditVenueVisits() }

        userDataRobot.editVenueVisitsIsDisplayed()

        userDataRobot.checkVisitIsDisplayedInRow(visit1, 0)
    }

    @Test
    fun deleteVisitedVenue_shouldKeepLatestBookTestTypeVenueDate() = notReported {
        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        waitFor { userDataRobot.editVenueVisitsIsDisplayed() }

        userDataRobot.userClicksEditVenueVisits()

        userDataRobot.clickDeleteVenueVisitOnFirstPosition()

        waitFor { userDataRobot.confirmDialogIsDisplayed() }

        userDataRobot.userClicksConfirmOnDialog()

        userDataRobot.clickDeleteVenueVisitOnFirstPosition()

        waitFor { userDataRobot.confirmDialogIsDisplayed() }

        userDataRobot.userClicksConfirmOnDialog()

        userDataRobot.checkLastVisitedBookTestTypeVenueDateIsDisplayed(latestRiskyVenueVisitDate.uiFormat(testAppContext.app))
    }

    @Test
    fun doNotDisplayTestResultSectionIfNoTestResults() = notReported {
        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        userDataRobot.checkLastTestResultIsNotDisplayed()
    }

    @Test
    fun displayLastPositivePcrAcknowledgedTestResultWithKeySubmissionSupported() = notReported {
        displayLastAcknowledgedTestResult(
            POSITIVE,
            LAB_RESULT,
            diagnosisKeySubmissionSupported = true
        )
    }

    @Test
    fun displayLastPositivePcrAcknowledgedTestResultWithKeySubmissionNotSupported() = notReported {
        displayLastAcknowledgedTestResult(
            POSITIVE,
            LAB_RESULT,
            diagnosisKeySubmissionSupported = false
        )
    }

    @Test
    fun displayLastPositiveAssistedLfdAcknowledgedTestResultWithKeySubmissionSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                POSITIVE,
                RAPID_RESULT,
                diagnosisKeySubmissionSupported = true
            )
        }

    @Test
    fun displayLastPositiveAssistedLfdAcknowledgedTestResultWithKeySubmissionNotSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                POSITIVE,
                RAPID_RESULT,
                diagnosisKeySubmissionSupported = false
            )
        }

    @Test
    fun displayLastPositiveUnassistedLfdAcknowledgedTestResultWithKeySubmissionSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                POSITIVE,
                RAPID_SELF_REPORTED,
                diagnosisKeySubmissionSupported = true
            )
        }

    @Test
    fun displayLastPositiveUnassistedLfdAcknowledgedTestResultWithKeySubmissionNotSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                POSITIVE,
                RAPID_SELF_REPORTED,
                diagnosisKeySubmissionSupported = false
            )
        }

    @Test
    fun displayLastNegativePcrAcknowledgedTestResultWithKeySubmissionSupported() = notReported {
        displayLastAcknowledgedTestResult(
            NEGATIVE,
            LAB_RESULT,
            diagnosisKeySubmissionSupported = true
        )
    }

    @Test
    fun displayLastNegativePcrAcknowledgedTestResultWithKeySubmissionNotSupported() = notReported {
        displayLastAcknowledgedTestResult(
            NEGATIVE,
            LAB_RESULT,
            diagnosisKeySubmissionSupported = false
        )
    }

    @Test
    fun displayLastNegativeAssistedLfdAcknowledgedTestResultWithKeySubmissionSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                NEGATIVE,
                RAPID_RESULT,
                diagnosisKeySubmissionSupported = true
            )
        }

    @Test
    fun displayLastNegativeAssistedLfdAcknowledgedTestResultWithKeySubmissionNotSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                NEGATIVE,
                RAPID_RESULT,
                diagnosisKeySubmissionSupported = false
            )
        }

    @Test
    fun displayLastNegativeUnassistedLfdAcknowledgedTestResultWithKeySubmissionSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                NEGATIVE,
                RAPID_SELF_REPORTED,
                diagnosisKeySubmissionSupported = true
            )
        }

    @Test
    fun displayLastNegativeUnassistedLfdAcknowledgedTestResultWithKeySubmissionNotSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                NEGATIVE,
                RAPID_SELF_REPORTED,
                diagnosisKeySubmissionSupported = false
            )
        }

    @Test
    fun requiresConfirmatoryTestNotReceivedFollowUpTestShouldBePending() = notReported {
        displayLastAcknowledgedTestResult(
            NEGATIVE,
            RAPID_SELF_REPORTED,
            diagnosisKeySubmissionSupported = false,
            requiresConfirmatoryTest = true
        )
    }

    @Test
    fun requiresConfirmatoryTestReceivedFollowUpTestShouldBeComplete() = notReported {
        displayLastAcknowledgedTestResult(
            NEGATIVE,
            RAPID_SELF_REPORTED,
            diagnosisKeySubmissionSupported = false,
            requiresConfirmatoryTest = true,
            receivedFollowUpTest = Instant.parse("2020-07-18T00:05:00.00Z")
        )
    }

    @Test
    fun displayLastPositiveAcknowledgedTestResultOfUnknownTypeWithKeySubmissionSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                POSITIVE,
                testKitType = null, // UNKNOWN
                diagnosisKeySubmissionSupported = true
            )
        }

    @Test
    fun displayLastNegativeAcknowledgedTestResultOfUnknownTypeWithKeySubmissionSupported() =
        notReported {
            displayLastAcknowledgedTestResult(
                NEGATIVE,
                testKitType = null, // UNKNOWN
                diagnosisKeySubmissionSupported = true
            )
        }

    private fun displayLastAcknowledgedTestResult(
        testResult: VirologyTestResult,
        testKitType: VirologyTestKitType?,
        diagnosisKeySubmissionSupported: Boolean,
        requiresConfirmatoryTest: Boolean = false,
        receivedFollowUpTest: Instant? = null
    ) {
        val initialTestResult = ReceivedTestResult(
            diagnosisKeySubmissionToken = "a",
            testEndDate = Instant.now(),
            testResult = testResult,
            testKitType = testKitType,
            requiresConfirmatoryTest = requiresConfirmatoryTest,
            diagnosisKeySubmissionSupported = diagnosisKeySubmissionSupported
        )

        testAppContext.getRelevantTestResultProvider().onTestResultAcknowledged(initialTestResult, Overwrite)

        if (receivedFollowUpTest != null) {
            val followupTest = ReceivedTestResult(
                diagnosisKeySubmissionToken = "b",
                testEndDate = receivedFollowUpTest,
                testResult = POSITIVE,
                testKitType = testKitType,
                requiresConfirmatoryTest = false,
                diagnosisKeySubmissionSupported = diagnosisKeySubmissionSupported
            )
            testAppContext.getRelevantTestResultProvider()
                .onTestResultAcknowledged(followupTest, Confirm(confirmedDate = receivedFollowUpTest))
        }

        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        val shouldKitTypeBeVisible = testKitType != null

        val date: String? = receivedFollowUpTest?.atZone(ZoneId.systemDefault())?.toLocalDate()
            ?.uiFormat(InstrumentationRegistry.getInstrumentation().targetContext)

        waitFor {
            userDataRobot.checkLastTestResultIsDisplayed(
                shouldKitTypeBeVisible,
                requiresConfirmatoryTest,
                date
            )
        }
    }

    @Test
    fun displayEncounterInIsolationWithoutNotificationDate() = notReported {
        testAppContext.setState(
            Isolation(
                isolationStart = Instant.now(),
                isolationConfiguration = DurationDays(),
                contactCase = ContactCase(
                    Instant.parse("2020-05-19T12:00:00Z"),
                    null,
                    LocalDate.now().plusDays(5),
                    dailyContactTestingOptInDate = null
                )
            )
        )

        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        waitFor { userDataRobot.checkEncounterIsDisplayed() }
        userDataRobot.checkExposureNotificationIsDisplayed()
        userDataRobot.checkExposureNotificationDateIsNotDisplayed()
        waitFor { userDataRobot.checkDailyContactTestingOptInDateIsNotDisplayed() }
    }

    @Test
    fun displayEncounterInIsolationWithNotificationDate() = notReported {
        testAppContext.setState(
            Isolation(
                isolationStart = Instant.now(),
                isolationConfiguration = DurationDays(),
                contactCase = ContactCase(
                    Instant.parse("2020-05-19T12:00:00Z"),
                    Instant.parse("2020-05-20T12:00:00Z"),
                    LocalDate.now().plusDays(5),
                    dailyContactTestingOptInDate = null
                )
            )
        )

        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        waitFor { userDataRobot.checkEncounterIsDisplayed() }
        userDataRobot.checkExposureNotificationIsDisplayed()
        userDataRobot.checkExposureNotificationDateIsDisplayed()
        waitFor { userDataRobot.checkDailyContactTestingOptInDateIsNotDisplayed() }
    }

    @Test
    fun displaySymptomsInIsolation() = notReported {
        testAppContext.setState(
            Isolation(
                isolationStart = Instant.now(),
                isolationConfiguration = DurationDays(),
                indexCase = IndexCase(
                    symptomsOnsetDate = LocalDate.now(),
                    expiryDate = LocalDate.now().plusDays(5),
                    selfAssessment = false
                )
            )
        )

        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        waitFor { userDataRobot.checkSymptomsAreDisplayed() }
        waitFor { userDataRobot.checkDailyContactTestingOptInDateIsNotDisplayed() }
    }

    @Test
    fun contactCaseOnly_notOptedInToDailyContactTesting_displayLastDayOfIsolationInIsolation() = notReported {
        testAppContext.setState(
            Isolation(
                isolationStart = Instant.now(),
                isolationConfiguration = DurationDays(),
                contactCase = ContactCase(
                    Instant.parse("2020-05-19T12:00:00Z"),
                    Instant.parse("2020-05-20T12:00:00Z"),
                    LocalDate.now().plusDays(5),
                    dailyContactTestingOptInDate = null
                )
            )
        )

        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        waitFor { userDataRobot.checkLastDayOfIsolationIsDisplayed() }
        waitFor { userDataRobot.checkExposureNotificationDateIsDisplayed() }
        waitFor { userDataRobot.checkDailyContactTestingOptInDateIsNotDisplayed() }
    }

    @Test
    fun doNotDisplayLastDayOfIsolationWhenNotInIsolation() = notReported {
        testAppContext.setState(Default())

        startTestActivity<UserDataActivity>()

        userDataRobot.checkActivityIsDisplayed()

        waitFor { userDataRobot.checkLastDayOfIsolationIsNotDisplayed() }
        waitFor { userDataRobot.checkDailyContactTestingOptInDateIsNotDisplayed() }
    }

    @Test
    fun previouslyIsolatedAsContactCaseOnly_optedInToDailyContactTesting_showDailyContactTestingOptInDate() =
        notReported {
            FeatureFlagTestHelper.enableFeatureFlag(DAILY_CONTACT_TESTING)

            testAppContext.setState(defaultWithPreviousIsolationContactCaseOnly)

            startTestActivity<UserDataActivity>()

            userDataRobot.checkActivityIsDisplayed()

            waitFor { userDataRobot.checkLastDayOfIsolationIsNotDisplayed() }
            waitFor { userDataRobot.checkExposureNotificationDateIsNotDisplayed() }
            waitFor { userDataRobot.checkDailyContactTestingOptInDateIsDisplayed() }
        }

    private val defaultWithPreviousIsolationContactCaseOnly = Default(
        previousIsolation = Isolation(
            isolationStart = Instant.now(),
            isolationConfiguration = DurationDays(),
            contactCase = ContactCase(
                Instant.parse("2020-05-19T12:00:00Z"),
                null,
                LocalDate.now().plusDays(5),
                dailyContactTestingOptInDate = LocalDate.now().plusDays(5)
            )
        )
    )
}
