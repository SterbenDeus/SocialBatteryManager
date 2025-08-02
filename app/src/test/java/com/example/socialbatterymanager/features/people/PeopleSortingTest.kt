package com.example.socialbatterymanager.features.people

import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.data.model.PersonLabel
import com.example.socialbatterymanager.data.model.PersonMood
import com.example.socialbatterymanager.features.people.data.PersonWithStats
import org.junit.Test
import org.junit.Assert.*

class PeopleSortingTest {

    private fun createTestPeople(): List<PersonWithStats> {
        return listOf(
            PersonWithStats(
                person = Person(
                    id = 1,
                    name = "Alice Johnson",
                    label = PersonLabel.CLOSE_FRIEND,
                    socialEnergyLevel = 75,
                    mood = PersonMood.HAPPY
                ),
                totalInteractions = 23,
                totalEnergyUsed = 552, // 9.2h in minutes
                interactionsThisWeek = 5
            ),
            PersonWithStats(
                person = Person(
                    id = 2,
                    name = "Bob Martinez",
                    label = PersonLabel.COWORKER,
                    socialEnergyLevel = 45,
                    mood = PersonMood.NEUTRAL
                ),
                totalInteractions = 18,
                totalEnergyUsed = 468, // 7.8h in minutes
                interactionsThisWeek = 3
            ),
            PersonWithStats(
                person = Person(
                    id = 3,
                    name = "Carol Davis",
                    label = PersonLabel.CLOSE_FRIEND,
                    socialEnergyLevel = 90,
                    mood = PersonMood.VERY_HAPPY
                ),
                totalInteractions = 31,
                totalEnergyUsed = 390, // 6.5h in minutes
                interactionsThisWeek = 7
            )
        )
    }

    @Test
    fun testSortByName() {
        val people = createTestPeople()
        val sorted = people.sortedBy { it.person.name }

        assertEquals("Alice Johnson", sorted[0].person.name)
        assertEquals("Bob Martinez", sorted[1].person.name)
        assertEquals("Carol Davis", sorted[2].person.name)
    }

    @Test
    fun testSortByEnergyDrain() {
        val people = createTestPeople()
        val sorted = people.sortedByDescending { it.totalEnergyUsed }

        assertEquals("Alice Johnson", sorted[0].person.name) // 9.2h
        assertEquals("Bob Martinez", sorted[1].person.name)  // 7.8h
        assertEquals("Carol Davis", sorted[2].person.name)   // 6.5h
    }

    @Test
    fun testSortByInteractions() {
        val people = createTestPeople()
        val sorted = people.sortedByDescending { it.totalInteractions }

        assertEquals("Carol Davis", sorted[0].person.name)   // 31 interactions
        assertEquals("Alice Johnson", sorted[1].person.name) // 23 interactions
        assertEquals("Bob Martinez", sorted[2].person.name)  // 18 interactions
    }

    @Test
    fun testPersonLabelsAndMoods() {
        val people = createTestPeople()

        assertEquals(PersonLabel.CLOSE_FRIEND, people[0].person.label)
        assertEquals(PersonLabel.COWORKER, people[1].person.label)
        assertEquals(PersonLabel.CLOSE_FRIEND, people[2].person.label)

        assertEquals(PersonMood.HAPPY, people[0].person.mood)
        assertEquals(PersonMood.NEUTRAL, people[1].person.mood)
        assertEquals(PersonMood.VERY_HAPPY, people[2].person.mood)
    }

    @Test
    fun testSocialEnergyLevels() {
        val people = createTestPeople()

        assertEquals(75, people[0].person.socialEnergyLevel)
        assertEquals(45, people[1].person.socialEnergyLevel)
        assertEquals(90, people[2].person.socialEnergyLevel)

        // Test energy level categorization
        assertTrue("High energy level should be >= 70", people[0].person.socialEnergyLevel >= 70)
        assertTrue("Medium energy level should be between 40-70", 
            people[1].person.socialEnergyLevel in 40..69)
        assertTrue("High energy level should be >= 70", people[2].person.socialEnergyLevel >= 70)
    }
}