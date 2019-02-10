package com.example.dell.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dell.popularmoviesstage2.Adapters.MovieAdapter;
import com.example.dell.popularmoviesstage2.Adapters.ReviewAdapter;
import com.example.dell.popularmoviesstage2.Adapters.TrailerAdapter;
import com.example.dell.popularmoviesstage2.database.AppDatabase;
import com.example.dell.popularmoviesstage2.database.MovieEntry;
import com.example.dell.popularmoviesstage2.utilities.NetworkUtils;
import com.example.dell.popularmoviesstage2.utilities.OpenMovieJsonUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DetailsActivity extends AppCompatActivity implements LoaderCallbacks<List<String>>
        , TrailerAdapter.ListItemClickListener {

    private ImageView mShowPosterImageView;
    private TextView mShowRateTextView;
    private TextView mShowDateTextView;
    private TextView mShowTitleTextView;
    private TextView mOverviewTextView;
    private ImageButton mFavoriteButton;
    private TextView mNoReviewsMessageTextView;
    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private RecyclerView trailerRecyclerView;
    private TrailerAdapter trailerAdapter;
    private TextView mNetworkConnectionMessage;
    private TextView mNoTrailersMessage;

    private NetworkInfo networkInfo;
    private Movie movie;
    private AppDatabase mDb;
    private boolean isFavorite = true;


    private static final int MOVIE_TRAILER_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mShowPosterImageView = findViewById(R.id.show_poster_iv);
        mShowRateTextView = findViewById(R.id.show_rate_tv);
        mShowDateTextView = findViewById(R.id.show_date_tv);
        mShowTitleTextView = findViewById(R.id.tv_show_movie_title);
        mOverviewTextView = findViewById(R.id.tv_show_movie_overView);
        mNoReviewsMessageTextView = findViewById(R.id.tv_no_reviews_message);
        mFavoriteButton = findViewById(R.id.like_Button);
        mNetworkConnectionMessage = findViewById(R.id.tv_no_network_connection_message);
        mNoTrailersMessage = findViewById(R.id.tv_no_trailers_message);

        final Intent intent = getIntent();
        movie = intent.getParcelableExtra("movie");

        final int movieId = movie.getMovieId();
        final Double rate = movie.getMovieRate();
        final String date = movie.getMovieDate();
        final String title = movie.getMovieTitle();
        final String overview = movie.getMovieOverview();
        final String poster = movie.getMoviePoster();

        String posterUrl = MovieAdapter.getImageUri(movie);
        Picasso.with(this)
                .load(posterUrl)
                .error(R.drawable.noimage)
                .into(mShowPosterImageView);

        mShowRateTextView.setText(Double.toString(rate));
        mShowDateTextView.setText(date);
        mShowTitleTextView.setText(title);
        mOverviewTextView.setText(overview);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkInfo = connMgr.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {
            mNetworkConnectionMessage.setVisibility(View.GONE);
            mNoTrailersMessage.setVisibility(View.GONE);

            LoaderCallbacks<List<String>> callbacks = DetailsActivity.this;
            getSupportLoaderManager().initLoader(MOVIE_TRAILER_LOADER_ID, null, callbacks);
            reviewsAsyncTask.execute();
        } else {
            mNetworkConnectionMessage.setVisibility(View.VISIBLE);
            mNoTrailersMessage.setVisibility(View.VISIBLE);
        }

        mDb = AppDatabase.getsInstance(getApplicationContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final MovieEntry checkFavoriteMovieEntry = mDb.movieDao().loadMovieById(movieId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // if it dose not exist in the favorite movie table , set the flag to false and the image view to be an empty heart :L
                        if (checkFavoriteMovieEntry == null) {
                            isFavorite = false;
                            mFavoriteButton.setBackground(getDrawable(R.drawable.icon_add_favorite));
                        }
                    }
                });
            }

        });


        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFavorite) {

                    final MovieEntry newMovieEntry = new MovieEntry(movieId, rate, poster, title, overview, date);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.movieDao().insertMovie(newMovieEntry);
                        }
                    });

                    mFavoriteButton.setBackground(getDrawable(R.drawable.icon_unfavorite));
                    isFavorite = true;
                    Toast.makeText(DetailsActivity.this, "Added to your favorite list",
                            Toast.LENGTH_SHORT).show();
                } else {

                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            MovieEntry movieEntry = mDb.movieDao().loadMovieById(movieId);
                            mDb.movieDao().deleteMovie(movieEntry);
                        }
                    });

                    isFavorite = false;
                    mFavoriteButton.setBackground(getDrawable(R.drawable.icon_add_favorite));
                    Toast.makeText(DetailsActivity.this, "Removed from your favorite list",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView = findViewById(R.id.reviews_rv);
        adapter = new ReviewAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        trailerRecyclerView = findViewById(R.id.trailers_rv);
        trailerAdapter = new TrailerAdapter(this, this);
        trailerRecyclerView.setAdapter(trailerAdapter);
        trailerRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        trailerRecyclerView.setLayoutManager(layoutManager);

    }

    @NonNull
    @Override
    public Loader<List<String>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<List<String>>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Nullable
            @Override
            public List<String> loadInBackground() {

                String id = Integer.toString(movie.getMovieId());
                URL trailerUrl = NetworkUtils.buildTrailerRequestUrl(id);

                String trailerJsonResponse = null;
                try {
                    trailerJsonResponse = NetworkUtils.getResponseFromHttpUrl(trailerUrl);
                } catch (IOException e1) {
                    e1.printStackTrace();

                }

                List<String> youtubeKeys;
                try {
                    youtubeKeys = OpenMovieJsonUtils.getMovieYoutubeKeyFromJson(trailerJsonResponse);

                    return youtubeKeys;
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<String>> loader, List<String> data) {
        trailerAdapter.setTrailerData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<String>> loader) {

    }

    private AsyncTask<Void, Void, List<Review>> reviewsAsyncTask = new AsyncTask<Void, Void, List<Review>>() {
        @Override
        protected List<Review> doInBackground(Void... voids) {

            String id = Integer.toString(movie.getMovieId());
            URL reviewUrl = NetworkUtils.buildReviewsRequestUrl(id);

            String reviewJsonResponse = null;

            try {
                reviewJsonResponse = NetworkUtils.getResponseFromHttpUrl(reviewUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<Review> reviews = null;
            try {
                reviews = OpenMovieJsonUtils.getMovieReviewFromJson(reviewJsonResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return reviews;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews.size() == 0 || reviews == null) {
                showNoReviewsMessage();

            } else {
                showReviews();
                adapter.setReviewData(reviews);
            }
        }
    };

    void showReviews() {
        recyclerView.setVisibility(View.VISIBLE);
        mNoReviewsMessageTextView.setVisibility(View.GONE);
    }

    void showNoReviewsMessage() {
        recyclerView.setVisibility(View.GONE);
        mNoReviewsMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListItemClick(String youtubeKey) {
        String videoPath = "https://www.youtube.com/watch?v=" + youtubeKey;
        Uri youTubeUri = Uri.parse(videoPath);
        Intent openYoutubeTrailerIntent = new Intent(Intent.ACTION_VIEW, youTubeUri);
        startActivity(openYoutubeTrailerIntent);
    }
}
