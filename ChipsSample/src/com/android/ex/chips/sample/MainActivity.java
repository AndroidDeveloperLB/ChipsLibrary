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
import java.util.Collection;
import java.util.List;
import java.util.Random;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Rfc822Tokenizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.MultiAutoCompleteTextView;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEditTextView.IChipListener;
import com.android.ex.chips.RecipientEntry;

public class MainActivity extends Activity
  {
  // code and sample found here:https://plus.google.com/+RichHyndman/posts/TSxaARVsRjF
  // and imported from here:https://android.googlesource.com/platform/frameworks/ex/+/android-sdk-support_r11
  private RecipientEditTextView mPhoneRetv;
  final Random                  mRandom =new Random();

  @Override
  protected void onCreate(final Bundle savedInstanceState)
    {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // email textview sample
    final RecipientEditTextView emailRetv=(RecipientEditTextView)findViewById(R.id.email_retv);
    emailRetv.setTokenizer(new Rfc822Tokenizer());
    emailRetv.setAdapter(new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_EMAIL,this)
      {});
    // contacts textview sample
    mPhoneRetv=(RecipientEditTextView)findViewById(R.id.phone_retv);
    mPhoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    final BaseRecipientAdapter adapter=new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE,this)
      {};
    mPhoneRetv.setThreshold(2);
    mPhoneRetv.setAdapter(adapter);
    mPhoneRetv.setChipListener(new IChipListener()
      {
        @Override
        public void onDataChanged()
          {
          android.util.Log.d("Applog"," \nphones view data changed:");
          printAllItems(mPhoneRetv);
          }
      });
    emailRetv.setChipListener(new IChipListener()
      {
        @Override
        public void onDataChanged()
          {
          android.util.Log.d("Applog","emails view data changed:");
          printAllItems(emailRetv);
          }
      });
    // NOTE: the query should run on a background thread. this is just for demonstration
    final List<RecipientEntry> allEntries=adapter.doQuery();
    android.util.Log.d("Applog","entries count:"+allEntries.size());
    findViewById(R.id.addRecipientButton).setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(final View v)
          {
          final View view=getCurrentFocus();
          if(view instanceof RecipientEditTextView)
            addRandomRecipient((RecipientEditTextView)view,allEntries);
          }
      });
    findViewById(R.id.removeRecipientButton).setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(final View v)
          {
          final View view=getCurrentFocus();
          if(view instanceof RecipientEditTextView)
            removeRandomRecipient((RecipientEditTextView)view);
          }
      });
    }

  private void removeRandomRecipient(final RecipientEditTextView view)
    {
    final List<RecipientEntry> recipients=new ArrayList<RecipientEntry>(view.getRecipients().values());
    final int count=recipients.size();
    if(count==0)
      return;
    final int idx=mRandom.nextInt(count);
    view.removeRecipient(recipients.get(idx));
    // Log.d("Applog","data after removing a random recipient:");
    // printAllItems(view);
    }

  private void addRandomRecipient(final RecipientEditTextView view,final List<RecipientEntry> allEntries)
    {
    final int idx=mRandom.nextInt(allEntries.size());
    view.addRecipient(allEntries.get(idx));
    // Log.d("Applog","data after adding a random recipient:");
    // printAllItems(view);
    }

  private void printAllItems(final RecipientEditTextView view)
    {
    final Collection<RecipientEntry> recipients=view.getRecipients().values();
    for(final RecipientEntry entry : recipients)
      android.util.Log.d("Applog",entry.getContactId()+" "+entry.getDisplayName());
    }

  /**
   * This method helps to retrieve the ui component size after it was create during the onCreate method
   *
   * @param view
   * - the view to get it's size
   * @param runnable
   */
  public static void runJustBeforeBeingDrawn(final View view,final Runnable runnable)
    {
    final OnPreDrawListener preDrawListener=new OnPreDrawListener()
      {
        @Override
        public boolean onPreDraw()
          {
          view.getViewTreeObserver().removeOnPreDrawListener(this);
          runnable.run();
          return true;
          }
      };
    view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
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
        url="https://play.google.com/store/apps/developer?id=Liran+Barsisa";
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
