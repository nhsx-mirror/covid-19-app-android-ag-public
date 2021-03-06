package uk.nhs.nhsx.covid19.android.app.di

import dagger.Module
import dagger.Provides
import uk.nhs.nhsx.covid19.android.app.di.viewmodel.MockTestResultViewModel
import uk.nhs.nhsx.covid19.android.app.testordering.BaseTestResultViewModel
import uk.nhs.nhsx.covid19.android.app.testordering.TestResultViewModel

@Module
class MockViewModelModule {
    @Provides
    fun provideTestResultViewModel(testResultViewModel: TestResultViewModel): BaseTestResultViewModel =
        if (MockTestResultViewModel.currentOptions.useMock) MockTestResultViewModel() else testResultViewModel
}
