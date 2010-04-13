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

/**パラメータ検証情報保持クラス
 * @author Yasumasa Ashida
 * @version 0.9.0
 */
public class ParamResult {
    private boolean result = false;
    private String message = null;

    /**
     * デフォルトコンストラクタ
     */
    public ParamResult(){}

    /**
     * パラメータ検証結果をセットする
     * @param result パラメータ検証結果
     */
    public final void setResult(boolean result){
        this.result = result;
    }

    /**
     * パラメータ検証結果を取得する
     * @return パラメータ検証結果
     */
    public final boolean getResult(){
        return result;
    }

    /**
     * メッセージをセットする
     * @param message メッセージ
     */
    public final void setMessage(String message){
        this.message = message;
    }

    /**
     * メッセージを取得する
     * @return メッセージ
     */
    public final String getMessage(){
        return message;
    }
}
