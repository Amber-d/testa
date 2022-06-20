package com.example.testa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 0x00000010;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    //自定义变量
    private ImageView imageShow;           //显示图片
    private Bitmap bmp;
    private final int IMAGE_OPEN = 0;      //打开图片
    public ImageView ivPhoto;
    public ImageView ivCamera;
    public Uri mCameraUri;

    /**
     * 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
     */
    public String mCameraImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageShow = (ImageView) findViewById(R.id.imageView1);
        ivCamera = findViewById(R.id.ivCamera);
        ivPhoto = findViewById(R.id.ivPhoto);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
//        ivCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                checkPermissionAndCamera();
//            }
//        });
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                //拒绝权限，弹出提示框。
                Toast.makeText(this, "拍照权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有权限，调起相机拍照。
            openCamera();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
    }

    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri();
            } else {
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    private boolean isAndroidQ = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //创建Menu
        //自定义menu 添加图标(使用自带图标)
        menu.add(1, 1, 1, "本地").
                setIcon(android.R.drawable.ic_menu_slideshow);
        menu.add(1, 2, 2, "相机").
                setIcon(android.R.drawable.ic_menu_view);
        SubMenu file = menu.addSubMenu(1, 3, 3, "图片处理");
        file.add(2, 1, 3, "怀旧").
                setIcon(android.R.drawable.ic_menu_edit);
        file.add(2, 2, 4, "浮雕").
                setIcon(android.R.drawable.ic_menu_gallery);
        file.add(2, 3, 5, "模糊").
                setIcon(android.R.drawable.ic_menu_crop);
        file.add(2, 4, 6, "光照").
                setIcon(android.R.drawable.ic_menu_camera);
        file.add(2, 5, 7, "锐化").
                setIcon(android.R.drawable.ic_menu_view);
        file.add(2, 6, 8, "油画").
                setIcon(android.R.drawable.ic_menu_slideshow);
        menu.add(1, 10, 9, "保存").
                setIcon(android.R.drawable.ic_menu_view);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //选择Menu
        //选择id 对应Menu.add的参数Menu.FIRST+i
        int id = item.getItemId();
        if (item.getGroupId() == 1) {
            switch (id) {
                case 1:
                    Toast.makeText(this, "打开本地图片", Toast.LENGTH_SHORT).show();
                    OpenImage();
                    break;
                case 2:
                    Toast.makeText(this, "打开相机", Toast.LENGTH_SHORT).show();
                    openCamera();
                    break;
                case 10:
                    Toast.makeText(this, "保存图片", Toast.LENGTH_SHORT).show();

                    break;
            }
        } else {
            switch (id) {
                case 1:
                    Toast.makeText(this, "图片怀旧效果", Toast.LENGTH_SHORT).show();
                    OldRemeberImage();
                    break;
                case 2:
                    Toast.makeText(this, "图片浮雕效果", Toast.LENGTH_SHORT).show();
                    ReliefImage();
                    break;
                case 3:
                    Toast.makeText(this, "图片模糊效果", Toast.LENGTH_SHORT).show();
                    FuzzyImage();
                    break;
                case 4:
                    Toast.makeText(this, "图片光照效果", Toast.LENGTH_SHORT).show();
                    SunshineImage();
                    break;
                case 5:
                    Toast.makeText(this, "图片锐化效果", Toast.LENGTH_SHORT).show();
                    SharpenImage();
                    break;
                case 6:
                    Toast.makeText(this, "图片油画效果", Toast.LENGTH_SHORT).show();
                    YouHua();
                    break;
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    private void SharpenImage() {
        int[] laplacian = new int[]{-1, -1, -1, -1, 9, -1, -1, -1, -1};
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int pixColor = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int idx = 0;
        float alpha = 0.3F;  //图片透明度
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        //图像处理
        for (int i = 1; i < height - 1; i++) {
            for (int k = 1; k < width - 1; k++) {
                idx = 0;
                newR = 0;
                newG = 0;
                newB = 0;
                for (int n = -1; n <= 1; n++)   //取出图像3*3领域像素
                {
                    for (int m = -1; m <= 1; m++)  //n行数不变 m列变换
                    {
                        pixColor = pixels[(i + n) * width + k + m];  //当前点(i,k)
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);
                        //图像像素与对应摸板相乘
                        newR = newR + (int) (pixR * laplacian[idx] * alpha);
                        newG = newG + (int) (pixG * laplacian[idx] * alpha);
                        newB = newB + (int) (pixB * laplacian[idx] * alpha);
                        idx++;
                    }
                }
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                //赋值
                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageShow.setImageBitmap(bitmap);
    }

    //自定义函数 打开图片
    public void OpenImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_OPEN);
    }


    public static boolean saveImageToGallery(Context context, Bitmap ab) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = ab.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //显示打开图片
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_OPEN) {
            Uri imageFileUri = data.getData();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;    //手机屏幕水平分辨率
            int height = dm.heightPixels;  //手机屏幕垂直分辨率
            try {
                //载入图片尺寸大小没载入图片本身 true
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inJustDecodeBounds = true;
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmpFactoryOptions);
                int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
                int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);
                //inSampleSize表示图片占原图比例 1表示原图
                if (heightRatio > 1 && widthRatio > 1) {
                    if (heightRatio > widthRatio) {
                        bmpFactoryOptions.inSampleSize = heightRatio;
                    } else {
                        bmpFactoryOptions.inSampleSize = widthRatio;
                    }
                }
                //图像真正解码 false
                bmpFactoryOptions.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmpFactoryOptions);
                imageShow.setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }  //end if
    }

    //图片怀旧处理
    private void OldRemeberImage() {
        /*
         * 怀旧处理算法即设置新的RGB
         * R=0.393r+0.769g+0.189b
         * G=0.349r+0.686g+0.168b
         * B=0.272r+0.534g+0.131b
         */
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
                newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
                newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageShow.setImageBitmap(bitmap);
        saveImageToGallery(this, bitmap);

    }

    //图片浮雕处理
