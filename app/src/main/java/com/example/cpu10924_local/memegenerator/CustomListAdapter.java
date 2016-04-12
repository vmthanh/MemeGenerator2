package com.example.cpu10924_local.memegenerator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by CPU10924-local on 3/23/2016.
 */
public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.CustomViewHolder>{
    private File[] files;
    private OnItemClickListener itemClickListener;
    public CustomListAdapter(File[] files)
    {
        this.files = files;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private LinearLayout mainHolder;
        private ImageView memeImage;
        private TextView memeImageName;

        public CustomViewHolder(View itemView) {
            super(itemView);
            mainHolder =(LinearLayout) itemView.findViewById(R.id.mainHolder);
            mainHolder.setOnClickListener(this);
            memeImage = (ImageView)itemView.findViewById(R.id.memeImage);
            memeImageName = (TextView)itemView.findViewById(R.id.memeImageName);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener!=null)
            {
                itemClickListener.onItemClick(itemView,getPosition());
            }
        }
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
    public void setOnItemClickListener(final OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
    @Override
    public int getItemCount() {
        if (files!=null){
            return files.length;
        }else{
            return 0;
        }

    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
            holder.memeImageName.setText(files[position].getName());
            LoadImage loadImage = new LoadImage(holder.memeImage);
            loadImage.execute(files[position].getPath());


    }
    private Bitmap decodeSampleBitmapFromUri(String path, int Width, int Height){
        Bitmap bm;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, Width, Height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    private int calculateInSampleSize(BitmapFactory.Options options,int Width, int Height)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > Height || width > Width) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > Height
                    && (halfWidth / inSampleSize) > Width) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private class LoadImage extends AsyncTask<String,Void,Bitmap>{
        private final WeakReference<ImageView> imageViewReference;
        public LoadImage(ImageView imageView){
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return decodeSampleBitmapFromUri(params[0], 30, 30);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_custom_list,parent,false);
        return new CustomViewHolder(v);
    }
}
