package com.android.ex.chips.sample;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.app.Fragment;
import android.os.Bundle;
import android.provider.ContactsContract.DisplayNameSources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;

public class CustomRecipientFragment extends Fragment
  {
  final Random mRandom =new Random();

  @Override
  public View onCreateView(final LayoutInflater inflater,final ViewGroup container,final Bundle savedInstanceState)
    {
    final View rootView=inflater.inflate(R.layout.custom_recipient_sample_fragment,container,false);
    // custom recipients textview sample
    final RecipientEditTextView customRetv=(RecipientEditTextView)rootView.findViewById(R.id.custom_retv);
    customRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    customRetv.setThreshold(2);
    final List<RecipientEntry> allEntries=generateFakeRecipientsEntries();
    customRetv.setAdapter(new BaseRecipientAdapter(getActivity(),4,allEntries)
      {});
    final View removeRecipientButton=rootView.findViewById(R.id.removeRecipientButton);
    removeRecipientButton.setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(final View v)
          {
          final View view=getActivity().getCurrentFocus();
          if(view instanceof RecipientEditTextView)
            removeRandomRecipient((RecipientEditTextView)view);
          }
      });
    final View addRecipientButton=rootView.findViewById(R.id.addRecipientButton);
    android.util.Log.d("Applog","entries count:"+allEntries.size());
    addRecipientButton.setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(final View v)
          {
          final View view=getActivity().getCurrentFocus();
          if(view instanceof RecipientEditTextView)
            addRandomRecipient((RecipientEditTextView)view,allEntries);
          }
      });
    final TextView availableRecipientsTextView=(TextView)rootView.findViewById(R.id.availableRecipientsTextView);
    for(final RecipientEntry recipientEntry : allEntries)
      availableRecipientsTextView.append("\n"+recipientEntry.getDisplayName()+" - "+recipientEntry.getDestination());
    return rootView;
    }

  private List<RecipientEntry> generateFakeRecipientsEntries()
    {
    final List<RecipientEntry> allEntries=new ArrayList<RecipientEntry>();
    for(int i=0;i<100;++i)
      {
      final RecipientEntry entry=RecipientEntry.constructTopLevelEntry("abc"+i,DisplayNameSources.NICKNAME,"address"+i,0,null,i,i,(String)null,true,false);
      allEntries.add(entry);
      }
    return allEntries;
    }

  private void removeRandomRecipient(final RecipientEditTextView view)
    {
    final List<RecipientEntry> recipients=new ArrayList<RecipientEntry>(view.getChosenRecipients());
    final int count=recipients.size();
    if(count==0)
      return;
    final int idx=mRandom.nextInt(count);
    view.removeRecipient(recipients.get(idx),true);
    // Log.d("Applog","data after removing a random recipient:");
    // printAllItems(view);
    }

  private void addRandomRecipient(final RecipientEditTextView view,final List<RecipientEntry> allEntries)
    {
    final int idx=mRandom.nextInt(allEntries.size());
    view.addRecipient(allEntries.get(idx),true);
    // Log.d("Applog","data after adding a random recipient:");
    // printAllItems(view);
    }
  }
