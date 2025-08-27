package eu.indiewalkabout.fridgemanager.core.di

import com.indiewalk.watchdog.earthquake.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.data.local.db.FeedWriterDao
import com.indiewalk.watchdog.earthquake.data.remote.EarthquakeApi
import com.indiewalk.watchdog.earthquake.data.remote.provideHttpClient
import com.indiewalk.watchdog.earthquake.data.repository.EQRepositoryImpl
import com.indiewalk.watchdog.earthquake.data.repository.EarthquakeRepository
import com.indiewalk.watchdog.earthquake.domain.repository.EQRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFridgeManagerRepository(
        earthquakeDao: EarthquakeDao,
        feedSnapshotDao: EarthquakeDao,
        feedWriterDao: FeedWriterDao,
        eerthquakeApi: EarthquakeApi,
        httpClient: HttpClient
    ): EQRepository {
        return EQRepositoryImpl(earthquakeDao, feedSnapshotDao, feedWriterDao, provideHttpClient())
    }
}