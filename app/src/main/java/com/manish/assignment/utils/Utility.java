package com.manish.assignment.utils;

import android.content.Context;
import android.widget.ImageView;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.manish.assignment.R;
import com.manish.assignment.model.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utility {


    public static List<Video> parseJSON(String str) {
        List<Video> videoList = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(str);
            JSONObject categories = (JSONObject) object.getJSONArray("categories").get(0);
            JSONArray jsonArray =  categories.getJSONArray("videos");
            for (int i = 0; i < jsonArray.length(); i++) {
                Video video = new Video();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("description")) {
                    video.setDescription(jsonObject.getString("description"));
                }
                if (jsonObject.has("subtitle")) {
                    video.setSubtitle(jsonObject.getString("subtitle"));
                }

                if (jsonObject.has("thumb")) {
                    video.setThumb(jsonObject.getString("thumb"));
                }
                if (jsonObject.has("title")) {
                    video.setTitle(jsonObject.getString("title"));
                }

                if (jsonObject.has("sources")) {
                    String string = (String) jsonObject.getJSONArray("sources").get(0);
                    List<String> stringList =new ArrayList<>();
                    stringList.add(string);
                    video.setSources(stringList);
                }

                videoList.add(video);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return videoList;
    }

    public static void loadImage(ImageView view, String url, CircularProgressDrawable progressDrawable) {

        RequestOptions options = new RequestOptions()
                .placeholder(progressDrawable)
                .error(R.drawable.ic_android_black_24dp);

        Glide.with(view.getContext())
                .setDefaultRequestOptions(options)
                .load(url)
                .into(view);
    }

    public static CircularProgressDrawable getProgressDrawable(Context context) {
        CircularProgressDrawable progressDrawable = new CircularProgressDrawable(context);
        progressDrawable.setStrokeWidth(10f);
        progressDrawable.setCenterRadius(50f);
        progressDrawable.start();
        return progressDrawable;
    }
}
