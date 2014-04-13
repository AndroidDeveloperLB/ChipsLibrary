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
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.DisplayNameSources;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

/**
 * Represents one entry inside recipient auto-complete list.
 */
public class RecipientEntry
{
/* package */static final int INVALID_CONTACT          =-1;
/**
 * A GENERATED_CONTACT is one that was created based entirely on information passed in to the RecipientEntry from an
 * external source that is not a real contact.
 */
/* package */static final int GENERATED_CONTACT        =-2;
/** Used when {@link #mDestinationType} is invalid and thus shouldn't be used for display. */
/* package */static final int INVALID_DESTINATION_TYPE =-1;
public static final int       ENTRY_TYPE_PERSON        =0;
public static final int       ENTRY_TYPE_SIZE          =1;
private final int             mEntryType;
/**
 * True when this entry is the first entry in a group, which should have a photo and display name, while the second
 * or later entries won't.
 */
private final boolean         mIsFirstLevel;
private final String          mDisplayName;
/** Destination for this contact entry. Would be an email address or a phone number. */
private final String          mDestination;
/** Type of the destination like {@link Email#TYPE_HOME} */
private final int             mDestinationType;
/**
 * Label of the destination which will be used when type was {@link Email#TYPE_CUSTOM}. Can be null when {@link #mDestinationType} is {@link #INVALID_DESTINATION_TYPE}.
 */
private final String          mDestinationLabel;
/** ID for the person */
private final long            mContactId;
/** ID for the destination */
private final long            mDataId;
private final boolean         mIsDivider;
private final Uri             mPhotoThumbnailUri;
private final boolean         mIsValid;
/**
 * This can be updated after this object being constructed, when the photo is fetched from remote directories.
 */
private byte[]                mPhotoBytes;
private final boolean         mIsGalContact;

private RecipientEntry(final int entryType,final String displayName,final String destination,final int destinationType,final String destinationLabel,final long contactId,final long dataId,final Uri photoThumbnailUri,final boolean isFirstLevel,final boolean isValid,final boolean isGalContact)
  {
  mEntryType=entryType;
  mIsFirstLevel=isFirstLevel;
  mDisplayName=displayName;
  mDestination=destination;
  mDestinationType=destinationType;
  mDestinationLabel=destinationLabel;
  mContactId=contactId;
  mDataId=dataId;
  mPhotoThumbnailUri=photoThumbnailUri;
  mPhotoBytes=null;
  mIsDivider=false;
  mIsValid=isValid;
  mIsGalContact=isGalContact;
  }

public boolean isValid()
  {
  return mIsValid;
  }

/**
 * Determine if this was a RecipientEntry created from recipient info or an entry from contacts.
 */
public static boolean isCreatedRecipient(final long id)
  {
  return id==RecipientEntry.INVALID_CONTACT||id==RecipientEntry.GENERATED_CONTACT;
  }

/**
 * Construct a RecipientEntry from just an address that has been entered. This address has not been resolved to a
 * contact and therefore does not have a contact id or photo.
 */
public static RecipientEntry constructFakeEntry(final String address,final boolean isValid)
  {
  final Rfc822Token[] tokens=Rfc822Tokenizer.tokenize(address);
  final String tokenizedAddress=tokens.length>0 ? tokens[0].getAddress() : address;
  return new RecipientEntry(ENTRY_TYPE_PERSON,tokenizedAddress,tokenizedAddress,INVALID_DESTINATION_TYPE,null,INVALID_CONTACT,INVALID_CONTACT,null,true,isValid,false /* isGalContact */);
  }

/**
 * Construct a RecipientEntry from just a phone number.
 */
public static RecipientEntry constructFakePhoneEntry(final String phoneNumber,final boolean isValid)
  {
  return new RecipientEntry(ENTRY_TYPE_PERSON,phoneNumber,phoneNumber,INVALID_DESTINATION_TYPE,null,INVALID_CONTACT,INVALID_CONTACT,null,true,isValid,false /* isGalContact */);
  }

/**
 * @return the display name for the entry. If the display name source is larger than {@link DisplayNameSources#PHONE} we use the contact's display name, but if not, i.e. the display name
 * came from an email address or a phone number, we don't use it to avoid confusion and just use the
 * destination instead.
 */
private static String pickDisplayName(final int displayNameSource,final String displayName,final String destination)
  {
  return displayNameSource>DisplayNameSources.PHONE ? displayName : destination;
  }

/**
 * Construct a RecipientEntry from just an address that has been entered with both an associated display name. This
 * address has not been resolved to a contact and therefore does not have a contact id or photo.
 */
public static RecipientEntry constructGeneratedEntry(final String display,final String address,final boolean isValid)
  {
  return new RecipientEntry(ENTRY_TYPE_PERSON,display,address,INVALID_DESTINATION_TYPE,null,GENERATED_CONTACT,GENERATED_CONTACT,null,true,isValid,false /* isGalContact */);
  }

public static RecipientEntry constructTopLevelEntry(final String displayName,final int displayNameSource,final String destination,final int destinationType,final String destinationLabel,final long contactId,final long dataId,final Uri photoThumbnailUri,final boolean isValid,final boolean isGalContact)
  {
  return new RecipientEntry(ENTRY_TYPE_PERSON,pickDisplayName(displayNameSource,displayName,destination),destination,destinationType,destinationLabel,contactId,dataId,photoThumbnailUri,true,isValid,isGalContact);
  }

public static RecipientEntry constructTopLevelEntry(final String displayName,final int displayNameSource,final String destination,final int destinationType,final String destinationLabel,final long contactId,final long dataId,final String thumbnailUriAsString,final boolean isValid,final boolean isGalContact)
  {
  return new RecipientEntry(ENTRY_TYPE_PERSON,pickDisplayName(displayNameSource,displayName,destination),destination,destinationType,destinationLabel,contactId,dataId,thumbnailUriAsString!=null ? Uri.parse(thumbnailUriAsString) : null,true,isValid,isGalContact);
  }

public static RecipientEntry constructSecondLevelEntry(final String displayName,final int displayNameSource,final String destination,final int destinationType,final String destinationLabel,final long contactId,final long dataId,final String thumbnailUriAsString,final boolean isValid,final boolean isGalContact)
  {
  return new RecipientEntry(ENTRY_TYPE_PERSON,pickDisplayName(displayNameSource,displayName,destination),destination,destinationType,destinationLabel,contactId,dataId,thumbnailUriAsString!=null ? Uri.parse(thumbnailUriAsString) : null,false,isValid,isGalContact);
  }

@Override
public int hashCode()
  {
  return mDestination.hashCode()+(int)mContactId;
  }

@Override
public boolean equals(final Object o)
  {
  final RecipientEntry other=(RecipientEntry)o;
  return o!=null&&mDestination.equals(other.mDestination)&&mContactId==other.mContactId;
  }

public int getEntryType()
  {
  return mEntryType;
  }

public String getDisplayName()
  {
  return mDisplayName;
  }

public String getDestination()
  {
  return mDestination;
  }

public int getDestinationType()
  {
  return mDestinationType;
  }

public String getDestinationLabel()
  {
  return mDestinationLabel;
  }

public long getContactId()
  {
  return mContactId;
  }

public long getDataId()
  {
  return mDataId;
  }

public boolean isFirstLevel()
  {
  return mIsFirstLevel;
  }

public Uri getPhotoThumbnailUri()
  {
  return mPhotoThumbnailUri;
  }

/** This can be called outside main Looper thread. */
public synchronized void setPhotoBytes(final byte[] photoBytes)
  {
  mPhotoBytes=photoBytes;
  }

/** This can be called outside main Looper thread. */
public synchronized byte[] getPhotoBytes()
  {
  return mPhotoBytes;
  }

public boolean isSeparator()
  {
  return mIsDivider;
  }

public boolean isSelectable()
  {
  return mEntryType==ENTRY_TYPE_PERSON;
  }

public boolean isGalContact()
  {
  return mIsGalContact;
  }

@Override
public String toString()
  {
  return mDisplayName+" <"+mDestination+">, isValid="+mIsValid;
  }
}
