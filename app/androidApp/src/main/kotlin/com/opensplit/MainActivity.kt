package com.opensplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.defaultComponentContext
import com.opensplit.component.defaultCContext
import com.opensplit.root.FakeRootComponent
import com.opensplit.root.RootComponent
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val factory: RootComponent.Factory by inject()
    val root = factory.create(defaultCContext(defaultComponentContext()))

    setContent { App(root) }
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App(FakeRootComponent())
}
