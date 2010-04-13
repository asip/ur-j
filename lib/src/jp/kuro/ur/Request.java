//UR(Unified Request) -  A http request parser
// Copyright (C) 2002-2010 Yasumasa Ashida.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

package jp.kuro.ur;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.util.*;

import java.util.regex.*;

/** リクエストパラメータ解析クラス
 * @author Yasumasa Ashida
 * @version 0.9.0
 */
public final class Request {

    public static final int MULTI_TYPE_ARRAY = 0;
    public static final int MULTI_TYPE_LIST = 1;

    private static final String ANALYZE_GET_1 = "([^&]+)=([^&]*)";
    private static final String ANALYZE_POST_1 = "multipart¥¥/form-data; boundary=([¥¥w¥¥W]*)";
    private static final String ENC_ISO8859_1 =  "ISO8859_1";
    private static final String ANALYZE_POST_2 = "Content-Disposition: form-data; name=\"([^\"]*)\"\\r\\n\\r\\n";
    private static final String ANALYZE_POST_2_1 = "Content-Disposition: form-data; name=\"([^\"]*)\"\\r\\n\\r\\n([\\w\\W]*)\\r\\n";
    private static final String ANALYZE_POST_3 = "^Content-Disposition: form-data; name=\"([^\"\\n]*)\"; filename=\"([^\"\\n]*)\"\\r\\nContent-Type: ([^\\n]*)\\r\\n";
    private static final String ANALYZE_POST_4 = "\\\\([^\\\\]*)$|/([^\\/]*)$";

    private static final String BOUNDARY_PART = "--";
    private static final String EN_N = "¥n";

    private static final String POST = "POST";
    private static final String GET = "GET";

    private String characterEncoding = "ISO8859_1";
    private HashMap<String,Boolean> arrayToOne = null;
    private int maxSize = 0;
    private int multiType = 0;

    private StringBuilder sbuf;

    private String qs;
    private HashMap<String,Object> query;

    private ArrayList<Object> prms;

    private byte[] content = null;
    private byte[] content_head;
    private boolean size_flg;
    private int rowCount;

    //テンポラリ変数
    private boolean isOver;
    private byte b;

    /**
     * デフォルトコンストラクタ
     */
    public Request() {
        //sbuf = new StringBuilder();
    }

    /**
     * リクエストのメッセージボディで使われている文字エンコーディングを変換します。
     * このメソッドはanalyzeメソッドを呼び出す前に実行されなければなりません。
     * @param enc 文字エンコーディング名
     */
    public final void setCharacterEncoding(String enc){
        this.characterEncoding = enc;
    }

    private String getCharacterEncoding(){
        return characterEncoding;
    }

    /** パラメータマップを取得します。
     *  パラメータマップの key は String 型。 パラメータマップの値はString,FileStorageもしくはArrayListです。
      * @return  HashMap パラメータ名が キー 、パラメータ値がマップの値と なっている不変な java.util.HashMap。
     */
    public final HashMap<String,Object> getQuery(){
        return query;
    }

    protected HashMap<String,Boolean> getArrayToOne() {
        return arrayToOne;
    }

    /**
     * パラメータが単一の値の場合の返値のタイプをパラメータ毎に制御します。<br>
     * trueの場合は配列(String[]、FileStorage)あるいはArrayListオブジェクトを返し、falseの場合は<br>
     * StringあるいはFileStorageオブジェクトを返します。デフォルトはfalseです。
     * @param name パラメータ名
     * @param flag   パラメータが単一の値の場合の返値制御フラグ
     */
    public final void setArrayToOne(String name,boolean flag){
        if(arrayToOne == null){
            arrayToOne = new HashMap<String,Boolean>();
        }
        arrayToOne.put(name, flag);
    }


    /**
     * パラメータの最大バイト長をセットします。
     * 単位はキロバイトです。
     * @param maxByteSize 最大バイト長
     */
    public final void setMaxByteSize(int maxByteSize){
        this.maxSize = maxByteSize * 1024;
    }

