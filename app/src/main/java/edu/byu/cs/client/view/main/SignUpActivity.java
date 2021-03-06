package edu.byu.cs.client.view.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import edu.byu.cs.client.R;
import edu.byu.cs.client.net.request.SignUpRequest;
import edu.byu.cs.client.presenter.SignUpPresenter;
import edu.byu.cs.client.view.asyncTasks.SignUpTask;

public class SignUpActivity extends AppCompatActivity implements SignUpPresenter.View, SignUpTask.SignUpObserver {

    private SignUpPresenter presenter;

    private EditText mUsername;
    private EditText mPassword;
    private EditText mFirstName;
    private EditText mLastName;
    private ImageView mImage;
    private Button mUploadButton;
    private String imageToUpload;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        presenter = new SignUpPresenter(this);

        mUsername = this.findViewById(R.id.usernameInput);
        mPassword = this.findViewById(R.id.passwordInput);
        mFirstName = this.findViewById(R.id.firstNameInput);
        mLastName = this.findViewById(R.id.lastNameInput);
        mUploadButton = this.findViewById(R.id.uploadButton);
        mImage = this.findViewById(R.id.imageView);

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 0);
            }
        });

    }

    @Override
    public void register(View v){
        SignUpTask signUpTask = new SignUpTask(this, presenter);
        SignUpRequest signUpRequest = new SignUpRequest(mUsername.getText().toString(),
                mPassword.getText().toString(),
                mFirstName.getText().toString(),
                mLastName.getText().toString(),
                imageToUpload);
        signUpTask.execute(signUpRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)         //TODO: get image from s3 correctly
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            final Uri imageUri = data.getData();
            mImage.setImageURI(imageUri);
            try {
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageToUpload = encodeImage(selectedImage);
            }
            catch(Exception x){
                System.err.print("Something went wrong while uploading photo:");
                x.printStackTrace();
            }
        }
    }

    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.URL_SAFE);
        String safeImage = encImage.replaceAll("\n","");

        return safeImage;
    }

    @Override
    public void signUpSuccess(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void signUpError(String error){
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }


}
