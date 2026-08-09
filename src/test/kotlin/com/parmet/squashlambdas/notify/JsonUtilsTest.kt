package com.parmet.squashlambdas.notify

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.cal.Action
import com.parmet.squashlambdas.json.Json
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import java.lang.reflect.Modifier
import java.time.Instant
import kotlin.reflect.KClass

class JsonUtilsTest {
    private val logger = KotlinLogging.logger { }

    private val match =
        Match(
            Court.Court2,
            Instant.parse("2018-03-26T22:45:00Z"),
            Instant.parse("2018-03-26T23:30:00Z"),
            "",
            setOf(Player(name = "Philipp Rimmler"))
        )

    private val clinic =
        Clinic(
            Court.Court2,
            Instant.parse("2018-03-26T22:45:00Z"),
            Instant.parse("2018-03-26T23:30:00Z"),
            ""
        )

    @Test
    fun `activity adapter works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            mapOf(
                Match::class to match,
                Clinic::class to clinic
            )

        assertHasAnExampleOfEachConcreteSubclass(Activity::class, instances)

        instances.forEach { (kclass, instance) ->
            val serialized = Json.encode(instance as Activity)
            logger.info { "Checking serialized form for $instance of type ${kclass.simpleName}: $serialized" }
            assertThat(serialized).contains("\"type\":\"${kclass.simpleName}\"")
        }
    }

    @Test
    fun `sport serializer works for all entries`() {
        Sport.entries.forEach { sport ->
            val serialized = Json.encode(sport)
            logger.info { "Checking serialized form for $sport: $serialized" }
            assertThat(serialized).isEqualTo("\"$sport\"")
        }
    }

    @Test
    fun `court serializer works for all entries`() {
        Court.entries.forEach { court ->
            val serialized = Json.prettyPrint(Json.element(court))
            logger.info { "Checking serialized form for $court: $serialized" }
            assertThat(serialized).contains(court.toString())
        }
    }

    @Test
    fun `action serializer works for all entries`() {
        Action.entries.forEach { action ->
            val serialized = Json.encode(action)
            logger.info { "Checking serialized form for $action: $serialized" }
            assertThat(serialized).isEqualTo("\"$action\"")
        }
    }

    private fun assertHasAnExampleOfEachConcreteSubclass(kclass: KClass<*>, instances: Map<KClass<*>, *>) {
        val jClassInstances = instances.mapKeys { it.key.java }

        Reflections("com.parmet").getSubTypesOf(kclass.java)
            .filter { it.isConcrete() }
            .forEach {
                logger.info { "Looking for an instance of $it" }
                assertThat(jClassInstances[it]).isNotNull()
            }
    }

    private fun Class<*>.isConcrete() =
        !Modifier.isAbstract(modifiers)
}
