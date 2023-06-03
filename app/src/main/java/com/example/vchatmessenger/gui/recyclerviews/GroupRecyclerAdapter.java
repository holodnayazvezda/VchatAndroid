package com.example.vchatmessenger.gui.recyclerviews;

import static com.example.vchatmessenger.core.api.server.MessageRestClient.addReader;
import static com.example.vchatmessenger.core.api.server.MessageRestClient.deleteMessage;
import static com.example.vchatmessenger.core.api.server.UserRestClient.canDeleteMessage;
import static com.example.vchatmessenger.core.api.server.UserRestClient.getBaseInfo;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vchatmessenger.gui.fragments.MiddleOfGroupViewFragment;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.fragments.TopOfSearchMessageFragment;
import com.example.vchatmessenger.dto.Message;
import com.example.vchatmessenger.dto.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupRecyclerAdapter extends RecyclerView.Adapter {
    private final List<Message> messages;
    private static Long userId;
    public static int chatType;
    private final String nameOfChat;
    private final String nameOfSearchedText;
    private static final int VIEW_TYPE_MESSAGE_SEND = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_CHANNEL = 3;
    public static boolean onSearch = false;

    public static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        TextView content, date_and_time;
        String nameOfSearchedText;

        public SendMessageViewHolder(View itemView, String nameOfSearchedText) {
            super(itemView);
            content = itemView.findViewById(R.id.content);
            date_and_time = itemView.findViewById(R.id.date_and_time);
            this.nameOfSearchedText = nameOfSearchedText;
        }

        void bind(Message message) {
            content.setText(message.getContent());
            if (message.getContent() != null && nameOfSearchedText != null) {
                if (message.getContent().toLowerCase().contains(nameOfSearchedText.toLowerCase())) {
                    int start = message.getContent().toLowerCase().indexOf(nameOfSearchedText.toLowerCase());
                    int end = start + nameOfSearchedText.length();
                    SpannableString string = new SpannableString(message.getContent());
                    string.setSpan(new BackgroundColorSpan(Color.argb(125, 132, 255, 1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    string.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.setText(string);
                }
            }
            String time = new SimpleDateFormat("HH:mm").format(message.getCreationDate());
            date_and_time.setText(time);

            if (!message.getReadersIds().contains(userId)) {
                new Thread(() -> {
                    ArrayList<String> authData = getAuthData(itemView.getContext());
                    addReader(authData.get(0), authData.get(1), message.getId());
                    message.getReadersIds().add(userId);
                }).start();
            }

            itemView.setOnLongClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle(R.string.delete_message)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            new Thread(() -> {
                                ArrayList<String> authData = getAuthData(itemView.getContext());
                                int messageDeletingResult = deleteMessage(authData.get(0), authData.get(1), message.getId());
                                if (messageDeletingResult == 1) {
                                    MiddleOfGroupViewFragment.messagesList.remove(message);
                                    MiddleOfGroupViewFragment.offset -= 1;
                                    MiddleOfGroupViewFragment.scrollTo -= 1;
                                    MiddleOfGroupViewFragment.list_of_messages.post(() -> MiddleOfGroupViewFragment.list_of_messages.getAdapter().notifyDataSetChanged());
                                }
                                if (onSearch) {
                                    if (message.getContent() != null && nameOfSearchedText != null) {
                                        if (message.getContent().toLowerCase().contains(nameOfSearchedText.toLowerCase())) {
                                            TopOfSearchMessageFragment.message_text.setText(TopOfSearchMessageFragment.message_text.getText().toString());
                                        }
                                    }
                                }
                            }).start();
                        })
                        .setNegativeButton(R.string.no, (dialogInterface, i) -> Toast.makeText(itemView.getContext(), R.string.deletion_canceled, Toast.LENGTH_SHORT).show());
                builder.create().show();
                return false;
            });
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView name, content, date_and_time;
        private final ShapeableImageView image;
        String nameOfSearchedText;


        public ReceivedMessageViewHolder(@NonNull View itemView, String nameOfSearchedText) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.content);
            date_and_time = itemView.findViewById(R.id.date_and_time);
            image = itemView.findViewById(R.id.image);
            this.nameOfSearchedText = nameOfSearchedText;
        }

        void bind(Message message) {
            content.setText(message.getContent());
            if (message.getContent() != null && nameOfSearchedText != null) {
                if (message.getContent().toLowerCase().contains(nameOfSearchedText.toLowerCase())) {
                    int start = message.getContent().toLowerCase().indexOf(nameOfSearchedText.toLowerCase());
                    int end = start + nameOfSearchedText.length();
                    SpannableString string = new SpannableString(message.getContent());
                    string.setSpan(new BackgroundColorSpan(Color.argb(150, 132, 255, 1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    string.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.setText(string);
                }
            }
            String time = new SimpleDateFormat("HH:mm").format(message.getCreationDate());
            date_and_time.setText(time);
            new Thread(() -> {
                User userBaseInfo = getBaseInfo(message.getOwnerId());
                if (userBaseInfo != null) {
                    new Thread(() -> name.post(() -> name.setText(userBaseInfo.getName()))).start();
                    new Thread(() -> {
                        byte[] imageData = android.util.Base64.decode(userBaseInfo.getImageData(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        image.post(() -> image.setImageBitmap(bitmap));
                    }).start();
                }
            }).start();

            if (!message.getReadersIds().contains(userId)) {
                new Thread(() -> {
                    ArrayList<String> authData = getAuthData(itemView.getContext());
                    addReader(authData.get(0), authData.get(1), message.getId());
                    message.getReadersIds().add(userId);
                }).start();
            }
            itemView.setOnLongClickListener(v -> {
                ArrayList<String> authData = getAuthData(itemView.getContext());
                AtomicInteger messageDeletingAbility = new AtomicInteger();
                Thread t = new Thread(() -> messageDeletingAbility.set(canDeleteMessage(authData.get(0), authData.get(1), message.getId())));
                t.start();
                try {t.join();} catch (InterruptedException e) {throw new RuntimeException(e);}
                if (messageDeletingAbility.get() == 1) {
                    new AlertDialog.Builder(itemView.getContext()).setTitle(R.string.delete_message)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> new Thread(() -> {
                                int messageDeletingResult = deleteMessage(authData.get(0), authData.get(1), message.getId());
                                if (messageDeletingResult == 1) {
                                    MiddleOfGroupViewFragment.messagesList.remove(message);
                                    MiddleOfGroupViewFragment.offset -= 1;
                                    MiddleOfGroupViewFragment.scrollTo -= 1;
                                    MiddleOfGroupViewFragment.list_of_messages.post(() -> MiddleOfGroupViewFragment.list_of_messages.getAdapter().notifyDataSetChanged());
                                }
                                if (onSearch) {
                                    if (message.getContent() != null && nameOfSearchedText != null) {
                                        if (message.getContent().toLowerCase().contains(nameOfSearchedText.toLowerCase())) {
                                            TopOfSearchMessageFragment.message_text.setText(TopOfSearchMessageFragment.message_text.getText().toString());
                                        }
                                    }
                                }
                            }).start())
                            .setNegativeButton(R.string.no, (dialogInterface, i) -> Toast.makeText(itemView.getContext(), R.string.deletion_canceled, Toast.LENGTH_SHORT).show())
                            .create().show();
                }
                return false;
            });
        }
    }

    public static class ReceivedMessageFromChannel extends RecyclerView.ViewHolder {

        private final TextView name, content, date_and_time;
        private final String nameOfChat;
        String nameOfSearchedText;


        public ReceivedMessageFromChannel(@NonNull View itemView, String nameOfSearchedText, String nameOfChat) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.content);
            date_and_time = itemView.findViewById(R.id.date_and_time);
            this.nameOfSearchedText = nameOfSearchedText;
            this.nameOfChat = nameOfChat;
        }

        void bind(Message message) {
            name.setText(nameOfChat);
            content.setText(message.getContent());
            if (message.getContent() != null && nameOfSearchedText != null) {
                if (message.getContent().toLowerCase().contains(nameOfSearchedText.toLowerCase())) {
                    int start = message.getContent().toLowerCase().indexOf(nameOfSearchedText.toLowerCase());
                    int end = start + nameOfSearchedText.length();
                    SpannableString string = new SpannableString(message.getContent());
                    string.setSpan(new BackgroundColorSpan(Color.argb(150, 132, 255, 1)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    string.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.setText(string);
                }
            }
            String time = new SimpleDateFormat("HH:mm").format(message.getCreationDate());
            date_and_time.setText(time);

            if (!message.getReadersIds().contains(userId)) {
                new Thread(() -> {
                    ArrayList<String> authData = getAuthData(itemView.getContext());
                    addReader(authData.get(0), authData.get(1), message.getId());
                    message.getReadersIds().add(userId);
                }).start();
            }

            itemView.setOnLongClickListener(v -> {
                ArrayList<String> authData = getAuthData(itemView.getContext());
                AtomicInteger messageDeletingAbility = new AtomicInteger();
                Thread t = new Thread(() -> messageDeletingAbility.set(canDeleteMessage(authData.get(0), authData.get(1), message.getId())));
                t.start();
                try {t.join();} catch (InterruptedException e) {throw new RuntimeException(e);}
                if (messageDeletingAbility.get() == 1) {
                    new AlertDialog.Builder(itemView.getContext()).setTitle(R.string.delete_message)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> new Thread(() -> {
                                int messageDeletingResult = deleteMessage(authData.get(0), authData.get(1), message.getId());
                                if (messageDeletingResult == 1) {
                                    MiddleOfGroupViewFragment.messagesList.remove(message);
                                    MiddleOfGroupViewFragment.offset -= 1;
                                    MiddleOfGroupViewFragment.scrollTo -= 1;
                                    MiddleOfGroupViewFragment.list_of_messages.post(() -> MiddleOfGroupViewFragment.list_of_messages.getAdapter().notifyDataSetChanged());
                                }
                                if (onSearch) {
                                    if (message.getContent() != null && nameOfSearchedText != null) {
                                        if (message.getContent().toLowerCase().contains(nameOfSearchedText.toLowerCase())) {
                                            TopOfSearchMessageFragment.message_text.setText(TopOfSearchMessageFragment.message_text.getText().toString());
                                        }
                                    }
                                }
                            }).start())
                            .setNegativeButton(R.string.no, (dialogInterface, i) -> Toast.makeText(itemView.getContext(), R.string.deletion_canceled, Toast.LENGTH_SHORT).show())
                            .create().show();
                }
                return false;
            });
        }
    }

    public GroupRecyclerAdapter(List<Message> messages, Long userId, int chatType, String nameOfChat, String nameOfSearchedText) {
        this.messages = messages;
        this.userId = userId;
        this.chatType = chatType;
        this.nameOfChat = nameOfChat;
        this.nameOfSearchedText = nameOfSearchedText;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (chatType == 2) {
            return VIEW_TYPE_MESSAGE_RECEIVED_CHANNEL;
        } else if (message.getOwnerId().equals(userId)) {
            return VIEW_TYPE_MESSAGE_SEND;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SEND) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_view_layout_from_me, parent, false);
            return new SendMessageViewHolder(view, nameOfSearchedText);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_view_layout_from_others, parent, false);
            return new ReceivedMessageViewHolder(view, nameOfSearchedText);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.channel_view_layout_from_others, parent, false);
            return new ReceivedMessageFromChannel(view, nameOfSearchedText, nameOfChat);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SEND:
                ((SendMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED_CHANNEL:
                ((ReceivedMessageFromChannel) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
