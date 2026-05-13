package com.opensplit

import kotlin.test.Test
import kotlin.test.assertEquals

class ClientServerSmokeTest {
    @Test
    fun clientAndSharedStarterStringsMatch() {
        val platformGreeting = ClientGreeting.subtitle("Java 21")
        val sharedGreeting = Greeting().greet()

        assertEquals("Hello, Java 21!", platformGreeting)
        assertEquals("Hello, Java ${System.getProperty("java.version")}!", sharedGreeting)
    }
}
