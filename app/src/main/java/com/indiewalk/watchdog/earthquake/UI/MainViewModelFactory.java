package com.indiewalk.watchdog.earthquake.UI;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory{


        private final String listType;

        public MainViewModelFactory(String listType) {
            this.listType = listType;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainViewModel(listType);
        }


}
