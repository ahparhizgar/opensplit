package com.opensplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.retainedComponent
import com.opensplit.component.defaultCContext
import com.opensplit.root.FakeRootComponent
import com.opensplit.root.RootComponentFactory
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val factory: RootComponentFactory by inject()
    val root = retainedComponent { factory.create(defaultCContext(it)) }

    setContent { App(root) }
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App(FakeRootComponent())
}
