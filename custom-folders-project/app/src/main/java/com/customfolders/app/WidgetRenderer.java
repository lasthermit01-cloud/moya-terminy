package com.customfolders.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class WidgetRenderer {
    private WidgetRenderer() {}

    public static Bitmap render(Context c, FolderStore.Folder f, int w, int h) {
        w = Math.max(w, 96);
        h = Math.max(h, 96);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        int base;
        try { base = Color.parseColor(f.color); }
        catch (Exception e) { base = Color.rgb(108, 99, 255); }
        int alpha = Math.max(30, Math.min(255, f.opacity));
        int color = Color.argb(alpha, Color.red(base), Color.green(base), Color.blue(base));

        float radius = "circle".equals(f.shape) ? Math.min(w, h) / 2f
                : "square".equals(f.shape) ? Ui.dp(c, 4)
                : Math.min(w, h) * .16f;
        RectF bounds = new RectF(3, 3, w - 3, h - 3);
        Path clip = new Path();
        clip.addRoundRect(bounds, radius, radius, Path.Direction.CW);

        cv.save();
        cv.clipPath(clip);
        p.setColor(color);
        cv.drawRect(0, 0, w, h, p);

        Bitmap customIcon = loadUri(c, f.iconUri);
        if (customIcon != null) {
            p.setAlpha(alpha);
            drawCenterCrop(cv, customIcon, new RectF(0, 0, w, h), p);
            cv.restore();
            return b;
        }

        Bitmap bg = loadUri(c, f.bgUri);
        if (bg != null) {
            p.setAlpha(alpha);
            drawCenterCrop(cv, bg, new RectF(0, 0, w, h), p);
        }
        cv.restore();

        int pad = Math.max(Ui.dp(c, 6), w / 18);
        int top = pad;

        if (f.emoji != null && !f.emoji.isEmpty()) {
            p.setAlpha(255);
            p.setColor(Color.WHITE);
            p.setTextSize(Math.min(w, h) / 3.2f);
            p.setTextAlign(Paint.Align.RIGHT);
            p.setTypeface(Typeface.DEFAULT);
            p.setShadowLayer(Ui.dp(c, 2), 0, Ui.dp(c, 1), 0x99000000);
            cv.drawText(f.emoji, w - pad, pad + p.getTextSize(), p);
            p.setTextAlign(Paint.Align.LEFT);
            p.clearShadowLayer();
        }

        PackageManager pm = c.getPackageManager();
        int count = Math.min(f.previewCount, f.packages.size());
        if (count > 0) {
            int cols = count <= 4 ? 2 : 3;
            int rows = (int) Math.ceil(count / (double) cols);
            int availH = h - top - pad;
            int cell = Math.min((w - pad * 2) / cols, Math.max(Ui.dp(c, 24), availH / Math.max(1, rows)));
            int icon = (int) (cell * .62f);
            for (int i = 0; i < count; i++) {
                int row = i / cols, col = i % cols;
                int x = pad + col * cell + (cell - icon) / 2;
                int y = top + row * cell + (cell - icon) / 2;
                try {
                    Drawable d = pm.getApplicationIcon(f.packages.get(i));
                    Bitmap ib = drawableBitmap(d, icon, icon);
                    p.setAlpha(255);
                    cv.drawBitmap(ib, null, new RectF(x, y, x + icon, y + icon), p);
                } catch (Exception ignored) {}
            }
        }
        return b;
    }

    public static Bitmap renderPreview(Context c, FolderStore.Folder f, int w, int h) {
        int labelH = f.showTitle ? Math.max(Ui.dp(c, 30), h / 6) : 0;
        int iconH = Math.max(Ui.dp(c, 80), h - labelH);
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(out);
        Bitmap icon = render(c, f, w, iconH);
        cv.drawBitmap(icon, 0, 0, null);
        if (f.showTitle) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(ThemeUtils.text(c));
            p.setTextSize(Math.max(Ui.dp(c, 12), labelH * .5f));
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            String name = f.name == null || f.name.trim().isEmpty() ? "Папка" : f.name.trim();
            cv.drawText(ellipsize(name, 22), w / 2f, iconH + labelH * .68f, p);
        }
        return out;
    }

    private static String ellipsize(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    private static void drawCenterCrop(Canvas canvas, Bitmap src, RectF dst, Paint paint) {
        float srcRatio = src.getWidth() / (float) src.getHeight();
        float dstRatio = dst.width() / dst.height();
        Rect crop;
        if (srcRatio > dstRatio) {
            int cropW = Math.max(1, Math.round(src.getHeight() * dstRatio));
            int left = Math.max(0, (src.getWidth() - cropW) / 2);
            crop = new Rect(left, 0, Math.min(src.getWidth(), left + cropW), src.getHeight());
        } else {
            int cropH = Math.max(1, Math.round(src.getWidth() / dstRatio));
            int top = Math.max(0, (src.getHeight() - cropH) / 2);
            crop = new Rect(0, top, src.getWidth(), Math.min(src.getHeight(), top + cropH));
        }
        canvas.drawBitmap(src, crop, dst, paint);
    }

    public static Bitmap loadUri(Context c, String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            Uri uri = Uri.parse(s);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                File file = new File(uri.getPath());
                try (InputStream in = new FileInputStream(file)) {
                    return BitmapFactory.decodeStream(in);
                }
            }
            try (InputStream in = c.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(in);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap drawableBitmap(Drawable d, int w, int h) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, w, h);
        d.draw(c);
        return b;
    }
}