    /**
     * パラメータが複数値の場合の返値を指定する。<br>
     * デフォルトは配列(0)です。
     * @param multiType 返値タイプ(0:配列、1:リスト)
     */
    public void setMultiType(int multiType) {
        this.multiType = multiType;
    }

    /** リクエストのメッセージボディを解析します。<br>
     * パラメータが単一の値の場合にはパラメータマップにStringあるいはFileStorageオブジェクトをセットし、<br>
     * パラメータが複数値の場合にはパラメータマップに配列(String[],FileStorage)あるいはArrayListオブジェクトを<br>
     * セットします(デフォルトは配列)。
     * @param req HttpServletRequestオブジェクト<br>
     * @return Requestオブジェクト
     * @throws IOException 入出力の例外が発生した場合
     */
    public final Request analyze(HttpServletRequest req) throws IOException {

        String method;

        ServletInputStream sis;

        query = new HashMap<String,Object>(16);

        method = req.getMethod();
        if(method.equals(POST)){

            sis=req.getInputStream();
            
            analyzePost(sis,req);

        }
        else if(method.equals(GET)){

            qs =  req.getQueryString();

            if(qs == null){
                return this;
            }

            //System.out.println(qs);
            parseQueryString();
        }
        alterMultiType();

        return this;
    }

    private void alterMultiType(){
        if(multiType == 0){
            String name;
            Object obj;
            Iterator itr;
            ArrayList aryList;
            String[] strs;
            FileStorage[] fss;

            itr= query.keySet().iterator();

            while(itr.hasNext()){
                name = (String)itr.next();
                obj = query.get(name);

                if(obj != null && obj instanceof ArrayList){
                    aryList = (ArrayList)obj;

                    if(aryList.size() > 0){
                        if(aryList.get(0) instanceof String){
                            strs = new String[aryList.size()];

                            for(int i=0;i < aryList.size();i++){
                                strs[i] = (String)aryList.get(i);
                            }
                            query.put(name,strs);
                        }
                        if(aryList.get(0) instanceof FileStorage){
                            fss = new FileStorage[aryList.size()];

                            for(int i=0;i < aryList.size();i++){
                                fss[i] = (FileStorage)aryList.get(i);
                            }
                            query.put(name,fss);
                        }
                    }
                }
            }
        }
    }

    private void analyzePost(ServletInputStream sis,HttpServletRequest req) throws IOException{

        Pattern ptn;
        Matcher matcher;

        InputStreamReader isr;
        BufferedReader br;

        String s;
        String method;
        String pattern;

        ArrayList<Part> split;


        ptn = Pattern.compile(ANALYZE_POST_1);
        matcher = ptn.matcher(req.getContentType());
        if(matcher.matches()){

            method = matcher.group(1);

            pattern = sbuf.append(BOUNDARY_PART).append(method).toString();

            split = splitBytes(sis,pattern);

            analyzeMultiPart(split);

        }else{

            isr = new InputStreamReader(sis,ENC_ISO8859_1);
            br = new BufferedReader(isr);

            //sbuf.setLength(0);
            sbuf = new StringBuilder();

            while((s = br.readLine()) !=null){
                sbuf.append(s).append(EN_N);
            }

            br.close();

            qs = sbuf.toString();

            if(qs == null){
                return;
            }
            //System.out.println(qs);
            parseQueryString();
        }
    }


    private void analyzeMultiPart(ArrayList<Part> split) throws IOException{

        Pattern ptn;
        Matcher matcher;

        byte[] bytes;

        String tmp;
        String name;
        String fileName;

        b= EN_N.getBytes(ENC_ISO8859_1)[0];

        for(Part pt : split){

            bytes = pt.getBytes();

            content = pt.getHeadBytes();

            tmp = new String(content,getCharacterEncoding());

            ptn = Pattern.compile(ANALYZE_POST_2);
            matcher = ptn.matcher(tmp);

            if(matcher.matches()){
                analyzeCharacter(new String(bytes,getCharacterEncoding()));
            }else{

                ptn = Pattern.compile(ANALYZE_POST_3);
                matcher = ptn.matcher(tmp);

                if(matcher.find()){
                    name = matcher.group(1);

                    FileStorage fs = new FileStorage();

                    fileName = matcher.group(2);
                    fs.setMimeType(matcher.group(3));

                    ptn = Pattern.compile(ANALYZE_POST_4);
                    matcher = ptn.matcher(fileName);

                    if(matcher.find()){
                        fs.setUploadPath(fileName);
                        fs.setUploadName(matcher.group(1));
                    }else{
                        fs.setUploadPath(null);
                        fs.setUploadName(fileName);
                    }

                    isOver = pt.isOver();
                    analyzeBinary(bytes,name,fs);
                }
            }
        }
    }

