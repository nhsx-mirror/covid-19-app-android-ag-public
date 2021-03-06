package uk.nhs.nhsx.covid19.android.app.status

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.nhs.nhsx.covid19.android.app.common.postcode.LocalAuthorityPostCodeProvider
import uk.nhs.nhsx.covid19.android.app.common.postcode.PostCodeDistrict.ENGLAND
import uk.nhs.nhsx.covid19.android.app.status.StatusViewModel.RiskyPostCodeViewState.Risk
import javax.inject.Inject

class RiskLevelViewModel @Inject constructor(
    private val localAuthorityPostCodeProvider: LocalAuthorityPostCodeProvider
) : ViewModel() {

    private val showMassTestingLiveData = MutableLiveData<Boolean>()
    fun showMassTesting() = showMassTestingLiveData

    fun onHandleRiskLevel(risk: Risk) {
        viewModelScope.launch {
            showMassTestingLiveData.postValue(
                localAuthorityPostCodeProvider.getPostCodeDistrict()?.let { it == ENGLAND } ?: false &&
                    risk.riskIndicator.policyData != null
            )
        }
    }
}
