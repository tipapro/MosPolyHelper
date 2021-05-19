package com.mospolytech.mospolyhelper.features.ui.account.applications

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.mospolytech.mospolyhelper.R
import com.mospolytech.mospolyhelper.databinding.FragmentAccountApplicationsBinding
import com.mospolytech.mospolyhelper.features.ui.account.applications.adapter.ApplicationsAdapter
import com.mospolytech.mospolyhelper.utils.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ApplicationsFragment: Fragment(R.layout.fragment_account_applications) {

    private val viewBinding by viewBinding(FragmentAccountApplicationsBinding::bind)
    private val viewModel by viewModel<ApplicationsViewModel>()

    private val adapter = ApplicationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.getInfo()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.applicationsSwipe.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.downloadInfo()
            }
        }

        viewBinding.applications.adapter = adapter

        lifecycleScope.launchWhenResumed {
            viewModel.applications.collect { result ->
                result.onSuccess {
                    viewBinding.progressLoading.gone()
                    viewBinding.applicationsSwipe.isRefreshing = false
                    adapter.items = it
                }.onFailure {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()
                    viewBinding.progressLoading.gone()
                    viewBinding.applicationsSwipe.isRefreshing = false
                }.onLoading {
                    if (!viewBinding.applicationsSwipe.isRefreshing)
                        viewBinding.progressLoading.show()
                }
            }
        }

    }
}