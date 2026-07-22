package com.opensplit.features.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.opensplit.dto.expense.SplitType
import com.opensplit.ui.OpenSplitTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustSplitScreen(component: AdjustSplitComponent) {
  val pagerState = rememberPagerState(pageCount = { SplitType.entries.size })
  val scope = rememberCoroutineScope()

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }
        .collect { component.onTabChanged(SplitType.entries[it]) }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    SecondaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
      SplitType.entries.forEachIndexed { index, type ->
        Tab(
            selected = pagerState.currentPage == index,
            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
        )
      }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f),
        key = { SplitType.entries[it].name },
    ) { page ->
      val onDone = { component.onDoneClicked() }
      when (SplitType.entries[page]) {
        SplitType.EQUALLY -> EquallySplitPage(component.equallyComponent, onDone)
        SplitType.Unequally -> UnequallySplitPage(component.unequallyComponent, onDone)
        SplitType.PERCENTAGE -> PercentageSplitPage(component.percentageComponent, onDone)
        SplitType.SHARES -> SharesSplitPage(component.sharesComponent, onDone)
        SplitType.ADJUSTMENT -> AdjustmentSplitPage(component.adjustmentComponent, onDone)
      }
    }
  }
}

val transparentTextFieldColors
  @Composable
  get() =
      TextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
      )

@Preview
@Composable
private fun AdjustSplitPreview() {
  OpenSplitTheme { AdjustSplitScreen(FakeAdjustSplitComponent()) }
}
