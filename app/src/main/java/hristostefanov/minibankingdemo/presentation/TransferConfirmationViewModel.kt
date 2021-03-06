package hristostefanov.minibankingdemo.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hristostefanov.minibankingdemo.NavGraphXmlDirections
import hristostefanov.minibankingdemo.R
import hristostefanov.minibankingdemo.business.dependences.ServiceException
import hristostefanov.minibankingdemo.business.interactors.AddMoneyIntoGoalInteractor
import hristostefanov.minibankingdemo.presentation.dependences.AmountFormatter
import hristostefanov.minibankingdemo.ui.TransferConfirmationFragmentArgs
import hristostefanov.minibankingdemo.util.NavigationChannel
import hristostefanov.minibankingdemo.util.StringSupplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NAVIGATION_DELAY_MS = 2000L

class TransferConfirmationViewModel constructor(
    private val args: TransferConfirmationFragmentArgs
) : ViewModel() {
    @Inject
    internal lateinit var interactor: AddMoneyIntoGoalInteractor
    @Inject
    internal lateinit var stringSupplier: StringSupplier
    @Inject
    internal lateinit var amountFormatter: AmountFormatter
    @Inject @NavigationChannel
    internal lateinit var navigationChannel: Channel<Navigation>

    private val _acknowledgementChannel = Channel<String>()
    val acknowledgementChannel: ReceiveChannel<String> = _acknowledgementChannel

    private val _info = MutableLiveData("")
    val info: LiveData<String> = _info

    @Inject
    internal fun init() {
        val amountFormatted = amountFormatter.format(
            args.roundUpAmount,
            args.accountCurrency.currencyCode
        )
        _info.value = stringSupplier.get(R.string.transferInfo)
             .format(amountFormatted, args.savingsGoal.name)
    }

    fun onConfirmCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.execute(
                    args.accountId,
                    args.savingsGoal.id,
                    args.accountCurrency,
                    args.roundUpAmount
                )

                _acknowledgementChannel.send(stringSupplier.get(R.string.success))
                delay(NAVIGATION_DELAY_MS)
                navigationChannel.send(Navigation.Before(R.id.savingsGoalsDestination))
            } catch (e: ServiceException) {
                e.localizedMessage?.also {
                    navigationChannel.send(Navigation.Forward(NavGraphXmlDirections.toErrorDialog(it)))
                }
            }
        }
    }
}