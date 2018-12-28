package ru.mr_reminder.mr_reminder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddMementoActivity extends AppCompatActivity implements OnMapReadyCallback {
    //Константы запросов
    static final int GALLERY_REQUEST = 1;
    private final int TAKE_PICTURE_REQUEST = 2;
    public static final int GALLERY_KITKAT_INTENT_CALLED = 3;
    private ImageView imageView;

    //Таблица
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

    //Элементы активности
    EditText nameBox;
    EditText descriptionBox;
    EditText datetimeBox;
    EditText latBox;
    EditText lngBox;
    EditText urlBox;
    Button delButton;
    Button photoBtn;
    Button saveButton;

    DBHelper sqlHelper;
    SQLiteDatabase db;
    Cursor mementoCursor;
    long mementoId = 0;

    //Карты
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    Marker marker;
    Location location; // Location

    LatLng curPosition = new LatLng(53.182498, 44.9989733);

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
        latBox = (EditText) findViewById(R.id.latView);
        lngBox = (EditText) findViewById(R.id.lngView);
        sqlHelper = new DBHelper(this);
        db = sqlHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                pickIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


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
            String latStr = mementoCursor.getString(mementoCursor.getColumnIndex(Cols.LATITUDE));
            String lngStr = mementoCursor.getString(mementoCursor.getColumnIndex(Cols.LONGITUDE));
            latBox.setText(latStr);
            lngBox.setText(lngStr);


            if (urlStr != null) {
                urlBox.setText(urlStr);
//
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//                startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                Bitmap bitmap = null;
//
//                try {
//                    bitmap = getBitmapFromUri(Uri.parse(urlStr));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                imageView.setImageBitmap(bitmap);

            } else {
                imageView.setVisibility(View.GONE);
            }

            mementoCursor.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationManager lm =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert lm != null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = lm
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double curLat;
        double curLong;

        if (location != null) {
            curLat = location.getLatitude();
            curLong = location.getLongitude();

            curPosition = new LatLng(curLat, curLong);
        }

        latBox = (EditText) findViewById(R.id.latView);
        lngBox = (EditText) findViewById(R.id.lngView);
        String lng = lngBox.getEditableText().toString();
        String lat = latBox.getEditableText().toString();

        if (!Objects.equals(lng, "") && !Objects.equals(lat, "")) {
            LatLng pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            MarkerOptions marker = new MarkerOptions().position(pos).title("Местоположение задания");
            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curPosition, 15));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(point.latitude, point.longitude)).title("Местоположение задания");
                mMap.clear();
                mMap.addMarker(marker);
                latBox = (EditText) findViewById(R.id.latView);
                lngBox = (EditText) findViewById(R.id.lngView);

                latBox.setText(Double.toString(point.latitude));
                lngBox.setText(Double.toString(point.longitude));

                System.out.println(point.latitude + "---" + point.longitude);
            }
        });
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

                    imageView.setImageURI(selectedImage);
                    urlBox = (EditText) findViewById(R.id.imgUrlView);
                    assert selectedImage != null;
                    urlBox.setText(selectedImage.toString());
                }
                break;

            case TAKE_PICTURE_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Проверяем, содержит ли результат маленькую картинку
                    if (imageReturnedIntent != null) {
                        if (imageReturnedIntent.hasExtra("output")) {

                            imageView = (ImageView) findViewById(R.id.imageView);
                            try {
                                bitmap = getBitmapFromUri(Uri.parse(String.valueOf(imageReturnedIntent.getParcelableExtra("output"))));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Какие-то действия с миниатюрой
                            Uri selectedImage = imageReturnedIntent.getData();
                            final int takeFlags = imageReturnedIntent.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
// Check for the freshest data.
                            assert selectedImage != null;
                            getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                            imageView.setImageBitmap(bitmap);

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
        cv.put(Cols.LATITUDE, latBox.getText().toString());
        cv.put(Cols.LONGITUDE, lngBox.getText().toString());
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
