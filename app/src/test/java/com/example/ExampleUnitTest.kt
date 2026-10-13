package com.example

import com.example.data.SampleData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun guestRoom_hasOneHostAndEightGuestSlots() {
        val room = SampleData.createInitialGuestRoom()
        assertNotNull(room.hostSeat)
        assertTrue(room.hostSeat.isHost)
        assertEquals(0, room.hostSeat.seatNumber)

        // Must support exactly 8 guest slots
        assertEquals(8, room.guestSeats.size)
        room.guestSeats.forEachIndexed { index, seat ->
            assertFalse(seat.isHost)
            assertEquals(index + 1, seat.seatNumber)
        }
    }

    @Test
    fun hostSeat_initialState_isConfiguredProperly() {
        val hostSeat = SampleData.createInitialGuestRoom().hostSeat
        assertTrue(hostSeat.isOccupied)
        assertTrue(hostSeat.videoOn)
        assertFalse(hostSeat.isMuted)
        assertEquals("👑 Room Host", hostSeat.roleTitle)
    }

    @Test
    fun guestSeats_haveEmptyAndOccupiedSlots() {
        val seats = SampleData.createInitialGuestRoom().guestSeats
        val occupied = seats.filter { it.isOccupied }
        val empty = seats.filter { !it.isOccupied }

        assertTrue(occupied.isNotEmpty())
        assertTrue(empty.isNotEmpty())
        assertEquals(8, occupied.size + empty.size)
    }
}
