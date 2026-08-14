package com.indiewalk.watchdog.earthquake.feat_statistics.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.MIGRATION_4_5
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.MIGRATION_5_6
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EarthquakeDatabase::class.java
    )

    @Test
    fun migration4To5AddsEmptyStatisticsCacheWithoutChangingExistingFeeds() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """INSERT INTO feed_snapshot (
                    generated, url, title, api, count, status,
                    minLon, minLat, minDepth, maxLon, maxLat, maxDepth
                ) VALUES (1, 'url', 'title', '1.0', 1, 200,
                    NULL, NULL, NULL, NULL, NULL, NULL)""".trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5).use { database ->
            assertEquals(1, database.singleInt("SELECT COUNT(*) FROM feed_snapshot"))
            assertEquals(0, database.singleInt("SELECT COUNT(*) FROM statistics_cache"))
        }
    }

    @Test
    fun migration5To6PreservesCachedSummaryAndLeavesInsightsEmpty() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """INSERT INTO statistics_cache (
                    cacheId, todayCount, weekCount, monthCount, yearCount,
                    threshold, retrievedAtEpochMillis, windowsJson,
                    strongestEventJson, nearestEventJson
                ) VALUES (1, 10, 20, 30, 40, 2.5, 1000, '{}', NULL, NULL)""".trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6).use { database ->
            database.query(
                "SELECT todayCount, insightsJson FROM statistics_cache WHERE cacheId = 1"
            ).use { cursor ->
                check(cursor.moveToFirst())
                assertEquals(10, cursor.getInt(0))
                assertEquals(true, cursor.isNull(1))
            }
        }
    }

    private fun SupportSQLiteDatabase.singleInt(query: String): Int =
        this.query(query).use { cursor ->
            check(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private companion object {
        const val TEST_DB = "statistics-migration-test"
    }
}
