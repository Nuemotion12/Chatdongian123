package com.chatt.demo;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.ChatUser;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.model.FileModel;
import com.chatt.demo.model.MapModel;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.chatt.demo.UserList.user;


/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */
public class Chat extends CustomActivity {
    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    ImageView btnEmoji;
    View contentChat;

    EmojIconActions emojIcon;
    private final String TAG ="Chat";
    boolean isLoadAllList;
    boolean isUpdate;
    private File filePathImageCamera;





    FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * The Conversation list.
     */
    private ArrayList<Conversation> convList;

    /**
     * The chat adapter.
     */
    private ChatAdapter adp;

    /**
     * The Editext to compose the message.
     */
    private EmojiconEditText txt;

    /**
     * The user name of buddy.
     */
    private ChatUser buddy;

    /**
     * The date of last message in conversation.
     */
    private Date lastMsgDate;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
     private ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);




        btnEmoji =(ImageView) findViewById(R.id.btnEmoji);
        btnEmoji.setOnClickListener(this);
        contentChat= (View) findViewById(R.id.contentChat);
        convList = new ArrayList<Conversation>();





        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);



        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EmojiconEditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(R.id.btnSend);

        buddy = (ChatUser) getIntent().getSerializableExtra(Const.EXTRA_DATA);

        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setTitle(buddy.getUsername());
        emojIcon = new EmojIconActions(this,contentChat,txt,btnEmoji);
        emojIcon.ShowEmojIcon();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Conversation conversation =(Conversation) adapterView.getAdapter().getItem(i);
                if(conversation.getMapModel() != null)
                {
                    String latitude = conversation.getMapModel().getLatitude();
                    String longitude = conversation.getMapModel().getLongitude();
                    String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude,longitude,latitude,longitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

                    startActivity(intent);

                }
                if(conversation.getFile() != null)
                {
                    Intent intent = new Intent(Chat.this,FullScreenImageActivity.class);

                    intent.putExtra("urlPhotoClick",conversation.getFile().getUrl_file());

                    startActivity(intent);

                }


            }
        });





    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        Log.e(TAG,"onResume");
        super.onResume();
        isLoadAllList = true;



            loadConversationList();








    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onPause()
     *
     */

    @Override
    protected void onPause() {
        super.onPause();


        convList.clear();


    }

    /* (non-Javadoc)
         * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
         */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btnSend) {
            sendMessage();


        }


    }

    /**
     *
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.sendPhoto:
                photoCameraIntent();
                break;
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sendLocation:
               locationPlacesIntent();
                break;
            case android.R.id.home :
                finish();



        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG,"onActivityResult#1");
        StorageReference storageRef = storage.getReferenceFromUrl(Utils.URL_STORAGE_REFERENCE).child(Utils.FOLDER_STORAGE_IMG);


        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG, "onActivityResult GALLERY_REQUEST:" + data.getData().toString());
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri);
                } else {
                    //URI IS NULL
                }
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (filePathImageCamera != null && filePathImageCamera.exists()) {
                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName() + "_camera");
                    sendFileFirebase(imageCameraRef, filePathImageCamera);
                } else {
                    //IS NULL
                }
            }

        }
        else if (requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place!=null){
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel = new MapModel(latLng.latitude+"",latLng.longitude+"");
                    final Conversation conversation = new Conversation(Calendar.getInstance().getTime(),user.getId(),buddy.getId(),mapModel);
                    Log.e(TAG,"sendLocation SENDING");
                    conversation.setStatus(Conversation.STATUS_SENDING);
                    convList.add(conversation);
                    final String key = FirebaseDatabase.getInstance()
                            .getReference("messages")
                            .push().getKey();
                    FirebaseDatabase.getInstance().getReference("messages").child(key)
                            .setValue(conversation)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful()) {
                                                               Log.e(TAG,"sendLocation SENDT");
                                                               convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_SENT);

                                                           } else {
                                                               Log.e(TAG,"sendLocation FAILED");
                                                               convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_FAILED);

                                                           }
                                                           FirebaseDatabase.getInstance()
                                                                   .getReference("messages")
                                                                   .child(key).setValue(convList.get(convList.indexOf(conversation)))
                                                                   .addOnCompleteListener(new
                                                                                                  OnCompleteListener<Void>() {
                                                                                                      @Override
                                                                                                      public void onComplete(@NonNull Task<Void> task) {



                                                                                                          adp.notifyDataSetChanged();
                                                                                                      }
                                                                                                  });

                                                       }
                                                   }
                            );
                    adp.notifyDataSetChanged();





                }


            }
        }
    }

    /**
     * Mo option chon hinh anh
     */

    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);




        startActivityForResult(Intent.createChooser(intent,"Get Photo from"), IMAGE_GALLERY_REQUEST);

    }

    /**
     * Mo camera
     */
    private void photoCameraIntent(){
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto+"camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(filePathImageCamera));

        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }

    /**
     * mo Google Place Service
     */
    private void locationPlacesIntent(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }




    /**
     * gui hinh tu may len storage
     * @param storageReference
     * @param file
     */
    private void sendFileFirebase(StorageReference storageReference, final Uri file){
        if (storageReference != null){

                final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
                StorageReference imageGalleryRef = storageReference.child(name + "_gallery");
                UploadTask uploadTask = imageGalleryRef.putFile(file);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG, "onSuccess sendFileFirebase");
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                        final Conversation conversation = new Conversation(
                                Calendar.getInstance().getTime(),
                                user.getId(),
                                buddy.getId(),
                                fileModel);
                        conversation.setStatus(Conversation.STATUS_SENDING);
                        convList.add(conversation);
                        final String key = FirebaseDatabase.getInstance()
                                .getReference("messages")
                                .push().getKey();
                        FirebaseDatabase.getInstance().getReference("messages").child(key)
                                .setValue(conversation)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {
                                                               if (task.isSuccessful()) {
                                                                   convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_SENT);

                                                               } else {
                                                                   convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_FAILED);

                                                               }
                                                               FirebaseDatabase.getInstance()
                                                                       .getReference("messages")
                                                                       .child(key).setValue(convList.get(convList.indexOf(conversation)))
                                                                       .addOnCompleteListener(new
                                                                                                      OnCompleteListener<Void>() {
                                                                                                          @Override
                                                                                                          public void onComplete(@NonNull Task<Void> task) {
                                                                                                              Log.e(TAG, "send Message #4:");


                                                                                                              adp.notifyDataSetChanged();
                                                                                                          }
                                                                                                      });

                                                           }
                                                       }
                                );





                    }


                });
            adp.notifyDataSetChanged();




        }else{
            //IS NULL
        }

    }

    /**
     * gui hinh chup len storage
     * @param storageReference
     * @param file
     */
    private void sendFileFirebase(StorageReference storageReference, final File file){
        if (storageReference != null){
            UploadTask uploadTask = storageReference.putFile(Uri.fromFile(file));
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG,"onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileModel = new FileModel("img",downloadUrl.toString(),file.getName(),file.length()+"");


                    final Conversation conversation = new Conversation(
                            Calendar.getInstance().getTime(),
                            user.getId(),
                            buddy.getId(),
                            fileModel);
                    conversation.setStatus(Conversation.STATUS_SENDING);
                    convList.add(conversation);
                    final String key = FirebaseDatabase.getInstance()
                            .getReference("messages")
                            .push().getKey();
                    FirebaseDatabase.getInstance().getReference("messages").child(key)
                            .setValue(conversation)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful()) {
                                                               convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_SENT);

                                                           } else {
                                                               convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_FAILED);

                                                           }
                                                           FirebaseDatabase.getInstance()
                                                                   .getReference("messages")
                                                                   .child(key).setValue(convList.get(convList.indexOf(conversation)))
                                                                   .addOnCompleteListener(new
                                                                                                  OnCompleteListener<Void>() {
                                                                                                      @Override
                                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                                          Log.e(TAG, "send Message #4:");


                                                                                                          adp.notifyDataSetChanged();
                                                                                                      }
                                                                                                  });

                                                       }
                                                   }
                            );

                }
            });
            adp.notifyDataSetChanged();

        }else{
            //IS NULL
        }

    }


    /**
     * Call this method to Send message to opponent. It does nothing if the text
     * is empty otherwise it creates a Parse object for Chat message and send it
     * to Parse server.
     */
    private void sendMessage() {
        Log.e(TAG,"send Message #1");
        if (txt.length() == 0)
            return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            Log.e(TAG,"send Message #2");
            final Conversation conversation = new Conversation(s,
                    Calendar.getInstance().getTime(),
                    user.getUid(),
                    buddy.getId()
                    );
            conversation.setStatus(Conversation.STATUS_SENDING);
            convList.add(conversation);
            Log.e(TAG,"send Message #3 conservation:"+ conversation.toString());
            final String key = FirebaseDatabase.getInstance()
                    .getReference("messages")
                    .push().getKey();
            FirebaseDatabase.getInstance().getReference("messages").child(key)
                    .setValue(conversation)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if (task.isSuccessful()) {
                                                       convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_SENT);
                                                       Log.e(TAG,"send Message #3 conservation:"+ conversation.toString());
                                                   } else {
                                                       convList.get(convList.indexOf(conversation)).setStatus(Conversation.STATUS_FAILED);
                                                       Log.e(TAG,"send Message #3 conservation:"+ conversation.toString());
                                                   }
                                                   FirebaseDatabase.getInstance()
                                                           .getReference("messages")
                                                           .child(key).setValue(convList.get(convList.indexOf(conversation)))
                                                           .addOnCompleteListener(new
                                                                                          OnCompleteListener<Void>() {
                                                                                              @Override
                                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                                  Log.e(TAG,"send Message #4:");


                                                                                                  adp.notifyDataSetChanged();
                                                                                              }
                                                                                          });

                                               }
                                           }
                    );
        }
        adp.notifyDataSetChanged();
        txt.setText(null);

    }

    /**
     * Load the conversation list from Parse server and save the date of last
     * message that will be used to load only recent new messages
     */
    private void loadConversationList() {
        DatabaseReference root =  FirebaseDatabase.getInstance().getReference("messages");
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("TAG","onChildAdded data:" + dataSnapshot.toString());
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Conversation conversation = dataSnapshot.getValue(Conversation.class);
                if(isLoadAllList == false) {
                    if (conversation.getReceiver().contentEquals(user.getUid()) && conversation.getSender().contentEquals(buddy.getId())  ) {
                        if (lastMsgDate == null
                                || lastMsgDate.before(conversation.getDate())) {

                            Log.e("TAG", "onChildAdded#2 LoadMessage contain =" + convList.contains(conversation));
                            convList.add(conversation);

                            lastMsgDate = conversation.getDate();

                            adp.notifyDataSetChanged();
                        }


                    }


                }







            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






        root.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange #1");


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Log.e(TAG, "onDataChange #2");
                    if (isLoadAllList) {


                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.e(TAG, "onDataChange #3 ds:" + ds.toString());
                            Conversation conversation = ds.getValue(Conversation.class);

                            if ((conversation.getReceiver().contentEquals(user.getUid()) && conversation.getSender().contentEquals(buddy.getId())) ||
                                    (conversation.getSender().contentEquals(user.getUid()) && conversation.getReceiver().contentEquals(buddy.getId()))

                                    ) {
                                Log.e(TAG, "onDataChange #4 ");
                                convList.add(conversation);
                                /*

                                if (lastMsgDate == null
                                        || lastMsgDate.before(conversation.getDate()))
                                    lastMsgDate = conversation.getDate();
                                    */

                                Log.e(TAG, "onDataChange #5 ");
                                adp.notifyDataSetChanged();


                            }
                        }


                        // convList.clear();
                        //convList.addAll( Collections.synchronizedList(data));


                    }
                }

                isLoadAllList = false;


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });











    }

    /**
     * The Class ChatAdapter is the adapter class for Chat ListView. This
     * adapter shows the Sent or Receieved Chat message in each list item.
     */
    private class ChatAdapter extends BaseAdapter {


        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return convList.size();
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Conversation getItem(int arg0) {
            return convList.get(arg0);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @SuppressLint("InflateParams")


        @Override
        public View getView(int pos, View v, ViewGroup arg2) {
            Log.e(TAG,"getView #1 pos:" + pos);
            Conversation c = getItem(pos);
            if (c.isSent()) {
                if (c.getFile() != null || c.getMapModel() != null) {
                    v = getLayoutInflater().inflate(R.layout.chat_item_sent_img, null);
                } else {
                    v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
                }
            }
            else {
                if (c.getFile() != null || c.getMapModel() != null) {
                v = getLayoutInflater().inflate(R.layout.chat_item_rcv_img, null);
            } else {
                v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);
            }


            }

            TextView lbl = (TextView) v.findViewById(R.id.lbl1);
            lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c
                            .getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS, 0));
            if(c.getFile() != null)
            {
                ImageView img_chat = (ImageView) v.findViewById(R.id.img_chat);

                Glide.with(img_chat.getContext()).load(c.getFile().getUrl_file())
                        .override(100, 100)
                        .fitCenter()
                        .into(img_chat);

            }
            else {
                if(c.getMapModel() != null)
                {
                    ImageView img_chat = (ImageView) v.findViewById(R.id.img_chat);
                    Glide.with(img_chat.getContext()).load(c.getMapModel().local())
                            .override(100, 100)
                            .fitCenter()
                            .into(img_chat);

                }
                else {

                    lbl = (TextView) v.findViewById(R.id.lbl2);
                    lbl.setText(c.getMsg());
                }
            }

            lbl = (TextView) v.findViewById(R.id.lbl3);
            if (c.isSent()) {
                if (c.getStatus() == Conversation.STATUS_SENT) {
                    Log.e(TAG,"getView #2 SENT");
                    lbl.setText(R.string.delivered_text);

                }
                else {
                    if (c.getStatus() == Conversation.STATUS_SENDING) {
                        lbl.setText(R.string.sending_text);
                        Log.e(TAG, "getView #2 SENDING");
                    }
                    else {
                        lbl.setText(R.string.failed_text);
                        Log.e(TAG, "getView #2 FAILED");
                    }
                }
            } else
                lbl.setText("");

            return v;
        }

    }





}
