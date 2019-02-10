package com.example.dell.popularmoviesstage2.utilities;

import com.example.dell.popularmoviesstage2.Movie;
import com.example.dell.popularmoviesstage2.database.MovieEntry;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMoviesUtils {

    public static List<Movie> getFavoriteMovies(List<MovieEntry> movieEntries){
        List<Movie> movies = new ArrayList<>();

        int length = movieEntries.size();
        for (int i = 0 ; i<length;i++){
            MovieEntry movieEntry = movieEntries.get(i);
             int mMovieId = movieEntry.getMovieId();
             Double rate = movieEntry.getMovieRate();
             String poster = movieEntry.getMoviePoster();
             String originalTitle = movieEntry.getMovieTitle();
             String overview = movieEntry.getMovieOverview();
             String date = movieEntry.getMovieDate();
            Movie movie = new Movie(mMovieId,rate,poster,originalTitle,overview,date);
            movies.add(movie);
        }
        return movies;

    }
}
