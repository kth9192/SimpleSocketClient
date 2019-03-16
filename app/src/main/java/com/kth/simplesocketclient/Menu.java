package com.kth.simplesocketclient;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Menu implements Parcelable {
    private String id;
    private String value;
    private List<MenuItem> popup;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.value);
        dest.writeTypedList(this.popup);
    }

    public Menu() {
    }

    protected Menu(Parcel in) {
        this.id = in.readString();
        this.value = in.readString();
        this.popup = in.createTypedArrayList(MenuItem.CREATOR);
    }

    public static final Parcelable.Creator<Menu> CREATOR = new Parcelable.Creator<Menu>() {
        @Override
        public Menu createFromParcel(Parcel source) {
            return new Menu(source);
        }

        @Override
        public Menu[] newArray(int size) {
            return new Menu[size];
        }
    };
}
