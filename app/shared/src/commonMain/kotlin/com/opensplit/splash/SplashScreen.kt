package com.opensplit.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.opensplit.root.TopLevelDestinationConfig
import com.opensplit.ui.OpenSplitTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
  Surface(modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
          Modifier.size(48.dp)
              .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
      )
    }
  }
}

data object SplashDestination : TopLevelDestinationConfig

@Preview
@Composable
fun SplashScreenPreview() {
  OpenSplitTheme { SplashScreen() }
}
