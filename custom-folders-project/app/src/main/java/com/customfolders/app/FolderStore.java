package com.customfolders.app;
import android.content.*;import org.json.*;import java.util.*;
public class FolderStore{
 private final SharedPreferences sp; public FolderStore(Context c){sp=c.getSharedPreferences("folders_db",0);} 
 public static class Folder{
  public long id;public String name="Папка",color="#6C63FF",bgUri="",iconUri="",emoji="📁",shape="rounded",animation="zoom",popupSize="medium";public int opacity=230,previewCount=4,grid=4,iconSize=64;public boolean showTitle=true,glass=true;public final ArrayList<String> packages=new ArrayList<>();
  JSONObject json()throws Exception{JSONObject o=new JSONObject();o.put("id",id);o.put("name",name);o.put("color",color);o.put("bgUri",bgUri);o.put("iconUri",iconUri);o.put("emoji",emoji);o.put("shape",shape);o.put("animation",animation);o.put("popupSize",popupSize);o.put("opacity",opacity);o.put("previewCount",previewCount);o.put("grid",grid);o.put("iconSize",iconSize);o.put("showTitle",showTitle);o.put("glass",glass);JSONArray a=new JSONArray();for(String p:packages)a.put(p);o.put("packages",a);return o;}
  static Folder from(JSONObject o){Folder f=new Folder();f.id=o.optLong("id");f.name=o.optString("name","Папка");f.color=o.optString("color","#6C63FF");f.bgUri=o.optString("bgUri","");f.iconUri=o.optString("iconUri","");f.emoji=o.optString("emoji","📁");f.shape=o.optString("shape","rounded");f.animation=o.optString("animation","zoom");f.popupSize=o.optString("popupSize","medium");f.opacity=o.optInt("opacity",230);f.previewCount=o.optInt("previewCount",4);f.grid=o.optInt("grid",4);f.iconSize=o.optInt("iconSize",64);f.showTitle=o.optBoolean("showTitle",true);f.glass=o.optBoolean("glass",true);JSONArray a=o.optJSONArray("packages");if(a!=null)for(int i=0;i<a.length();i++)f.packages.add(a.optString(i));return f;}
 }
 public List<Folder> all(){ArrayList<Folder> out=new ArrayList<>();try{JSONArray a=new JSONArray(sp.getString("folders","[]"));for(int i=0;i<a.length();i++)out.add(Folder.from(a.getJSONObject(i)));}catch(Exception ignored){}return out;}
 public Folder get(long id){for(Folder f:all())if(f.id==id)return f;return null;}
 public long save(Folder f){List<Folder> a=all();if(f.id<=0)f.id=System.currentTimeMillis();boolean found=false;for(int i=0;i<a.size();i++)if(a.get(i).id==f.id){a.set(i,f);found=true;break;}if(!found)a.add(f);write(a);return f.id;}
 public void delete(long id){List<Folder>a=all();a.removeIf(f->f.id==id);write(a);}
 public Folder duplicate(long id){Folder x=get(id);if(x==null)return null;Folder f=Folder.from(toJson(x));f.id=System.currentTimeMillis();f.name=x.name+" — копія";save(f);return f;}
 private JSONObject toJson(Folder f){try{return f.json();}catch(Exception e){return new JSONObject();}}
 private void write(List<Folder>a){JSONArray j=new JSONArray();for(Folder f:a)try{j.put(f.json());}catch(Exception ignored){}sp.edit().putString("folders",j.toString()).apply();}
 public String exportAll(){JSONObject root=new JSONObject();try{root.put("format","custom-folders-v1");root.put("folders",new JSONArray(sp.getString("folders","[]")));root.put("templates",new JSONArray(sp.getString("templates","[]")));}catch(Exception ignored){}return root.toString();}
 public void importAll(String s)throws Exception{JSONObject r=new JSONObject(s);if(!"custom-folders-v1".equals(r.optString("format")))throw new Exception("Невідомий формат");sp.edit().putString("folders",r.optJSONArray("folders")==null?"[]":r.getJSONArray("folders").toString()).putString("templates",r.optJSONArray("templates")==null?"[]":r.getJSONArray("templates").toString()).apply();}
 public void saveTemplate(String name,Folder f){JSONArray a;try{a=new JSONArray(sp.getString("templates","[]"));JSONObject o=f.json();o.remove("id");o.remove("packages");o.put("templateName",name);a.put(o);sp.edit().putString("templates",a.toString()).apply();}catch(Exception ignored){}}
 public JSONArray templates(){try{return new JSONArray(sp.getString("templates","[]"));}catch(Exception e){return new JSONArray();}}
}
