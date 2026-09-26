package com.customfolders.app;
import android.app.Activity;import android.content.*;import android.content.res.Configuration;import android.graphics.Color;
public final class ThemeUtils{
 private ThemeUtils(){}
 public static String mode(Context c){return c.getSharedPreferences("settings",0).getString("theme","system");}
 public static void setMode(Context c,String m){c.getSharedPreferences("settings",0).edit().putString("theme",m).apply();}
 public static boolean dark(Context c){String m=mode(c);if("dark".equals(m))return true;if("light".equals(m))return false;return (c.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;}
 public static void apply(Activity a){a.setTheme(dark(a)?R.style.Theme_CustomFolders_Dark:R.style.Theme_CustomFolders_Light);}
 public static int bg(Context c){return dark(c)?Color.rgb(16,17,22):Color.rgb(246,247,251);}public static int surface(Context c){return dark(c)?Color.rgb(29,30,38):Color.WHITE;}public static int text(Context c){return dark(c)?Color.rgb(241,242,247):Color.rgb(28,29,35);}public static int sub(Context c){return dark(c)?Color.rgb(171,173,187):Color.rgb(101,103,115);}public static int accent(Context c){return dark(c)?Color.rgb(156,149,255):Color.rgb(108,99,255);}public static int line(Context c){return dark(c)?Color.rgb(50,52,63):Color.rgb(230,231,238);}
}
