package com.android.ex.chips.sample;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import com.android.ex.chips.RecipientAlternatesAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;

/** this class is provided just for the demo, which doesn't have read-contacts permission */
public class CustomRecipientEditTextView extends RecipientEditTextView
  {
  public CustomRecipientEditTextView(final Context context,final AttributeSet attrs)
    {
    super(context,attrs);
    }

  protected Cursor getCursorForConstruction(final Context context,final long contactId,final int queryType)
    {
    return new AbstractCursor()
      {
        @Override
        public boolean isNull(final int column)
          {
          return false;
          }

        @Override
        public String getString(final int column)
          {
          return null;
          }

        @Override
        public short getShort(final int column)
          {
          return 0;
          }

        @Override
        public long getLong(final int column)
          {
          return 0;
          }

        @Override
        public int getInt(final int column)
          {
          return 0;
          }

        @Override
        public float getFloat(final int column)
          {
          return 0;
          }

        @Override
        public double getDouble(final int column)
          {
          return 0;
          }

        @Override
        public int getCount()
          {
          return 0;
          }

        @Override
        public String[] getColumnNames()
          {
          return new String[] {BaseColumns._ID};
          }
      };
    }

  @Override
  protected ListAdapter createAlternatesAdapter(final DrawableRecipientChip chip)
    {
    return new RecipientAlternatesAdapter(getContext(),//
        getCursorForConstruction(getContext(),chip.getContactId(),getAdapter().getQueryType()),//
        chip.getDataId(),getAdapter().getQueryType(),this)
      {};
    }
  }
