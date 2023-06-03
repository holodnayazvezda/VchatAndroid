package com.example.vchatmessenger.gui.recyclerviews;

import static com.example.vchatmessenger.core.api.server.MessageRestClient.getLastMessage;
import static com.example.vchatmessenger.core.api.server.UserRestClient.canDeleteChat;
import static com.example.vchatmessenger.core.api.server.UserRestClient.removeChat;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getId;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vchatmessenger.gui.fragments.GroupViewFragment;
import com.example.vchatmessenger.gui.activities.GroupViewActivity;
import com.example.vchatmessenger.gui.fragments.NoChatFoundFragment;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.fragments.SearchChatFragment;
import com.example.vchatmessenger.gui.fragments.SelectChatFragment;
import com.example.vchatmessenger.gui.fragments.TopOfSearchChatFragment;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.dto.Message;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.fragments.ChatViewFragment;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {
    private static List<Group> chats = null;
    private static int scrolledPosition = 0;
    private static RecyclerView recyclerView;

    public static int getScrolledPosition() {
        return scrolledPosition;
    }

    public static void setScrolledPosition(int scrolledPosition) {
        ChatRecyclerAdapter.scrolledPosition = scrolledPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView chatImage;
        private final TextView nameOfChat;
        private final TextView lastMsg;
        private final TextView lastMsgTime;
        private final TextView unreadMsgCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatImage = itemView.findViewById(R.id.chat_image);
            nameOfChat = itemView.findViewById(R.id.name_of_chat);
            lastMsg = itemView.findViewById(R.id.last_msg);
            lastMsgTime = itemView.findViewById(R.id.last_msg_time);
            unreadMsgCount = itemView.findViewById(R.id.unread_msg_count);
        }

        public void setDialog(Group chat) {
            ArrayList<String> authData = getAuthData(itemView.getContext());
            int position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            if (position <= 2) {
                position = 0;
            }
            setScrolledPosition(position);
            new Thread(() -> {
                byte[] imageData = Base64.decode(chat.getImageData(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                chatImage.post(() -> chatImage.setImageBitmap(bitmap));
            }).start();
            new Thread(() -> nameOfChat.post(() -> nameOfChat.setText(chat.getName()))).start();
            new Thread(() -> {
                lastMsg.post(() -> lastMsg.setText(R.string.no_messages));
                lastMsgTime.post(() -> lastMsgTime.setText(""));
            }).start();
            if (chat.getUnreadMsgCount() > 0) {
                new Thread(() -> unreadMsgCount.post(() -> unreadMsgCount.setText(String.valueOf(chat.getUnreadMsgCount())))).start();
            } else {
                new Thread(() -> unreadMsgCount.post(() -> unreadMsgCount.setText(""))).start();
            }
            new Thread(() -> {
                Message message = getLastMessage(authData.get(0), authData.get(1), chat.getId());
                if (message != null) {
                    lastMsg.post(() -> lastMsg.setText(message.getContent()));
                    String time = new SimpleDateFormat("HH:mm").format(message.getCreationDate());
                    lastMsgTime.post(() -> lastMsgTime.setText(time));
                } else {
                    lastMsg.post(() -> lastMsg.setText(R.string.no_messages));
                    lastMsgTime.post(() -> lastMsgTime.setText(""));
                }
            }).start();
            itemView.setOnClickListener(view -> {
                Bundle data = new Bundle();
                data.putLong("chatId", chat.getId());
                data.putString("nameOfChat", chat.getName());
                data.putLong("userId", getId(itemView.getContext()));
                data.putInt("chatType", chat.getType());
                data.putBoolean("noCheck", true);
                if (itemView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Intent intent = new Intent(itemView.getContext(), GroupViewActivity.class);
                    intent.putExtras(data);
                    itemView.getContext().startActivity(intent);
                } else {
                    // получить fragment manager из activity
                    FragmentManager fragmentManager = ((FragmentActivity) itemView.getContext()).getSupportFragmentManager();
                    // начать транзакцию
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    // создаем bundle с данными (id)
                    GroupViewFragment groupViewFragment = new GroupViewFragment();
                    groupViewFragment.setArguments(data);
                    fragmentTransaction.replace(R.id.empty_dialog_horizontal, groupViewFragment);
                    fragmentTransaction.commit();
                }
            });
            itemView.setOnLongClickListener(v -> {
                AtomicInteger canDeleteChat = new AtomicInteger();
                if (ChatViewActivity.startSearchChat) {
                    Thread t = new Thread(() -> canDeleteChat.set(canDeleteChat(authData.get(0), authData.get(1), chat.getId())));
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    canDeleteChat.set(1);
                }
                if (canDeleteChat.get() == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setTitle(R.string.delete_chat)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> new Thread(() -> {
                                removeChat(authData.get(0), authData.get(1), chat.getId());
                                ChatViewFragment.chatList.remove(chat);
                                ChatViewFragment.scrollToChat = 0;
                                ChatViewFragment.list_of_chats.post(() -> ChatViewFragment.list_of_chats.getAdapter().notifyDataSetChanged());
                                ChatViewFragment.offset -= 1;
                                if (ChatViewActivity.isStartSearchChat()) {
                                    if (chats.size() > 0) {
                                        SearchChatFragment.foundChatList.remove(chat);
                                        TopOfSearchChatFragment.setCurrentPosition(0);
                                        SearchChatFragment.list_of_chats.post(() -> SearchChatFragment.list_of_chats.getAdapter().notifyDataSetChanged());
                                        SearchChatFragment.offset -= 1;
                                    } else {
                                        NoChatFoundFragment noChatFoundFragment = new NoChatFoundFragment();
                                        if (itemView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                            ((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.empty_dialog_vertical, noChatFoundFragment).commit();
                                        } else {
                                            ((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_chats_layout, noChatFoundFragment).commit();
                                        }
                                    }
                                }
                                if (itemView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    if (chat.getId() == GroupViewFragment.id && GroupViewFragment.id != -1) {
                                        ((FragmentActivity) itemView.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.empty_dialog_horizontal, new SelectChatFragment()).commit();
                                    }
                                }
                            }).start())
                            .setNegativeButton(R.string.no, (dialogInterface, i) -> Toast.makeText(itemView.getContext(), R.string.deletion_canceled, Toast.LENGTH_SHORT).show());
                    builder.create().show();
                }
                return false;
            });
        }
    }

    public ChatRecyclerAdapter(List<Group> chats) {
        ChatRecyclerAdapter.chats = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_view_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setDialog(chats.get(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        ChatRecyclerAdapter.recyclerView = recyclerView;
    }
}
