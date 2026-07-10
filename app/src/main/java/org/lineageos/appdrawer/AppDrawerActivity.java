/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.appdrawer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ListAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppDrawerActivity extends Activity {
  private PackageManager packageManager = null;
  private List<ApplicationInfo> applist = null;
  private ApplicationAdapter listadaptor = null;
  private GridView grid;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView((int) R.layout.activity_main);
    this.packageManager = getPackageManager();
    new LoadApplications().execute();
    this.grid = (GridView) findViewById(R.id.grid);
    this.grid.setAdapter((ListAdapter) this.listadaptor);
    this.grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        try {
          String packageName = AppDrawerActivity.this.applist.get(position).packageName;
          Intent intent = AppDrawerActivity.this.packageManager.getLaunchIntentForPackage(packageName);
          if (intent == null) {
            intent = AppDrawerActivity.this.packageManager.getLeanbackLaunchIntentForPackage(packageName);
          }
          if (intent != null) {
            AppDrawerActivity.this.startActivity(intent);
          }
        } catch (ActivityNotFoundException e) {
          Toast.makeText(AppDrawerActivity.this, e.getMessage(),  Toast.LENGTH_LONG).show();
        } catch (Exception e2) {
          Toast.makeText(AppDrawerActivity.this, e2.getMessage(), Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  private class LoadApplications extends AsyncTask<Void, Void, Void> {
    private ProgressDialog progress = null;

    @Override
    protected Void doInBackground(Void... params) {
      applist = new ArrayList<>();

      Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
      mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
      List<ResolveInfo> launchables = packageManager.queryIntentActivities(mainIntent, 0);

      Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
      leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
      launchables.addAll(packageManager.queryIntentActivities(leanbackIntent, 0));

      Set<String> packageNames = new HashSet<>();
      for (ResolveInfo resolveInfo : launchables) {
        if (packageNames.add(resolveInfo.activityInfo.packageName)) {
          applist.add(resolveInfo.activityInfo.applicationInfo);
        }
      }

      Collections.sort(applist, new ApplicationInfo.DisplayNameComparator(packageManager));
      listadaptor = new ApplicationAdapter(AppDrawerActivity.this, R.layout.grid_item, applist);

      return null;
    }

    @Override
    protected void onCancelled() {
      super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void result) {
      grid.setAdapter(listadaptor);
      progress.dismiss();
      super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
      progress = ProgressDialog.show(AppDrawerActivity.this, null, "Loading application info...");
      super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
      super.onProgressUpdate(values);
    }
  }
}
