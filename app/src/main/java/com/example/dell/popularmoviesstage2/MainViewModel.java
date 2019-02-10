package com.example.dell.popularmoviesstage2;

import android.app.Application;

import com.example.dell.popularmoviesstage2.database.AppDatabase;
import com.example.dell.popularmoviesstage2.database.MovieEntry;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<MovieEntry>> movies;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getsInstance(this.getApplication());
        movies = database.movieDao().loadAllMovies();
    }

    public LiveData<List<MovieEntry>> getTasks() {
        return movies;
    }
}
