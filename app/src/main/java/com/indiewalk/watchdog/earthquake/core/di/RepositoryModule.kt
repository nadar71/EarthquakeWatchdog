package eu.indiewalkabout.fridgemanager.core.di

import android.content.Context
import com.indiewalk.watchdog.earthquake.core.data.repository.AppPreferencesRepositoryImpl
import com.indiewalk.watchdog.earthquake.core.data.repository.FilterPreferencesRepositoryImpl
import com.indiewalk.watchdog.earthquake.core.data.repository.LocationRepositoryImpl
import com.indiewalk.watchdog.earthquake.core.domain.repository.AppPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.FilterPreferencesRepository
import com.indiewalk.watchdog.earthquake.core.domain.repository.LocationRepository
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedSnapshotDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.FeedWriterDao
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.remote.EarthquakeApi
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.repository.EQRepositoryImpl
import com.indiewalk.watchdog.earthquake.feat_eqslist.domain.repository.EQRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(
        @ApplicationContext context: Context
    ): AppPreferencesRepository = AppPreferencesRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideFilterPreferencesRepository(
        @ApplicationContext context: Context
    ): FilterPreferencesRepository = FilterPreferencesRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context
    ): LocationRepository = LocationRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideEQRepository(
        earthquakeDao: EarthquakeDao,
        feedSnapshotDao: FeedSnapshotDao,
        feedWriterDao: FeedWriterDao,
        earthquakeApi: EarthquakeApi,
        appPreferencesRepository: AppPreferencesRepository,
        filterPreferencesRepository: FilterPreferencesRepository
    ): EQRepository {
        return EQRepositoryImpl(
            earthquakeDao,
            feedSnapshotDao,
            feedWriterDao,
            earthquakeApi,
            appPreferencesRepository,
            filterPreferencesRepository
        )
    }
}
