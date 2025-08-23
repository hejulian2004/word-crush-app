package com.example.wordcrush.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.wordcrush.Activity.GameRecordActivity;
import com.example.wordcrush.Activity.LoginActivity;
import com.example.wordcrush.Database.GameRecord.GameRecordEntity;
import com.example.wordcrush.GameRecord.GameRecord;
import com.example.wordcrush.R;
import com.example.wordcrush.Server.AccountServer;
import com.example.wordcrush.Server.GameRecordServer;
import com.example.wordcrush.Server.WordServer;
import com.example.wordcrush.Tools.GameRecordCallBack;
import com.example.wordcrush.Tools.MessageCallBack;
import com.example.wordcrush.Tools.MyCallBack;
import com.example.wordcrush.Tools.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment implements View.OnClickListener{

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private TextView usernameText;
    ImageView avatarImageView;
    private Button showScore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.logoutButton).setOnClickListener(this);
        view.findViewById(R.id.gameRecordBtn).setOnClickListener(this);
        view.findViewById(R.id.changeAvatarBtn).setOnClickListener(this);
        view.findViewById(R.id.changePasswordBtn).setOnClickListener(this);
        view.findViewById(R.id.syncCloudData).setOnClickListener(this);
        showScore = view.findViewById(R.id.showScore);
        showScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllScore();
            }
        });
        usernameText = view.findViewById(R.id.usernameText);
        usernameText.setText(Tools.username);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        setAvatar();
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        File file = uriToFile(imageUri);
                        AccountServer.getInstance().uploadImageToServer(file, new MessageCallBack() {
                            @Override
                            public void onSuccess(String result) {
                                Tools.sendLog(result);
                                requireActivity().runOnUiThread(()->{
                                    reSetAvatar();
                                    Tools.toast("头像修改成功！", requireContext());
                                });
                            }

                            @Override
                            public void onFailure(String e) {
                                Tools.sendLog(e);
                                Tools.toast("头像修改失败！", requireContext());
                            }
                        });
                    }
                });
    }

    private void getAllScore() {
        GameRecordServer.getInstance().getAllScore(requireContext(), new MyCallBack() {
            @Override
            public void onSuccess(Bundle bundle) {
                getActivity().runOnUiThread(()->{
                    new AlertDialog.Builder(getContext())
                            .setTitle("查看游戏分数")
                            .setMessage("闯关模式分数：" + bundle.getInt("score0") + "\n" + "限时模式分数：" +bundle.getInt("score1"))
                            .setPositiveButton("确定", null)
                            .show();
                });
            }

            @Override
            public void onFailure(Bundle bundle) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logoutButton){
            new AlertDialog.Builder(requireContext())
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        logout();
                        dialog.dismiss();
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if(v.getId() == R.id.gameRecordBtn){
            Intent intent = new Intent(getActivity(), GameRecordActivity.class);
            startActivity(intent);
        } else if(v.getId() == R.id.changeAvatarBtn){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        } else if (v.getId() == R.id.changePasswordBtn) {
            showChangePasswordDialog(getContext());
        } else if (v.getId() == R.id.syncCloudData){
            syncCloudData(getContext());
        }
    }

    private void syncCloudData(Context context) {
        if(context == null)return;
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("同步云端数据")
                .setMessage("云端数据将覆盖本地数据，未上传云端的本地数据将被删除，无法恢复！")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GameRecordServer.getInstance().getAllRecordFromCloud(new GameRecordCallBack() {
                            @Override
                            public void onSuccess(List<GameRecord> gameRecords) {
                                GameRecordServer.getInstance().deleteAllRecords(requireContext());
                                List<GameRecordEntity> gameRecordEntities = new ArrayList<>();
                                for(GameRecord record : gameRecords){
                                    gameRecordEntities.add(new GameRecordEntity(record));
                                }
                                GameRecordServer.getInstance().insertAllRecords(requireContext(), gameRecordEntities, new MessageCallBack() {
                                    @Override
                                    public void onSuccess(String result) {
                                        //getAllScore();
                                        Tools.sendLog(result);
                                    }

                                    @Override
                                    public void onFailure(String e) {
                                        Tools.sendLog(e);
                                    }
                                });
                                WordServer.getInstance().setAllWordNotMaster(requireContext());
                                for(GameRecord record: gameRecords){
                                    WordServer.getInstance().setMaster(requireContext(), record.getLearnedWords());
                                }

                                ((Activity) context).runOnUiThread(()->{
                                    Tools.toast("数据同步成功！",getContext());
                                });
                            }

                            @Override
                            public void onFailure(String e) {
                                ((Activity) context).runOnUiThread(()->{
                                    Tools.toast(e,getContext());
                                });
                            }
                        });
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(Color.RED);
        }
    }

    private void logout(){
        SharedPreferences sp = requireContext().getSharedPreferences("word-crush", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("token", "");
        editor.putString("uid", "");
        editor.apply();
        WordServer.getInstance().setAllWordNotMaster(requireContext());
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        Tools.avatarUrl="";
        startActivity(intent);
        if(getActivity() != null){
            getActivity().finish();
        }
    }
    private File uriToFile(Uri uri) {
        File file = new File(requireContext().getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // 可选：缩放图片以进一步压缩（比如压到宽高不超过 1024）
            int maxSize = 1024;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float scale = Math.min((float) maxSize / width, (float) maxSize / height);
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        Math.round(width * scale),
                        Math.round(height * scale),
                        true);
            }

            // 将 Bitmap 压缩为 JPG 并写入文件
            try (OutputStream outputStream = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // 80 表示压缩质量（0-100）
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public void setAvatar(){
        if(Tools.avatarUrl.isEmpty()){
            reSetAvatar();
            return;
        }
        Glide.with(requireContext())
                .load(Tools.avatarUrl)
                .placeholder(R.drawable.default_avatar) // 默认头像
                .into(avatarImageView);
    }
    public void reSetAvatar(){
        String avatarUrl = Tools.DOMAIN + "/static/avatars/" + Tools.username + ".jpg" + "?t=" + System.currentTimeMillis();
        Tools.avatarUrl = avatarUrl;
        Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.drawable.default_avatar) // 默认头像
                .into(avatarImageView);
    }

    private void showChangePasswordDialog(Context context) {
        // 创建输入框
        EditText oldPasswordInput = new EditText(context);
        oldPasswordInput.setHint("当前密码");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText newPasswordInput = new EditText(context);
        newPasswordInput.setHint("新密码");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText confirmPasswordInput = new EditText(context);
        confirmPasswordInput.setHint("确认新密码");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // 用 LinearLayout 包裹输入框
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        layout.addView(oldPasswordInput);
        layout.addView(newPasswordInput);
        layout.addView(confirmPasswordInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("修改密码");
        builder.setView(layout);

        builder.setPositiveButton("确认", null);
        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            confirmBtn.setOnClickListener(v -> {
                String oldPwd = oldPasswordInput.getText().toString().trim();
                String newPwd = newPasswordInput.getText().toString().trim();
                String confirmPwd = confirmPasswordInput.getText().toString().trim();

                if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                    Tools.toast("请填写所有字段", context);
                } else if (!newPwd.equals(confirmPwd)) {
                    Tools.toast("两次新密码不一致", context);
                } else if (newPwd.length() < 6) {
                    Tools.toast("密码位数须大于6位", context);
                } else {
                    // TODO: 这里执行修改密码逻辑，比如调用服务器接口
                    AccountServer.getInstance().changePassword(oldPwd, newPwd, new MessageCallBack() {
                        @Override
                        public void onSuccess(String result) {
                            ((Activity) context).runOnUiThread(()->{
                                Tools.toast(result, context);
                                logout();
                                dialog.dismiss();
                            });
                        }

                        @Override
                        public void onFailure(String e) {
                            ((Activity) context).runOnUiThread(()->{
                                Tools.toast(e, context);
                            });
                        }
                    });

                }
            });
        });

        dialog.show();
    }
    
}
