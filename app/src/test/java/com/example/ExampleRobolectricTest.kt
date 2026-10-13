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
  }

  @Test
  fun `verify join stream requests and seat assignment workflow`() {
    val room = SampleData.createInitialGuestRoom()
    assertTrue("Should have sample pending join requests", room.pendingJoinRequests.isNotEmpty())
    val firstReq = room.pendingJoinRequests.first()
    assertEquals("Elena_Singer", firstReq.userName)
    assertEquals(5, firstReq.requestedSeatNumber)

    // Test ViewModel seat request and approval
    val viewModel = com.example.viewmodel.ZaraLiveViewModel()
    val initialPending = viewModel.uiState.value.guestRoom.pendingJoinRequests.size

    // User submits request
    viewModel.requestToJoinStream(requestedSeat = 6, videoOn = true, audioOn = true, note = "Testing seat 6")
    assertEquals(initialPending + 1, viewModel.uiState.value.guestRoom.pendingJoinRequests.size)
    assertTrue(viewModel.uiState.value.hasPendingJoinRequest)

    // Find the request and approve it
    val myReq = viewModel.uiState.value.guestRoom.pendingJoinRequests.last()
    viewModel.approveJoinRequest(myReq.id, targetSeatNumber = 6)

    // Check Seat 6 is now occupied by current user
    val seat6 = viewModel.uiState.value.guestRoom.guestSeats.find { it.seatNumber == 6 }
    assertTrue(seat6?.isOccupied == true)
    assertTrue(viewModel.uiState.value.isCurrentUserInGuestSeat)
    assertEquals(6, viewModel.uiState.value.currentUserSeatIndex)

    // Leave seat
    viewModel.leaveGuestSeat()
    val seat6AfterLeave = viewModel.uiState.value.guestRoom.guestSeats.find { it.seatNumber == 6 }
    assertTrue(seat6AfterLeave?.isOccupied == false)
    assertTrue(!viewModel.uiState.value.isCurrentUserInGuestSeat)
  }
}
