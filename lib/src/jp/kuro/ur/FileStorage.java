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


/**
 * ファイル情報保持クラス
 * @author Yasumasa Ashida
 * @version 0.9.0
 */
public final class FileStorage {
    private String uploadPath = null;
    private String uploadName = null;
    private String mimeType = null;
    private byte[] data = null;
    private boolean isOverMaxByteSize;

    /**
     * デフォルトコンストラクタ
     */
    public FileStorage(){}

    protected final void setUploadPath(String uploadPath){
        this.uploadPath = uploadPath;
    }

    /**
     * ファイルパスを取得する
     * @return ファイルパス
     */
    public final String getUploadPath(){
        return uploadPath;
    }

    protected final void setUploadName(String uploadName){
        this.uploadName = uploadName;
    }

    /**
     * ファイル名を取得する
     * @return  ファイル名
     */
    public final String getUploadName(){
        return uploadName;
    }

    protected final void setMimeType(String mimeType){
        this.mimeType = mimeType;
    }

    /**
     * MIMEタイプを取得する
     * @return MIMEタイプ
     */
    public final String getMimeType(){
        return mimeType;
    }

    protected final void setData(byte[] data){
        this.data = data;
    }

    /**
     * ファイル本体(バイトデータ)を取得する
     * @return ファイル本体(バイトデータ)
     */
    public final byte[] getData(){
        return data;
    }

    /**
     * 最大バイト長判定結果を取得する。
     * <br>設定したバイト長を超えていなければfalse、超えていればtrueを返す。
     * @return 最大バイト長判定結果
     */
    public boolean isOverMaxByteSize() {
        return isOverMaxByteSize;
    }

    protected void setOverMaxByteSize(boolean overMaxByteSize) {
        isOverMaxByteSize = overMaxByteSize;
    }

}
