package hristostefanov.starlingdemo.ui


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import hristostefanov.starlingdemo.R
import hristostefanov.starlingdemo.databinding.AccountsFragmentBinding
import hristostefanov.starlingdemo.presentation.AccountsViewModel

class AccountsFragment : Fragment() {
    private lateinit var binding: AccountsFragmentBinding

    private val viewModel: AccountsViewModel by viewModels {
        UIUnitTestRegistry.viewModelFactory ?: ViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = AccountsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // needed for observing LiveData
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.accounts_options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.logout) {
            viewModel.onLogout()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private inner class ViewModelFactory :
        AbstractSavedStateViewModelFactory(this, null) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return AccountsViewModel(handle).also { sessionComponent().inject(it) } as T
        }
    }
}

