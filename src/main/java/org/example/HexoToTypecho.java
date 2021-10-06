package org.example;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexoToTypecho {

    public static void readFileToString(String path) throws SQLException {
        File dir = new File(path);
        if(dir.isDirectory()){
            String files[] = dir.list();
            for(String s: files){
                File file = new File(dir + "/" + s);
                if(!file.isDirectory()){
                    modifyMysql(getHeader(fileToString(file)));
//                    getHeader(fileToString(file));
                }
            }
        }
    }

    private static String fileToString(File file){
        try{
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();
            return new String(buffer,"utf-8");
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private static Map getHeader(String file){
        String reg = "(---)[\\s\\S]{0,300}(---)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(file);
        int start = 0;
        int end = 0;
        String header = null;
        while(matcher.find()){
            header = matcher.group();
            header = header.substring(3,header.length()-3);
            start = matcher.start();
            end = matcher.end();
        }
        header = header.replaceAll("\t","  ");
        Yaml yaml = new Yaml();
        Map<String,Object> conf =(Map<String,Object>) yaml.load(header);
        file = file.substring(end,file.length());
        file = file.replaceAll("/image/" ,"http://localhost:5001/typecho_blog/usr/uploads/image/" );
        conf.put("content","<!--markdown-->"+file);
//        byte[] str = "/.jar包来".getBytes(StandardCharsets.UTF_8);
//        System.out.println(Base64.getEncoder().encodeToString(str));
        return conf;
    }

    private static void modifyMysql(Map conf) throws SQLException {
        ComboPooledDataSource ds = new ComboPooledDataSource("hexototypecho");
        Connection con =ds.getConnection();
        Statement st = con.createStatement();
        /*
         * contents sheet
         */
        String title = (String)conf.get("title");
        Date date = (Date)conf.get("date");
        String text = (String)conf.get("content");
//        System.out.println(text);

        String sql = "insert into typecho_blog_contents values(null,'"+title+"',null,"+date.getTime()/1000+","+date.getTime()/1000+",'"
                +text.replaceAll("'","  ")+"',0,1,null,'post','publish',null,0,'1','1','1',0,0,0)";
        st.executeUpdate(sql);
        /*
         *  relationships sheet
         */
        ArrayList<String> cates = ( ArrayList<String>)conf.get("categories");
        if(cates != null){
            for(String s: cates){
                sql = "select * from typecho_blog_metas where name='"+s+"' and type='category'";
                Statement st3 = con.createStatement();
                ResultSet rs = st3.executeQuery(sql);
                while(rs.next()){
                    int mid = Integer.parseInt(rs.getString("mid"));
                    int cid=0;
                    String sql4 = "select cid from typecho_blog_contents where title='"+title+"'";
                    Statement st2 = con.createStatement();
                    ResultSet rs2 = st2.executeQuery(sql4);
                    int i=1;
                    while(rs2.next() && i!=0){
                        cid = Integer.parseInt(rs2.getString("cid"));
                        i=0;
                    }
                    rs2.close();st2.close();
                    String sql3 = "insert into typecho_blog_relationships values("+cid+","+mid+")";
                    st.executeUpdate(sql3);
                    System.out.println(sql3);
                }
                rs.close();
                st3.close();
            }
        }
        ArrayList<String> tags = ( ArrayList<String>)conf.get("tag");
        if(tags != null){
            for(String s: tags){
                sql = "select * from typecho_blog_metas where name='"+s+"' and type='category'";
                Statement st3 = con.createStatement();
                ResultSet rs = st3.executeQuery(sql);
                while(rs.next()){
                    int mid = Integer.parseInt(rs.getString("mid"));
                    int cid=0;
                    String sql4 = "select cid from typecho_blog_contents where title='"+title+"'";
                    Statement st2 = con.createStatement();
                    ResultSet rs2 = st2.executeQuery(sql4);
                    int i=1;
                    while(rs2.next() && i!=0){
                        cid = Integer.parseInt(rs2.getString("cid"));
                        i=0;
                    }
                    rs2.close();
                    st2.close();
                    String sql3 = "insert into typecho_blog_relationships values("+cid+","+mid+")";
                    st.executeUpdate(sql3);
                    System.out.println(sql3);
                }
                rs.close();
                st3.close();
            }
        }

        /*
         *  metas sheet change count
         */
        sql = "select mid,count(*) count from typecho_blog_relationships group by mid";
        Statement st3 = con.createStatement();
        ResultSet rs = st3.executeQuery(sql);
        while(rs.next()){
            int mid = Integer.parseInt(rs.getString("mid"));
            String sql2 = "update typecho_blog_metas set count="+Integer.parseInt(rs.getString("count"))+"  where mid="+mid;
            st.executeUpdate(sql2);
        }
        rs.close();
        st3.close();
        st.close();

        /*
         *  fields sheet
         */
        con.close();
    }

//    public static void main(String[] args) throws SQLException {
//        readFileToString("D:\\web\\hexo\\blog\\myblog\\source\\_posts");
//    }
}
