package com.mirutina.diaria;

import android.app.*;
import android.os.*;
import android.content.*;
import android.provider.Settings;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    JSONArray week;
    LinearLayout content, tabs;
    Handler handler = new Handler();
    int selectedDay;
    final String[] days={"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};
    final int navy=Color.rgb(21,32,57), muted=Color.rgb(108,120,144), blue=Color.rgb(70,95,224), background=Color.rgb(246,248,253);
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    int today(){return (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)+5)%7;}
    int minutes(String s){String[] a=s.split(":");return Integer.parseInt(a[0])*60+Integer.parseInt(a[1]);}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    GradientDrawable border(int color,int stroke,int radius){GradientDrawable d=shape(color,radius);d.setStroke(dp(1),stroke);return d;}
    TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    void gap(LinearLayout l,int h){View v=new View(this);l.addView(v,lp(1,h));}
    void pad(View v,int l,int t,int r,int b){v.setPadding(dp(l),dp(t),dp(r),dp(b));}
    void add(LinearLayout parent,View child){parent.addView(child,lp(-1,-2));}
    public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(background);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);selectedDay=today();try{week=Scheduler.load(this);}catch(Exception ignored){}if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},11);Scheduler.schedule(this);render();handler.postDelayed(new Runnable(){public void run(){if(selectedDay==today())render();handler.postDelayed(this,60000);}},60000);}
    void render(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(background);
        LinearLayout outer=column();scroll.addView(outer);
        LinearLayout hero=column();pad(hero,22,30,22,26);GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{navy,Color.rgb(48,65,123)});hero.setBackground(gradient);add(outer,hero);
        TextView eyebrow=text("✦  TU ORGANIZADOR PERSONAL",12,Color.rgb(177,195,255),true);add(hero,eyebrow);gap(hero,10);
        add(hero,text("Mi Rutina Diaria",30,Color.WHITE,true));gap(hero,8);
        add(hero,text("Organiza tus días. Avanza a tu ritmo.",14,Color.rgb(213,223,250),false));gap(hero,24);
        Calendar now=Calendar.getInstance();String time=String.format(Locale.getDefault(),"%02d:%02d",now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
        LinearLayout info=row();pad(info,15,14,15,14);info.setBackground(shape(Color.rgb(57,74,128),16));
        LinearLayout left=column();left.addView(text("HOY ES",10,Color.rgb(193,205,245),true));gap(left,5);left.addView(text(days[today()],19,Color.WHITE,true));info.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout right=column();right.setGravity(Gravity.RIGHT);right.addView(text("HORA ACTUAL",10,Color.rgb(193,205,245),true));gap(right,5);right.addView(text(time,19,Color.WHITE,true));info.addView(right);add(hero,info);
        content=column();pad(content,18,20,18,30);add(outer,content);
        add(content,text("Tu semana",22,navy,true));gap(content,5);add(content,text("Selecciona un día para ver sus actividades",13,muted,false));gap(content,14);
        HorizontalScrollView horizontal=new HorizontalScrollView(this);horizontal.setHorizontalScrollBarEnabled(false);tabs=row();horizontal.addView(tabs);add(content,horizontal);gap(content,20);
        renderTabs();renderDay();setContentView(scroll);
    }
    void renderTabs(){tabs.removeAllViews();for(int d=0;d<7;d++){final int index=d;TextView tab=text(days[d].substring(0,Math.min(3,days[d].length())).toUpperCase(Locale.ROOT),13,d==selectedDay?Color.WHITE:muted,true);tab.setGravity(Gravity.CENTER);pad(tab,17,14,17,14);tab.setBackground(d==selectedDay?shape(blue,14):border(Color.WHITE,Color.rgb(228,233,244),14));LinearLayout.LayoutParams params=lp(-2,-2);params.setMargins(0,0,dp(8),0);tabs.addView(tab,params);tab.setOnClickListener(v->{selectedDay=index;render();});}}
    String category(String s){String a=s.toLowerCase(Locale.ROOT);if(a.contains("universidad")||a.contains("biblioteca")||a.contains("repaso")||a.contains("tareas"))return "📚";if(a.contains("barbería")||a.contains("instituto"))return "✂";if(a.contains("fútbol")||a.contains("cancha"))return "⚽";if(a.contains("trompeta"))return "♫";if(a.contains("traslado")||a.contains("regreso"))return "➜";if(a.contains("desayuno")||a.contains("almuerzo")||a.contains("cena"))return "☕";if(a.contains("dormir")||a.contains("descanso")||a.contains("ducha"))return "☾";return "✓";}
    void saveWeek(){getSharedPreferences("routine",MODE_PRIVATE).edit().putString("week",week.toString()).apply();Scheduler.schedule(this);render();}
    void editActivity(int index){
        try{
            JSONArray entries=week.getJSONArray(selectedDay);
            JSONArray old=index>=0?entries.getJSONArray(index):null;
            LinearLayout form=column();pad(form,20,8,20,0);
            EditText name=new EditText(this);name.setSingleLine(true);name.setHint("Actividad (ej. Practicar trompeta)");if(old!=null)name.setText(old.getString(2));add(form,name);
            TextView hint=text("Horario en formato 24 horas (HH:mm)",12,muted,false);add(form,hint);
            EditText start=new EditText(this);start.setSingleLine(true);start.setHint("Inicio, ej. 19:40");start.setInputType(android.text.InputType.TYPE_CLASS_DATETIME);if(old!=null)start.setText(old.getString(0));add(form,start);
            EditText end=new EditText(this);end.setSingleLine(true);end.setHint("Fin, ej. 20:40");end.setInputType(android.text.InputType.TYPE_CLASS_DATETIME);if(old!=null)end.setText(old.getString(1));add(form,end);
            CheckBox notify=new CheckBox(this);notify.setText("Avisarme 10 minutos antes");notify.setChecked(old==null||old.length()<4||old.optBoolean(3,true));add(form,notify);
            AlertDialog.Builder dialog=new AlertDialog.Builder(this).setTitle(index<0?"Nueva actividad":"Editar actividad").setView(form).setNegativeButton("Cancelar",(d,w)->{}).setPositiveButton("Guardar",null);
            if(index>=0)dialog.setNeutralButton("Eliminar",(d,w)->new AlertDialog.Builder(this).setTitle("Eliminar actividad").setMessage("¿Quieres eliminar esta actividad?").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(x,y)->{JSONArray updated=new JSONArray();for(int i=0;i<entries.length();i++)if(i!=index)updated.put(entries.opt(i));try{week.put(selectedDay,updated);saveWeek();}catch(Exception ignored){}}).show());
            AlertDialog popup=dialog.create();popup.setOnShowListener(v->popup.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b->{
                String n=name.getText().toString().trim(),a=start.getText().toString().trim(),z=end.getText().toString().trim();
                if(n.isEmpty()||!a.matches("([01]\\d|2[0-3]):[0-5]\\d")||!z.matches("([01]\\d|2[0-3]):[0-5]\\d")||minutes(z)<=minutes(a)){Toast.makeText(this,"Revisa el nombre y las horas (HH:mm). El fin debe ser posterior al inicio.",Toast.LENGTH_LONG).show();return;}
                try{JSONArray item=new JSONArray();item.put(a);item.put(z);item.put(n);item.put(notify.isChecked());JSONArray updated=new JSONArray();for(int i=0;i<entries.length();i++){if(i==index)updated.put(item);else updated.put(entries.get(i));}if(index<0)updated.put(item);
                    ArrayList<JSONArray> sorted=new ArrayList<>();for(int i=0;i<updated.length();i++)sorted.add(updated.getJSONArray(i));Collections.sort(sorted,(x,y)->Integer.compare(minutes(x.optString(0)),minutes(y.optString(0))));JSONArray result=new JSONArray();for(JSONArray j:sorted)result.put(j);week.put(selectedDay,result);saveWeek();popup.dismiss();
                }catch(Exception e){Toast.makeText(this,"No se pudo guardar",Toast.LENGTH_SHORT).show();}
            }));popup.show();
        }catch(Exception e){Toast.makeText(this,"No se pudo abrir el editor",Toast.LENGTH_SHORT).show();}
    }
    void renderDay(){
        try{
            JSONArray entries=week.getJSONArray(selectedDay);Calendar now=Calendar.getInstance();int currentMin=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE);int complete=0;for(int i=0;i<entries.length();i++)if(done(i))complete++;
            LinearLayout overview=column();pad(overview,19,19,19,19);overview.setBackground(shape(Color.WHITE,20));add(content,overview);
            LinearLayout top=row();TextView day=text(days[selectedDay],21,navy,true);top.addView(day,new LinearLayout.LayoutParams(0,-2,1));TextView tag=text(selectedDay==today()?"HOY":"PLAN SEMANAL",11,blue,true);pad(tag,10,7,10,7);tag.setBackground(shape(Color.rgb(236,240,255),9));top.addView(tag);add(overview,top);gap(overview,8);
            add(overview,text(complete+" de "+entries.length()+" actividades completadas",13,muted,false));gap(overview,13);
            ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(entries.length());progress.setProgress(complete);progress.setProgressTintList(android.content.res.ColorStateList.valueOf(blue));progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(234,238,248)));add(overview,progress);
            if(selectedDay==today()){
                gap(content,15);LinearLayout nowCard=column();pad(nowCard,18,18,18,18);nowCard.setBackground(shape(Color.rgb(231,237,255),18));
                String current="Un momento para ti",next="No quedan actividades programadas";boolean found=false;
                for(int i=0;i<entries.length();i++){JSONArray a=entries.getJSONArray(i);int start=minutes(a.getString(0)),end=minutes(a.getString(1));if(currentMin>=start&&currentMin<end)current=a.getString(2);if(!found&&start>currentMin){next=a.getString(0)+" · "+a.getString(2);found=true;}}
                add(nowCard,text("●  EN ESTE MOMENTO",11,blue,true));gap(nowCard,7);add(nowCard,text(current,20,navy,true));gap(nowCard,12);add(nowCard,text("DESPUÉS",11,muted,true));gap(nowCard,4);add(nowCard,text(next,14,navy,false));add(content,nowCard);
            }
            gap(content,22);add(content,text("Actividades del día",20,navy,true));gap(content,5);add(content,text("Marca las actividades o toca una tarjeta para editarla",13,muted,false));gap(content,12);
            TextView addTask=text("＋  Agregar actividad",15,Color.WHITE,true);addTask.setGravity(Gravity.CENTER);pad(addTask,12,14,12,14);addTask.setBackground(shape(blue,14));addTask.setOnClickListener(v->editActivity(-1));add(content,addTask);gap(content,15);
            for(int i=0;i<entries.length();i++){
                JSONArray a=entries.getJSONArray(i);String start=a.getString(0),end=a.getString(1),name=a.getString(2);final int index=i;
                LinearLayout card=row();pad(card,15,15,12,15);card.setBackground(border(Color.WHITE,Color.rgb(231,235,245),18));
                TextView emoji=text(category(name),24,blue,true);emoji.setGravity(Gravity.CENTER);emoji.setBackground(shape(Color.rgb(238,241,253),13));card.addView(emoji,lp(46,46));
                LinearLayout detail=column();LinearLayout.LayoutParams dl=new LinearLayout.LayoutParams(0,-2,1);dl.setMargins(dp(13),0,dp(7),0);card.addView(detail,dl);
                TextView title=text(name,15,navy,true);add(detail,title);gap(detail,5);add(detail,text(start+" – "+end,12,muted,false));
                CheckBox check=new CheckBox(this);check.setButtonTintList(android.content.res.ColorStateList.valueOf(blue));check.setChecked(done(i));check.setContentDescription("Completar "+name);check.setOnCheckedChangeListener((button,checked)->{getPreferences(0).edit().putBoolean(key(index),checked).apply();render();});card.addView(check);card.setOnClickListener(v->editActivity(index));
                LinearLayout.LayoutParams cp=lp(-1,-2);cp.setMargins(0,0,0,dp(10));content.addView(card,cp);
            }
            gap(content,9);TextView notifications=text("🔔  Recordatorios 10 minutos antes de cada actividad",13,muted,false);add(content,notifications);gap(content,12);
            Button alarm=new Button(this);alarm.setText("Configurar alarmas y recordatorios");alarm.setOnClickListener(v->{if(Build.VERSION.SDK_INT>=31){if(!((AlarmManager)getSystemService(ALARM_SERVICE)).canScheduleExactAlarms())startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));else Scheduler.schedule(this);}else Scheduler.schedule(this);});add(content,alarm);
        }catch(Exception e){add(content,text("No se pudo cargar el horario: "+e.getMessage(),14,muted,false));}
    }
    String key(int index){Calendar c=Calendar.getInstance();int diff=selectedDay-today();c.add(Calendar.DAY_OF_YEAR,diff);String identity="";try{JSONArray item=week.getJSONArray(selectedDay).getJSONArray(index);identity=item.optString(0)+"|"+item.optString(2);}catch(Exception ignored){}return selectedDay+"-"+identity+"-"+c.get(Calendar.YEAR)+"-"+c.get(Calendar.DAY_OF_YEAR);}
    boolean done(int index){return getPreferences(0).getBoolean(key(index),false);}
    protected void onResume(){super.onResume();Scheduler.schedule(this);}
}
