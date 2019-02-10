package com.example.dell.popularmoviesstage2.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dell.popularmoviesstage2.Movie;
import com.example.dell.popularmoviesstage2.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{
    private List<Movie> mMovies;
    private Context mContext;
    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onListItemClick(Movie clickedMovie);
    }

    public MovieAdapter(Context context, ListItemClickListener listener) {
        this.mContext = context;
        this.mOnClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       private ImageView poster;

        public ViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.iv_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            Movie clickedItem = mMovies.get(clickedPosition);
            mOnClickListener.onListItemClick(clickedItem);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        ImageView posterImageView = holder.poster;

        String posterUri = getImageUri(movie);

        Picasso.with(mContext)
                .load(posterUri)
                .error(R.drawable.noimage)
                .into(posterImageView);

    }

    @Override
    public int getItemCount() {
        if(null==mMovies){
            return 0;
        }
        return mMovies.size();
    }

    public void setMovieData(List<Movie> movieData) {
        mMovies = movieData;
        notifyDataSetChanged();
    }

    public static String getImageUri (Movie movie){

        final String BASE_URL = "image.tmdb.org";
        final String SIZE = "w185";
        String posterPath = movie.getMoviePoster();
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(BASE_URL)
                .appendPath("t")
                .appendPath("p")
                .appendPath(SIZE)
                .appendEncodedPath(posterPath);

        return builder.build().toString();
    }

}