package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleData
import com.example.model.StreamPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zara Live", appName)
  }

  @Test
  fun `verify guest room has exactly 1 host seat and 8 guest seats`() {
    val room = SampleData.createInitialGuestRoom()
    assertTrue("Host seat must be designated as host", room.hostSeat.isHost)
    assertEquals("Guest room must have exactly 8 guest seats", 8, room.guestSeats.size)
    val seatNumbers = room.guestSeats.map { it.seatNumber }
    assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), seatNumbers)
  }

  @Test
  fun `verify multi-platform destinations support Twitch, YouTube and others`() {
    val destinations = SampleData.initialMultiStreamDestinations()
    val platforms = destinations.map { it.platform }
    assertTrue(platforms.contains(StreamPlatform.TWITCH))
    assertTrue(platforms.contains(StreamPlatform.YOUTUBE))
    assertTrue(platforms.contains(StreamPlatform.TIKTOK))
  }
}
