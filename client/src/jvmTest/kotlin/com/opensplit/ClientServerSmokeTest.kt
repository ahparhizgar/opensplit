package com.opensplit

import com.opensplit.ClientGreeting
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientServerSmokeTest {
    @Test
    fun clientAndSharedStarterStringsMatch() {
        val javaVersion = System.getProperty("java.version")
        val platformGreeting = ClientGreeting.subtitle("Java $javaVersion")
        val sharedGreeting = Greeting().greet()

        assertEquals("Hello, Java $javaVersion!", platformGreeting)
        assertEquals("Hello, Java $javaVersion!", sharedGreeting)
    }
}
