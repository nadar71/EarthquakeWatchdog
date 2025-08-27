package eu.indiewalkabout.fridgemanager.core.di

import android.content.Context
import com.indiewalk.watchdog.earthquake.feat_eqslist.data.local.db.EarthquakeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEarthquakeDatabase(
        @ApplicationContext context: Context
    ): EarthquakeDatabase {
        return EarthquakeDatabase.getDbInstance(context)
    }

    @Provides
    @Singleton
    fun provideFoodDao(foodDatabase: FoodDatabase) = foodDatabase.foodDbDao()

}