    private void analyzeCharacter(String tmp){

        Pattern ptn;
        Matcher matcher;

        String name;
        String value;

        ptn = Pattern.compile(ANALYZE_POST_2_1);
        matcher = ptn.matcher(tmp);

        if(matcher.matches()){
            name = matcher.group(1);
            value =  matcher.group(2);

            setQuery(name,value);
        }
    }

    private void setQuery(String name,String value){
        Object obj;
        Boolean boolObject;

        boolean contentFlag;

        obj = query.get(name);
        if(obj == null){
            if(arrayToOne == null){
                contentFlag = false;
            }else{
                boolObject = arrayToOne.get(name);
                if(null == boolObject){
                    contentFlag = false;
                }else{
                    contentFlag = boolObject;
                }
            }
            if(!contentFlag){
                query.put(name,value);
            }else{
                prms = new ArrayList<Object>();
                prms.add(value);
                query.put(name,prms);
            }
        }else{
            if(obj instanceof String){
                prms = new ArrayList<Object>();
                prms.add(obj);
                prms.add(value);
            }
            if(obj instanceof ArrayList){
                prms = (ArrayList<Object>)obj;
                prms.add(value);
            }
            query.put(name,prms);
        }
    }

    private void analyzeBinary(byte[] bytes,String name,FileStorage fs){

        int readBytes;
        int limit;
        int temp_length;

        if(isOver){
            fs.setData(new byte[0]);
            fs.setOverMaxByteSize(true);
        }else{
            limit = 0;
            readBytes = 0;
            temp_length = bytes.length;

            for(int j=0;j<temp_length;j++){
                readBytes++;
                if(bytes[j] == b){
                    limit++;
                    if(limit == 3){
                        content = new byte[temp_length - readBytes - 2];
                        System.arraycopy(bytes,readBytes,content,0,temp_length - readBytes - 2);
                        break;
                    }
                }
            }
            fs.setData(content);
            fs.setOverMaxByteSize(false);
        }

        setQuery(name,fs);
    }

    /**
     *
     * @param name リクエストパラメータ名
     * @param fs ファイルストレージ
     */
    private void setQuery(String name,FileStorage fs){
        Object obj;
        Boolean boolObject;

        boolean contentFlag;

        obj = query.get(name);
        if(obj == null){
            if(arrayToOne == null){
                contentFlag = false;
            }else{
                boolObject = arrayToOne.get(name);
                if(boolObject == null){
                    contentFlag = false;
                }else{
                    contentFlag = boolObject;
                }
            }
            if(!contentFlag){
                query.put(name,fs);
            }else{
                prms = new ArrayList<Object>();
                prms.add(fs);
                query.put(name,prms);
            }
        }else{
            if(obj instanceof FileStorage){
                prms = new ArrayList<Object>();
                prms.add(obj);
                prms.add(fs);
            }
            if(obj instanceof ArrayList){
                prms = (ArrayList<Object>)obj;
                prms.add(fs);
            }
            query.put(name,prms);
        }
    }

    private void parseQueryString() throws IOException{
        Pattern ptn;
        Matcher matcher;

        String name;
        String value;

        ptn = Pattern.compile(ANALYZE_GET_1);
        matcher = ptn.matcher(qs);

        while(matcher.find()){
            name = matcher.group(1);
            value = matcher.group(2);
            value = URLDecode(value,getCharacterEncoding());

            setQuery(name,value);
        }
    }


