package com.edu.swufe.myapplication;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyList2Activity extends ListActivity implements Runnable, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private String TAG="mylist2";
    Handler handler;
    private List<HashMap<String, String>> listItems;//存放文字、图片信息
    private SimpleAdapter listItemAdapter;//适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        initListView();
        //MyAdapter myAdapter=new MyAdapter( this,R.layout.list_item,listItems );
        this.setListAdapter( listItemAdapter );
        Thread t = new Thread( this );
        t.start();
        ;

        handler = new Handler() {


            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 7) {
                    listItems = (List<HashMap<String, String>>) msg.obj;
                    listItemAdapter = new SimpleAdapter( MyList2Activity.this, listItems,
                            R.layout.list_item,
                            new String[]{"ItemTitle", "ItemDetail"},
                            new int[]{R.id.itemTitle, R.id.itemDetail}
                    );
                    setListAdapter( listItemAdapter );
                }
                super.handleMessage( msg );
            }

            ;
        };
        //添加一个点击事件的监听
        getListView().setOnItemClickListener(this ) ;
        //长按
        getListView().setOnItemLongClickListener(  this);
    }
        private void initListView() {
                listItems = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < 10; i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put( "ItemTitle", "Rate:" + i );//标题文字
                    map.put( "ItemDetail", "detail" + i );//详情描述
                    listItems.add( map );
                }
                //生成适配器的Item和动态数组对应的元素
                listItemAdapter = new SimpleAdapter( this, listItems,//ListItems数据源
                        R.layout.list_item,//ListItem的XML布局的实现
                        new String[]{"ItemTitle", "ItemDetail"},
                        new int[]{R.id.itemTitle, R.id.itemDetail}
                );
            }

            public void run() {

                //获取网络数据，放入list带回到主线程中
                List<HashMap<String, String>> retList = new ArrayList<>();
                Document doc = null;
                try {
                    Thread.sleep( 3000 );
                    doc = Jsoup.connect( "http://www.bankofchina.com/sourcedb/whpj/" ).get();
                    //doc=Jsoup.parse(html);
                    Log.i( TAG, "run:" + doc.title() );
                    Elements tables = doc.getElementsByTag( "table" );

//            for(Element table :tables){
//                Log.i(TAG,"run:table["+i+"]="+table);
//                i++;
//            }
                    Element table = tables.get( 1 );
                    //Log.i(TAG,"run:table6="+table6);
                    //获取TD中的数据
                    Elements tds = table.getElementsByTag( "td" );
                    for (int i = 0; i < tds.size(); i += 8) {
                        Element td1 = tds.get( i );
                        Element td2 = tds.get( i + 5 );

                        String str1 = td1.text();
                        String val = td2.text();

                        Log.i( TAG, "run=" + str1 + "==>" + val );
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put( "ItemTitle", str1 );
                        map.put( "ItemDetail", val );
                        retList.add( map );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                 //msg.what=5;
                //msg.obj="Hello from run()";
                Message msg = handler.obtainMessage( 7 );
                msg.obj = retList;
                handler.sendMessage( msg );
            }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击行，获取数据项，从数据项中获取数据
        Log.i( TAG,"onItemClick:parent="+parent );
        Log.i( TAG,"onItemClick:view="+view );
        Log.i( TAG,"onItemClick:position="+position );
        Log.i( TAG,"onItemClick:id="+id );
        HashMap<String,String> map= (HashMap<String, String>) getListView().getItemAtPosition( position );
        String titleStr =map.get( "ItemTitle" );
        String detailStr =map.get( "ItemDetail" );
        Log.i( TAG,"onItemClick:titleStr="+titleStr );
        Log.i( TAG,"onItemClick:detailStr="+detailStr );
        //控件中获取元素
        TextView title =(TextView)view.findViewById(R.id.itemTitle);
        TextView detail =(TextView)view.findViewById(R.id.itemDetail);
        //获取数据
        String title2= String.valueOf( title.getText() ) ;
        String detail2= String.valueOf( detail.getText() ) ;

        Log.i( TAG,"onItemClick:title2="+title2 );
        Log.i( TAG,"onItemClick:detail2="+detail2 );
        //打开新的页面传入参数
        Intent rateCalc=new Intent( this ,RateCalcActivity.class);
        //带入参数
        rateCalc.putExtra( "title",titleStr );
        //放入汇率
        rateCalc.putExtra( "rate",Float.parseFloat(detailStr  ) );
        //传到下一个页面
        startActivity( rateCalc );
    }
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        Log.i( TAG,"onItemLongClick:长按列表项position"+position );
        //长按删除操作
        //listItems.remove( position );
        //listItemAdapter.notifyDataSetChanged();
        AlertDialog.Builder builder=new AlertDialog.Builder( this );
        builder.setTitle( "提示" ).setMessage( "请确认是否删除当前数据" ).setPositiveButton( "是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i( TAG,"onclick:对话框事件处理" );
                listItems.remove( position );
                listItemAdapter.notifyDataSetChanged();
            }
        } ).setNegativeButton( "否",null);
        builder.create().show();
        Log.i( TAG,"onItemClick:size="+listItems.size() );
        //长按不调用短按方法
        return true;
    }
}
