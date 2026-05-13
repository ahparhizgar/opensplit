package com.opensplit

import kotlin.test.Test
import kotlin.test.assertEquals

class ClientGreetingTest {
    @Test
    fun formatsClientTitle() {
        assertEquals("OpenSplit", ClientGreeting.title())
    }

    @Test
    fun formatsPlatformSubtitle() {
        assertEquals("Hello, Java 21!", ClientGreeting.subtitle("Java 21"))
    }
}
