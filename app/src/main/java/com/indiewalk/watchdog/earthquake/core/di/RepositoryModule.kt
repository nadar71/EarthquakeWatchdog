package eu.indiewalkabout.fridgemanager.core.di

import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedSnapshotDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedWriterDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.EarthquakeApi
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.provideHttpClient
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.repository.EQRepositoryImpl
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
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
        feedSnapshotDao: FeedSnapshotDao,
        feedWriterDao: FeedWriterDao,
        earthquakeApi: EarthquakeApi
    ): EQRepository {
        return EQRepositoryImpl(earthquakeDao, feedSnapshotDao, feedWriterDao, earthquakeApi)
    }
}