//底片效果也非常简单:将当前像素点的RGB值分别与255之差后的值作为当前点的RGB
//灰度图像:通常使用的方法是gray=0.3*pixR+0.59*pixG+0.11*pixB
    private void ReliefImage() {
        /*
         * 算法原理：(前一个像素点RGB-当前像素点RGB+127)作为当前像素点RGB值
         * 在ABC中计算B点浮雕效果(RGB值在0~255)
         * B.r = C.r - B.r + 127
         * B.g = C.g - B.g + 127
         * B.b = C.b - B.b + 127
         */
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1; i < height - 1; i++) {
            for (int k = 1; k < width - 1; k++) {
                //获取前一个像素颜色
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                //获取当前像素
                pixColor = pixels[(width * i + k) + 1];
                newR = Color.red(pixColor) - pixR + 127;
                newG = Color.green(pixColor) - pixG + 127;
                newB = Color.blue(pixColor) - pixB + 127;
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                pixels[width * i + k] = Color.argb(255, newR, newG, newB);
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageShow.setImageBitmap(bitmap);
    }

    //图像模糊处理
    private void FuzzyImage() {
        /*
         * 算法原理：
         * 简单算法将像素周围八个点包括自身共九个点RGB值分别相加后平均,当前像素点的RGB值
         * 复杂算法采用高斯模糊
         * 高斯矩阵 int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
         * 将九个点的RGB值分别与高斯矩阵中的对应项相乘的和,再除以一个相应的值作为当前像素点的RGB
         */
        int[] gauss = new int[]{1, 2, 1, 2, 4, 2, 1, 2, 1};  // 高斯矩阵
        int delta = 16; // 除以值 值越小图片会越亮,越大则越暗
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR, newG, newB;
        int pos = 0;    //位置
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        //循环赋值
        for (int i = 1; i < height - 1; i++) {
            for (int k = 1; k < width - 1; k++) {
                pos = 0;
                newR = 0;
                newG = 0;
                newB = 0;
                for (int m = -1; m <= 1; m++)  //宽不变
                {
                    for (int n = -1; n <= 1; n++) //高先变
                    {
                        pixColor = pixels[(i + m) * width + k + n];
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);
                        //3*3像素相加
                        newR = newR + (int) (pixR * gauss[pos]);
                        newG = newG + (int) (pixG * gauss[pos]);
                        newB = newB + (int) (pixB * gauss[pos]);
                        pos++;
                    }
                }
                newR /= delta;
                newG /= delta;
                newB /= delta;
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageShow.setImageBitmap(bitmap);
    }

    //图片光照效果
    private void SunshineImage() {
        /*
         * 算法原理：(前一个像素点RGB-当前像素点RGB+127)作为当前像素点RGB值
         * 在ABC中计算B点浮雕效果(RGB值在0~255)
         * B.r = C.r - B.r + 127
         * B.g = C.g - B.g + 127
         * B.b = C.b - B.b + 127
         * 光照中心取长宽较小值为半径,也可以自定义从左上角射过来
         */
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        //围绕圆形光照
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY);
        float strength = 150F;  //光照强度100-150
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1; i < height - 1; i++) {
            for (int k = 1; k < width - 1; k++) {
                //获取前一个像素颜色
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = pixR;
                newG = pixG;
                newB = pixB;
                //计算当前点到光照中心的距离,平面坐标系中两点之间的距离
                int distance = (int) (Math.pow((centerY - i), 2) + Math.pow((centerX - k), 2));
                if (distance < radius * radius) {
                    //按照距离大小计算增强的光照值
                    int result = (int) (strength * (1.0 - Math.sqrt(distance) / radius));
                    newR = pixR + result;
                    newG = newG + result;
                    newB = pixB + result;
                }
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                pixels[width * i + k] = Color.argb(255, newR, newG, newB);
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageShow.setImageBitmap(bitmap);
    }
    public void YouHua()
    {
        Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), Bitmap.Config.RGB_565);
        int color;
        int Radio = 0;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Random rnd = new Random();
        int iModel = 10;
        int i = width - iModel;
        while (i > 1)
        {
            int j = height - iModel;
            while (j > 1)
            {
                int iPos = rnd.nextInt(100000) % iModel;
                color = bmp.getPixel(i + iPos, j + iPos);
                bitmap.setPixel(i, j, color);
                j = j - 1;
            }
            i = i - 1;
        }
        imageShow.setImageBitmap(bitmap);
    }
}