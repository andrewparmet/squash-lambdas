package com.parmet.squashlambdas.notify

import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.cal.Action
import mu.KotlinLogging
import org.junit.Test
import org.reflections.Reflections
import java.lang.reflect.Modifier
import java.time.Instant
import kotlin.reflect.KClass

class GsonUtilsTest {
    private val logger = KotlinLogging.logger { }

    private val match =
        Match(
            Court.Court2,
            Instant.parse("2018-03-26T22:45:00Z"),
            Instant.parse("2018-03-26T23:30:00Z"),
            setOf("Philipp Rimmler"))

    private val clinic =
        Clinic(
            Court.Court2,
            Instant.parse("2018-03-26T22:45:00Z"),
            Instant.parse("2018-03-26T23:30:00Z"))

    @Test
    fun `activity adapter works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            mapOf(
                Match::class to match,
                Clinic::class to clinic)

        assertHasAnExampleOfEachConcreteSubclass(Activity::class, instances)

        assertSerializedFormContainsTypeTag(instances, Activity::class, ACTIVITY_ADAPTER)
    }

    @Test
    fun `sport adapter works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            listOf(Sport.Squash, Sport.Hardball, Sport.Racquets, Sport.Tennis)
                .associate { it::class to it }

        assertHasAnExampleOfEachConcreteSubclass(Sport::class, instances)

        assertSerializedFormContainsTypeTag(instances, Sport::class, SPORT_ADAPTER)
    }

    @Test
    fun `court adapter works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            listOf(
                Court.Court1, Court.Court2, Court.Court3, Court.Court5, Court.Court6,
                Court.Court7, Court.TennisCourt, Court.RacquetsCourt
            ).associate { it::class to it }

        assertHasAnExampleOfEachConcreteSubclass(Court::class, instances)

        assertSerializedFormContainsTypeTag(instances, Court::class, COURT_ADAPTER)
    }

    @Test
    fun `action adapter works for all subclasses`() {
        val instances: Map<KClass<*>, *> =
            listOf(Action.Create, Action.Update, Action.Delete, Action.None)
                .associate { it::class to it }

        assertHasAnExampleOfEachConcreteSubclass(Action::class, instances)

        assertSerializedFormContainsTypeTag(instances, Action::class, ACTION_ADAPTER)
    }

    private fun assertHasAnExampleOfEachConcreteSubclass(klass: KClass<*>, instances: Map<KClass<*>, *>) {
        val jClassInstances = instances.mapKeys { it.key.java }

        Reflections("com.parmet").getSubTypesOf(klass.java)
            .filter { it.isConcrete() }
            .forEach {
                logger.info { "Looking for an instance of $it" }
                assertThat(jClassInstances[it]).isNotNull()
            }
    }

    private fun assertSerializedFormContainsTypeTag(
        instances: Map<KClass<*>, *>,
        klass: KClass<*>,
        adapter: TypeAdapter<*>
    ) {
        instances.forEach { name, instance ->
            val serialized =
                GsonBuilder()
                    .registerTypeHierarchyAdapter(klass.java, adapter)
                    .create()
                    .toJson(instance)
            logger.info { "Checking serialized form for $instance of type ${name.simpleName}: $serialized" }

            assertThat(serialized).contains("\"type\":\"${name.simpleName}\"")
        }
    }

    private fun Class<*>.isConcrete() = !Modifier.isAbstract(modifiers)
}
