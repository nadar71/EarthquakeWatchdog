package com.indiewalk.watchdog.earthquake.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlin.reflect.KClass

class AppNavigator<T : Any>(
    initialBackStack: MutableList<T>,
    private val topLevelDestinations: Set<KClass<out T>>
) {
    private val mutableBackStack = initialBackStack

    val backStack: List<T>
        get() = mutableBackStack

    val currentDestination: T
        get() = mutableBackStack.last()

    val canNavigateBack: Boolean
        get() = mutableBackStack.size > 1

    fun navigateTo(destination: T) {
        mutableBackStack += destination
    }

    fun replaceWith(destination: T) {
        mutableBackStack.clear()
        mutableBackStack += destination
    }

    fun switchTopLevel(destination: T) {
        if (currentDestination == destination) return

        val existingIndex = mutableBackStack.indexOfLast { it::class == destination::class }
        if (existingIndex >= 0) {
            while (mutableBackStack.lastIndex > existingIndex) {
                mutableBackStack.removeAt(mutableBackStack.lastIndex)
            }
            return
        }

        while (mutableBackStack.size > 1 &&
            currentDestination::class !in topLevelDestinations
        ) {
            mutableBackStack.removeAt(mutableBackStack.lastIndex)
        }

        mutableBackStack += destination
    }

    fun navigateBack() {
        if (!canNavigateBack) return
        mutableBackStack.removeAt(mutableBackStack.lastIndex)
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator<AppDestination> {
    val backStack = remember { mutableStateListOf<AppDestination>(AppDestination.Intro) }
    return remember(backStack) {
        AppNavigator(
            initialBackStack = backStack,
            topLevelDestinations = appTopLevelDestinationClasses
        )
    }
}
