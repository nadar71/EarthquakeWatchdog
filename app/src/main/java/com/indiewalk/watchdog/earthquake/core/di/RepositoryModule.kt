package eu.indiewalkabout.fridgemanager.core.di

import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.data.repository.EarthquakeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFridgeManagerRepository(
        earthquakeDao: EarthquakeDao,
        feedSnapshotDao: EarthquakeDao
    ): EarthquakeRepository {
        return EarthquakeRepositoryImpl(earthquakeDao, feedSnapshotDao)
    }
}