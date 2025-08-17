package com.parmet.squashlambdas.notify

import com.fasterxml.jackson.databind.module.SimpleModule
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

    private val module =
        SimpleModule()
            .addSerializer(Sport::class.java, SportSerializer)
            .addSerializer(Court::class.java, CourtSerializer)
            .addSerializer(Action::class.java, ActionSerializer)

    @Test
    fun `activity adapter works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            mapOf(
                Match::class to match,
                Clinic::class to clinic
            )

        assertHasAnExampleOfEachConcreteSubclass(Activity::class, instances)

        // With Jackson annotations on Activity, writing as base type includes "type"
        instances.forEach { (kclass, instance) ->
            val serialized = Json.mapper.writeValueAsString(instance as Activity)
            logger.info { "Checking serialized form for $instance of type ${kclass.simpleName}: $serialized" }
            assertThat(serialized).contains("\"type\":\"${kclass.simpleName}\"")
        }
    }

    @Test
    fun `sport serializer works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            listOf(Sport.Squash, Sport.Hardball, Sport.Racquets, Sport.Tennis, Sport.Fitness)
                .associateBy { it::class }

        assertHasAnExampleOfEachConcreteSubclass(Sport::class, instances)

        assertSerializedFormContainsTypeString(instances, Sport::class)
    }

    @Test
    fun `court serializer works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            listOf(
                Court.Court1, Court.Court2, Court.Court3, Court.Court5, Court.Court6,
                Court.Court7, Court.TennisCourt, Court.RacquetsCourt, Court.FitnessClasses,
            ).associateBy { it::class }

        assertHasAnExampleOfEachConcreteSubclass(Court::class, instances)

        assertSerializedFormContainsTypeString(instances, Court::class)
    }

    @Test
    fun `action serializer works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            listOf(Action.Create, Action.Update, Action.Delete, Action.None)
                .associateBy { it::class }

        assertHasAnExampleOfEachConcreteSubclass(Action::class, instances)

        assertSerializedFormContainsTypeString(instances, Action::class)
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

    private fun assertSerializedFormContainsTypeString(instances: Map<KClass<*>, *>, klass: KClass<*>) {
        val mapper = Json.mapper.copy().registerModule(module)
        instances.forEach { (kclass, instance) ->
            val serialized = mapper.writeValueAsString(instance)
            logger.info { "Checking serialized form for $instance of type ${kclass.simpleName}: $serialized" }
            assertThat(serialized).contains(instance.toString())
        }
    }
}
