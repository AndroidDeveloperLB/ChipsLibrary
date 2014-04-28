/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.android.ex.chips;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.ex.chips.BaseRecipientAdapter.DirectoryListQuery;
import com.android.ex.chips.BaseRecipientAdapter.DirectorySearchParams;
import com.android.ex.chips.Queries.Query;

/**
 * RecipientAlternatesAdapter backs the RecipientEditTextView for managing contacts queried by email or by phone number.
 */
public class RecipientAlternatesAdapter extends CursorAdapter
  {
  static final int                           MAX_LOOKUPS          =50;
  private final LayoutInflater               mLayoutInflater;
  private final long                         mCurrentId;
  private int                                mCheckedItemPosition =-1;
  private final OnCheckedItemChangedListener mCheckedItemChangedListener;
  private static final String                TAG                  ="RecipAlternates";
  public static final int                    QUERY_TYPE_EMAIL     =0;
  public static final int                    QUERY_TYPE_PHONE     =1;
  private Query                              mQuery;

  public interface RecipientMatchCallback
    {
    public void matchesFound(Map<String,RecipientEntry> results);

    /**
     * Called with all addresses that could not be resolved to valid recipients.
     */
    public void matchesNotFound(Set<String> unfoundAddresses);
    }

  public static void getMatchingRecipients(final Context context,final BaseRecipientAdapter adapter,final ArrayList<String> inAddresses,final Account account,final RecipientMatchCallback callback)
    {
    getMatchingRecipients(context,adapter,inAddresses,QUERY_TYPE_EMAIL,account,callback);
    }

  /**
   * Get a HashMap of address to RecipientEntry that contains all contact information for a contact with the provided
   * address, if one exists. This may block the UI, so run it in an async task.
   *
   * @param context
   * Context.
   * @param inAddresses
   * Array of addresses on which to perform the lookup.
   * @param callback
   * RecipientMatchCallback called when a match or matches are found.
   * @return HashMap<String,RecipientEntry>
   */
  public static void getMatchingRecipients(final Context context,final BaseRecipientAdapter adapter,final ArrayList<String> inAddresses,final int addressType,final Account account,final RecipientMatchCallback callback)
    {
    Queries.Query query;
    if(addressType==QUERY_TYPE_EMAIL)
      query=Queries.EMAIL;
    else query=Queries.PHONE;
    final int addressesSize=Math.min(MAX_LOOKUPS,inAddresses.size());
    final HashSet<String> addresses=new HashSet<String>();
    final StringBuilder bindString=new StringBuilder();
    // Create the "?" string and set up arguments.
    for(int i=0;i<addressesSize;i++)
      {
      final Rfc822Token[] tokens=Rfc822Tokenizer.tokenize(inAddresses.get(i).toLowerCase(Locale.getDefault()));
      addresses.add(tokens.length>0 ? tokens[0].getAddress() : inAddresses.get(i));
      bindString.append("?");
      if(i<addressesSize-1)
        bindString.append(",");
      }
    if(Log.isLoggable(TAG,Log.DEBUG))
      Log.d(TAG,"Doing reverse lookup for "+addresses.toString());
    final String[] addressArray=new String[addresses.size()];
    addresses.toArray(addressArray);
    HashMap<String,RecipientEntry> recipientEntries=null;
    Cursor c=null;
    try
      {
      c=context.getContentResolver().query(query.getContentUri(),query.getProjection(),query.getProjection()[Queries.Query.DESTINATION]+" IN ("+bindString.toString()+")",addressArray,null);
      recipientEntries=processContactEntries(c);
      callback.matchesFound(recipientEntries);
      }
    finally
      {
      if(c!=null)
        c.close();
      }
    // See if any entries did not resolve; if so, we need to check other
    // directories
    final Set<String> matchesNotFound=new HashSet<String>();
    if(recipientEntries.size()<addresses.size())
      {
      final List<DirectorySearchParams> paramsList;
      Cursor directoryCursor=null;
      try
        {
        directoryCursor=context.getContentResolver().query(DirectoryListQuery.URI,DirectoryListQuery.PROJECTION,null,null,null);
        if(directoryCursor==null)
          paramsList=null;
        else paramsList=BaseRecipientAdapter.setupOtherDirectories(context,directoryCursor,account);
        }
      finally
        {
        if(directoryCursor!=null)
          directoryCursor.close();
        }
      // Run a directory query for each unmatched recipient.
      final HashSet<String> unresolvedAddresses=new HashSet<String>();
      for(final String address : addresses)
        if(!recipientEntries.containsKey(address))
          unresolvedAddresses.add(address);
      matchesNotFound.addAll(unresolvedAddresses);
      if(paramsList!=null)
        {
        Cursor directoryContactsCursor=null;
        for(final String unresolvedAddress : unresolvedAddresses)
          for(int i=0;i<paramsList.size();i++)
            try
              {
              directoryContactsCursor=doQuery(unresolvedAddress,1,paramsList.get(i).directoryId,account,context.getContentResolver(),query);
              }
            finally
              {
              if(directoryContactsCursor!=null&&directoryContactsCursor.getCount()==0)
                {
                directoryContactsCursor.close();
                directoryContactsCursor=null;
                }
              else break;
              }
        }
      }
    // If no matches found in contact provider or the directories, try the extension
    // matcher.
    // todo (aalbert): This whole method needs to be in the adapter?
    if(adapter!=null)
      {
      final Map<String,RecipientEntry> entries=adapter.getMatchingRecipients(matchesNotFound);
      if(entries!=null&&entries.size()>0)
        {
        callback.matchesFound(entries);
        for(final String address : entries.keySet())
          matchesNotFound.remove(address);
        }
      }
    callback.matchesNotFound(matchesNotFound);
    }

  private static HashMap<String,RecipientEntry> processContactEntries(final Cursor c)
    {
    final HashMap<String,RecipientEntry> recipientEntries=new HashMap<String,RecipientEntry>();
    if(c!=null&&c.moveToFirst())
      do
        {
        final String address=c.getString(Queries.Query.DESTINATION);
        final RecipientEntry newRecipientEntry=RecipientEntry.constructTopLevelEntry(c.getString(Queries.Query.NAME),c.getInt(Queries.Query.DISPLAY_NAME_SOURCE),c.getString(Queries.Query.DESTINATION),c.getInt(Queries.Query.DESTINATION_TYPE),c.getString(Queries.Query.DESTINATION_LABEL),c.getLong(Queries.Query.CONTACT_ID),c.getLong(Queries.Query.DATA_ID),c.getString(Queries.Query.PHOTO_THUMBNAIL_URI),true,false /*
                                                                                                                                                                                                                                                                                                                                                                                                                              * isGalContact
                                                                                                                                                                                                                                                                                                                                                                                                                              * TODO(skennedy) We should
                                                                                                                                                                                                                                                                                                                                                                                                                              * look these up eventually
                                                                                                                                                                                                                                                                                                                                                                                                                              */);
        /*
         * In certain situations, we may have two results for one address, where one of the results is just the
         * email address, and the other has a name and photo, so we want to use the better one.
         */
        final RecipientEntry recipientEntry=getBetterRecipient(recipientEntries.get(address),newRecipientEntry);
        recipientEntries.put(address,recipientEntry);
        if(Log.isLoggable(TAG,Log.DEBUG))
          Log.d(TAG,"Received reverse look up information for "+address+" RESULTS: "+" NAME : "+c.getString(Queries.Query.NAME)+" CONTACT ID : "+c.getLong(Queries.Query.CONTACT_ID)+" ADDRESS :"+c.getString(Queries.Query.DESTINATION));
        }
      while(c.moveToNext());
    return recipientEntries;
    }

  /**
   * Given two {@link RecipientEntry}s for the same email address, this will return the one that contains more
   * complete information for display purposes. Defaults to <code>entry2</code> if no significant differences are
   * found.
   */
  static RecipientEntry getBetterRecipient(final RecipientEntry entry1,final RecipientEntry entry2)
    {
    // If only one has passed in, use it
    if(entry2==null)
      return entry1;
    if(entry1==null)
      return entry2;
    // If only one has a display name, use it
    if(!TextUtils.isEmpty(entry1.getDisplayName())&&TextUtils.isEmpty(entry2.getDisplayName()))
      return entry1;
    if(!TextUtils.isEmpty(entry2.getDisplayName())&&TextUtils.isEmpty(entry1.getDisplayName()))
      return entry2;
    // If only one has a display name that is not the same as the destination, use it
    if(!TextUtils.equals(entry1.getDisplayName(),entry1.getDestination())&&TextUtils.equals(entry2.getDisplayName(),entry2.getDestination()))
      return entry1;
    if(!TextUtils.equals(entry2.getDisplayName(),entry2.getDestination())&&TextUtils.equals(entry1.getDisplayName(),entry1.getDestination()))
      return entry2;
    // If only one has a photo, use it
    if((entry1.getPhotoThumbnailUri()!=null||entry1.getPhotoBytes()!=null)&&entry2.getPhotoThumbnailUri()==null&&entry2.getPhotoBytes()==null)
      return entry1;
    if((entry2.getPhotoThumbnailUri()!=null||entry2.getPhotoBytes()!=null)&&entry1.getPhotoThumbnailUri()==null&&entry1.getPhotoBytes()==null)
      return entry2;
    // Go with the second option as a default
    return entry2;
    }

  private static Cursor doQuery(final CharSequence constraint,final int limit,final Long directoryId,final Account account,final ContentResolver resolver,final Query query)
    {
    String constraintStr=constraint.toString();
    final Uri.Builder builder;
    String selection=null;
    String[] selectionArgs=null;
    if(query!=Queries.PHONE)
      builder=query.getContentFilterUri().buildUpon().appendPath(constraintStr).appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,String.valueOf(limit+BaseRecipientAdapter.ALLOWANCE_FOR_DUPLICATES));
    else
      {
      builder=query.getContentUri().buildUpon().appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,String.valueOf(limit+BaseRecipientAdapter.ALLOWANCE_FOR_DUPLICATES));
      selection=Contacts.DISPLAY_NAME+" LIKE ? OR "+Phone.NUMBER+" LIKE ?";
      constraintStr="%"+constraintStr+"%";
      selectionArgs=new String[] {constraintStr,constraintStr};
      }
    if(directoryId!=null)
      builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,String.valueOf(directoryId));
    if(account!=null)
      {
      builder.appendQueryParameter(BaseRecipientAdapter.PRIMARY_ACCOUNT_NAME,account.name);
      builder.appendQueryParameter(BaseRecipientAdapter.PRIMARY_ACCOUNT_TYPE,account.type);
      }
    // final long start = System.currentTimeMillis();
    final Uri uri=builder.build();
    final Cursor cursor=resolver.query(uri,query.getProjection(),selection,selectionArgs,null);
    // final long end = System.currentTimeMillis();
    // if (DEBUG) {
    // Log.d(TAG, "Time for autocomplete (query: " + constraint + ", directoryId: " + directoryId
    // + ", num_of_results: " + (cursor != null ? cursor.getCount() : "null") + "): " + (end - start)
    // + " ms");
    // }
    return cursor;
    }

  public RecipientAlternatesAdapter(final Context context,final long contactId,final long currentId,final OnCheckedItemChangedListener listener)
    {
    this(context,contactId,currentId,QUERY_TYPE_EMAIL,listener);
    }

  public RecipientAlternatesAdapter(final Context context,final long contactId,final long currentId,//
      final int queryMode,final OnCheckedItemChangedListener listener)
    {
    super(context,getCursorForConstruction(context,contactId,queryMode),0);
    mLayoutInflater=LayoutInflater.from(context);
    mCurrentId=currentId;
    mCheckedItemChangedListener=listener;
    if(queryMode==QUERY_TYPE_EMAIL)
      mQuery=Queries.EMAIL;
    else if(queryMode==QUERY_TYPE_PHONE)
      mQuery=Queries.PHONE;
    else
      {
      mQuery=Queries.EMAIL;
      Log.e(TAG,"Unsupported query type: "+queryMode);
      }
    }

  public RecipientAlternatesAdapter(final Context context,final Cursor c,final long currentId,final int queryMode,final OnCheckedItemChangedListener listener)
    {
    super(context,c,0);
    mLayoutInflater=LayoutInflater.from(context);
    mCurrentId=currentId;
    mCheckedItemChangedListener=listener;
    if(queryMode==QUERY_TYPE_EMAIL)
      mQuery=Queries.EMAIL;
    else if(queryMode==QUERY_TYPE_PHONE)
      mQuery=Queries.PHONE;
    else
      {
      mQuery=Queries.EMAIL;
      Log.e(TAG,"Unsupported query type: "+queryMode);
      }
    }

  protected static Cursor getCursorForConstruction(final Context context,final long contactId,final int queryType)
    {
    final Cursor cursor;
    if(queryType==QUERY_TYPE_EMAIL)
      cursor=context.getContentResolver().query(Queries.EMAIL.getContentUri(),Queries.EMAIL.getProjection(),Queries.EMAIL.getProjection()[Queries.Query.CONTACT_ID]+" =?",new String[] {String.valueOf(contactId)},null);
    else cursor=context.getContentResolver().query(Queries.PHONE.getContentUri(),Queries.PHONE.getProjection(),Queries.PHONE.getProjection()[Queries.Query.CONTACT_ID]+" =?",new String[] {String.valueOf(contactId)},null);
    return removeDuplicateDestinations(cursor);
    }

  /**
   * @return a new cursor based on the given cursor with all duplicate destinations removed.
   * It's only intended to use for the alternate list, so... - This method ignores all other fields and dedupe
   * solely on the destination. Normally, if a cursor contains multiple contacts and they have the same
   * destination, we'd still want to show both. - This method creates a MatrixCursor, so all data will be kept
   * in memory. We wouldn't want to do this if the original cursor is large, but it's okay here because the
   * alternate list won't be that big.
   */
  // Visible for testing
  /* package */static Cursor removeDuplicateDestinations(final Cursor original)
    {
    final MatrixCursor result=new MatrixCursor(original.getColumnNames(),original.getCount());
    final HashSet<String> destinationsSeen=new HashSet<String>();
    original.moveToPosition(-1);
    while(original.moveToNext())
      {
      final String destination=original.getString(Query.DESTINATION);
      if(destinationsSeen.contains(destination))
        continue;
      destinationsSeen.add(destination);
      result.addRow(new Object[] {original.getString(Query.NAME),original.getString(Query.DESTINATION),original.getInt(Query.DESTINATION_TYPE),original.getString(Query.DESTINATION_LABEL),original.getLong(Query.CONTACT_ID),original.getLong(Query.DATA_ID),original.getString(Query.PHOTO_THUMBNAIL_URI),original.getInt(Query.DISPLAY_NAME_SOURCE)});
      }
    return result;
    }

  @Override
  public long getItemId(final int position)
    {
    final Cursor c=getCursor();
    if(c.moveToPosition(position))
      c.getLong(Queries.Query.DATA_ID);
    return -1;
    }

  public RecipientEntry getRecipientEntry(final int position)
    {
    final Cursor c=getCursor();
    c.moveToPosition(position);
    return RecipientEntry.constructTopLevelEntry(c.getString(Queries.Query.NAME),c.getInt(Queries.Query.DISPLAY_NAME_SOURCE),c.getString(Queries.Query.DESTINATION),c.getInt(Queries.Query.DESTINATION_TYPE),c.getString(Queries.Query.DESTINATION_LABEL),c.getLong(Queries.Query.CONTACT_ID),c.getLong(Queries.Query.DATA_ID),c.getString(Queries.Query.PHOTO_THUMBNAIL_URI),true,false /*
                                                                                                                                                                                                                                                                                                                                                                                          * isGalContact TODO(skennedy) We should
                                                                                                                                                                                                                                                                                                                                                                                          * look these up eventually
                                                                                                                                                                                                                                                                                                                                                                                          */);
    }

  @Override
  public View getView(final int position,View convertView,final ViewGroup parent)
    {
    final Cursor cursor=getCursor();
    cursor.moveToPosition(position);
    if(convertView==null)
      convertView=newView();
    if(cursor.getLong(Queries.Query.DATA_ID)==mCurrentId)
      {
      mCheckedItemPosition=position;
      if(mCheckedItemChangedListener!=null)
        mCheckedItemChangedListener.onCheckedItemChanged(mCheckedItemPosition);
      }
    bindView(convertView,convertView.getContext(),cursor);
    return convertView;
    }

  // TODO: this is VERY similar to the BaseRecipientAdapter. Can we combine
  // somehow?
  @Override
  public void bindView(final View view,final Context context,final Cursor cursor)
    {
    final int position=cursor.getPosition();
    final TextView display=(TextView)view.findViewById(android.R.id.title);
    final ImageView imageView=(ImageView)view.findViewById(android.R.id.icon);
    final RecipientEntry entry=getRecipientEntry(position);
    if(position==0)
      {
      display.setText(cursor.getString(Queries.Query.NAME));
      display.setVisibility(View.VISIBLE);
      // TODO: see if this needs to be done outside the main thread
      // as it may be too slow to get immediately.
      imageView.setImageURI(entry.getPhotoThumbnailUri());
      imageView.setVisibility(View.VISIBLE);
      }
    else
      {
      display.setVisibility(View.GONE);
      imageView.setVisibility(View.GONE);
      }
    final TextView destination=(TextView)view.findViewById(android.R.id.text1);
    destination.setText(cursor.getString(Queries.Query.DESTINATION));
    final TextView destinationType=(TextView)view.findViewById(android.R.id.text2);
    if(destinationType!=null)
      destinationType.setText(mQuery.getTypeLabel(context.getResources(),cursor.getInt(Queries.Query.DESTINATION_TYPE),cursor.getString(Queries.Query.DESTINATION_LABEL)).toString().toUpperCase(Locale.getDefault()));
    }

  @Override
  public View newView(final Context context,final Cursor cursor,final ViewGroup parent)
    {
    return newView();
    }

  private View newView()
    {
    return mLayoutInflater.inflate(R.layout.chips_recipient_dropdown_item,null);
    }

  /* package */static interface OnCheckedItemChangedListener
    {
    public void onCheckedItemChanged(int position);
    }
  }
