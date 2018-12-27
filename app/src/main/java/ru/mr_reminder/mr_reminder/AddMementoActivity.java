package ru.mr_reminder.mr_reminder;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMementoActivity extends AppCompatActivity {
    static final int GALLERY_REQUEST = 1;
    private final int TAKE_PICTURE_REQUEST = 2;
    public static final int GALLERY_KITKAT_INTENT_CALLED = 3;
    private ImageView imageView;


    public static final String TABLE_NAME = "Memento";

    public static final class Cols {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String DATETIME = "datetime";
        public static final String PHOTO = "photo";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";

    }

    EditText nameBox;
    EditText descriptionBox;
    EditText datetimeBox;
    EditText urlBox;
    Button delButton;
    Button photoBtn;
    Button saveButton;

    DBHelper sqlHelper;
    SQLiteDatabase db;
    Cursor mementoCursor;
    long mementoId = 0;

    public final static String DEBUG_TAG = "AddMementoActivity";

    public static File photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memento);

        nameBox = (EditText) findViewById(R.id.name);
        descriptionBox = (EditText) findViewById(R.id.description);
        datetimeBox = (EditText) findViewById(R.id.datetime);
        delButton = (Button) findViewById(R.id.deleteButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        urlBox = (EditText) findViewById(R.id.imgUrlView);
        photoBtn = (Button) findViewById(R.id.photoBtn);

        sqlHelper = new DBHelper(this);
        db = sqlHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
//we will handle the returned data in onActivityResult
                startActivityForResult(Intent.createChooser(pickIntent, "Выбрать изображение"), GALLERY_REQUEST);
            }
        });

        Button photoBtn = (Button) findViewById(R.id.photoBtn);
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//getThumbnailPicture();
                saveFullImage();
            }
        });
        if (extras != null) {
            mementoId = extras.getLong("id");
        }
        // если 0, то добавление
        if (mementoId > 0) {
            // получаем элемент по id из бд
            mementoCursor = db.rawQuery("select * from " + TABLE_NAME + " where " +
                    Cols.ID + "=?", new String[]{String.valueOf(mementoId)});
            mementoCursor.moveToFirst();
            nameBox.setText(mementoCursor.getString(mementoCursor.getColumnIndex(Cols.NAME)));
            descriptionBox.setText(mementoCursor.getString(mementoCursor.getColumnIndex(Cols.DESCRIPTION)));
            datetimeBox.setText(mementoCursor.getString(mementoCursor.getColumnIndex(Cols.DATETIME)));
            String urlStr = mementoCursor.getString(mementoCursor.getColumnIndex(Cols.PHOTO));

            if (urlStr != null) {
                urlBox.setText(urlStr);

//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//                startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                Bitmap bitmap = null;

                try {
                    bitmap = getBitmapFromUri(Uri.parse(urlStr));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageView.setImageBitmap(bitmap);

            } else {
                imageView.setVisibility(View.GONE);
            }

            mementoCursor.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Bitmap bitmap = null;
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        switch (requestCode) {
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    final int takeFlags = imageReturnedIntent.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
// Check for the freshest data.
                    assert selectedImage != null;
                    getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                    imageView.setImageURI(selectedImage);
                    urlBox = (EditText) findViewById(R.id.imgUrlView);
                    urlBox.setText(selectedImage.toString());
                }
                break;

            case TAKE_PICTURE_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Проверяем, содержит ли результат маленькую картинку
                    if (imageReturnedIntent != null) {
                        if (imageReturnedIntent.hasExtra("output")) {
                            Bitmap thumbnailBitmap = imageReturnedIntent.getParcelableExtra("output");
                            // Какие-то действия с миниатюрой
                            Uri selectedImage = imageReturnedIntent.getData();
                            final int takeFlags = imageReturnedIntent.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
// Check for the freshest data.
                            assert selectedImage != null;
                            getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                            imageView.setImageBitmap(thumbnailBitmap);

                            urlBox = (EditText) findViewById(R.id.imgUrlView);
                            urlBox.setText(selectedImage.toString());
                        }
                    }
                }
                break;
        }

    }

    private void getThumbnailPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }

    private void saveFullImage() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.ENGLISH);
        Date date = Calendar.getInstance().getTime();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        File file = new File(Environment.getExternalStorageDirectory(),
                date.toString() + ".jpg");

        Uri outputFileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }


    public void save(View view) {
        ContentValues cv = new ContentValues();
        cv.put(Cols.NAME, nameBox.getText().toString());
        cv.put(Cols.DESCRIPTION, descriptionBox.getText().toString());
        cv.put(Cols.DATETIME, datetimeBox.getText().toString());
        cv.put(Cols.PHOTO, urlBox.getText().toString());

        if (mementoId > 0) {
            db.update(TABLE_NAME, cv, Cols.ID + "=" + String.valueOf(mementoId), null);
        } else {
            db.insert(TABLE_NAME, null, cv);
        }
        goHome();
    }

    public void delete(View view) {
        db.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(mementoId)});
        goHome();
    }

    private void goHome() {
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
