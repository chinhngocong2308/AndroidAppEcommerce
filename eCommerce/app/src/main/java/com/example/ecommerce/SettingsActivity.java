package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText fullNameEditText, userPhoneEditText, addressEditText, checkedPasswordText;
    private TextView profileChangeTextBtn;
    private  ImageView closeTextBtn, saveTextButton;
    private StorageTask uploadTask;
    private Uri imageUri;
    private String myUrl = "";
    private StorageReference storageProfilePrictureRef;
    private String checker="";

    private static final int GalleryPick =  1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfilePrictureRef = FirebaseStorage.getInstance().getReference().child("Profile picture");

        String phoneorderold = Prevalent.currentOnlineUser.getPhoneOrder();
        String fullnameold = Prevalent.currentOnlineUser.getName();
        String addressold = Prevalent.currentOnlineUser.getAddress();

        profileImageView = (CircleImageView) findViewById(R.id.settings_profile_image);

        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
        userPhoneEditText = (EditText) findViewById(R.id.settings_phone_number);
        addressEditText = (EditText) findViewById(R.id.settings_address);

        checkedPasswordText = (EditText) findViewById(R.id.checked_password);
        //placeholder edit text
        userPhoneEditText.setText(phoneorderold);
        addressEditText.setText(addressold);
        fullNameEditText.setText(fullnameold);

        profileChangeTextBtn = (TextView) findViewById(R.id.profile_image_change_btn);
        closeTextBtn = (ImageView) findViewById(R.id.close_settings_btn);
        saveTextButton = (ImageView) findViewById(R.id.update_account_settings_btn);

        userInfoDisplay(profileImageView, fullNameEditText, userPhoneEditText, addressEditText);

        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";
                OpenGallery();
//                CropImage.activity(imageUri)
//                        .setAspectRatio(1, 1)
//                        .start(SettingsActivity.this);
            }
        });

        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checker.equals("clicked"))
                {
                    userInfoSaved();
                }
                else
                {
                    updateOnlyUserInfo();
                }
            }
        });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    private void updateOnlyUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        String passworduser = Prevalent.currentOnlineUser.getPassword();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullNameEditText.getText().toString());
        userMap.put("address", addressEditText.getText().toString());
        userMap.put("phoneOrder", userPhoneEditText.getText().toString());

        String checkedpassword = checkedPasswordText.getText().toString();
        if(checker.equals("clicked"))
        {
            uploadImage();
        }
        //  Validate form
        if (TextUtils.isEmpty(userPhoneEditText.getText().toString()))
        {
            Toast.makeText(this, "Vui lòng điện loại số điện thoại", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullNameEditText.getText().toString()))
        {
            Toast.makeText(this, "Vui lòng điện lại họ tên", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(addressEditText.getText().toString()))
        {
            Toast.makeText(this, "Vui lòng điền lại địa chỉ", Toast.LENGTH_SHORT).show();
        }



        // Save
        if (!TextUtils.isEmpty(fullNameEditText.getText().toString()) && !TextUtils.isEmpty(addressEditText.getText().toString())
                && !TextUtils.isEmpty(userPhoneEditText.getText().toString())) {

            if (checkedpassword.equals(passworduser))
            {
                ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);

                Toast.makeText(SettingsActivity.this, "Cập nhật thông tin cá nhân thành công.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                Toast.makeText(this, "Mật khẩu xác nhận không đúng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }



    private void userInfoSaved()
    {
//        if (TextUtils.isEmpty(fullNameEditText.getText().toString()))
//        {
//            Toast.makeText(this, "Họ tên đẩy đủ không được để trống", Toast.LENGTH_SHORT).show();
//        }
//        else if (TextUtils.isEmpty(addressEditText.getText().toString()))
//        {
//            Toast.makeText(this, "Địa không được để trống", Toast.LENGTH_SHORT).show();
//        }
//        else if (TextUtils.isEmpty(userPhoneEditText.getText().toString()))
//        {
//            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
//        }
       if(checker.equals("clicked"))
        {
            uploadImage();
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cập nhập thông tin");
        progressDialog.setMessage("Vui lòng đợi, chúng tôi đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if(imageUri != null )
        {
            final StorageReference fileRef = storageProfilePrictureRef.child(Prevalent.currentOnlineUser.getPhone() + ".jpg");

            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name", fullNameEditText.getText().toString());
                        userMap.put("address", addressEditText.getText().toString());
                        userMap.put("phoneOrder", userPhoneEditText.getText().toString());
                        userMap.put("image", myUrl);
                        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        progressDialog.dismiss();
                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                        Toast.makeText(SettingsActivity.this, "Cập nhật thông tin cá nhân thành công.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, "Ảnh đại diện chưa được chọn", Toast.LENGTH_SHORT).show();

        }

    }

    private void userInfoDisplay(CircleImageView profileImageView, EditText fullNameEditText,
                                 EditText userPhoneEditText, EditText addressEditText)
    {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if (snapshot.child("image").exists())
                    {
                        String image = snapshot.child("image").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();
                        String phone = snapshot.child("phone").getValue().toString();
                        String address = snapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageView);
                        fullNameEditText.setText(name);
                        userPhoneEditText.setText(phone);
                        addressEditText.setText(address);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}