package com.bibhas.handsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bibhas.handsapp.Activities.ViewMediaActivity;
import com.bibhas.handsapp.Models.Messages;
import com.bibhas.handsapp.R;
import com.bibhas.handsapp.Util.SharedData;
import com.bibhas.handsapp.Util.SharedMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "MessageAdapter==";

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private FirebaseStorage storage;

    private final int type_text = 1111;
    private final int type_image = 2222;
    private final int type_audio = 3333;
    private final int type_video = 4444;
    private final int type_file_one = 5555;

    private String dir;
    private String received_image_dir;
    private String received_thumb_image_dir;
    private String received_audio_dir;
    private String received_video_dir;
    private String received_thumb_video_dir;
    private String received_file_dir;

    private String sent_image_dir;
    private String sent_thumb_image_dir;
    private String sent_audio_dir;
    private String sent_video_dir;
    private String sent_thumb_video_dir;
    private String sent_file_dir;


    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        dir = SharedData.app_dir;

        sent_image_dir = dir + SharedData.sent_image_dir;
        sent_thumb_image_dir = dir + SharedData.sent_thumb_image_dir;
        sent_audio_dir = dir + SharedData.sent_audio_dir;
        sent_video_dir = dir + SharedData.sent_video_dir;
        sent_thumb_video_dir = dir + SharedData.sent_thumb_video_dir;
        sent_file_dir = dir + SharedData.sent_file_one_dir;

        received_image_dir = dir + SharedData.received_image_dir;
        received_thumb_image_dir = dir + SharedData.received_thumb_image_dir;
        received_audio_dir = dir + SharedData.received_audio_dir;
        received_video_dir = dir + SharedData.received_video_dir;
        received_thumb_video_dir = dir + SharedData.received_thumb_video_dir;
        received_file_dir = dir + SharedData.received_file_one_dir;

        switch (viewType) {
            case 1111:
                return new TextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_text, parent, false));
            case 2222:
                return new PhotoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_photo, parent, false));
            case 3333:
                return new AudioViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_audio, parent, false));
            case 4444:
                return new PhotoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_photo, parent, false));
            //return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_video, parent, false));
            case 5555:
                return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_file_one, parent, false));
        }
        return null;
    }


    public class TextViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout container;
        private TextView receiver_name;
        private TextView received_time;
        private TextView received_messageText;
        private TextView sent_messageText;

        public TextViewHolder(View view) {
            super(view);
            try {
                receiver_name = view.findViewById(R.id.receiver_name);
                container = view.findViewById(R.id.container);
                received_time = view.findViewById(R.id.received_time);
                received_messageText = view.findViewById(R.id.message_text_layout);
                sent_messageText = view.findViewById(R.id.sent_messageText);
            } catch (Exception e) {
                Log.d(TAG, "TextViewHolder: " + e.getMessage());
            }
        }
    }


    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView messageImage;
        private ImageView icon_paly_video;
        private ImageView download_icon;
        private RelativeLayout container;
        private TextView downloading_percent;
        private ProgressBar progress_bar;
        private TextView receiver_name;
        private TextView received_time;

        private ImageView messageImage2;
        private ImageView icon_paly_video2;
        private ImageView download_icon2;
        private RelativeLayout container2;
        private TextView downloading_percent2;
        private ProgressBar progress_bar2;

        private PhotoViewHolder(View view) {
            super(view);
            try {
                messageImage = view.findViewById(R.id.message_image_layout);
                icon_paly_video = view.findViewById(R.id.video_pay_icon);
                download_icon = view.findViewById(R.id.download_icon);
                container = view.findViewById(R.id.container);
                downloading_percent = view.findViewById(R.id.percent_image);
                progress_bar = view.findViewById(R.id.progress_image);
                receiver_name = view.findViewById(R.id.receiver_name);
                received_time = view.findViewById(R.id.received_time);

                messageImage2 = view.findViewById(R.id.message_image_layout2);
                icon_paly_video2 = view.findViewById(R.id.video_pay_icon2);
                download_icon2 = view.findViewById(R.id.download_icon2);
                container2 = view.findViewById(R.id.container2);
                downloading_percent2 = view.findViewById(R.id.percent_image2);
                progress_bar2 = view.findViewById(R.id.progress_image2);

            } catch (Exception e) {
                Log.d(TAG, "PhotoViewHolder: " + e.getMessage());
            }
        }
    }


    public class AudioViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout container;
        private ImageView play_icon;
        private ImageView pause_icon;
        private ImageView stop_icon;
        private ImageView download_icon;
        private ProgressBar progress_bar;
        private TextView downloading_percent;
        private TextView receiver_name;
        private TextView received_time;

        private RelativeLayout container2;
        private ImageView play_icon2;
        private ImageView pause_icon2;
        private ImageView stop_icon2;
        private ImageView download_icon2;
        private ProgressBar progress_bar2;
        private TextView downloading_percent2;


        public AudioViewHolder(View view) {
            super(view);
            try {
                container = view.findViewById(R.id.container);
                play_icon = view.findViewById(R.id.play_icon);
                pause_icon = view.findViewById(R.id.pause_icon);
                stop_icon = view.findViewById(R.id.stop);
                download_icon = view.findViewById(R.id.download_icon);
                progress_bar = view.findViewById(R.id.progress_audio);
                downloading_percent = view.findViewById(R.id.percent_image);
                receiver_name = view.findViewById(R.id.receiver_name);
                received_time = view.findViewById(R.id.received_time);

                container2 = view.findViewById(R.id.container2);
                play_icon2 = view.findViewById(R.id.play_icon2);
                pause_icon2 = view.findViewById(R.id.pause_icon2);
                stop_icon2 = view.findViewById(R.id.stop2);
                download_icon2 = view.findViewById(R.id.download_icon2);
                progress_bar2 = view.findViewById(R.id.progress_audio2);
                downloading_percent2 = view.findViewById(R.id.percent_image2);
            } catch (Exception e) {
                Log.d(TAG, "AudioViewHolder: " + e.getMessage());
            }
        }
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout container;
        private VideoView messageVideo;
        private ImageView download_icon;
        private TextView downloading_percent;
        private ProgressBar progress_bar;

        private RelativeLayout container2;
        private VideoView messageVideo2;
        private ImageView download_icon2;
        private TextView downloading_percent2;
        private ProgressBar progress_bar2;

        public VideoViewHolder(View view) {
            super(view);
            try {
                container = view.findViewById(R.id.container);
                messageVideo = view.findViewById(R.id.message_video_layout2);
                download_icon = view.findViewById(R.id.download_video);
                downloading_percent = view.findViewById(R.id.percent_image);
                progress_bar = view.findViewById(R.id.progress_image);

                container2 = view.findViewById(R.id.container2);
                messageVideo2 = view.findViewById(R.id.message_video_layout2);
                download_icon2 = view.findViewById(R.id.download_video2);
                downloading_percent2 = view.findViewById(R.id.percent_image2);
                progress_bar2 = view.findViewById(R.id.progress_image2);

            } catch (Exception e) {
                Log.d(TAG, "VideoViewHolder: " + e.getMessage());
            }

        }
    }


    public class FileViewHolder extends RecyclerView.ViewHolder {

        private TextView doc_file_name;
        private ImageView download_icon;
        private RelativeLayout container;
        private TextView downloading_percent;
        private ProgressBar progress_bar;
        private TextView receiver_name;
        private TextView received_time;

        private TextView doc_file_name2;
        private ImageView download_icon2;
        private RelativeLayout container2;
        private TextView downloading_percent2;
        private ProgressBar progress_bar2;

        private FileViewHolder(View view) {
            super(view);
            try {
                doc_file_name = view.findViewById(R.id.doc_file_name);
                download_icon = view.findViewById(R.id.download_icon);
                container = view.findViewById(R.id.container);
                downloading_percent = view.findViewById(R.id.percent_image);
                progress_bar = view.findViewById(R.id.progress_image);
                receiver_name = view.findViewById(R.id.receiver_name);
                received_time = view.findViewById(R.id.received_time);

                doc_file_name2 = view.findViewById(R.id.doc_file_name2);
                download_icon2 = view.findViewById(R.id.download_icon2);
                container2 = view.findViewById(R.id.container2);
                downloading_percent2 = view.findViewById(R.id.percent_image2);
                progress_bar2 = view.findViewById(R.id.progress_image2);

            } catch (Exception e) {
                Log.d(TAG, "PhotoViewHolder: " + e.getMessage());
            }
        }
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int i) {
        String message_sender_id = SharedData.current_user_id;
        Messages message = mMessageList.get(i);

        //mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        switch (holder.getItemViewType()) {
            case type_text:
                TextViewHolder textViewHolder = (TextViewHolder) holder;
                setTextMessage(textViewHolder, message, message_sender_id);
                break;

            case type_image:
                PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
                setImageMessage(photoViewHolder, message, message_sender_id);
                break;

            case type_audio:
                AudioViewHolder audioViewHolder = (AudioViewHolder) holder;
                setAudioMessage(audioViewHolder, message, message_sender_id);
                break;

            case type_video:
                PhotoViewHolder photoViewHolder2 = (PhotoViewHolder) holder;
                setVideoThumbImageMessage(photoViewHolder2, message, message_sender_id);

                //VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                //setVideoMessage(videoViewHolder, message, message_sender_id);
                break;

            case type_file_one:
                FileViewHolder fileViewHolder = (FileViewHolder) holder;
                setFileMessage(fileViewHolder, message, message_sender_id);
                break;

            default:
                break;

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position).getType().equals("text")) {
            return type_text;

        } else if (mMessageList.get(position).getType().equals("image")) {
            return type_image;

        } else if (mMessageList.get(position).getType().equals("audio")) {
            return type_audio;

        } else if (mMessageList.get(position).getType().equals("video")) {
            return type_video;

        } else if (mMessageList.get(position).getType().equals("file_one")) {
            return type_file_one;

        } else {
            // Unhandled message type.
            return -1111;
        }
    }


    private void setTextMessage(final TextViewHolder viewHolder, Messages message, String sender_id) {
        if (sender_id.equals(message.getFrom())) {
            //=========== sent message =================
            viewHolder.sent_messageText.setVisibility(View.VISIBLE);
            viewHolder.container.setVisibility(View.GONE);

            viewHolder.sent_messageText.setText(message.getMessage());

        } else {
            //=========== received message =================
            viewHolder.sent_messageText.setVisibility(View.GONE);
            viewHolder.container.setVisibility(View.VISIBLE);

            viewHolder.received_messageText.setText(message.getMessage());
            setNameAndTime(viewHolder.receiver_name, viewHolder.received_time, message);
        }
    }

    /**
     * only download thumb image to show in chat
     */
    private void setImageMessage(final PhotoViewHolder viewHolder, final Messages message, String sender_id) {
        ImageView messageImage;
        ImageView download_icon;
        ProgressBar progressBar;
        TextView percentage;
        String file_dir = null;
        String thumb_file_dir = null;

        if (sender_id.equals(message.getFrom())) {
            //=========== for sent message =================
            //SharedMethods.changelayoutPosition(viewHolder.container);
            viewHolder.container.setVisibility(View.GONE);

            viewHolder.container2.setVisibility(View.VISIBLE);
            viewHolder.messageImage2.setVisibility(View.VISIBLE);
            //viewHolder.download_icon2.setVisibility(View.VISIBLE);

            messageImage = viewHolder.messageImage2;
            download_icon = viewHolder.download_icon2;
            progressBar = viewHolder.progress_bar2;
            percentage = viewHolder.downloading_percent2;

        } else {
            //SharedMethods.changelayoutPositionRight(viewHolder.container);
            viewHolder.container.setVisibility(View.VISIBLE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            //viewHolder.download_icon.setVisibility(View.VISIBLE);

            viewHolder.container2.setVisibility(View.GONE);

            messageImage = viewHolder.messageImage;
            download_icon = viewHolder.download_icon;
            progressBar = viewHolder.progress_bar;
            percentage = viewHolder.downloading_percent;

            setNameAndTime(viewHolder.receiver_name, viewHolder.received_time, message);
        }

        final String file_name = SharedMethods.getFileName(message.getThumb_image(), "thumb_images", SharedData.image_format)
                + SharedData.image_format;
        boolean is_sent_image_exist = SharedMethods.checkFileExist(sent_thumb_image_dir, file_name);
        boolean is_received_image_exist = SharedMethods.checkFileExist(received_thumb_image_dir, file_name);

        Log.d(TAG, "message : " + message.getMessage());
        Log.d(TAG, "file_name: " + file_name);
        Log.d(TAG, "is_sent_image_exist: " + is_sent_image_exist);
        Log.d(TAG, "is_received_image_exist: " + is_received_image_exist);

        // if image allready downloaded in storage then show it from storage
        if (is_sent_image_exist) {
            thumb_file_dir = sent_thumb_image_dir + "/" + file_name;
            file_dir = sent_image_dir + "/" + file_name.replace(SharedData.thumb_image_file_type, SharedData.image_file_type);
            showImage(thumb_file_dir, messageImage);

            // if image allready downloaded in storage then show it from storage
        } else if (is_received_image_exist) {
            thumb_file_dir = received_thumb_image_dir + "/" + file_name;
            file_dir = received_image_dir + "/" + file_name.replace(SharedData.thumb_image_file_type, SharedData.image_file_type);

            showImage(thumb_file_dir, messageImage);

        } else {
            final ImageView temp_messageImage = messageImage;
            final ImageView temp_download_icon = download_icon;
            final ProgressBar temp_ProgressBar = progressBar;
            final TextView temp_percentage = percentage;

            download_icon.setVisibility(View.VISIBLE);

            Picasso picasso = Picasso.with(temp_messageImage.getContext());
            picasso.setIndicatorsEnabled(false);
            picasso.with(temp_messageImage.getContext())
                    .load(message.getThumb_image())
                    .placeholder(R.drawable.default_avatar)
                    .into(messageImage);

            thumb_file_dir = received_thumb_image_dir + "/" + file_name;
            download_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    temp_download_icon.setVisibility(View.GONE);

                    // From an HTTPS URL
                    StorageReference fileRef = storage.getReferenceFromUrl(message.getThumb_image());
                    //download thumb_image
                    SharedMethods.saveThumbFileInStorage(fileRef,
                            received_thumb_image_dir,
                            file_name);

                    //download full_image

                    StorageReference fileRef2 = storage.getReferenceFromUrl(message.getMessage());
                    SharedMethods.saveFileInStorage(
                            fileRef2,
                            received_image_dir,
                            file_name.replace(SharedData.thumb_image_file_type, SharedData.image_file_type),
                            temp_messageImage,
                            temp_ProgressBar,
                            temp_percentage);
                }
            });

            file_dir = received_image_dir + "/" + file_name.replace(SharedData.thumb_image_file_type, SharedData.image_file_type);
        }

        final String temp_file_dir = file_dir;
        messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ViewMediaActivity.class);
                intent.putExtra("file_type", SharedData.image_file_type);
                intent.putExtra("file_dir", temp_file_dir);
                v.getContext().startActivity(intent);

                //Log.d(TAG, "onClick: " + temp_file_dir);
            }
        });
    }

    private void setAudioMessage(final AudioViewHolder viewHolder, final Messages message, final String sender_id) {
        ImageView play_icon;
        ImageView pause_icon;
        ImageView stop_icon;
        ImageView download_icon;
        ProgressBar progress_bar;

        final MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.reset();
        //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //=========== sent message =================
        if (sender_id.equals(message.getFrom())) {
            viewHolder.container2.setVisibility(View.VISIBLE);
            viewHolder.container.setVisibility(View.GONE);
            viewHolder.receiver_name.setVisibility(View.GONE);
            viewHolder.received_time.setVisibility(View.GONE);

            play_icon = viewHolder.play_icon2;
            pause_icon = viewHolder.pause_icon2;
            stop_icon = viewHolder.stop_icon2;
            download_icon = viewHolder.download_icon2;
            progress_bar = viewHolder.progress_bar2;

        } else {
            viewHolder.container.setVisibility(View.VISIBLE);
            viewHolder.container2.setVisibility(View.GONE);

            play_icon = viewHolder.play_icon;
            pause_icon = viewHolder.pause_icon;
            stop_icon = viewHolder.stop_icon;
            download_icon = viewHolder.download_icon;
            progress_bar = viewHolder.progress_bar;

            setNameAndTime(viewHolder.receiver_name, viewHolder.received_time, message);
        }

        final String file_name = SharedData.audio_file_type + SharedMethods.getFileName(message.getMessage(), SharedData.audio_file_type, SharedData.audio_format) + SharedData.audio_format;

        boolean is_sent_file_exist = SharedMethods.checkFileExist(sent_audio_dir, file_name);
        boolean is_received_file_exist = SharedMethods.checkFileExist(received_audio_dir, file_name);

        //Log.d(TAG, "is_sent_file_exist: " + is_sent_file_exist + "---------is_received_file_exist :" + is_received_file_exist);

        if (is_sent_file_exist) {               // if image allready downloaded in storage then show it from storage
            try {
                play_icon.setVisibility(View.VISIBLE);
                download_icon.setVisibility(View.GONE);
                progress_bar.setVisibility(View.GONE);

                player.setDataSource(sent_audio_dir + "/" + file_name);
                player.prepare();
                //player.start();

            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }

        } else if (is_received_file_exist) {            // if image allready downloaded in storage then show it from storage
            try {
                play_icon.setVisibility(View.VISIBLE);
                download_icon.setVisibility(View.GONE);
                progress_bar.setVisibility(View.GONE);

                player.setDataSource(received_audio_dir + "/" + file_name);
                player.prepare();
                //player.start();

            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }

        } else {
            //============  play audio streem from firebase============
//            viewHolder.streeming.setVisibility(View.VISIBLE);
//            viewHolder.streeming.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                        player.setDataSource(message.getMessage());
//                        player.prepare();
//
//                        viewHolder.streeming.setVisibility(View.GONE);
//                        pause_icon.setVisibility(View.VISIBLE);
//
//                    } catch (Exception e) {
//                        Log.d(TAG, "Exception: " + e.getMessage());
//                    }
//                }
//            });

            //============ download audio from firebase============

            final ImageView temp_play_icon = play_icon;
            final ImageView temp_pause_icon = pause_icon;
            final ImageView temp_download_icon = download_icon;
            final ProgressBar temp_progress_bar = progress_bar;

            temp_download_icon.setVisibility(View.VISIBLE);
            temp_progress_bar.setVisibility(View.GONE);

            temp_download_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        StorageReference fileRef = storage.getReferenceFromUrl(message.getMessage());
                        //SharedMethods.saveFileInStorage(fileRef, received_audio_dir, file_name);
                        SharedMethods.saveFileInStorage(fileRef, received_audio_dir, file_name, player, viewHolder.progress_bar, viewHolder.downloading_percent, temp_play_icon);
                        temp_download_icon.setVisibility(View.GONE);

                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                        Toast.makeText(temp_play_icon.getContext(), " download failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        final ImageView temp_play_icon = play_icon;
        final ImageView temp_pause_icon = pause_icon;
        final ImageView temp_download_icon = download_icon;
        final ImageView temp_stop_icon = stop_icon;
        final ProgressBar temp_progress_bar = progress_bar;

        play_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    temp_play_icon.setVisibility(View.GONE);
                    temp_pause_icon.setVisibility(View.VISIBLE);
                    temp_stop_icon.setVisibility(View.VISIBLE);
                    temp_progress_bar.setVisibility(View.VISIBLE);
                    player.start();

                    new CountDownTimer(player.getDuration(), 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            player.release();
                            temp_play_icon.setVisibility(View.GONE);
                            temp_pause_icon.setVisibility(View.VISIBLE);
                            temp_progress_bar.setVisibility(View.GONE);
                        }
                    }.start();

                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
        });

        pause_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    temp_play_icon.setVisibility(View.VISIBLE);
                    temp_pause_icon.setVisibility(View.GONE);
                    temp_stop_icon.setVisibility(View.VISIBLE);
                    temp_progress_bar.setVisibility(View.GONE);

                    player.pause();

                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
        });

        stop_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    temp_play_icon.setVisibility(View.VISIBLE);
                    temp_pause_icon.setVisibility(View.GONE);
                    temp_progress_bar.setVisibility(View.GONE);

                    player.seekTo(0);
                    player.pause();

                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
            }
        });
    }

    private void setVideoMessage(final VideoViewHolder viewHolder, final Messages message, String sender_id) {
        VideoView videoView = viewHolder.messageVideo;
        String file_dir = null;

        //Creating and setting MediaController
        if (sender_id.equals(message.getFrom())) {
            //=========== for sent message =================
            SharedMethods.changelayoutPosition(viewHolder.container);

        } else {

        }

        final String file_name = SharedData.video_file_type + SharedMethods.getFileName(message.getMessage(), "thumb_image", SharedData.video_format) + SharedData.video_format;
        //Log.d(TAG, "file: " + file_name);

        boolean is_sent_file_exist = SharedMethods.checkFileExist(sent_video_dir, file_name);
        boolean is_received_file_exist = SharedMethods.checkFileExist(received_video_dir, file_name);

        //Log.d(TAG, "is_sent_video_exist: " + is_sent_file_exist + "---------is_received_video_exist :" + is_received_file_exist);

        if (is_sent_file_exist) {               // if image allready downloaded in storage then show it from storage
            try {
                viewHolder.download_icon.setVisibility(View.GONE);
                file_dir = sent_video_dir + "/" + file_name;

            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        } else if (is_received_file_exist) {            // if image allready downloaded in storage then show it from storage
            try {
                viewHolder.download_icon.setVisibility(View.GONE);
                file_dir = received_video_dir + "/" + file_name;

            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        } else {
            file_dir = received_video_dir + "/" + file_name;
            viewHolder.download_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.download_icon.setVisibility(View.GONE);
                    try {
                        StorageReference fileRef = storage.getReferenceFromUrl(message.getMessage());
                        SharedMethods.saveFileInStorage(fileRef, received_video_dir, file_name, null, viewHolder.progress_bar, viewHolder.downloading_percent);

                    } catch (Exception e) {
                        Log.d(TAG, "Exception: " + e.getMessage());
                        Toast.makeText(viewHolder.messageVideo.getContext(), " download failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        final String temp_file_dir = file_dir;
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ViewMediaActivity.class);
                intent.putExtra("file_type", SharedData.video_file_type);
                intent.putExtra("file_dir", temp_file_dir);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void setVideoThumbImageMessage(final PhotoViewHolder viewHolder, final Messages message, String sender_id) {
        ImageView messageImage;
        ImageView icon_video_play;
        ImageView download_icon;
        ProgressBar progressBar;
        TextView percentage;
        String file_dir = null;
        String thumb_file_dir = null;

        if (sender_id.equals(message.getFrom())) {
            //=========== for sent message =================
            //SharedMethods.changelayoutPosition(viewHolder.container);
            viewHolder.container.setVisibility(View.GONE);

            viewHolder.container2.setVisibility(View.VISIBLE);
            viewHolder.messageImage2.setVisibility(View.VISIBLE);
            //viewHolder.download_icon2.setVisibility(View.VISIBLE);

            messageImage = viewHolder.messageImage2;
            icon_video_play = viewHolder.icon_paly_video2;
            download_icon = viewHolder.download_icon2;
            progressBar = viewHolder.progress_bar2;
            percentage = viewHolder.downloading_percent2;


        } else {
            //SharedMethods.changelayoutPositionRight(viewHolder.container);
            viewHolder.container.setVisibility(View.VISIBLE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            //viewHolder.download_icon.setVisibility(View.VISIBLE);

            viewHolder.container2.setVisibility(View.GONE);

            messageImage = viewHolder.messageImage;
            icon_video_play = viewHolder.icon_paly_video;
            download_icon = viewHolder.download_icon;
            progressBar = viewHolder.progress_bar;
            percentage = viewHolder.downloading_percent;

            setNameAndTime(viewHolder.receiver_name, viewHolder.received_time, message);
        }
        icon_video_play.setVisibility(View.VISIBLE);


        final String file_name = SharedMethods.getFileName(message.getThumb_image(), "thumb_images", SharedData.image_format)
                + SharedData.image_format;
        boolean is_sent_image_exist = SharedMethods.checkFileExist(sent_thumb_video_dir, file_name);
        boolean is_received_image_exist = SharedMethods.checkFileExist(received_thumb_video_dir, file_name);

        Log.d(TAG, "message : " + message.getMessage());
        Log.d(TAG, "getThumb_image : " + message.getThumb_image());
        Log.d(TAG, "file_name: " + file_name);
        Log.d(TAG, "is_sent_image_exist: " + is_sent_image_exist);
        Log.d(TAG, "is_received_image_exist: " + is_received_image_exist);

        // if image allready downloaded in storage then show it from storage
        if (is_sent_image_exist) {
            //download_icon.setVisibility(View.GONE);

            thumb_file_dir = sent_thumb_video_dir + "/" + file_name;
            file_dir = sent_video_dir + "/" + file_name.replace(SharedData.thumb_image_file_type, SharedData.video_file_type);
            showImage(thumb_file_dir, messageImage);

            // if image allready downloaded in storage then show it from storage
        } else if (is_received_image_exist) {
            //download_icon.setVisibility(View.GONE);

            thumb_file_dir = received_thumb_video_dir + "/" + file_name;
            file_dir = received_video_dir + "/" + file_name.replace(SharedData.thumb_image_file_type, SharedData.video_file_type);

            showImage(thumb_file_dir, messageImage);

        } else {
            final ImageView temp_messageImage = messageImage;
            final ImageView temp_download_icon = download_icon;
            final ProgressBar temp_ProgressBar = progressBar;
            final TextView temp_percentage = percentage;

            download_icon.setVisibility(View.VISIBLE);

            Picasso picasso = Picasso.with(temp_messageImage.getContext());
            picasso.setIndicatorsEnabled(false);
            picasso.with(temp_messageImage.getContext())
                    .load(message.getThumb_image())
                    .placeholder(R.drawable.default_avatar)
                    .into(messageImage);

            thumb_file_dir = received_thumb_image_dir + "/" + file_name;
            download_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    temp_download_icon.setVisibility(View.GONE);

                    // From an HTTPS URL
                    StorageReference fileRef = storage.getReferenceFromUrl(message.getThumb_image());
                    //download thumb_image
                    SharedMethods.saveThumbFileInStorage(fileRef,
                            received_thumb_video_dir,
                            file_name);

                    //download full_video
                    StorageReference fileRef2 = storage.getReferenceFromUrl(message.getMessage());
                    SharedMethods.saveFileInStorage(
                            fileRef2,
                            received_video_dir,
                            SharedMethods.getFileName(message.getMessage(),"full_video",SharedData.video_format) + SharedData.video_format,    //full_video%2FvideoALZ4LPH93PX03B6TSM0D.jpg --> videoALZ4LPH93PX03B6TSM0D.mp4
                            temp_messageImage,
                            temp_ProgressBar,
                            temp_percentage);
                }
            });

            file_dir = received_video_dir + "/" + file_name.replace(SharedData.thumb_image_file_type, SharedData.video_file_type);
        }

        final String temp_file_dir = file_dir;
        messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ViewMediaActivity.class);
                intent.putExtra("file_type", SharedData.video_file_type);
                intent.putExtra("file_dir", temp_file_dir);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void setFileMessage(final FileViewHolder viewHolder, final Messages message, String sender_id) {
        TextView doc_file_name;
        ImageView download_icon;
        ProgressBar progressBar;
        TextView percentage;
        String file_dir = null;
        String thumb_file_dir = null;

        if (sender_id.equals(message.getFrom())) {
            //=========== for sent message ================

            viewHolder.container.setVisibility(View.GONE);
            viewHolder.container2.setVisibility(View.VISIBLE);
            viewHolder.doc_file_name2.setVisibility(View.VISIBLE);
            //viewHolder.download_icon2.setVisibility(View.VISIBLE);

            doc_file_name = viewHolder.doc_file_name2;
            download_icon = viewHolder.download_icon2;
            progressBar = viewHolder.progress_bar2;
            percentage = viewHolder.downloading_percent2;

        } else {
            //=========== for receive message =================

            viewHolder.container.setVisibility(View.VISIBLE);
            viewHolder.doc_file_name.setVisibility(View.VISIBLE);
            viewHolder.container2.setVisibility(View.GONE);

            doc_file_name = viewHolder.doc_file_name;
            download_icon = viewHolder.download_icon;
            progressBar = viewHolder.progress_bar;
            percentage = viewHolder.downloading_percent;

            setNameAndTime(viewHolder.receiver_name, viewHolder.received_time, message);
        }

        final String file_name = SharedMethods.getFileName(message.getMessage(), "full_" + SharedData.file_one_file_type, SharedData.file_one_format)
                + SharedData.file_one_format;
        boolean is_sent_image_exist = SharedMethods.checkFileExist(sent_file_dir, file_name);
        boolean is_received_image_exist = SharedMethods.checkFileExist(received_file_dir, file_name);

        doc_file_name.setText(file_name);

        Log.d(TAG, "message : " + message.getMessage());
        Log.d(TAG, "file_name: " + file_name);
        Log.d(TAG, "is_sent_image_exist: " + is_sent_image_exist);
        Log.d(TAG, "is_received_image_exist: " + is_received_image_exist);

        // if image allready downloaded in storage then show it from storage
        if (is_sent_image_exist) {
            thumb_file_dir = sent_file_dir + "/" + file_name;
            file_dir = sent_file_dir + "/" + file_name.replace(SharedData.file_one_file_type, SharedData.file_one_file_type);
            //showImage(thumb_file_dir, messageImage);

            // if image allready downloaded in storage then show it from storage
        } else if (is_received_image_exist) {
            thumb_file_dir = received_file_dir + "/" + file_name;
            file_dir = received_file_dir + "/" + file_name.replace(SharedData.file_one_file_type, SharedData.file_one_file_type);

            //showImage(thumb_file_dir, messageImage);

        } else {
            final ImageView temp_download_icon = download_icon;
            final ProgressBar temp_ProgressBar = progressBar;
            final TextView temp_percentage = percentage;

            download_icon.setVisibility(View.VISIBLE);

            download_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    temp_download_icon.setVisibility(View.GONE);

                    StorageReference fileRef = storage.getReferenceFromUrl(message.getMessage());
                    SharedMethods.saveFileInStorage(
                            fileRef,
                            received_file_dir,
                            file_name.replace(SharedData.file_one_file_type, SharedData.file_one_file_type),
                            null,
                            temp_ProgressBar,
                            temp_percentage);
                }
            });

            file_dir = received_file_dir + "/" + file_name.replace(SharedData.file_one_file_type, SharedData.file_one_file_type);
        }

        final String temp_file_dir = file_dir;
        doc_file_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Open file", Toast.LENGTH_SHORT).show();
                try {
                    File file = new File(temp_file_dir);
                    Uri path = Uri.fromFile(file);
                    Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                    pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pdfOpenintent.setDataAndType(path, "application/pdf");

                    v.getContext().startActivity(pdfOpenintent);
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());

                    Toast.makeText(v.getContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showImage(String file_dir, final ImageView messageImage) {
        try {
            final File f = new File(file_dir);
            Picasso picasso = Picasso.with(messageImage.getContext());
            picasso.setIndicatorsEnabled(false);
            picasso.with(messageImage.getContext())
                    .load(f)
                    .placeholder(R.drawable.default_avatar)
                    .into(messageImage);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }

    }


    private void setNameAndTime(final TextView name, TextView time, Messages message) {
        String date_time = SharedMethods.getDateTime(message.getTime());
        time.setText(date_time);

        mUserDatabase.child(message.getFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String receiver_name = "";
                try {
                    receiver_name = dataSnapshot.child("name").getValue().toString();
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.getMessage());
                }
                name.setText(receiver_name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
