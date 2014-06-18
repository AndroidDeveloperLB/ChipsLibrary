/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.ex.chips.sample;
import java.util.ArrayList;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity
  {
  // code and sample found here:https://plus.google.com/+RichHyndman/posts/TSxaARVsRjF
  // and imported from here:https://android.googlesource.com/platform/frameworks/ex/+/android-sdk-support_r11
  @Override
  protected void onCreate(final Bundle savedInstanceState)
    {
    super.onCreate(savedInstanceState);
    getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    final ArrayList<String> itemList=new ArrayList<String>();
    itemList.add("Contacts Recipients");
    itemList.add("Customized Recipients");
    final ArrayAdapter<String> navAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,itemList);
    getActionBar().setListNavigationCallbacks(navAdapter,new OnNavigationListener()
      {
        @Override
        public boolean onNavigationItemSelected(final int itemPosition,final long itemId)
          {
          switch(itemPosition)
            {
            case 0:
              getFragmentManager().beginTransaction().replace(android.R.id.content,new ContactsRecipientsFragment()).commit();
              break;
            case 1:
              getFragmentManager().beginTransaction().replace(android.R.id.content,new CustomRecipientFragment()).commit();
              break;
            }
          return true;
          }
      });
    }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu)
    {
    getMenuInflater().inflate(R.menu.activity_main,menu);
    return super.onCreateOptionsMenu(menu);
    }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item)
    {
    String url=null;
    switch(item.getItemId())
      {
      case R.id.menuItem_all_my_apps:
        url="https://play.google.com/store/apps/developer?id=AndroidDeveloperLB";
        break;
      case R.id.menuItem_all_my_repositories:
        url="https://github.com/AndroidDeveloperLB";
        break;
      case R.id.menuItem_current_repository_website:
        url="https://github.com/AndroidDeveloperLB/ChipsLibrary";
        break;
      }
    if(url==null)
      return true;
    final Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    startActivity(intent);
    return true;
    }
  }
