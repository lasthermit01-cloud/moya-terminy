package com.customfolders.app;
import android.app.*;import android.appwidget.*;import android.content.*;import android.os.*;import android.widget.*;
public class FolderWidgetProvider extends AppWidgetProvider{
 public static final String PREF="widget_map";
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);}
 @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle o){update(c,m,id);}
 @Override public void onDeleted(Context c,int[] ids){SharedPreferences.Editor e=c.getSharedPreferences(PREF,0).edit();for(int id:ids)e.remove("w"+id);e.apply();}
 public static void bind(Context c,int widgetId,long folderId){c.getSharedPreferences(PREF,0).edit().putLong("w"+widgetId,folderId).apply();update(c,AppWidgetManager.getInstance(c),widgetId);}
 public static void updateAll(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);ComponentName n=new ComponentName(c,FolderWidgetProvider.class);for(int id:m.getAppWidgetIds(n))update(c,m,id);}
 public static void update(Context c,AppWidgetManager m,int id){long fid=c.getSharedPreferences(PREF,0).getLong("w"+id,-1);FolderStore.Folder f=new FolderStore(c).get(fid);RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.widget_folder);if(f==null){rv.setImageViewBitmap(R.id.widget_image,WidgetRenderer.render(c,new FolderStore.Folder(),180,180));m.updateAppWidget(id,rv);return;}Bundle op=m.getAppWidgetOptions(id);int minW=op.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,110),minH=op.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,110);int w=Math.min(700,Math.max(110,Ui.dp(c,minW))),h=Math.min(700,Math.max(110,Ui.dp(c,minH)));rv.setImageViewBitmap(R.id.widget_image,WidgetRenderer.render(c,f,w,h));Intent i=new Intent(c,FolderPopupActivity.class).putExtra("folderId",f.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent pi=PendingIntent.getActivity(c,(int)(id*31L&0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);rv.setOnClickPendingIntent(R.id.widget_image,pi);m.updateAppWidget(id,rv);}
}
