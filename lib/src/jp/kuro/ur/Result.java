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

/**
 * 検証結果保持クラス
 * @author Yasumasa Ashida
 * @version 0.9.0
 */
public class Result {
    private HashMap detail = new HashMap();
    private boolean result = false;

    /**
     * デフォルトコンストラクタ
     */
    public Result(){}

    protected final void setResult(boolean result){
        this.result = result;
    }

    /**
     * 検証結果を取得する
     * @return 検証結果
     */
    public final boolean result(){
        return result;
    }

    protected final void setDetail(HashMap detail){
        this.detail = detail;
    }

    /**
     * 検証結果の詳細情報をHashMapオブジェクトとして取得する。
     * この HashMapのキーはString、値はParamResultもしくはArrayList。
     * @return  HashMapオブジェクト、この HashMapのキーはString、値はParamResultもしくはArrayList
     */
    public final HashMap detail(){
        return detail;
    }

}
