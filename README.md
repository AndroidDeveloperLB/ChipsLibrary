Note: this is deprecated. Google made better solution for it in its Material-design SDK:

- https://material.io/components/chips/android#using-chips
- https://medium.com/material-design-in-action/chips-material-components-for-android-46001664a40f
- https://developer.android.com/reference/com/google/android/material/chip/Chip

----

ChipsLibrary
=================

This is a fork of Google's chips library shown [**here**][1], and can be downloaded from [**here**][3]

![screenshot][3]

What's different from the original library :

1. It's easy to import and build on Eclipse. :)

2. Removed buggy dragging feature and the classes it uses. 

3. fixed setOnItemClickListener (for clicking on chips) so that it will use both the chips library's logic AND your code.

4. Removed T9 searching for phones, and instead you can search for either phone numbers or names, by exactly what you type.
Of course, you can always revert back to what Google has done, if you wish.     

5. Added features :
 1. getRecipients - query all recipients (should usually be done on a background thread)
 2. addRecipient , removeRecipient - adds/removes a recipient. also, ability to control if you wish to be notified about chips added/removed when you are the one who triggered it.
 4. setChipListener - sets a listener that'll notify you when the number of chips has changed.
 5. setFocusBehavior - ability to choose what to do when focus removed/received.
 6. New CTOR for the BaseRecipientAdapter, which allows to choose which recipients to use . Note that this is only for demonstrations
 7. setChosenRecipients - ability to set all chosen recipients.
 8. removeAllRecipients - removes all of the chosen recipients
 9. getChosenRecipients - returns all of the chosen recipients
 
 IMPORTANT: you can only safely call the recipients' operations after the view got its size feagured out. 
You can use the function "runJustBeforeBeingDrawn" as I've written on some StackOverflow posts (like [**here**][4]), in case you need to use those operations as soon as possible.  

 In order to make it easy for you to find the code for those added features, I've put them all at the end of "RecipientEditTextView.java" file.

6. Made the code a bit more readable. Not enough, but still... :)
 
Notes :

- on some devices (like Galaxy S2 and Galaxy S3) , pressing backspace after the chip will sometimes show its number instead of removing the chip (and its data) completely.
There might be some other weird behaviors on those devices, which are caused due to changes on the default behavior of MultiAutoCompleteTextView

- It seems the original library has a lot of "TODO"s and even some that say to fix issues. Not sure how important they are though

- I've tried to balance the changes I've made vs the code that is available, so that I won't create more bugs. 
If you've noticed a bug, please write about it, especially if you know how to fix it.

- Like the original library, this one's minimal API is 11 . 

- Be sure to check the sample. It's a bit messy, but it shows what can be done.  

- There is also a "demo" for those who don't wish to use it to show real contacts. It lacks some of the features of the original one (showing contacts images, showing alternative contacts, and maybe others), but you can customize it to your needs and also add the missing features.



  [1]: https://plus.google.com/+RichHyndman/posts/TSxaARVsRjF
  [2]: http://https://android.googlesource.com/platform/frameworks/ex/+/android-sdk-support_r11/chips/
  [3]: https://lh3.googleusercontent.com/-0tiDXRdjE9w/UEKSRdUaS6I/AAAAAAAAoqw/thtcKMWSWKs/w393-h683-no/png.png
  [4]: http://stackoverflow.com/a/10923514/878126