    /**
     *
     * @param sis 入力
     * @param boundary バウンダリ
     * @return リクエスト配列
     * @throws java.io.IOException 例外
     */
    private ArrayList<Part> splitBytes(ServletInputStream sis,String boundary)
    throws IOException {

        boolean contentFlag;

        String tmp;
        int ret;

        byte[] buf;
        byte[] content_line;
        byte[] content_bk;
        int readBytes;

        ArrayList<Part> bar = new ArrayList<Part>();

        buf =  new byte[8*1024];
        contentFlag = false;
        readBytes = 0;

        while ((ret = sis.readLine(buf, 0, buf.length)) > -1) {
            content_line = new byte[ret];
            System.arraycopy(buf,0,content_line,0,ret);
            tmp = new String(content_line,ENC_ISO8859_1);

            if(tmp.indexOf(boundary) >= 0){
                if(contentFlag){
                    Part part = new Part();

                    part.setHeadBytes(content_head);

                    if(size_flg){
                        part.setOver(true);
                        part.setBytes(new byte[0]);
                    }else{
                        part.setOver(false);
                        part.setBytes(content);
                    }

                    bar.add(part);
                }

                contentFlag = true;
                size_flg = false;
                readBytes=0;
                content = new byte[0];
                rowCount = 0;
            }else{
                if(!size_flg){
                    content_bk = content;
                    readBytes += ret;
                    content = new byte[readBytes];

                    if(maxSize > 0 && readBytes > maxSize){
                        size_flg = true;
                    }else{
                        System.arraycopy(content_bk,0,
                                content,0,content_bk.length);
                        System.arraycopy(content_line,0,
                                content,content_bk.length,ret);

                        if(rowCount == 1){
                            content_head = new byte[readBytes];
                            System.arraycopy(content,0,
                                content_head,0,content.length);
                        }

                        if(rowCount < 2){
                            rowCount++;
                        }
                    }
                }
            }
        }
        sis.close();

        return bar;
    }

    /**
     *
     * URLエンコード文字列をデコードして返す
     *
     * @param str URLエンコード文字列
     * @param enc エンコーディング
     * @exception IllegalArgumentException
     * @throws java.io.UnsupportedEncodingException 例外
     * @return 文字列
     */
    private String URLDecode(String str, String enc)
            throws UnsupportedEncodingException  {

        byte[] bytes;

        if (str == null) return null;

        bytes = str.getBytes(ENC_ISO8859_1);

        return URLDecode(bytes, enc);

    }

    /**
     * URLエンコードバイト列をデコードして返す
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes URLエンコードバイト列
     * @param enc エンコーディング
     * @exception IllegalArgumentException
     * @return 文字列
     */
    private String URLDecode(byte[] bytes, String enc) {

        int len;
        byte b;
        int ix;
        int ox;

        if (bytes == null)
            return (null);

        len = bytes.length;
        ix = 0;
        ox = 0;
        while (ix < len) {
            b = bytes[ix++];     // Get byte to test
            if (b == '+') {
                b = (byte)' ';
            } else if (b == '%') {
                b = (byte) ((convertHexDigit(bytes[ix++]) << 4)
                        + convertHexDigit(bytes[ix++]));
            }
            bytes[ox++] = b;
        }
        if (enc != null) {
            try {
                return new String(bytes, 0, ox, enc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new String(bytes, 0, ox);

    }


    /**
     * バイト文字を16進数に変換する
     *
     * @param b バイト文字
     * @return バイト文字
     */
    private byte convertHexDigit( byte b ) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }

    protected class Part{
        private byte[] bytes;
        private byte[] headBytes;
        private boolean isOver;

        protected final void setBytes(byte[] bytes){
            this.bytes = bytes;
        }

        protected final byte[] getBytes(){
            return bytes;
        }

        protected final void setHeadBytes(byte[] headBytes){
            this.headBytes= headBytes;
        }

        protected final byte[] getHeadBytes(){
            return headBytes;
        }

        protected final void setOver(boolean isOver){
            this.isOver = isOver;
        }

        protected final boolean isOver(){
            return isOver;
        }
    }
}
