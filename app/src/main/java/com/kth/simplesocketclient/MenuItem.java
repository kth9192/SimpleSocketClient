package com.kth.simplesocketclient;

import android.os.Parcel;
import android.os.Parcelable;

public class MenuItem implements Parcelable {
    private String value;
    private String onclick;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.value);
        dest.writeString(this.onclick);
    }

    public MenuItem() {
    }

    protected MenuItem(Parcel in) {
        this.value = in.readString();
        this.onclick = in.readString();
    }

    public static final Parcelable.Creator<MenuItem> CREATOR = new Parcelable.Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel source) {
            return new MenuItem(source);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };
}
