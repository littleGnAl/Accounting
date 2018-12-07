package com.littlegnal.accounting.ui.main

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.airbnb.mvrx.BaseMvRxActivity
import com.littlegnal.accounting.R
import com.littlegnal.accounting.ui.addedit.AddOrEditViewModel
import com.littlegnal.accounting.ui.summary.SummaryViewModel
import javax.inject.Inject

class MainActivity : BaseMvRxActivity() {

  @Inject
  lateinit var summaryViewModelFactory: SummaryViewModel.Factory
  @Inject
  lateinit var mainViewModelFactory: MainViewModel.Factory
  @Inject
  lateinit var addOrEditViewModelFactory: AddOrEditViewModel.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val toolbar = findViewById<Toolbar>(R.id.base_toolbar)
    toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
    setSupportActionBar(toolbar)
    setupActionBarWithNavController(findNavController(R.id.nav_host_fragment))
  }

  fun updateTitle(title: CharSequence) {
    findViewById<Toolbar>(R.id.base_toolbar).title = title
  }
}
