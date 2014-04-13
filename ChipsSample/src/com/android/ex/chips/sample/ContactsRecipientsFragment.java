package com.android.ex.chips.sample;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Rfc822Tokenizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEditTextView.IChipListener;
import com.android.ex.chips.RecipientEntry;

public class ContactsRecipientsFragment extends Fragment
  {
  private RecipientEditTextView mPhoneRetv;
  final Random                  mRandom =new Random();

  @Override
  public View onCreateView(final LayoutInflater inflater,final ViewGroup container,final Bundle savedInstanceState)
    {
    final View rootView=inflater.inflate(R.layout.contact_recipient_sample_fragment,container,false);
    // email recipients textview sample
    final RecipientEditTextView emailRetv=(RecipientEditTextView)rootView.findViewById(R.id.email_retv);
    emailRetv.setTokenizer(new Rfc822Tokenizer());
    emailRetv.setAdapter(new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_EMAIL,getActivity())
      {});
    emailRetv.setChipListener(new IChipListener()
      {
        @Override
        public void onDataChanged()
          {
          android.util.Log.d("Applog","emails view data changed:");
          printAllItems(emailRetv);
          }
      });
    // contacts recipients textview sample
    mPhoneRetv=(RecipientEditTextView)rootView.findViewById(R.id.phone_retv);
    mPhoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    final BaseRecipientAdapter adapter=new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE,getActivity())
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
    // NOTE: the query should run on a background thread. this is just for demonstration
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
    final TextView tutorialTextView=(TextView)rootView.findViewById(R.id.device_recipients_tutorialTextView);
    if(!checkReadContactsPermission())
      {
      tutorialTextView.setText(R.string.device_recipients_tutorial_unavailable);
      mPhoneRetv.setEnabled(false);
      emailRetv.setEnabled(false);
      addRecipientButton.setEnabled(false);
      removeRecipientButton.setEnabled(false);
      }
    else
      {
      tutorialTextView.setText(R.string.device_recipients_tutorial);
      final List<RecipientEntry> allEntries=adapter.doQuery();
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
      }
    return rootView;
    }

  private boolean checkReadContactsPermission()
    {
    final String permission="android.permission.READ_CONTACTS";
    final int res=getActivity().checkCallingOrSelfPermission(permission);
    return res==PackageManager.PERMISSION_GRANTED;
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

  private void printAllItems(final RecipientEditTextView view)
    {
    final Collection<RecipientEntry> recipients=view.getChosenRecipients();
    for(final RecipientEntry entry : recipients)
      android.util.Log.d("Applog",entry.getContactId()+" "+entry.getDisplayName());
    }
  }
