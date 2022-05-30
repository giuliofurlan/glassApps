/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.giufu.youtube_enterprise.fragments;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giufu.youtube_enterprise.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Fragment with the two column layout.
 */
public class ColumnLayoutFragment extends BaseFragment {

  private static final String IMAGE_KEY = "image_key";
  private static final String TEXT_KEY = "text_key";
  private static final String FOOTER_KEY = "footer_key";
  private static final String TIMESTAMP_KEY = "timestamp_key";
  private static final int TEXT_SIZE = 30;
  private static final int IMAGE_PADDING = 40;

  /**
   * Returns new instance of {@link ColumnLayoutFragment}.
   *
   * @param image is a android image resource to create a imageView on the left column.
   * @param text is a String with the card main text.
   * @param footer is a String with the card footer text.
   * @param timestamp is a String with the card timestamp text.
   */
  public static ColumnLayoutFragment newInstance(String image, String text, String footer,
                                                 String timestamp) {
    final ColumnLayoutFragment myFragment = new ColumnLayoutFragment();

    final Bundle args = new Bundle();
    args.putString(IMAGE_KEY, image);
    args.putString(TEXT_KEY, text);
    args.putString(FOOTER_KEY, footer);
    args.putString(TIMESTAMP_KEY, timestamp);
    myFragment.setArguments(args);

    return myFragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.left_column_layout, container, false);


    if (getArguments() != null) {
      final ImageView imageView = new ImageView(getActivity());
      imageView.setPadding(IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING, IMAGE_PADDING);
      //imageView.setImageResource(getArguments().getInt(IMAGE_KEY));

      try {
        imageView.setImageDrawable(drawableFromUrl(getArguments().getString(IMAGE_KEY)));
      } catch (IOException e) {
        e.printStackTrace();
      }
      final FrameLayout leftColumn = view.findViewById(R.id.left_column);
      leftColumn.addView(imageView);

      final TextView textView = new TextView(getActivity());
      textView.setText(getArguments().getString(TEXT_KEY));
      textView.setTextSize(TEXT_SIZE);
      textView.setTypeface(Typeface.create(getString(R.string.thin_font), Typeface.NORMAL));

      final FrameLayout rightColumn = view.findViewById(R.id.right_column);
      rightColumn.addView(textView);

      final TextView footer = view.findViewById(R.id.footer);
      footer.setText(getArguments().getString(FOOTER_KEY, getString(R.string.empty_string)));

      final TextView timestamp = view.findViewById(R.id.timestamp);
      timestamp.setText(getArguments().getString(TIMESTAMP_KEY, getString(R.string.empty_string)));
    }
    return view;
  }
  public static Drawable drawableFromUrl(String url) throws IOException {
    Bitmap x;

    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.connect();
    InputStream input = connection.getInputStream();

    x = BitmapFactory.decodeStream(input);
    return new BitmapDrawable(Resources.getSystem(), x);
  }


}
