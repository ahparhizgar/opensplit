package com.opensplit

import com.opensplit.ClientGreeting
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientGreetingTest {
    @Test
    fun formatsClientTitle() {
        assertEquals("OpenSplit", ClientGreeting.title())
    }

    @Test
    fun formatsPlatformSubtitle() {
        val javaVersion = System.getProperty("java.version")
        assertEquals("Hello, Java $javaVersion!", ClientGreeting.subtitle("Java $javaVersion"))
    }
}
