package com.example.dell.popularmoviesstage2.utilities;

import com.example.dell.popularmoviesstage2.Movie;
import com.example.dell.popularmoviesstage2.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public final class OpenMovieJsonUtils {

    public static List<Movie> getMovieListFromJson (String movieJsonString) throws JSONException {
        List <Movie> movies = new ArrayList<>();

        final String RESULTS_ARRAY = "results";
        final String MOVIE_ID = "id";
        final String ORIGINAL_TITLE = "original_title";
        final String VOTE_AVERAGE = "vote_average";
        final String POSTER_PATH = "poster_path";
        final String OVERVIEW ="overview";
        final String DATE = "release_date";
        final String MESSAGE_CODE = "cod";

        JSONObject movieJson = new JSONObject(movieJsonString);

        /* Is there an error? */
        if ( movieJson.has(MESSAGE_CODE)) {
            int errorCode = movieJson.getInt(MESSAGE_CODE);
            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return null;
                default:
                    return null;
            }
        }

            JSONArray resultsArray = movieJson.getJSONArray(RESULTS_ARRAY);
            for (int i=0; i<resultsArray.length(); i++){
                JSONObject movieSingleItem = resultsArray.getJSONObject(i);

                int id = movieSingleItem.getInt(MOVIE_ID);
                Double rate = movieSingleItem.getDouble(VOTE_AVERAGE);
                String poster = movieSingleItem.getString(POSTER_PATH);
                String originalTitle = movieSingleItem.getString(ORIGINAL_TITLE);
                String overview = movieSingleItem.getString(OVERVIEW);
                String date = movieSingleItem.getString(DATE);

                Movie movie = new Movie(id,rate,poster,originalTitle,overview,date);
                movies.add(movie);
            }
        return movies;
    }

    public static List<String> getMovieYoutubeKeyFromJson (String trailerJsonString) throws JSONException{
        final String RESULTS_ARRAY = "results";
        final String KEY_ITEM = "key";
        final List<String> keys = new ArrayList<>();

        JSONObject root = new JSONObject(trailerJsonString);
        JSONArray resultsArray = root.getJSONArray(RESULTS_ARRAY);
        for (int i =0 ; i<resultsArray.length(); i++){
            JSONObject trailerInfo = resultsArray.getJSONObject(i);
            String key = trailerInfo.getString(KEY_ITEM);
            keys.add(key);
        }
        return keys;
    }

    public static List<Review> getMovieReviewFromJson (String reviewJsonString) throws JSONException{
        final String RESULTS_ARRAY = "results";
        final String AUTHOR_ITEM = "author";
        final String CONTENT_ITEM = "content";

        List<Review> reviews = new ArrayList<>();

        JSONObject root = new JSONObject(reviewJsonString);
        JSONArray resultsArray = root.getJSONArray(RESULTS_ARRAY);
        for (int i = 0 ; i<resultsArray.length() ; i++){
            JSONObject reviewItem = resultsArray.getJSONObject(i);
            String author = reviewItem.getString(AUTHOR_ITEM);
            String review = reviewItem.getString(CONTENT_ITEM);
            reviews.add(new Review(review,author));
        }

        return reviews;
    }
}
