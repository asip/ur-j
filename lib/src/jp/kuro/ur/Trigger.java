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

import java.util.HashMap;
import java.util.ArrayList;

/**
 * リクエストパラメータ検証クラス
 * @author Yasumasa Ashida
 * @version 0.9.0
 */
public final class Trigger {
    private HashMap<String,Object> vmap = new HashMap<String,Object>();
    private HashMap<String,Boolean> arrayToOne;

    private ArrayList<String> keyList = new ArrayList<String>();
    private HashMap<String,Object> detail = new HashMap<String,Object>();

    //テンポラリ変数
    private boolean bool;

    /**
     * バリデータを登録する
     * @param paramName パラメータ名
     * @param val Validatorオブジェクト
     */
    public final void add(String paramName,Validator val){
        if(!vmap.containsKey(paramName)){
            vmap.put(paramName,val);
            keyList.add(paramName);
        }else{
            Object obj = vmap.get(paramName);
            if(obj instanceof Validator){
                ArrayList<Validator> aryList = new ArrayList<Validator>();
                aryList.add((Validator)obj);
                aryList.add(val);
                //Validator[] aryList = new Validator[2];
                //aryList[0] = (Validator)obj;
                //aryList[1] = val;
                vmap.put(paramName,aryList);
            }
            else if(obj instanceof ArrayList){
                ArrayList<Validator> aryList = (ArrayList<Validator>)obj;
                aryList.add(val);
            }
            //else if(obj instanceof Validator[]){
            //    Validator[] aryList_back = (Validator[])obj;
            //    Validator[] aryList = new Validator[aryList_back.length + 1];
            //    System.arraycopy(aryList_back,0,aryList,0,aryList_back.length);
            //    aryList[aryList_back.length] = val;
            //    vmap.put(paramName,aryList);
            //}
        }
    }

    /** バリデータを用いてリクエストパラメータを検証する。<br>
     * 引数として渡されたRequestとバリデータを照合しパラメータマップに<br>
     * 格納されたデータの検証を行い、検証結果をResultオブジェクトに<br>
     * 格納して返します。
     * @param req Requestオブジェクト
     * @return Resultオブジェクト
     */
    public final Result validate(Request req){
        Result res;

        //String name;
        Object valObj;
        Object obj;

        arrayToOne = req.getArrayToOne();

        res = new Result();

        bool = true;

        for(String name : keyList){

            obj = req.getQuery().get(name);
            if(obj == null){
                return null;
            }

            valObj = vmap.get(name);

            if(!(obj instanceof ArrayList)){
                bool = validateNoneArray(name,obj,valObj);
            }else{
                bool = validateArray(name,obj,valObj);
            }

            req.getQuery().put(name,obj);
        }
        res.setDetail(detail);
        res.setResult(bool);

        return res;
    }

    private boolean validateNoneArray(String name,Object obj,Object val_obj){

        boolean contentFlag;
        Boolean boolObject;

        ArrayList<ParamResult> prs;
        ParamResult pr;

        pr = validateParameter(obj,val_obj);

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
            detail.put(name,pr);
        }else{
            prs = new ArrayList<ParamResult>();
            prs.add(pr);
            detail.put(name,prs);
        }

        return bool;
    }

    private boolean validateArray(String name,Object obj,Object val_obj){
        ArrayList<ParamResult> prs;
        ParamResult pr;

        prs =new ArrayList<ParamResult>();

        if(obj instanceof ArrayList){
            ArrayList<Object> prms;
            prms  = (ArrayList<Object>)obj;

            for(Object prm : prms){
                pr = validateParameter(prm,val_obj);
                prs.add(pr);
            }
            detail.put(name,prs);
        }
        else if(obj instanceof String[]){
            String[] strs = (String[])obj;
            for(String str : strs){
                pr = validateParameter(str,val_obj);
                prs.add(pr);
            }
            detail.put(name,prs);
        }
        else if(obj instanceof FileStorage[]){
            FileStorage[] fss = (FileStorage[])obj;
            for(FileStorage fs : fss){
                pr = validateParameter(fs,val_obj);
                prs.add(pr);
            }
            detail.put(name,prs);
        }

        return bool;
    }


    private ParamResult validateParameter(Object obj,Object val_obj){
        boolean b = false;
        ArrayList<Validator> aryList;
        //Validator[] aryList;
        ParamResult pr = null;

        if(val_obj instanceof Validator ){
            pr = ((Validator)val_obj).validate(obj);
            b =  pr.getResult();

            if(!b){
                bool = false;
            }
        }

        if(val_obj instanceof ArrayList){
            aryList = (ArrayList<Validator>)val_obj;
            for(Validator val : aryList){
                pr = val.validate(obj);
                b = pr.getResult();

                if(!b){
                    break;
                }
            }
            if(!b){
                bool = false;
            }
        }
        //if(val_obj instanceof Validator[]){
        //    aryList = (Validator[])val_obj;
        //    for(int j=0;j<aryList.length;j++){
        //        pr = aryList[j].validate(obj);
        //        b = pr.getResult();
        //
        //        if(!b){
        //            break;
        //        }
        //    }
        //    if(!b){
        //        bool = false;
        //    }
        //}

        return pr;
    }

}
