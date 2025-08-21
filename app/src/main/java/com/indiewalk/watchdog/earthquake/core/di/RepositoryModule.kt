package eu.indiewalkabout.fridgemanager.core.di

import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDbDao
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
        earthquakeDbDao: EarthquakeDbDao
    ): EarthquakeRepository {
        return EarthquakeRepositoryImpl(earthquakeDbDao)
    }
}