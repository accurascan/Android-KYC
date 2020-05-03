package com.accurascan.accurasdk.sample.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LivenessData implements Parcelable {

    public String livenessResult, livenessScore, glassesScore, glassesDecision, retryFeedbackSuggestion, message;

    protected LivenessData(Parcel in) {
        livenessResult = in.readString();
        livenessScore = in.readString();
        glassesScore = in.readString();
        glassesDecision = in.readString();
        retryFeedbackSuggestion = in.readString();
        message = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(livenessResult);
        dest.writeString(livenessScore);
        dest.writeString(glassesScore);
        dest.writeString(glassesDecision);
        dest.writeString(retryFeedbackSuggestion);
        dest.writeString(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LivenessData> CREATOR = new Creator<LivenessData>() {
        @Override
        public LivenessData createFromParcel(Parcel in) {
            return new LivenessData(in);
        }

        @Override
        public LivenessData[] newArray(int size) {
            return new LivenessData[size];
        }
    };
}
